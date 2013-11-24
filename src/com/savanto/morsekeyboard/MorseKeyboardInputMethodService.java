package com.savanto.morsekeyboard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author savanto
 *
 */
@SuppressLint("DefaultLocale")
public class MorseKeyboardInputMethodService extends InputMethodService implements KeyboardView.OnKeyboardActionListener
{
	/**
	 * GUI elements
	 */
	private MorseKeyboardView keyboardView;
	private MorseKeyboard keyboard;
	private TextView morseView;
	private MorseKeyboard.Key shiftKey;
	private Drawable shifted;
	private Drawable unshifted;
	private boolean vibrate;
	private final long[] VIBRATE_PATTERN = { 0, 1 };

	/**
	 * Composition elements
	 */
	private MorseCode morseCode = new MorseCode();
	private StringBuilder morseString = new StringBuilder();
	private int morse;

	/**
	 * Key flags
	 */
	private boolean dah = false;
	private boolean mediumGap = false;
	private int pressedKey;
	private boolean shift;
	private boolean capslock;

	/**
	 * Timing
	 */
	private boolean timing;
	private long dahThreshold;
	private long signalStart;
	private GapTimer gapTimer = new GapTimer();
	private long shortGapLength;
	private long mediumGapLength;

	private SharedPreferences prefs;
	private Vibrator vibrator;

	/////////// Methods

	@Override
	public void onCreate()
	{
		super.onCreate();

		Resources r = this.getResources();
		this.unshifted = r.getDrawable(R.drawable.key_unshifted);
		this.shifted = r.getDrawable(R.drawable.key_shifted);

		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
	}

	@Override
	public View onCreateInputView()
	{
		this.keyboardView = (MorseKeyboardView) this.getLayoutInflater()
				.inflate(R.layout.input, null);
		this.keyboardView.setOnKeyboardActionListener(this);
		this.keyboard = new MorseKeyboard(this, R.xml.morse);
		this.keyboardView.setKeyboard(this.keyboard);
		this.shiftKey = this.keyboard.getKeys().get(this.keyboard.getShiftKeyIndex());
		return this.keyboardView;
	}

	@Override
	public View onCreateCandidatesView()
	{
		this.morseView = (TextView) this.getLayoutInflater().inflate(R.layout.compose, null);
		return this.morseView;
	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting)
	{
		super.onStartInput(attribute, restarting);

		// Get initial shift state from preferences
		if (! restarting)
		{
			this.shift = this.prefs.getBoolean(this.getString(R.string.pref_key_autoshift), false);
			this.capslock = this.prefs.getBoolean(this.getString(R.string.pref_key_autocaps), false);
			this.vibrate = this.prefs.getBoolean(this.getString(R.string.pref_key_vibrate), false);
		}
		else
		{
			// Update shift key icon.
			if (this.shift || this.capslock)
				this.shiftKey.icon = this.shifted;
			else
				this.shiftKey.icon = this.unshifted;

			this.keyboardView.invalidateKey(this.keyboard.getShiftKeyIndex());
		}

		// Timing info from preferences
		this.timing = this.prefs.getBoolean(this.getString(R.string.pref_key_realistic), false);
		this.dahThreshold = this.prefs.getLong(this.getString(R.string.pref_key_dah), 300) * 1000000;
		final long ditLength = this.dahThreshold / 3;
		this.shortGapLength = this.prefs.getLong(this.getString(R.string.pref_key_short_gap), 3) * ditLength;
		this.mediumGapLength = this.prefs.getLong(this.getString(R.string.pref_key_medium_gap), 7) * ditLength;

		this.resetMorse();
		this.updateMorseView();
	}

	@Override
	public void onFinishInput()
	{
		super.onFinishInput();

		if (this.keyboardView != null)
			this.keyboardView.closing();
	}

	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd,
			int newSelStart, int newSelEnd,
			int candidatesStart, int candidatesEnd)
	{
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
				candidatesStart, candidatesEnd);

		// If the current selection in the text view changes, we should
		// clear whatever candidate text we have.
		if (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)
		{
			this.resetMorse();

			InputConnection ic = this.getCurrentInputConnection();
			if (ic != null)
				ic.finishComposingText();
		}
	}

	// This seems to be for "hard" keys only
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				// The InputMethodService already takes care of the back
				// key for us, to dismiss the input method if it is shown.
				// However, our keyboard could be showing a pop-up window
				// that back should dismiss, so we first allow it to do that.
				if (event.getRepeatCount() == 0 && this.keyboardView != null)
				{
					if (this.keyboardView.handleBack())
						return true;
				}
				break;

			case KeyEvent.KEYCODE_DEL:
				// Special handling of the delete key: if we are currently
				// composing text for the user, we want to modify that instead
				// of letting the application do the delete itself.
				if (this.morse == 0)
				{
					this.onKey(Keyboard.KEYCODE_DELETE, null);
					return true;
				}
				break;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void updateMorseView()
	{
		if (this.morseView != null)
		{
			if (this.morse > 0)
				this.setCandidatesViewShown(true);
			this.morseView.setText(this.morseString.toString());
		}
	}

	private void resetMorse()
	{
		this.morseString.setLength(0);
		this.morse = 0;
	}

	/**
	 * Appends a dit or a dah to the morse character being composed,
	 * if it is possible, and returns true. Returns false if append failed.
	 * @param signal - can be only MorseKeyboard.KEYCODE_DIT or MorseKeyboard.KEYCODE_DAH
	 * @return true if success, false otherwise.
	 */
	private boolean appendMorse(int signal)
	{
		// Morse characters may not be more than 8 elements long
		if (this.morseString.length() >= 8)
		{
			Toast.makeText(this, this.getString(R.string.error_morse_length_exceeded), Toast.LENGTH_SHORT).show();
			return false;
		}

		switch (signal)
		{
			case MorseKeyboard.KEYCODE_DIT:
				this.morseString.append('.');
				this.morse = this.morse << 2 | MorseKeyboard.KEYCODE_DIT;
				break;
			case MorseKeyboard.KEYCODE_DAH:
				this.morseString.append('-');
				this.morse = this.morse << 4 | MorseKeyboard.KEYCODE_DAH;
				break;
			default:
				return false;
		}
		this.updateMorseView();
		return true;
	}

	private void deleteMorse()
	{
		if (this.morse > 0)
		{
			this.morseString.delete(this.morseString.length() - 1, this.morseString.length());
			// Check if last character is a dit or a dah, to know how many bits to lop off.
			if ((this.morse & MorseKeyboard.KEYCODE_DAH) == MorseKeyboard.KEYCODE_DAH)
				this.morse = this.morse >> 4;
			else if ((this.morse & MorseKeyboard.KEYCODE_DIT) == MorseKeyboard.KEYCODE_DIT)
				this.morse = this.morse >> 2;

			this.updateMorseView();
		}
		else
		{
			this.getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
			this.getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
		}
	}

	private void updateShift(boolean shiftPressed)
	{
		// Update shift due to user pressing shift.
		if (shiftPressed)
		{
			if (this.capslock)
			{
				this.shift = false;
				this.capslock = false;
			}
			else
			{
				this.capslock = this.shift;
				this.shift = ! this.shift;
			}
		}
		else // Update shift due to typing.
			this.shift = false;

		// Update shift key icon.
		if (this.shift || this.capslock)
			this.shiftKey.icon = this.shifted;
		else
			this.shiftKey.icon = this.unshifted;

		this.keyboardView.invalidateKey(this.keyboard.getShiftKeyIndex());
	}

	/* Implementation of KeyboardView.OnKeyboardActionListener */

	/**
	 * Set the flags for alternate key actions on long-press.
	 * @param primaryCode - the code of the alternate key.
	 */
	public void onLongKey(int primaryCode)
	{
		switch (primaryCode)
		{
			case MorseKeyboard.KEYCODE_DAH:
				this.dah = true;
				break;
			case MorseKeyboard.KEYCODE_MEDIUM_GAP:
				this.mediumGap = true;
				break;
		}
	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes)
	{
		switch (primaryCode)
		{
			case MorseKeyboard.KEYCODE_SETTINGS:
				this.getApplication().startActivity(new Intent(this.getBaseContext(), MorseKeyboardSettings.class)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				break;

			case MorseKeyboard.KEYCODE_REFERENCE:
				AlertDialog ref = new AlertDialog.Builder(this)
					.setView(this.getLayoutInflater().inflate(R.layout.reference, null))
					.setTitle(R.string.reference_title)
					.setMessage(R.string.reference_message)
					.setNeutralButton("Ok", new DialogInterface.OnClickListener()
						{ @Override public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); } })
					.create();
				
				Window window = ref.getWindow();
				WindowManager.LayoutParams lp = window.getAttributes();
				lp.token = this.keyboardView.getWindowToken();
				lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
				window.setAttributes(lp);
				window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				
				ref.show();

				break;

			case Keyboard.KEYCODE_SHIFT:
				this.updateShift(true);
				break;

			case Keyboard.KEYCODE_DELETE:
				this.deleteMorse();
				break;

			case MorseKeyboard.KEYCODE_GAP:
				// Determine the gap to produce.
				// If we are composing, gap is short -- between letters only.
				// If we are not composing, gap is medium -- between words.

				// Short gap: send current letter.
				if (this.morse > 0)
				{
					String s = this.morseCode.lookup(this.morse);
					if (s == null)
					{
						Toast.makeText(this, this.getString(R.string.error_morse_not_recognized), Toast.LENGTH_SHORT).show();
						return;
					}

					// Check for capitalization
					if (this.shift || this.capslock)
						s = s.toUpperCase();

					// Check for medium gap override set from long-press
					if (this.mediumGap)
						s += " ";

					this.getCurrentInputConnection().commitText(s, 1);
					this.resetMorse();
					this.updateMorseView();
				}
				// Medium gap: send space
				else
					this.getCurrentInputConnection().commitText(Character.toString(' '), 1);

				this.mediumGap = false;
				this.updateShift(false);
				
				break;

			case MorseKeyboard.KEYCODE_ENTER:
				if (this.morse > 0)
				{
					String s = this.morseCode.lookup(this.morse);
					if (s == null)
					{
						Toast.makeText(this, this.getString(R.string.error_morse_not_recognized), Toast.LENGTH_SHORT).show();
						return;
					}

					// Check for capitalization
					if (this.shift || this.capslock)
						s = s.toUpperCase();

					this.getCurrentInputConnection().commitText(s, 1);
					this.resetMorse();
					this.updateMorseView();
				}

				this.getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
				this.getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));

				this.mediumGap = false;
				this.updateShift(false);

				break;

			case MorseKeyboard.KEYCODE_SIGNAL:
				if (! this.timing)
				{
					if (this.dah)
						this.appendMorse(MorseKeyboard.KEYCODE_DAH);
					else
						this.appendMorse(MorseKeyboard.KEYCODE_DIT);
					this.dah = false;
				}
				break;
		}
	}

	@Override
	public void onPress(int primaryCode)
	{
		// Begin vibration
		if (this.vibrate && primaryCode == MorseKeyboard.KEYCODE_SIGNAL)
			this.vibrator.vibrate(this.VIBRATE_PATTERN, 0);

		this.pressedKey = primaryCode;
		// Record signal start time
		this.signalStart = System.nanoTime();
		// Cancel gap timer
		this.gapTimer.cancel(true);
	}

	@Override
	public void onRelease(int primaryCode)
	{
		// Cancel vibration
		this.vibrator.cancel();

		if (primaryCode != this.pressedKey)
			return;

		if (this.timing && primaryCode == MorseKeyboard.KEYCODE_SIGNAL)
		{
			// Check signal length and decide if it's a dit or dah
			if (System.nanoTime() - this.signalStart < this.dahThreshold)
				this.appendMorse(MorseKeyboard.KEYCODE_DIT);
			else
				this.appendMorse(MorseKeyboard.KEYCODE_DAH);

			// Start the gap timer.
			this.gapTimer = new GapTimer();
			this.gapTimer.execute();
		}
	}

	private class GapTimer extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params)
		{
			// Time short gap
			try
			{
				Thread.sleep(MorseKeyboardInputMethodService.this.shortGapLength / 1000000);
			}
			catch (InterruptedException e)
			{
				this.cancel(true);
			}

			if (! this.isCancelled())
				this.publishProgress();

			// Time medium gap
			try
			{
				Thread.sleep((MorseKeyboardInputMethodService.this.mediumGapLength 
						- MorseKeyboardInputMethodService.this.shortGapLength) / 1000000);
			}
			catch (InterruptedException e)
			{
				this.cancel(true);
			}

			if (! this.isCancelled())
				return true;

			return false;
		}

		@Override
		protected void onProgressUpdate(Void... progress)
		{
			// Short gap: send current letter.
			if (MorseKeyboardInputMethodService.this.morse > 0)
			{
				String s = MorseKeyboardInputMethodService.this.morseCode.lookup(MorseKeyboardInputMethodService.this.morse);
				if (s == null)
				{
					Toast.makeText(MorseKeyboardInputMethodService.this,
							MorseKeyboardInputMethodService.this.getString(R.string.error_morse_not_recognized), 
							Toast.LENGTH_SHORT).show();
					this.cancel(true);
					return;
				}

				// Check for capitalization
				if (MorseKeyboardInputMethodService.this.shift 
						|| MorseKeyboardInputMethodService.this.capslock)
					s = s.toUpperCase();

				MorseKeyboardInputMethodService.this.getCurrentInputConnection().commitText(s, 1);
				MorseKeyboardInputMethodService.this.resetMorse();
				MorseKeyboardInputMethodService.this.updateMorseView();
			}
			// Medium gap: send space
			else
			{
				MorseKeyboardInputMethodService.this.getCurrentInputConnection().commitText(Character.toString(' '), 1);
				this.cancel(true);
			}
			MorseKeyboardInputMethodService.this.updateShift(false);
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			// Timer completed: send medium gap
			if (result)
			{
				// If timer completes, short gap has already been sent,
				// along with any composition text.
				// Only need to send medium gap (space).
				MorseKeyboardInputMethodService.this.getCurrentInputConnection().commitText(Character.toString(' '), 1);
			}
		}
	}
	
	@Override
	public void onText(CharSequence arg0)
	{ }

	@Override
	public void swipeDown()
	{ }

	@Override
	public void swipeLeft()
	{ }

	@Override
	public void swipeRight()
	{ }

	@Override
	public void swipeUp()
	{ }
}

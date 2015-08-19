package com.savanto.morsekeyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;

/**
 * @author savanto
 *
 */
public class MorseKeyboard extends Keyboard
{
	public static final int KEYCODE_DIT = 1;	// 0b1
	public static final int KEYCODE_DAH = 7;	// 0b111
	public static final int KEYCODE_SIGNAL = -10;
	public static final int KEYCODE_SHORT_GAP = -30;
	public static final int KEYCODE_MEDIUM_GAP = -70;

	public static final int KEYCODE_SETTINGS = -100;
	public static final int KEYCODE_REFERENCE = -101;

	public static final int KEYCODE_ENTER = 10;
	public static final int KEYCODE_GAP = 32;

	/**
	 * @param context
	 * @param xmlLayoutResId
	 */
	public MorseKeyboard(Context context, int xmlLayoutResId)
	{
		super(context, xmlLayoutResId);
	}

	/**
	 * @param context
	 * @param xmlLayoutResId
	 * @param modeId
	 */
	public MorseKeyboard(Context context, int xmlLayoutResId, int modeId)
	{
		super(context, xmlLayoutResId, modeId);
	}
}

package com.savanto.morsekeyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


public class SeekBarPreference extends DialogPreference {
    private final Context mContext;
    private TextView mValueTextView;

    private final String mLabel;
    private final String mUnits;
    private final int mMaxValue;
    private final int mMinValue;
    private final int mDefaultValue;
    private int mProgressValue;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        // Get StyleAttributes properly.
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);

        // Get SeekBar min/max/default values.
        this.mMinValue = ta.getInt(R.styleable.SeekBarPreference_minValue, 0);
        this.mMaxValue = ta.getInt(R.styleable.SeekBarPreference_maxValue, 100);
        this.mDefaultValue = ta.getInt(
                R.styleable.SeekBarPreference_android_defaultValue, this.mMinValue);

        // Get the Dialog and SeekBar labels text.
        this.mLabel = ta.getString(R.styleable.SeekBarPreference_android_dialogMessage);
        this.mUnits = ta.getString(R.styleable.SeekBarPreference_units);

        // Destroy the TypedArray
        ta.recycle();
    }

    @Override
    protected View onCreateDialogView() {
        // Get the current value from saved preferences.
        this.mProgressValue = (int) this.getPersistedLong(this.mDefaultValue);

        // Inflate layout
        final LayoutInflater inflater = LayoutInflater.from(this.mContext);
        final View view = inflater.inflate(R.layout.seek_bar_preference, null);

        // Setup the info message.
        ((TextView) view.findViewById(R.id.seek_bar_info)).setText(this.mLabel);

        // Setup the SeekBar min/max labels.
        ((TextView) view.findViewById(R.id.seek_bar_min)).setText(Integer.toString(this.mMinValue));
        ((TextView) view.findViewById(R.id.seek_bar_max)).setText(Integer.toString(this.mMaxValue));

        // Setup the SeekBar.
        final SeekBar seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        seekBar.setMax(this.mMaxValue - this.mMinValue);
        seekBar.setProgress(this.mProgressValue - this.mMinValue);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromTouch) {
                SeekBarPreference.this.mProgressValue = value + SeekBarPreference.this.mMinValue;
                SeekBarPreference.this.mValueTextView.setText(
                        Integer.toString(SeekBarPreference.this.mProgressValue)
                                + SeekBarPreference.this.mUnits);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { /* NOP */ }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { /* NOP */ }
        });

        // Setup SeekBar current value label.
        this.mValueTextView = (TextView) view.findViewById(R.id.seek_bar_value);
        this.mValueTextView.setText(
                Integer.toString(SeekBarPreference.this.mProgressValue)
                        + SeekBarPreference.this.mUnits);

        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            if (this.shouldPersist()) {
                this.persistLong(this.mProgressValue);
            }
            this.notifyChanged();
        }
    }

    public String getUnits() {
        return this.mUnits;
    }

    public int getProgress() {
        return this.mProgressValue;
    }

    public int getDefaultValue() {
        return this.mDefaultValue;
    }
}

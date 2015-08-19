package com.savanto.morsekeyboard;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


public class MorseKeyboardSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	private SharedPreferences prefs;

	private SeekBarPreference dah;
	private SeekBarPreference shortGap;
	private SeekBarPreference mediumGap;

	private CheckBoxPreference autoshift;
	private CheckBoxPreference autocaps;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Add preferences from resources
		this.addPreferencesFromResource(R.xml.settings);
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);

		// Get references to the preferences
		this.dah = (SeekBarPreference) this.getPreferenceScreen().findPreference(this.getText(R.string.pref_key_dah));
		this.shortGap = (SeekBarPreference) this.getPreferenceScreen().findPreference(this.getText(R.string.pref_key_short_gap));
		this.mediumGap = (SeekBarPreference) this.getPreferenceScreen().findPreference(this.getText(R.string.pref_key_medium_gap));

		this.autoshift = (CheckBoxPreference) this.getPreferenceScreen().findPreference(this.getText(R.string.pref_key_autoshift));
		this.autocaps = (CheckBoxPreference) this.getPreferenceScreen().findPreference(this.getText(R.string.pref_key_autocaps));

		// Get the shared preferences
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// Set initial dynamic summary values of preferences
		this.dah.setSummary(Long.toString(this.prefs.getLong(this.dah.getKey(),
				this.dah.getDefaultValue())) + this.dah.getUnits());
		this.shortGap.setSummary(Long.toString(this.prefs.getLong(this.shortGap.getKey(),
				this.shortGap.getDefaultValue())) + this.shortGap.getUnits());
		this.mediumGap.setSummary(Long.toString(this.prefs.getLong(this.mediumGap.getKey(),
				this.mediumGap.getDefaultValue())) + this.mediumGap.getUnits());

		// Set initial capitalization checkboxes to exclude eachother
		this.autoshift.setEnabled(! this.autocaps.isChecked());
		this.autocaps.setEnabled(! this.autoshift.isChecked());

		// Register listener for preference changes to update summaries
		this.prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		// Unregister preference change listener
		this.prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
	{
		Preference pref = this.findPreference(key);
		if (pref instanceof SeekBarPreference)
		{
			SeekBarPreference seekBarPref = (SeekBarPreference) pref;
			pref.setSummary(Integer.toString(seekBarPref.getProgress()) + seekBarPref.getUnits());
		}
		else if (key == this.getString(R.string.pref_key_autoshift))
		{
			if (this.autoshift.isChecked())
			{
				this.autocaps.setChecked(false);
				this.autocaps.setEnabled(false);
			}
			else
				this.autocaps.setEnabled(true);
		}
		else if (key == this.getString(R.string.pref_key_autocaps))
		{
			if (this.autocaps.isChecked())
			{
				this.autoshift.setChecked(false);
				this.autoshift.setEnabled(false);
			}
			else
				this.autoshift.setEnabled(true);
		}
	}
}

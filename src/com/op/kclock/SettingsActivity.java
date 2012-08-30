package com.op.kclock;

import com.op.kclock.cookconst.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.preference.Preference.OnPreferenceClickListener;
import android.content.pm.*;
import com.op.kclock.misc.*;
import android.media.*;
import com.op.kclock.settings.*;

public class SettingsActivity extends PreferenceActivity {

	private SharedPreferences prefs;
	public static final String CUSTOM_SOUNDFILE_KEY = "pref_soundfile_key";
	private static final int REQUEST_PICK_FILE = 4;

	private CheckBoxPreference isSessSavePref;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setVolumeControlStream(AudioManager.STREAM_NOTIFICATION);
		addPreferencesFromResource(R.xml.settings);

		String version;
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(MainActivity.TAG, "Package name not found", e);
			version = getString(R.string.pref_info_version_error);
		}
		findPreference(getString(R.string.pref_info_version_key)).setSummary(
				version);

		findPreference(getString(R.string.pref_info_version_key)).setSummary(
				version);

		// add onclick for select shader
		Preference customPref = findPreference(CUSTOM_SOUNDFILE_KEY);
		if (customPref != null) {
			customPref
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {


						public boolean onPreferenceClick(Preference preference) {
							// spawn the file chooser
							Intent fileChooserI = new Intent(
									SettingsActivity.this,
									FileChooserActivity.class);
							startActivityForResult(fileChooserI,
									REQUEST_PICK_FILE);
							// startActivity(fileChooserI);
							return true;
						}
					});
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case REQUEST_PICK_FILE:
					if(data.hasExtra(FileChooserActivity.EXTRA_FILE_PATH)) {
						findViewById(R.string.pref_soundfile_key);
						String filePath = data.getStringExtra(FileChooserActivity.EXTRA_FILE_PATH);
						
						//this.getApplicationContext().gets
						Preference customPref = findPreference(CUSTOM_SOUNDFILE_KEY);
						customPref.setTitle(filePath);
						customPref.setDefaultValue("123");
					}
			}
		}
	}
}

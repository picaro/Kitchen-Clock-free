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

public class SettingsActivity extends PreferenceActivity
{

    private SharedPreferences prefs;

	private CheckBoxPreference isSessSavePref;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_NOTIFICATION);
		addPreferencesFromResource(R.xml.settings);

		String version;
		try
		{	
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);		
			version = pi.versionName;
		}
		catch (PackageManager.NameNotFoundException e)
		{
			Log.e(MainActivity.TAG, "Package name not found", e);
			version = getString(R.string.pref_info_version_error);
		}		findPreference(getString(R.string.pref_info_version_key)).setSummary(version);

		findPreference(getString(R.string.pref_info_version_key)).setSummary(version);


		// add onclick for select shader
		Preference customPref = findPreference("pref_data_file_key");
		if (customPref != null)
		{ 
			customPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
				{
					public boolean onPreferenceClick(Preference preference)
					{                                       
						// spawn the file chooser
						Intent fileChooserI = new Intent(this, FileChooserActivity.class); 
						//startActivityForResult(intent, REQUEST_PICK_FILE);
						startActivity(fileChooserI);

						return true;
					}
				});
		}

    }
}

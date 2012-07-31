package com.op.kclock;

import com.op.kclock.cookconst.*;
import android.content.*;
import android.os.*;
import android.preference.*;

public class SettingsActivity extends PreferenceActivity
{

    private SharedPreferences prefs;
	
	private CheckBoxPreference isSessSavePref;
  	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.settings);
		addPreferencesFromResource(R.xml.settings);

		prefs = getSharedPreferences(SettingsConst.SETTINGS, 0);
		
		isSessSavePref = (CheckBoxPreference) findPreference( SettingsConst.SAVE_SESSION);
    }
}

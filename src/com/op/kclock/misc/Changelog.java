/**
 *  Kitchen Clock
 *  Copyright (C) 2012 Alexander Pastukhov
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */ 

package com.op.kclock.misc;


import com.op.kclock.R;
import com.op.kclock.cookconst.SettingsConst;
//import com.op.kclock.settings.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import com.op.kclock.utils.*;

public class Changelog {
	private static final String TAG = "Changelog";

	public static boolean show(Activity activity) {
		SharedPreferences preferences = activity.getSharedPreferences(SettingsConst.PREF_CHANGELOG, Activity.MODE_PRIVATE);
		int prefVersion = preferences.getInt(SettingsConst.PREF_APP_VERSION, 1);
		int currentVersion;
		try {
			PackageInfo pi = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 1);
			currentVersion = pi.versionCode;
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Package name not found", e);
			return false;
		}
		if (prefVersion != 0) {
			if (currentVersion > prefVersion) {
				showChangelogDialog(activity);
			}
		}
		preferences.edit().putInt(SettingsConst.PREF_APP_VERSION, currentVersion).commit();
		return true;
	}

	protected static void showChangelogDialog(Activity activity) {
		new AlertDialog.Builder(activity)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setTitle(R.string.changelog_title)
		.setView(Utils.dialogWebView(activity, activity.getString(R.string.changelog_filename)))
		.setPositiveButton(R.string.ok, null)
		.show();
	}
}

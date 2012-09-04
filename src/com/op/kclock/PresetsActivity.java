package com.op.kclock;

import java.util.List;

import com.op.kclock.model.AlarmClock;
import com.op.kclock.utils.HistoryDAO;

import android.app.Activity;
import android.os.*;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PresetsActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//Bundle savedInstanceState = null;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.presets);
		LinearLayout presetsList = (LinearLayout)findViewById(R.id.presets_list);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		TextView preset = new TextView(getApplicationContext());
		//for(){		
		//}
		preset.setText("123");
		presetsList.addView(preset);
		
		HistoryDAO historyDAO = new HistoryDAO(getApplicationContext());
		List<AlarmClock> histories = historyDAO.getAlarmsList();
	}


}

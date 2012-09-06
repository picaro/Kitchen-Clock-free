package com.op.kclock;

import java.util.List;

import com.op.kclock.model.AlarmClock;
import com.op.kclock.utils.HistoryDAO;

import android.app.Activity;
import android.os.*;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.op.kclock.utils.*;

public class PresetsActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//Bundle savedInstanceState = null;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.presets);
		LinearLayout presetsList = (LinearLayout)findViewById(R.id.presets_list);
	//	LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		
		PresetDAO presetDAO = new PresetDAO(getApplicationContext());
		presetDAO.open();
		List<AlarmClock> presets = presetDAO.getList();
		for(AlarmClock alarm : presets){
		TextView preset = new TextView(getApplicationContext());
		String name = (alarm.getName() == null || alarm.getName().length() == 0)?
		"alarm-":alarm.getName(); 	
		preset.setText(name + "-" + alarm.toString());
			presetsList.addView(preset);
		}
		presetDAO.close();			
		
		
		LinearLayout logsList = (LinearLayout)findViewById(R.id.logs_list);
		HistoryDAO historyDAO = new HistoryDAO(getApplicationContext());
		historyDAO.open();
		List<AlarmClock> histories = presetDAO.getList();
		for(AlarmClock alarm : histories){
			TextView preset = new TextView(getApplicationContext());
			preset.setText("-"+alarm.getHour());
			logsList.addView(preset);
			
		}
		
				TextView preset = new TextView(getApplicationContext());
			preset.setText("-");
			logsList.addView(preset);
		
		historyDAO.close();
	}


}

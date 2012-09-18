package com.op.kclock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.op.kclock.model.AlarmClock;

import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.op.kclock.utils.*;
import com.op.kclock.dialogs.TimePickDialog;
import android.view.*;
import com.op.kclock.utils.DBHelper;

public class PresetsActivity extends Activity implements OnClickListener
{

	/** Called when the activity is first created. */
	private List<AlarmClock> histories  = null;
	private List<AlarmClock> presets = null;
	private final Map<View, AlarmClock> historyMap = new HashMap<View, AlarmClock>();
	//private Map<View, AlarmClock> presetsMap = new HashMap<View, AlarmClock>();
	private GestureDetector gestureDetector;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		//Bundle savedInstanceState = null;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.presets);
		final LinearLayout presetsList = (LinearLayout)findViewById(R.id.presets_list);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		DBHelper dbHelper = new DBHelper(getApplicationContext());
		dbHelper.open();

		//Show presets list
		presets = dbHelper.getPresetsList();
		for (final AlarmClock alarm : presets)
		{
			
			String name = (alarm.getName() == null || alarm.getName().length() == 0) ?
				"alarm": alarm.getName(); 	
			final View convertView = inflater.inflate(R.layout.preset_unit, null);
			final TextView tvName = (TextView) convertView.findViewById(R.id.name);
			tvName.setText(name);
			final TextView tvTime = (TextView) convertView.findViewById(R.id.time);
			tvTime.setText(alarm.toString());
			convertView.setOnClickListener(this);
			final View tvDel = convertView.findViewById(R.id.delpreset);
   			tvDel.setOnClickListener(new View.OnClickListener(){

					public void onClick(View p2)
					{
						View p1 = (View)p2.getParent();
						presetsList.removeView(p1);
						for (AlarmClock alarm : presets)
						{
							if( alarm.getElement() == p1){
								presets.remove(alarm);
								DBHelper dbHelper = new DBHelper(getApplicationContext());
								dbHelper.open();
								dbHelper.deletePreset(alarm.getId());
								dbHelper.close();
								break;
							}
						}
						historyMap.remove(p1);
				
				
					}

				
			});
			presetsList.addView(convertView, 0);
			historyMap.put(convertView, alarm);

			// On long click on preset - show edit dialog
			convertView.setOnLongClickListener(new View.OnLongClickListener() { 
					@Override
					public boolean onLongClick(View v)
					{
						TimePickDialog timePickDialog = null;	
						timePickDialog = new TimePickDialog(PresetsActivity.this);
						timePickDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
						timePickDialog.setAlarm(alarm);

						timePickDialog.setDialogResult(new TimePickDialog.OnMyDialogResult() {
								public void finish(AlarmClock newAlarm)
								{
									tvName.setText(alarm.getName());
									tvTime.setText(alarm.toString());
								}
							});
						timePickDialog.show();
						return true;
					}
    			});

		}

		// show logs list
		LinearLayout logsList = (LinearLayout)findViewById(R.id.logs_list);
		histories = dbHelper.getHistoryList();
		for (AlarmClock alarm : histories)
		{
			String name = (alarm.getName() == null || alarm.getName().length() == 0) ?
				"alarm": alarm.getName(); 	
			View convertView = inflater.inflate(R.layout.hist_unit, null);
			TextView tvName = (TextView) convertView.findViewById(R.id.name);
			tvName.setText(name);
			TextView tvTime = (TextView) convertView.findViewById(R.id.time);
			tvTime.setText(alarm.toString());
			convertView.setOnClickListener(this);
			logsList.addView(convertView, 0);
			historyMap.put(convertView, alarm);
		}

		Intent mainActivity = new Intent(this, MainActivity.class);
		mainActivity.putExtra("alarm_extra", "");
		dbHelper.close();
	}

	@Override
	public void onClick(View v)
	{
		AlarmClock alarm = (AlarmClock)historyMap.get(v);
		Intent mainActivity = new Intent(this, MainActivity.class);
		//	mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mainActivity.putExtra("alarm_extra", alarm);
		startActivity(mainActivity);

	}

	
	class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			Intent intent = new Intent(PresetsActivity.this.getBaseContext(), MainActivity.class);

            if (Math.abs(e1.getY() - e2.getY()) > MainActivity.SWIPE_MAX_OFF_PATH) {
                return false;
            }

            // left to right swipe
			if (e2.getX() - e1.getX() > MainActivity.SWIPE_MIN_DISTANCE && Math.abs(velocityX) > MainActivity.SWIPE_THRESHOLD_VELOCITY) {
			 startActivity(intent);
			 PresetsActivity.this.overridePendingTransition(
			 R.anim.slide_in_left, 
			 R.anim.slide_out_right
			 );
			}

            return false;
        }

        // It is necessary to return true from onDown for the onFling event to register
        @Override
        public boolean onDown(MotionEvent e) {
			return true;
        }

    }

}

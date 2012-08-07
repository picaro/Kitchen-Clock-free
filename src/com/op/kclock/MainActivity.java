package com.op.kclock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;

import com.op.kclock.alarm.AlarmService;
import com.op.kclock.alarm.AlarmServiceImpl;
import com.op.kclock.alarm.WakeUpLock;
import com.op.kclock.cookconst.SettingsConst;
import com.op.kclock.dialogs.TimePickDialog;
import com.op.kclock.misc.Log;
import com.op.kclock.model.AlarmClock;
import com.op.kclock.music.MusicHandler;
import com.op.kclock.ui.TextViewWithMenu;
import com.op.kclock.misc.*;


public class MainActivity extends Activity implements OnClickListener
{

    private MusicHandler music = null;
	private Handler handler ;    


	LinearLayout mainL = null; 
	public final static String TAG = "AlarmaClockActivity";
	NotificationManager mNotificationManager;
//	private SharedPreferences mPrefs;

	private ArrayList<AlarmClock> alarmList = new ArrayList<AlarmClock>();

	TextView tvOut;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarmclock);

//		ActionBar actionBar = getActionBar();
		//actionBar.setCustomView(R.layout.actionbar_view);
//		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM 
//			| ActionBar.DISPLAY_SHOW_HOME);
	 

		Eula.show(this); 
       		Changelog.show(this);

		if (alarmList == null) alarmList  = new ArrayList<AlarmClock>();   

		tvOut = (TextView) findViewById(R.id.tvOut);
		if (alarmList.size() > 0)
		{ 
			drawAlarms();
		}
		else
		{
			addAlarmDialog();
		} 	
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		alarmList	= savedInstanceState.getParcelableArrayList("SAVE_SELECTED");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		savedInstanceState.putParcelableArrayList("SAVE_SELECTED", alarmList);
		super.onSaveInstanceState(savedInstanceState);
	}
	
	private void drawAlarms()
	{
		//get alarms from settings
		for (AlarmClock alarm : alarmList)
		{
			//draw alarm
			drawAlarm(alarm);
		}
	}



	private void addAlarm()
	{
		addAlarm(new AlarmClock());
	}

	private void addAlarm(AlarmClock newAlarm)
	{
		if (newAlarm.getElement() == null)
		{
			drawAlarm(newAlarm);
			alarmList.add(newAlarm);
		}
	}

	private LinearLayout drawAlarm(AlarmClock alarm)
	{
		mainL = (LinearLayout) findViewById(R.id.alarm_layout);
		LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

		LinearLayout itemView = (LinearLayout) inflater.inflate(R.layout.alarm_incl, null);
		alarm.setElement(itemView);
		((TextViewWithMenu)(alarm.getWidget())).setAlarm(alarm);
		//alarm.setTimeFromWidget();
		alarm.updateElement();
		itemView.setOnClickListener(this);

		AlarmService alarmService = new AlarmServiceImpl(this, 100, handler);
		alarmService.setAlarmClock(alarm);
		alarm.setState(AlarmClock.TimerState.RUNNING);
		new Thread(alarmService).start();



		registerForContextMenu(alarm.getWidget());
		//add the itemView
		mainL.addView(itemView, new TableLayout.LayoutParams(
						  LayoutParams.FILL_PARENT, 
						  LayoutParams.WRAP_CONTENT));
		return itemView;
	}

	// ON-CLICK

    public void onClick(View v)
	{

		SharedPreferences sp= this.getSharedPreferences(SettingsConst.SETTINGS, 0);
		int ss = sp.getInt("trr", 0);

		for(AlarmClock alarm : alarmList){
			if(alarm.getElement().getChildAt(1)== v) {
					if (alarm.getState() == AlarmClock.TimerState.RUNNING){
						alarm.setState(AlarmClock.TimerState.PAUSED);
					} else 	if (alarm.getState() == AlarmClock.TimerState.PAUSED){
						alarm.setState(AlarmClock.TimerState.RUNNING);
					} else 	if (alarm.getState() == AlarmClock.TimerState.ALARMING){
						alarm.alarmSTOP();		
					}
				break;
			}
		}
		
		tvOut.setText("Нажата кнопка Cancel" 
		+ v + " " + alarmList.get(0).getElement().getChildAt(1));
		ss++;
		sp.edit().putInt("ttr", ss);
		sp.edit().commit();
	}

    public void onTimerClick(View v)
	{
		AlarmClock alarm = 	((TextViewWithMenu)v).getAlarm();

		if (alarm.getState().equals(AlarmClock.TimerState.ALARMING))
		{
			//	alarm.finalize();
			alarm.setState(AlarmClock.TimerState.STOPPED);

		}
		else
		{
			alarm.setState(AlarmClock.TimerState.PAUSED);

		}
	}	


// ============================================================
// ====================  MENUS	===============================
// ============================================================

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		new MenuInflater(this).inflate(R.menu.alarm_clock_options, menu);
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
									ContextMenu.ContextMenuInfo menuInfo)
	{
		//menuInfo = new ExpandableListContextMenuInfo(v, 1, 1); 
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.alarm_context, menu);
		super.onCreateContextMenu(menu, v, menuInfo);	

		//    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		//long itemID = info.position;
//	      menu.setHeaderTitle("lior" + 2);
  	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_settings:{
					Intent i3 = new Intent(this, SettingsActivity.class); 
					startActivity(i3);	
					return true;

				}
			case R.id.menu_add:
				addAlarmDialog();


				return true;
			case R.id.menu_exit:{

					//	mNotificationManager.cancel(SettingsConst.APP_NOTIF_ID);
					System.exit(0);
				}

		}	
		return false;
	}



	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		TextViewWithMenu.TextViewMenuInfo menuInfo = (TextViewWithMenu.TextViewMenuInfo) item.getMenuInfo();  
		TextViewWithMenu text = (TextViewWithMenu) menuInfo.targetView;  

		switch (item.getItemId())
		{
			case R.id.settime:{
					setAlarmDialog(text.getAlarm());
					return true;
				}
			case R.id.remove:{
					this.deleteAlarm(text);		
					return true;
				}
		}
		return super.onContextItemSelected(item);
	}


// ============================================================
// ====================  END MENUS	===============================
// ============================================================
	private void deleteAlarm(TextViewWithMenu text)
	{
		for (AlarmClock alarm:alarmList)
		{
			if (alarm.getElement().getChildAt(1) == (TextViewWithMenu)text)
			{
				alarmList.remove(alarm);
				if (alarm.getState().equals(AlarmClock.TimerState.ALARMING)) alarm.stopNOW(this);
				alarm.setState(AlarmClock.TimerState.STOPPED);
				break;
			}
		}
		mainL.removeView(((LinearLayout)text.getParent()));
	}

	private void addAlarmDialog()
	{
		setAlarmDialog(null);		
	}

	private void setAlarmDialog(AlarmClock alarm)
	{
		TimePickDialog dialog = new TimePickDialog(MainActivity.this);
		dialog.setAlarm(alarm);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		dialog.setDialogResult(new TimePickDialog.OnMyDialogResult(){
				public void finish(AlarmClock newAlarm)
				{
					addAlarm(newAlarm);
				}
			});
		dialog.show();
	}


	/**
     * Creates new MusicHandler and starts playing, Also creates ShowStopper.
     */
    private void playMusic()
	{
        music = new MusicHandler();
        music.setMusic(this);
        music.play(true);
		/*    ShowStopper stopper = new ShowStopper(PreferenceService.getAlarmLength(this), music, vibrator);
		 showStopperThread = new Thread(stopper);
		 showStopperThread.start();*/
    }



	/**
	 * Finishes the activity, also closes the various things started by onCreate.
	 */
    @Override
    public void finish()
	{
        Log.v(TAG, "finish");
		//alarmService.alarmSTOP();
    }


}


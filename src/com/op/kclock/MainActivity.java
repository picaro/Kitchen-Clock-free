package com.op.kclock;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.preference.PreferenceManager;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.LinearLayout.*;
import com.op.kclock.*;
import com.op.kclock.alarm.*;
import com.op.kclock.cookconst.*;
import com.op.kclock.dialogs.*;
import com.op.kclock.misc.*;
import com.op.kclock.model.*;
import com.op.kclock.music.*;
import com.op.kclock.ui.*;
import java.util.*;
import android.view.animation.*;
import android.animation.*;

//import android.view.View.View.OnClickListener;


public class MainActivity extends Activity implements OnClickListener
{

    private MusicHandler music = null;
	private Handler handler ;    


	LinearLayout mainL = null; 
	public final static String TAG = "AlarmaClockActivity";
	NotificationManager mNotificationManager;
	private SharedPreferences mPrefs;

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
	 	if(savedInstanceState!=null)	alarmList = savedInstanceState.getParcelableArrayList("SAVE_SELECTED");
		if (alarmList == null) alarmList = new ArrayList<AlarmClock>();

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
		Log.d("oo","start");
		
		startService(new Intent(this, ClockService.class));
	}

	
	
	/**
	 * 
	 * @param timer
	 * @param tickerText
	 * @param contentTitle
	 * @param contentText
	 */
	private void sendTimeIsOverNotification(int timer) {
		int icon;
		
		Context mContext = this.getApplicationContext();
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) mContext.getSystemService(ns);

		icon = R.drawable.stat_notify_alarm;
		CharSequence mTickerText =  " - " + mContext.getResources().getString(R.string.app_name);
		long when = System.currentTimeMillis();
	
timer = (int) when + 4000;
	Notification notification = new Notification(icon, mTickerText, when);
		notification.number = timer+ 1;

		CharSequence mContentTitle = "tt";
		CharSequence mContentText = mContext.getResources().getString(R.string.countdown_ended);

		Intent clickIntent = new Intent(mContext, MainActivity.class);
		clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
							 | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, clickIntent,
																PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(mContext, mContentTitle, mContentText, contentIntent);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			String defaultNotification = "select";// "android.resource://com.leinardi.kitchentimer/" + R.raw.mynotification;
		if (mPrefs.getBoolean(mContext.getString(R.string.pref_notification_sound_key), true)) {
			if (mPrefs.getBoolean(mContext.getString(R.string.pref_notification_custom_sound_key), false)) {
				String customNotification = mPrefs.getString(mContext
															 .getString(R.string.pref_notification_ringtone_key), defaultNotification);
				//	if (!customNotification.equals(defaultNotification)) {
			    	notification.sound = Uri.parse(customNotification);
				//	}
			} else {
				//	notification.sound = Uri.parse(defaultNotification);
			}
		}
		if (mPrefs.getBoolean(mContext.getString(R.string.pref_notification_insistent_key), true))
			notification.flags |= Notification.FLAG_INSISTENT;
		if (mPrefs.getBoolean(mContext.getString(R.string.pref_notification_vibrate_key), true)) {
			String mVibratePattern = mPrefs.getString(mContext
													  .getString(R.string.pref_notification_vibrate_pattern_key), "");
			if (!mVibratePattern.equals("")) {
				notification.vibrate = AlarmClock.parseVibratePattern(mVibratePattern);
			} else {
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			}
		}
		if (mPrefs.getBoolean(mContext.getString(R.string.pref_notification_led_key), true)) {
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			notification.ledARGB = Color.parseColor(mPrefs.getString(mContext
																	 .getString(R.string.pref_notification_led_color_key), "red"));
			int mLedBlinkRate = Integer.parseInt(mPrefs.getString(mContext
																  .getString(R.string.pref_notification_led_blink_rate_key), "2"));
			notification.ledOnMS = 500;
			notification.ledOffMS = mLedBlinkRate * 1000;
		}

		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		mNotificationManager.notify(timer + 10, notification);
	}	
	
	
	
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
				Log.d("oo","onrestore");

		super.onRestoreInstanceState(savedInstanceState);
		alarmList = savedInstanceState.getParcelableArrayList("SAVE_SELECTED");

		//android.view.Display display = ((android.view.WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();      
        Display display = getWindowManager().getDefaultDisplay(); 
		for(AlarmClock alarm : alarmList){
			LinearLayout alarmL = alarm.getElement();
			TextViewWithMenu tw = (TextViewWithMenu) alarmL.getChildAt(1);
			tw.setTextSize(33);
			tvOut.setText("33" + tw);
			
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
				Log.d("oo","onsave");

		
		savedInstanceState.putParcelableArrayList("SAVE_SELECTED", alarmList);
		super.onSaveInstanceState(savedInstanceState);
	}
	
	private void drawAlarms()
	{
		//get alarms from settings
	     for(AlarmClock alarm : alarmList){
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
		
		Animation in = AnimationUtils.loadAnimation(this,R.anim.fade_in);
            	Animation out = AnimationUtils.loadAnimation(this,R.anim.fade_out);
		TextViewWithMenu textView = (TextViewWithMenu)(alarm.getWidget());
	    //       textView.setOutAnimation(out);
		textView.setAlarm(alarm);
		
		alarm.updateElement();
		itemView.setOnClickListener(this);

		AlarmService alarmService = new AlarmServiceImpl(this, handler);
		alarmService.setAlarmClock(alarm);
		alarm.setState(AlarmClock.TimerState.RUNNING);
		new Thread(alarmService).start();



		registerForContextMenu(alarm.getWidget());
		//add the itemView
		mainL.addView(itemView, new TableLayout.LayoutParams(
						  LayoutParams.FILL_PARENT, 
						  LayoutParams.WRAP_CONTENT));
	
		textView.startAnimation(in);
		
	return itemView;
	}

	// ON-CLICK

    public void onClick(View v)
	{
	//	sendTimeIsOverNotification(0); 
		SharedPreferences sp= this.getSharedPreferences(SettingsConst.SETTINGS, 0);
		int ss = sp.getInt("trr", 0);

		for(AlarmClock alarm : alarmList){
			TextViewWithMenu tvTimer = (TextViewWithMenu) alarm.getElement().getChildAt(1);
			if(tvTimer == v) {
					if (alarm.getState() == AlarmClock.TimerState.RUNNING){
						alarm.setState(AlarmClock.TimerState.PAUSED);
						
						tvTimer.setTextColor(getResources().getColor(R.color.gray));
						//tvTimer.setShadowLayer(2f, 4f, 0f, 0);
					Animation hyperspaceJump = 
           					 AnimationUtils.loadAnimation(this, R.anim.fade_out);	
					alarm.getWidget().startAnimation(hyperspaceJump);	
					} else 	if (alarm.getState() == AlarmClock.TimerState.PAUSED){
						tvTimer.setTextColor(getResources().getColor(R.color.white));
						
						Animation hyperspaceJump = 
							AnimationUtils.loadAnimation(this, R.anim.fade_in);	
						alarm.getWidget().startAnimation(hyperspaceJump);	
						
						alarm.setState(AlarmClock.TimerState.RUNNING);
					} else 	if (alarm.getState() == AlarmClock.TimerState.ALARMING){
						alarm.alarmSTOP();		
					}
				break;
			}
		}
		
	//tvOut.setText("__");
		ss++;
		sp.edit().putInt("ttr", ss);
		sp.edit().commit();
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
	public void onCreateContextMenu(ContextMenu menu, View v,									ContextMenu.ContextMenuInfo menuInfo)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.alarm_context, menu);
		super.onCreateContextMenu(menu, v, menuInfo);	

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
			case R.id.menu_add:{
				addAlarmDialog();
				return true;
			}
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
					deleteAlarm(text);		
					return true;
				}
		}
		return super.onContextItemSelected(item);
	}


// ============================================================
// ====================  END MENUS	===============================
// ============================================================
	private void deleteAlarm(final TextViewWithMenu text)
	{
		for (final AlarmClock alarm:alarmList)
		{
			if (alarm.getElement().getChildAt(1) == (TextViewWithMenu)text)
			{
				//final AlarmClock falarm = alarm;
				Animation hyperspaceJump = 
           			AnimationUtils.loadAnimation(this, R.anim.hsjump);
           			hyperspaceJump.setAnimationListener( new Animation.AnimationListener() {
						    @Override
						    public void onAnimationEnd(Animation arg0) {
						           		  		alarmList.remove(alarm);
												if (alarm.getState().equals(AlarmClock.TimerState.ALARMING)) alarm.alarmSTOP();
												alarm.setState(AlarmClock.TimerState.STOPPED);
												alarm.getElement().setVisibility(View.GONE);
								//	mainL.removeView(((LinearLayout)alarm.getElement() ));
								alarm.setElement(null);
								
												
						    }
						     @Override
					            public void onAnimationStart(Animation animation)
					            { }
					            
						    public void onAnimationRepeat(Animation animation)
					            { }

						
					});
           			
           			
           		alarm.getElement().startAnimation(hyperspaceJump);	
				
				break;
			}
		}
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


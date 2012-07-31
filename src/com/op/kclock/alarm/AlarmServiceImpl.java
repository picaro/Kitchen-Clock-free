	package com.op.cooker.alarm;

import android.content.*;
import android.os.*;
import android.telephony.TelephonyManager;
import android.util.*;

import com.op.cooker.misc.Log;
import com.op.cooker.model.*;
import com.op.cooker.model.AlarmClock.TimerState;

import java.util.*;
import android.widget.*;

/**
 *  Class that handles Putting alarm to file and to device. So that we can wake our code on correct time.
 */
public class AlarmServiceImpl implements AlarmService 
{

 

	private long updateInterval;

        
    private AlarmClock widget;

    private final String TAG = "Alarm Service";
    //private final AlarmManager alarmManager;
    private final Context context;
    private final NotificationFactory notificationFactory;
	
    // The alarm is static, so that deleting alarm from Alarm dialog will also show correct information
    // to the alarm service in
    //private static AlarmClock alarm;
    Handler handler;
    
    
    public AlarmServiceImpl(Context context, long updateInterval, Handler _handler) {
		this.context = context.getApplicationContext();
     		this.updateInterval = updateInterval;
     		this.handler = _handler;
		//time = 0;
		//startTime = 0;

		   
       this.notificationFactory = new NotificationFactory(this.context);
  
		
	}
    
	public void setAlarmClock(AlarmClock alarm)
	{
		this.widget = alarm;
	}


	@Override
	public void run() {
do{		widget.updateElement();
			try
			{
	 			Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{}
}		while(widget.tick() && widget.getState() == AlarmClock.TimerState.RUNNING);
		widget.updateElement();		
		if ( widget.getState() == AlarmClock.TimerState.RUNNING)  widget.alarmNOW(context);
		
	}
	
	
	/**
	 * Sets the timer into a running state and
	 * initialises all time values.
	 */
//	public void start() {
	//	startTime = time = System.currentTimeMillis();

//	}

    // Dependency injection for Unit testing.
    /**
     * Do not use this. Only for testing purposes.
     * @param context
     * @param alarmManager
     * @param notificationFactory
     */
//    public AlarmServiceImpl(Context context, AlarmManager alarmManager,
//                            NotificationFactory notificationFactory) {
//        this.context = context.getApplicationContext();
//    //    this.alarmManager = alarmManager;
//      
//        this.notificationFactory = notificationFactory;
//     //   alarm = new Alarm(2, 3, 2, true);
//    }

    /**
     * Adds new alarm to android for our program.
     * @param hours wake up hours
     * @param minutes wake up minutes
     * @param interval wake up interval
     * @return
     */
//    @Override
    public String addAlarm(int hours, int minutes, int interval) {
     
         //    alarm = new Alarm(hours, minutes, interval, true);
//        if (alarm.isEnabled()) { //write succeeded
            Calendar calendar = Calendar.getInstance();
            notificationFactory.setNotification(calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE), hours, minutes);
        //    addWakeUpAttempt(calendar);
            Log.v(TAG, "Adding alarm was successful");
     //   }
        return null;//alarm;
    }







    /**
     * Deletes alarm. From device and from file.
     */
    @Override
    public void deleteAlarm() {
       // Intent intent = new Intent(context, AlarmReceiver.class);
    //    PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);

        //alarmManager.cancel(sender);
       
        notificationFactory.resetNotification();
     //   alarm.setEnabled(false);
    }

    public boolean isAlarmSet() {
        return false;// alarm.isEnabled();
    }

    @Override
    public int getAlarmHours() {
        return 0;//alarm.getHours();
    }

    @Override
    public int getAlarmMinutes() {
        return 0;// alarm.getMinutes();
    }

    @Override
    public int getAlarmInterval() {
        return 100;//alarm.getInterval();
    }


	@Override
	public TimerState getStatus() {
		return widget.getState();
	}

    

	public void finalize(){	
		if (widget != null) widget.alarmSTOP();
	}

}

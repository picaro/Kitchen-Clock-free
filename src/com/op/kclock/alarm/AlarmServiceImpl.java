package com.op.kclock.alarm;

import android.content.*;
import android.os.*;
import android.telephony.TelephonyManager;
import android.util.*;

import com.op.kclock.misc.Log;
import com.op.kclock.model.*;
import com.op.kclock.model.AlarmClock.TimerState;

import java.util.*;
import android.widget.*;

/**
 *  Class that handles Putting alarm to file and to device. So that we can wake our code on correct time.
 */
public class AlarmServiceImpl implements AlarmService 
{

    private AlarmClock widget;

    private final String TAG = "Alarm Service";
    private final Context context;
    private final NotificationFactory notificationFactory;

    Handler handler;


    public AlarmServiceImpl(Context context, Handler _handler)
	{
		this.context = context.getApplicationContext();
		this.handler = _handler;
		this.notificationFactory = new NotificationFactory(this.context);
	}

	public void setAlarmClock(AlarmClock alarm)
	{
		this.widget = alarm;
	}


	@Override
	public void run()
	{
		do{		
			widget.updateElement();
			try
			{
	 			Thread.sleep(1000);
				if(  widget.getState() == AlarmClock.TimerState.STOPPED) break;
			}
			catch (InterruptedException e)
			{}
		} while(widget.tick());
		widget.updateElement();		
		if (widget.getState() == AlarmClock.TimerState.RUNNING)  widget.alarmNOW(context);

	}


    /**
     * Adds new alarm to android for our program.
     * @param hours wake up hours
     * @param minutes wake up minutes
     * @param interval wake up interval
     * @return
     */
//    @Override
    public String addAlarm(int hours, int minutes, int interval)
	{

		Calendar calendar = Calendar.getInstance();
		notificationFactory.setNotification(calendar.get(Calendar.HOUR_OF_DAY),
											calendar.get(Calendar.MINUTE), hours, minutes);
  		Log.v(TAG, "Adding alarm was successful");
       return null;//alarm;
    }


	@Override
	public TimerState getStatus()
	{
		return widget.getState();
	}



	public void finalize()
	{	
		if (widget != null) widget.alarmSTOP();
	}

}

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
package com.op.kclock.alarm;

import java.util.Calendar;

import android.content.Context;
import android.os.Handler;

import com.op.kclock.misc.Log;
import com.op.kclock.model.AlarmClock;
import com.op.kclock.model.AlarmClock.TimerState;

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
			if (widget.getState() == AlarmClock.TimerState.RUNNING) widget.updateElement();
			try
			{
	 			Thread.sleep(1000);
				if(  widget.getState() == AlarmClock.TimerState.STOPPED) break;
			}
			catch (InterruptedException e)
			{}
	
	} while(widget.tick());	
		widget.updateElement();		
		if (widget.getState() == AlarmClock.TimerState.ALARMING)  widget.alarmNOW(context);

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
		if (widget != null) widget.alarmSTOP(context);
	}

}

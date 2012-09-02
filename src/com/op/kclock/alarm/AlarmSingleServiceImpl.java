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
import java.util.List;

import android.content.Context;
import android.os.Handler;

import com.op.kclock.misc.Log;
import com.op.kclock.model.AlarmClock;
import com.op.kclock.model.AlarmClock.TimerState;

/**
 * Class that handles Putting alarm to file and to device. So that we can wake
 * our code on correct time.
 */
public class AlarmSingleServiceImpl implements Runnable {

	private final String TAG = "Alarm Service";
	private final Context context;
	private static boolean running = false;
	private List<AlarmClock> alarmList;
	// private static AlarmSingleServiceImpl instance;

	Handler handler;

	public AlarmSingleServiceImpl(Context _context, Handler _handler,
			List<AlarmClock> _alarmList) {
		context = _context.getApplicationContext();
		handler = _handler;
		running = true;
		alarmList = _alarmList;
	}

	@Override
	public void run() {
		do {
			if (alarmList != null) {
				for (AlarmClock alarm : alarmList) {
					if (alarm.getState() == AlarmClock.TimerState.RUNNING){
						if (alarm.tick()){
							alarm.updateElement();			
						} else {
							alarm.updateElement();
							if (alarm.getState() == AlarmClock.TimerState.ALARMING)  alarm.alarmNOW(); 
						}
					}
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (running);
		// widget.updateElement();
		// if (widget.getState() == AlarmClock.TimerState.ALARMING)
		// widget.alarmNOW(context);

	}

}

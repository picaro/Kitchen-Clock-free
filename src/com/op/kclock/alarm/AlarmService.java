package com.op.kclock.alarm;

import java.util.Calendar;
import android.widget.*;

import android.content.Context;
import com.op.kclock.model.*;
import com.op.kclock.model.AlarmClock.TimerState;

/*
 * Logic class for keeping track of the alarms. Alarm service ensures that new alarms are sent to the Android's
 * alarm manager, saved in a file and a correct notification is displayed. It will also calculate correct times
 * for the first wake up attempts, if the new alarm has an interval.
 *
 * POSSIBLE MODIFICATIONS:
 * Make this class singleton to get rid of the static alarm.
 *
 */
public interface AlarmService extends Runnable {

	public void setAlarmClock(AlarmClock alarm);

	public TimerState getStatus();

}

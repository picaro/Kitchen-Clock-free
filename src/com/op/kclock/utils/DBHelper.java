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

package com.op.kclock.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.op.kclock.MainActivity;
import com.op.kclock.model.*;
import com.op.kclock.misc.*;
import com.op.kclock.cookconst.*;

public class DBHelper {
	SQLiteDatabase db;
	Context context;
	DatabaseHelper dbHelper;
	static final String ALARM_TABLE = "ALARM";

	public static final String ID = "id";
	public static final String NAME = "alarmname";
	public static final String SECONDS = "seconds";
	public static final String INITSECONDS = "initseconds";
	public static final String STATE = "state";
	public static final String PINNED = "pinned";
	public static final String ACTIVE = "active";
	public static final String DATEADD = "dateadd";
	public static final String USAGECNT = "usagecnt";

	static final String DB_NAME = "DB_KCLOCK";
	public static String[] ALARM_COLUMNS = new String[] { ID, NAME, SECONDS,
			INITSECONDS, STATE, PINNED, ACTIVE, DATEADD, USAGECNT };
	static final String HISTORY_TABLE = "HISTORY";
	public static String[] HISTORY_COLUMNS = new String[] { ID, NAME, SECONDS,
			INITSECONDS, STATE, PINNED, ACTIVE, DATEADD, USAGECNT };
	static final String PRESET_TABLE="PRESET";
	public static String[] PRESET_COLUMNS = new String[] { ID, NAME, SECONDS,INITSECONDS,STATE,PINNED,ACTIVE,DATEADD, USAGECNT };


	public DBHelper(Context context) {
		this.context = context;
		this.dbHelper = new DatabaseHelper(context);
	}

	public static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, SettingsConst.DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + ALARM_TABLE + " (" + ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + NAME
					+ " TEXT NOT NULL," + SECONDS + " INTEGER NOT NULL, "
					+ INITSECONDS + " INTEGER NOT NULL, " + PINNED
					+ " INTEGER NOT NULL, " + ACTIVE + " INTEGER NOT NULL, "
					+ DATEADD + " INTEGER NOT NULL, " + USAGECNT
					+ " INTEGER NOT NULL, " + STATE + " INTEGER NOT NULL" + ")");

			db.execSQL("CREATE TABLE IF NOT EXISTS " + HISTORY_TABLE + " ("
					+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + NAME
					+ " TEXT NOT NULL," + SECONDS + " INTEGER NOT NULL, "
					+ INITSECONDS + " INTEGER NOT NULL, " + PINNED
					+ " INTEGER NOT NULL, " + ACTIVE + " INTEGER NOT NULL, "
					+ DATEADD + " INTEGER NOT NULL, " + USAGECNT
					+ " INTEGER NOT NULL, " + STATE + " INTEGER NOT NULL" + ")");
			db.execSQL("CREATE TABLE IF NOT EXISTS " +
					   PRESET_TABLE + " ("+ 
					   ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
					   NAME+" TEXT NOT NULL," +
					   SECONDS+" INTEGER NOT NULL, " +
					   INITSECONDS+" INTEGER NOT NULL, " +
					   PINNED+" INTEGER NOT NULL, " +
					   ACTIVE +" INTEGER NOT NULL, " +
					   DATEADD+" INTEGER NOT NULL, " +
					   USAGECNT+" INTEGER NOT NULL, " +
					   STATE+" INTEGER NOT NULL"		
					   +")"
					   );
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d("DbTool.onUpgrade", "old:" + oldVersion + " new:"
					+ newVersion);
			if (db.getVersion() == 1) {
				db.execSQL("ALTER TABLE " + ALARM_TABLE + " RENAME TO tmp_"
						+ ALARM_TABLE);
				onCreate(db);
				db.execSQL("INSERT INTO " + ALARM_TABLE + "(" + NAME + ", "
						+ STATE + ", " + SECONDS + ") " + "SELECT " + NAME
						+ ", " + STATE + ", " + SECONDS + " " + "FROM tmp_"
						+ ALARM_TABLE);
				db.execSQL("DROP TABLE IF EXISTS tmp_" + ALARM_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
				db.execSQL("DROP TABLE IF EXISTS "+PRESET_TABLE);
			} else {
				db.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE);
				db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
				db.execSQL("DROP TABLE IF EXISTS "+PRESET_TABLE);
				onCreate(db);
			}
		}
	}

	public void insertHistory(AlarmClock record) {
		ContentValues values = new ContentValues();
		values.put(NAME, record.getName() == null ? "alarm" : record.getName());
		values.put(STATE, record.getState().toString());
		values.put(SECONDS, record.getTime());
		values.put(INITSECONDS, record.getInitSeconds());
		values.put(PINNED, record.isPinned() ? 1 : 0);
		values.put(ACTIVE, record.isActive() ? 1 : 0);
		values.put(DATEADD, record.getDateAdd());
		values.put(USAGECNT, record.getUsageCnt());
		db.insert(HISTORY_TABLE, null, values);
	}

	public void updateHistory(AlarmClock record) {
		ContentValues values = new ContentValues();
		values.put(NAME, record.getName());
		values.put(STATE, record.getState().toString());
		values.put(SECONDS, record.getTime());
		values.put(INITSECONDS, record.getInitSeconds());
		values.put(PINNED, record.isPinned() ? 1 : 0);
		values.put(ACTIVE, record.isActive() ? 1 : 0);
		values.put(DATEADD, record.getDateAdd());
		values.put(USAGECNT, record.getUsageCnt());
		db.update(HISTORY_TABLE, values, ID + "=" + record.getId(), null);
	}

	public void deleteHistory(long examId) {
		db.delete(HISTORY_TABLE, ID + "=" + examId, null);
	}

	public List<AlarmClock> getHistoryList() {
		Cursor cursor;

		open();
		List<AlarmClock> alarmList = new ArrayList<AlarmClock>();
		cursor = getHistoryRecords();
		if (cursor.moveToFirst()) {
			do {
				int idColIndex = cursor.getColumnIndex(DBHelper.ID);
				int nameColIndex = cursor.getColumnIndex(DBHelper.NAME);
				int seconds = cursor.getColumnIndex(DBHelper.SECONDS);
				int initSeconds = cursor.getColumnIndex(DBHelper.INITSECONDS);
				int pinned = cursor.getColumnIndex(DBHelper.PINNED);
				int active = cursor.getColumnIndex(DBHelper.ACTIVE);
				int dateadd = cursor.getColumnIndex(DBHelper.DATEADD);
				int usagecnt = cursor.getColumnIndex(DBHelper.USAGECNT);
				// int state = cursor.getColumnIndex(DbTool.STATE);
				AlarmClock alarm = new AlarmClock(context);
				alarm.setId(cursor.getInt(idColIndex));
				alarm.setTime(cursor.getInt(seconds));
				alarm.setInitSeconds(cursor.getInt(initSeconds));
				alarm.setPinned(cursor.getInt(pinned) == 1 ? true : false);
				alarm.setActive(cursor.getInt(active) == 1 ? true : false);
				alarm.setDateAdd(cursor.getInt(dateadd));
				alarm.setUsageCnt(cursor.getInt(usagecnt));
				alarm.restart();
				// alarm.setState(context, AlarmClock.TimerState.valueOf(cursor
				// .getString(state)));
				alarm.setName(cursor.getString(nameColIndex));
				alarm.setState(AlarmClock.TimerState.PAUSED);
				alarmList.add(alarm);
				Log.d(MainActivity.TAG, "ID = " + cursor.getInt(idColIndex)
						+ ", " + NAME + " = " + cursor.getString(nameColIndex)
						+ ", " + SECONDS + " = " + cursor.getString(seconds));
			} while (cursor.moveToNext());
		} else
			Log.d(MainActivity.TAG, "0 rows");
		close();
		return alarmList;
	}

	public void truncateHistory() {
		db.delete(HISTORY_TABLE, null, null);
	}

	public Cursor queryHistory(long examId) {
		return db.query(HISTORY_TABLE, HISTORY_COLUMNS, "_id=" + examId, null,
				null, null, null);
	}

	public Cursor getHistoryRecords() {
		return db.rawQuery("SELECT * FROM " + HISTORY_TABLE + " ORDER BY "
				+ NAME, null);
	}

	public void open() {
		db = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void insertAlarm(AlarmClock record) {
		ContentValues values = new ContentValues();
		values.put(NAME, record.getName() == null ? "alarm" : record.getName());
		values.put(STATE, record.getState().toString());
		values.put(SECONDS, record.getTime());
		values.put(INITSECONDS, record.getInitSeconds());
		values.put(PINNED, record.isPinned() ? 1 : 0);
		values.put(ACTIVE, record.isActive() ? 1 : 0);
		values.put(DATEADD, record.getDateAdd());
		values.put(USAGECNT, record.getUsageCnt());
		db.insert(ALARM_TABLE, null, values);
	}

	public void updateAlarm(AlarmClock record) {
		ContentValues values = new ContentValues();
		values.put(NAME, record.getName());
		values.put(STATE, record.getState().toString());
		values.put(SECONDS, record.getTime());
		values.put(INITSECONDS, record.getInitSeconds());
		values.put(PINNED, record.isPinned() ? 1 : 0);
		values.put(ACTIVE, record.isActive() ? 1 : 0);
		values.put(DATEADD, record.getDateAdd());
		values.put(USAGECNT, record.getUsageCnt());
		db.update(ALARM_TABLE, values, ID + "=" + record.getId(), null);
	}

	public void deleteAlarm(long examId) {
		db.delete(ALARM_TABLE, ID + "=" + examId, null);
	}

	public List<AlarmClock> getAlarmsList() {
		Cursor cursor;

		open();
		List<AlarmClock> alarmList = new ArrayList<AlarmClock>();
		cursor = getAlarmRecords();
		if (cursor.moveToFirst()) {
			do {
				int idColIndex = cursor.getColumnIndex(DBHelper.ID);
				int nameColIndex = cursor.getColumnIndex(DBHelper.NAME);
				int seconds = cursor.getColumnIndex(DBHelper.SECONDS);
				int initSeconds = cursor.getColumnIndex(DBHelper.INITSECONDS);
				int pinned = cursor.getColumnIndex(DBHelper.PINNED);
				int active = cursor.getColumnIndex(DBHelper.ACTIVE);
				int dateadd = cursor.getColumnIndex(DBHelper.DATEADD);
				int usagecnt = cursor.getColumnIndex(DBHelper.USAGECNT);
				// int state = cursor.getColumnIndex(DbTool.STATE);
				AlarmClock alarm = new AlarmClock(context);
				alarm.setId(cursor.getInt(idColIndex));
				alarm.setTime(cursor.getInt(seconds));
				alarm.setInitSeconds(cursor.getInt(initSeconds));
				alarm.setPinned(cursor.getInt(pinned) == 1 ? true : false);
				alarm.setActive(cursor.getInt(active) == 1 ? true : false);
				alarm.setDateAdd(cursor.getInt(dateadd));
				alarm.setUsageCnt(cursor.getInt(usagecnt));
				alarm.restart();
				// alarm.setState(context, AlarmClock.TimerState.valueOf(cursor
				// .getString(state)));
				alarm.setName(cursor.getString(nameColIndex));
				alarm.setState(AlarmClock.TimerState.PAUSED);
				alarmList.add(alarm);
				Log.d(MainActivity.TAG, "ID = " + cursor.getInt(idColIndex)
						+ ", " + NAME + " = " + cursor.getString(nameColIndex)
						+ ", " + SECONDS + " = " + cursor.getString(seconds));
			} while (cursor.moveToNext());
		} else
			Log.d(MainActivity.TAG, "0 rows");
		close();
		return alarmList;
	}

	public void truncateAlarms() {
		db.delete(ALARM_TABLE, null, null);
	}

	public Cursor queryAlarm(long examId) {
		return db.query(ALARM_TABLE, ALARM_COLUMNS, "_id=" + examId, null,
				null, null, null);
	}

	public Cursor getAlarmRecords() {
		return db.rawQuery(
				"SELECT * FROM " + ALARM_TABLE + " ORDER BY " + NAME, null);
	}
	
	
	public void insertPreset(AlarmClock record){
		ContentValues values = new ContentValues();
		values.put(NAME,  record.getName()== null? "alarm":  record.getName());
		values.put(STATE, record.getState().toString());
		values.put(SECONDS, record.getTime());
		values.put(INITSECONDS, record.getInitSeconds());
		values.put(PINNED, record.isPinned() ? 1 : 0);
		values.put(ACTIVE, record.isActive() ? 1 : 0);
		values.put(DATEADD, record.getDateAdd());
		values.put(USAGECNT, record.getUsageCnt());
		db.insert(PRESET_TABLE, null, values);
	}

	public void updatePreset(AlarmClock record){
		ContentValues values = new ContentValues();
		values.put(NAME, record.getName());
		values.put(STATE, record.getState().toString());
		values.put(SECONDS, record.getTime());
		values.put(INITSECONDS, record.getInitSeconds());
		values.put(PINNED, record.isPinned() ? 1 : 0);
		values.put(ACTIVE, record.isActive() ? 1 : 0);
		values.put(DATEADD, record.getDateAdd());
		values.put(USAGECNT, record.getUsageCnt());
		db.update(PRESET_TABLE, values, ID+"="+record.getId(), null);
	}

	public void deletePreset(long examId){
		db.delete(PRESET_TABLE, ID+"=" + examId, null);
	}

	public List<AlarmClock> getPresetsList() {
		Cursor cursor;

		open();
		List<AlarmClock> alarmList = new ArrayList<AlarmClock>();
		cursor = getPresetRecords();
		if (cursor.moveToFirst()) {
			do {
				int idColIndex = cursor.getColumnIndex(DBHelper.ID);
				int nameColIndex = cursor.getColumnIndex(DBHelper.NAME);
				int seconds = cursor.getColumnIndex(DBHelper.SECONDS);
				int initSeconds = cursor.getColumnIndex(DBHelper.INITSECONDS);
				int pinned = cursor.getColumnIndex(DBHelper.PINNED);
				int active = cursor.getColumnIndex(DBHelper.ACTIVE);
				int dateadd = cursor.getColumnIndex(DBHelper.DATEADD);
				int usagecnt = cursor.getColumnIndex(DBHelper.USAGECNT);
				//int state = cursor.getColumnIndex(DbTool.STATE);
				AlarmClock alarm = new AlarmClock(context);
				alarm.setId(cursor.getInt(idColIndex));
				alarm.setTime(cursor.getInt(seconds));
				alarm.setInitSeconds(cursor.getInt(initSeconds));
				alarm.setPinned(cursor.getInt(pinned) == 1? true : false);
				alarm.setActive(cursor.getInt(active) == 1? true : false);
				alarm.setDateAdd(cursor.getInt(dateadd));
				alarm.setUsageCnt(cursor.getInt(usagecnt));
				alarm.restart();
//			alarm.setState(context, AlarmClock.TimerState.valueOf(cursor
//					.getString(state)));
				alarm.setName(cursor.getString(nameColIndex));
				alarm.setState(AlarmClock.TimerState.PAUSED);
				alarmList.add(alarm);
				Log.d(MainActivity.TAG,
					  "ID = " + cursor.getInt(idColIndex) + ", "
					  + NAME + " = "
					  + cursor.getString(nameColIndex) + ", "
					  + SECONDS + " = "
					  + cursor.getString(seconds));
			} while (cursor.moveToNext());
		} else
			Log.d(MainActivity.TAG, "0 rows");
		close();
		return alarmList;
	}

	public void truncatePresets(){
		db.delete(PRESET_TABLE, null, null);
	}


	public Cursor presetQuery(long examId){
		return db.query(PRESET_TABLE, PRESET_COLUMNS, "_id=" + examId, null, null, null,null);
	}

	public Cursor getPresetRecords(){
		return db.rawQuery("SELECT * FROM "+ PRESET_TABLE + " ORDER BY " + NAME, null);
	}

}

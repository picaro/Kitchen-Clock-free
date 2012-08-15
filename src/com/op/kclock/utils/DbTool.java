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

public class DbTool {
	SQLiteDatabase db;	
	Context context;
	DatabaseHelper dbHelper;
	static final String DB_NAME="DB_KCLOCK";
	static final String TABLE="ALARM";
	
	public static final String ID="id";
	public static final String NAME="alarmname";
	public static final String SECONDS="seconds";
	public static final String STATE="state";
	
	public static String[] COLUMNS = new String[] { ID, NAME, SECONDS,STATE };

	static final int DB_VERSION=1;

	public DbTool(Context context) {
		this.context=context;
		this.dbHelper=new DatabaseHelper(context);
	}

	public static class DatabaseHelper extends SQLiteOpenHelper{
		public DatabaseHelper(Context context){
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS " +
					TABLE + " ("+ 
					ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
					NAME+" TEXT NOT NULL," +
					SECONDS+" INTEGER NOT NULL, " +
		     		STATE+" INTEGER NOT NULL"		
					+")"
			);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d("DbTool.onUpgrade", "old:"+oldVersion+" new:"+newVersion);
			if(db.getVersion()==1){
				db.execSQL("ALTER TABLE " + TABLE + " RENAME TO tmp_"+ TABLE);
				onCreate(db);
				db.execSQL("INSERT INTO " +
						TABLE + "("+ 
						NAME +", " +
						STATE +", " +
						SECONDS +") " +
						"SELECT " + 
						NAME+", " +
						STATE +", " +
						SECONDS + " " +
						"FROM tmp_" + TABLE
				);
				db.execSQL("DROP TABLE IF EXISTS tmp_"+TABLE);
			}else{
				db.execSQL("DROP TABLE IF EXISTS "+TABLE);
				onCreate(db);
			}
		}
	}

	public void open(){
		db=dbHelper.getWritableDatabase();
	}
	public void close(){
		dbHelper.close();
	}

	public void insert(AlarmClock record){
		ContentValues values = new ContentValues();
		values.put(NAME,  record.getName()== null? "alarm":  record.getName());
		values.put(STATE, record.getState().toString());
		values.put(SECONDS, record.getTime());
		db.insert(TABLE, null, values);
	}

	public void update(AlarmClock record){
		ContentValues values = new ContentValues();
		values.put(NAME, record.getName());
		values.put(STATE, record.getState().toString());
		values.put(SECONDS, record.getTime());
		db.update(TABLE, values, ID+"="+record.getId(), null);
	}

	public void delete(long examId){
		db.delete(TABLE, ID+"=" + examId, null);
	}

	public List<AlarmClock> getAlarmsList() {
		Cursor cursor;

		open();
		List<AlarmClock> alarmList = new ArrayList<AlarmClock>();
		cursor = getRecords();
		if (cursor.moveToFirst()) {
			do {
			int idColIndex = cursor.getColumnIndex(DbTool.ID);
			int nameColIndex = cursor.getColumnIndex(DbTool.NAME);
			int seconds = cursor.getColumnIndex(DbTool.SECONDS);
			//int state = cursor.getColumnIndex(DbTool.STATE);
			AlarmClock alarm = new AlarmClock();
			alarm.setId(cursor.getInt(idColIndex));
			alarm.setSec(cursor.getInt(seconds));
//			alarm.setState(context, AlarmClock.TimerState.valueOf(cursor
//					.getString(state)));
			alarm.setName(cursor.getString(nameColIndex));
			alarm.setState(context, AlarmClock.TimerState.PAUSED);
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

	public void truncate(){
		db.delete(TABLE, null, null);
	}


	public Cursor query(long examId){
		return db.query(TABLE, COLUMNS, "_id=" + examId, null, null, null,null);
	}

	public Cursor getRecords(){
		return db.rawQuery("SELECT * FROM "+ TABLE + " ORDER BY " + NAME, null);
	}
}


package com.mg.mig.bikeplus;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.InputMismatchException;

/**
 * Created by miguelguevara on 4/25/16.
 */
public class MyDbHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "bikedata";
    public static final String TABLE_USERS = "users";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    public static final String TAG = "db";

    public MyDbHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_USERS_TABLE = "CREATE TABLE " +
                TABLE_USERS + "(" +
                COLUMN_ID + " INTEGER," +
                COLUMN_USERNAME + " TEXT," +
                COLUMN_PASSWORD + " TEXT,)";
        db.execSQL(CREATE_USERS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public void addUser(Users user){
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_ID, user.getId());
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    public Users findUser(Integer id){
        String quiery = "Select * FROM " +
                TABLE_USERS + " WHERE " + COLUMN_ID + "= \"" + id + "\"";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(quiery, null);
        Users user = new Users();

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();

            user.setId(Integer.parseInt(cursor.getString(0)));
            user.setUsername(cursor.getString(1));
            user.setPassword(cursor.getString(2));

            cursor.close();
        }
        else {
            user = null;
        }
        db.close();
        return user;
    }

    public boolean deleterUser(int id){
        boolean result = false;
        String quiry = "Select * FROM " +
                TABLE_USERS + " WHERE " + COLUMN_ID + "=  \"" + id + "\"";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(quiry, null);
        Users user = new Users();
        Log.i(TAG, "deleteUSer: entering if statement");
        if(cursor.moveToFirst()){
            Log.i(TAG, "deleteUSer: if in");
            user.setId(Integer.parseInt(cursor.getString(0)));
            db.delete(TABLE_USERS, COLUMN_ID + " = ?", new String[]{String.valueOf(user.getId())});
            cursor.close();
            result = true;
        }
        db.close();
        return result;
    }
}

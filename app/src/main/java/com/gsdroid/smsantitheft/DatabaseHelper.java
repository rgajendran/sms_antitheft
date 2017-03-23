package com.gsdroid.smsantitheft;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "SMSAntiTheft.db";
    public static final String TABLE_NAME = "ANTI";
    public static final String COL_1 = "id";
    public static final String COL_2 = "DATE";
    public static final String COL_4 = "MESSAGE";
    public Context c;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        c = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, DATE TEXT, MESSAGE TEXT)");
        Message.toast(c, "Storage Setting Up...");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String date, String Message){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, date);
        contentValues.put(COL_4, Message);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1)
            return false;
        else
            return true;

    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME + " ORDER BY DATE DESC", null);
        return res;
    }


    public void deleteData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE "+TABLE_NAME);
        Message.toast(c,"Reset Complete...");
        onCreate(db);
    }
}

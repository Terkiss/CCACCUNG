package com.example.alllistener;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DatabaseManager extends DataBaseModel {
    public static DatabaseManager _Instance = new DatabaseManager();

    private DatabaseManager(){

    }



    // 데이터 베이스의 버전
    int version = 4;

    Context context;
    DatabaseHelper myDBHelper = null;
    SQLiteDatabase db;


    public DatabaseManager initialization(@NonNull Context context)
    {
        if(dbName.length() < 1 || tableCreateSQL.size() < 1)
        {
            Toast.makeText(context, "디비 이름 또는 테이블 이름 또는 sql를 먼저 설정 해주세요", Toast.LENGTH_LONG).show();
            return null;
        }

        if(this.context == null ||myDBHelper == null)
        {
            this.context = context;
            myDBHelper = new DatabaseHelper(context);

            // 데이터 베이스 읽고 쓰기 가능 모드로 오픈
            db = myDBHelper.getWritableDatabase();
        }
  
        return _Instance;
    }


    public void executeSQL(String SQL)
    {
        db.execSQL(SQL);
    }

    public  void CreateTable(@NonNull String _TableName, @NonNull String[] args)
    {
        String temp ="";
        for(int i = 0; i < args.length; i++)
        {
            temp += args[i]+", ";
        }
        String sql = "create table "+_TableName+"("+temp+");";

        db.execSQL(sql);
    }
    public  void CreateTable(@NonNull String SQL)
    {
        db.execSQL(SQL);

    }

    public void insertData(String _TableName, String[] filedName, String[] data){
        String filed ="";
        String Riddle="(";

        for(int i = 0; i<filedName.length; i++)
        {
            if(i == filedName.length-1)
            {
                filed += filedName[i];
                Riddle +="?);";
                break;
            }
            filed += filedName[i]+", ";
            Riddle +="?, ";
        }
        String sql = "insert into "+_TableName+"("+filed+") values"+Riddle;
        JeongLog.log.logD(sql);
        db.execSQL(sql, data);
    }
    public void insertData(String table, String nullColumnHack, ContentValues values)
    {
        db.insert(table, nullColumnHack, values);
    }

    public void deleteData(String _TableName, String fieldName, String[] args)
    {
        String sql = "delete from "+_TableName+" where "+fieldName+" =?";
        db.execSQL(sql, args);
    }

    public void deleteDataForTable(String _TableName)
    {
        String sql = "DELETE FROM "+_TableName+";";

        executeSQL(sql);
    }



    public Cursor selectdata(String sql){
        Cursor cursor = db.rawQuery(sql,null);
        return cursor;
    }




    private class DatabaseHelper extends SQLiteOpenHelper
    {
        // 헬퍼 생성
        public DatabaseHelper(@Nullable Context context)
        {
            super(context, dbName, null, version);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {


            if(tableCreateSQL.size() > 0)
            {
                for(int i = 0 ; i < tableCreateSQL.size(); i++)
                {
                    db.execSQL(tableCreateSQL.get(i));
                }
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // 데이터 베이스의 변경이 있을떄
        }
    }

}

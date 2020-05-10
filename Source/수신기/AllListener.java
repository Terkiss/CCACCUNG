package com.example.alllistener;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.annotation.RequiresApi;

@SuppressLint("OverrideAbstract")
public class AllListener extends NotificationListenerService {
    public AllListener() {
        new JeongLog();
    }
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        JeongLog.log.logD("onNotificationRemoved ~ " +
                " packageName: " + sbn.getPackageName() +
                " id: " + sbn.getId());

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        Notification notification = sbn.getNotification();

        Bundle extras = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            extras = sbn.getNotification().extras;
        }

        String title = extras.getString(Notification.EXTRA_TITLE);

        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);

        CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Icon smallIcon = notification.getSmallIcon();
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Icon largeIcon = notification.getLargeIcon();
        }



        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String corvertTime = sdf.format(sbn.getPostTime());
        

        if(title == null || text == null)
        {
            return;
        }
        else if(sbn.getPackageName().equals("com.kakao.talk"))
        {
           // MainActivity.textView.append(data);

           DataInsert( subText, title, text, corvertTime);
        }


    }

    private void DataInsert(CharSequence room, String sender, CharSequence content, String time)
    {

        if(room == null || room.length() < 1)
        {
            // 갠톡일경우임!
            room = sender; // 갠톡방은 보낸 사람과 동일
        }
         Cursor cursor = DatabaseManager._Instance.selectdata("select * from room where room ='"+room+"';");
        JeongLog.log.logD(cursor.toString());
        if(cursor.getCount() < 1)
        {
            DatabaseManager._Instance.insertData("room",new String[]{"room"}, new String[]{room.toString()});
        }
        DatabaseManager._Instance.insertData("KaKaoTable",new String[]{"room", "sender", "content", "time"}, new String[]{room.toString(), sender, content.toString(), time});
    }
}

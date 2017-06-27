package jp.kemomimi.util;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shintani on 2017/06/23.
 */

public class NoNotificationChannel {
    private static final String CHANNELGROUPID = "NstaNotif";
    private static String mGroupName = "Notification group";
    private static int mPrio = NotificationManager.IMPORTANCE_DEFAULT;

    @TargetApi(Build.VERSION_CODES.O)
    public static void setGroupName(String groupName){
        mGroupName = groupName;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void setImportance(int importance){
        mPrio = importance;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void wrappNotify(Context context, int id, Notification notification, String channelName){
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        wrappNotify(manager, id, notification, channelName);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void wrappNotify(Context context, int id, Notification notification){
        wrappNotify(context, id, notification, "Notification");
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void wrappNotify(NotificationManager manager, int id, Notification notification){
        wrappNotify(manager, id, notification, "Notification");
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void wrappNotify(NotificationManager manager, int id, Notification notification, String channelName){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String genid = String.valueOf(String.valueOf(notification.ledARGB) +
                    (notification.sound != null?notification.sound.toString():"no sound") +
                    (notification.vibrate != null?Arrays.toString(notification.vibrate):"no vibrate") +
                    notification.visibility + notification.getBadgeIconType() + notification.flags);

            String channelid = android.util.Base64.encodeToString(genid.getBytes(), android.util.Base64.URL_SAFE);

            Field mChannelId = null;

            try {
                mChannelId = Notification.class.getDeclaredField("mChannelId");
                mChannelId.setAccessible(true);
                mChannelId.set(notification, channelid);

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            List<NotificationChannelGroup> ncgs = manager.getNotificationChannelGroups();
            if(ncgs != null){
                for(NotificationChannelGroup g:ncgs){
                    if(g.getId().equals(CHANNELGROUPID)){
//                        for(NotificationChannel c:g.getChannels()){
//                            manager.deleteNotificationChannel(c.getId());
//                        }
                        manager.deleteNotificationChannelGroup(CHANNELGROUPID);
                        break;
                    }
                }
            }

            NotificationChannelGroup ncg = new NotificationChannelGroup(CHANNELGROUPID, mGroupName);
            NotificationChannel nc = new NotificationChannel(channelid,
                    channelName, mPrio);

            nc.setLockscreenVisibility(notification.visibility);
            nc.enableLights((notification.flags & Notification.FLAG_SHOW_LIGHTS) == 1);
            if(notification.vibrate != null) {
                nc.enableVibration(true);
                nc.setVibrationPattern(notification.vibrate);
            }
            nc.setLightColor(notification.ledARGB);
            if(notification.sound != null) {
                nc.setSound(notification.sound, nc.getAudioAttributes());
            }
            nc.setShowBadge(notification.getBadgeIconType() != Notification.BADGE_ICON_NONE);
            nc.setGroup(CHANNELGROUPID);

            manager.createNotificationChannelGroup(ncg);
            manager.createNotificationChannel(nc);

            manager.notify(id, notification);

        } else {

            manager.notify(id, notification);

        }

    }
}

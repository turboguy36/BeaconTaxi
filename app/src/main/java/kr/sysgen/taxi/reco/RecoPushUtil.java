package kr.sysgen.taxi.reco;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.SplashActivity;

/**
 * Created by leehg on 2016-05-19.
 */
public class RecoPushUtil {
    private final int mNotificationID = 0xAAAC;

    public static final int TYPE_ENTER_TAXI = 3;

    private Context mContext;

    public RecoPushUtil(Context c){
        this.mContext = c;
    }

    final long[] vibratePattern = new long[]{0, 300, 200, 300};

    final Uri soundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    /**
     *
     * @param title
     * @param msg
     * @param param
     * @param type
     */
    public void popupNotification(String title, String msg, String param, int type){
        final int requestId = (int) System.currentTimeMillis();

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Push 를 클릭 했을 때 열리는 Activity
        Intent notificationIntent = new Intent(mContext, SplashActivity.class)
                .setAction(Intent.ACTION_MAIN)
                .putExtra(mContext.getString(R.string.parameter), param)
                .setAction("myString"+ requestId)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, requestId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            builder = getBuilderMarshmallow(title, msg, contentIntent);
        }else{
            builder = getBuilder(title, msg, contentIntent);
        }

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        builder.setStyle(inboxStyle);

        final int notificationId = mNotificationID+type;
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     *
     * @param title
     * @param msg
     * @param type
     */
    public void popupNotification(String title, String msg, int type){
        final int requestId = (int) System.currentTimeMillis();

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Push 를 클릭 했을 때 열리는 Activity
        Intent notificationIntent = new Intent(mContext, SplashActivity.class)
                .setAction(Intent.ACTION_MAIN)
                .setAction("myString"+ requestId)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, requestId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            builder = getBuilderMarshmallow(title, msg, contentIntent);
        }else{
            builder = getBuilder(title, msg, contentIntent);
        }

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        builder.setStyle(inboxStyle);

        final int notificationId = mNotificationID+type;
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     *
     * @return
     */
    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.icon_small_white : R.mipmap.ic_launcher;
    }

    /**
     *
     * @param title
     * @param msg
     * @param contentIntent

     * @return
     */
    private NotificationCompat.Builder getBuilderMarshmallow(String title, String msg, PendingIntent contentIntent){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setSubText(msg)
                .setContentIntent(contentIntent)
                .setSound(soundURI)
                .setVibrate(vibratePattern)
                .setSmallIcon(getNotificationIcon())
                .setLights(Color.RED, 3000, 1000);
        return builder;
    }

    /**
     *
     * @param title
     * @param msg
     * @param contentIntent
     * @return
     */
    private NotificationCompat.Builder getBuilder(String title, String msg, PendingIntent contentIntent){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(msg)
                .setContentIntent(contentIntent)
                .setSound(soundURI)
                .setVibrate(vibratePattern)
                .setSmallIcon(getNotificationIcon())
                .setLights(Color.RED, 3000, 1000);
        return builder;
    }
}

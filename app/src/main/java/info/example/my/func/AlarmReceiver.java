package info.example.my.func;



import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import info.example.my.R;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "alarm_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 알람 발생 시 알림 표시
        showNotification(context, "알람", "설정한 시간이 되었습니다!");
    }

    private void showNotification(Context context, String title, String message) {
        // NotificationManager 가져오기
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8.0 이상에서는 Notification Channel 필요
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "알람 채널",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        // NotificationCompat.Builder 생성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.alarm) // 알림 아이콘
                .setContentTitle(title) // 알림 제목
                .setContentText(message) // 알림 메시지
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 알림 우선순위
                .setAutoCancel(true); // 클릭 시 알림 삭제

        // 알림 표시
        notificationManager.notify(1, builder.build());
    }
}

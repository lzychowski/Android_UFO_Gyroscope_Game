// Leszek Zychowski

package com.yolonerds.sampleandroidgame;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

// http://stackoverflow.com/questions/4765517/start-service-in-android
// http://javapapers.com/android/how-to-play-audio-in-android/
// http://stackoverflow.com/questions/21043059/play-background-sound-in-android-applications
public class MusicPlayer extends Service {

    MediaPlayer player;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onCreate() {
        player = MediaPlayer.create(this, R.raw.theme_audio);
        player.setLooping(true);
        player.setVolume(100,100);
    }

    public void onDestroy() {
        player.stop();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        player.start();
        return 1;
    }
}

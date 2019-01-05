package com.example.musicplayer;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import java.util.ArrayList;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


//

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener,
         MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;
    private boolean shuffle = false;
    private Random rand;


    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public void onDestroy() {
        player.stop();
        player.release();
        stopForeground(true);
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return false;
    }


    public void onCreate() {
        super.onCreate();

        songPosn = 0;
        player = new MediaPlayer();
        initMusicPlayer();
        rand = new Random();

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
       // startMyOwnForeground();
        //}
        //else {




        //  startForeground(1, new Notification());




        // }
/*
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = getString(R.string.app_name);
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setDescription(channelId);
        notificationChannel.setSound(null, null);

        notificationManager.createNotificationChannel(notificationChannel);
        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(android.provider.MediaStore.Audio.Media.TITLE)
                .setSmallIcon(R.drawable.icon_musicplayer)
                //.setPriority(Notification.PRIORITY_DEFAULT)
                .build();
     */
    }

    public void setShuffle() {
        shuffle = !shuffle;
    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        // player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e("aftsongpic", "oncmplt");
        if (player.getCurrentPosition() > 0) {

                mp.reset();
                Log.e("aftsongpic", "mp.reset");


                playNext();
                Log.e("aftsongpic", "plynxt1");



        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void playSong() {
        player.reset();
        Song playSong = songs.get(songPosn);
        songTitle = playSong.getTitle();
        long currSong = playSong.getID();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

            player.prepareAsync();
            Log.e("aftsongpic", "prpasync");

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
/*
        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();
*/

        String NOTIFICATION_CHANNEL_ID = "com.example.musicplayer";
        String channelName = "MyBackgroundService";

        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        Log.e("aftsongpic" , "1");
        chan.setLightColor(Color.BLUE);
        Log.e("aftsongpic" , "2");
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        Log.e("aftsongpic" , "3");
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.e("aftsongpic" , "4");
        assert manager != null;
        Log.e("aftsongpic" , "5");
        manager.createNotificationChannel(chan);
        Log.e("aftsongpic" , "6");







        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Log.e("aftsongpic" , "7");
        Notification notification = notificationBuilder.setOngoing(true)

                .setSmallIcon(R.drawable.icon_musicplayer)
                .setContentTitle("Music Player")
                .setContentText(android.provider.MediaStore.Audio.Media.TITLE)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        Log.e("aftsongpic" , "8");
        startForeground(1, notification);
        Log.e("aftsongpic" , "9");

    }






    public void setSong(int songIndex) {
        songPosn = songIndex;
    }

    public int getPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPng() {
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
    }

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void go() {
        player.start();
    }

    public void playPrev() {
        songPosn--;
        if (songPosn == 0) songPosn = songs.size() - 1;
        playSong();
    }

    public void playNext() {
        if (shuffle) {
            int newSong = songPosn;
            while (newSong == songPosn) {
                newSong = rand.nextInt(songs.size());
            }
            songPosn = newSong;
        } else {
            songPosn++;
            if (songPosn == songs.size()) songPosn = 0;
        }
        playSong();


    }
/*
    private void startMyOwnForeground() {


        String NOTIFICATION_CHANNEL_ID = "com.example.musicplayer";
        String channelName = "MyBackgroundService";

        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        Log.e("aftsongpic" , "1");
        chan.setLightColor(Color.BLUE);
        Log.e("aftsongpic" , "2");
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        Log.e("aftsongpic" , "3");
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.e("aftsongpic" , "4");
        assert manager != null;
        Log.e("aftsongpic" , "5");
        manager.createNotificationChannel(chan);
        Log.e("aftsongpic" , "6");







       NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Log.e("aftsongpic" , "7");
        Notification notification = notificationBuilder.setOngoing(true)

                .setSmallIcon(R.drawable.icon_musicplayer)
                .setContentTitle("Music Player")
                .setContentText(android.provider.MediaStore.Audio.Media.TITLE)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        Log.e("aftsongpic" , "8");
        startForeground(1, notification);
        Log.e("aftsongpic" , "9");

    }
    */
}




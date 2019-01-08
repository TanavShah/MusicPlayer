package com.example.musicplayer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.AdapterView;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements MediaPlayerControl {

    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private MusicController controller;
    private boolean paused=false, playbackPaused=false;
    private String sname;
    private String thisTitle;
    private String thisArtist;
    private MediaPlayer p1;
    private Boolean isSelected;
    private Boolean forcolour = false;
    public static MainActivity instance;

    public void getSongList() {
        //retrieve song info
    }

/*
    public void listequal(ListView lv)
    {
        for(int i = 0;i<songList.size();i++)
        {
            lv.getChildAt(i) = songView.getChildAt(i);
        }
    }
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

                return;
            }}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

                return;
            }}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WAKE_LOCK)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.WAKE_LOCK},1);

                return;
            }}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.FOREGROUND_SERVICE},1);

                return;
            }}
        songView = findViewById(R.id.song_list);
        songList = new ArrayList<Song>();


        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            while (musicCursor.moveToNext()) {
                long thisId = musicCursor.getLong(idColumn);
                thisTitle = musicCursor.getString(titleColumn);
                thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
        }
        getSongList();
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
    try {
        setController();
    }
    catch(Exception e)
    {
        Log.e("aftsongpic","setcontroller");
    }


    }






    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicBound = true;
            Log.e("aftsongpic","onsrvconn");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
            Log.e("aftsongpic","onsrvdisconn");
        }
    };

    @Override
    protected void onStart() {
        Log.e("aftsongpic","start");
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);


        }
    }



    public void songPicked(View view){
/*
        int color1 = Color.parseColor("#8AF3F3");
        for(int i=0;i<songList.size();i++)
        {
            view.setBackgroundColor(color1);
        }
*/


        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        Log.e("aftsongpic","songpic");


            musicSrv.playSong();
            Log.e("aftsongpic","plysng");

/*
        if(musicSrv.isPng() == true)
        {
            int color = Color.parseColor("#E19D36");
            view.setBackgroundColor(color);
            Log.e("aftsongpic" , "colour");
        }
        else
        {
            int color1 = Color.parseColor("#8AF3F3");
            view.setBackgroundColor(color1);
            Log.e("aftsongpic" , "colour1");
        }

*/

            if (playbackPaused) {
                setController();
                Log.e("aftsongpic","stctrl");
                playbackPaused = false;
                Log.e("aftsongpic","plb paused");
            }

        int Posn = musicSrv.getPosn();
        //int songPosn = p1.getCurrentPosition();
        Song plySong = songList.get(Posn);

        String songTitle = plySong.getTitle();
        String songArtist = plySong.getArtist();
        long currSong = plySong.getID();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try {
            p1.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }


        int color = Color.parseColor("#74C9CB");
        int color1 = Color.parseColor("#8AF3F3");
       // int position = musicSrv.getPosn();
        //musicSrv.setSong(position);
       // int position = getCurrentPosition();
        int position;
        if(musicSrv!=null && musicBound==true && musicSrv.isPng())
        {
            position = musicSrv.getPosn();
            Log.e("aftsongpic","setpos");
        }
        else
        {
            position = 0;
        }
        String log1 = Integer.toString(position);
        Log.e("aftsongpic",log1);
        for (int i = 0; i < songList.size(); i++) {
            if (i == position) {
                View v1 =  songView.getChildAt(i);
               // v1.setBackgroundColor(color);
                Log.e("aftsongpic" , "colour");
            }// else {
               // View v2 =  songView.getChildAt(i);
               // v2.setBackgroundColor(color1);
                //Log.e("aftsongpic" , "colour1");
            //}
        }


        Intent i = new Intent(MainActivity.this, MusicService.class);
        //String strName = null;
        //i.putExtra(Intent.EXTRA_TEXT, strName);
        i.putExtra("name", songTitle);
        i.putExtra("name1" , songArtist);
        startService(i);

        controller.getBottom();
        controller.getScaleX();
        controller.getScaleY();
            controller.show(0);


        Log.e("aftsongpic","cshow");




      //  songView = findViewById(R.id.song_list);
        //songList = new ArrayList<Song>();
        //songView.getDefaultFocusHighlightEnabled();


        //int color = Color.parseColor("#E3AB33");
        //view.setBackgroundColor(color);
       // view.setHovered(true);





    }



    @Override
    protected void onDestroy() {
        if (musicBound) unbindService(musicConnection);
        stopService(playIntent);
        Log.e("aftsongpic","ondest");
        musicSrv=null;
        super.onDestroy();
        Log.e("aftsongpic","ondest1");
    }

    private void setController(){
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        int color11 = Color.parseColor("#087689");
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
        controller.getSolidColor();
        controller.setBottom(0);

        //controller.setBackgroundColor(color11);
        //controller.setBottom(0);

    }

    private void playNext(){




            musicSrv.playNext();
            Log.e("aftsongpic","plynxt");




        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
        controller.getBottom();
        controller.getScaleX();
        controller.getScaleY();
    }

    private void playPrev(){

            musicSrv.playPrev();
            Log.e("aftsongpic","plyprev");


        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
        controller.getBottom();
        controller.getScaleX();
        controller.getScaleY();
    }

    @Override
    public void start() {

            musicSrv.go();
            Log.e("aftsongpic","msgo");


    }

    @Override
    public void pause() {
        playbackPaused=true;

            musicSrv.pausePlayer();
            Log.e("aftsongpic","mspauseply");


    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound ==true && musicSrv.isPng())
        return musicSrv.getDur();
  else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound==true && musicSrv.isPng())
        return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {

            musicSrv.seek(pos);
            Log.e("aftsongpic","mspauseply1");


    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound==true)
        return musicSrv.isPng();
        return false;
    }
    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    protected void onPause(){
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                    musicSrv.setShuffle();
                    Log.e("aftsongpic","msshuffle");
                    controller.show();
                Toast t3 = Toast.makeText(getApplicationContext(),
                        "Press Shuffle Button Again To Enter/Exit Shuffle Mode",
                        Toast.LENGTH_SHORT);
                    t3.show();

                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;

        }
        return super.onOptionsItemSelected(item);

    }

    }



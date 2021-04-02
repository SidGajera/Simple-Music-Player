package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<ModelAudio> audioArrayList;
    RecyclerView recyclerView;
    MediaPlayer mediaPlayer;
    double current_pos, total_duration;
    TextView current, total,audio_name;
    ImageView prev, next, pause;
    SeekBar seekBar;
    int audio_index = 0;
    public static final int PERMISSION_READ = 0;
    public static final int PERMISSION_WRITE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPermission()) {
            setAudio();
        }
    }

    private boolean checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int WRITE_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ);
            return false;
        }
        if((WRITE_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE);
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case  PERMISSION_READ: {
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(getApplicationContext(), "Please allow storage permission", Toast.LENGTH_LONG).show();
                    } else {
                        setAudio();
                    }
                }
            }
            case PERMISSION_WRITE: {
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(getApplicationContext(), "Please allow storage permission", Toast.LENGTH_LONG).show();
                    } else {
                        setAudio();
                    }
                }
            }
        }
    }

    private void setAudio() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        GridLayoutManager manager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        current = (TextView) findViewById(R.id.current);
        total = (TextView) findViewById(R.id.total);
        audio_name = (TextView) findViewById(R.id.audio_name);
        prev = (ImageView) findViewById(R.id.prev);
        next = (ImageView) findViewById(R.id.next);
        pause = (ImageView) findViewById(R.id.pause);
        seekBar = (SeekBar) findViewById(R.id.seekbar);

        audioArrayList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();

        getAudioFiles();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                current_pos = seekBar.getProgress();
                mediaPlayer.seekTo((int) current_pos);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audio_index++;
                if (audio_index < (audioArrayList.size())) {
                    playAudio(audio_index);
                } else  {
                    audio_index = 0;
                    playAudio(audio_index);
                }
            }
        });

        if (!audioArrayList.isEmpty()) {
            playAudio(audio_index);
            prevAudio();
            nextAudio();
            setPause();
        }
    }

    private void setPause() {
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    pause.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                } else {
                    mediaPlayer.start();
                    pause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                }
            }
        });
    }

    private void nextAudio() {
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio_index < (audioArrayList.size()-1)) {
                    audio_index++;
                    playAudio(audio_index);
                } else {
                    audio_index = 0;
                    playAudio(audio_index);
                }
            }
        });
    }

    private void prevAudio() {
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio_index > 0) {
                    audio_index--;
                    playAudio(audio_index);
                } else {
                    audio_index = audioArrayList.size() - 1;
                    playAudio(audio_index);
                }
            }
        });
    }

    private void playAudio(int pos) {
        try  {
            mediaPlayer.reset();
            //set file path
            mediaPlayer.setDataSource(this, audioArrayList.get(pos).getAudioUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
            pause.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
            audio_name.setText(audioArrayList.get(pos).getAudioTitle());
            audio_index = pos;
            Log.e("TAG", "playAudio: " + pos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setAudioProgress();
    }

    private void setAudioProgress() {
        current_pos = mediaPlayer.getCurrentPosition();
        total_duration = mediaPlayer.getDuration();

        total.setText(timerConversion((long) total_duration));
        current.setText(timerConversion((long) current_pos));
        seekBar.setMax((int) total_duration);
        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    current_pos = mediaPlayer.getCurrentPosition();
                    current.setText(timerConversion((long) current_pos));
                    seekBar.setProgress((int) current_pos);
                    handler.postDelayed(this, 1000);
                } catch (IllegalStateException ed){
                    ed.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    public String timerConversion(long value) {
        String audioTime;
        int dur = (int) value;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            audioTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            audioTime = String.format("%02d:%02d", mns, scs);
        }
        return audioTime;
    }

    private void getAudioFiles() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                ModelAudio modelAudio = new ModelAudio();
                modelAudio.setAudioTitle(title);
                modelAudio.setAudioArtist(artist);
                modelAudio.setAudioDuration(duration);
                modelAudio.setAudioUri(Uri.parse(url));
                audioArrayList.add(modelAudio);
            } while (cursor.moveToNext());
        }

        AudioAdapter audioAdapter = new AudioAdapter(this ,audioArrayList);
        recyclerView.setAdapter(audioAdapter);

        audioAdapter.setOnItemClickListener(new AudioAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos, View v) {
                playAudio(pos);

                Log.e("TAG", "onItemClick: pos = "+pos+"" +
                        "\n getAudioUri = "+audioArrayList.get(pos).getAudioUri());
            }
        });
    }
}
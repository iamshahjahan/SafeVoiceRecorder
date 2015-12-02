package com.example.khan.safevoicerecorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    String LOG = "RECORDER";
    InetAddress destination = null;
//    layouts to display while starting and stopping the application

    RelativeLayout start;
    RelativeLayout stop;
//   udp packets requirement

    public byte[] buffer;
    public static DatagramSocket socket;
    private int port = 50009;

//    getting audio record class's object
    AudioRecord record;
    private int sampleRate = 16000;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    int minBufSize;
//    = AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat);
    private boolean status = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_microphone);
        toolbar.setTitle(" Safe Audio Recorder");
        setSupportActionBar(toolbar);
        start = (RelativeLayout) findViewById(R.id.start_recording_layout);
        stop = (RelativeLayout) findViewById(R.id.stop_recording_layout);
        stop.setVisibility(View.GONE);
    }


    public void startRecording(View view) {
        start.setVisibility(View.GONE);
        stop.setVisibility(View.VISIBLE);
        minBufSize = AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat);
        status = true;
        Log.d(LOG, "starting recording");
        startStreaming();


    }

    private void startStreaming() {

        Thread startRecord = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket = new DatagramSocket();
                        } catch (SocketException e) {
                            e.printStackTrace();
                        }

                        buffer = new byte[minBufSize];

                        DatagramPacket packet;

                        try {
                             destination = InetAddress.getByName("10.31.0.12");
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }

                        record = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize * 10);

                        record.startRecording();

                        while ( status )
                        {
                            minBufSize = record.read(buffer,0,buffer.length);
                            packet = new DatagramPacket(buffer,buffer.length,destination,port);
                            Log.d(LOG,"Writing in status " + destination );
                            try {
                                Log.d(LOG, "Trying to send. " + minBufSize);
                                socket.send(packet);
                                Log.d(LOG, "Sent successfully.");
                            } catch (IOException e) {

                                Log.d(LOG,"Socket is not sending properly.");
                                e.printStackTrace();
                            }
                        }

                    }
                }
        );
        startRecord.start();
    }


    public void stopRecording(View view) {
        start.setVisibility(View.VISIBLE);
        stop.setVisibility(View.GONE);
        status = false;
        Log.d(LOG,"I am about to release the record.");
        record.release();
        Log.d(LOG, "I have released.");
    }
}

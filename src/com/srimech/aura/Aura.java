package com.srimech.aura;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.media.AudioTrack;
import android.media.AudioManager;
import android.media.AudioFormat;
import android.content.Context;
import android.os.Message;

import android.os.Bundle;
import android.util.*;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

public class Aura extends Activity
{

  // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
  // and modified by Steve Pomeroy <steve@staticfree.info>
  private final int sampleRate = 44100;
  private int oldVolume = 0;
  private byte generatedSnd[];
  private final String LOG_TAG = "Aura";

  private int numSamples = 0;

  Handler handler = new Handler();
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    final Button leftButton = (Button) findViewById(R.id.leftbutton);
    if(leftButton != null) {
      leftButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          genTone(1.5,1.5,10);
          playSound();
        }
      });
    }
    final Button updateIPButton = (Button) findViewById(R.id.updateIPbutton);
    if(updateIPButton != null) {
      updateIPButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {




          updateIP();
        }
      });
    }

    final SeekBar lseekbar = (SeekBar) findViewById(R.id.leftseek);
    final SeekBar rseekbar = (SeekBar) findViewById(R.id.rightseek);
    final Handler handler = new Handler() {
      public void handleMessage(Message msg) {
        int left = msg.arg1;
        lseekbar.setProgress(left);
        int right = msg.arg2;
        rseekbar.setProgress(right);
        Bundle d = msg.getData();
        int duration = d.getInt("duration");

        // Finally, drive...
        genTone(1.5,1.5,duration);
        playSound();
      }
    };
    ListenerThread lt = new ListenerThread(handler);
    lt.start();
  }



  @Override
  protected void onResume() {
    super.onResume();
    
    AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    oldVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);


    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

  }
  
  @Override
  protected void onPause() {
    super.onPause();
    AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolume, 0);

  }


  void updateIP() {
    // Start a new thread to receive packets

    TextView ipView = (TextView) findViewById(R.id.ipaddr);
    String addr = getLocalIpAddress();
    if(addr == null) {
      ipView.setText("Unknown host!");
    }
    else
    {
      ipView.setText(addr);

    }
  }



  private class ListenerThread extends Thread {
    Handler mHandler;
    final static int STATE_DONE = 0;
    final static int STATE_RUNNING = 1;
    int mState;
    ListenerThread(Handler h) {
      mHandler = h;
    }
       
    public void run() {
      mState = STATE_RUNNING;   
      DatagramSocket clientSocket = null;
      byte[] receiveData = new byte[256];;
      DatagramPacket receivePacket = null;
      try {
        clientSocket = new DatagramSocket(6502);
        receivePacket = new DatagramPacket(receiveData, 3);
      } catch (IOException e) {
        return;
      }
      
      while (mState == STATE_RUNNING) {
        try {
          // Receive
          
          clientSocket.receive(receivePacket);  // Does this block?
        } catch (UnknownHostException e) {
        } catch (IOException e) {
        }
        Message msg = mHandler.obtainMessage();
        msg.arg1 = receiveData[0];
        msg.arg2 = receiveData[1];
        Bundle d = new Bundle();
        d.putInt("duration",receiveData[2]);
        msg.setData(d);
        mHandler.sendMessage(msg);
      }
    }
        
  }
  void genTone(double leftx, double rightx, int duration)
  {
    int roundedDuration = (duration-1)/10+1;
    int actualDuration = duration*sampleRate/10;
    numSamples = roundedDuration * sampleRate;
    generatedSnd = new byte[4*numSamples];   
    // Get value of seek 
    SeekBar lseekbar = (SeekBar) findViewById(R.id.leftseek);
    SeekBar rseekbar = (SeekBar) findViewById(R.id.rightseek);
    int left = lseekbar.getProgress();
    int right = rseekbar.getProgress();

    // convert to 16 bit pcm sound array
    // assumes the sample buffer is normalised.
    int idx = 0;
    int i;
    for (i=0;i<numSamples;i++) {
      // scale to maximum amplitude
      int mod = i % 882;
      short val = 0;
      int pulselen = 44 + 44*left/100;
      if(mod > 44 && mod <= 44+pulselen && i<actualDuration) {
        val = -32768;
      }

      // in 16 bit wav PCM, first byte is the low order byte
      generatedSnd[idx++] = (byte) (val & 0x00ff);
      generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

      val = 0;
      pulselen = 44 + 44*right/100;
      if(mod > 244 && mod <= 244+pulselen && i<actualDuration) {
        val = -32768;
      }

      // in 16 bit wav PCM, first byte is the low order byte
      generatedSnd[idx++] = (byte) (val & 0x00ff);
      generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

    }
  }
  
  
  String getLocalIpAddress() 
  {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements() ;) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          if (!inetAddress.isLoopbackAddress()) {
            return inetAddress.getHostAddress().toString();
          }
        }
      }
    } catch (SocketException ex) {
      Log.e(LOG_TAG, ex.toString());
    }
    return null;
  }
  
  
  void playSound(){
    final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                                                 sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                                                 AudioFormat.ENCODING_PCM_16BIT, numSamples,
                                                 AudioTrack.MODE_STATIC);
    audioTrack.write(generatedSnd, 0, generatedSnd.length);
    audioTrack.play();
  }
}

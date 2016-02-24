package beartooth.com.simpleaudio.audio.android;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import beartooth.com.simpleaudio.audio.AudioSystem;
import beartooth.com.simpleaudio.audio.RecordCallback;

/**
 * Created by root on 1/5/16.
 */
public class AndroidAudioSystem implements AudioSystem {

  public static final int DEFAULT_SAMPLE_RATE = 8000;
  public static final int DEFAULT_CHANNEL_COUNT = 1;
  public static final int DEFAULT_SAMPLE_RESOLUTION = 16;

  private AudioTrackProvider trackProvider;
  private AudioTrack audioTrack;
  private LinkedBlockingQueue<byte[]> playbackQueue;
  private Thread playbackThread;

  private AudioRecordProvider recordProvider;
  private AudioRecord audioRecord;
  private RecordCallback recordCallback;
  private AtomicBoolean recordRun;
  private Thread recordThread;

  private int sampleRate;
  private int channelCount;
  private int sampleResolution;

  public AndroidAudioSystem(AudioTrackProvider trackProvider, AudioRecordProvider recordProvider) {
    this.trackProvider = trackProvider;
    this.recordProvider = recordProvider;
    this.recordRun = new AtomicBoolean(true);
    recordCallback = getEmptyCallback();
    sampleRate = DEFAULT_SAMPLE_RATE;
    channelCount = DEFAULT_CHANNEL_COUNT;
    sampleResolution = DEFAULT_SAMPLE_RESOLUTION;
    initPlayback();
    initRecording();
  }

  @Override
  public void play(byte[] samples) {
    if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
      Log.println(Log.ASSERT, "play", "playing track");
      audioTrack.play();
    }
    try {
      playbackQueue.put(samples);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void record(int chunkSize) {
    if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
      return;
    }
    recordRun.getAndSet(true);
    recordThread = new Thread(getRecordThreadWork(chunkSize));
    recordThread.start();
    audioRecord.startRecording();
  }

  @Override
  public void setRecordCallback(RecordCallback callback) {
    this.recordCallback = callback;
  }

  @Override
  public void stopRecord() {
    audioRecord.stop();
    recordRun.getAndSet(false);
    recordThread = null;
  }

  public AudioTrack getAudioTrack() {
    return audioTrack;
  }

  public Thread getPlaybackThread() {
    return playbackThread;
  }

  public AudioRecord getAudioRecord() {
    return audioRecord;
  }

  public RecordCallback getRecordCallback() {
    return recordCallback;
  }

  public int getSampleRate() {
    return sampleRate;
  }

  public void setSampleRate(int sampleRate) {
    this.sampleRate = sampleRate;
    initPlayback();
    initRecording();
  }

  public int getChannelCount() {
    return channelCount;
  }

  public void setChannelCount(int channelCount) {
    this.channelCount = channelCount;
    initPlayback();
    initRecording();
  }

  public int getSampleResolution() {
    return sampleResolution;
  }

  public void setSampleResolution(int sampleResolution) {
    this.sampleResolution = sampleResolution;
    initPlayback();
    initRecording();
  }

  private void initPlayback() {
    audioTrack = trackProvider.getAudioTrack(
        sampleRate,
        getAndroidPlaybackChannelMask(),
        getAndroidEncoding());
    if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
      throw new IllegalStateException("AudioTrack failed to initialize.");
    }
    playbackQueue = new LinkedBlockingQueue<>();
    playbackThread = new Thread(getPlaybackThreadWork());
    playbackThread.setPriority(Thread.MAX_PRIORITY);
    playbackThread.start();
  }

  private void initRecording() {
    audioRecord = recordProvider.getAudioRecord(
        sampleRate,
        getAndroidRecordChannelMask(),
        getAndroidEncoding()
    );
    if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
      // do you have the RECORD_AUDIO permission? (is permission accepted for app on android M)
      throw new IllegalStateException("AudioRecord failed to initialize.");
    }

  }

  private int getAndroidPlaybackChannelMask() {
    switch (channelCount) {
      case 2:
        return AudioFormat.CHANNEL_OUT_STEREO;
      default:
        return AudioFormat.CHANNEL_OUT_MONO;
    }
  }

  private int getAndroidRecordChannelMask() {
    switch (channelCount) {
      case 2:
        return AudioFormat.CHANNEL_IN_STEREO;
      default:
        return AudioFormat.CHANNEL_IN_MONO;
    }
  }

  private int getAndroidEncoding() {
    switch (sampleResolution) {
      case 8:
        return AudioFormat.ENCODING_PCM_8BIT;
      default:
        return AudioFormat.ENCODING_PCM_16BIT;
    }
  }

  private Runnable getPlaybackThreadWork() {
    return new Runnable() {
      @Override
      public void run() {
        while (true) {
          try {
            byte[] toPlay = playbackQueue.take();
            int wrote = audioTrack.write(toPlay, 0, toPlay.length);
          } catch (InterruptedException e) {
            break;
          }
        }
      }
    };
  }

  private Runnable getRecordThreadWork(final int chunkSize) {
    return new Runnable() {
      byte[] chunk = new byte[chunkSize];
      @Override
      public void run() {
        while(recordRun.get()) {
          audioRecord.read(chunk, 0, chunkSize);
          recordCallback.onData(chunk);
        }
      }
    };
  }

  private RecordCallback getEmptyCallback() {
    return new RecordCallback() {
      @Override
      public void onData(byte[] recordedSamples) {
      }
    };
  }

  @Override
  public void finalize() throws Throwable {
    playbackThread.interrupt();
    recordRun.getAndSet(false);
    super.finalize();
  }
}

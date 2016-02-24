package beartooth.com.simpleaudio.audio.android;

import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by root on 1/5/16.
 */
public class DefaultAudioRecordProvider implements AudioRecordProvider {

  @Override
  public AudioRecord getAudioRecord(int sampleRate, int channelConfig, int encoding) {
    int bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        channelConfig,
        encoding);
    bufferSize = bufferSize <= 0 ? 960 : bufferSize * 2;
    return new AudioRecord(
        MediaRecorder.AudioSource.DEFAULT,
        sampleRate,
        channelConfig,
        encoding,
        bufferSize
    );
  }
}

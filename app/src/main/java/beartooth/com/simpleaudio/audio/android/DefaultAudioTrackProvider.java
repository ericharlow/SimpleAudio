package beartooth.com.simpleaudio.audio.android;

import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created by root on 1/5/16.
 */
public class DefaultAudioTrackProvider implements AudioTrackProvider {

  @Override
  public AudioTrack getAudioTrack(int sampleRate, int channelConfig, int encoding) {
//    int bufferSize = AudioTrack.getMinBufferSize(
//        sampleRate,
//        channelConfig,
//        encoding);
//    bufferSize = bufferSize <= 0 ? 960*5 : bufferSize * 5;
    /*
      Using this buffer size to overcome the BM77 taking breaks during transmission,
      A better approach is needed.
    */
    int bufferSize = 6400;
    return new AudioTrack(
        AudioManager.STREAM_MUSIC,
        sampleRate,
        channelConfig,
        encoding,
        bufferSize,
        AudioTrack.MODE_STREAM
    );
  }
}

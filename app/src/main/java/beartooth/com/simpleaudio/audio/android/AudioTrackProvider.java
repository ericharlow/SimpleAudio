package beartooth.com.simpleaudio.audio.android;

import android.media.AudioTrack;

/**
 * Created by root on 1/5/16.
 */
public interface AudioTrackProvider {

  AudioTrack getAudioTrack(int sampleRate, int channelConfig, int encoding);

}

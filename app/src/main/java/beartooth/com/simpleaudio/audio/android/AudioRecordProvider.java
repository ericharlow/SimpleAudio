package beartooth.com.simpleaudio.audio.android;

import android.media.AudioRecord;

/**
 * Created by root on 1/5/16.
 */
public interface AudioRecordProvider {

  AudioRecord getAudioRecord(int sampleRate, int channelConfig, int encoding);

}

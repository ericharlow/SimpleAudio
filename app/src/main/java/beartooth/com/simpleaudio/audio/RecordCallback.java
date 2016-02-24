package beartooth.com.simpleaudio.audio;

/**
 * Created by root on 1/5/16.
 */
public interface RecordCallback {

  void onData(byte[] recordedSamples);

}

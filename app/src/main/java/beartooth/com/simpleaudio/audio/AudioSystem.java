package beartooth.com.simpleaudio.audio;

/**
 * Created by root on 1/5/16.
 */
public interface AudioSystem {

  void play(byte[] samples);

  void record(int chunkSize);

  void setRecordCallback(RecordCallback callback);

  void stopRecord();


}

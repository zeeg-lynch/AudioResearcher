/**
 * Created by dmytrocherednyk on 04.02.16.
 */

import javazoom.jl.player.Player;
import org.tritonus.share.sampled.TAudioFormat;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.sound.sampled.*;
import java.io.*;
import java.util.Map;

public class JavaSoundTest {

    private static String fileName = "Star Wars - The Imperial March.mp3";

    public void testPlay(String filename)
    {
        try {
            File file = new File(filename);
            AudioInputStream in= AudioSystem.getAudioInputStream(file);
            AudioInputStream din = null;
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
            din = AudioSystem.getAudioInputStream(decodedFormat, in);
            // Play now.
            rawplay(decodedFormat, din);
            in.close();
        } catch (Exception e)
        {
            //Handle exception.
        }
    }

    private void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException,                                                                                                LineUnavailableException
    {
        byte[] data = new byte[4096];
        SourceDataLine line = getLine(targetFormat);
        if (line != null)
        {
            // Start
            line.start();
            int nBytesRead = 0, nBytesWritten = 0;
            while (nBytesRead != -1)
            {
                nBytesRead = din.read(data, 0, data.length);
                if (nBytesRead != -1) nBytesWritten = line.write(data, 0, nBytesRead);
            }
            // Stop
            line.drain();
            line.stop();
            line.close();
            din.close();
        }
    }

    private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException
    {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }

    public static void main(String[] args) {

        /*JavaSoundTest javaSoundTest = new JavaSoundTest();
        javaSoundTest.testPlay(fileName);*/

        File f = new File(fileName);
        MediaLocator ml = null;
        javax.media.Player p = null;
        try {
            ml = new MediaLocator(f.toURL());

            p = Manager.createPlayer(ml);
            p.start();
            System.out.println(p.getMediaTime().getSeconds());
            /*while (true) {
                Thread.sleep(1000);

                System.out.println("Media Time :: "+p.getMediaTime().getSeconds());
                System.out.println("Duration :: "+p.getDuration().getSeconds());
                if(p.getMediaTime().getSeconds() == p.getDuration().getSeconds())
                    break;
            }*/
            p.stop();
            p.deallocate();
            p.close();
        } catch (NoPlayerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } /*catch (InterruptedException e) {
            e.printStackTrace();
        }*/


        /*File file = new File(fileName);
        AudioFileFormat baseFileFormat = null;
        AudioFormat baseFormat = null;
        try {
            baseFileFormat = AudioSystem.getAudioFileFormat(file);
        baseFormat = baseFileFormat.getFormat();
// TAudioFileFormat properties
        if (baseFileFormat instanceof TAudioFileFormat)
        {
            Map properties = ((TAudioFileFormat)baseFileFormat).properties();
            String key = "author";
            String val = (String) properties.get(key);
            key = "mp3.id3tag.v2";
            InputStream tag= (InputStream) properties.get(key);
        }
// TAudioFormat properties
        if (baseFormat instanceof TAudioFormat)
        {
            Map properties = ((TAudioFormat)baseFormat).properties();
            String key = "bitrate";
            Integer val = (Integer) properties.get(key);
        }
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

}

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Chars;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;
import javafx.util.converter.ByteStringConverter;

import javax.sound.sampled.*;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by dmytrocherednyk on 15.01.16.
 */
public class MusicReader extends Application {
    public static void main(String[] args) {


        byte tempBuffer[] = new byte[10000];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream input = null;

        File mp3 = new File("Fit For Rivals - Crash.mp3");
        try {
                input = new FileInputStream(mp3);
            int len;
            while((len = input.read(tempBuffer)) > 0) {
                out.write(tempBuffer, 0, len);
            }

            out.close();
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // FFT code below ...
        byte audio[] = out.toByteArray();
        // ...

//        for (byte b : audio) {
//            System.out.print(b + "\t");
//        }

        System.out.println(audio.length);

        /*System.out.println("Running");
        System.out.println(System.getProperty("java.version"));
        final AudioFileFormat.Type [] types = AudioSystem.getAudioFileTypes();
        for (final AudioFileFormat.Type t : types) {
            System.out.println("Returning Type : " + t);
        } // End of the for //
        final String PATH = "Fit For Rivals - Crash.mp3";
        final File file = new File(PATH);
        final AudioInputStream in;
        try {
            in = AudioSystem.getAudioInputStream(file);
            AudioInputStream din = null;
            final AudioFormat baseFormat = in.getFormat();
            final AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);

            System.out.println("Channels : " + baseFormat.getChannels());
//            din = AudioSystem.getAudioInputStream(decodedFormat, in);
//            rawplay(decodedFormat, din);
            in.close();
            System.out.println("Done");
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        drawHistogram(audio);
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Drawing Operations Test");
        Group root = new Group();
        Canvas canvas = new Canvas(300, 250);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawShapes(gc);
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private static byte getMaxOfByteArray(byte[] array) {
        if (array.length==0) {
            throw new ArrayIndexOutOfBoundsException("THE ARRAY IS EMPTY, LOL");
        }
        byte max = array[0];
        for (int i = 1; i < array.length-1; i++) {
            max = (max>array[i])?max:array[i];
        }
        return max;
    }

    private static byte getMinOfByteArray(byte[] array) {
        if (array.length==0) {
            throw new ArrayIndexOutOfBoundsException("THE ARRAY IS EMPTY, LOL");
        }
        byte min = array[0];
        for (int i = 1; i < array.length-1; i++) {
            min = (min<array[i])?min:array[i];
        }
        return min;
    }

    private static void drawHistogram(byte[] bytes) {
        System.out.println("MAX: " + getMaxOfByteArray(bytes));
        System.out.println("MIN: " + getMinOfByteArray(bytes));
    }

    private void drawShapes(GraphicsContext gc) {
        gc.setFill(Color.GREEN);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(5);
        gc.strokeLine(40, 10, 10, 40);
        gc.fillOval(10, 60, 30, 30);
        gc.strokeOval(60, 60, 30, 30);
        gc.fillRoundRect(110, 60, 30, 30, 10, 10);
        gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
        gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
        gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
        gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
        gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
        gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
        gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
        gc.fillPolygon(new double[]{10, 40, 10, 40},
                new double[]{210, 210, 240, 240}, 4);
        gc.strokePolygon(new double[]{60, 90, 60, 90},
                new double[]{210, 210, 240, 240}, 4);
        gc.strokePolyline(new double[]{110, 140, 110, 140},
                new double[]{210, 210, 240, 240}, 4);
    }
}

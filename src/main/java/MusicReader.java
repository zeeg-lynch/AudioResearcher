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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by dmytrocherednyk on 15.01.16.
 */
public class MusicReader extends Application {

    private static byte[] audio;
    private static Random random = new Random();

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
        audio = out.toByteArray();
        // ...

//        for (byte b : audio) {
//            System.out.print(b + "\t");
//        }

        System.out.println(audio.length);



            double[] FFTResult = FFT(cutTheHeader(audio));
            /*System.out.println("PRINITNG THE FFT RESULT:");
            for (double v : FFTResult) {
                System.out.print(v + "\t");
            }*/


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

        /*try {
            createResizedAudioFromByteArray(audio, 0.5);
        } catch (IOException e) {
            e.printStackTrace();
        }

        


        launch(args);*/

    }





    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Drawing Operations Test");
        Group root = new Group();

        Canvas canvas = new Canvas(1000, 255);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawHistogram(audio,gc);
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    static double[] readFully(File file)
            throws UnsupportedAudioFileException, IOException {
        AudioInputStream in = AudioSystem.getAudioInputStream(file);
        AudioFormat     fmt = in.getFormat();

        byte[] bytes;
        try {
            if(fmt.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                throw new UnsupportedAudioFileException();
            }

            // read the data fully
            bytes = new byte[in.available()];
            in.read(bytes);
        } finally {
            in.close();
        }

        int   bits = fmt.getSampleSizeInBits();
        double max = Math.pow(2, bits - 1);

        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(fmt.isBigEndian() ?
                ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

        double[] samples = new double[bytes.length * 8 / bits];
        // convert sample-by-sample to a scale of
        // -1.0 <= samples[i] < 1.0
        for(int i = 0; i < samples.length; ++i) {
            switch(bits) {
                case 8:  samples[i] = ( bb.get()      / max );
                    break;
                case 16: samples[i] = ( bb.getShort() / max );
                    break;
                case 32: samples[i] = ( bb.getInt()   / max );
                    break;
                case 64: samples[i] = ( bb.getLong()  / max );
                    break;
                default: throw new UnsupportedAudioFileException();
            }
        }

        return samples;
    }

    private static byte[] cutTheHeader(byte[] byteArray) {
        byte[] cutByteArray = new byte[byteArray.length-128];
        for (int i = 0; i < cutByteArray.length; i++) {
            cutByteArray[i] = byteArray[i];
        }
        return cutByteArray;
    }

    private static double[] cutTheHeader(double[] byteArray) {
        double[] cutByteArray = new double[byteArray.length-128];
        for (int i = 0; i < cutByteArray.length; i++) {
            cutByteArray[i] = byteArray[i];
        }
        return cutByteArray;
    }



    private static void drawHistogram(byte[] bytes, GraphicsContext gc) {
        double xCoord = 0;
        int step = bytes.length/1000;
//        int step = 1;
            gc.setFill(Color.BLACK);
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(1);
        for (int i = 0; i < bytes.length-step; i+=step) {
//        for (int i = 0; i < 1000; i+=step) {
            gc.strokeLine(xCoord,bytes[i]+127,xCoord+1,bytes[i+step]+127);
            System.out.println(bytes[i]);
            xCoord++;
        }
//        gc.strokeLine(0,0,100,50);
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

    private static void createAudioFromByteArray(byte[] array) throws IOException {
        File f = new File(String.valueOf(random.nextInt())+".mp3");
        f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(array);
        fos.flush();
        fos.close();
    }

    private static void createResizedAudioFromByteArray(byte[] array, double resizeCoeff) throws IOException {
        if (resizeCoeff>0 || resizeCoeff<1) {
            byte[] newByteArray = new byte[array.length];
            for (int i = 0; i < newByteArray.length; i++) {
                newByteArray[i] = (byte) (array[i]*resizeCoeff);

            }
            createAudioFromByteArray(newByteArray);
        }
        else {
            System.out.println("ResizeCoeff should be between 0 and 1!");
        }
    }

    /*public static double[] toDoubleArray(byte[] byteArray){
        int times = Double.SIZE / Byte.SIZE;
        double[] doubles = new double[byteArray.length / times];
        for(int i=0;i<doubles.length;i++){
            doubles[i] = ByteBuffer.wrap(byteArray, i*times, times).getDouble();
        }
        return doubles;
    }*/

    private static double[] toDoubleArray(byte[] byteArray) {
        double[] result = new double[byteArray.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = byteArray[i];
        }
        return result;
    }

    private static double[] FFT(byte[] realPart, byte[] imagPart) {
        return fft(toDoubleArray(realPart), toDoubleArray(imagPart), true);
    }

    private static double[] FFT(byte[] byteArray) {
        System.out.println("STARTING THE FFT");
        int chunkSize = 4096;
        System.out.println("Initial array size: " + byteArray.length);
        double[] resultingArray = new double[(byteArray.length/chunkSize)*chunkSize];
        System.out.println("Resulting array size: " + resultingArray.length);
        byte[] realPart = new byte[chunkSize];
        byte[] imaginaryPart = new byte[chunkSize];
        for (int i = 0; i < (byteArray.length/chunkSize)-2; i++) {
            realPart = Arrays.copyOfRange(byteArray,i*chunkSize,(i+1)*chunkSize);
            double[] fftRes = FFT(realPart, imaginaryPart);
            System.out.println("==============================================\n " + (i+1) + " CHUNK OF DATA:");
            for (double resPart : fftRes) {
                System.out.print(resPart + "\t");
            }
            System.arraycopy(fftRes,0,resultingArray,i*chunkSize,fftRes.length);
        }
        return resultingArray;
    }

    private static double[] FFT(double[] doubles) {
        System.out.println("STARTING THE FFT");
        return fft(doubles, new double[doubles.length], true);
    }

    private void provideSpectrumAnalisys(double[] results) {
        /*for(int i = 0; i < results.length; i++) {
            int freq = 1;
            for(int line = 1; line < size; line++) {
                // To get the magnitude of the sound at a given frequency slice
                // get the abs() from the complex number.
                // In this case I use Math.log to get a more managable number (used for color)
                double magnitude = Math.log(results[i][freq].abs()+1);

                // The more blue in the color the more intensity for a given frequency point:
                g2d.setColor(new Color(0,(int)magnitude*10,(int)magnitude*20));
                // Fill:
                g2d.fillRect(i*blockSizeX, (size-line)*blockSizeY,blockSizeX,blockSizeY);

                // I used a improviced logarithmic scale and normal scale:
                if (logModeEnabled && (Math.log10(line) * Math.log10(line)) > 1) {
                    freq += (int) (Math.log10(line) * Math.log10(line));
                } else {
                    freq++;
                }
            }
        }*/
    }



    /**
     * @author Orlando Selenu
     *http://www.wikijava.org/wiki/The_Fast_Fourier_Transform_in_Java_%28part_1%29
     */
    public static double[] fft(final double[] inputReal, double[] inputImag,
                               boolean DIRECT) {
        // - n is the dimension of the problem
        // - nu is its logarithm in base e
        int n = inputReal.length;

        // If n is a power of 2, then ld is an integer (_without_ decimals)
        double ld = Math.log(n) / Math.log(2.0);

        // Here I check if n is a power of 2. If exist decimals in ld, I quit
        // from the function returning null.
        if (((int) ld) - ld != 0) {
            System.out.println("The number of elements is not a power of 2.");
            return null;
        }

        // Declaration and initialization of the variables
        // ld should be an integer, actually, so I don't lose any information in
        // the cast
        int nu = (int) ld;
        int n2 = n / 2;
        int nu1 = nu - 1;
        double[] xReal = new double[n];
        double[] xImag = new double[n];
        double tReal, tImag, p, arg, c, s;

        // Here I check if I'm going to do the direct transform or the inverse
        // transform.
        double constant;
        if (DIRECT)
            constant = -2 * Math.PI;
        else
            constant = 2 * Math.PI;

        // I don't want to overwrite the input arrays, so here I copy them. This
        // choice adds \Theta(2n) to the complexity.
        for (int i = 0; i < n; i++) {
            xReal[i] = inputReal[i];
            xImag[i] = inputImag[i];
        }

        // First phase - calculation
        int k = 0;
        for (int l = 1; l <= nu; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++) {
                    p = bitreverseReference(k >> nu1, nu);
                    // direct FFT or inverse FFT
                    arg = constant * p / n;
                    c = Math.cos(arg);
                    s = Math.sin(arg);
                    tReal = xReal[k + n2] * c + xImag[k + n2] * s;
                    tImag = xImag[k + n2] * c - xReal[k + n2] * s;
                    xReal[k + n2] = xReal[k] - tReal;
                    xImag[k + n2] = xImag[k] - tImag;
                    xReal[k] += tReal;
                    xImag[k] += tImag;
                    k++;
                }
                k += n2;
            }
            k = 0;
            nu1--;
            n2 /= 2;
        }

        // Second phase - recombination
        k = 0;
        int r;
        while (k < n) {
            r = bitreverseReference(k, nu);
            if (r > k) {
                tReal = xReal[k];
                tImag = xImag[k];
                xReal[k] = xReal[r];
                xImag[k] = xImag[r];
                xReal[r] = tReal;
                xImag[r] = tImag;
            }
            k++;
        }

        // Here I have to mix xReal and xImag to have an array (yes, it should
        // be possible to do this stuff in the earlier parts of the code, but
        // it's here to readibility).
        double[] newArray = new double[xReal.length * 2];
        double radice = 1 / Math.sqrt(n);
        for (int i = 0; i < newArray.length; i += 2) {
            int i2 = i / 2;
            // I used Stephen Wolfram's Mathematica as a reference so I'm going
            // to normalize the output while I'm copying the elements.
            newArray[i] = xReal[i2] * radice;
            newArray[i + 1] = xImag[i2] * radice;
        }
        return newArray;
    }

    /**
     * The reference bitreverse function.
     */
    private static int bitreverseReference(int j, int nu) {
        int j2;
        int j1 = j;
        int k = 0;
        for (int i = 1; i <= nu; i++) {
            j2 = j1 / 2;
            k = 2 * k + j1 - 2 * j2;
            j1 = j2;
        }
        return k;
    }
}

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.*;

/**
 * Created by dmytrocherednyk on 15.01.16.
 */
public class MusicAnalyzer extends Application {

    private static byte[] audio;
    private static Random random = new Random();
    private static final int samplingFreq = 44100; //default sampling frequency
    private static final int chunkSize = 256; //size of chunk of data for input to FFT (should be a power of 2)
    private static final double verticalZoom = 0.004;
    private static SpectrumPoint[][] spectrogram = new SpectrumPoint[1000][];
//    private static String fileName = "Star Wars - The Imperial March.mp3";
//    private static String fileName = "Metallica - Star Wars Imperial March.mp3";
    private static String fileName = "Fit For Rivals - Crash.mp3";
//    private static String fileName = "Fit For Rivals - CrashWithoutTags.mp3";
//    private static String fileName = "Moby - Enter the matrixWithoutTags.mp3";
//    private static String fileName = "Moby - Enter the matrix.mp3";
//    private static String fileName = "L's Theme.mp3";
//    private static String fileName = "John Murphy - Don Abandons Alice (OST 28 Weeks Later).mp3";
    private static double sliceBorder = 1.05;
    private static boolean showOnlyPeaks = true;
    private static double[] axesFrequencies = {5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000};
    private static double NyquistFrequency = samplingFreq/2;
    private static double requiredFrequency = 10000;
    private static double searchRange = 5000;
    private static boolean strictSearch = true;
    private static double maxScaledAmplitude;
    private static double scaledSliceBorder;
//    private static String fileName = "Fit For Rivals - Crash.mp3";


    private static class SpectrumPoint {
        private double re;
        private double im;
        private double amplitude;
        private int relativeFrequency;
        private double frequency;
        private double normalizedAmplitude;
        int time;
        Color color;

        public SpectrumPoint(Complex complex, int relativeFrequency, int time) {
            re = complex.re();
            im = complex.im();
            amplitude = getAmplitude();
            normalizedAmplitude = getAmplitudeInvolvingPhase(verticalZoom);
            this.relativeFrequency = relativeFrequency;
            this.frequency = getRealFrequencyFromRelative(relativeFrequency);
            this.time = time;
            this.color = getAmplitudeColorRepresentation();
//            this.color = getAmplitudeFullColorRepresentation();
//            this.color = new Color(1,0,0,0.5);
        }

        private double getRealFrequencyFromRelative(int relativeFrequency) {
            return relativeFrequency*samplingFreq/chunkSize;
        }

        public double getAmplitude() {
            return sqrt(re*re + im*im);
        }

        public double getAmplitudeInvolvingPhase(double verticalZoom) {
//        const float value	= (log10f( sqrtf( (realValue * realValue) + (imagValue * imagValue) ) * rcpVerticalZoom ) + 1.0f) * 0.5f;
            return log10(sqrt(re*re + im*im)*verticalZoom)*0.5;
        }

    /*public double getFrequency() {
//        sqrt( (realValue * realValue) + (imagValue * imagValue) );

    }*/
        private Color getAmplitudeColorRepresentation() {
            double amplitudeForOperation = normalizedAmplitude;
            amplitudeForOperation = abs(amplitudeForOperation);
            if (amplitudeForOperation > 1) {
                amplitudeForOperation = 1;
            }
            try {
                return (amplitudeForOperation < sliceBorder) ? new Color(0,amplitudeForOperation,0,1) : new Color(1,0,0,1);
            }
            catch (IllegalArgumentException e) {
                System.err.println(e.toString());
                System.err.println("AMPLITUDE IS : " + amplitude);
            }
            return new Color(0,0,1,1);
        }

        private Color getAmplitudeFullColorRepresentation() {
            double red;
            double green;
            double blue;
            double amplitudeForOperation = normalizedAmplitude;
            amplitudeForOperation = abs(amplitudeForOperation);
            if (amplitudeForOperation > 1) {
                amplitudeForOperation = 1;
            }
            red = (amplitudeForOperation>0.333) ? 1 : amplitudeForOperation*3;
            green = (amplitudeForOperation>0.666) ? 1 : amplitudeForOperation*1.5;
            blue = amplitudeForOperation;
            return new Color(red,green,blue,1);
        }

        @Override
        public String toString() {
            return "SpectrumPoint{" +
                    "re=" + re +
                    ", im=" + im +
                    ", amplitude=" + amplitude +
                    ", frequency=" + frequency +
                    ", normalizedAmplitude=" + normalizedAmplitude +
                    ", time=" + time +
                    ", color=" + color +
                    '}';
        }
    }

    public static void main(String[] args) {

        /////////////////////////////////////////////////////////////////////////
        audio = getAudioByteStream(fileName);
        // ...

//        for (byte b : audio) {
//            System.out.print(b + "\t");
//        }

        System.out.println(audio.length);



        int curIndex = 0;
        byte[] currentChunk;

        for (int chunks = 0; chunks < spectrogram.length; chunks++) {

            currentChunk = Arrays.copyOfRange(audio, curIndex, curIndex+chunkSize);
            curIndex += chunkSize;
    //        System.out.println(currentChunk.length);


/*
    //            double[] FFTResult = FFT(cutTheHeader(audio));
                System.out.println("PRINITNG THE FFT RESULT:");
                for (double v : FFTResult) {
                    System.out.print(v + "\t");
                }*/


            int sum = 0;
            for (byte b : currentChunk) {
                sum+=b;
            }

            System.out.println("SUM: " + sum);


            Complex[] fftRes = FFT.fft(byteToComplex(currentChunk));
            for (Complex complexNumb : fftRes) {
                System.out.println(complexNumb.toString());
            }

            SpectrumPoint[] spectrum = new SpectrumPoint[fftRes.length];
            double maxAmplitudeInvolvingPhase = 0;
            double maxAmplitude = 0;
            int maxAmplitudeIndex = 0;
            double maxFreq = 0;
            for (int i = 0; i < fftRes.length; i++) {
                spectrum[i] = new SpectrumPoint(fftRes[i], i, chunks);
                double amplitudeInvolvingPhase = spectrum[i].getAmplitudeInvolvingPhase(verticalZoom);
    //            System.out.println("Frequency: " + getFrequencyFromIndex(i, samplingFreq, chunkSize) + "; Value: " + fftRes[i].toString());
                System.out.println("================================\nFrequency: " + getFrequencyFromIndex(i, samplingFreq, chunkSize)
                        + "\nAmplitude: " + spectrum[i].getAmplitude()
                        +  "\nNormalized Amplitude with Phase: " + amplitudeInvolvingPhase);
                System.out.println(spectrum[i].toString() + "\n=========================\n");
//                if (maxAmplitudeInvolvingPhase < spectrum[i].normalizedAmplitude) {
                if (maxAmplitude < spectrum[i].amplitude) {
                    maxAmplitudeInvolvingPhase = spectrum[i].normalizedAmplitude;
                    maxAmplitude = spectrum[i].amplitude;
                    maxAmplitudeIndex = i;
                }
            }
            spectrogram[chunks] = spectrum;
            System.out.println("MAX AMPLITUDE INVOLVING PHASE: " + maxAmplitudeInvolvingPhase);
            System.out.println("MAX AMPLITUDE: " + spectrum[maxAmplitudeIndex].getAmplitude());
            maxScaledAmplitude = maxAmplitudeInvolvingPhase;
            System.out.println(fftRes.length);
        }



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
        }*/

        


        launch(args);

    }

    public static byte[] getAudioByteStream(String fileName) {
        File file = new File(fileName);
        AudioInputStream in= null;
        try {
            in = AudioSystem.getAudioInputStream(file);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        /////////////////////////////////////////////////////////////////////////

        byte tempBuffer[] = new byte[10000];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream input = null;

        File mp3 = new File(fileName);
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
        return audio;
    }


    private static Complex[] doubleToComplex(double[] array) {
        Complex[] result = new Complex[array.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new Complex(array[i], 0);
        }
        return result;
    }

    private static Complex[] byteToComplex(byte[] array) {
        Complex[] result = new Complex[array.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new Complex(array[i], 0);
        }
        return result;
    }




    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Music spectral analyzer");
        GridPane gridPane = new GridPane();
        //time domain
        Canvas timeDomainCanvas = new Canvas(1000, 255);
        GridPane.setConstraints(timeDomainCanvas,0,3,20,1);
        GraphicsContext gc = timeDomainCanvas.getGraphicsContext2D();
        drawTimeDomain(audio,gc);
        gridPane.getChildren().add(timeDomainCanvas);
//        root.getChildren().add(timeDomainCanvas);
        //spectrogram
        Canvas spectrogramCanvas = new Canvas(spectrogram.length, chunkSize+1);
        final int bottom = (int) gridPane.snappedBottomInset();
        final int top = (int) gridPane.snappedTopInset();
        spectrogramCanvas.setLayoutY(top);
        Label topFreqBordLabel = new Label(" Top frequency border: ");
        GridPane.setConstraints(topFreqBordLabel,2,1);
        Label botFreqBordLabel = new Label(" Bottom frequency border: ");
        GridPane.setConstraints(botFreqBordLabel,0,1);
        TextField bottomFreqBorder = new TextField("7000");
        GridPane.setConstraints(bottomFreqBorder,1,1);
        TextField topFreqBorder = new TextField("9000");
        GridPane.setConstraints(topFreqBorder,3,1);
        Label songNameLabel = new Label(" Song name: ");
        GridPane.setConstraints(songNameLabel,5,1);
        TextField songNameField = new TextField("Fit For Rivals - Crash.mp3");
        GridPane.setConstraints(songNameField,6,1);
        Label amplPercentageLabel = new Label(" Amplitude percentage: ");
        GridPane.setConstraints(amplPercentageLabel,0,2);
        TextField amplPercentage = new TextField("85");
        GridPane.setConstraints(amplPercentage,1,2);
        Label matchAmountLabel = new Label(" Points amount to match: ");
        GridPane.setConstraints(matchAmountLabel,2,2);
        TextField matchAmount = new TextField("20");
        GridPane.setConstraints(matchAmount,3,2);
        Button searchButton = new Button("Search");
        GridPane.setConstraints(searchButton,6,2);


        GridPane.setConstraints(spectrogramCanvas,0,0,20,1);
        GraphicsContext spectrogramGraphicsContext = spectrogramCanvas.getGraphicsContext2D();
        drawSpectrogram(spectrogram,spectrogramGraphicsContext);
        gridPane.getChildren().add(spectrogramCanvas);
        gridPane.getChildren().add(topFreqBordLabel);
        gridPane.getChildren().add(topFreqBorder);
        gridPane.getChildren().add(botFreqBordLabel);
        gridPane.getChildren().add(songNameLabel);
        gridPane.getChildren().add(songNameField);
        gridPane.getChildren().add(bottomFreqBorder);
        gridPane.getChildren().add(amplPercentageLabel);
        gridPane.getChildren().add(amplPercentage);
        gridPane.getChildren().add(matchAmountLabel);
        gridPane.getChildren().add(matchAmount);
        gridPane.getChildren().add(searchButton);
//        gridPane.getChildren().add(bottomFreqBorder);
//        gridPane.getChildren().add(spectrogramCanvas);
//        root.getChildren().add(spectrogramCanvas);
        primaryStage.setScene(new Scene(gridPane));
        primaryStage.show();
        System.out.println("SPECTROGRAM IS SHOWN");
        ArrayList<SpectrumPoint> fingerPrintFromSpectrogram = getFingerPrintFromSpectrogram(spectrogram, 0.61);
        System.out.println("FINGERPRINT SIZE: " + fingerPrintFromSpectrogram.size());
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
        int chunkSize = 512;
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




   /* *//**
     *http://knowm.org/exploring-bird-song-with-a-spectrogram-in-java/
     * *//*

    private void buildSpectrogram() {

        short[] amplitudes = wave.getSampleAmplitudes();
        int numSamples = amplitudes.length;

        int pointer = 0;

        // overlapping
        if (overlapFactor > 1) {

            int numOverlappedSamples = numSamples * overlapFactor;
            int backSamples = fftSampleSize * (overlapFactor - 1) / overlapFactor;
            int fftSampleSize_1 = fftSampleSize - 1;
            short[] overlapAmp = new short[numOverlappedSamples];
            pointer = 0;
            for (int i = 0; i < amplitudes.length; i++) {
                overlapAmp[pointer++] = amplitudes[i];
                if (pointer % fftSampleSize == fftSampleSize_1) {
                    // overlap
                     i -= backSamples;
                }
            }
            numSamples = numOverlappedSamples;
            amplitudes = overlapAmp;
        }

        numFrames = numSamples / fftSampleSize;
        framesPerSecond = (int) (numFrames / wave.getLengthInSeconds());

        // set signals for fft
        WindowFunction window = new WindowFunction();
        double[] win = window.generate(WindowType.HAMMING, fftSampleSize);

        double[][] signals = new double[numFrames][];
        for (int f = 0; f < numFrames; f++) {
            signals[f] = new double[fftSampleSize];
            int startSample = f * fftSampleSize;
            for (int n = 0; n < fftSampleSize; n++) {
                signals[f][n] = amplitudes[startSample + n] * win[n];
            }
        }

        absoluteSpectrogram = new double[numFrames][];
        // for each frame in signals, do fft on it
        FastFourierTransform fft = new FastFourierTransform();
        for (int i = 0; i < numFrames; i++) {
            absoluteSpectrogram[i] = fft.getMagnitudes(signals[i]);
        }

        if (absoluteSpectrogram.length > 0) {

            numFrequencyUnit = absoluteSpectrogram[0].length;
            frequencyBinSize = (double) wave.getWaveHeader().getSampleRate() / 2 / numFrequencyUnit; // frequency could be caught within the half of
            // nSamples according to Nyquist theory
            frequencyRange = wave.getWaveHeader().getSampleRate() / 2;

            // normalization of absoultSpectrogram
            spectrogram = new double[numFrames][numFrequencyUnit];

            // set max and min amplitudes
            double maxAmp = Double.MIN_VALUE;
            double minAmp = Double.MAX_VALUE;
            for (int i = 0; i < numFrames; i++) {
                for (int j = 0; j < numFrequencyUnit; j++) {
                    if (absoluteSpectrogram[i][j] > maxAmp) {
                        maxAmp = absoluteSpectrogram[i][j];
                    }
                    else if (absoluteSpectrogram[i][j] < minAmp) {
                        minAmp = absoluteSpectrogram[i][j];
                    }
                }
            }

            // normalization
            // avoiding divided by zero
            double minValidAmp = 0.00000000001F;
            if (minAmp == 0) {
                minAmp = minValidAmp;
            }

            double diff = Math.log10(maxAmp / minAmp); // perceptual difference
            for (int i = 0; i < numFrames; i++) {
                for (int j = 0; j < numFrequencyUnit; j++) {
                    if (absoluteSpectrogram[i][j] < minValidAmp) {
                        spectrogram[i][j] = 0;
                    }
                    else {
                        spectrogram[i][j] = (Math.log10(absoluteSpectrogram[i][j] / minAmp)) / diff;
                        // System.out.println(spectrogram[i][j]);
                    }
                }
            }
        }
    }*/

    private static double getFrequencyFromIndex(int index, double samplingFreq, double chunkSize) {
//        44100 / 1024
        return index*samplingFreq/chunkSize;
    }

    private static int getIndexFromFrequency(double frequency, double samplingFreq, double chunkSize) {
        return (int) ( frequency/(samplingFreq/chunkSize));
    }

    private static void drawSpectrogram(SpectrumPoint[][] spectrogram, GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.setLineWidth(1);
        scaledSliceBorder = maxScaledAmplitude*sliceBorder;
        //axes
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,spectrogram.length,chunkSize);

        int[] axesFrequenciesCoords = new int[axesFrequencies.length];
        for (int i = 0; i < axesFrequenciesCoords.length; i++) {
            axesFrequenciesCoords[i] = getIndexFromFrequency(axesFrequencies[i], samplingFreq, chunkSize);
            gc.strokeLine(0,axesFrequenciesCoords[i], 10, axesFrequenciesCoords[i]);
            gc.strokeText((axesFrequencies[i]/1000) + " kHz", 12, axesFrequenciesCoords[i]);
        }
        //marking Nyquist Freequency
        int NyquistFreqIndex = getIndexFromFrequency(NyquistFrequency, samplingFreq, chunkSize);
        gc.strokeText("Nyquist Frequency (" + NyquistFrequency + " Hz)", 10, NyquistFreqIndex + 3);
        gc.setStroke(Color.GREEN);
        gc.strokeLine(0,NyquistFreqIndex,spectrogram.length,NyquistFreqIndex);

        double topFreqBorder = requiredFrequency+searchRange/2;
        double bottomFreqBorder = requiredFrequency-searchRange/2;

        //draw spectrogram
        gc.setStroke(Color.RED);
        SpectrumPoint spectrumPoint;
        for (int i = 0; i < spectrogram.length; i++) {
            for (int j = 0; j < spectrogram[i].length; j++) {
                spectrumPoint = spectrogram[i][j];
                if (showOnlyPeaks) {
                    if (spectrumPoint.normalizedAmplitude > scaledSliceBorder) {
                        if (strictSearch) {
                            if (spectrumPoint.frequency < topFreqBorder && spectrumPoint.frequency > bottomFreqBorder) {
                                gc.strokeRect(spectrumPoint.time, spectrumPoint.relativeFrequency, 5, 5);
                            }
                        }
                        else {

                            gc.strokeRect(spectrumPoint.time, spectrumPoint.relativeFrequency, 5, 5);
                        }
                    }
                }
                else {
                    gc.setStroke(spectrumPoint.color);
                    gc.strokeRect(spectrumPoint.time, spectrumPoint.relativeFrequency, 2, 2);
                }
//                gc.strokeLine(spectrumPoint.time, spectrumPoint.relativeFrequency, spectrumPoint.time, spectrumPoint.relativeFrequency);
//                System.out.println("DRAWING POINT WITH COORDS " + spectrumPoint.time + " " + spectrumPoint.relativeFrequency + " AND COLOR " + gc.getStroke());
            }
        }
    }


    public ArrayList<SpectrumPoint> getFingerPrintFromSpectrogram(SpectrumPoint[][] spectrogram, double bottomAmplitudeBorder, double topAmplitudeBorder) {
        SpectrumPoint spectrumPoint;
        ArrayList<SpectrumPoint> result = new ArrayList<SpectrumPoint>();
        for (int i = 0; i < spectrogram.length; i++) {
            for (int j = 0; j < spectrogram[i].length; j++) {
                spectrumPoint = spectrogram[i][j];
                if (spectrumPoint.amplitude < topAmplitudeBorder && spectrumPoint.amplitude > bottomAmplitudeBorder) {
                       result.add(spectrumPoint);
                }
            }
        }
        return result;
    }

    public ArrayList<SpectrumPoint> getFingerPrintFromSpectrogram(SpectrumPoint[][] spectrogram, double bottomAmplitudeBorder) {
        SpectrumPoint spectrumPoint;
        ArrayList<SpectrumPoint> result = new ArrayList<SpectrumPoint>();
        for (int i = 0; i < spectrogram.length; i++) {
            for (int j = 0; j < spectrogram[i].length; j++) {
                spectrumPoint = spectrogram[i][j];
                if (spectrumPoint.normalizedAmplitude > bottomAmplitudeBorder) {
                    result.add(spectrumPoint);
                }
            }
        }
        return result;
    }

    public ArrayList<SpectrumPoint> getTopPercentsOfAmplitudes(SpectrumPoint[][] spectrogram, double topPercentsAmount) {
        if (topPercentsAmount < 0 || topPercentsAmount > 1) {
            System.out.println("topPercentsAmount SHOULD BE BETWEEN 0 AND 1");
            return null;
        }
        SpectrumPoint spectrumPoint;
        ArrayList<SpectrumPoint> result = new ArrayList<SpectrumPoint>();
        double sliceBorder = 1 - topPercentsAmount;
        double maxAmplitude = 0;
        for (int i = 0; i < spectrogram.length; i++) {
            for (int j = 0; j < spectrogram[i].length; j++) {
                spectrumPoint = spectrogram[i][j];
                if (maxAmplitude < spectrumPoint.amplitude) {
                    maxAmplitude = spectrumPoint.amplitude;
                }
            }
        }
        System.out.println("MAX AMPLITUDE : " + maxAmplitude);
        //REDO
        for (SpectrumPoint[] spectrumRow : spectrogram) {
            for (SpectrumPoint point : spectrumRow) {
                if (point.getAmplitude()/maxAmplitude > sliceBorder) {
                    result.add(point);
                }
            }
        }
        return result;
    }

    private static void drawTimeDomain(byte[] bytes, GraphicsContext gc) {
        double xCoord = 0;
        int step = bytes.length/1000;
//        int step = 1;
            gc.setFill(Color.BLACK);
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(1);
        for (int i = 0; i < bytes.length-step; i+=step) {
//        for (int i = 0; i < 1000; i+=step) {
            gc.strokeLine(xCoord,bytes[i]+127,xCoord+1,bytes[i+step]+127);
//            System.out.println(bytes[i]);
            xCoord++;
        }
//        gc.strokeLine(0,0,100,50);
    }
}

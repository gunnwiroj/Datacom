package wav;

import java.lang.Math;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class WriteExample
{
    static String fileNameString;
    static File file;
    static String filePath;

    static RandomAccessFile raw;

    static int byteCount = 0;

    static float freq;
    static int sRate = 44100;
    static int bitDepth = 16;
    static int nChannels = 1;
    static int dur;
    static String data;
    static float changeRate;

    public static void main(String[] args)
    {   Scanner in = new Scanner(System.in);
        System.out.print("Frequency : ");
        freq = in.nextInt();
        changeRate = (float)((2.0 * Math.PI * freq) / sRate);
        System.out.print("Duration time (seconds) : ");
        dur = in.nextInt();
        System.out.print("File Name : ");
        String name = in.next();
        fileNameString = name + ".wav";
        file = new File(fileNameString);
        filePath = file.getAbsolutePath();
        //System.out.print("Data : ");
        //data = in.next();
        try
        {
            raw = new RandomAccessFile(filePath, "rw");

            raw.setLength(0); // Set file length to 0, to prevent unexpected behavior in case the file already existed
            raw.writeBytes("RIFF");
            raw.writeInt(0); // Final file size not known yet, write 0. This is = sample count + 36 bytes from header.
            raw.writeBytes("WAVE");
            raw.writeBytes("fmt ");
            raw.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
            raw.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
            raw.writeShort(Short.reverseBytes((short)nChannels));// Number of channels, 1 for mono, 2 for stereo
            raw.writeInt(Integer.reverseBytes(sRate)); // Sample rate
            raw.writeInt(Integer.reverseBytes(sRate*bitDepth*nChannels/8)); // Byte rate, SampleRate*NumberOfChannels*bitDepth/8
            raw.writeShort(Short.reverseBytes((short)(nChannels*bitDepth/8))); // Block align, NumberOfChannels*bitDepth/8
            raw.writeShort(Short.reverseBytes((short)bitDepth)); // Bit Depth
            raw.writeBytes("data");
            raw.writeInt(0); // Data chunk size not known yet, write 0. This is = sample count.
        }
        catch(IOException e)
        {
            System.out.println("I/O exception occured while writing data");
        }

        for (int i = 0; i < sRate*dur; i++)
        {
            writeSample( (float)Math.sin( i * changeRate ) );
        }

        closeFile();
        System.out.println("Finished");
    }

    static void writeSample(float floatValue)
    {
        try 
        {
            char shortSample = (char)( (floatValue)*0x7FFF );
            raw.writeShort(Character.reverseBytes(shortSample));
            byteCount += 2;
        }
        catch(IOException e)
        {
            System.out.println("I/O exception occured while writing data");
        }
    }

    static void closeFile() 
    {
        try 
        {
            raw.seek(4); // Write size to RIFF header
            raw.writeInt(Integer.reverseBytes(byteCount + 36));
            raw.seek(40); // Write size to Subchunk2Size field
            raw.writeInt(Integer.reverseBytes(byteCount));
            raw.close();
        }
        catch(IOException e)
        {
            System.out.println("I/O exception occured while closing output file");
        }
    }
}
package net.iaround.utils;

public class Mp3Lame {
	// LAME
    static {
        System.loadLibrary("mp3lame");
    }    
    public static native void initEncoder(int numChannels, int sampleRate, int bitRate, int mode, int quality);
    public static native void destroyEncoder();
    public static native int encodeBuffer(byte[] input, int len_input, byte[] output, int len_output);
    
} 

package com.ormosia.audio;

//¿ìËÙ¸µÀïÒ¶±ä»»
public class FFT {   
    private int FFT_N_LOG = 0; // FFT_N_LOG <= 13   
    public int FFT_N = 0;   
    private float MINY = 0.0f;
    private float[] real, imag, sintable, costable;   
    private int[] bitReverse;   
  
    public FFT(int nlog) {  
    	FFT_N_LOG = nlog;
    	FFT_N = 1 << FFT_N_LOG;   
        MINY = (float) ((FFT_N << 2) * Math.sqrt(2));
    	
        real = new float[FFT_N];   
        imag = new float[FFT_N];   
        sintable = new float[FFT_N >> 1];   
        costable = new float[FFT_N >> 1];   
        bitReverse = new int[FFT_N];   
  
        int i, j, k, reve;   
        for (i = 0; i < FFT_N; i++) {   
            k = i;   
            for (j = 0, reve = 0; j != FFT_N_LOG; j++) {   
                reve <<= 1;   
                reve |= (k & 1);   
                k >>>= 1;   
            }   
            bitReverse[i] = reve;   
        }   
  
        double theta, dt = 2 * 3.14159265358979323846 / FFT_N;   
        for (i = 0; i < (FFT_N >> 1); i++) {   
            theta = i * dt;   
            costable[i] = (float) Math.cos(theta);
            sintable[i] = (float) Math.sin(theta);
        }   
    }   
  
      
    public void calculate(float[] realIO) {   
        int i, j, k, ir, exchanges = 1, idx = FFT_N_LOG - 1;   
        float cosv, sinv, tmpr, tmpi;   
        for (i = 0; i != FFT_N; i++) {   
            real[i] = realIO[bitReverse[i]];   
            imag[i] = 0;   
        }   
  
        for (i = FFT_N_LOG; i != 0; i--) {   
            for (j = 0; j != exchanges; j++) {   
                cosv = costable[j << idx];   
                sinv = sintable[j << idx];   
                for (k = j; k < FFT_N; k += exchanges << 1) {   
                    ir = k + exchanges;   
                    tmpr = cosv * real[ir] - sinv * imag[ir];   
                    tmpi = cosv * imag[ir] + sinv * real[ir];   
                    real[ir] = real[k] - tmpr;   
                    imag[ir] = imag[k] - tmpi;   
                    real[k] += tmpr;   
                    imag[k] += tmpi;   
                }   
            }   
            exchanges <<= 1;   
            idx--;   
        }   
  
        j = FFT_N >> 1;   
          
        sinv = MINY;   
        cosv = -MINY;   
        for (i = j; i != 0; i--) {   
            tmpr = real[i];   
            tmpi = imag[i];   
            if (tmpr > cosv && tmpr < sinv && tmpi > cosv && tmpi < sinv)   
                realIO[i - 1] = 0;   
            else  
                realIO[i - 1] = tmpr * tmpr + tmpi * tmpi;   
        }   
    }   
}  

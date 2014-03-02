#include <jni.h>
#include "ffta.h"

/*                                                                        */
/*         La estructura de la fft es la siguiente:                       */
/*                                                                        */
/*  ____________________________________________________________________  */
/*  |   |   |  ....           |   |   |   |  ....                   |   | */
/*  |   |   |                 |   |   |   |                         |   | */
/*  --------------------------------------------------------------------- */
/*    0   1                    127 128 129                           256  */
/*                                                                        */ 
/*                                                                        */ 
/*    0: Parte real del primer punto                                      */
/*    1: Parte real del sefundo punto                                     */
/*    127: Parte real del n'umero 128                                     */
/*                                                                        */
/*    128: No v'alido                                                     */
/*                                                                        */
/*    256: Parte imaginaria del primer punto                              */
/*    255: Parte imaginaria del segundo punto                             */   
/*    129: Parte imaginaria del punto 128                                 */
/*                                                                        */
/*    IMPORTANTE: Le tengo  que pasar un vector de 257 reales.            */

 

/*************************************************************************/
/* Does a hartley transform of "256" points in the array "fz". */
/* ** FFT and FHT routines <=> Copyright 1988, 1993; Ron Mayer. */



void Java_android_musicfeel_ActivityGrabadora_fht(float fz[257])
{
   int i,k,k1,k2,k3,k4,kx;
   float *fi,*fn,*gi;
   int *ind, *find;
   float a;

   TRIG_VARS;

   for (ind = puntos, find = puntos + 240; ind < find; ) {
      k1 = *ind++;
      k2 = *ind++;
      a = fz[k1];
      fz[k1] = fz[k2];
      fz[k2] = a;
   }

   for (fi=fz,fn=fz+256;fi<fn;fi+=4) {
      float f0,f1,f2,f3;
      f1     = *fi-fi[1 ];
      f0     = *fi+fi[1 ];
      f3     = fi[2 ]-fi[3 ];
      f2     = fi[2 ]+fi[3 ];
      fi[2 ] = (f0-f2);
      *fi    = (f0+f2);
      fi[3 ] = (f1-f3);
      fi[1 ] = (f1+f3);
   }

 k = 0;
 do
    {
     float s1,c1;
     k  += 2;
     k1  = 1  << k;
     k2  = k1 << 1;
     k4  = k2 << 1;
     k3  = k2 + k1;
     kx  = k1 >> 1;
	 fi  = fz;
	 gi  = fi + kx;
	 fn  = fz + 256;
	 do
	    {
	     float g0,f0,f1,g1,f2,g2,f3,g3;
	     f1      = fi[0 ] - fi[k1];
	     f0      = fi[0 ] + fi[k1];
	     f3      = fi[k2] - fi[k3];
	     f2      = fi[k2] + fi[k3];
	     fi[k2]  = f0	  - f2;
	     fi[0 ]  = f0	  + f2;
	     fi[k3]  = f1	  - f3;
	     fi[k1]  = f1	  + f3;
	     g1      = gi[0 ] - gi[k1];
	     g0      = gi[0 ] + gi[k1];
	     g3      = SQRT2  * gi[k3];
	     g2      = SQRT2  * gi[k2];
	     gi[k2]  = g0	  - g2;
	     gi[0 ]  = g0	  + g2;
	     gi[k3]  = g1	  - g3;
	     gi[k1]  = g1	  + g3;
	     gi     += k4;
	     fi     += k4;
	    } while (fi<fn);
     TRIG_INIT(k,c1,s1);
     for (i=1;i<kx;i++)
	{
	 float c2,s2;
	 TRIG_NEXT(k,c1,s1);
	 c2 = c1*c1 - s1*s1;
	 s2 = 2*(c1*s1);
	     fn = fz + 256;
	     fi = fz +i;
	     gi = fz +k1-i;
	     do
		{
		 float a,b,g0,f0,f1,g1,f2,g2,f3,g3;
		 b       = s2*fi[k1] - c2*gi[k1];
		 a       = c2*fi[k1] + s2*gi[k1];
		 f1      = fi[0 ]    - a;
		 f0      = fi[0 ]    + a;
		 g1      = gi[0 ]    - b;
		 g0      = gi[0 ]    + b;
		 b       = s2*fi[k3] - c2*gi[k3];
		 a       = c2*fi[k3] + s2*gi[k3];
		 f3      = fi[k2]    - a;
		 f2      = fi[k2]    + a;
		 g3      = gi[k2]    - b;
		 g2      = gi[k2]    + b;
		 b       = s1*f2     - c1*g3;
		 a       = c1*f2     + s1*g3;
		 fi[k2]  = f0        - a;
		 fi[0 ]  = f0        + a;
		 gi[k3]  = g1        - b;
		 gi[k1]  = g1        + b;
		 b       = c1*g2     - s1*f3;
		 a       = s1*g2     + c1*f3;
		 gi[k2]  = g0        - a;
		 gi[0 ]  = g0        + a;
		 fi[k3]  = f1        - b;
		 fi[k1]  = f1        + b;
		 gi     += k4;
		 fi     += k4;
		} while (fi<fn);
	}
     TRIG_RESET(k,c1,s1);
    } while (k4<256);
}
 /*************************************************************************/
/* Does a real-valued fourier transform of "256" points of the
   "real" and "imag" arrays.  The real part of the transform ends
   up in the first half of the array and the imaginary part of the
   transform ends up in the second half of the array.
   ** FFT and FHT routines <=> Copyright 1988, 1993; Ron Mayer. */

 void  Java_android_musicfeel_ActivityGrabadora_realfft (JNIEnv* env,jobject this,jfloat real[257])
{
   float a,b;
   int i,j;
   Java_android_musicfeel_ActivityGrabadora_fht(real);
   for (i=1, j=255; i<128; i++, j--) {
      a = real[i];
      b = real[j];
      real[j] = (a-b)*0.5;
      real[i] = (a+b)*0.5;
   }
   real[256] = 0.0;
}




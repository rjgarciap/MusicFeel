package android.musicfeel;

/**
 * Clase usada para llevar a cabo la transformada FFT de las muestras
 * @author Miriam Martin Gonzalez y Ricardo J. Garcia Pinel
 *
 */
public class ProcesadorMuestras {

	/**
	 * Declaracion del metodo nativo realFFT
	 * @param r, muestras grabadas por el microfono.
	 */
	public native void realfft (float r[]);

	/**
     *  Importamos la libreria formada por el codigo C
     */
    static {
		System.loadLibrary("FFT");
		}
}

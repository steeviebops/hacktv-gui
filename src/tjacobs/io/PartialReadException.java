package tjacobs.io;

public class PartialReadException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PartialReadException(int got, int total) {
		super("Got " + got + " of " + total + " bytes");
	}
}
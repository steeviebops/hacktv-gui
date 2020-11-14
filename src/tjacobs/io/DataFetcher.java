package tjacobs.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import tjacobs.io.TimeOut.TimeOutCmd;

/**
 * InfoFetcher is a generic way to read data from an input stream (file, socket, etc)
 * InfoFetcher can be set up with a thread so that it reads from an input stream 
 * and report to registered listeners as it gets
 * more information. This vastly simplifies the process of always re-writing
 * the same code for reading from an input stream.
 * @author tjacobs01
 */
	public class DataFetcher implements Runnable, TimeOutCmd {
		public byte[] buf;
		public InputStream in;
		public int waitTime;
		private ArrayList<FetcherListener> mFetcherListeners;
		public int got = 0;
		protected boolean mClearBufferFlag = false;

		public DataFetcher(InputStream in, byte[] buf, int waitTime) {
			this.buf = buf;
			this.in = in;
			this.waitTime = waitTime;
		}
		
		public void addFetcherListener(FetcherListener listener) {
			if (mFetcherListeners == null) {
				mFetcherListeners = new ArrayList<FetcherListener>(2);
			}
			if (!mFetcherListeners.contains(listener)) {
				mFetcherListeners.add(listener);
			}
		}
		
		public void removeFetcherListener(FetcherListener fll) {
			if (mFetcherListeners == null) {
				return;
			}
			mFetcherListeners.remove(fll);
		}
		
		public byte[] readCompletely() {
			run();
			return buf;
		}
		
		public int got() {
			return got;
		}
		
		/** Override this to implement other implementations
		 * 
		 */
		public void timeOut() {
			try {
				if (in != null)
				in.close();
			}
			catch (IOException iox) {
				iox.printStackTrace();
			}
		}
		
		public void run() {
			TimeOut to = null;
			if (waitTime > 0) {
				to = new TimeOut(this, waitTime);
				Thread t = new Thread(to);
				t.start();
			}			
			int b;
			try {
				if (in == null) {
					signalListeners(true);
					return;
				}
				while ((b = in.read()) != -1) {
					if (to != null) to.tick();
					if (got + 1 > buf.length) {
						buf = IOUtils.expandBuf(buf);
					}
					int start = got;
					buf[got++] = (byte) b;
					int available = in.available();
					//System.out.println("got = " + got + " available = " + available + " buf.length = " + buf.length);
					if (got + available > buf.length) {
						buf = IOUtils.expandBuf(buf, Math.max(got + available, buf.length * 2));
					}
					got += in.read(buf, got, available);
					signalListeners(false, start);
					if (mClearBufferFlag) {
						mClearBufferFlag = false;
						got = 0;
					}
				}
			} catch (IOException iox) {
				iox.printStackTrace();
				throw new PartialReadException(got, buf.length);
			} finally {
				if (to != null) to.stop();
				buf = IOUtils.trimBuf(buf, got);
				signalListeners(true);
			}
		}
				
		private void setClearBufferFlag(boolean status) {
			mClearBufferFlag = status;
		}
		
		public void clearBuffer() {
			setClearBufferFlag(true);
		}
		
		private void signalListeners(boolean over) {
			signalListeners (over, 0);
		}
		
		private void signalListeners(boolean over, int start) {
			if (mFetcherListeners != null) {
				Iterator<FetcherListener> i = mFetcherListeners.iterator();
				while (i.hasNext()) {
					FetcherListener fll = i.next();
					if (over) {
						fll.fetchedAll(buf);
					} else {
						fll.fetchedMore(buf, start, got);
					}
				}				
			}
		}
		
		public static interface FetcherListener {
			public void fetchedMore(byte[] buf, int start, int end);
			public void fetchedAll(byte[] buf);
		}
		
		public static class ToPrintStream implements FetcherListener {
			PrintStream stream;
			public ToPrintStream(PrintStream ps) {
				stream = ps;
			}
			
			@Override
			public void fetchedAll(byte[] buf) {
			}

			@Override
			public void fetchedMore(byte[] buf, int start, int end) {
				stream.print(new String(buf, start, end - start));
			}			
		}

		public static class ToStandardOut extends ToPrintStream {
			
			public ToStandardOut() {
				super(System.out);
			}
		}		
	}

package tjacobs.io;

public class TimeOut implements Runnable {
	private long mWaitTime;
	private boolean mRunning = true;
	private Thread mMyThread;
	private TimeOutCmd mTimeOutCmd;
	
	public static final int DEFAULT_URL_WAIT_TIME = 30 * 1000; // 30 Seconds
	public static final int NO_TIMEOUT = -1;
	public static final int DEFAULT_WAIT_TIME = NO_TIMEOUT;
	
	public static interface TimeOutCmd {
		public void timeOut();
	}
	
	public TimeOut(TimeOutCmd cmd) {
		this(cmd, DEFAULT_WAIT_TIME);
	}
	public TimeOut(TimeOutCmd cmd, int timeToWait) {
		mWaitTime = timeToWait;
		mTimeOutCmd = cmd;
	}
	
	public void stop() {
		mRunning = false;
		mTimeOutCmd.timeOut();
		if (mMyThread != null) mMyThread.interrupt();
	}
	
	/**
	 * reset the TimeOut
	 *
	 */
	public void tick() {
		if (mMyThread != null)
			mMyThread.interrupt();
	}
	
	public void run () {
		mMyThread = Thread.currentThread();
		while (true) {
			try {
				Thread.sleep(mWaitTime);
				stop();
			}
			catch (InterruptedException ex) {
				if (!mRunning) {
					return;
				}
			}
		}
	}
}
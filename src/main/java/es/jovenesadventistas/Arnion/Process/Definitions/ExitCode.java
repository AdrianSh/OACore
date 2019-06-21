package es.jovenesadventistas.Arnion.Process.Definitions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExitCode extends Exception {
	private static final long serialVersionUID = 4555964545042008449L;
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ExitCode.class);
	private Exception e;
	private int exitCode;

	public enum ExitCodes {
		NOTEXECUTED(-10000000), InterruptedException(-1000001), IOException(-1000002), ExecutionException(-1000003),
		Exception(-1000004);
		private int code;

		private ExitCodes(int code) {
			this.code = code;
		}
	}

	public ExitCode(Exception e) {
		this.e = e;
		this.exitCode = ExitCodes.Exception.code;
	}

	public ExitCode(Exception e, ExitCodes code) {
		this.e = e;
		this.exitCode = code.code;
	}

	public ExitCode(int exitCode) {
		this.exitCode = exitCode;
	}

	public ExitCode(String message) {
		super(message);
	}

	public ExitCode(Throwable cause) {
		super(cause);
	}

	public ExitCode(String message, Throwable cause) {
		super(message, cause);
	}

	public ExitCode(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	public int getExitCode() {
		return this.exitCode;
	}
	
	public Exception getException() {
		return this.e;
	}

	@Override
	public String toString() {
		return "ExitCode [e=" + e + ", exitCode=" + exitCode + "]";
	}
}
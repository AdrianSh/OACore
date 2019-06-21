package es.jovenesadventistas.oacore.process;

public enum ExitCodes {
	NOTEXECUTED(-1000000), InterruptedException(-1000001), IOException(-1000002), ExecutionException(-1000003);

	private int code;

	private ExitCodes(int exitCode) {
		this.setCode(exitCode);
	}

	public int getCode() {
		return code;
	}

	private void setCode(int code) {
		this.code = code;
	}

	public static int getMinExitCode() {
		return -1000000;
	}
}

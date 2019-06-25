package es.ucm.oacore;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;

import es.jovenesadventistas.Arnion.Process.AProcess;
import es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecutor;
import es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecution.ProcessExecutionDetails;

public class RunProcess {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(RunProcess.class);
	
	
	public static void main(String[] args) {

		try {
			ProcessExecutor pExecutor = ProcessExecutor.getInstance();
			ExecutorService executorService = Executors.newSingleThreadExecutor();

			AProcess p1 = new AProcess("java", "-version");
			AProcess p2 = new AProcess("java", "-version");

			p1.setInheritIO(true);
			p2.setInheritIO(true);

			ProcessExecutionDetails pExec1 = new ProcessExecutionDetails(p1);
			ProcessExecutionDetails pExec2 = new ProcessExecutionDetails(p2);

			logger.debug("Executing somes...");
			
			pExecutor.execute(executorService, pExec1);
			pExecutor.execute(executorService, pExec2);

			executorService.shutdown();

			try {
				printStreams(pExec1);
				printStreams(pExec2);
			} catch (IOException | InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

			logger.debug("ExitCode1 {} ExitCode2 {}", pExec1.getExitCode().get(), pExec2.getExitCode().get());
			
		} catch (IOException | InterruptedException | ExecutionException e) {
			logger.error("Error when running a process...", e);
		}
	}

	public static void printStreams(ProcessExecutionDetails p)
			throws IOException, InterruptedException, ExecutionException {
		Process proc = p.getSystemProcess().get();

		if (proc != null) {
			InputStream inpStream = proc.getInputStream();
			InputStream errStream = proc.getErrorStream();
			if (inpStream != null)
				readStream(inpStream, "INPUT");
			if (errStream != null)
				readStream(errStream, "ERROR");
		}
	}

	public static void readStream(InputStream is, String desc) throws IOException {
		ByteSource byteSource = new ByteSource() {
			@Override
			public InputStream openStream() throws IOException {
				return is;
			}
		};

		String text = byteSource.asCharSource(Charsets.UTF_8).read();
		System.out.println("[" + desc + "]" + text);
	}

}

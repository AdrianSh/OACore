package es.ucm.oacore;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;

import es.jovenesadventistas.oacore.ProcessExecutor.AsynchronousProcessExecutor;
import es.jovenesadventistas.oacore.ProcessExecutor.ProcessExecutor;
import es.jovenesadventistas.oacore.ProcessExecutor.SynchronousProcessExecutor;
import es.jovenesadventistas.oacore.ProcessExecutor.ProcessExecution.AsynchProcessExecution;
import es.jovenesadventistas.oacore.ProcessExecutor.ProcessExecution.BasicProcessExecution;
import es.jovenesadventistas.oacore.process.MProcess;
import es.jovenesadventistas.oacore.process.SynchProcess;

public class RunAsynchProcess {

	public static void main(String[] args) {
		try {
			ProcessExecutor pExecutor = AsynchronousProcessExecutor.getInstance();
			ExecutorService executorService = Executors.newSingleThreadExecutor();

			MProcess p1 = new SynchProcess("java", "-version");
			MProcess p2 = new SynchProcess("java", "-version");

			p1.setInheritIO(true);
			p2.setInheritIO(true);

			
			AsynchProcessExecution pExec1 = new AsynchProcessExecution(p1);
			AsynchProcessExecution pExec2 = new AsynchProcessExecution(p2);

			pExecutor.execute(executorService, pExec1);
			pExecutor.execute(executorService, pExec2);

			executorService.shutdown();

			try {
				printStreams(pExec1);
				printStreams(pExec2);
			} catch (IOException | InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

			System.out.println("ExitCode1: " + pExec1.getExitCode() + " ExitCode2: " + pExec2.getExitCode());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printStreams(AsynchProcessExecution p) throws IOException, InterruptedException, ExecutionException {
		Process proc = p.getProcess().get();

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

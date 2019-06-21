package es.ucm.oacore;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;

import es.jovenesadventistas.oacore.ProcessExecutor.ProcessExecutor;
import es.jovenesadventistas.oacore.ProcessExecutor.SynchronousProcessExecutor;
import es.jovenesadventistas.oacore.ProcessExecutor.ProcessExecution.BasicProcessExecution;
import es.jovenesadventistas.oacore.process.MProcess;
import es.jovenesadventistas.oacore.process.SynchProcess;

public class RunASimpleProcess {

	public static void main(String[] args) {
		try {
			ProcessExecutor pExecutor = SynchronousProcessExecutor.getInstance();
			ExecutorService executorService = Executors.newSingleThreadExecutor();

			MProcess p1 = new SynchProcess("java", "-version");
			MProcess p2 = new SynchProcess("java", "-version");

			p1.setInheritIO(true);
			p2.setInheritIO(true);

			BasicProcessExecution pExec1 = new BasicProcessExecution(p1);
			BasicProcessExecution pExec2 = new BasicProcessExecution(p2);

			pExecutor.execute(executorService, pExec1);
			pExecutor.execute(executorService, pExec2);

			executorService.shutdown();

			while (!executorService.isTerminated()) {
			}
			
			try {
				printStreams(pExec1);
				printStreams(pExec2);
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("ExitCode1: " + pExec1.getExitCode() + " ExitCode2: " + pExec2.getExitCode());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printStreams(BasicProcessExecution p) throws IOException {
		Process proc = p.getProcess();

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

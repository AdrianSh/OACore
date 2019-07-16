package es.ucm.oacore;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;

import es.jovenesadventistas.Arnion.Process.AProcess;
import es.jovenesadventistas.Arnion.Process.Binders.ExitCodeBinder;
import es.jovenesadventistas.Arnion.Process.Binders.Publishers.ConcurrentLinkedQueuePublisher;
import es.jovenesadventistas.Arnion.Process.Binders.Subscribers.ConcurrentLinkedQueueSubscriber;
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
			
			// Binder section			
			ExitCodeBinder b1 = new ExitCodeBinder(pExec1, new ConcurrentLinkedQueueSubscriber<>(), new ConcurrentLinkedQueuePublisher<>());
			ExitCodeBinder b2 = new ExitCodeBinder(pExec2, new ConcurrentLinkedQueueSubscriber<>(), new ConcurrentLinkedQueuePublisher<>());
			
			// Join the output of the process 1 to the input of the process 2
			b1.markAsReady();
			b1.subscribe(b2);
			
			pExec1.setBinder(b1);
			pExec2.setBinder(b2);
			
			logger.debug("Executing somes...");
			
			pExecutor.execute(executorService, pExec1);
			pExecutor.execute(executorService, b1);
			pExecutor.execute(executorService, pExec2);
			pExecutor.execute(executorService, b2);
			
			executorService.shutdown();

			printStreams(pExec1);
			printStreams(pExec2);
			
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

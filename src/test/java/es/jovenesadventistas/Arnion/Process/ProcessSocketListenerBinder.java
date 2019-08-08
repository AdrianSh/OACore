package es.jovenesadventistas.Arnion.Process;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.jovenesadventistas.Arnion.Process.Binders.RunnableBinder;
import es.jovenesadventistas.Arnion.Process.Binders.StdOutBinder;
import es.jovenesadventistas.Arnion.Process.Binders.Publishers.SocketListenerPublisher;
import es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecutor;
import es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecution.ProcessExecutionDetails;

public class ProcessSocketListenerBinder {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	//public ProcessSocketListenerBinder() {
	public static void main(String[] args) {
		try {
			ProcessExecutor pExecutor = ProcessExecutor.getInstance();
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			ExecutorService executorService2 = Executors.newSingleThreadExecutor();

			AProcess p2 = new AProcess("C:\\\\Program Files\\\\nodejs\\\\node.exe", "index.js", "write", "outputBindSocket.txt");
			p2.setWorkingDirectory(new File("C:\\Privado\\TFG\\Arnion-Processes\\File\\"));

			ProcessExecutionDetails pExec2 = new ProcessExecutionDetails(p2);
			SocketListenerPublisher sPub = null;

			try {
				sPub = new SocketListenerPublisher(new Socket("localhost", 23));
			} catch (IOException e) {
				System.err.println("Couldn't get the InputStream for the socket.");
				e.printStackTrace();
			}

			RunnableBinder b1 = new RunnableBinder(sPub);
			StdOutBinder b2 = new StdOutBinder(pExec2);

			sPub.subscribe(b2);
			b1.markAsReady();

			pExec2.setBinder(b2);
			
			b2.onFinish( (Void) -> {
				System.out.println("StdOut writer finished...");
				return Void;
			});
			
			b1.onFinish( (Void) -> {
				System.out.println("Socket finished...");
				executorService.shutdown();
				executorService2.shutdown();
				return Void;
			});

			logger.debug("Executing somes...");

			pExecutor.execute(executorService, b1);
			pExecutor.execute(executorService2, pExec2);
			pExecutor.execute(executorService2, b2);
		} catch (IOException e) {
			logger.error("Error when running a process...", e);
		}
	}
}

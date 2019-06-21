package es.jovenesadventistas.oacore.ProcessExecutor;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import es.jovenesadventistas.oacore.ProcessExecutor.ProcessExecution.BasicProcessExecution;
import es.jovenesadventistas.oacore.ProcessExecutor.ProcessExecution.ProcessExecution;
import es.jovenesadventistas.oacore.process.ExitCodes;

/**
 * A Synchronous execution of a process means to execute a process and WAIT FOR
 * IT until it finalizes.
 * 
 * @author Adrian E. Sanchez Hurtado
 *
 */
public class SynchronousProcessExecutor implements ProcessExecutor {
	private static SynchronousProcessExecutor instance;
	private AtomicBoolean running;

	private SynchronousProcessExecutor() {
		this.running = new AtomicBoolean(false);
	}

	public static SynchronousProcessExecutor getInstance() {
		if (instance == null)
			instance = new SynchronousProcessExecutor();
		return instance;
	}

	@Override
	public void execute(ExecutorService executorService, ProcessExecution p) throws IOException {
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				int exit = -1;
				try {
					System.out.println("Executing the process...");
					running.set(true);
					Process proc = p.getProcessDefinition().execute();
					((BasicProcessExecution)p).setProcess(proc);
					exit = proc.waitFor();
				} catch (IOException e) {
					exit = ExitCodes.IOException.getCode();
					e.printStackTrace();
				} catch (InterruptedException e) {
					exit = ExitCodes.InterruptedException.getCode();
					e.printStackTrace();
				} finally {
					running.set(false);
					((BasicProcessExecution)p).getExitCode().set(exit);
					((BasicProcessExecution)p).getExecuted().set(true);
				}
			}
		});

		/*
		 * 
		 * 
		 * Executor executor = MoreExecutors.directExecutor();
		 * 
		 * AtomicBoolean executed = new AtomicBoolean();
		 * 
		 * executor.execute(() -> { try { Thread.sleep(500); } catch
		 * (InterruptedException e) { e.printStackTrace(); } executed.set(true); });
		 * 
		 * assertTrue(executed.get());
		 * 
		 * 
		 * EXITING
		 * 
		 * ThreadPoolExecutor executor = (ThreadPoolExecutor)
		 * Executors.newFixedThreadPool(5); ExecutorService executorService =
		 * MoreExecutors.getExitingExecutorService(executor, 100,
		 * TimeUnit.MILLISECONDS);
		 * 
		 * executorService.submit(() -> { while (true) { } });
		 * 
		 * 
		 * SEQUENCE OF TASKS
		 * 
		 * ExecutorService executorService = Executors.newCachedThreadPool();
		 * ListeningExecutorService listeningExecutorService =
		 * MoreExecutors.listeningDecorator(executorService);
		 * 
		 * ListenableFuture<String> future1 = listeningExecutorService.submit(() ->
		 * "Hello"); ListenableFuture<String> future2 =
		 * listeningExecutorService.submit(() -> "World");
		 * 
		 * String greeting = Futures.allAsList(future1,
		 * future2).get().stream().collect(Collectors.joining(" "));
		 * assertEquals("Hello World", greeting);
		 * 
		 */
	}
}

package es.jovenesadventistas.oacore.ProcessExecutor;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import es.jovenesadventistas.oacore.ProcessExecutor.ProcessExecution.ProcessExecution;

public interface ProcessExecutor {
	public void execute(ExecutorService executorService, ProcessExecution p) throws IOException;
}

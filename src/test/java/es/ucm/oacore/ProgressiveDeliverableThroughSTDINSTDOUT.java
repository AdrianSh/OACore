package es.ucm.oacore;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import es.jovenesadventistas.Arnion.Process.AProcess;
import es.jovenesadventistas.Arnion.Process.Binders.StdInBinder;
import es.jovenesadventistas.Arnion.Process.Binders.Publishers.ConcurrentLinkedQueuePublisher;
import es.jovenesadventistas.Arnion.Process.Binders.Subscribers.ConcurrentLinkedQueueSubscriber;
import es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecutor;
import es.jovenesadventistas.Arnion.ProcessExecutor.ProcessExecution.ProcessExecutionDetails;

public class ProgressiveDeliverableThroughSTDINSTDOUT {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	public static void main(String[] args) {

		try {
			ProcessExecutor pExecutor = ProcessExecutor.getInstance();
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			ExecutorService executorService2 = Executors.newSingleThreadExecutor();

			
			/*
			 * ***************************************************************************
			 * 
			 * Progressive Deliverable through stdin stdout
			 * 
			 * ***************************************************************************
			 * La ejecución de un proceso que lea de un fichero y lo escriba por consola
			 * linea a linea progresivamente.
			 * 
			 * El binder de este proceso estaría ready y cada linea leida la sacaria por la
			 * salida estandar.
			 * 
			 * 
			 * El siguiente proceso recibiria estas lineas por su entrada estandar y las
			 * escribiria en un fichero.
			 * 
			 * El binder de este tendría que esperar a estar ready (que estaría tan pronto
			 * comience a recibir mensajes) [IDEA: initial count of messages for being
			 * ready]
			 * 
			 * 
			 * *****************************************************************************
			 * 
			 * Para saber cuando comienza... termina... el flujo de mensajes por la salida /
			 * entrada estandar, debo poder manejarlos como FLUJOS en los que pueda
			 * comprobar si siguen abierto... en el caso de que se termine terminar... sino
			 * continuar... y asi...
			 * 
			 * 
			 * El primer proceso seguira ejecutandose en paralelo mientras va generando
			 * mensajes que pasara al otro...
			 * 
			 * El segundo proceso que leera de la entrada estandar debe saber como
			 * terminar... (o matar el proceso) ¿Cuándo? Controlando como FLUJO para
			 * comprobar de que ya se ha terminado de escribir en el anterior... En este
			 * caso se puede comprobar directamente en el binder si no se puede en el flujo
			 * directamente; ya que el productor del binder terminara (Complete)
			 * 
			 * 
			 */

			AProcess p1 = new AProcess("C:\\Program Files\\nodejs\\node.exe", "index.js", "read", "input0.txt");
			p1.setWorkingDirectory(new File("C:\\Privado\\TFG\\Arnion-Processes\\File\\"));
			AProcess p2 = new AProcess("C:\\\\Program Files\\\\nodejs\\\\node.exe", "index.js", "write", "output1.txt");
			p2.setWorkingDirectory(new File("C:\\Privado\\TFG\\Arnion-Processes\\File\\"));


			ProcessExecutionDetails pExec1 = new ProcessExecutionDetails(p1);
			ProcessExecutionDetails pExec2 = new ProcessExecutionDetails(p2);
			
			// Binder section
			StdInBinder b1 = new StdInBinder(pExec1, new ConcurrentLinkedQueueSubscriber<>(),
					new ConcurrentLinkedQueuePublisher<>());
			StdInBinder b2 = new StdInBinder(pExec2, new ConcurrentLinkedQueueSubscriber<>(),
					new ConcurrentLinkedQueuePublisher<>());

			// Join the output of the process 1 to the input of the process 2
			b1.markAsReady();
			b1.subscribe(b2);

			pExec1.setBinder(b1);
			pExec2.setBinder(b2);

			logger.debug("Executing somes...");
			
			pExecutor.execute(executorService, pExec1);
			pExecutor.execute(executorService, b1);
			pExecutor.execute(executorService2, pExec2);
			pExecutor.execute(executorService2, b2);
			

			executorService.shutdown();
			executorService2.shutdown();
			
			// printStreams(pExec1);
			// printStreams(pExec2);

		} catch (IOException e) { 
			logger.error("Error when running a process...", e);
		}
	}
}

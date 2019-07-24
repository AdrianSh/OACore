package es.jovenesadventistas.Arnion.Process.Binders.Publishers;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.Flow.Subscriber;

import es.jovenesadventistas.Arnion.Process.Binders.Transfers.SocketTransfer;

public class SocketServerPublisher<T extends SocketTransfer> extends SubmissionPublisher<T> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	private ConcurrentHashMap<Subscriber<? super T>, Socket> subscribers;
	private ServerSocket ss;

	public SocketServerPublisher(ServerSocket ss) {
		this.subscribers = new ConcurrentHashMap<Subscriber<? super T>, Socket>();
		this.ss = ss;
	}

	public void subscribe(Subscriber<? super T> subscriber) {
		try {
			this.subscribers.put(subscriber, this.ss.accept());
		} catch (IOException e) {
			logger.error("An exception occurs when subscribing and waiting for the socket.", e);
		}
	}

	public int submit(T data) {
		this.subscribers.forEach((s, socket) -> {
			try {
				OutputStreamWriter os = new OutputStreamWriter(socket.getOutputStream());
				os.write(data.toString());
				os.flush();
			} catch (IOException e) {
				logger.error("An exception occurs when writing the item on the socket.", e);
			}

			s.onNext(data);
		});
		return 0;
	}

	public void close() {

		this.subscribers.forEach((c, socket) -> {
			c.onComplete();
			try {
				socket.shutdownOutput();
			} catch (IOException e) {
				logger.error("An exception occurs when shuting down socket's output.", e);
			}
		});

		try {
			this.ss.close();
		} catch (IOException e) {
			logger.error("An exception occurs when closing the server socket.", e);
		}
		this.subscribers.clear();
	}

}

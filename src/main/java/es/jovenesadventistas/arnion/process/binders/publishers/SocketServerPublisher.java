package es.jovenesadventistas.arnion.process.binders.publishers;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import es.jovenesadventistas.arnion.process.binders.subscribers.ASubscriber;
import es.jovenesadventistas.arnion.process.binders.transfers.SocketTransfer;

public class SocketServerPublisher<T extends SocketTransfer> extends ASubmissionPublisher<T> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	private ConcurrentHashMap<ASubscriber<? super T>, Socket> subscribers;
	private ServerSocket ss;
	@Id
	private ObjectId id = new ObjectId();

	public SocketServerPublisher(ServerSocket ss) {
		this.subscribers = new ConcurrentHashMap<ASubscriber<? super T>, Socket>();
		this.ss = ss;
	}

	public void subscribe(ASubscriber<? super T> subscriber) {
		try {
			subscriber.onSubscribe(this);
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
		return 1;
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

	@Override
	public void request(long n) {
		logger.debug("{} requested.", n);
	}

	@Override
	public void cancel() {
		logger.debug("Cancel request.");
	}

	public ConcurrentHashMap<ASubscriber<? super T>, Socket> getSubscribersMap() {
		return subscribers;
	}

	public void setSubscribers(ConcurrentHashMap<ASubscriber<? super T>, Socket> subscribers) {
		this.subscribers = subscribers;
	}

	public ServerSocket getSs() {
		return ss;
	}

	public void setSs(ServerSocket ss) {
		this.ss = ss;
	}

	@Override
	public ObjectId getId() {
		return this.id;
	}
	
	@Override
	public void setId(ObjectId id) {
		if (id != null)
			this.id = id;		
	}
}

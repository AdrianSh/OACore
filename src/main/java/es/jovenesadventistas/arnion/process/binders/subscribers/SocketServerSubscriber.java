package es.jovenesadventistas.arnion.process.binders.subscribers;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Flow.Subscription;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import es.jovenesadventistas.arnion.process.binders.publishers.APublisher;
import es.jovenesadventistas.arnion.process.binders.transfers.Transfer;

public class SocketServerSubscriber<T extends Transfer> implements ASubscriber<T> {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	@Id
	private ObjectId id = new ObjectId();
	private APublisher subscription;
	private ServerSocket ss;
	private Socket s;	
	private ConcurrentLinkedQueue<T> data;
	private Long numRequest, numReceived;
	private boolean subscribed;
	

	public SocketServerSubscriber(ServerSocket ss) {
		this.data = new ConcurrentLinkedQueue<T>();
		this.ss = ss;
		this.subscribed = false;
		this.numReceived = 0L;
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		if(subscription instanceof APublisher) {
			this.subscription = (APublisher) subscription;
			this.subscribed = true;
			// this.subscription.request(1);
		} else {
			logger.error("Cannot onSubscribe using an unknown subscription. It should implements APublisher.");
		}
	}

	@Override
	public void onNext(T item) {
		try {
			logger.info("Got {}", item);
			
			this.numReceived = this.numReceived == null ? 1L : ++this.numReceived;
			
			if(s != null) {
				OutputStreamWriter os = new OutputStreamWriter(s.getOutputStream());
				os.write(item.toString());
				os.flush();
			} else{
				this.data.add(item);
			}
			
			if(this.numRequest == null || this.numRequest <= this.numReceived)
				s.shutdownOutput();
			
		} catch (IOException e) {
			logger.error("An exception occurs when writing the item on the socket.", e);
		}
		// subscription.request(1);
	}

	public void request(Long n) throws IOException {
		this.s = this.ss.accept();
		this.numRequest = n;
		this.subscription.request(n);
	}

	public void requestOne() throws IOException {
		this.request(1L);
	}

	public T getData() {
		return this.data.poll();
	}
	
	public ConcurrentLinkedQueue<T> getAllData() {
		return this.data;
	}
	
	public boolean isSubscribed() {
		return this.subscribed;
	}

	@Override
	public void onError(Throwable throwable) {
		logger.error("An error ocurred in the exit-code subscriber", throwable);
		this.subscribed = false;
	}

	@Override
	public void onComplete() {
		// this.data.clear();
		this.subscribed = false;
		
		try {
			this.ss.close();
		} catch (IOException e) {
			logger.error("An exception occurs when closing the server socket.", e);
		}
		
		logger.debug("No more data from the subscription: {}", this.subscription);
	}

	public APublisher getSubscription() {
		return subscription;
	}

	public void setSubscription(APublisher subscription) {
		this.subscription = subscription;
	}

	public ServerSocket getSs() {
		return ss;
	}

	public void setSs(ServerSocket ss) {
		this.ss = ss;
	}

	public Socket getS() {
		return s;
	}

	public void setS(Socket s) {
		this.s = s;
	}

	public Long getNumRequest() {
		return numRequest;
	}

	public void setNumRequest(Long numRequest) {
		this.numRequest = numRequest;
	}

	public Long getNumReceived() {
		return numReceived;
	}

	public void setNumReceived(Long numReceived) {
		this.numReceived = numReceived;
	}

	public void setData(ConcurrentLinkedQueue<T> data) {
		this.data = data;
	}

	public void setSubscribed(boolean subscribed) {
		this.subscribed = subscribed;
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

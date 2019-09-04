package es.jovenesadventistas.oacore.repository.converters;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import es.jovenesadventistas.arnion.process.binders.Binder;
import es.jovenesadventistas.arnion.process.binders.subscribers.ASubscriber;
import es.jovenesadventistas.arnion.process.binders.subscribers.ConcurrentLinkedQueueSubscriber;
import es.jovenesadventistas.arnion.process.binders.subscribers.SocketServerSubscriber;
import es.jovenesadventistas.arnion.process.binders.subscribers.SocketSubscriber;
import es.jovenesadventistas.arnion.process.binders.subscribers.TransferStoreSubscriber;
import es.jovenesadventistas.arnion.process.binders.transfers.Transfer;

public class ASubscriberConverter {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	@WritingConverter
	public static class ASubscriberWriteConverter implements Converter<ASubscriber<?>, Document> {
		public Document convert(ASubscriber<?> source) {
			Document document = new Document();
			document.put("_id", source.getId());
			String subscriberType = source.getClass().getName();
			document.put("subscriberType", subscriberType);
			switch (subscriberType) {
			case "es.jovenesadventistas.arnion.process.binders.Subscribers.ConcurrentLinkedQueueSubscriber":
				// Subscriptions are realized when running.
				@SuppressWarnings("unchecked")
				ConcurrentLinkedQueueSubscriber<Transfer> concurrentLinkedQueueSubscriber = (ConcurrentLinkedQueueSubscriber<Transfer>) source;
				document.put("data", concurrentLinkedQueueSubscriber.getAllData());
				break;
			case "es.jovenesadventistas.arnion.process.binders.Subscribers.SocketServerSubscriber":
				@SuppressWarnings("unchecked")
				SocketServerSubscriber<Transfer> socketServerSubscriber = (SocketServerSubscriber<Transfer>) source;
				document.put("data", socketServerSubscriber.getAllData());
				document.put("socket_port", socketServerSubscriber.getSs().getLocalPort());
				break;
			case "es.jovenesadventistas.arnion.process.binders.Subscribers.SocketSubscriber":
				@SuppressWarnings("unchecked")
				SocketSubscriber<Transfer> socketSubscriber = (SocketSubscriber<Transfer>) source;
				document.put("data", socketSubscriber.getAllData());
				document.put("socket_port", socketSubscriber.getS().getPort());
				document.put("socket_host", socketSubscriber.getS().getInetAddress().getHostAddress());
				break;
			case "es.jovenesadventistas.arnion.process.binders.Subscribers.TransferStoreSubscriber":
				break;
			case "es.jovenesadventistas.arnion.process.binders.ExitCodeBinder":
				return new BinderConverter.BinderWriteConverter().convert((Binder) source);
			default:
				throw new IllegalArgumentException("Unexpected value for subscriberType: " + subscriberType);
			}
			return document;
		}
	}

	@ReadingConverter
	public static class ASubscriberReadConverter implements Converter<Document, ASubscriber<?>> {
		@SuppressWarnings("unchecked")
		public ASubscriber<?> convert(Document source) {
			ASubscriber<?> r = null;
			String subscriberType = source.getString("subscriberType");
			switch (subscriberType) {
			case "es.jovenesadventistas.arnion.process.binders.Subscribers.ConcurrentLinkedQueueSubscriber":
				ConcurrentLinkedQueueSubscriber<Transfer> concurrentLinkedQueueSubscriber = new ConcurrentLinkedQueueSubscriber<Transfer>();
				concurrentLinkedQueueSubscriber
						.setData((ConcurrentLinkedQueue<Transfer>) source.get("data", ConcurrentLinkedQueue.class));
				r = concurrentLinkedQueueSubscriber;
				break;
			case "es.jovenesadventistas.arnion.process.binders.Subscribers.SocketServerSubscriber":
				Integer ssPort = source.getInteger("socket_port");
				try {
					SocketServerSubscriber<Transfer> socketServerSubscriber = new SocketServerSubscriber<Transfer>(
							new ServerSocket(ssPort));
					socketServerSubscriber
							.setData((ConcurrentLinkedQueue<Transfer>) source.get("data", ConcurrentLinkedQueue.class));
					r = socketServerSubscriber;
				} catch (IOException e) {
					logger.error("Could not open server socket with port: " + ssPort, e);
				}
				break;
			case "es.jovenesadventistas.arnion.process.binders.Subscribers.SocketSubscriber":
				Integer sPort = source.getInteger("socket_port");
				String sInetAddress = source.getString("socket_host");
				try {
					SocketSubscriber<Transfer> socketSubscriber = new SocketSubscriber<Transfer>(
							new Socket(sInetAddress, sPort));
					socketSubscriber.setData(new ConcurrentLinkedQueue<Transfer>(source.get("data", ArrayList.class)));
					r = socketSubscriber;
				} catch (IOException e) {
					logger.error("Could not open socket with port: " + sPort + " and host: " + sInetAddress, e);
				}
				break;
			case "es.jovenesadventistas.arnion.process.binders.Subscribers.TransferStoreSubscriber":
				TransferStoreSubscriber<Transfer> transferStoreSubscriber = new TransferStoreSubscriber<Transfer>();
				r = transferStoreSubscriber;
				break;
			case "es.jovenesadventistas.arnion.process.binders.ExitCodeBinder":
				r = (ASubscriber<?>) new BinderConverter.BinderReadConverter().convert(source);
			default:
				throw new IllegalArgumentException("Unexpected value for subscriberType: " + subscriberType);
			}
			ObjectId id = source.getObjectId("_id");
			if (id != null && r != null)
				r.setId(id);
			return r;
		}
	}
}

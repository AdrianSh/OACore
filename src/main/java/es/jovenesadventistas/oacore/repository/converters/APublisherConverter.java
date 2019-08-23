package es.jovenesadventistas.oacore.repository.converters;

import java.io.IOException;
import java.lang.reflect.TypeVariable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import es.jovenesadventistas.arnion.process.binders.Publishers.APublisher;
import es.jovenesadventistas.arnion.process.binders.Publishers.ConcurrentLinkedQueuePublisher;
import es.jovenesadventistas.arnion.process.binders.Publishers.SocketListenerPublisher;
import es.jovenesadventistas.arnion.process.binders.Publishers.SocketServerPublisher;
import es.jovenesadventistas.arnion.process.binders.Transfers.SocketTransfer;
import es.jovenesadventistas.arnion.process.binders.Transfers.Transfer;

public class APublisherConverter {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	
	@WritingConverter
	public static class APublisherWriteConverter implements Converter<APublisher, Document> {		
		public Document convert(APublisher source) {
			Document document = new Document();
			document.put("_id", source.getId());
			String publisherType = source.getClass().getName();
			TypeVariable<?>[] publisherTypeParameters = source.getClass().getTypeParameters();
			document.put("publisherType", publisherType);
			document.put("publisherTypeParameters", publisherTypeParameters);
			switch (publisherType) {
			case "es.jovenesadventistas.arnion.process.binders.Publishers.ConcurrentLinkedQueuePublisher":
				// Subscriptions are made on execution time.
				break;
			case "es.jovenesadventistas.arnion.process.binders.Publishers.SocketListenerPublisher":
				SocketListenerPublisher socketListenerPublisher = (SocketListenerPublisher) source;
				document.put("socket_port", socketListenerPublisher.getSocket().getPort());
				document.put("socket_host", socketListenerPublisher.getSocket().getInetAddress());
				break;
			case "es.jovenesadventistas.arnion.process.binders.Publishers.SocketServerPublisher":
				@SuppressWarnings("unchecked")
				SocketServerPublisher<SocketTransfer> socketServerPublisher = (SocketServerPublisher<SocketTransfer>) source;
				document.put("socket_port", socketServerPublisher.getSs().getLocalPort());
				break;
			default:
				throw new IllegalArgumentException("Unexpected value for publisherType: " + publisherType);
			}
			return document;
		}
	}
	
	@ReadingConverter
	public static class APublisherReadConverter implements Converter<Document, APublisher> {
		public APublisher convert(Document source) {
			APublisher r = null;
			String publisherType = source.getString("publisherType");
			switch (publisherType) {
			case "es.jovenesadventistas.arnion.process.binders.Publishers.ConcurrentLinkedQueuePublisher":
				r = new ConcurrentLinkedQueuePublisher<Transfer>();
				break;
			case "es.jovenesadventistas.arnion.process.binders.Publishers.SocketListenerPublisher":
				SocketListenerPublisher socketListenerPublisher = null;
				try {
					socketListenerPublisher = new SocketListenerPublisher(new Socket(source.get("socket_host", InetAddress.class), source.getInteger("socket_port")));
				} catch (IOException e) {
					logger.error("Could not open the socket for the SocketListenerPublisher.", e);
				}
				r = socketListenerPublisher;
				break;
			case "es.jovenesadventistas.arnion.process.binders.Publishers.SocketServerPublisher":
				SocketServerPublisher<SocketTransfer> socketServerPublisher = null;
				try {
					socketServerPublisher = new SocketServerPublisher<>(new ServerSocket(source.getInteger("socket_port")));
				} catch (IOException e) {
					logger.error("Could not open the socket for the SocketListenerPublisher.", e);
				}
				r = socketServerPublisher;
				break;
			default:
				throw new IllegalArgumentException("Unexpected value for publisherType: " + publisherType);
			}
			r.setId(source.getObjectId("_id"));
			return r;
		}
	}
}

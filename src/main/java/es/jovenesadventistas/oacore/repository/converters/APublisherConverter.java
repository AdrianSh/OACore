package es.jovenesadventistas.oacore.repository.converters;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import es.jovenesadventistas.arnion.process.binders.publishers.APublisher;
import es.jovenesadventistas.arnion.process.binders.publishers.ConcurrentLinkedQueuePublisher;
import es.jovenesadventistas.arnion.process.binders.publishers.SocketListenerPublisher;
import es.jovenesadventistas.arnion.process.binders.publishers.SocketServerPublisher;
import es.jovenesadventistas.arnion.process.binders.transfers.SocketTransfer;
import es.jovenesadventistas.arnion.process.binders.transfers.Transfer;

public class APublisherConverter {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	
	@WritingConverter
	public static class APublisherWriteConverter implements Converter<APublisher, Document> {		
		public Document convert(APublisher source) {
			
			Document document = new Document();
			document.put("_id", source.getId());
			String publisherType = source.getClass().getName();
			document.put("publisherType", publisherType);
			switch (publisherType) {
			case "es.jovenesadventistas.arnion.process.binders.publishers.ConcurrentLinkedQueuePublisher":
				// Subscriptions are made on execution time.
				break;
			case "es.jovenesadventistas.arnion.process.binders.publishers.SocketListenerPublisher":
				SocketListenerPublisher socketListenerPublisher = (SocketListenerPublisher) source;
				document.put("socket_port", socketListenerPublisher.getSocket().getPort());
				document.put("socket_host", socketListenerPublisher.getSocket().getInetAddress().getHostAddress());
				break;
			case "es.jovenesadventistas.arnion.process.binders.publishers.SocketServerPublisher":
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
			case "es.jovenesadventistas.arnion.process.binders.publishers.ConcurrentLinkedQueuePublisher":
				r = new ConcurrentLinkedQueuePublisher<Transfer>();
				break;
			case "es.jovenesadventistas.arnion.process.binders.publishers.SocketListenerPublisher":
				SocketListenerPublisher socketListenerPublisher = null;
				try {
					socketListenerPublisher = new SocketListenerPublisher(new Socket(source.getString("socket_host"), source.getInteger("socket_port")));
				} catch (IOException e) {
					logger.error("Could not open the socket for the SocketListenerPublisher.", e);
				}
				r = socketListenerPublisher;
				break;
			case "es.jovenesadventistas.arnion.process.binders.publishers.SocketServerPublisher":
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
			ObjectId id = source.getObjectId("_id");
			
			if(id != null && r != null)
				r.setId(id);
			return r;
		}
	}
}

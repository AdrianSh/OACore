package es.jovenesadventistas.arnion.process.binders.Subscribers;

import java.util.concurrent.Flow.Subscriber;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public interface ASubscriber<T> extends Subscriber<T> {
	public ObjectId getId();
}

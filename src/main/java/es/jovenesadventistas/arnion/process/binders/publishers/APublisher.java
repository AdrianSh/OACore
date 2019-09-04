package es.jovenesadventistas.arnion.process.binders.publishers;

import java.util.concurrent.Flow.Subscription;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public interface APublisher extends Subscription {
	public ObjectId getId();
	public void setId(ObjectId id);
}

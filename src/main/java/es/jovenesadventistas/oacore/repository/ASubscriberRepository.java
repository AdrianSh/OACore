package es.jovenesadventistas.oacore.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import es.jovenesadventistas.arnion.process.binders.subscribers.ASubscriber;

public interface ASubscriberRepository extends MongoRepository<ASubscriber<?>, String> {
	ASubscriber<?> findById(ObjectId id);
}
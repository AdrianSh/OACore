package es.jovenesadventistas.oacore.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import es.jovenesadventistas.arnion.process.binders.Publishers.APublisher;

public interface APublisherRepository extends MongoRepository<APublisher, String> {
	APublisher findById(ObjectId id);
}
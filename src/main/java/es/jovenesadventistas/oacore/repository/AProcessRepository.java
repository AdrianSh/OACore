package es.jovenesadventistas.oacore.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import es.jovenesadventistas.arnion.process.AProcess;

public interface AProcessRepository extends MongoRepository<AProcess, String> {
	AProcess findByUserId(ObjectId userId);
	AProcess findById(ObjectId id);
}
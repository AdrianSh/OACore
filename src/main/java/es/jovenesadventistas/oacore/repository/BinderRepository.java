package es.jovenesadventistas.oacore.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import es.jovenesadventistas.arnion.process.binders.Binder;
import es.jovenesadventistas.oacore.model.User;

public interface BinderRepository extends MongoRepository<Binder, String> {
	Binder findById(ObjectId id);
}
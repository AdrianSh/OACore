package es.jovenesadventistas.oacore.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import es.jovenesadventistas.oacore.model.User;

public interface UserRepository extends MongoRepository<User, String> {
	User findByUsername(String username);
	User findById(ObjectId id);
	User findByEmail(String email);
}
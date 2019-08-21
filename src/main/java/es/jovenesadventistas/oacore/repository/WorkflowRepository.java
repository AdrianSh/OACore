package es.jovenesadventistas.oacore.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import es.jovenesadventistas.arnion.workflow.Workflow;
import es.jovenesadventistas.oacore.model.User;

public interface WorkflowRepository extends MongoRepository<Workflow, String> {
	Workflow findByUserId(ObjectId userId);
	Workflow findById(ObjectId id);
}
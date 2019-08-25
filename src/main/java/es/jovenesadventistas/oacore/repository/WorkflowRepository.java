package es.jovenesadventistas.oacore.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import es.jovenesadventistas.arnion.workflow.Workflow;

public interface WorkflowRepository extends MongoRepository<Workflow, String> {
	List<Workflow> findByUserId(ObjectId userId);
	Workflow findById(ObjectId id);
}
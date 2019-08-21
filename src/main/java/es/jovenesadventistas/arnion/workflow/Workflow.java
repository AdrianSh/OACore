package es.jovenesadventistas.arnion.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import es.jovenesadventistas.arnion.process.AProcess;
import es.jovenesadventistas.arnion.process.binders.Binder;
import es.jovenesadventistas.arnion.process_executor.ProcessExecution.ProcessExecutionDetails;
import es.jovenesadventistas.oacore.model.User;

public class Workflow {
	@Id
	private ObjectId id = new ObjectId();
	
	protected List<ObjectId> processIds;
	
	/*
	@DBRef
	protected List<ProcessExecutionDetails> processExecutionDetails;
	
	@DBRef
	protected List<ExecutorService> executorServices;
	
	@DBRef
	protected List<Binder> binders;
	*/

	private ObjectId userId;
	
	public Workflow(ObjectId userId) {
		this.processIds = new ArrayList<ObjectId>();
		/*
		this.processExecutionDetails = new ArrayList<ProcessExecutionDetails>(); 
		this.executorServices = new ArrayList<ExecutorService>();
		this.binders = new ArrayList<Binder>();
		*/
		this.setUserId(userId);
	}

	public ObjectId getUserId() {
		return userId;
	}

	public void setUserId(ObjectId userId) {
		this.userId = userId;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public List<ObjectId> getProcessIds() {
		return processIds;
	}

	public void setProcessIds(List<ObjectId> processIds) {
		this.processIds = processIds;
	}
	
	public void addProcessId(ObjectId pId) {
		this.processIds.add(pId);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Workflow) {
			Workflow u2 = (Workflow) o;
			return this.id == u2.id;
		}
		return false;
	}
	
}

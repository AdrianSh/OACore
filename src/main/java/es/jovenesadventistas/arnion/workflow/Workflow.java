package es.jovenesadventistas.arnion.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import es.jovenesadventistas.arnion.process.AProcess;
import es.jovenesadventistas.arnion.process.binders.Binder;
import es.jovenesadventistas.arnion.process_executor.ProcessExecution.ProcessExecutionDetails;

@Document
public class Workflow {
	@Id
	private ObjectId id = new ObjectId();

	protected List<AProcess> process;
	protected List<ProcessExecutionDetails> processExecutionDetails;
	protected List<ExecutorService> executorServices;
	protected List<Binder> binders;

	private ObjectId userId;

	public Workflow(ObjectId userId) {
		this.process = new ArrayList<AProcess>();
		this.processExecutionDetails = new ArrayList<ProcessExecutionDetails>();
		this.executorServices = new ArrayList<ExecutorService>();
		this.binders = new ArrayList<Binder>();
		this.setUserId(userId);
	}

	public Workflow(ObjectId id, ObjectId userId, List<AProcess> process, List<ProcessExecutionDetails> processExecutionDetails,
			List<ExecutorService> executorServices, List<Binder> binders) {
		if(id != null)
			this.id = id;
		this.userId = userId;
		this.process = process;
		this.processExecutionDetails = processExecutionDetails;
		this.executorServices = executorServices;
		this.binders = binders;
	}
	
	public Workflow(ObjectId userId, List<AProcess> process, List<ProcessExecutionDetails> processExecutionDetails,
			List<ExecutorService> executorServices, List<Binder> binders) {
		this.userId = userId;
		this.process = process;
		this.processExecutionDetails = processExecutionDetails;
		this.executorServices = executorServices;
		this.binders = binders;
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
		if (id != null)
			this.id = id;
	}

	public List<AProcess> getProcess() {
		return process;
	}

	public void setProcess(List<AProcess> process) {
		this.process = process;
	}
	
	public void addProcess(AProcess process) {
		if(this.process == null) this.process = new ArrayList<AProcess>();
		this.process.add(process);
	}

	public void setProcessExecutionDetails(List<ProcessExecutionDetails> processExecutionDetails) {
		this.processExecutionDetails = processExecutionDetails;
	}

	public List<ExecutorService> getExecutorServices() {
		return executorServices;
	}

	public void setExecutorServices(List<ExecutorService> executorServices) {
		this.executorServices = executorServices;
	}

	public List<Binder> getBinders() {
		return binders;
	}

	public void setBinders(List<Binder> binders) {
		this.binders = binders;
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

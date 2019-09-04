package es.jovenesadventistas.arnion.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import es.jovenesadventistas.arnion.process.AProcess;
import es.jovenesadventistas.arnion.process.binders.Binder;
import es.jovenesadventistas.arnion.process_executor.process_execution.ProcessExecutionDetails;

@Document
public class Workflow {

	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	
	public static class Coord {
		protected int x, y;
		
		public Coord(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public int x() {
			return this.x;
		}
		
		public int y() {
			return this.y;
		}
	}
	
	public static class Pair<T, S> {
		protected T o1;
		protected S o2;
		
		public Pair(T o1, S o2) {
			this.o1 = o1;
			this.o2 = o2;
		}
		
		public T o1() {
			return this.o1;
		}
		
		public S o2() {
			return this.o2;
		}
	}
	
	@Id
	private ObjectId id = new ObjectId();
	protected List<AProcess> process;
	protected HashMap<ObjectId, ProcessExecutionDetails> processExecutionDetails;
	protected List<ExecutorService> executorServices;
	protected List<Binder> binders;
	protected HashMap<ObjectId, Coord> objCoords;
	protected HashMap<ObjectId, Pair<ObjectId, ObjectId>> binderProcesses;
	protected HashMap<String, String> executorAssigned;
	private HashMap<ObjectId, HashMap<ObjectId, Pair<ObjectId, ObjectId>>> bindersFromAProcessOrig = new HashMap<>();

	private ObjectId userId;
	

	public Workflow(ObjectId userId) {
		this.process = new ArrayList<AProcess>();
		this.objCoords = new HashMap<>();
		this.processExecutionDetails = new HashMap<>();
		this.executorServices = new ArrayList<ExecutorService>();
		this.binders = new ArrayList<Binder>();
		this.binderProcesses = new HashMap<>();
		this.executorAssigned = new HashMap<>();
		this.setUserId(userId);
		this.reestructureBinderProcessesMap();
	}

	public Workflow(ObjectId id, ObjectId userId, List<AProcess> process, HashMap<ObjectId, Coord> objCoords, HashMap<ObjectId, ProcessExecutionDetails> processExecutionDetails,
			List<ExecutorService> executorServices, List<Binder> binders, HashMap<ObjectId, Pair<ObjectId, ObjectId>> binderProcesses, HashMap<String, String> executorAssigned) {
		if(id != null)
			this.id = id;
		this.userId = userId;
		this.process = process;
		this.objCoords = objCoords;
		this.processExecutionDetails = processExecutionDetails;
		this.executorServices = executorServices;
		this.binders = binders;
		this.binderProcesses = binderProcesses;
		this.executorAssigned = executorAssigned;
		this.reestructureBinderProcessesMap();
	}
	
	public Workflow(ObjectId userId, List<AProcess> process, HashMap<ObjectId, Coord> objCoords, HashMap<ObjectId, ProcessExecutionDetails> processExecutionDetails,
			List<ExecutorService> executorServices, List<Binder> binders, HashMap<ObjectId, Pair<ObjectId, ObjectId>> binderProcesses, HashMap<String, String> executorAssigned) {
		this.userId = userId;
		this.process = process;
		this.objCoords = objCoords;
		this.processExecutionDetails = processExecutionDetails;
		this.executorServices = executorServices;
		this.binders = binders;
		this.binderProcesses = binderProcesses;
		this.executorAssigned = executorAssigned;
		this.reestructureBinderProcessesMap();
	}

	private void reestructureBinderProcessesMap() {
		for (ObjectId binId : this.binderProcesses.keySet()) {
			Pair<ObjectId, ObjectId> processOrgDst = this.binderProcesses.get(binId); 
			ObjectId p1 = processOrgDst.o1(), p2 = processOrgDst.o2();
			HashMap<ObjectId, Pair<ObjectId, ObjectId>> p1List = this.bindersFromAProcessOrig.get(p1);
			
			if(p1List == null)
				p1List = new HashMap<>();
			if(p1List.get(p2) == null) {
				p1List.put(p2, new Pair<>(binId, null));
			} else {
				Pair<ObjectId, ObjectId> pr = p1List.get(p2);
				if(pr.o2 == null)
					pr.o2 = binId;
				else
					logger.error("It doesn't look nice... there is only possible 2 binders for the same link between two processes.");
			}
			this.bindersFromAProcessOrig.put(p1, p1List);
		}
	}
	
	public HashMap<ObjectId, Pair<ObjectId, ObjectId>> getBinderAProcessFromAProcess(ObjectId pId){
		return this.bindersFromAProcessOrig.get(pId);
	}
	
	public Integer getExecutorNumAssigned(ObjectId pId) {
		String in = pId != null ? this.executorAssigned.get(pId.toHexString()) : null;
		Integer r = Integer.parseInt(in == null ? "0": in);
		return r != null ? r : 0;
	}
	
	public ProcessExecutionDetails getProcExecDetls(ObjectId pId) {
		return this.processExecutionDetails.get(pId);
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

	public void setProcessExecutionDetails(HashMap<ObjectId, ProcessExecutionDetails> processExecutionDetails) {
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

	public void addBinderAdditionalData(Binder b, AProcess orig, AProcess dest) {
		if(this.binders.contains(b))
			this.binders.add(b);
		this.binderProcesses.put(b.getId(), new Pair<ObjectId, ObjectId>(orig != null ? orig.getId() : null, dest != null ? dest.getId() : null));
	}


	public HashMap<ObjectId, Coord> getObjCoords() {
		return objCoords;
	}

	public void setObjCoords(HashMap<ObjectId, Coord> objCoords) {
		this.objCoords = objCoords;
	}

	public HashMap<ObjectId, Pair<ObjectId, ObjectId>> getBinderProcesses() {
		return binderProcesses;
	}

	public void setBinderProcesses(HashMap<ObjectId, Pair<ObjectId, ObjectId>> binderProcesses) {
		this.binderProcesses = binderProcesses;
	}

	public HashMap<String, String> getExecutorAssigned() {
		return executorAssigned;
	}

	public void setExecutorAssigned(HashMap<String, String> executorAssigned) {
		this.executorAssigned = executorAssigned;
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

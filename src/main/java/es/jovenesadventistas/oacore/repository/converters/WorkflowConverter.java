package es.jovenesadventistas.oacore.repository.converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import com.google.gson.Gson;
import es.jovenesadventistas.arnion.process.AProcess;
import es.jovenesadventistas.arnion.process.binders.Binder;
import es.jovenesadventistas.arnion.process_executor.ProcessExecution.ProcessExecutionDetails;
import es.jovenesadventistas.arnion.workflow.Workflow;
import es.jovenesadventistas.oacore.controller.AdminController;
import es.jovenesadventistas.oacore.repository.AProcessRepository;
import es.jovenesadventistas.oacore.repository.BinderRepository;

public class WorkflowConverter {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	@WritingConverter
	public static class WorkflowWriteConverter implements Converter<Workflow, Document> {
		private Gson gson = new Gson();

		public Document convert(Workflow source) {
			AProcessRepository rep = AdminController.getInstance().getaProcessRepository();
			BinderRepository binRep = AdminController.getInstance().getBinderRepository();
			Document document = new Document();

			document.put("_id", source.getId());
			List<ObjectId> processIds = new ArrayList<ObjectId>();
			for (AProcess p : source.getProcess()) {
				rep.save(p);
				processIds.add(p.getId());
			}
			document.put("process", processIds);
			document.put("userId", source.getUserId());
			List<String> executorServices = new ArrayList<String>();
			for (ExecutorService exService : source.getExecutorServices()) {
				executorServices.add(exService.getClass().getName());
			}
			document.put("executorServices", executorServices);

			List<ObjectId> binders = new ArrayList<>();
			for (Binder binder : source.getBinders()) {
				binRep.save(binder);
				binders.add(binder.getId());
			}
			document.put("binder", binders);
			document.put("objCoords", gson.toJson(source.getObjCoords()));
			document.put("executorAssigned", gson.toJson(source.getExecutorAssigned()));

			HashMap<String, String> binderProcessesPlainOrig = new HashMap<>();
			HashMap<String, String> binderProcessesPlainDest = new HashMap<>();
			HashMap<ObjectId, Workflow.Pair<ObjectId, ObjectId>> binderProcesses = source.getBinderProcesses();

			if (binderProcesses != null) {
				Set<ObjectId> binderIds = binderProcesses.keySet();
				for (ObjectId key : binderIds) {
					Workflow.Pair<ObjectId, ObjectId> coords = binderProcesses.get(key);
					binderProcessesPlainOrig.put(key.toHexString(), coords.o1().toHexString());
					binderProcessesPlainDest.put(key.toHexString(), coords.o2().toHexString());
				}
			}

			document.put("binderProcessesOrig", gson.toJson(binderProcessesPlainOrig));
			document.put("binderProcessesDest", gson.toJson(binderProcessesPlainDest));
			return document;
		}
	}

	@ReadingConverter
	public static class WorkflowReadConverter implements Converter<Document, Workflow> {
		private Gson gson = new Gson();

		@SuppressWarnings("unchecked")
		public Workflow convert(Document source) {
			AProcessRepository rep = AdminController.getInstance().getaProcessRepository();
			BinderRepository binRep = AdminController.getInstance().getBinderRepository();
			List<AProcess> process = new ArrayList<AProcess>();
			HashMap<ObjectId, ProcessExecutionDetails> processExecutionDetails = new HashMap<>();
			List<ObjectId> processIds = source.get("process", List.class);
			// if (processIds != null) {
			for (ObjectId pId : processIds) {
				AProcess proc = rep.findById(pId);
				if (proc != null)
					process.add(proc);
				try {
					processExecutionDetails.put(pId, new ProcessExecutionDetails(proc));
				} catch (Exception e) {
					logger.error("Could not read AProcess from MongoDB with ObjectID: " + pId);
				}
			}
			// }
			List<ExecutorService> executorServices = new ArrayList<ExecutorService>();
			List<String> executorServiceClassNames = source.get("executorServices", List.class);
			if (executorServices != null) {
				for (String executorServiceClassName : executorServiceClassNames) {
					switch (executorServiceClassName) {
					case "java.util.concurrent.Executors$FinalizableDelegatedExecutorService":
					case "java.util.concurrent.Executors.FinalizableDelegatedExecutorService":
						executorServices.add(Executors.newSingleThreadExecutor());
						break;
					default:
						throw new IllegalArgumentException(
								"Unexpected value for Executors: " + executorServiceClassName);
					}
				}
			}

			List<Binder> binders = new ArrayList<>();
			List<ObjectId> binderIds = source.get("binder", List.class);
			if (binderIds != null)
				for (ObjectId bId : binderIds) {
					Binder binder = binRep.findById(bId);
					if (binder != null) {
						binders.add(binder);
					} else {
						logger.error("Could not read Binder from MongoDB with ObjectID: " + bId);
					}
				}

			HashMap<ObjectId, Workflow.Coord> objCoords = gson.fromJson(source.getString("objCoords"), HashMap.class);
			HashMap<ObjectId, Integer> executorAssigned = gson.fromJson(source.getString("executorAssigned"), HashMap.class);
			HashMap<String, String> binderProcessesPlainOrig = gson.fromJson(source.getString("binderProcessesOrig"),
					HashMap.class);
			HashMap<String, String> binderProcessesPlainDest = gson.fromJson(source.getString("binderProcessesDest"),
					HashMap.class);

			HashMap<ObjectId, Workflow.Pair<ObjectId, ObjectId>> binderProcesses = new HashMap<ObjectId, Workflow.Pair<ObjectId, ObjectId>>();

			if (binderProcessesPlainOrig != null) {
				Set<String> bIds = binderProcessesPlainOrig.keySet();
				for (String key : bIds) {
					binderProcesses.put(new ObjectId(key),
							new Workflow.Pair<ObjectId, ObjectId>(new ObjectId(binderProcessesPlainOrig.get(key)),
									new ObjectId(binderProcessesPlainDest.get(key))));
				}
			}

			return new Workflow(source.getObjectId("_id"), source.getObjectId("userId"), process, objCoords,
					processExecutionDetails, executorServices, binders, binderProcesses, executorAssigned);
		}
	}

}

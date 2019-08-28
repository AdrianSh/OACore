package es.jovenesadventistas.oacore.repository.converters;

import java.util.concurrent.SubmissionPublisher;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import es.jovenesadventistas.arnion.process.AProcess;
import es.jovenesadventistas.arnion.process.binders.Binder;
import es.jovenesadventistas.arnion.process.binders.DirectStdInBinder;
import es.jovenesadventistas.arnion.process.binders.ExitCodeBinder;
import es.jovenesadventistas.arnion.process.binders.RunnableBinder;
import es.jovenesadventistas.arnion.process.binders.StdInBinder;
import es.jovenesadventistas.arnion.process.binders.StdOutBinder;
import es.jovenesadventistas.arnion.process.binders.Publishers.APublisher;
import es.jovenesadventistas.arnion.process.binders.Publishers.ConcurrentLinkedQueuePublisher;
import es.jovenesadventistas.arnion.process.binders.Subscribers.ASubscriber;
import es.jovenesadventistas.arnion.process.binders.Subscribers.ConcurrentLinkedQueueSubscriber;
import es.jovenesadventistas.arnion.process.binders.Transfers.IntegerTransfer;
import es.jovenesadventistas.arnion.process.binders.Transfers.StringTransfer;
import es.jovenesadventistas.arnion.process.binders.Transfers.Transfer;
import es.jovenesadventistas.arnion.process_executor.ProcessExecution.ProcessExecutionDetails;
import es.jovenesadventistas.oacore.controller.AdminController;
import es.jovenesadventistas.oacore.repository.AProcessRepository;
import es.jovenesadventistas.oacore.repository.APublisherRepository;
import es.jovenesadventistas.oacore.repository.ASubscriberRepository;
import es.jovenesadventistas.oacore.repository.BinderRepository;

public class BinderConverter {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	@WritingConverter
	public static class BinderWriteConverter implements Converter<Binder, Document> {
		@SuppressWarnings("unchecked")
		public Document convert(Binder source) {
			AdminController adminController = AdminController.getInstance();
			AProcessRepository aProcessRepository = adminController.getaProcessRepository();
			ASubscriberRepository aSubscriberRepository = adminController.getaSubscriberRepository();
			APublisherRepository aPublisherRepository = adminController.getaPublisherRepository();
			BinderRepository binderRepository = adminController.getBinderRepository();
			Document document = new Document();
			document.put("_id", source.getId());
			String binderType = source.getClass().getName();
			document.put("binderType", binderType);
			ASubscriber<?> sub = null;
			APublisher pub = null;
			AProcess proc = null;
			ProcessExecutionDetails procExecDetls = null;

			try {
				switch (binderType) {
				case "es.jovenesadventistas.arnion.process.binders.DirectStdInBinder":
					DirectStdInBinder directStdInBinder = (DirectStdInBinder) source;
					procExecDetls = directStdInBinder.getProcExecDetails();
					proc = procExecDetls != null ? procExecDetls.getProcess() : null;
					document.put("processId", proc != null ? proc.getId() : null);
					break;
				case "es.jovenesadventistas.arnion.process.binders.ExitCodeBinder":
					try {
						ExitCodeBinder exitCodeBinder = (ExitCodeBinder) source;
						procExecDetls = exitCodeBinder.getProcExecDetails();
						proc = procExecDetls != null ? procExecDetls.getProcess() : null;

						if (proc != null) {
							aProcessRepository.save(proc);
							document.put("processId", proc.getId());
						}
						sub = exitCodeBinder.getSubscriber();
						if (sub != null) {
							aSubscriberRepository.save(sub);
							document.put("subscriberId", sub.getId());
						}
						pub = exitCodeBinder.getAPublisher();
						if (pub != null) {
							aPublisherRepository.save(pub);
							document.put("publisherId", pub.getId());
						}

					} catch (Exception e) {
						logger.error("Could not convert to document an ExitCodeBinder.", e);
						throw new IllegalArgumentException(e);
					}

					break;
				case "es.jovenesadventistas.arnion.process.binders.RunnableBinder":
					RunnableBinder runnableBinder = (RunnableBinder) source;
					Runnable runnable = runnableBinder.getRunnable();
					document.put("runnableTypeName", runnable.getClass().getName());
					if (runnable instanceof Binder) {
						document.put("runnableType", "Binder");
						binderRepository.save((Binder) runnable);
						document.put("runnableId", ((Binder) runnable).getId());
					} else if (runnable instanceof APublisher) {
						document.put("runnableType", "APublisher");
						aPublisherRepository.save((APublisher) runnable);
						document.put("runnableId", ((APublisher) runnable).getId());
					} else if (runnable instanceof ASubscriber<?>) {
						document.put("runnableType", "ASubscriber");
						aSubscriberRepository.save((ASubscriber<Transfer>) runnable);
						document.put("runnableId", ((ASubscriber<Transfer>) runnable).getId());
					} else {
						if (runnable != null)
							throw new IllegalArgumentException(
									"Runnable type cannot be saved: " + runnable.getClass().getName());
					}
					break;
				case "es.jovenesadventistas.arnion.process.binders.StdInBinder":
					try {
						StdInBinder stdInBinder = (StdInBinder) source;
						procExecDetls = stdInBinder.getProcExecDetails();
						proc = procExecDetls != null ? procExecDetls.getProcess() : null;
						if (proc != null) {
							aProcessRepository.save(proc);
							document.put("processId", proc.getId());
						}
						pub = stdInBinder.getStdInAPublisher();
						if (pub != null) {
							aPublisherRepository.save(pub);
							document.put("stdInPublisherId", pub.getId());
						}
						pub = stdInBinder.getStdInErrorAPublisher();
						if (pub != null) {
							aPublisherRepository.save(pub);
							document.put("stdInErrorPublisherId", pub.getId());
						}
					} catch (Exception e) {
						throw new IllegalArgumentException("An error ocurred: " + e.getMessage(), e);
					}
					break;
				case "es.jovenesadventistas.arnion.process.binders.StdOutBinder":
					StdOutBinder stdOutBinder = (StdOutBinder) source;
					procExecDetls = stdOutBinder.getProcExecDetails();
					proc = procExecDetls != null ? procExecDetls.getProcess() : null;
					if (proc != null) {
						aProcessRepository.save(proc);
						document.put("processId", proc.getId());
					}
					break;

				default:
					throw new IllegalArgumentException("Unexpected value for binderType: " + binderType);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Could not convert to document: " + binderType + " Binder: " + source, e);
			}
			return document;
		}
	}

	@ReadingConverter
	public static class BinderReadConverter implements Converter<Document, Binder> {
		@SuppressWarnings("unchecked")
		public Binder convert(Document source) {
			AdminController adminController = AdminController.getInstance();
			AProcessRepository aProcessRepository = adminController.getaProcessRepository();
			ASubscriberRepository aSubscriberRepository = adminController.getaSubscriberRepository();
			APublisherRepository aPublisherRepository = adminController.getaPublisherRepository();
			BinderRepository binderRepository = adminController.getBinderRepository();

			Binder r = null;
			AProcess proc = null;
			ASubscriber<?> sub = null;
			APublisher pub = null, pub2 = null;
			String binderType = source.getString("binderType");
			ObjectId id = null;

			try {
				switch (binderType) {
				case "es.jovenesadventistas.arnion.process.binders.DirectStdInBinder":
					id = source.getObjectId("processId");
					proc = id != null ? aProcessRepository.findById(id) : null;
					DirectStdInBinder directStdInBinder = new DirectStdInBinder(
							proc != null ? new ProcessExecutionDetails(proc) : null);
					r = directStdInBinder;
					break;
				case "es.jovenesadventistas.arnion.process.binders.ExitCodeBinder":
					id = source.getObjectId("processId");
					if (id != null)
						proc = aProcessRepository.findById(id);
					id = source.getObjectId("subscriberId");
					if (id != null)
						sub = aSubscriberRepository.findById(id);
					id = source.getObjectId("publisherId");
					if (id != null)
						pub = aPublisherRepository.findById(id);
					if (sub instanceof ConcurrentLinkedQueueSubscriber
							&& pub instanceof ConcurrentLinkedQueuePublisher) {
						r = new ExitCodeBinder(proc == null ? null : new ProcessExecutionDetails(proc),
								(ConcurrentLinkedQueueSubscriber<IntegerTransfer>) sub,
								(ConcurrentLinkedQueuePublisher<IntegerTransfer>) pub);
					} else {
						String err = "Error trying to convert to an ExitCodeBinder using a Subscriber or Publisher or another type: "
								+ sub.getClass().getName();
						logger.error(err);
						throw new IllegalArgumentException(err);
					}
					break;
				case "es.jovenesadventistas.arnion.process.binders.RunnableBinder":
					Runnable runnable = null;
					switch (source.getString("runnableType")) {
					case "Binder":
						id = source.getObjectId("runnableId");
						if (id != null)
							runnable = binderRepository.findById(id);
						break;
					case "APublisher":
						id = source.getObjectId("runnableId");
						if (id != null)
							runnable = (Runnable) aPublisherRepository.findById(id);
						break;
					case "ASubscriber":
						id = source.getObjectId("runnableId");
						if (id != null)
							runnable = (Runnable) aSubscriberRepository.findById(id);
						break;
					default:
						throw new IllegalArgumentException(
								"Unexpected runnable type value: " + source.getString("runnableType") + " className: "
										+ source.getString("runnableTypeName"));
					}

					r = new RunnableBinder(runnable);
					break;
				case "es.jovenesadventistas.arnion.process.binders.StdInBinder":
					id = source.getObjectId("processId");
					if (id != null)
						proc = aProcessRepository.findById(id);
					id = source.getObjectId("stdInPublisherId");
					if (id != null)
						pub = aPublisherRepository.findById(id);
					id = source.getObjectId("stdInErrorPublisherId");
					if (id != null)
						pub2 = aPublisherRepository.findById(id);
					r = new StdInBinder(proc == null ? null : new ProcessExecutionDetails(proc),
							(SubmissionPublisher<StringTransfer>) pub, (SubmissionPublisher<StringTransfer>) pub2);
					break;
				case "es.jovenesadventistas.arnion.process.binders.StdOutBinder":
					id = source.getObjectId("processId");
					if (id != null)
						proc = aProcessRepository.findById(id);
					r = new StdOutBinder(proc == null ? null : new ProcessExecutionDetails(proc));
					break;
				default:
					throw new IllegalArgumentException("Unexpected value for binderType: " + binderType);
				}

			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Could not parse binder: " + binderType + " document: " + source.toJson() + ", " + e.getMessage(), e);
			}

			ObjectId pid = source.getObjectId("_id");
			if (pid != null && r != null)
				r.setId(pid);
			return r;
		}
	}
}

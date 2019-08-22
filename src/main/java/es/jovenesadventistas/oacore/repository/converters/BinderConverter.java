package es.jovenesadventistas.oacore.repository.converters;

import java.util.concurrent.SubmissionPublisher;

import org.bson.Document;
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
			String binderType = source.getClass().getName();
			document.put("binderType", binderType);
			ASubscriber<?> sub = null;
			APublisher pub = null;
			AProcess proc = null;

			try {
			switch (binderType) {
			case "es.jovenesadventistas.arnion.process.binders.DirectStdInBinder":
				DirectStdInBinder directStdInBinder = (DirectStdInBinder) source;
				document.put("processId", directStdInBinder.getProcExecDetails().getProcess().getId());
				break;
			case "es.jovenesadventistas.arnion.process.binders.ExitCodeBinder":
				try {
					ExitCodeBinder exitCodeBinder = (ExitCodeBinder) source;
					proc = exitCodeBinder.getProcExecDetails().getProcess();
					aProcessRepository.save(proc);
					document.put("processId", proc.getId());
					sub = exitCodeBinder.getSubscriber();
					aSubscriberRepository.save(sub);
					document.put("subscriberId", sub.getId());
					pub = exitCodeBinder.getAPublisher();
					document.put("publisherId", pub.getId());

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
					throw new IllegalArgumentException(
							"Runnable type cannot be saved: " + runnable.getClass().getName());
				}
				break;
			case "es.jovenesadventistas.arnion.process.binders.StdInBinder":
				try {
					StdInBinder stdInBinder = (StdInBinder) source;
					proc = stdInBinder.getProcExecDetails().getProcess();
					aProcessRepository.save(proc);
					document.put("processId", proc.getId());
					pub = stdInBinder.getStdInAPublisher();
					aPublisherRepository.save(pub);
					document.put("stdInPublisherId", pub.getId());
					pub = stdInBinder.getStdInErrorAPublisher();
					aPublisherRepository.save(pub);
					document.put("stdInErrorPublisherId", pub.getId());
				} catch (Exception e) {
					throw new IllegalArgumentException(
							"Publisher is not an APublisher instance: " + pub.getClass().getName(), e);
				}
				break;
			case "es.jovenesadventistas.arnion.process.binders.StdOutBinder":
				StdOutBinder stdOutBinder = (StdOutBinder) source;
				proc = stdOutBinder.getProcExecDetails().getProcess();
				aProcessRepository.save(proc);
				document.put("processId", proc.getId());
				break;

			default:
				throw new IllegalArgumentException("Unexpected value for binderType: " + binderType);
			}
			} catch (Exception e) {
				throw new IllegalArgumentException("Could not convert to document: " + binderType + " Binder: " + source, e);
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

			try {
				switch (binderType) {
				case "es.jovenesadventistas.arnion.process.binders.DirectStdInBinder":
					proc = aProcessRepository.findById(source.getObjectId("processId"));
					DirectStdInBinder directStdInBinder = new DirectStdInBinder(new ProcessExecutionDetails(proc));
					r = directStdInBinder;
					break;
				case "es.jovenesadventistas.arnion.process.binders.ExitCodeBinder":
					proc = aProcessRepository.findById(source.getObjectId("processId"));
					sub = aSubscriberRepository.findById(source.getObjectId("subscriberId"));
					pub = aPublisherRepository.findById(source.getObjectId("publisherId"));
					if (sub instanceof ConcurrentLinkedQueueSubscriber
							&& pub instanceof ConcurrentLinkedQueuePublisher) {
						ExitCodeBinder exitCodeBinder = new ExitCodeBinder(new ProcessExecutionDetails(proc),
								(ConcurrentLinkedQueueSubscriber<IntegerTransfer>) sub,
								(ConcurrentLinkedQueuePublisher<IntegerTransfer>) pub);
						r = exitCodeBinder;
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
						runnable = binderRepository.findById(source.getObjectId("runnableId"));
						break;
					case "APublisher":
						runnable = (Runnable) aPublisherRepository.findById(source.getObjectId("runnableId"));
						break;
					case "ASubscriber":
						runnable = (Runnable) aSubscriberRepository.findById(source.getObjectId("runnableId"));
						break;
					default:
						throw new IllegalArgumentException(
								"Unexpected runnable type value: " + source.getString("runnableType") + " className: "
										+ source.getString("runnableTypeName"));
					}

					r = new RunnableBinder(runnable);
					break;
				case "es.jovenesadventistas.arnion.process.binders.StdInBinder":
					proc = aProcessRepository.findById(source.getObjectId("processId"));
					pub = aPublisherRepository.findById(source.getObjectId("stdInPublisherId"));
					pub2 = aPublisherRepository.findById(source.getObjectId("stdInErrorPublisherId"));
					r = new StdInBinder(new ProcessExecutionDetails(proc), (SubmissionPublisher<StringTransfer>) pub,
							(SubmissionPublisher<StringTransfer>) pub2);
					break;
				case "es.jovenesadventistas.arnion.process.binders.StdOutBinder":
					proc = aProcessRepository.findById(source.getObjectId("processId"));
					r = new StdOutBinder(new ProcessExecutionDetails(proc));
					break;
				default:
					throw new IllegalArgumentException("Unexpected value for binderType: " + binderType);
				}

			} catch (Exception e) {
				throw new IllegalArgumentException("Could not parse binder: " + binderType + " document: " + source.toJson(), e);
			}
			return r;
		}
	}
}

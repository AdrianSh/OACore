package es.jovenesadventistas.oacore.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import es.jovenesadventistas.arnion.process.AProcess;
import es.jovenesadventistas.arnion.process.binders.Binder;
import es.jovenesadventistas.arnion.process.binders.ReactiveStreamBinder;
import es.jovenesadventistas.arnion.process.binders.RunnableBinder;
import es.jovenesadventistas.arnion.process.binders.StdInBinder;
import es.jovenesadventistas.arnion.process.binders.publishers.APublisher;
import es.jovenesadventistas.arnion.process.binders.subscribers.ASubscriber;
import es.jovenesadventistas.arnion.process.binders.transfers.Transfer;
import es.jovenesadventistas.arnion.process_executor.ProcessExecutor;
import es.jovenesadventistas.arnion.process_executor.process_execution.ProcessExecutionDetails;
import es.jovenesadventistas.arnion.workflow.Workflow;
import es.jovenesadventistas.oacore.Messages;
import es.jovenesadventistas.oacore.model.User;
import es.jovenesadventistas.oacore.repository.AProcessRepository;
import es.jovenesadventistas.oacore.repository.APublisherRepository;
import es.jovenesadventistas.oacore.repository.ASubscriberRepository;
import es.jovenesadventistas.oacore.repository.BinderRepository;
import es.jovenesadventistas.oacore.repository.UserRepository;
import es.jovenesadventistas.oacore.repository.WorkflowRepository;
import es.jovenesadventistas.oacore.repository.converters.AProcessConverter;
import es.jovenesadventistas.oacore.repository.converters.APublisherConverter;
import es.jovenesadventistas.oacore.repository.converters.ASubscriberConverter;
import es.jovenesadventistas.oacore.repository.converters.BinderConverter;
import es.jovenesadventistas.oacore.repository.converters.WorkflowConverter;

@RestController
@RequestMapping("/admin")
public class AdminController {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
	// private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AdminController.class);

	@Autowired
	private Environment env;

	@Autowired
	Messages messages;

	// Making repositories accesible to other non-spring classes by using this
	// instance
	private static AdminController instance;
	private ExecutorService executorService;
	private ProcessExecutor pExecutor = ProcessExecutor.getInstance();
	private HashMap<User, List<ObjectId>> executedObjs;

	@Autowired
	private WorkflowRepository workflowRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AProcessRepository aProcessRepository;

	@Autowired
	private APublisherRepository aPublisherRepository;

	@Autowired
	private ASubscriberRepository aSubscriberRepository;

	@Autowired
	private BinderRepository binderRepository;

	private BinderConverter.BinderReadConverter binderReadConverter = new BinderConverter.BinderReadConverter();
	private BinderConverter.BinderWriteConverter binderWriteConverter = new BinderConverter.BinderWriteConverter();
	private WorkflowConverter.WorkflowReadConverter workflowReadConverter = new WorkflowConverter.WorkflowReadConverter();
	private WorkflowConverter.WorkflowWriteConverter workflowWriteConverter = new WorkflowConverter.WorkflowWriteConverter();
	private AProcessConverter.AProcessReadConverter aProcessReadConverter = new AProcessConverter.AProcessReadConverter();
	private AProcessConverter.AProcessWriteConverter aProcessWriteConverter = new AProcessConverter.AProcessWriteConverter();
	private APublisherConverter.APublisherReadConverter aPublisherReadConverter = new APublisherConverter.APublisherReadConverter();
	private APublisherConverter.APublisherWriteConverter aPublisherWriteConverter = new APublisherConverter.APublisherWriteConverter();
	private ASubscriberConverter.ASubscriberReadConverter aSubscriberReadConverter = new ASubscriberConverter.ASubscriberReadConverter();
	private ASubscriberConverter.ASubscriberWriteConverter aSubscriberWriteConverter = new ASubscriberConverter.ASubscriberWriteConverter();

	@PostConstruct
	public void initialize() {
		AdminController.instance = this;
		this.executorService = Executors.newSingleThreadExecutor();
		this.executedObjs = new HashMap<>();
	}

	@ModelAttribute
	public void addAttributes(Model model, Locale locale, HttpServletRequest httpServletRequest) {
		model.addAttribute("s", "/static");
		model.addAttribute("siteUrl", env.getProperty("es.jovenesadventistas.oacore.site-url"));
		model.addAttribute("siteName", env.getProperty("es.jovenesadventistas.oacore.site-name"));
		model.addAttribute("shortSiteName", env.getProperty("es.jovenesadventistas.oacore.short-site-name"));
		model.addAttribute("pageTitle", httpServletRequest.getRequestURI());
		model.addAttribute("defaultPageTitle", "Admin.");

		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		String formattedDate = dateFormat.format(new Date());

		model.addAttribute("serverTime", formattedDate);

		try {
			User u = UserController.getInstance().getPrincipal().getUser();
			model.addAttribute("user", u);
			logger.info("Administration loaded by {}", u.getLogin());
		} catch (Exception e) {
			logger.error("ERROR! Somebody without 'admin' role has requested HK loading!");
		}
	}

	private boolean isAdmin(HttpServletRequest request, HttpServletResponse response) {
		if (!UserController.isAdmin()) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return false;
		} else
			return true;
	}

	@RequestMapping(value = { "/binders" }, method = RequestMethod.GET)
	public Object getBinders(@RequestParam(name = "binder", required = false) String binder, Locale locale, Model model,
			HttpServletResponse response, HttpServletRequest request) {
		if (!this.isAdmin(request, response)) {
			return null;
		} else {
			ArrayList<Class<? extends Binder>> binders = Binder.binders();
			if (binder != null) {
				try {
					Class<?> c = Class.forName(binder);
					// if(c.getDeclaredConstructors()[0].newInstance() instanceof Binder) {
					if (Arrays.asList(c.getInterfaces()).contains(Binder.class)
							|| Arrays.asList(c.getInterfaces()).contains(ReactiveStreamBinder.class)) {
						return null; // ((Binder) c.getDeclaredConstructor(null).newInstance(null)).getForm();
					} else {
						throw new IllegalAccessException("It is not a valid binder name: " + binder);
					}
				} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException
						| SecurityException e) { // | InstantiationException | InvocationTargetException |
													// NoSuchMethodException e) {
					logger.error("Could not get binder properties for " + binder, e);
				}
			}
			return binders;
		}
	}

	private void addExecutedObj(User u, ObjectId id) {
		List<ObjectId> executedObjs = this.executedObjs.get(u);
		if (executedObjs == null) {
			executedObjs = new ArrayList<>();
			this.executedObjs.put(u, executedObjs);
		}

		executedObjs.add(id);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "/start" }, method = RequestMethod.POST)
	public Object test(Locale locale, Model model, HttpServletResponse response, HttpServletRequest request)
			throws Exception {
		response.setContentType("application/json");
		if (!this.isAdmin(request, response) || !request.getContentType().equals("application/json")
				|| request.getContentLengthLong() < 1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}

		User u = UserController.getInstance().getPrincipal().getUser();
		Workflow w = workflowRepository.findByUserId(u.getId()).get(0);

		if (w == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}

		String json = new String(request.getInputStream().readAllBytes());
		logger.debug(json);
		Object r = null;
		Gson gson = new Gson();
		Integer numExecutors = Math.max(1, w.getExecutorServices().size());
		List<ExecutorService> executorServices = new ArrayList<>();
		List<ObjectId> startingProgramIds = new ArrayList<>();
		List<String> startingProgramHIds = gson.fromJson(json, List.class);

		startingProgramHIds.forEach(hId -> {
			logger.info("Starting program Id: " + hId);
			startingProgramIds.add(new ObjectId(hId));
		});

		// Add executor services
		for (int i = 0; i < numExecutors; i++)
			executorServices.add(Executors.newSingleThreadExecutor());

		try {
			for (ObjectId pId : startingProgramIds) {
				// Those programs doesn't have a prec. process
				AProcess p = aProcessRepository.findById(pId);
				if (p == null)
					throw new NullPointerException("This process doesn't exists.");

				// Does it has a binder linked?
				HashMap<ObjectId, Workflow.Pair<ObjectId, ObjectId>> binderAProcess = w
						.getBinderAProcessFromAProcess(pId);
				ProcessExecutionDetails procExDtls = w.getProcExecDetls(pId);

				this.execute(u, procExDtls, pExecutor, executorServices, w, binderAProcess,
						new RunnableBinder(null, procExDtls.getProcess()), null);
			}

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
			r = gson.toJson(e);
		}

		return gson.toJson(r);
	}

	@SuppressWarnings("unchecked")
	private void execute(User u, ProcessExecutionDetails pproc, ProcessExecutor pExecutor,
			List<ExecutorService> executorServices, Workflow w,
			HashMap<ObjectId, Workflow.Pair<ObjectId, ObjectId>> binderAProcess, Binder lastBinder,
			Publisher<Transfer> publisher) {
		logger.debug("Execution chain... Process:" + pproc + ", binderAProcess: " + binderAProcess + ", last Binder: " + lastBinder + ", Publisher: " + publisher);
		this.executorService.execute(() -> {
			try {
				pproc.setBinder(lastBinder);

				if (publisher != null && lastBinder instanceof ASubscriber) {
					logger.debug("Subscribing the last binder {} to publisher {}", lastBinder, publisher);
					publisher.subscribe((Subscriber<? super Transfer>) lastBinder);
				}

				logger.debug("# Executing Binder (1) {}", lastBinder);
				this.executeObj(u, lastBinder, executorServices, w); // Execute Binder (1)

				if (this.pExecutor.canExecute(pproc)) {
					logger.debug("# Executing Process {}", pproc);
					this.executeObj(u, pproc, executorServices, w); // Execute Process
				} else {
					throw new Exception("Cannot execute AProcess id: " + pproc.getProcess().getId()
							+ " binder is not ready. The chain of execution is stopped.");
				}

				if (binderAProcess != null) {
					binderAProcess.forEach((p2, binders) -> {
						Binder b1 = binderRepository.findById(binders.o1()),
								b2 = binderRepository.findById(binders.o2()), bOrg, bDest;

						if (b1 != null && b1.getAProcess().getId().equals(pproc.getProcess().getId())) {
							bOrg = b1;
							bDest = b2;
						} else {
							bOrg = b2;
							bDest = b1;
						}

						bOrg.onFinish((Void) -> {
							logger.debug("Binder executed: " + bOrg.getId());
							return Void;
						});

						bDest.onFinish((Void) -> {
							logger.debug("Binder executed: " + bOrg.getId());
							return Void;
						});

						if (publisher != null && bOrg instanceof ASubscriber) {
							logger.debug("Subscribing the bOrg binder {} to publisher {}", bOrg, publisher);
							publisher.subscribe((Subscriber<? super Transfer>) bOrg);
						}
						
						try {
							logger.debug("# Executing Binder (2) {}", bOrg);
							this.executeObj(u, bOrg, executorServices, w); // Execute Binder (2)
						} catch (Exception e) {
							logger.error("Could not execute the second binder: " + bOrg, e);
						}

						// Recursive call to follow the execution chain
						HashMap<ObjectId, Workflow.Pair<ObjectId, ObjectId>> nextBinderAProcess = w
								.getBinderAProcessFromAProcess(p2);
						ProcessExecutionDetails nextProcessExcDtls = w.getProcExecDetls(p2);

						logger.debug("Following execution chain to AProcess: " + p2);

						Publisher<Transfer> pub = bOrg instanceof Publisher ? (Publisher<Transfer>) bOrg : null;

						try {
							if (bOrg instanceof RunnableBinder && ((RunnableBinder) bOrg).getRunnable() != null
									&& ((RunnableBinder) bOrg).getRunnable() instanceof Publisher)
								pub = (Publisher<Transfer>) ((RunnableBinder) bOrg).getRunnable();
							else if (bOrg instanceof StdInBinder && ((StdInBinder) bOrg).getStdInAPublisher() != null)
								pub = (Publisher<Transfer>) ((StdInBinder) bOrg).getStdInAPublisher();
						} catch (Exception e) {
							logger.error("Could not set the Publisher for the next execution...", e);
						}

						this.execute(u, nextProcessExcDtls, pExecutor, executorServices, w, nextBinderAProcess, bDest,
								pub);
					});
				} else {
					this.executorService.execute(() -> {
						logger.info("Turning everything off...");
						executorServices.forEach(e -> {
							e.shutdown();
						});
					});
				}
			} catch (Exception e) {
				logger.error("An error ocurred when following the execution chain... ", e);
			}
		});
	}

	private void executeObj(User u, ProcessExecutionDetails procExcDetls, List<ExecutorService> executorServices,
			Workflow w) throws IOException {
		ObjectId id = procExcDetls.getProcess().getId();
		Integer executorNum = w.getExecutorNumAssigned(id);
		logger.debug("Execute AProcess on executor num: " + executorNum + ", ProcessExecutionDetails: " + procExcDetls);
		this.pExecutor.execute(executorServices.get(executorNum), procExcDetls, (executed) -> {
			if (executed)
				this.addExecutedObj(u, id);
			else
				logger.error("Could not execute AProcess id: " + id);
			return null;
		});
	}

	private void executeObj(User u, Binder binder, List<ExecutorService> executorServices, Workflow w)
			throws IOException {
		ObjectId id = binder.getId();
		Integer executorNum = w.getExecutorNumAssigned(id);
		logger.debug("Execute Binder on executor num: " + executorNum + ", Binder: " + binder);
		this.pExecutor.execute(executorServices.get(executorNum), binder, (executed) -> {
			if (executed)
				this.addExecutedObj(u, id);
			else
				logger.error("Could not execute Binder id: " + id);
			return null;
		});
	}

	@RequestMapping(value = { "/generic/{type}" }, method = RequestMethod.GET)
	public Object readGenericByUserId(@PathVariable("type") String type, Locale locale, Model model,
			HttpServletResponse response, HttpServletRequest request) throws IOException {
		Object r = null;
		if (this.isAdmin(request, response)) {
			User u = UserController.getInstance().getPrincipal().getUser();

			switch (type.toLowerCase()) {
			case "workflow":
				List<Workflow> w = workflowRepository.findByUserId(u.getId());
				if (w != null && w.size() > 0) {
					Workflow wkf = w.get(0);

					if (wkf != null)
						r = this.workflowWriteConverter.convert(wkf).toJson();
				} else {
					Workflow wkf = new Workflow(u.getId());
					wkf = this.workflowRepository.save(wkf);
					r = this.workflowWriteConverter.convert(wkf).toJson();
				}
				break;
			case "aprocess":
				List<AProcess> aProc = aProcessRepository.findByUserId(u.getId());
				if (aProc != null && aProc.size() > 0)
					r = this.aProcessWriteConverter.convert(aProc.get(0)).toJson();
				break;
			default:
				response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			}
		}

		if (r == null || !(r instanceof String) && r instanceof Optional && ((Optional<?>) r).isEmpty())
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return r;
	}

	@RequestMapping(value = { "/generic/{type}/{id}" }, method = RequestMethod.GET)
	public Object readGeneric(@PathVariable("type") String type, @PathVariable("id") String id, Locale locale,
			Model model, HttpServletResponse response, HttpServletRequest request) throws IOException {
		Object r = null;
		if (this.isAdmin(request, response)) {
			try {
				switch (type.toLowerCase()) {
				case "binder":
					Optional<Binder> b = binderRepository.findById(id);
					if (b.isPresent())
						r = this.binderWriteConverter.convert(b.get()).toJson();
					break;
				case "workflow":
					Optional<Workflow> w = workflowRepository.findById(id);
					if (w.isPresent())
						r = this.workflowWriteConverter.convert(w.get()).toJson();
					break;
				case "aprocess":
					Optional<AProcess> aProc = aProcessRepository.findById(id);
					if (aProc.isPresent())
						r = this.aProcessWriteConverter.convert(aProc.get()).toJson();
					break;
				case "apublisher":
					Optional<APublisher> aPubl = aPublisherRepository.findById(id);
					if (aPubl.isPresent())
						r = this.aPublisherWriteConverter.convert(aPubl.get()).toJson();
					break;
				case "asubscriber":
					Optional<ASubscriber<?>> aSubs = aSubscriberRepository.findById(id);
					if (aSubs.isPresent())
						r = this.aSubscriberWriteConverter.convert(aSubs.get()).toJson();
					break;
				default:
					response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
				}
			} catch (Exception e) {
				logger.error("Error while getting " + type, e);
			}
		}

		if (r == null || !(r instanceof String) && ((Optional<?>) r).isEmpty())
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		return r;
	}

	/**
	 * Request should always include:
	 * 
	 * X-CSRF-TOKEN: XXXXXXXX Origin: http://xxxxxx Content-Type: application/json
	 * 
	 */
	@RequestMapping(value = { "/generic/{type}/{id}" }, method = RequestMethod.DELETE)
	public ObjectId deleteGeneric(@PathVariable("type") String type, @PathVariable("id") String id, Locale locale,
			Model model, HttpServletResponse response, HttpServletRequest request) throws IOException {
		ObjectId r = null;
		// User u = UserController.getInstance().getPrincipal().getUser();
		if (this.isAdmin(request, response)) {
			try {
				switch (type.toLowerCase()) {
				case "binder":
					Optional<Binder> b = binderRepository.findById(id);
					if (b.isPresent()) {
						r = b.get().getId();
						binderRepository.delete(b.get());
					}
					break;
				case "workflow":
					Optional<Workflow> w = workflowRepository.findById(id);
					if (w.isPresent()) {
						r = w.get().getId();
						workflowRepository.delete(w.get());
					}
					break;
				case "aprocess":
					Optional<AProcess> aProc = aProcessRepository.findById(id);
					if (aProc.isPresent()) {
						r = aProc.get().getId();
						aProcessRepository.delete(aProc.get());
					}
					break;
				case "apublisher":
					Optional<APublisher> aPubl = aPublisherRepository.findById(id);
					if (aPubl.isPresent()) {
						r = aPubl.get().getId();
						aPublisherRepository.delete(aPubl.get());
					}
					break;
				case "asubscriber":
					Optional<ASubscriber<?>> aSubs = aSubscriberRepository.findById(id);
					if (aSubs.isPresent()) {
						r = aSubs.get().getId();
						aSubscriberRepository.delete(aSubs.get());
					}
					break;
				default:
					response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
				}
			} catch (Exception e) {
				logger.error("Error while deleting " + type, e);
			}
		}

		if (r == null)
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		return r;
	}

	/**
	 * Request should always include:
	 * 
	 * X-CSRF-TOKEN: XXXXXXXX Origin: http://xxxxxx Content-Type: application/json
	 * 
	 */
	@RequestMapping(value = { "/generic/{type}" }, method = RequestMethod.PUT)
	public Object updateGeneric(@PathVariable("type") String type, Locale locale, Model model,
			HttpServletResponse response, HttpServletRequest request) throws IOException {
		if (!request.getContentType().equals("application/json") || request.getContentLengthLong() < 1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}

		User u = UserController.getInstance().getPrincipal().getUser();
		String json = new String(request.getInputStream().readAllBytes());
		Object r = null;
		Document d = Document.parse(json);
		ObjectId id = d.getObjectId("_id");

		if (id == null)
			response.sendError(HttpServletResponse.SC_NO_CONTENT);
		else if (this.isAdmin(request, response)) {
			try {
				switch (type.toLowerCase()) {
				case "binder":
					if (binderRepository.findById(id) != null) {
						Binder b = this.binderReadConverter.convert(d);
						if (b != null)
							r = this.binderWriteConverter.convert(binderRepository.save(b)).toJson();
					}
					break;
				case "workflow":
					if (workflowRepository.findById(id) != null) {
						Workflow w = this.workflowReadConverter.convert(d);
						w.setUserId(u.getId());
						if (w != null)
							r = this.workflowWriteConverter.convert(workflowRepository.save(w)).toJson();
					}
					break;
				case "aprocess":
					if (aProcessRepository.findById(id) != null) {
						AProcess aProcess = this.aProcessReadConverter.convert(d);
						aProcess.setUserId(u.getId());
						if (aProcess != null)
							r = this.aProcessWriteConverter.convert(aProcessRepository.save(aProcess)).toJson();
					}
					break;
				case "apublisher":
					if (aPublisherRepository.findById(id) != null) {
						APublisher aPublisher = this.aPublisherReadConverter.convert(d);
						if (aPublisher != null)
							r = this.aPublisherWriteConverter.convert(aPublisherRepository.save(aPublisher)).toJson();
					}
					break;
				case "asubscriber":
					if (aSubscriberRepository.findById(id) != null) {
						ASubscriber<?> aSubscriber = this.aSubscriberReadConverter.convert(d);
						if (aSubscriber != null)
							r = this.aSubscriberWriteConverter.convert(aSubscriberRepository.save(aSubscriber))
									.toJson();
					}
					break;
				default:
					response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
				}
			} catch (Exception e) {
				logger.error("Error while putting " + type, e);
			}
			if (r == null)
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		return r;
	}

	/**
	 * Request should always include:
	 * 
	 * X-CSRF-TOKEN: XXXXXXXX Origin: http://xxxxxx Content-Type: application/json
	 * 
	 */
	@RequestMapping(value = { "/generic/{type}" }, method = RequestMethod.POST)
	public Object createGeneric(@PathVariable("type") String type, Locale locale, Model model,
			HttpServletResponse response, HttpServletRequest request) throws IOException {
		response.setContentType("application/json");
		if (!request.getContentType().equals("application/json") || request.getContentLengthLong() < 1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}

		User u = UserController.getInstance().getPrincipal().getUser();
		String json = new String(request.getInputStream().readAllBytes());
		Object r = null;
		logger.debug(json);
		Document d = Document.parse(json);
		ObjectId id = d.getObjectId("_id");

		if (this.isAdmin(request, response)) {
			try {
				switch (type.toLowerCase()) {
				case "binder":
					if (binderRepository.findById(id) == null) {
						Binder b = this.binderReadConverter.convert(d);
						if (b != null) {
							b = binderRepository.save(b);
							r = this.binderWriteConverter.convert(b).toJson();
						} else
							response.sendError(HttpServletResponse.SC_NOT_FOUND);
					} else {
						response.sendError(HttpServletResponse.SC_CONFLICT);
					}
					break;
				case "workflow":
					if (workflowRepository.findById(id) == null) {
						Workflow w = this.workflowReadConverter.convert(d);
						if (w != null) {
							w.setUserId(u.getId());
							w = workflowRepository.save(w);
							r = this.workflowWriteConverter.convert(w).toJson();
						} else
							response.sendError(HttpServletResponse.SC_NOT_FOUND);
					} else {
						response.sendError(HttpServletResponse.SC_CONFLICT);
					}
					break;
				case "aprocess":
					if (aProcessRepository.findById(id) == null) {
						AProcess aProcess = this.aProcessReadConverter.convert(d);
						if (aProcess != null) {
							aProcess.setUserId(u.getId());
							aProcess = aProcessRepository.save(aProcess);
							r = this.aProcessWriteConverter.convert(aProcess).toJson();
						} else
							response.sendError(HttpServletResponse.SC_NOT_FOUND);
					} else {
						response.sendError(HttpServletResponse.SC_CONFLICT);
					}
					break;
				case "apublisher":
					if (aPublisherRepository.findById(id) == null) {
						APublisher aPublisher = this.aPublisherReadConverter.convert(d);
						if (aPublisher != null) {
							aPublisher = aPublisherRepository.save(aPublisher);
							r = this.aPublisherWriteConverter.convert(aPublisher).toJson();
						} else
							response.sendError(HttpServletResponse.SC_NOT_FOUND);
					} else {
						response.sendError(HttpServletResponse.SC_CONFLICT);
					}
					break;
				case "asubscriber":
					if (aSubscriberRepository.findById(id) == null) {
						ASubscriber<?> aSubscriber = this.aSubscriberReadConverter.convert(d);
						if (aSubscriber != null) {
							aSubscriber = aSubscriberRepository.save(aSubscriber);
							r = this.aSubscriberWriteConverter.convert(aSubscriber).toJson();
						} else
							response.sendError(HttpServletResponse.SC_NOT_FOUND);
					} else {
						response.sendError(HttpServletResponse.SC_CONFLICT);
					}
					break;
				default:
					response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
				}
			} catch (Exception e) {
				logger.error("Error while post-ing " + type, e);
			}
			if (r == null)
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		return r;
	}

	public static AdminController getInstance() {
		return instance;
	}

	public static void setInstance(AdminController instance) {
		AdminController.instance = instance;
	}

	public WorkflowRepository getWorkflowRepository() {
		return workflowRepository;
	}

	public void setWorkflowRepository(WorkflowRepository workflowRepository) {
		this.workflowRepository = workflowRepository;
	}

	public UserRepository getUserRepository() {
		return userRepository;
	}

	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public AProcessRepository getaProcessRepository() {
		return aProcessRepository;
	}

	public void setaProcessRepository(AProcessRepository aProcessRepository) {
		this.aProcessRepository = aProcessRepository;
	}

	public APublisherRepository getaPublisherRepository() {
		return aPublisherRepository;
	}

	public void setaPublisherRepository(APublisherRepository aPublisherRepository) {
		this.aPublisherRepository = aPublisherRepository;
	}

	public ASubscriberRepository getaSubscriberRepository() {
		return aSubscriberRepository;
	}

	public void setaSubscriberRepository(ASubscriberRepository aSubscriberRepository) {
		this.aSubscriberRepository = aSubscriberRepository;
	}

	public BinderRepository getBinderRepository() {
		return binderRepository;
	}

	public void setBinderRepository(BinderRepository binderRepository) {
		this.binderRepository = binderRepository;
	}
}

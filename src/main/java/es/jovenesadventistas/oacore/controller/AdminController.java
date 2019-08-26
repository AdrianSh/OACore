package es.jovenesadventistas.oacore.controller;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import es.jovenesadventistas.arnion.process.AProcess;
import es.jovenesadventistas.arnion.process.binders.Binder;
import es.jovenesadventistas.arnion.process.binders.ReactiveStreamBinder;
import es.jovenesadventistas.arnion.process.binders.StdInBinder;
import es.jovenesadventistas.arnion.process.binders.StdOutBinder;
import es.jovenesadventistas.arnion.process.binders.Publishers.APublisher;
import es.jovenesadventistas.arnion.process.binders.Publishers.ConcurrentLinkedQueuePublisher;
import es.jovenesadventistas.arnion.process.binders.Subscribers.ASubscriber;
import es.jovenesadventistas.arnion.process.binders.Subscribers.SocketSubscriber;
import es.jovenesadventistas.arnion.process.binders.Transfers.StringTransfer;
import es.jovenesadventistas.arnion.process_executor.ProcessExecution.ProcessExecutionDetails;
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
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private Environment env;

	@Autowired
	Messages messages;

	// Making repositories accesible to other non-spring classes by using this
	// instance
	private static AdminController instance;

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
		return true;
		/*
		 * DESCOMENTAR ESTO y SECURITY CONFIG!!! IDEA: Agregar a los logs de actividad
		 * lo que se tiene de la request if (!UserController.isAdmin()) {
		 * response.setStatus(HttpServletResponse.SC_FORBIDDEN); return false; } else
		 * return true;
		 */
	}

	@CrossOrigin(origins = "http://localhost:39429")
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
	
	@CrossOrigin(origins = "http://localhost:39429")
	@RequestMapping(value = { "/test" }, method = RequestMethod.GET)
	public Object test(@RequestParam(name = "binder", required = false) String binder, Locale locale, Model model,
			HttpServletResponse response, HttpServletRequest request) throws Exception {
		if (!this.isAdmin(request, response)) {
			return null;
		} else {
			User u = UserController.getInstance().getPrincipal().getUser();
			Workflow w = new Workflow(u.getId());
			
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			ExecutorService executorService2 = Executors.newSingleThreadExecutor();
			
			w.getExecutorServices().add(executorService);
			w.getExecutorServices().add(executorService2);

			
			AProcess p1 = new AProcess("C:\\Program Files\\nodejs\\node.exe", "index.js", "read", "input0.txt");
			p1.setWorkingDirectory(new File("C:\\Privado\\TFG\\Arnion-Processes\\File\\"));
			AProcess p2 = new AProcess("C:\\\\Program Files\\\\nodejs\\\\node.exe", "index.js", "write", "output1.txt");
			p2.setWorkingDirectory(new File("C:\\Privado\\TFG\\Arnion-Processes\\File\\"));

			aProcessRepository.save(p1);
			aProcessRepository.save(p2);
			
			ProcessExecutionDetails pExec1 = new ProcessExecutionDetails(p1);
			ProcessExecutionDetails pExec2 = new ProcessExecutionDetails(p2);
			
			w.addProcess(p1);
			w.addProcess(p2);

			// Binder section
			ConcurrentLinkedQueuePublisher<StringTransfer> pub1 = new ConcurrentLinkedQueuePublisher<StringTransfer>();
			StdInBinder b1 = new StdInBinder(pExec1, pub1, null);
			StdOutBinder b2 = new StdOutBinder(pExec2);

			w.getBinders().add(b1);
			w.getBinders().add(b2);

			// Join the output of the process 1 to the input of the process 2
			b1.markAsReady();
			pExec1.setBinder(b1);
			pExec2.setBinder(b2);
			pub1.subscribe(b2);
			

			aPublisherRepository.save(pub1);
			binderRepository.save(b1);
			binderRepository.save(b2);
			workflowRepository.save(w);
			
			ASubscriber<?> asub = new SocketSubscriber<StringTransfer>(new Socket("localhost", 21));
			aSubscriberRepository.save(asub);
			
			return this.workflowWriteConverter.convert(w).toJson();
		}
	}
	
	@CrossOrigin(origins = "http://localhost:39429")
	@RequestMapping(value = { "/generic/{type}" }, method = RequestMethod.GET)
	public Object readGenericByUserId(@PathVariable("type") String type, Locale locale,
			Model model, HttpServletResponse response, HttpServletRequest request) throws IOException {
		Object r = null;
		if (this.isAdmin(request, response)) {
			User u = UserController.getInstance().getPrincipal().getUser();
			
			switch (type.toLowerCase()) {
			case "workflow":
				List<Workflow> w = workflowRepository.findByUserId(u.getId());
				if (w != null && w.size() > 0)
					r = this.workflowWriteConverter.convert(w.get(0)).toJson();
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

		if (r == null || !(r instanceof String) && ((Optional<?>) r).isEmpty())
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return r;
	}
	
	@CrossOrigin(origins = "http://localhost:39429")
	@RequestMapping(value = { "/generic/{type}/{id}" }, method = RequestMethod.GET)
	public Object readGeneric(@PathVariable("type") String type, @PathVariable("id") String id, Locale locale,
			Model model, HttpServletResponse response, HttpServletRequest request) throws IOException {
		Object r = null;
		if (this.isAdmin(request, response)) {
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
		}

		if (r == null || !(r instanceof String) && ((Optional<?>) r).isEmpty())
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		return r;
	}

	@CrossOrigin(origins = "http://localhost:39429")
	@RequestMapping(value = { "/generic/{type}/{id}" }, method = RequestMethod.DELETE)
	public ObjectId deleteGeneric(@PathVariable("type") String type, @PathVariable("id") String id, Locale locale,
			Model model, HttpServletResponse response, HttpServletRequest request) throws IOException {
		ObjectId r = null;
		if (this.isAdmin(request, response)) {
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
		}

		if (r == null)
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		return r;
	}

	@CrossOrigin(origins = "http://localhost:39429")
	@RequestMapping(value = { "/generic/{type}" }, method = RequestMethod.PUT)
	public Object updateGeneric(@PathVariable("type") String type, Locale locale, Model model,
			HttpServletResponse response, HttpServletRequest request) throws IOException {
		if (request.getContentType() != "application/json" || request.getContentLengthLong() < 1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}

		String json = new String(request.getInputStream().readAllBytes());
		Object r = null;
		Document d = Document.parse(json);
		ObjectId id = d.getObjectId("_id");

		if (id == null)
			response.sendError(HttpServletResponse.SC_NO_CONTENT);
		else if (this.isAdmin(request, response)) {
			switch (type.toLowerCase()) {
			case "binder":
				if (binderRepository.findById(id) != null) {
					Binder b = this.binderReadConverter.convert(d);
					if (b != null)
						r = binderRepository.save(b);

				}
				break;
			case "workflow":
				if (workflowRepository.findById(id) != null) {
					Workflow w = this.workflowReadConverter.convert(d);
					if (w != null)
						r = workflowRepository.save(w);
				}
				break;
			case "aprocess":
				if (aProcessRepository.findById(id) != null) {
					AProcess aProcess = this.aProcessReadConverter.convert(d);
					if (aProcess != null)
						r = aProcessRepository.save(aProcess);
				}
				break;
			case "apublisher":
				if (aPublisherRepository.findById(id) != null) {
					APublisher aPublisher = this.aPublisherReadConverter.convert(d);
					if (aPublisher != null)
						r = aPublisherRepository.save(aPublisher);
				}
				break;
			case "asubscriber":
				if (aSubscriberRepository.findById(id) != null) {
					ASubscriber<?> aSubscriber = this.aSubscriberReadConverter.convert(d);
					if (aSubscriber != null)
						r = aSubscriberRepository.save(aSubscriber);
				}
				break;
			default:
				response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
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
	@CrossOrigin(origins = "http://localhost:39429", exposedHeaders = {"Access-Control-Allow-Origin"})
	@RequestMapping(value = { "/generic/{type}" }, method = RequestMethod.POST)
	public Object createGeneric(@PathVariable("type") String type, Locale locale, Model model,
			HttpServletResponse response, HttpServletRequest request) throws IOException {
		response.setContentType("application/json");
		if (!request.getContentType().equals("application/json") || request.getContentLengthLong() < 1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}

		String json = new String(request.getInputStream().readAllBytes());
		Object r = null;
		Document d = Document.parse(json);
		ObjectId id = d.getObjectId("_id");

		if (this.isAdmin(request, response)) {
			switch (type.toLowerCase()) {
			case "binder":
				if (binderRepository.findById(id) == null) {
					Binder b = this.binderReadConverter.convert(d);
					if (b != null) {
						r = binderRepository.save(b);
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
						// w.setUserId();
						r = workflowRepository.save(w);
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
						r = aProcessRepository.save(aProcess);
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
						r = aPublisherRepository.save(aPublisher);
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
						r = aSubscriberRepository.save(aSubscriber);
					} else
						response.sendError(HttpServletResponse.SC_NOT_FOUND);
				} else {
					response.sendError(HttpServletResponse.SC_CONFLICT);
				}
				break;
			default:
				response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			}
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

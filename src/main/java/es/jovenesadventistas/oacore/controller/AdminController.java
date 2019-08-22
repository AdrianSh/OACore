package es.jovenesadventistas.oacore.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException.NotFound;

import es.jovenesadventistas.arnion.process.AProcess;
import es.jovenesadventistas.arnion.process.binders.Binder;
import es.jovenesadventistas.arnion.process.binders.ReactiveStreamBinder;
import es.jovenesadventistas.arnion.process.binders.Publishers.APublisher;
import es.jovenesadventistas.arnion.process.binders.Subscribers.ASubscriber;
import es.jovenesadventistas.arnion.workflow.Workflow;
import es.jovenesadventistas.oacore.Messages;
import es.jovenesadventistas.oacore.UserDetails;
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
	
	// Making repositories accesible to other non-spring classes by using this instance
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

	@CrossOrigin(origins = "http://localhost:26712")
	@RequestMapping(value = { "/binders" }, method = RequestMethod.GET)
	public Object getBinders(@RequestParam(name = "binder", required = false) String binder,
			Locale locale, Model model, HttpServletResponse response, HttpServletRequest request) {
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
				} catch (ClassNotFoundException | IllegalAccessException
						| IllegalArgumentException | SecurityException e) { // | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
					logger.error("Could not get binder properties for " + binder, e);
				}
			}
			return binders;
		}
	}
	
	@CrossOrigin(origins = "http://localhost:26712")
	@RequestMapping(value = { "/generic/{type}/{id}" }, method = RequestMethod.GET)
	public Object readGeneric(@PathVariable("type") String type, @PathVariable("id") String id,
			Locale locale, Model model, HttpServletResponse response, HttpServletRequest request) throws IOException {
		Object r = null;
		if (this.isAdmin(request, response)) {
			switch (type.toLowerCase()) {
			case "binder":
				r = binderRepository.findById(id);
				break;
			case "workflow":
				r = workflowRepository.findById(id);
				break;
			case "aprocess":
				r = aProcessRepository.findById(id);
				break;
			case "apublisher":
				r = aPublisherRepository.findById(id);
				break;
			case "asubscriber":
				r = aSubscriberRepository.findById(id);
				break;
			default:
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
		
		if(r == null || ((Optional<?>) r).isEmpty())
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		return r;
	}
	
	@CrossOrigin(origins = "http://localhost:26712")
	@RequestMapping(value = { "/generic/{type}" }, method = RequestMethod.POST)
	public Object createGeneric(@PathVariable("type") String type, @PathVariable("data") String json,
			Locale locale, Model model, HttpServletResponse response, HttpServletRequest request) {
		Object r = null;
		if (this.isAdmin(request, response)) {
			switch (type.toLowerCase()) {
			case "binder":
				Binder b = new BinderConverter.BinderReadConverter().convert(Document.parse(json));
				r = binderRepository.save(b);
				break;
			case "workflow":
				Workflow w = new WorkflowConverter.WorkflowReadConverter().convert(Document.parse(json));
				r = workflowRepository.save(w);
				break;
			case "aprocess":
				AProcess aProcess = new AProcessConverter.AProcessReadConverter().convert(Document.parse(json));
				r = aProcessRepository.save(aProcess);
				break;
			case "apublisher":
				APublisher aPublisher = new APublisherConverter.APublisherReadConverter().convert(Document.parse(json));
				r = aPublisherRepository.save(aPublisher);
				break;
			case "asubscriber":
				ASubscriber<?> aSubscriber = new ASubscriberConverter.ASubscriberReadConverter().convert(Document.parse(json));
				r = aSubscriberRepository.save(aSubscriber);
				break;
			default:
				throw new IllegalArgumentException("Unexpected type: " + type.toLowerCase());
			}
		}
		return r;
	}
	
	@CrossOrigin(origins = "http://localhost:26712")
	@RequestMapping(value = { "/workflow" }, method = RequestMethod.GET)
	public Object getWorkflow(@RequestParam(name = "id", required = false) String id,
			Locale locale, Model model, HttpServletResponse response, HttpServletRequest request) {
		if (!this.isAdmin(request, response)) {
			return null;
		} else {
			UserDetails ud = UserController.getInstance().getPrincipal();
			User u = ud != null ? ud.getUser() : null;
			if(u == null)
				return null;
			
			Workflow w = workflowRepository.findByUserId(u.getId());
			if(w == null) {
				w = new Workflow(u.getId());
				// u.setMainWorkflow(w);
				// userRepository.save(u);
				
				AProcess p = new AProcess("test", "-a");
				p.setUserId(u.getId());
				aProcessRepository.save(p);
				
				w.addProcess(p);
				workflowRepository.save(w);
			}
			return w;
		}
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

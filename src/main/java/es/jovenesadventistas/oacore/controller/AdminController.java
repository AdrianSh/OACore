package es.jovenesadventistas.oacore.controller;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.jovenesadventistas.arnion.process.AProcess;
import es.jovenesadventistas.arnion.process.binders.Binder;
import es.jovenesadventistas.arnion.process.binders.ReactiveStreamBinder;
import es.jovenesadventistas.arnion.workflow.Workflow;
import es.jovenesadventistas.oacore.Messages;
import es.jovenesadventistas.oacore.UserDetails;
import es.jovenesadventistas.oacore.model.User;
import es.jovenesadventistas.oacore.repository.AProcessRepository;
import es.jovenesadventistas.oacore.repository.UserRepository;
import es.jovenesadventistas.oacore.repository.WorkflowRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private Environment env;

	@Autowired
	Messages messages;
	
	@Autowired
	private WorkflowRepository workflowRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private AProcessRepository aProcessRepository;

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

	@CrossOrigin(origins = "http://localhost:24674")
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
						return ((Binder) c.getDeclaredConstructor(null).newInstance(null)).getForm();
					} else {
						throw new IllegalAccessException("It is not a valid binder name: " + binder);
					}
				} catch (ClassNotFoundException | IllegalAccessException
						| IllegalArgumentException | SecurityException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
					logger.error("Could not get binder properties for " + binder, e);
				}
			}
			return binders;
		}
	}
	
	@CrossOrigin(origins = "http://localhost:24674")
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
				u.setMainWorkflow(w);
				userRepository.save(u);
				
				AProcess p = new AProcess("test", "-a");
				p.setUserId(u.getId());
				aProcessRepository.save(p);
				
				w.addProcessId(p.getId());
				workflowRepository.save(w);
			}
			return w;
		}
	}
}

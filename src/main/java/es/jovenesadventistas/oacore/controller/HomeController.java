package es.jovenesadventistas.oacore.controller;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import es.jovenesadventistas.oacore.Messages;
import es.jovenesadventistas.oacore.UserDetails;

@Controller
public class HomeController {

	@Autowired
	private Environment env;

	@Autowired
	Messages messages;

	@ModelAttribute
	public void addAttributes(Model model, Locale locale, HttpServletRequest httpServletRequest) {
		model.addAttribute("s", "/static");
		model.addAttribute("siteUrl", env.getProperty("es.jovenesadventistas.oacore.site-url"));
		model.addAttribute("siteName", env.getProperty("es.jovenesadventistas.oacore.site-name"));
		model.addAttribute("shortSiteName", env.getProperty("es.jovenesadventistas.oacore.short-site-name"));
		model.addAttribute("pageTitle", httpServletRequest.getRequestURI());
		model.addAttribute("defaultPageTitle", "Bienvenido");

		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		String formattedDate = dateFormat.format(new Date());

		model.addAttribute("serverTime", formattedDate);

		/*
		 * // Articles
		 
		model.addAttribute("categorias",
				entityManager.createNamedQuery("allTagsOrderByDate").setMaxResults(10000).getResultList());
		model.addAttribute("rightArticulos",
				entityManager.createNamedQuery("topArticles").setMaxResults(10).getResultList());
		model.addAttribute("tags", entityManager.createNamedQuery("allTags").getResultList());
		*/

	}

	@RequestMapping(value = { "/", "/home", "/index", "/login" }, method = RequestMethod.GET)
	public String homePage(Locale locale, Model model, @RequestParam(required = false) String error) {
		// model.addAttribute("actividades", entityManager.createNamedQuery("allActividad").getResultList());
		// model.addAttribute("lastarticulos", entityManager.createNamedQuery("allArticulosOrderByDate").setMaxResults(10000).getResultList());

		UserDetails uds = UserController.getInstance().getPrincipal();
		if (uds != null) {
			model.addAttribute("user", uds.getUser());
		}

		if (error != null)
			model.addAttribute("loginError", "Usuario/Contraseña incorrectos.");

		return "articulos/articulos";
	}

	@RequestMapping(value = "/noregistro", method = RequestMethod.GET)
	public String noRegistro(Locale locale, Model model) {
		model.addAttribute("mMensaje", "Debes estar registrado.");
		return "noregistro";
	}

	@RequestMapping(value = "/inicio_sesion", method = RequestMethod.GET)
	public String login(Locale locale, Model model) {
		return "login";
	}

	/**
	 * A not-very-dynamic view that shows an "about us".
	 */
	@RequestMapping(value = { "/about", "/sobre", "/nosotros" }, method = RequestMethod.GET)
	@Transactional
	public String about(Locale locale, Model model) {
		UserDetails uds = UserController.getInstance().getPrincipal();
		if (uds != null) {
			model.addAttribute("user", uds.getUser());
		}

		return "about";
	}
}

package es.jovenesadventistas.oacore.controller;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import es.jovenesadventistas.oacore.Messages;
import es.jovenesadventistas.oacore.model.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

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

	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET)
	@Transactional
	public String admin(Locale locale, Model model) {
		String returnn = "admin";

		if (!UserController.isAdmin()) {
			returnn = "redirect:/admin/login";
		} else {
			User u = UserController.getInstance().getPrincipal().getUser();
			Actividad atv = Actividad.createActividad("Ha entrado a la administración", u, new Date());
			
			// u.addActividad(actvs, atv);
			// model.addAttribute("mensajes", entityManager.createNamedQuery("allMensajesByUser").setParameter("userParam", u).getResultList());
			// model.addAttribute("actividades", entityManager.createNamedQuery("allActividad").setMaxResults(10).getResultList());
		}

		return returnn;
	}

	@RequestMapping(value = "/tables", method = RequestMethod.GET)
	public String adminTables(Locale locale, Model model) {
		String returnn = "admin/tables";

		if (!UserController.isAdmin()) {
			returnn = "redirect:/admin/login";
		} else {
			User u = UserController.getInstance().getPrincipal().getUser();
			/* 
			model.addAttribute("mensajes",
					entityManager.createNamedQuery("allMensajesByUser").setParameter("userParam", u).getResultList());
			model.addAttribute("actividades",
					entityManager.createNamedQuery("allActividad").setMaxResults(10).getResultList());
			model.addAttribute("tabla_users",
					entityManager.createNamedQuery("allUsers").setMaxResults(10000).getResultList());
			model.addAttribute("tabla_comentarios",
					entityManager.createNamedQuery("allComentarios").setMaxResults(10000).getResultList());
			model.addAttribute("tabla_comentarios_perfil",
					entityManager.createNamedQuery("allComentarioPerfil").setMaxResults(10000).getResultList());
			model.addAttribute("tabla_periodicos",
					entityManager.createNamedQuery("allPeriodicos").setMaxResults(10000).getResultList());
			model.addAttribute("tabla_tags",
					entityManager.createNamedQuery("allTags").setMaxResults(10000).getResultList());
					*/
		}

		return returnn;
	}

	@RequestMapping(value = "/forms", method = RequestMethod.GET)
	public String adminForms(Locale locale, Model model) {
		String returnn = "admin/forms";

		if (!UserController.isAdmin()) {
			returnn = "redirect:/admin/login";
		} else {
			User u = UserController.getInstance().getPrincipal().getUser();
			logger.info("Ripper articles interface Opened by {}", u.getLogin());
			// model.addAttribute("periodicos", entityManager.createNamedQuery("allPeriodicos").setMaxResults(10000).getResultList());
		}

		return returnn;
	}

	/* ######## PAGINAS NO USADAS PERO PARA FUTUROS FORMATOS ######### 
	@RequestMapping(value = "/panels-wells", method = RequestMethod.GET)
	public String adminPanelWells(Locale locale, Model model) {
		return !UserController.isAdmin() ? "redirect:/admin/login" : "admin/panels-wells";
	}

	@RequestMapping(value = "/buttons", method = RequestMethod.GET)
	public String adminButtons(Locale locale, Model model) {
		return !UserController.isAdmin() ? "redirect:/admin/login" : "admin/buttons";
	}

	@RequestMapping(value = "/notifications", method = RequestMethod.GET)
	public String adminNotifications(Locale locale, Model model) {
		return !UserController.isAdmin() ? "redirect:/admin/login" : "admin/notifications";
	}

	@RequestMapping(value = "/typography", method = RequestMethod.GET)
	public String adminTypography(Locale locale, Model model) {
		return !UserController.isAdmin() ? "redirect:/admin/login" : "admin/typography";
	}

	@RequestMapping(value = "/icons", method = RequestMethod.GET)
	public String adminIcons(Locale locale, Model model) {
		return !UserController.isAdmin() ? "redirect:/admin/login" : "admin/icons";
	}

	@RequestMapping(value = "/grid", method = RequestMethod.GET)
	public String adminGrid(Locale locale, Model model) {
		return !UserController.isAdmin() ? "redirect:/admin/login" : "admin/grid";
	}

	@RequestMapping(value = "/blank", method = RequestMethod.GET)
	public String adminBlank(Locale locale, Model model) {
		return !UserController.isAdmin() ? "redirect:/admin/login" : "admin/blank";
	}

	@RequestMapping(value = "/loginSettings", method = RequestMethod.GET)
	public String adminLoginSettings(Locale locale, Model model) {
		return !UserController.isAdmin() ? "redirect:/admin/login" : "admin/login";
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String adminlogin(Locale locale, Model model) {
		return UserController.isAdmin() ? "redirect:/admin" : "redirect:/login";
	}

	@RequestMapping(value = "/flot", method = RequestMethod.GET)
	public String adminFlot(Locale locale, Model model) {
		return !UserController.isAdmin() ? "redirect:/admin/login" : "admin/flot";
	}

	@RequestMapping(value = "/morris", method = RequestMethod.GET)
	public String adminMorris(Locale locale, Model model) {
		return !UserController.isAdmin() ? "redirect:/admin/login" : "admin/morris";
	}
	*/
}

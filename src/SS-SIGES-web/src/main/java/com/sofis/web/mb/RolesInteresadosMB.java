package com.sofis.web.mb;

import com.sofis.entities.data.RolesInteresados;
import com.sofis.exceptions.BusinessException;
import com.sofis.web.componentes.SofisPopupUI;
import com.sofis.web.delegates.RolesInteresadosDelegate;
import com.sofis.web.properties.Labels;
import com.sofis.web.utils.JSFUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

@ManagedBean(name = "rolesInteresadosMB")
@ViewScoped
public class RolesInteresadosMB implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(RolesInteresadosMB.class.getName());

	private static final String POPUP_MSG = "popupMsg";
	private static final String ROL_NOMBRE = "rolintNombre";

	@ManagedProperty("#{inicioMB}")
	private InicioMB inicioMB;
	@Inject
	private SofisPopupUI renderPopupEdicion;
	@Inject
	private RolesInteresadosDelegate rolesInteresadosDelegate;

	private String cantElementosPorPagina = "25";
	private String elementoOrdenacion = ROL_NOMBRE;

	// 0=descendente, 1=ascendente.
	private int ascendente = 1;

	private String filtroNombre;
	private List<RolesInteresados> listaResultado;
	private RolesInteresados rolEnEdicion;

	@PostConstruct
	public void init() {

		filtroNombre = "";
		listaResultado = new ArrayList<>();
		rolEnEdicion = new RolesInteresados();

		inicioMB.cargarOrganismoSeleccionado();

		buscar();
	}

	public String agregar() {
		rolEnEdicion = new RolesInteresados();

		renderPopupEdicion.abrir();
		return null;
	}

	public String eliminar(Integer rolPk) {
		if (rolPk != null) {
			try {
				rolesInteresadosDelegate.eliminarRol(rolPk);
				for (RolesInteresados a : listaResultado) {
					if (a.getRolintPk().equals(rolPk)) {
						listaResultado.remove(a);
						break;
					}
				}
			} catch (BusinessException e) {
				logger.log(Level.SEVERE, null, e);

				for (String iterStr : e.getErrores()) {
					JSFUtils.agregarMsgError("", Labels.getValue(iterStr), null);
				}
				inicioMB.setRenderPopupMensajes(Boolean.TRUE);
			}
		}
		return null;
	}

	public String buscar() {
		Map<String, Object> mapFiltro = new HashMap<>();
		mapFiltro.put("nombre", filtroNombre);
		listaResultado = rolesInteresadosDelegate.busquedaRolFiltro(inicioMB.getOrganismo().getOrgPk(), mapFiltro, elementoOrdenacion, ascendente);

		return null;
	}

	public String editar(Integer rolPk) {

		try {
			rolEnEdicion = rolesInteresadosDelegate.obtenerRolesInteresadosPorId(rolPk);
		} catch (BusinessException ex) {
			logger.log(Level.SEVERE, null, ex);

			for (String iterStr : ex.getErrores()) {
				JSFUtils.agregarMsgError(POPUP_MSG, Labels.getValue(iterStr), null);
			}
		}

		renderPopupEdicion.abrir();
		return null;
	}

	public String guardar() {
		rolEnEdicion.setRolintOrgFk(inicioMB.getOrganismo());

		try {
			rolEnEdicion = rolesInteresadosDelegate.guardarRol(rolEnEdicion);

			if (rolEnEdicion != null) {
				renderPopupEdicion.cerrar();
				buscar();
			}
		} catch (BusinessException be) {
			logger.log(Level.SEVERE, be.getMessage(), be);

			for (String iterStr : be.getErrores()) {
				JSFUtils.agregarMsgError(POPUP_MSG, Labels.getValue(iterStr), null);
			}

		}
		return null;
	}

	public void cancelar() {
		renderPopupEdicion.cerrar();
	}

	public String limpiar() {
		filtroNombre = null;
		listaResultado = null;
		elementoOrdenacion = ROL_NOMBRE;
		ascendente = 1;

		return null;
	}

	public void cambiarCantPaginas(ValueChangeEvent evt) {
		buscar();
	}

	public void cambiarCriterioOrdenacion(ValueChangeEvent evt) {
		elementoOrdenacion = evt.getNewValue().toString();
		buscar();
	}

	public void cambiarAscendenteOrdenacion(ValueChangeEvent evt) {
		ascendente = Integer.valueOf(evt.getNewValue().toString());
		buscar();
	}

	public void actualizarHabilitado(RolesInteresados rol) {
		try {
			rol.setHabilitado(!rol.getHabilitado());

			rolesInteresadosDelegate.guardarRol(rol);
		} catch (BusinessException be) {

			for (String iterStr : be.getErrores()) {
				JSFUtils.agregarMsgError(POPUP_MSG, Labels.getValue(iterStr), null);
			}
		}
	}

	public SofisPopupUI getRenderPopupEdicion() {
		return renderPopupEdicion;
	}

	public void setRenderPopupEdicion(SofisPopupUI renderPopupEdicion) {
		this.renderPopupEdicion = renderPopupEdicion;
	}

	public String getCantElementosPorPagina() {
		return cantElementosPorPagina;
	}

	public void setCantElementosPorPagina(String cantElementosPorPagina) {
		this.cantElementosPorPagina = cantElementosPorPagina;
	}

	public String getElementoOrdenacion() {
		return elementoOrdenacion;
	}

	public void setElementoOrdenacion(String elementoOrdenacion) {
		this.elementoOrdenacion = elementoOrdenacion;
	}

	public int getAscendente() {
		return ascendente;
	}

	public void setAscendente(int ascendente) {
		this.ascendente = ascendente;
	}

	public String getFiltroNombre() {
		return filtroNombre;
	}

	public void setFiltroNombre(String filtroNombre) {
		this.filtroNombre = filtroNombre;
	}

	public RolesInteresados getRolEnEdicion() {
		return rolEnEdicion;
	}

	public void setRolEnEdicion(RolesInteresados rolEnEdicion) {
		this.rolEnEdicion = rolEnEdicion;
	}

	public List<RolesInteresados> getListaResultado() {
		return listaResultado;
	}

	public void setListaResultado(List<RolesInteresados> listaResultado) {
		this.listaResultado = listaResultado;
	}

	public void setInicioMB(InicioMB inicioMB) {
		this.inicioMB = inicioMB;
	}

}

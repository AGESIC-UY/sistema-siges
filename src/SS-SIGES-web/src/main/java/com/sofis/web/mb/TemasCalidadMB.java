package com.sofis.web.mb;

import com.sofis.entities.constantes.ConstanteApp;
import com.sofis.entities.constantes.ConstantesEntities;
import com.sofis.entities.data.TemasCalidad;
import com.sofis.exceptions.BusinessException;
import com.sofis.web.componentes.SofisPopupUI;
import com.sofis.web.delegates.TemasCalidadDelegate;
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

/**
 *
 * @author Usuario
 */
@ManagedBean(name = "temasCalidadMB")
@ViewScoped
public class TemasCalidadMB implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ConstanteApp.LOGGER_NAME);
    private static final String BUSQUEDA_MSG = "frmTemaCalidad:btnBuscar";
    private static final String POPUP_MSG = "frmPopup:btnGuardar";

    @ManagedProperty("#{inicioMB}")
    private InicioMB inicioMB;
    @Inject
    private SofisPopupUI renderPopupEdicion;
    @Inject
    private TemasCalidadDelegate entidadDelegate;
    // Variables
    private String cantElementosPorPagina = "25";
    private String elementoOrdenacion = ConstantesEntities.TCA_ATT_NOMBRE;
    // 0=descendente, 1=ascendente.
    private int ascendente = 1;
    private String filtroNombre;
    private List<TemasCalidad> listaResultado;
    private TemasCalidad entidadEnEdicion;

    public TemasCalidadMB() {
        filtroNombre = "";
        listaResultado = new ArrayList<>();
        entidadEnEdicion = new TemasCalidad();
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

    public List<TemasCalidad> getListaResultado() {
        return listaResultado;
    }

    public void setListaResultado(List<TemasCalidad> listaResultado) {
        this.listaResultado = listaResultado;
    }

    public TemasCalidad getEntidadEnEdicion() {
        return entidadEnEdicion;
    }

    public void setEntidadEnEdicion(TemasCalidad entidadEnEdicion) {
        this.entidadEnEdicion = entidadEnEdicion;
    }

    public void setInicioMB(InicioMB inicioMB) {
        this.inicioMB = inicioMB;
    }

    @PostConstruct
    public void init() {
        inicioMB.cargarOrganismoSeleccionado();

        buscar();
    }

    /**
     * Action agregar.
     *
     * @return
     */
    public String agregar() {
        entidadEnEdicion = new TemasCalidad();

        renderPopupEdicion.abrir();
        return null;
    }

    /**
     * Action eliminar un TemasCalidad.
     *
     * @return
     */
    public String eliminar(Integer tcaPk) {
        if (tcaPk != null) {
            try {
                entidadDelegate.eliminar(tcaPk);
                for (TemasCalidad a : listaResultado) {
                    if (a.getTcaPk().equals(tcaPk)) {
                        listaResultado.remove(a);
                        break;
                    }
                }
                JSFUtils.agregarMsg(BUSQUEDA_MSG, "info_tca_eliminado", null);

            } catch (BusinessException e) {
                logger.log(Level.SEVERE, null, e);
                JSFUtils.agregarMsgs(BUSQUEDA_MSG, e.getErrores());
                inicioMB.setRenderPopupMensajes(Boolean.TRUE);
            }
        }
        return null;
    }

    /**
     * Action Buscar.
     *
     * @return
     */
    public String buscar() {
        Map<String, Object> mapFiltro = new HashMap<>();
        mapFiltro.put(ConstantesEntities.TCA_ATT_NOMBRE, filtroNombre);
        listaResultado = entidadDelegate.busquedaPorFiltro(inicioMB.getOrganismo().getOrgPk(), mapFiltro, elementoOrdenacion, ascendente);

        return null;
    }

    public String editar(Integer tcaPk) {
        try {
            entidadEnEdicion = entidadDelegate.obtenerPorId(tcaPk);
        } catch (BusinessException ex) {
            logger.log(Level.SEVERE, null, ex);
            JSFUtils.agregarMsgs(POPUP_MSG, ex.getErrores());
        }

        renderPopupEdicion.abrir();
        return null;
    }

    public String guardar() {
        entidadEnEdicion.setTcaOrgFk(inicioMB.getOrganismo());

        try {
            entidadEnEdicion = entidadDelegate.guardar(entidadEnEdicion);
            JSFUtils.agregarMsg(BUSQUEDA_MSG, "info_tca_agregado", null);

            if (entidadEnEdicion != null) {
                renderPopupEdicion.cerrar();
                buscar();
            }
        } catch (BusinessException be) {
            logger.log(Level.SEVERE, be.getMessage(), be);
            JSFUtils.agregarMsgs(POPUP_MSG, be.getErrores());
        }
        return null;
    }

    public void cancelar() {
        renderPopupEdicion.cerrar();
    }

    /**
     * Action limpiar formulario de busqueda.
     *
     * @return
     */
    public String limpiar() {
        filtroNombre = null;
        listaResultado = null;
        elementoOrdenacion = ConstantesEntities.TCA_ATT_NOMBRE;
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
}

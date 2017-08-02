package com.sofis.web.delegates;

import com.sofis.business.ejbs.ProyectosBean;
import com.sofis.entities.data.Configuracion;
import com.sofis.entities.data.Entregables;
import com.sofis.entities.data.Estados;
import com.sofis.entities.data.ProyReplanificacion;
import com.sofis.entities.data.Proyectos;
import com.sofis.entities.data.SsUsuario;
import com.sofis.entities.tipos.FichaTO;
import com.sofis.entities.tipos.FiltroExpVisuaTO;
import com.sofis.entities.tipos.IdNombreTO;
import com.sofis.entities.tipos.ProyectoTO;
import com.sofis.exceptions.GeneralException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

/**
 *
 * @author Usuario
 */
public class ProyectosDelegate {

    @Inject
    ProyectosBean proyectoBean;

    public Proyectos obtenerProyPorId(Integer id) throws GeneralException {
        return proyectoBean.obtenerProyPorId(id);
    }

    public Proyectos obtenerProyPorId(Integer id, Boolean loadProyPublicados) throws GeneralException {
        return proyectoBean.obtenerProyPorId(id, loadProyPublicados);
    }

    public void guardarIndicadoresSimple(Integer proyPk, boolean programas, boolean soloFaltantes, Integer orgPk, Map<String, Configuracion> confs, boolean procesarPrograma) {
        proyectoBean.guardarIndicadoresSimple(proyPk, programas, soloFaltantes, orgPk, confs, procesarPrograma);
    }

    public Proyectos guardarProyecto(FichaTO fichaTO, SsUsuario usuario, Integer orgPk) throws GeneralException {
        return proyectoBean.guardarProyecto(fichaTO, usuario, orgPk);
    }

    public Proyectos guardarProyectoAprobacion(FichaTO fichaTO, SsUsuario usuario, Integer orgPk) throws GeneralException {
        return proyectoBean.guardarProyectoAprobacion(fichaTO, usuario, orgPk);
    }

    public Proyectos guardarProyectoAprobacion(Proyectos proy, SsUsuario usuario, Integer orgPk) throws GeneralException {
        return proyectoBean.guardarProyectoAprobacion(proy, usuario, orgPk);
    }

    public Proyectos guardarProyectoRetrocederEstado(Integer proyPk, SsUsuario usuario, Integer orgPk, ProyReplanificacion replanificacion) throws GeneralException {
        return proyectoBean.guardarProyectoRetrocederEstado(proyPk, usuario, orgPk, replanificacion);
    }
    
    public Proyectos guardarProyectoRechazarCambioEstado(Integer proyPk, SsUsuario usuario, Integer orgPk, ProyReplanificacion replanificacion) throws GeneralException {
        return proyectoBean.guardarProyectoRechazarCambioEstado(proyPk, usuario, orgPk, replanificacion);
    }

    public Proyectos guardarCopiaProyecto(Integer proyPk, String nombre, Date fechaComienzoProyCopia, SsUsuario usu, Integer orgPk) {
        return proyectoBean.guardarCopiaProyecto(proyPk, nombre, fechaComienzoProyCopia, usu, orgPk);
    }

    public Proyectos obtenerProyPorIdEager(Integer proyPk) {
        return proyectoBean.obtenerProyPorIdEager(proyPk);
    }

    public Proyectos moverProyecto(Proyectos p, SsUsuario usaurio) throws GeneralException{
        return proyectoBean.moverProyecto(p, usaurio);
    }

    public Set<Proyectos> obtenerProyPorProgId(Integer fichaFk) {
        return proyectoBean.obtenerProyPorProgId(fichaFk);
    }
    
    public Set<Proyectos> obtenerProyPorProgId(Integer fichaFk, Boolean activos) {
        return proyectoBean.obtenerProyPorProgId(fichaFk, activos);
    }

    public Proyectos darBajaProyecto(Integer progPk, SsUsuario usuario, Integer orgPk) throws GeneralException {
        return proyectoBean.darBajaProyecto(progPk, usuario, orgPk);
    }

    public List<Proyectos> obtenerTodos() {
        return proyectoBean.obtenerTodos();
    }

    public String obtenerUltimaActualizacionColor(Estados estado, Date proyFechaAct, Integer proySemaforoAmarillo, Integer proySemaforoRojo) {
        return proyectoBean.obtenerUltimaActualizacionColor(estado, proyFechaAct, proySemaforoAmarillo, proySemaforoRojo);
    }

    public Double porcentajePesoTotalProyecto(Integer proyPk) {
        return proyectoBean.porcentajePesoTotalProyecto(proyPk);
    }

    public List<Integer> obtenerIdsProyPorOrg(Integer orgPk) throws GeneralException {
        return proyectoBean.obtenerIdsProyPorOrg(orgPk);
    }

    public List<Integer> obtenerIdsProyPorOrg(Integer orgPk, Boolean activos) throws GeneralException {
        return proyectoBean.obtenerIdsProyPorOrg(orgPk, activos);
    }

    public List<Proyectos> obtenerActivos(Integer orgPk) {
        return proyectoBean.obtenerActivosPorOrg(orgPk);
    }

    public List<Proyectos> obtenerProyComboPorOrg(Integer orgPk) {
        return proyectoBean.obtenerProyComboPorOrg(orgPk);
    }

    public void controlarEntregables(Integer proyPk, boolean resetLineaBase) {
        proyectoBean.controlarEntregables(proyPk, resetLineaBase);
    }

    public List<Entregables> recalcularEntregablesPadres(Set<Entregables> entSet) {
        return proyectoBean.recalcularEntregablesPadres(entSet);
    }

    public List<Proyectos> obtenerProyPorGerente(Integer usuPk, Integer orgPk) {
        return proyectoBean.obtenerProyPorGerente(usuPk, orgPk);
    }

    public Date obtenerPrimeraFecha() {
        return proyectoBean.obtenerPrimeraFecha();
    }

    public Date obtenerUltimaFecha() {
        return proyectoBean.obtenerUltimaFecha();
    }

    public Integer porcentajeAvanceEnTiempo(Integer proyPk) {
        return proyectoBean.porcentajeAvanceEnTiempo(proyPk);
    }

    public Integer porcentajeAvanceEnTiempoLBase(Set<Entregables> entregables) {
        return proyectoBean.porcentajeAvanceEnTiempoLBase(entregables);
    }

    public List<Proyectos> obtenerPorUsuarioParticipanteActivo(Integer usuId, Integer orgPk) {
        return proyectoBean.obtenerPorUsuarioParticipanteActivo(usuId, orgPk);
    }

    public void controlarProdAcumulados(Integer proyPk, Boolean actualizarPrograma) {
        proyectoBean.controlarProdAcumulados(proyPk, actualizarPrograma);
    }

    public List<FichaTO> buscarPorFiltro(FiltroExpVisuaTO filtro) {
        return proyectoBean.buscarPorFiltro(filtro);
    }

    public Proyectos exportarVisualizador(Integer fichaFk, SsUsuario usuario) {
        return proyectoBean.exportarVisualizador(fichaFk, usuario);
    }

    public List<IdNombreTO> obtenerProyIdNombre(Integer usuid, Integer orgPk) {
        return proyectoBean.obtenerProyIdNombre(usuid, orgPk);
    }

    public boolean esColaboradorProy(Integer usuId, Integer proyPk) {
        return proyectoBean.esColaboradorProy(usuId, proyPk);
    }

    public Integer obtenerOrgPkPorProyPk(Integer proyPk) {
        return proyectoBean.obtenerOrgPkPorProyPk(proyPk);
    }

    public ProyectoTO obtenerProyTO(Integer proyPk) {
        return proyectoBean.obtenerProyTO(proyPk);
    }

    public List<Proyectos> obtenerProyHermanos(Integer proyPk) {
        return proyectoBean.obtenerProyHermanos(proyPk);
    }
}

package com.sofis.data.daos;

import com.sofis.data.utils.DAOUtils;
import com.sofis.entities.codigueras.ConfiguracionCodigos;
import com.sofis.entities.codigueras.SsRolCodigos;
import com.sofis.entities.constantes.ConstanteApp;
import com.sofis.entities.data.Areas;
import com.sofis.entities.data.AreasTags;
import com.sofis.entities.data.Configuracion;
import com.sofis.entities.data.Estados;
import com.sofis.entities.data.Programas;
import com.sofis.entities.data.Proyectos;
import com.sofis.entities.data.SsUsuario;
import com.sofis.entities.enums.TipoFichaEnum;
import com.sofis.entities.tipos.FichaTO;
import com.sofis.entities.tipos.FiltroInicioTO;
import com.sofis.generico.utils.generalutils.CollectionsUtils;
import com.sofis.generico.utils.generalutils.StringsUtils;
import com.sofis.persistence.dao.exceptions.DAOGeneralException;
import com.sofis.persistence.dao.imp.hibernate.HibernateJpaDAOImp;
import com.sofis.persistence.dao.reference.EntityReference;
import com.sofis.sofisform.to.AND_TO;
import com.sofis.sofisform.to.CriteriaTO;
import com.sofis.sofisform.to.MatchCriteriaTO;
import com.sofis.sofisform.to.OR_TO;
import com.sofis.utils.CriteriaTOUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Usuario
 */
public class ProgramasProyectosDAO extends HibernateJpaDAOImp<Proyectos, Integer> implements Serializable {

    private static final Logger logger = Logger.getLogger(ConstanteApp.LOGGER_NAME);
    private static final long serialVersionUID = 1L;

    public ProgramasProyectosDAO(EntityManager em) {
	super(em);
    }

    /**
     * Retorna una lista con programas y proyectos que cumplan con los
     * parametros aportados.
     *
     * @param estadoId
     * @param organismoId
     * @return Lista de FichaTO
     * @throws DAOGeneralException
     */
    public List<FichaTO> obtenerProyProgPendientes(SsUsuario usuario, Integer orgPk) throws DAOGeneralException {
	String queryStr = "SELECT NEW com.sofis.entities.tipos.FichaTO(p)"
		+ " FROM ProgramasProyectos p"
		+ " WHERE (p.estado = :pendientePMOT"
		+ " OR (p.estadoPendiente = :cancelarPMOT)"
		+ " OR (p.estado = :pendientePMOF AND p.pmoFederada = :pmoFederada))"
		+ " AND p.organismo = :organismoId"
		+ " AND (p.activo is null OR p.activo = :activo)"
		+ " ORDER BY p.nombre";
	Query query = super.getEm().createQuery(queryStr);
	query.setParameter("pendientePMOT", usuario.isRol(SsRolCodigos.PMO_TRANSVERSAL, orgPk) ? Estados.ESTADOS.PENDIENTE_PMOT.estado_id : 0);
	query.setParameter("cancelarPMOT", usuario.isRol(SsRolCodigos.PMO_TRANSVERSAL, orgPk) ? Estados.ESTADOS.SOLICITUD_CANCELAR_PMOT.estado_id : 0);
	query.setParameter("pendientePMOF", usuario.isRol(SsRolCodigos.PMO_FEDERADA, orgPk) || usuario.isRol(SsRolCodigos.PMO_TRANSVERSAL, orgPk) ? Estados.ESTADOS.PENDIENTE_PMOF.estado_id : 0);
	query.setParameter("pmoFederada", usuario.getUsuId());
	query.setParameter("organismoId", orgPk);
	query.setParameter("activo", true);

	List<FichaTO> resultado = query.getResultList();
//        logger.log(Level.INFO, "ProyProg pendientes: {0}", resultado.size());
	return resultado;
    }

    /**
     * A partir del filtro crea los criterias para realizar la consulta sobre el
     * Proyecto.
     *
     * @param usuario el usuario que ejecuta la consulta
     * @param filtro el filtro
     * @return
     */
    private CriteriaTO crearFiltroProyecto(SsUsuario usuario, FiltroInicioTO filtro, boolean huerfanos) {
	List<CriteriaTO> criterias = new ArrayList<>();
	boolean soloProyecto = huerfanos || filtro.getProgPk() != null;

	// ProgramaPk
	if (filtro.getProgPk() != null) {
	    MatchCriteriaTO prog1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.progPk", filtro.getProgPk());
	    criterias.add(prog1);
	}

	List<Object> estados = null;

	if (huerfanos || (filtro.getNivel() != null && (filtro.getNivel().equals(2)
		|| !(filtro.getProgPk() != null && filtro.getNivel().equals(1))))) {

	    if (filtro.getAreasOrganizacion() != null && filtro.getNivel().equals(2)) {
		Areas area = filtro.getAreasOrganizacion();
		MatchCriteriaTO proyArea = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyAreaFk.areaPk", area.getAreaPk());
		criterias.add(proyArea);
	    }

	    // Nombre
	    if (!StringsUtils.isEmpty(filtro.getNombre())) {
//                MatchCriteriaTO nombre1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "proyNombre", filtro.getNombre());
		CriteriaTO nombre1 = DAOUtils.createMatchCriteriaTOString("proyNombre", filtro.getNombre());
		if (soloProyecto) {
		    criterias.add(nombre1);
		} else {
//                    MatchCriteriaTO nombre2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "proyProgFk.progNombre", filtro.getNombre());
		    CriteriaTO nombre2 = DAOUtils.createMatchCriteriaTOString("proyProgFk.progNombre", filtro.getNombre());
		    OR_TO orCriteria = CriteriaTOUtils.createORTO(nombre1, nombre2);
		    criterias.add(orCriteria);
		}
	    }

	    // Sponsor
	    if (filtro.getSponsor() != null && !filtro.getSponsor().equals(-1)) {
		MatchCriteriaTO nombre1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyUsrSponsorFk.usuId", filtro.getSponsor());
		if (soloProyecto) {
		    criterias.add(nombre1);
		} else {
		    MatchCriteriaTO nombre2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.progUsrSponsorFk.usuId", filtro.getSponsor());
		    OR_TO orCriteria = new OR_TO();
		    orCriteria.setCriteria1(nombre1);
		    orCriteria.setCriteria2(nombre2);
		    criterias.add(orCriteria);
		}
	    }

	    // Gerente o Adjunto
	    if (filtro.getGerenteOAdjunto() != null && !filtro.getGerenteOAdjunto().equals(-1)) {
		MatchCriteriaTO gerente1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyUsrGerenteFk.usuId", filtro.getGerenteOAdjunto());
		MatchCriteriaTO adjunto1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyUsrAdjuntoFk.usuId", filtro.getGerenteOAdjunto());
		if (soloProyecto) {
		    OR_TO orCriteria1 = new OR_TO();
		    orCriteria1.setCriteria1(gerente1);
		    orCriteria1.setCriteria2(adjunto1);
		    criterias.add(orCriteria1);
		} else {
		    OR_TO orCriteria1 = new OR_TO();
		    orCriteria1.setCriteria1(gerente1);
		    orCriteria1.setCriteria2(adjunto1);

		    MatchCriteriaTO gerente2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.progUsrGerenteFk.usuId", filtro.getGerenteOAdjunto());
		    MatchCriteriaTO adjunto2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.progUsrAdjuntoFk.usuId", filtro.getGerenteOAdjunto());
		    OR_TO orCriteria2 = new OR_TO();
		    orCriteria2.setCriteria1(gerente2);
		    orCriteria2.setCriteria2(adjunto2);

		    OR_TO orCriteria3 = new OR_TO();
		    orCriteria3.setCriteria1(orCriteria1);
		    orCriteria3.setCriteria2(orCriteria2);
		    criterias.add(orCriteria3);
		}
	    }

	    // PMO Federada
	    if (filtro.getPmoFederada() != null && !filtro.getPmoFederada().equals(-1)) {
		MatchCriteriaTO nombre1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyUsrPmofedFk.usuId", filtro.getPmoFederada());
		if (soloProyecto) {
		    criterias.add(nombre1);
		} else {
		    MatchCriteriaTO nombre2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.progUsrPmofedFk.usuId", filtro.getPmoFederada());
		    OR_TO orCriteria = new OR_TO();
		    orCriteria.setCriteria1(nombre1);
		    orCriteria.setCriteria2(nombre2);
		    criterias.add(orCriteria);
		}
	    }

	    // Anio Desde
	    if (filtro.getFechaDesde() != null) {
		MatchCriteriaTO anioDesde1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "proyIndices.proyindPeriodoInicio", filtro.getFechaDesde());
		if (soloProyecto) {
		    criterias.add(anioDesde1);
		} else {
		    MatchCriteriaTO anioDesde2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "proyProgFk.progIndices.progindPeriodoInicio", filtro.getFechaHasta());
		    OR_TO orCriteria = new OR_TO();
		    orCriteria.setCriteria1(anioDesde1);
		    orCriteria.setCriteria2(anioDesde2);
		    criterias.add(orCriteria);
		}
	    }

	    // Anio Hasta
	    if (filtro.getFechaHasta() != null) {
		MatchCriteriaTO anioHasta1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "proyIndices.proyindPeriodoFin", filtro.getFechaHasta());
		if (soloProyecto) {
		    criterias.add(anioHasta1);
		} else {
		    MatchCriteriaTO anioHasta2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "proyProgFk.progIndices.progindPeriodoFin", filtro.getFechaHasta());
		    OR_TO orCriteria = new OR_TO();
		    orCriteria.setCriteria1(anioHasta1);
		    orCriteria.setCriteria2(anioHasta2);
		    criterias.add(orCriteria);
		}
	    }

	    //Area tematica
	    if (filtro.getAreasTematicas() != null && !filtro.getAreasTematicas().isEmpty()) {
		List<CriteriaTO> areaTemCriteria = new ArrayList<>();
		for (AreasTags areaTem : filtro.getAreasTematicas()) {
		    MatchCriteriaTO areaTag1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "areasTematicasSet.arastagPk", areaTem.getArastagPk());
		    if (soloProyecto) {
			areaTemCriteria.add(areaTag1);
		    } else {
			MatchCriteriaTO areaTag2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.areasTematicasSet.arastagPk", areaTem.getArastagPk());
			OR_TO orCriteria = new OR_TO();
			orCriteria.setCriteria1(areaTag1);
			orCriteria.setCriteria2(areaTag2);
			areaTemCriteria.add(orCriteria);
		    }
		}

		if (areaTemCriteria.size() > 1) {
		    criterias.add(CriteriaTOUtils.createORTO(areaTemCriteria.toArray(new CriteriaTO[]{})));
		} else {
		    criterias.add(areaTemCriteria.get(0));
		}
	    }

	    // Interesado ambito
	    if (filtro.getInteresadoAmbitoOrganizacion() != null) {
//		MatchCriteriaTO ambito1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "interesadosList.intOrgaFk.orgaAmbFk.amb_pk", filtro.getInteresadoAmbitoOrganizacion().getAmbPk());
                MatchCriteriaTO ambito1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "interesadosList.intOrgaFk.orgaAmbFk.ambPk", filtro.getInteresadoAmbitoOrganizacion().getAmbPk());
		if (soloProyecto) {
		    criterias.add(ambito1);
		} else {
//		    MatchCriteriaTO ambito2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "proyProgFk.interesadosList.intOrgaFk.orgaAmbFk.amb_pk", filtro.getInteresadoAmbitoOrganizacion().getAmbPk());
                    MatchCriteriaTO ambito2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.interesadosList.intOrgaFk.orgaAmbFk.ambPk", filtro.getInteresadoAmbitoOrganizacion().getAmbPk());
		    OR_TO orCriteria = new OR_TO();
		    orCriteria.setCriteria1(ambito1);
		    orCriteria.setCriteria2(ambito2);
		    criterias.add(orCriteria);
		}
	    }

	    // Interesado Nombre
	    if (!StringsUtils.isEmpty(filtro.getInteresadoNombre())) {
//                MatchCriteriaTO nombre1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "interesadosList.intPersonaFk.persNombre", filtro.getInteresadoNombre());
		CriteriaTO nombre1 = DAOUtils.createMatchCriteriaTOString("interesadosList.intPersonaFk.persNombre", filtro.getInteresadoNombre());
		if (soloProyecto) {
		    criterias.add(nombre1);
		} else {
//                    MatchCriteriaTO nombre2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "proyProgFk.interesadosList.intPersonaFk.persNombre", filtro.getInteresadoNombre());
		    CriteriaTO nombre2 = DAOUtils.createMatchCriteriaTOString("proyProgFk.interesadosList.intPersonaFk.persNombre", filtro.getInteresadoNombre());
		    OR_TO orCriteria = new OR_TO();
		    orCriteria.setCriteria1(nombre1);
		    orCriteria.setCriteria2(nombre2);
		    criterias.add(orCriteria);
		}
	    }

	    // Interesado Organizacion
	    if (filtro.getInteresadoOrganizacion() != null) {
		MatchCriteriaTO nombre1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "interesadosList.intOrgaFk.orgaPk", filtro.getInteresadoOrganizacion().getOrgaPk());
		if (soloProyecto) {
		    criterias.add(nombre1);
		} else {
		    MatchCriteriaTO nombre2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.interesadosList.intOrgaFk.orgaPk", filtro.getInteresadoOrganizacion().getOrgaPk());
		    OR_TO orCriteria = new OR_TO();
		    orCriteria.setCriteria1(nombre1);
		    orCriteria.setCriteria2(nombre2);
		    criterias.add(orCriteria);
		}
	    }

	    //Interesado Rol
	    if (filtro.getInteresadoRol() != null && !filtro.getInteresadoRol().equals(-1)) {
		MatchCriteriaTO nombre1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "interesadosList.intRolintFk.rolintPk", filtro.getInteresadoRol());
		if (soloProyecto) {
		    criterias.add(nombre1);
		} else {
		    MatchCriteriaTO nombre2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.interesadosList.intRolintFk.rolintPk", filtro.getInteresadoRol());
		    OR_TO orCriteria = new OR_TO();
		    orCriteria.setCriteria1(nombre1);
		    orCriteria.setCriteria2(nombre2);
		    criterias.add(orCriteria);
		}
	    }

	    //Presupuesto Proveedor
	    if (filtro.getOrgaProveedor() != null && !filtro.getOrgaProveedor().equals(-1)) {
		MatchCriteriaTO orgProv1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyPreFk.adquisicionSet.adqProvOrga.orgaPk", filtro.getOrgaProveedor().getOrgaPk());
		if (soloProyecto) {
		    criterias.add(orgProv1);
		} else {
		    MatchCriteriaTO orgProv2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.progPreFk.adquisicionSet.adqProvOrga.orgaPk", filtro.getOrgaProveedor().getOrgaPk());
		    OR_TO orCriteria = new OR_TO();
		    orCriteria.setCriteria1(orgProv1);
		    orCriteria.setCriteria2(orgProv2);
		    criterias.add(orCriteria);
		}
	    }

	    //Presupuesto Fuente
	    if (filtro.getFuenteFinanciamiento() != null && !filtro.getFuenteFinanciamiento().equals(-1)) {
		MatchCriteriaTO fuente1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyPreFk.fuenteFinanciamiento.fuePk", filtro.getFuenteFinanciamiento().getFuePk());
		MatchCriteriaTO fuente2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyPreFk.adquisicionSet.adqFuente.fuePk", filtro.getFuenteFinanciamiento().getFuePk());
		CriteriaTO fuenteA = CriteriaTOUtils.createORTO(fuente1, fuente2);
		if (soloProyecto) {
		    criterias.add(fuenteA);
		} else {
		    MatchCriteriaTO fuente3 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.progPreFk.fuenteFinanciamiento.fuePk", filtro.getFuenteFinanciamiento().getFuePk());
		    MatchCriteriaTO fuente4 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.progPreFk.adquisicionSet.adqFuente.fuePk", filtro.getFuenteFinanciamiento().getFuePk());
		    CriteriaTO fuenteB = CriteriaTOUtils.createORTO(fuente3, fuente4);
		    OR_TO orCriteria = new OR_TO();
		    orCriteria.setCriteria1(fuenteA);
		    orCriteria.setCriteria2(fuenteB);
		    criterias.add(orCriteria);
		}
	    }

	    // Riesgos Altos
	    if (filtro.getCantidadRiesgosAltos() != null && filtro.getCantidadRiesgosAltos() > 0) {
		MatchCriteriaTO riesgoAlto1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "proyIndices.proyindRiesgoAlto", filtro.getCantidadRiesgosAltos());
		if (soloProyecto) {
		    criterias.add(riesgoAlto1);
		} else {
		    MatchCriteriaTO riesgoAlto2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "proyProgFk.progIndices.progindRiesgoAlto", filtro.getCantidadRiesgosAltos());
		    OR_TO orCriteria = new OR_TO();
		    orCriteria.setCriteria1(riesgoAlto1);
		    orCriteria.setCriteria2(riesgoAlto2);
		    criterias.add(orCriteria);
		}
	    }

	    // Riegos Exposicion(por colores)
	    if (CollectionsUtils.isNotEmpty(filtro.getGradoRiesgo())) {
		List<CriteriaTO> listCriteriaTo = new ArrayList<>();
		for (Object grado : filtro.getGradoRiesgo()) {
		    String g = (String) grado;
		    Configuracion confAmarillo = filtro.getConfiguracion().get("RIESGO_INDICE_LIMITE_AMARILLO");
		    Configuracion confRojo = filtro.getConfiguracion().get("RIESGO_INDICE_LIMITE_ROJO");

		    if (!g.isEmpty() && confAmarillo != null && confRojo != null) {
			switch (g) {
			    case "1":
				MatchCriteriaTO riesgoBajo = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "proyIndices.proyindRiesgoExpo", Double.parseDouble(confAmarillo.getCnfValor()));
				MatchCriteriaTO sinRiesgo1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NULL, "proyIndices.proyindRiesgoExpo", 1);
				CriteriaTO criteriaBajo = CriteriaTOUtils.createORTO(riesgoBajo, sinRiesgo1);

				if (soloProyecto) {
				    listCriteriaTo.add(criteriaBajo);
				} else {
				    MatchCriteriaTO riesgoBajo2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "proyProgFk.progIndices.progindRiesgoExpo", Double.parseDouble(confAmarillo.getCnfValor()));
				    MatchCriteriaTO sinRiesgoBajo2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NULL, "proyProgFk.progIndices.progindRiesgoExpo", 1);
				    CriteriaTO criteriaBajo2 = CriteriaTOUtils.createORTO(riesgoBajo2, sinRiesgoBajo2);
				    OR_TO orCriteria = new OR_TO(criteriaBajo, criteriaBajo2);
				    listCriteriaTo.add(orCriteria);
				}
				break;

			    case "2":
				MatchCriteriaTO riesgoMedio1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATER, "proyIndices.proyindRiesgoExpo", Double.parseDouble(confAmarillo.getCnfValor()));
				MatchCriteriaTO riesgoMedio2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESS, "proyIndices.proyindRiesgoExpo", Double.parseDouble(confRojo.getCnfValor()));
				AND_TO andCriteria1 = new AND_TO(riesgoMedio1, riesgoMedio2);
				if (soloProyecto) {
				    listCriteriaTo.add(andCriteria1);
				} else {
				    MatchCriteriaTO riesgoMedio3 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATER, "proyProgFk.progIndices.progindRiesgoExpo", Double.parseDouble(confAmarillo.getCnfValor()));
				    MatchCriteriaTO riesgoMedio4 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESS, "proyProgFk.progIndices.progindRiesgoExpo", Double.parseDouble(confRojo.getCnfValor()));
				    AND_TO andCriteria2 = new AND_TO(riesgoMedio3, riesgoMedio4);
				    OR_TO orCriteria = new OR_TO(andCriteria1, andCriteria2);
				    listCriteriaTo.add(orCriteria);
				}
				break;

			    case "3":
				MatchCriteriaTO riesgoAlto1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "proyIndices.proyindRiesgoExpo", Double.parseDouble(confRojo.getCnfValor()));
				if (soloProyecto) {
				    listCriteriaTo.add(riesgoAlto1);
				} else {
				    MatchCriteriaTO riesgoAlto2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "proyProgFk.progIndices.progindRiesgoExpo", Double.parseDouble(confRojo.getCnfValor()));
				    OR_TO orCriteria = new OR_TO(riesgoAlto1, riesgoAlto2);
				    listCriteriaTo.add(orCriteria);
				}
				break;
			}
		    }
		}
		if (!listCriteriaTo.isEmpty()) {
		    if (listCriteriaTo.size() > 1) {
			CriteriaTO[] arrCriteriaTo = listCriteriaTo.toArray(new CriteriaTO[listCriteriaTo.size()]);
			criterias.add(CriteriaTOUtils.createORTO(arrCriteriaTo));
		    } else {
			criterias.add(listCriteriaTo.get(0));
		    }
		}
	    }

	    // Calidad
	    if (filtro.getCalidadIndice() != null && filtro.getCalidadIndice() > 0) {
		Configuracion confAmarillo = filtro.getConfiguracion().get(ConfiguracionCodigos.CALIDAD_LIMITE_AMARILLO);
		Configuracion confRojo = filtro.getConfiguracion().get(ConfiguracionCodigos.CALIDAD_LIMITE_ROJO);

		if (confAmarillo != null && confRojo != null) {
		    switch (filtro.getCalidadIndice()) {
			case 1:
			    MatchCriteriaTO calidadAlta = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATER, "proyIndices.proyindCalInd", Double.parseDouble(confAmarillo.getCnfValor()));
			    if (soloProyecto) {
				criterias.add(calidadAlta);
			    } else {
				MatchCriteriaTO calidadAltaProg = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATER, "proyProgFk.progIndices.progindCalInd", Double.parseDouble(confAmarillo.getCnfValor()));
				criterias.add(CriteriaTOUtils.createORTO(calidadAlta, calidadAltaProg));
			    }
			    break;

			case 2:
			    MatchCriteriaTO calidadMedia1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATER, "proyIndices.proyindCalInd", Double.parseDouble(confRojo.getCnfValor()));
			    MatchCriteriaTO calidadMedia2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "proyIndices.proyindCalInd", Double.parseDouble(confAmarillo.getCnfValor()));
			    AND_TO orProyMedia = CriteriaTOUtils.createANDTO(calidadMedia1, calidadMedia2);
			    if (soloProyecto) {
				criterias.add(orProyMedia);
			    } else {
				MatchCriteriaTO calidadMediaProg1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATER, "proyProgFk.progIndices.progindCalInd", Double.parseDouble(confRojo.getCnfValor()));
				MatchCriteriaTO calidadMediaProg2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "proyProgFk.progIndices.progindCalInd", Double.parseDouble(confAmarillo.getCnfValor()));
				OR_TO orProgMedia = CriteriaTOUtils.createORTO(calidadMediaProg1, calidadMediaProg2);
				criterias.add(CriteriaTOUtils.createORTO(orProyMedia, orProgMedia));
			    }
			    break;

			case 3:
			    MatchCriteriaTO calidadBajaProy = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "proyIndices.proyindCalInd", Double.parseDouble(confRojo.getCnfValor()));
			    if (soloProyecto) {
				criterias.add(calidadBajaProy);
			    } else {
				MatchCriteriaTO calidadBajaProg = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "proyProgFk.progIndices.progindCalInd", Double.parseDouble(confRojo.getCnfValor()));
				OR_TO orProgBaja = CriteriaTOUtils.createORTO(calidadBajaProy, calidadBajaProg);
				criterias.add(orProgBaja);
			    }
			    break;
		    }
		}
	    }

	    // Estados Filtro
	    if (filtro.getEstados()
		    != null && !filtro.getEstados().isEmpty()) {
		estados = filtro.getEstados();
	    }
	}

	if (estados == null || estados.isEmpty()) {
	    estados = new ArrayList<>();
	    estados.add(Estados.ESTADOS.INICIO.estado_id);
	    estados.add(Estados.ESTADOS.PLANIFICACION.estado_id);
	    estados.add(Estados.ESTADOS.EJECUCION.estado_id);
	    estados.add(Estados.ESTADOS.FINALIZADO.estado_id);
	}

	// Estados
	if (estados
		!= null) {
	    List<CriteriaTO> estadosCriteria = new ArrayList<>();
	    for (Object estadoId : estados) {
		Integer estadoIdInt = Integer.parseInt(estadoId + "");
		MatchCriteriaTO estado1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyEstFk.estPk", estadoIdInt);
		if (soloProyecto) {
		    estadosCriteria.add(estado1);
		} else {
		    MatchCriteriaTO estado2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.progEstFk.estPk", estadoIdInt);
		    OR_TO orCriteria = new OR_TO();
		    orCriteria.setCriteria1(estado1);
		    orCriteria.setCriteria2(estado2);
		    estadosCriteria.add(orCriteria);
		}
	    }

	    if (estadosCriteria.size() == 1) {
		criterias.add(estadosCriteria.get(0));
	    } else if (estadosCriteria.size() > 1) {
		criterias.add(CriteriaTOUtils.createORTO(estadosCriteria.toArray(new CriteriaTO[]{})));
	    }
	}

	// Activo
	if (filtro.getActivo()
		!= null) {
	    MatchCriteriaTO activo1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "activo", filtro.getActivo());
	    if (soloProyecto) {
		criterias.add(activo1);
	    } else {
		MatchCriteriaTO activo2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "activo", filtro.getActivo());
		OR_TO orCriteria = new OR_TO();
		orCriteria.setCriteria1(activo1);
		orCriteria.setCriteria2(activo2);
		criterias.add(orCriteria);
	    }
	}

	if (criterias.size() == 1) {
	    return criterias.get(0);
	}

	if (criterias.size() > 1) {
	    AND_TO criteria = CriteriaTOUtils.createANDTO(criterias.toArray(new CriteriaTO[]{}));
	    return criteria;
	}

	return null;
    }

    /**
     * A partir del filtro crea los criterias para realizar la consulta sobre el
     * Programa.
     *
     * @param usuario
     * @param filtro
     * @return
     */
    private CriteriaTO crearFiltroPrograma(SsUsuario usuario, FiltroInicioTO filtro, boolean incProy) {

	List<CriteriaTO> criterias = new ArrayList<>();
	// Nombre
	if (!StringsUtils.isEmpty(filtro.getNombre())) {
//	    MatchCriteriaTO crit1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "progNombre", filtro.getNombre());
	    CriteriaTO crit1 = DAOUtils.createMatchCriteriaTOString("progNombre", filtro.getNombre());
	    if (!incProy) {
		criterias.add(crit1);
	    } else {
//		MatchCriteriaTO crit2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "proyectosSet.proyNombre", filtro.getNombre());
		CriteriaTO crit2 = DAOUtils.createMatchCriteriaTOString("proyectosSet.proyNombre", filtro.getNombre());
		CriteriaTO nombre = CriteriaTOUtils.createORTO(crit1, crit2);
		criterias.add(nombre);
	    }
	}

	// Sponsor
	if (filtro.getSponsor() != null && !filtro.getSponsor().equals(-1)) {
	    MatchCriteriaTO crit1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "progUsrSponsorFk.usuId", filtro.getSponsor());
	    if (!incProy) {
		criterias.add(crit1);
	    } else {
		MatchCriteriaTO crit2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyectosSet.proyUsrSponsorFk.usuId", filtro.getSponsor());
		CriteriaTO nombre = CriteriaTOUtils.createORTO(crit1, crit2);
		criterias.add(nombre);
	    }
	}

	// Gerente o Adjunto
	if (filtro.getGerenteOAdjunto() != null && !filtro.getGerenteOAdjunto().equals(-1)) {
	    MatchCriteriaTO gerente1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "progUsrGerenteFk.usuId", filtro.getGerenteOAdjunto());
	    MatchCriteriaTO adjunto1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "progUsrAdjuntoFk.usuId", filtro.getGerenteOAdjunto());
	    OR_TO orCriteria = CriteriaTOUtils.createORTO(gerente1, adjunto1);
	    if (!incProy) {
		criterias.add(orCriteria);
	    } else {
		MatchCriteriaTO gerente2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyectosSet.proyUsrGerenteFk.usuId", filtro.getGerenteOAdjunto());
		MatchCriteriaTO adjunto2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyectosSet.proyUsrAdjuntoFk.usuId", filtro.getGerenteOAdjunto());
		OR_TO orCriteria2 = CriteriaTOUtils.createORTO(gerente2, adjunto2);
		criterias.add(CriteriaTOUtils.createORTO(orCriteria, orCriteria2));
	    }
	}

	// PMO Federada
	if (filtro.getPmoFederada() != null && !filtro.getPmoFederada().equals(-1)) {
	    MatchCriteriaTO crit1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "progUsrPmofedFk.usuId", filtro.getPmoFederada());
	    if (!incProy) {
		criterias.add(crit1);
	    } else {
		MatchCriteriaTO crit2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyectosSet.proyUsrPmofedFk.usuId", filtro.getPmoFederada());
		criterias.add(CriteriaTOUtils.createORTO(crit1, crit2));
	    }
	}

	// Anio Desde
	if (filtro.getFechaDesde() != null) {
	    MatchCriteriaTO anioDesde1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "progIndices.progindPeriodoInicio", filtro.getFechaDesde());
	    if (!incProy) {
		criterias.add(anioDesde1);
	    } else {
		MatchCriteriaTO crit2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "proyectosSet.proyIndices.proyindPeriodoInicio", filtro.getFechaDesde());
		criterias.add(CriteriaTOUtils.createORTO(anioDesde1, crit2));
	    }
	}

	// Anio Hasta
	if (filtro.getFechaHasta() != null) {
	    MatchCriteriaTO anioHasta1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "progIndices.progindPeriodoFin", filtro.getFechaHasta());
	    if (!incProy) {
		criterias.add(anioHasta1);
	    } else {
		MatchCriteriaTO anioHasta2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "proyectosSet.proyIndices.proyindPeriodoFin", filtro.getFechaHasta());
		criterias.add(CriteriaTOUtils.createORTO(anioHasta1, anioHasta2));
	    }
	}

	//Area tematica
	if (filtro.getAreasTematicas() != null && !filtro.getAreasTematicas().isEmpty()) {
	    List<CriteriaTO> areaTemCriteria = new ArrayList<>();
	    for (AreasTags areaTem : filtro.getAreasTematicas()) {
		MatchCriteriaTO areaTag1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "areasTematicasSet.arastagPk", areaTem.getArastagPk());
		if (!incProy) {
		    areaTemCriteria.add(areaTag1);
		} else {
		    MatchCriteriaTO areaTag2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyectosSet.areasTematicasSet.arastagPk", areaTem.getArastagPk());
		    areaTemCriteria.add(CriteriaTOUtils.createORTO(areaTag1, areaTag2));
		}
	    }

	    if (areaTemCriteria.size() > 1) {
		criterias.add(CriteriaTOUtils.createORTO(areaTemCriteria.toArray(new CriteriaTO[]{})));
	    } else {
		criterias.add(areaTemCriteria.get(0));
	    }
	}

	// Interesado ambito
	if (filtro.getInteresadoAmbitoOrganizacion() != null) {
//	    MatchCriteriaTO ambito1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "interesadosList.intOrgaFk.orgaAmbFk.amb_pk", filtro.getInteresadoAmbitoOrganizacion().getAmbPk());
            MatchCriteriaTO ambito1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "interesadosList.intOrgaFk.orgaAmbFk.ambPk", filtro.getInteresadoAmbitoOrganizacion().getAmbPk());
	    if (!incProy) {
		criterias.add(ambito1);
	    } else {
//		MatchCriteriaTO ambito2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "proyectosSet.interesadosList.intOrgaFk.orgaAmbFk.amb_pk", filtro.getInteresadoAmbitoOrganizacion().getAmbPk());
                MatchCriteriaTO ambito2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyectosSet.interesadosList.intOrgaFk.orgaAmbFk.ambPk", filtro.getInteresadoAmbitoOrganizacion().getAmbPk());
		criterias.add(CriteriaTOUtils.createORTO(ambito1, ambito2));
	    }
	}

	// Interesado Nombre
	if (!StringsUtils.isEmpty(filtro.getInteresadoNombre())) {
//	    MatchCriteriaTO nombre1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "interesadosList.intPersonaFk.persNombre", filtro.getInteresadoNombre());
	    CriteriaTO nombre1 = DAOUtils.createMatchCriteriaTOString("interesadosList.intPersonaFk.persNombre", filtro.getInteresadoNombre());
	    if (!incProy) {
		criterias.add(nombre1);
	    } else {
//		MatchCriteriaTO nombre2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "proyectosSet.interesadosList.intPersonaFk.persNombre", filtro.getInteresadoNombre());
		CriteriaTO nombre2 = DAOUtils.createMatchCriteriaTOString("proyectosSet.interesadosList.intPersonaFk.persNombre", filtro.getInteresadoNombre());
		criterias.add(CriteriaTOUtils.createORTO(nombre1, nombre2));
	    }
	}

	// Interesado Organizacion
	if (filtro.getInteresadoOrganizacion() != null) {
	    MatchCriteriaTO nombre1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "interesadosList.intOrgaFk.orgaPk", filtro.getInteresadoOrganizacion().getOrgaPk());
	    criterias.add(nombre1);
	    if (!incProy) {
		criterias.add(nombre1);
	    } else {
		MatchCriteriaTO nombre2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyectosSet.interesadosList.intOrgaFk.orgaPk", filtro.getInteresadoOrganizacion().getOrgaPk());
		criterias.add(CriteriaTOUtils.createORTO(nombre1, nombre2));
	    }
	}

	//Interesado Rol
	if (filtro.getInteresadoRol() != null && !filtro.getInteresadoRol().equals(-1)) {
	    MatchCriteriaTO nombre1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "interesadosList.intRolintFk.rolintPk", filtro.getInteresadoRol());
	    if (!incProy) {
		criterias.add(nombre1);
	    } else {
		MatchCriteriaTO nombre2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyectosSet.interesadosList.intRolintFk.rolintPk", filtro.getInteresadoRol());
		criterias.add(CriteriaTOUtils.createORTO(nombre1, nombre2));
	    }
	}

	//Presupuesto Proveedor
	if (filtro.getOrgaProveedor() != null) {
	    MatchCriteriaTO orgProv1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "progPreFk.adquisicionSet.adqProvOrga.orgaPk", filtro.getOrgaProveedor().getOrgaPk());
	    if (!incProy) {
		criterias.add(orgProv1);
	    } else {
		MatchCriteriaTO orgProv2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyectosSet.proyPreFk.adquisicionSet.adqProvOrga.orgaPk", filtro.getOrgaProveedor().getOrgaPk());
		criterias.add(CriteriaTOUtils.createORTO(orgProv1, orgProv2));
	    }
	}

	//Presupuesto Fuente
	if (filtro.getFuenteFinanciamiento() != null) {
	    MatchCriteriaTO fuente1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "progPreFk.fuenteFinanciamiento.fuePk", filtro.getFuenteFinanciamiento().getFuePk());
	    MatchCriteriaTO fuente2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "progPreFk.adquisicionSet.adqFuente.fuePk", filtro.getFuenteFinanciamiento().getFuePk());
	    CriteriaTO fuenteA = CriteriaTOUtils.createORTO(fuente1, fuente2);
	    if (!incProy) {
		criterias.add(fuenteA);
	    } else {
		MatchCriteriaTO fuente3 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyectosSet.proyPreFk.fuenteFinanciamiento.fuePk", filtro.getFuenteFinanciamiento().getFuePk());
		MatchCriteriaTO fuente4 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyectosSet.proyPreFk.adquisicionSet.adqFuente.fuePk", filtro.getFuenteFinanciamiento().getFuePk());
		CriteriaTO fuenteB = CriteriaTOUtils.createORTO(fuente3, fuente4);
		criterias.add(CriteriaTOUtils.createORTO(fuenteA, fuenteB));
	    }
	}

	// Riesgos Altos
	if (filtro.getCantidadRiesgosAltos() != null && filtro.getCantidadRiesgosAltos() > 0) {
	    MatchCriteriaTO riesgoAlto1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "progIndices.progindRiesgoAlto", filtro.getCantidadRiesgosAltos());
	    criterias.add(riesgoAlto1);
	    if (!incProy) {
		criterias.add(riesgoAlto1);
	    } else {
		MatchCriteriaTO riesgoAlto2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "proyectosSet.proyIndices.proyindRiesgoAlto", filtro.getCantidadRiesgosAltos());
		criterias.add(CriteriaTOUtils.createORTO(riesgoAlto1, riesgoAlto2));
	    }
	}

	// Riegos Exposicion(por colores)
	if (CollectionsUtils.isNotEmpty(filtro.getGradoRiesgo())) {
	    List<CriteriaTO> listCriteriaTo = new ArrayList<>();

	    for (Object grado : filtro.getGradoRiesgo()) {
		String g = (String) grado;
		Configuracion confAmarillo = filtro.getConfiguracion().get("RIESGO_INDICE_LIMITE_AMARILLO");
		Configuracion confRojo = filtro.getConfiguracion().get("RIESGO_INDICE_LIMITE_ROJO");

		if (!g.isEmpty() && confAmarillo != null && confRojo != null) {
		    switch (g) {
			case "1":
			    MatchCriteriaTO riesgoBajo = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "progIndices.progindRiesgoExpo", Double.parseDouble(confAmarillo.getCnfValor()));
			    MatchCriteriaTO sinRiesgo1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NULL, "progIndices.progindRiesgoExpo", 1);
			    MatchCriteriaTO sinRiesgo2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "progIndices.progindRiesgoExpo", 0d);
			    if (!incProy) {
				listCriteriaTo.add(CriteriaTOUtils.createORTO(riesgoBajo, sinRiesgo1, sinRiesgo2));
			    } else {
				MatchCriteriaTO riesgoBajoB = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "proyectosSet.proyIndices.progindRiesgoExpo", Double.parseDouble(confAmarillo.getCnfValor()));
				MatchCriteriaTO sinRiesgo1B = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NULL, "proyectosSet.proyIndices.progindRiesgoExpo", 1);
				MatchCriteriaTO sinRiesgo2B = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyectosSet.proyIndices.progindRiesgoExpo", 0d);
				listCriteriaTo.add(CriteriaTOUtils.createORTO(riesgoBajo, sinRiesgo1, sinRiesgo2, riesgoBajoB, sinRiesgo1B, sinRiesgo2B));
			    }
			    break;

			case "2":
			    MatchCriteriaTO riesgoMedio1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATER, "progIndices.progindRiesgoExpo", Double.parseDouble(confAmarillo.getCnfValor()));
			    MatchCriteriaTO riesgoMedio2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESS, "progIndices.progindRiesgoExpo", Double.parseDouble(confRojo.getCnfValor()));
			    AND_TO riesgoMedio = CriteriaTOUtils.createANDTO(riesgoMedio1, riesgoMedio2);
			    if (!incProy) {
				listCriteriaTo.add(riesgoMedio);
			    } else {
				MatchCriteriaTO riesgoMedio1B = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATER, "proyectosSet.proyIndices.progindRiesgoExpo", Double.parseDouble(confAmarillo.getCnfValor()));
				MatchCriteriaTO riesgoMedio2B = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESS, "proyectosSet.proyIndices.progindRiesgoExpo", Double.parseDouble(confRojo.getCnfValor()));
				AND_TO riesgoMedioB = CriteriaTOUtils.createANDTO(riesgoMedio1B, riesgoMedio2B);
				listCriteriaTo.add(CriteriaTOUtils.createORTO(riesgoMedio, riesgoMedioB));
			    }
			    break;

			case "3":
			    MatchCriteriaTO riesgoAlto = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "progIndices.progindRiesgoExpo", Double.parseDouble(confRojo.getCnfValor()));
			    if (!incProy) {
				listCriteriaTo.add(riesgoAlto);
			    } else {
				MatchCriteriaTO riesgoAltoB = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "proyectosSet.proyIndices.progindRiesgoExpo", Double.parseDouble(confRojo.getCnfValor()));
				listCriteriaTo.add(CriteriaTOUtils.createORTO(riesgoAlto, riesgoAltoB));
			    }
			    break;
		    }
		}
	    }

	    if (!listCriteriaTo.isEmpty()) {
		if (listCriteriaTo.size() > 1) {
		    CriteriaTO[] arrCriteriaTo = listCriteriaTo.toArray(new CriteriaTO[listCriteriaTo.size()]);
		    criterias.add(CriteriaTOUtils.createORTO(arrCriteriaTo));
		} else {
		    criterias.add(listCriteriaTo.get(0));
		}
	    }
	}

	// Estados Filtro
	List<Object> estados;
	if (filtro.getEstados() != null && !filtro.getEstados().isEmpty()) {
	    estados = filtro.getEstados();
	} else {
	    estados = new ArrayList<>();
	    estados.add(Estados.ESTADOS.INICIO.estado_id);
	    estados.add(Estados.ESTADOS.PLANIFICACION.estado_id);
	    estados.add(Estados.ESTADOS.EJECUCION.estado_id);
	    estados.add(Estados.ESTADOS.FINALIZADO.estado_id);
	}

	// Estados
	if (estados != null) {
	    List<CriteriaTO> estadosCriteria = new ArrayList<>();
	    for (Object estadoId : estados) {
		Integer estadoIdInt = Integer.parseInt(estadoId + "");
		MatchCriteriaTO estado1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "progEstFk.estPk", estadoIdInt);
		if (!incProy) {
		    estadosCriteria.add(estado1);
		} else {
		    MatchCriteriaTO estado2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyectosSet.proyEstFk.estPk", estadoIdInt);
		    OR_TO orCriteria = CriteriaTOUtils.createORTO(estado1, estado2);
		    estadosCriteria.add(orCriteria);
		}
	    }

	    if (!estadosCriteria.isEmpty()) {
		criterias.add(CriteriaTOUtils.createORTO(estadosCriteria.toArray(new CriteriaTO[]{})));
	    }
	}

	// Activo
	if (filtro.getActivo() != null) {
	    MatchCriteriaTO activo1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "activo", filtro.getActivo());
	    criterias.add(activo1);
	    if (!incProy) {
		criterias.add(activo1);
	    } else {
		MatchCriteriaTO activo2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyectosSet.activo", filtro.getActivo());
		criterias.add(CriteriaTOUtils.createORTO(activo1, activo2));
	    }
	}

	if (criterias.size() == 1) {
	    return criterias.get(0);
	}
	if (criterias.size() > 1) {
	    AND_TO criteria = CriteriaTOUtils.createANDTO(criterias.toArray(new CriteriaTO[]{}));
	    return criteria;
	}

	return null;
    }

    /**
     * Retorna los proyectos que no dependen de un programa y aplicando el
     * filtro.
     *
     * @param orgId
     * @param areaId
     * @param usuario
     * @param filtro
     * @return
     * @throws DAOGeneralException
     */
    public List<Proyectos> buscarProyHuerfanosPorFiltro(Integer orgId, Integer areaId, SsUsuario usuario, FiltroInicioTO filtro) throws DAOGeneralException {
	return buscarProyectosPorFiltro(orgId, areaId, usuario, filtro, true);
    }

    /**
     * Retorna los proyectos que dependen de un programa y se le aplica el
     * filtro.
     *
     * @param orgId
     * @param areaId
     * @param usuario
     * @param filtro
     * @return
     * @throws DAOGeneralException
     */
    public List<Proyectos> buscarProyPorFiltro(Integer orgId, Integer areaId, SsUsuario usuario, FiltroInicioTO filtro) throws DAOGeneralException {
	return buscarProyectosPorFiltro(orgId, areaId, usuario, filtro, false);
    }

    /**
     * Retorna los proyectos de un programa según el filtro.
     *
     * @param orgId
     * @param areaId
     * @param usuario
     * @param filtro
     * @return
     * @throws DAOGeneralException
     */
    public List<Proyectos> buscarProyPorFiltroYProg(Integer orgId, Integer areaId, SsUsuario usuario, FiltroInicioTO filtro) throws DAOGeneralException {
	return buscarProyectosPorFiltro(orgId, areaId, usuario, filtro, false);
    }

    /**
     * Dado el filtro de búsqueda retorna la lista de proyectos que no tienen
     * programas asociados y cumplen con el filtro
     *
     * @param orgPk la organización seleccionado del usuario
     * @param areaId la area seleccionada
     * @param usuario el usaurio que realiza la búsqueda
     * @param filtro el filtro
     * @param huerfanos Si se incluyen los proyectos que no dependen de un
     * programa.
     * @return la lista de proyectos
     * @throws DAOGeneralException
     */
    public List<Proyectos> buscarProyectosPorFiltro(Integer orgPk, Integer areaId, SsUsuario usuario, FiltroInicioTO filtro, boolean huerfanos) throws DAOGeneralException {

	List<CriteriaTO> criterios = new ArrayList<>();

	// Area
	if (areaId != null) {
	    MatchCriteriaTO proyArea1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyAreaFk.areaPk", areaId);
	    if (huerfanos) {
		criterios.add(proyArea1);
	    } else {
		MatchCriteriaTO proyArea2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.progAreaFk.areaPk", areaId);
		OR_TO or1 = CriteriaTOUtils.createORTO(proyArea1, proyArea2);
		criterios.add(or1);
	    }
	}

	// Organismo
	MatchCriteriaTO proyOrga1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyOrgFk.orgPk", orgPk);
	if (huerfanos) {
	    criterios.add(proyOrga1);
	} else {
	    MatchCriteriaTO proyOrga2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.progOrgFk.orgPk", orgPk);
	    OR_TO or2 = CriteriaTOUtils.createORTO(proyOrga1, proyOrga2);
	    criterios.add(or2);
	}

	// Permisos de Lectura
	if (!usuario.isUsuarioPMOT(orgPk)) {
	    CriteriaTO proyUsuPM = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NOT_EQUALS, "proyUsrGerenteFk.usuId", usuario.getUsuId());
	    CriteriaTO proyUsuAdjunto = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NOT_EQUALS, "proyUsrAdjuntoFk.usuId", usuario.getUsuId());
	    CriteriaTO proyUsuSponsor = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NOT_EQUALS, "proyUsrSponsorFk.usuId", usuario.getUsuId());
	    CriteriaTO proyUsuPMOF = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NOT_EQUALS, "proyUsrPmofedFk.usuId", usuario.getUsuId());
	    CriteriaTO criteriaUsu = CriteriaTOUtils.createORTO(proyUsuPM, proyUsuAdjunto, proyUsuSponsor, proyUsuPMOF);

	    CriteriaTO proyAreaPermiso1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EMPTY, "areasRestringidasSet", 1);
	    CriteriaTO proyAreaPermiso2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NOT_MEMBER_OF, "areasRestringidasSet", usuario.getUsuArea(orgPk));
	    CriteriaTO orProyPermiso = CriteriaTOUtils.createORTO(proyAreaPermiso1, proyAreaPermiso2);

	    criterios.add(new AND_TO(criteriaUsu, orProyPermiso));
	}

	CriteriaTO criteriaFiltro = null;
	if (filtro != null) {
	    criteriaFiltro = crearFiltroProyecto(usuario, filtro, huerfanos);
	    criterios.add(criteriaFiltro);
	}

	if (huerfanos) {
	    MatchCriteriaTO proyHuerfano = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NULL, "proyProgFk", 1);
	    criterios.add(proyHuerfano);
	}

	CriteriaTO criteria;
	if (criterios.size() == 1) {
	    criteria = criterios.get(0);
	} else {
	    criteria = CriteriaTOUtils.createANDTO(criterios.toArray(new CriteriaTO[0]));

	}

//        printCriteria(criteria, 2, 1);
	List<Proyectos> proyectosResult = super.findEntityByCriteria(Proyectos.class, criteria, new String[]{
	    "proyNombre"}, new boolean[]{true}, null, null);
	return proyectosResult;
    }

    /**
     * Dado el filtro de búsqueda retorna la lista de programas que cumplen con
     * el filtro.
     *
     * @param orgPk
     * @param areaId
     * @param usuario
     * @param filtro
     * @param incProy
     * @return Lista de Programas
     * @throws DAOGeneralException
     */
    public List<Programas> buscarProgPorFiltro(Integer orgPk, Integer areaId, SsUsuario usuario, FiltroInicioTO filtro, boolean incProy) throws DAOGeneralException {

	List<CriteriaTO> criterios = new ArrayList<>();

	//Activo
	MatchCriteriaTO proyActivo = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "activo", !Boolean.FALSE);

	// Area
	if (areaId != null) {
	    MatchCriteriaTO progArea = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.progAreaFk.areaPk", areaId);
	    MatchCriteriaTO proyArea = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyAreaFk.areaPk", areaId);

	    AND_TO andProy = new AND_TO(proyArea, proyActivo);
	    CriteriaTO proyHijo = andProy;

	    OR_TO orCriteria = new OR_TO(progArea, proyHijo);
	    criterios.add(orCriteria);
	} else {
	    criterios.add(proyActivo);
	}

	// Organismo
	MatchCriteriaTO progOrga = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.progOrgFk.orgPk", orgPk);
	criterios.add(progOrga);

	// Permisos de Lectura
	if (!usuario.isUsuarioPMO(orgPk)) {
//            CriteriaTO progAreaPermiso = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NOT_EQUALS, "proyProgFk.areasRestringidasSet.areaPk", usuario.getUsuArea(orgPk).getAreaPk());
	}
	// Permisos de Lectura
	if (!usuario.isUsuarioPMOT(orgPk)
		&& usuario.getUsuArea(orgPk) != null) {
	    CriteriaTO proyUsuPM = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NOT_EQUALS, "proyUsrGerenteFk.usuId", usuario.getUsuId());
	    CriteriaTO proyUsuAdjunto = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NOT_EQUALS, "proyUsrAdjuntoFk.usuId", usuario.getUsuId());
	    CriteriaTO proyUsuSponsor = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NOT_EQUALS, "proyUsrSponsorFk.usuId", usuario.getUsuId());
	    CriteriaTO proyUsuPMOF = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NOT_EQUALS, "proyUsrPmofedFk.usuId", usuario.getUsuId());
	    CriteriaTO criteriaUsu = CriteriaTOUtils.createORTO(proyUsuPM, proyUsuAdjunto, proyUsuSponsor, proyUsuPMOF);
	    criterios.add(criteriaUsu);

	    CriteriaTO proyAreaPermiso1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EMPTY, "areasRestringidasSet", 1);
	    CriteriaTO proyAreaPermiso2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NOT_EQUALS, "areasRestringidasSet.areaPk", usuario.getUsuArea(orgPk).getAreaPk());
	    CriteriaTO orProyPermiso = CriteriaTOUtils.createORTO(proyAreaPermiso1, proyAreaPermiso2);
	    criterios.add(orProyPermiso);

	    CriteriaTO progAreaPermiso1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EMPTY, "proyProgFk.areasRestringidasSet", 1);
	    CriteriaTO progAreaPermiso2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NOT_MEMBER_OF, "proyProgFk.areasRestringidasSet", usuario.getUsuArea(orgPk));
	    CriteriaTO orProgPermiso = CriteriaTOUtils.createORTO(progAreaPermiso1, progAreaPermiso2);
	    criterios.add(orProgPermiso);
	}

	if (filtro != null) {
	    CriteriaTO criteriaFiltroProg = crearFiltroProgramaPorProy(filtro);
	    criterios.add(criteriaFiltroProg);
	}

	CriteriaTO criteria;
	if (criterios.size() == 1) {
	    criteria = criterios.get(0);
	} else {
	    criteria = CriteriaTOUtils.createANDTO(criterios.toArray(new CriteriaTO[0]));
	}

	ProyectosDAO proyDao = new ProyectosDAO(super.getEm());
	List<Programas> programasResult = new ArrayList<>();
	List<EntityReference<Integer>> entitiesReferences = proyDao.findEntityReferenceByCriteria(Proyectos.class, criteria, new String[]{
	    "proyProgFk.progNombre"}, new boolean[]{true}, null, null, new String[]{"proyProgFk"});

	for (EntityReference<Integer> e : entitiesReferences) {
	    Programas progPk = (Programas) e.getPropertyMap().get("proyProgFk");
	    if (progPk != null) {
		programasResult.add(progPk);
	    }
	}

	return programasResult;
    }

    private void printCriteria(CriteriaTO c, int i, int k) {
	if (c == null) {
	    logger.info(k + " :" + sps(i) + " NULL");
	} else if (c instanceof MatchCriteriaTO) {
	    MatchCriteriaTO m = (MatchCriteriaTO) c;
	    logger.info(k + " :" + sps(i) + " " + m.getProperty() + " " + m.getMatchType() + " " + m.getValue());

	} else if (c instanceof AND_TO) {
	    AND_TO a = (AND_TO) c;
	    printCriteria(a.getCriteria1(), i + 1, k++);
	    logger.info(k + " :" + sps(i) + " AND ");
	    printCriteria(a.getCriteria2(), i + 1, k++);

	} else if (c instanceof OR_TO) {
	    OR_TO a = (OR_TO) c;
	    printCriteria(a.getCriteria1(), i + 1, k++);
	    logger.info(k + " :" + sps(i) + " OR ");
	    printCriteria(a.getCriteria2(), i + 1, k++);

	} else {
	    logger.info(k + " :--" + c.toString());
	}
    }

    private String sps(int i) {
	String s = "";
	for (int j = 0; j < i; j++) {
	    s += "  ";
	}
	return s;
    }

    private CriteriaTO crearFiltroProgramaPorProy(FiltroInicioTO filtro) {

	List<CriteriaTO> criterias = new ArrayList<>();
	// Ficha
	if (filtro.getFichaFk() != null && filtro.getTipoFicha() != null) {
	    if (filtro.getTipoFicha().equals(TipoFichaEnum.PROYECTO.id)) {
		MatchCriteriaTO crit1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyPk", filtro.getFichaFk());
		criterias.add(crit1);
	    } else {
		MatchCriteriaTO crit1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyProgFk.progPk", filtro.getFichaFk());
		criterias.add(crit1);
	    }
	}

	// Nombre
	if (!StringsUtils.isEmpty(filtro.getNombre())) {
//	    MatchCriteriaTO crit2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "proyNombre", filtro.getNombre());
	    CriteriaTO crit2 = DAOUtils.createMatchCriteriaTOString("proyNombre", filtro.getNombre());
	    criterias.add(crit2);
	}

	// Sponsor
	if (filtro.getSponsor() != null && !filtro.getSponsor().equals(-1)) {
	    MatchCriteriaTO crit2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyUsrSponsorFk.usuId", filtro.getSponsor());
	    criterias.add(crit2);
	}

	// Gerente o Adjunto
	if (filtro.getGerenteOAdjunto() != null && !filtro.getGerenteOAdjunto().equals(-1)) {
	    MatchCriteriaTO gerente2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyUsrGerenteFk.usuId", filtro.getGerenteOAdjunto());
	    MatchCriteriaTO adjunto2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyUsrAdjuntoFk.usuId", filtro.getGerenteOAdjunto());
	    OR_TO orCriteria2 = CriteriaTOUtils.createORTO(gerente2, adjunto2);
	    criterias.add(orCriteria2);
	}

	// PMO Federada
	if (filtro.getPmoFederada() != null && !filtro.getPmoFederada().equals(-1)) {
	    MatchCriteriaTO crit2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyUsrPmofedFk.usuId", filtro.getPmoFederada());
	    criterias.add(crit2);
	}

	// Anio Desde
	if (filtro.getFechaDesde() != null) {
	    MatchCriteriaTO crit2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "proyIndices.proyindPeriodoInicio", filtro.getFechaDesde());
	    criterias.add(crit2);
	}

	// Anio Hasta
	if (filtro.getFechaHasta() != null) {
	    MatchCriteriaTO anioHasta2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "proyIndices.proyindPeriodoFin", filtro.getFechaHasta());
	    criterias.add(anioHasta2);
	}

	//Area tematica
	if (filtro.getAreasTematicas() != null && !filtro.getAreasTematicas().isEmpty()) {
	    List<CriteriaTO> areaTemCriteria = new ArrayList<>();
	    for (AreasTags areaTem : filtro.getAreasTematicas()) {
		MatchCriteriaTO areaTag2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "areasTematicasSet.arastagPk", areaTem.getArastagPk());
		areaTemCriteria.add(areaTag2);
	    }

	    if (areaTemCriteria.size() > 1) {
		criterias.add(CriteriaTOUtils.createORTO(areaTemCriteria.toArray(new CriteriaTO[]{})));
	    } else {
		criterias.add(areaTemCriteria.get(0));
	    }
	}

	// Interesado ambito
	if (filtro.getInteresadoAmbitoOrganizacion() != null) {
	    /**
	     * Bruno 10-04-17: cambio en el atributo amb_pk, este es el nombre de la columna en la bbdd, se cambia por el nombre del atributo
	     */
//	    MatchCriteriaTO ambito2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "interesadosList.intOrgaFk.orgaAmbFk.amb_pk", filtro.getInteresadoAmbitoOrganizacion().getAmbPk());
	    MatchCriteriaTO ambito2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "interesadosList.intOrgaFk.orgaAmbFk.ambPk", filtro.getInteresadoAmbitoOrganizacion().getAmbPk());
	    criterias.add(ambito2);
	}

	// Interesado Nombre
	if (!StringsUtils.isEmpty(filtro.getInteresadoNombre())) {
//	    MatchCriteriaTO nombre2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "interesadosList.intPersonaFk.persNombre", filtro.getInteresadoNombre());
	    CriteriaTO nombre2 = DAOUtils.createMatchCriteriaTOString("interesadosList.intPersonaFk.persNombre", filtro.getInteresadoNombre());
	    criterias.add(nombre2);
	}

	// Interesado Organizacion
	if (filtro.getInteresadoOrganizacion() != null) {
	    MatchCriteriaTO nombre2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "interesadosList.intOrgaFk.orgaPk", filtro.getInteresadoOrganizacion().getOrgaPk());
	    criterias.add(nombre2);
	}

	//Interesado Rol
	if (filtro.getInteresadoRol() != null && !filtro.getInteresadoRol().equals(-1)) {
	    MatchCriteriaTO nombre2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "interesadosList.intRolintFk.rolintPk", filtro.getInteresadoRol());
	    criterias.add(nombre2);
	}

	//Presupuesto Proveedor
	if (filtro.getOrgaProveedor() != null) {
	    MatchCriteriaTO orgProv2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyPreFk.adquisicionSet.adqProvOrga.orgaPk", filtro.getOrgaProveedor().getOrgaPk());
	    criterias.add(orgProv2);
	}

	//Presupuesto Fuente
	if (filtro.getFuenteFinanciamiento() != null) {
	    MatchCriteriaTO fuente3 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyPreFk.fuenteFinanciamiento.fuePk", filtro.getFuenteFinanciamiento().getFuePk());
	    MatchCriteriaTO fuente4 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyPreFk.adquisicionSet.adqFuente.fuePk", filtro.getFuenteFinanciamiento().getFuePk());
	    CriteriaTO fuenteB = CriteriaTOUtils.createORTO(fuente3, fuente4);
	    criterias.add(fuenteB);
	}

	// Riesgos Altos
	if (filtro.getCantidadRiesgosAltos() != null && filtro.getCantidadRiesgosAltos() > 0) {
	    MatchCriteriaTO riesgoAlto2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "proyIndices.proyindRiesgoAlto", filtro.getCantidadRiesgosAltos());
	    criterias.add(riesgoAlto2);
	}

	// Riegos Exposicion(por colores)
	if (CollectionsUtils.isNotEmpty(filtro.getGradoRiesgo())) {
	    List<CriteriaTO> listCriteriaTo = new ArrayList<>();

	    for (Object grado : filtro.getGradoRiesgo()) {
		String g = (String) grado;
		Configuracion confAmarillo = filtro.getConfiguracion().get("RIESGO_INDICE_LIMITE_AMARILLO");
		Configuracion confRojo = filtro.getConfiguracion().get("RIESGO_INDICE_LIMITE_ROJO");

		if (!g.isEmpty() && confAmarillo != null && confRojo != null) {
		    switch (g) {
			case "1":
			    MatchCriteriaTO riesgoBajoB = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "proyIndices.proyindRiesgoExpo", Double.parseDouble(confAmarillo.getCnfValor()));
			    MatchCriteriaTO sinRiesgo1B = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NULL, "proyIndices.proyindRiesgoExpo", 1);
			    MatchCriteriaTO sinRiesgo2B = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyIndices.proyindRiesgoExpo", 0d);
			    listCriteriaTo.add(CriteriaTOUtils.createORTO(riesgoBajoB, sinRiesgo1B, sinRiesgo2B));
			    break;

			case "2":
			    MatchCriteriaTO riesgoMedio1B = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATER, "proyIndices.proyindRiesgoExpo", Double.parseDouble(confAmarillo.getCnfValor()));
			    MatchCriteriaTO riesgoMedio2B = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESS, "proyIndices.proyindRiesgoExpo", Double.parseDouble(confRojo.getCnfValor()));
			    AND_TO riesgoMedioB = CriteriaTOUtils.createANDTO(riesgoMedio1B, riesgoMedio2B);
			    listCriteriaTo.add(riesgoMedioB);
			    break;

			case "3":
			    MatchCriteriaTO riesgoAltoB = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "proyIndices.proyindRiesgoExpo", Double.parseDouble(confRojo.getCnfValor()));
			    listCriteriaTo.add(riesgoAltoB);
			    break;
		    }
		}
	    }

	    if (!listCriteriaTo.isEmpty()) {
		if (listCriteriaTo.size() > 1) {
		    CriteriaTO[] arrCriteriaTo = listCriteriaTo.toArray(new CriteriaTO[listCriteriaTo.size()]);
		    criterias.add(CriteriaTOUtils.createORTO(arrCriteriaTo));
		} else {
		    criterias.add(listCriteriaTo.get(0));
		}
	    }
	}

	// Calidad
	if (filtro.getCalidadIndice() != null && filtro.getCalidadIndice() > 0) {
	    Configuracion confAmarillo = filtro.getConfiguracion().get("CALIDAD_LIMITE_AMARILLO");
	    Configuracion confRojo = filtro.getConfiguracion().get("CALIDAD_LIMITE_ROJO");

	    if (confAmarillo != null && confRojo != null) {
		switch (filtro.getCalidadIndice()) {
		    case 1:
			MatchCriteriaTO calidadAlta = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATER, "proyIndices.proyindCalInd", Double.parseDouble(confAmarillo.getCnfValor()));
			criterias.add(calidadAlta);
			break;

		    case 2:
			MatchCriteriaTO calidadMedia1 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATER, "proyIndices.proyindCalInd", Double.parseDouble(confRojo.getCnfValor()));
			MatchCriteriaTO calidadMedia2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "proyIndices.proyindCalInd", Double.parseDouble(confAmarillo.getCnfValor()));
			criterias.add(CriteriaTOUtils.createANDTO(calidadMedia1, calidadMedia2));
			break;

		    case 3:
			MatchCriteriaTO calidadBaja = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "proyIndices.proyindCalInd", Double.parseDouble(confRojo.getCnfValor()));
			criterias.add(calidadBaja);
			break;
		}
	    }
	}

	// Estados Filtro
	List<Object> estados;
	if (filtro.getEstados() != null && !filtro.getEstados().isEmpty()) {
	    estados = filtro.getEstados();
	} else {
	    estados = new ArrayList<>();
	    estados.add(Estados.ESTADOS.INICIO.estado_id);
	    estados.add(Estados.ESTADOS.PLANIFICACION.estado_id);
	    estados.add(Estados.ESTADOS.EJECUCION.estado_id);
	    estados.add(Estados.ESTADOS.FINALIZADO.estado_id);
	}

	// Estados
	if (estados != null) {
	    List<CriteriaTO> estadosCriteria = new ArrayList<>();
	    for (Object estadoId : estados) {
		Integer estadoIdInt = Integer.parseInt(estadoId + "");
		MatchCriteriaTO estado2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyEstFk.estPk", estadoIdInt);
		estadosCriteria.add(estado2);
	    }

	    if (filtro.getEstados().size() > 1) {
		criterias.add(CriteriaTOUtils.createORTO(estadosCriteria.toArray(new CriteriaTO[]{})));
	    } else if (!estadosCriteria.isEmpty()) {
		criterias.add(estadosCriteria.get(0));
	    }
	}

	// Activo
	if (filtro.getActivo() != null) {
	    MatchCriteriaTO activo2 = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "activo", filtro.getActivo());
	    criterias.add(activo2);
	}

	if (criterias.size() == 1) {
	    return criterias.get(0);
	}
	if (criterias.size() > 1) {
	    AND_TO criteria = CriteriaTOUtils.createANDTO(criterias.toArray(new CriteriaTO[]{}));
	    return criteria;
	}

	return null;
    }
}

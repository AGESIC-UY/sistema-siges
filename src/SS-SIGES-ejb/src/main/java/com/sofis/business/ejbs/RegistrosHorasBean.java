package com.sofis.business.ejbs;

import com.sofis.business.validations.RegistrosHorasValidacion;
import com.sofis.data.daos.RegistrosHorasDao;
import com.sofis.entities.codigueras.ConfiguracionCodigos;
import com.sofis.entities.constantes.ConstanteApp;
import com.sofis.entities.constantes.MensajesNegocio;
import com.sofis.entities.data.Moneda;
import com.sofis.entities.data.RegistrosHoras;
import com.sofis.exceptions.BusinessException;
import com.sofis.exceptions.GeneralException;
import com.sofis.exceptions.TechnicalException;
import com.sofis.generico.utils.generalutils.CollectionsUtils;
import com.sofis.persistence.dao.exceptions.DAOGeneralException;
import com.sofis.sofisform.to.CriteriaTO;
import com.sofis.sofisform.to.MatchCriteriaTO;
import com.sofis.utils.CriteriaTOUtils;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Usuario
 */
@Named
@Stateless(name = "RegistrosHorasBean")
@LocalBean
public class RegistrosHorasBean {

    @Inject
    private DatosUsuario du;
    @PersistenceContext(unitName = ConstanteApp.PERSISTENCE_CONTEXT_UNIT_NAME)
    private EntityManager em;
    private static final Logger logger = Logger.getLogger(RegistrosHorasBean.class.getName());

    @Inject
    private ProyectosBean proyectosBean;
    @Inject
    private ConfiguracionBean configuracionBean;
    
    
    //private String usuario;
    //private String origen;
    
    @PostConstruct
    public void init(){
        //usuario = du.getCodigoUsuario();
        //origen = du.getOrigen();
    }
    

    public RegistrosHoras obtenerRegHorasPorPk(Integer rhPk) {
        RegistrosHorasDao dao = new RegistrosHorasDao(em);
        try {
            return dao.findById(RegistrosHoras.class, rhPk);
        } catch (DAOGeneralException ex) {
            logger.log(Level.SEVERE, null, ex);
            BusinessException be = new BusinessException();
            be.addError(MensajesNegocio.ERROR_REGISTROSHORAS_OBTENER);
            throw be;
        }
    }

    public List<RegistrosHoras> registrarHoras(List<RegistrosHoras> registroHoras, Integer orgPk) {
        if (CollectionsUtils.isNotEmpty(registroHoras)) {
            for (RegistrosHoras rh : registroHoras) {
                if (rh.getRhAprobado() == null) {
                    rh.setRhAprobado(false);
                }
                registrarHoras(rh, orgPk);
            }
        }
        return registroHoras;
    }

    public RegistrosHoras registrarHoras(RegistrosHoras registroHoras, Integer orgPk) throws GeneralException {
        if (registroHoras == null) {
            return null;
        }
        RegistrosHorasValidacion.validar(registroHoras);
        RegistrosHorasDao registrosHorasDao = new RegistrosHorasDao(em);
        try {
            registroHoras = registrosHorasDao.update(registroHoras);
            proyectosBean.guardarIndicadores(registroHoras.getRhProyectoFk().getProyPk(), orgPk);

        } catch (BusinessException be) {
            //Si es de tipo negocio envía la misma excepción
            throw be;
        } catch (Exception ex) {
            logger.logp(Level.SEVERE, ParticipantesBean.class.getName(), "registrarHoras", ex.getMessage(), ex);
            TechnicalException ge = new TechnicalException(ex);
            ge.addError(ex.getMessage());
            throw ge;
        }
        return registroHoras;
    }

    public List<RegistrosHoras> obtenerRegistrosHoras(Integer usuId, Integer proyPk, Integer entPk, Date fDesde, Date fHasta, Long desde,
                                                    Long cant, Integer aprobado, Integer orgPk) {
        List<RegistrosHoras> registrosHoras = new LinkedList<>();
        try {
            RegistrosHorasDao registrosHorasDao = new RegistrosHorasDao(em);

            List<CriteriaTO> crits = new LinkedList<>();

            if (usuId != null) {
                crits.add(CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "rhUsuarioFk.usuId", usuId));
            }
            if (proyPk != null) {
                crits.add(CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "rhProyectoFk.proyPk", proyPk));
            }
            if (entPk != null) {
                crits.add(CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "rhEntregableFk.entPk", entPk));
            }
            if (fDesde != null) {
                crits.add(CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.GREATEREQUAL, "rhFecha", fDesde));
            }
            if (fHasta != null) {
                crits.add(CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.LESSEQUAL, "rhFecha", fHasta));
            }
            if (orgPk != null) {
                crits.add(CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "rhProyectoFk.proyOrgFk.orgPk", orgPk));
            }           
            if (aprobado != null) {
                if (aprobado.equals(0)) {
                    CriteriaTO aproFalse = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "rhAprobado", Boolean.FALSE);
                    CriteriaTO aproNull = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.NULL, "rhAprobado", 1);
                    crits.add(CriteriaTOUtils.createORTO(aproFalse, aproNull));
                } else if (aprobado.equals(1)) {
                    crits.add(CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "rhAprobado", Boolean.TRUE));
                }
            }
     
            if (crits.isEmpty()) {
                registrosHoras = registrosHorasDao.findAll(RegistrosHoras.class);
            } else {
                CriteriaTO crit = null;
                if (crits.size() == 1)
                    crit = crits.get(0);
                else
                    crit = CriteriaTOUtils.createANDTO(crits.toArray(new CriteriaTO[0]));
                                
                //Postgre no permite un select distinct y que en el select no esten los campos del order by.
                //String[] orderBy1 = new String[]{"rhFecha", "rhUsuarioFk.usuPrimerNombre", "rhUsuarioFk.usuPrimerApellido", "rhProyectoFk.proyNombre"};
                //boolean[] orderBy2 = new boolean[]{false, true, true, true};
                String[] orderBy1 = new String[]{"rhFecha"};
                boolean[] orderBy2 = new boolean[]{false};
                registrosHoras = registrosHorasDao.findEntityByCriteria(RegistrosHoras.class, crit, orderBy1, orderBy2, desde, cant);
            }
            return registrosHoras;
        } catch (BusinessException be) {
            //Si es de tipo negocio envía la misma excepción
            throw be;
        } catch (Exception ex) {
            logger.logp(Level.SEVERE, ParticipantesBean.class.getName(), "registrarHoras", ex.getMessage(), ex);
            TechnicalException ge = new TechnicalException(ex);
            ge.addError(ex.getMessage());
            throw ge;
        }
    }

    public Double obtenerHorasAbrobPorProy(Integer proyPk, Integer usuId) {
        if (proyPk != null && usuId != null) {
            RegistrosHorasDao dao = new RegistrosHorasDao(em);
            Double result = dao.obtenerHorasAbrobPorProy(proyPk, usuId);
            return result;
        }
        return null;
    }

    public Double obtenerHorasPendPorProy(Integer proyPk, Integer usuId) {
        if (proyPk != null && usuId != null) {
            RegistrosHorasDao dao = new RegistrosHorasDao(em);
            Double result = dao.obtenerHorasPendPorProy(proyPk, usuId);
            return result;
        }
        return null;
    }

    public void eliminarHoras(Integer hrPk) {
        if (hrPk != null) {
            RegistrosHorasDao dao = new RegistrosHorasDao(em);
            try {
                RegistrosHoras rh = obtenerRegHorasPorPk(hrPk);
                dao.delete(rh, du.getCodigoUsuario(),du.getOrigen());
            } catch (DAOGeneralException ex) {
                logger.log(Level.SEVERE, null, ex);
                BusinessException be = new BusinessException();
                be.addError(MensajesNegocio.ERROR_REGISTROSHORAS_ELIMINAR);
                throw be;
            }
        }
    }

    public boolean usuTieneHorasAprob(Integer usuId, Integer proyPk) {
        if (usuId != null && proyPk != null) {
            RegistrosHorasDao dao = new RegistrosHorasDao(em);
            return dao.usuTieneHorasAprob(usuId, proyPk);
        }
        return false;
    }

    public List<RegistrosHoras> obtenerHorasPorProy(Integer proyPk) {
        if (proyPk != null) {
            RegistrosHorasDao dao = new RegistrosHorasDao(em);
            try {
                return dao.findByOneProperty(RegistrosHoras.class, "rhProyectoFk.proyPk", proyPk);
            } catch (DAOGeneralException ex) {
                logger.log(Level.SEVERE, null, ex);
                BusinessException be = new BusinessException();
                be.addError(MensajesNegocio.ERROR_REGISTROSHORAS_OBTENER);
                throw be;
            }
        }
        return null;
    }

    public List<Moneda> obtenerMonedasPorProy(Integer proyPk) {
        if (proyPk != null) {
            String database = configuracionBean.obtenerCnfValorPorCodigo(ConfiguracionCodigos.DATABASE_ENGINE, null);
            RegistrosHorasDao dao = new RegistrosHorasDao(em);
            return dao.obtenerMonedasPorProy(proyPk, database);
        }
        return null;
    }

    public Double obtenerImporteTotalHs(Integer proyPk, Integer orgPk, Integer monPk, Integer mes, Integer year, Integer usuPk, Boolean aprobado) {
        if (proyPk != null && monPk != null) {
            RegistrosHorasDao dao = new RegistrosHorasDao(em);
            return dao.obtenerImporteTotalHs(proyPk, orgPk, monPk, mes, year, usuPk, aprobado);
        }
        return null;
    }
    
    public Double obtenerImporteTotalHsAprob(Integer proyPk, Integer orgPk, Integer monPk, Integer mes, Integer anio, Integer usuPk) {
        if (proyPk != null && monPk != null) {
            RegistrosHorasDao dao = new RegistrosHorasDao(em);
            return dao.obtenerImporteTotalHsAprob(proyPk, orgPk, monPk, mes, anio, usuPk);
        }
        return null;
    }
            
    public Boolean tieneHoraYValorEnMonedaAnio(Integer proyPk, Integer monPk, Integer anio, Integer usuPk, Boolean aprobado) {
        if (proyPk != null) {
            RegistrosHorasDao dao = new RegistrosHorasDao(em);
            return dao.tieneHoraYValorEnMonedaAnio(proyPk, monPk, anio, usuPk, aprobado);
        }
        return null;
    }        

    public Double obtenerImporteTotalHsPend(Integer proyPk, Integer orgPk, Integer monPk, Integer mes, Integer anio, Integer usuPk) {
        if (proyPk != null && monPk != null) {
            RegistrosHorasDao dao = new RegistrosHorasDao(em);
            return dao.obtenerImporteTotalHsPend(proyPk, orgPk, monPk, mes, anio, usuPk);
        }
        return null;
    }

    public Date obtenerPrimeraFecha(Integer proyPk, boolean asc) {
        if (proyPk != null) {
            RegistrosHorasDao dao = new RegistrosHorasDao(em);
            return dao.obtenerPrimeraFecha(proyPk, asc);
        }
        return null;
    }

    public boolean tieneDependenciasEnt(Integer entPk) {
        RegistrosHorasDao dao = new RegistrosHorasDao(em);
        return dao.tieneDependenciasEnt(entPk);
    }
}

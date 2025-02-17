package com.sofis.business.validations;

import com.sofis.business.utils.ProgProyUtils;
import com.sofis.business.utils.Utils;
import com.sofis.entities.constantes.ConstanteApp;
import com.sofis.entities.constantes.ConstantesErrores;
import com.sofis.entities.constantes.MensajesNegocio;
import com.sofis.entities.data.Estados;
import com.sofis.entities.data.Proyectos;
import com.sofis.entities.data.SsUsuario;
import com.sofis.exceptions.BusinessException;
import com.sofis.generico.utils.generalutils.StringsUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public class ProyectosValidacion {

    private static final Logger logger = Logger.getLogger(ProyectosValidacion.class.getName());

    public static boolean validar(Proyectos proy, SsUsuario usuario, Integer orgPk) throws BusinessException {

        BusinessException be = new BusinessException();
        be.addError(MensajesNegocio.ERROR_PROYECTO);

        if (proy == null) {
            be.addError(ConstantesErrores.ERROR_DATO_VACIO);
        } else {
            boolean isAlta = proy.getProyPk() == null;

            if (proy.isActivo()) {
                /**
                 * 2019-07-05: se elimina esta validación, ya que no permite
                 * guardar proyectos si el programa está en estado pendiente
                 */
//                if (	proy.getProyProgFk() != null && 
//						proy.getProyProgFk().getProgEstFk() != null &&
//						proy.getProyProgFk().getProgEstFk().isPendientes()) {
//                    be.addError(MensajesNegocio.ERROR_FICHA_PROG_HABILITADO);
//                }
                if (proy.getProyOrgFk() == null) {
                    be.addError(MensajesNegocio.ERROR_FICHA_ORGANISMO);
                }
                if (StringsUtils.isEmpty(proy.getProyNombre())) {
                    be.addError(MensajesNegocio.ERROR_FICHA_NOMBRE);
                } else if (proy.getProyNombre().length() > 100) {
                    be.addError(Utils.mensajeLargoCampo("nombreProgProy", orgPk, 100));
                }
                if (proy.getProyAreaFk() == null) {
                    be.addError(MensajesNegocio.ERROR_FICHA_AREA);
                }
                if (proy.getProyUsrAdjuntoFk() == null) {
                    be.addError(MensajesNegocio.ERROR_FICHA_ADJUNTO);
                }
                if (proy.getProyUsrSponsorFk() == null) {
                    be.addError(MensajesNegocio.ERROR_FICHA_SPONSOR);
                }
                if (proy.getProyUsrGerenteFk() == null) {
                    be.addError(MensajesNegocio.ERROR_FICHA_GERENTE);
                }
                if (proy.getProySemaforoAmarillo() == null || proy.getProySemaforoAmarillo() <= 0) {
                    be.addError(MensajesNegocio.ERROR_FICHA_SEMAFORO_AMARILLO);
                }
                if (proy.getProySemaforoRojo() == null || proy.getProySemaforoRojo() <= 0) {
                    be.addError(MensajesNegocio.ERROR_FICHA_SEMAFORO_ROJO);
                }

                if (proy.getProyProgFk() != null && (proy.getProyPeso() == null || proy.getProyPeso() < 0)) {
                    be.addError(MensajesNegocio.ERROR_FICHA_PESO);
                }

                if (proy.getActivo() == null) {
                    be.addError(MensajesNegocio.ERROR_FICHA_ACTIVO);
                }

                if (!StringsUtils.isEmpty(proy.getProySituacionActual())
                        && proy.getProySituacionActual().length() > 20000) {
                    be.addError(Utils.mensajeLargoCampo("ficha_situacionActualFicha", orgPk, 20000));
                }

                if (proy.getProyPreFk() != null) {
                    if (proy.getProyPreFk().getFuenteFinanciamiento() != null
                            || (proy.getProyPreFk().getPreBase() != null && proy.getProyPreFk().getPreBase() > 0)
                            || proy.getProyPreFk().getPreMoneda() != null) {
                        /*
                         if (proy.getProyPreFk().getFuenteFinanciamiento() == null) {
                         be.addError(MensajesNegocio.ERROR_PRESUPUESTO_FUENTE_FINANC);
                         }
                         if (proy.getProyPreFk().getPreBase() == null
                         || proy.getProyPreFk().getPreBase() <= 0) {
                         be.addError(MensajesNegocio.ERROR_PRESUPUESTO_BASE);
                         }
                         if (proy.getProyPreFk().getPreMoneda() == null) {
                         be.addError(MensajesNegocio.ERROR_PRESUPUESTO_MONEDA);
                         }
                         */
                    }
                }

                if (!isAlta) {
                    if (proy.getProyUsrPmofedFk() == null) {
                        be.addError(MensajesNegocio.ERROR_FICHA_PMOF);
                    }
                }

                if (usuario != null) {
                    boolean isPM = ProgProyUtils.isUsuarioGerenteOAdjunto(proy, usuario);
                    boolean isPMOT = usuario.isUsuarioPMOT(orgPk);
                    boolean isInicioYSolAceptacion = proy.getProyEstFk().isEstado(Estados.ESTADOS.INICIO.estado_id)
                            && (proy.isEstPendienteFk());
                    boolean isPlanOEjecucion = proy.getProyEstFk().isEstado(Estados.ESTADOS.PLANIFICACION.estado_id)
                            || proy.getProyEstFk().isEstado(Estados.ESTADOS.EJECUCION.estado_id);

                    if (isPM && (isInicioYSolAceptacion || isPlanOEjecucion)
                            || (isPMOT && isPlanOEjecucion)) {
                        if (StringsUtils.isEmpty(proy.getProyObjetivo())) {
                            be.addError(MensajesNegocio.ERROR_FICHA_OBJETIVO);
                        } else if (proy.getProyObjetivo().length() > 4000) {
                            be.addError(Utils.mensajeLargoCampo("objFicha", orgPk, 4000));
                        }
                        if (StringsUtils.isEmpty(proy.getProyObjPublico())) {
                            be.addError(MensajesNegocio.ERROR_FICHA_PUBLICO_OBJ);
                        } else if (proy.getProyObjPublico().length() > 4000) {
                            be.addError(Utils.mensajeLargoCampo("objPublicoFicha", orgPk, 4000));
                        }
                    }
                }
            }
        }

        if (be.getErrores().size() > 1) {
            logger.logp(Level.INFO, ProyectosValidacion.class.getName(), "validar", be.getErrores().toString(), be);
            throw be;
        }

        return true;
    }
}

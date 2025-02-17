package com.sofis.business.validations;

import com.sofis.entities.constantes.ConstanteApp;
import com.sofis.entities.constantes.MensajesNegocio;
import com.sofis.entities.data.Interesados;
import com.sofis.entities.data.Participantes;
import com.sofis.exceptions.BusinessException;
import com.sofis.generico.utils.generalutils.StringsUtils;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public class ParticipantesValidacion {

    private static final Logger logger = Logger.getLogger(ParticipantesValidacion.class.getName());

    public static boolean validar(Participantes participante) throws BusinessException {

        BusinessException be = new BusinessException();

        if (participante == null) {
            be.addError(MensajesNegocio.ERROR_PARTICIPANTE_NULL);
        } else {
            if (participante.getPartProyectoFk() == null) {
                be.addError(MensajesNegocio.ERROR_PARTICIPANTE_PROYECTO);
            }
            if (participante.getPartUsuarioFk() == null) {
                be.addError(MensajesNegocio.ERROR_PARTICIPANTE_USUARIO);
            }
            if (participante.getPartEntregablesFk() == null) {
                be.addError(MensajesNegocio.ERROR_PARTICIPANTE_ENTREGABLE);
            }
        }

        if (be.getErrores().size() > 0) {
            logger.logp(Level.WARNING, ParticipantesValidacion.class.getName(), "validar", be.getErrores().toString(), be);
            throw be;
        }

        return true;
    }
}

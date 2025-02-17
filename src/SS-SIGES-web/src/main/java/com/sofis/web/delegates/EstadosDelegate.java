package com.sofis.web.delegates;


import com.sofis.business.ejbs.EstadosBean;
import com.sofis.entities.data.Estados;
import com.sofis.exceptions.GeneralException;
import java.util.List;
import  javax.inject.Inject;

/**
 *
 * @author Usuario
 */
public class EstadosDelegate {
    
    @Inject
    private EstadosBean estadosBean;
    
    public Estados obtenerEstadosPorId(int estPk) throws GeneralException {
        return estadosBean.obtenerEstadosPorId(estPk);
    }

    public List<Estados> obtenerEstadosRequridosDoc(List<Integer> listEstPk) throws GeneralException {
        return estadosBean.obtenerEstadosRequridosDoc(listEstPk);
    }

    public List<Estados> obtenerEstadosCombo() {
        return estadosBean.obtenerEstadosCombo();
    }
    
    public boolean isOrdenProcesoMenor(Estados e1, Estados e2) {
        return estadosBean.isOrdenProcesoMenor(e1, e2);
    }
    
    public String estadoStr(Integer estPk, Integer orgPk) {
        return estadosBean.estadoStr(estPk, orgPk);
    }
}

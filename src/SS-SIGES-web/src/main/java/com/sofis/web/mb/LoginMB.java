package com.sofis.web.mb;

import com.sofis.entities.constantes.ConstanteApp;
import com.sofis.exceptions.BusinessException;
import com.sofis.web.delegates.SsUsuarioDelegate;
import com.sofis.web.genericos.constantes.ConstantesNavegacion;
import com.sofis.web.utils.JSFUtils;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Usuario
 */
@ManagedBean(name = "LoginMB")
@ViewScoped
public class LoginMB implements Serializable{

    private static final Logger logger = Logger.getLogger(ConstanteApp.LOGGER_NAME);
    @Inject
    private SsUsuarioDelegate ssUsuarioDelegate;
    private String username;
    private String password;
    private boolean ingresarMailParaResetearContrasenia = false;
    private String mailParaResetearContrasenia = "";

    public String getUsername() {
        return this.username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String login() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        try {
            request.login(this.username, this.password);
            logger.log(Level.INFO, "CALL LOGIN");
        } catch (ServletException e) {
            context.addMessage(null, new FacesMessage("Login failed."));
            return "error";
        }
        return ConstantesNavegacion.IR_A_INICIO;
    }

    public boolean isIngresarMailParaResetearContrasenia() {
        return ingresarMailParaResetearContrasenia;
    }

    public void setIngresarMailParaResetearContrasenia(boolean ingresarMailParaResetearContrasenia) {
        this.ingresarMailParaResetearContrasenia = ingresarMailParaResetearContrasenia;
    }

    public String getMailParaResetearContrasenia() {
        return mailParaResetearContrasenia;
    }

    public void setMailParaResetearContrasenia(String mailParaResetearContrasenia) {
        this.mailParaResetearContrasenia = mailParaResetearContrasenia;
    }

    public String recordarContrasenia() {
        ingresarMailParaResetearContrasenia = true;
        return null;
    }

    public String aceptarCambiarContrasenia() {
        if (!mailParaResetearContrasenia.trim().isEmpty()) {
            try {
                ssUsuarioDelegate.resetearContrasenia(mailParaResetearContrasenia, null);
                String msg = "Se le ha enviado una nueva contraseña a " + mailParaResetearContrasenia;
                JSFUtils.agregarMsgInfo(msg);
                mailParaResetearContrasenia = "";
                ingresarMailParaResetearContrasenia = false;
            } catch (BusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                JSFUtils.agregarMensajes(FacesMessage.SEVERITY_ERROR, ex.getErrores());
            }
        }
        return null;
    }

    public String cancelarCambiarContrasenia() {
        mailParaResetearContrasenia = "";
        ingresarMailParaResetearContrasenia = false;
        return null;
    }
}

package com.sofis.entities.data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.envers.Audited;

/**
 *
 * @author Usuario
 */
@Entity
@Audited
@Table(name = "programas_proyectos")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ProgramasProyectos.findAll", query = "SELECT p FROM ProgramasProyectos p"),
    @NamedQuery(name = "ProgramasProyectos.findById", query = "SELECT p FROM ProgramasProyectos p WHERE p.id = :id"),
    @NamedQuery(name = "ProgramasProyectos.findByTipoFicha", query = "SELECT p FROM ProgramasProyectos p WHERE p.tipoFicha = :tipoFicha"),
    @NamedQuery(name = "ProgramasProyectos.findByNombre", query = "SELECT p FROM ProgramasProyectos p WHERE p.nombre = :nombre"),
    @NamedQuery(name = "ProgramasProyectos.findByEstado", query = "SELECT p FROM ProgramasProyectos p WHERE p.estado = :estado")})
public class ProgramasProyectos implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Size(max = 13)
    @Column(name = "id")
    private String id;
    @Column(name = "fichaFk")
    private Integer fichaFk;
    @Basic(optional = false)
    @NotNull
    @Column(name = "tipoFicha")
    private Long tipoFicha;
    @Column(name = "fechaCrea")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date fechaCrea;
    @Column(name = "organismo")
    private Integer organismo;
    @Column(name = "nombre")
    private String nombre;
    @Basic(optional = false)
    @NotNull
    @Column(name = "estado")
    private Integer estado;
    @Column(name = "estadoPendiente")
    private Integer estadoPendiente;
    @Column(name = "areaPk")
    private Integer areaPk;
    @Column(name = "areaNombre")
    private String areaNombre;
    
    @Column(name = "estadoNombre")
    private String estadoNombre;
    @Column(name = "gerente")
    private Integer gerente;
    @Column(name = "gerentePrimerNombre")
    private String gerentePrimerNombre;
    @Column(name = "gerentePrimerApellido")
    private String gerentePrimerApellido;
    @Column(name = "pmoFederada")
    private Integer pmoFederada;
    @Column(name = "activo")
    private Boolean activo;

    public ProgramasProyectos() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getFichaFk() {
        return fichaFk;
    }

    public void setFichaFk(Integer fichaFk) {
        this.fichaFk = fichaFk;
    }

    public Long getTipoFicha() {
        return tipoFicha;
    }

    public void setTipoFicha(Long tipoFicha) {
        this.tipoFicha = tipoFicha;
    }

    public Integer getOrganismo() {
        return organismo;
    }

    public void setOrganismo(Integer organismo) {
        this.organismo = organismo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    public Integer getEstadoPendiente() {
        return estadoPendiente;
    }

    public void setEstadoPendiente(Integer estadoPendiente) {
        this.estadoPendiente = estadoPendiente;
    }

    public Integer getAreaPk() {
        return areaPk;
    }

    public void setAreaPk(Integer areaPk) {
        this.areaPk = areaPk;
    }

    public String getAreaNombre() {
        return areaNombre;
    }

    public void setAreaNombre(String areaNombre) {
        this.areaNombre = areaNombre;
    }

    public String getEstadoNombre() {
        return estadoNombre;
    }

    public void setEstadoNombre(String estadoNombre) {
        this.estadoNombre = estadoNombre;
    }

    public Integer getGerente() {
        return gerente;
    }

    public void setGerente(Integer gerente) {
        this.gerente = gerente;
    }

    public String getGerentePrimerNombre() {
        return gerentePrimerNombre;
    }

    public void setGerentePrimerNombre(String gerentePrimerNombre) {
        this.gerentePrimerNombre = gerentePrimerNombre;
    }

    public String getGerentePrimerApellido() {
        return gerentePrimerApellido;
    }

    public void setGerentePrimerApellido(String gerentePrimerApellido) {
        this.gerentePrimerApellido = gerentePrimerApellido;
    }

    public Integer getPmoFederada() {
        return pmoFederada;
    }

    public void setPmoFederada(Integer pmoFederada) {
        this.pmoFederada = pmoFederada;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Date getFechaCrea() {
        return fechaCrea;
    }

    public void setFechaCrea(Date fechaCrea) {
        this.fechaCrea = fechaCrea;
    }

}

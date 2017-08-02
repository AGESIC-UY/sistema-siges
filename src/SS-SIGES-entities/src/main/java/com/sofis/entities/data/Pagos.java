package com.sofis.entities.data;

import com.sofis.entities.constantes.ConstantesEstandares;
import com.sofis.generico.utils.generalutils.DatesUtils;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Usuario
 */
@Entity
@Table(name = "pagos")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Pagos.findAll", query = "SELECT p FROM Pagos p"),
    @NamedQuery(name = "Pagos.findByPagPk", query = "SELECT p FROM Pagos p WHERE p.pagPk = :pagPk"),
    @NamedQuery(name = "Pagos.findByPagObservacion", query = "SELECT p FROM Pagos p WHERE p.pagObservacion = :pagObservacion"),
    @NamedQuery(name = "Pagos.findByPagFechaPlanificada", query = "SELECT p FROM Pagos p WHERE p.pagFechaPlanificada = :pagFechaPlanificada"),
    @NamedQuery(name = "Pagos.findByPagImportePlanificado", query = "SELECT p FROM Pagos p WHERE p.pagImportePlanificado = :pagImportePlanificado"),
    @NamedQuery(name = "Pagos.findByPagFechaReal", query = "SELECT p FROM Pagos p WHERE p.pagFechaReal = :pagFechaReal"),
    @NamedQuery(name = "Pagos.findByPagImporteReal", query = "SELECT p FROM Pagos p WHERE p.pagImporteReal = :pagImporteReal")})
public class Pagos implements Serializable {
    
    public static final int OBSERVACION_LENGHT = 3000;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "pag_pk")
    private Integer pagPk;
    @JoinColumn(name = "pag_adq_fk", referencedColumnName = "adq_pk")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Adquisicion pagAdqFk;
    @JoinColumn(name = "pag_ent_fk", referencedColumnName = "ent_pk")
    @ManyToOne(fetch = FetchType.EAGER)
    private Entregables entregables;
    @Column(name = "pag_observacion")
    private String pagObservacion;
    @Column(name = "pag_fecha_planificada")
    @Temporal(TemporalType.DATE)
    private Date pagFechaPlanificada;
    @Column(name = "pag_importe_planificado")
    private Double pagImportePlanificado;
    @Column(name = "pag_fecha_real")
    @Temporal(TemporalType.DATE)
    private Date pagFechaReal;
    @Column(name = "pag_importe_real")
    private Double pagImporteReal;
    @Column(name = "pag_txt_referencia")
    private String pagTxtReferencia;
    @Column(name = "pag_confirmar")
    private Boolean pagConfirmar;

    //@OneToOne(mappedBy = "docsPagoFk", fetch = FetchType.EAGER)
    //private Documentos documento;

    public Pagos() {
    }

    public Pagos(Integer pagPk) {
        this.pagPk = pagPk;
    }

    public Pagos(Integer pagPk, Date pagFechaPlanificada, Double pagImportePlanificado) {
        this.pagPk = pagPk;
        this.pagFechaPlanificada = pagFechaPlanificada;
        this.pagImportePlanificado = pagImportePlanificado;
    }

    public Integer getPagPk() {
        return pagPk;
    }

    public void setPagPk(Integer pagPk) {
        this.pagPk = pagPk;
    }

    public Adquisicion getPagAdqFk() {
        return pagAdqFk;
    }

    public void setPagAdqFk(Adquisicion pagAdqFk) {
        this.pagAdqFk = pagAdqFk;
    }

    public String getPagObservacion() {
        return pagObservacion;
    }

    public void setPagObservacion(String pagObservacion) {
        this.pagObservacion = pagObservacion;
    }

    public Date getPagFechaPlanificada() {
        return pagFechaPlanificada;
    }

    public void setPagFechaPlanificada(Date pagFechaPlanificada) {
        this.pagFechaPlanificada = pagFechaPlanificada;
    }

    public Double getPagImportePlanificado() {
        return pagImportePlanificado;
    }

    public void setPagImportePlanificado(Double pagImportePlanificado) {
        this.pagImportePlanificado = pagImportePlanificado;
    }

    public Date getPagFechaReal() {
        return pagFechaReal;
    }

    public void setPagFechaReal(Date pagFechaReal) {
        this.pagFechaReal = pagFechaReal;
    }

    public Double getPagImporteReal() {
        return pagImporteReal;
    }

    public void setPagImporteReal(Double pagImporteReal) {
        this.pagImporteReal = pagImporteReal;
    }

    public String getPagTxtReferencia() {
        return pagTxtReferencia;
    }

    public void setPagTxtReferencia(String pagTxtReferencia) {
        this.pagTxtReferencia = pagTxtReferencia;
    }

    public Boolean getPagConfirmar() {
        return pagConfirmar;
    }

    public void setPagConfirmar(Boolean pagConfirmar) {
        this.pagConfirmar = pagConfirmar;
    }

    public boolean isPagConfirmado() {
        return pagConfirmar != null ? pagConfirmar : false;
    }

   /* public Documentos getDocumento() {
        return documento;
    }

    public void setDocumento(Documentos documento) {
        this.documento = documento;
    }*/

    public Entregables getEntregables() {
        return entregables;
    }

    public void setEntregables(Entregables entregables) {
        this.entregables = entregables;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (pagPk != null ? pagPk.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        
        if (!(object instanceof Pagos)) {
            return false;
        }
        Pagos other = (Pagos) object;

        if (pagPk == null || other.pagPk == null) {
            return this == object;
        }

        if ((this.pagPk == null && other.pagPk != null) || (this.pagPk != null && !this.pagPk.equals(other.pagPk))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sofis.entities.data.Pagos[ pagPk=" + pagPk + " ]";
    }

    /**
     * Calculo del saldo.
     *
     * @return Saldo
     */
    public Double getImporteSaldo() {
        return (pagImportePlanificado - pagImporteReal);
    }

    /**
     * Calculo del porcentaje de ejecución
     *
     * @return Porcentaje de ejecución
     */
    public Double getEjecucion() {
        return pagImporteReal != null && pagImportePlanificado != null ? (pagImporteReal * 100 / pagImportePlanificado) : 0d;
    }

    /**
     *
     * @return
     */
    public String getTextoCombo() {
        DecimalFormat df = new DecimalFormat(ConstantesEstandares.IMPORTE_FORMAT_PATTERN);
        return new StringBuilder()
                .append("(")
                .append(pagAdqFk.getAdqNombre())
                .append(") ")
                .append(DatesUtils.toStringFormat(getPagFechaPlanificada(), ConstantesEstandares.CALENDAR_PATTERN))
                .append(" ")
                .append(pagAdqFk.getAdqMoneda().getMonSigno())
                .append(df.format(getPagImportePlanificado()))
                .toString();
    }
}

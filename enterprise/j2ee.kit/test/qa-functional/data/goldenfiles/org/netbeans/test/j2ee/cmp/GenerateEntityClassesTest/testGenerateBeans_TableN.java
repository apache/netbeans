/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author jskrivanek
 */
@Entity
@Table(name = "TABLEN")
@NamedQueries({
    @NamedQuery(name = "TableN.findAll", query = "SELECT t FROM TableN t")})
public class TableN implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "A")
    private BigInteger a;
    @Lob
    @Column(name = "B")
    private Serializable b;
    @Column(name = "C")
    private Character c;
    @Lob
    @Column(name = "D")
    private String d;
    @Column(name = "E")
    @Temporal(TemporalType.DATE)
    private Date e;
    @Column(name = "F")
    private Integer f;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "G")
    private Double g;
    @Column(name = "H")
    private Double h;
    @Column(name = "I")
    private Double i;
    @Column(name = "J")
    private Integer j;
    @Lob
    @Column(name = "K")
    private String k;
    @Column(name = "L")
    private Integer l;
    @Column(name = "M")
    private Float m;
    @Column(name = "N")
    private Short n;
    @Column(name = "O")
    @Temporal(TemporalType.TIME)
    private Date o;
    @Column(name = "P")
    @Temporal(TemporalType.TIMESTAMP)
    private Date p;
    @Column(name = "Q")
    private String q;

    public TableN() {
    }

    public TableN(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigInteger getA() {
        return a;
    }

    public void setA(BigInteger a) {
        this.a = a;
    }

    public Serializable getB() {
        return b;
    }

    public void setB(Serializable b) {
        this.b = b;
    }

    public Character getC() {
        return c;
    }

    public void setC(Character c) {
        this.c = c;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public Date getE() {
        return e;
    }

    public void setE(Date e) {
        this.e = e;
    }

    public Integer getF() {
        return f;
    }

    public void setF(Integer f) {
        this.f = f;
    }

    public Double getG() {
        return g;
    }

    public void setG(Double g) {
        this.g = g;
    }

    public Double getH() {
        return h;
    }

    public void setH(Double h) {
        this.h = h;
    }

    public Double getI() {
        return i;
    }

    public void setI(Double i) {
        this.i = i;
    }

    public Integer getJ() {
        return j;
    }

    public void setJ(Integer j) {
        this.j = j;
    }

    public String getK() {
        return k;
    }

    public void setK(String k) {
        this.k = k;
    }

    public Integer getL() {
        return l;
    }

    public void setL(Integer l) {
        this.l = l;
    }

    public Float getM() {
        return m;
    }

    public void setM(Float m) {
        this.m = m;
    }

    public Short getN() {
        return n;
    }

    public void setN(Short n) {
        this.n = n;
    }

    public Date getO() {
        return o;
    }

    public void setO(Date o) {
        this.o = o;
    }

    public Date getP() {
        return p;
    }

    public void setP(Date p) {
        this.p = p;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TableN)) {
            return false;
        }
        TableN other = (TableN) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "test.TableN[ id=" + id + " ]";
    }

}

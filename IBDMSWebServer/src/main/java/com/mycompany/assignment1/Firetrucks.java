/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.assignment1;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Kristen
 */
@Entity
@Table(name = "firetrucks")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Firetrucks.findAll", query = "SELECT f FROM Firetrucks f"),
    @NamedQuery(name = "Firetrucks.findById", query = "SELECT f FROM Firetrucks f WHERE f.id = :id"),
    @NamedQuery(name = "Firetrucks.findByName", query = "SELECT f FROM Firetrucks f WHERE f.name = :name"),
    @NamedQuery(name = "Firetrucks.findByDesignatedFireId", query = "SELECT f FROM Firetrucks f WHERE f.designatedFireId = :designatedFireId")})
public class Firetrucks implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "name")
    private String name;
    @Column(name = "designatedFireId")
    private Integer designatedFireId;

    public Firetrucks() {
    }

    public Firetrucks(Integer id) {
        this.id = id;
    }

    public Firetrucks(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDesignatedFireId() {
        return designatedFireId;
    }

    public void setDesignatedFireId(Integer designatedFireId) {
        this.designatedFireId = designatedFireId;
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
        if (!(object instanceof Firetrucks)) {
            return false;
        }
        Firetrucks other = (Firetrucks) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.assignment1.Firetrucks[ id=" + id + " ]";
    }
    
}

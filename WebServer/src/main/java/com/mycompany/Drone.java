/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany;

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

/**
 *
 * @author Kristen
 */
@Entity
@Table(name = "drone")
@NamedQueries({
    @NamedQuery(name = "Drone.findAll", query = "SELECT d FROM Drone d"),
    @NamedQuery(name = "Drone.findById", query = "SELECT d FROM Drone d WHERE d.id = :id"),
    @NamedQuery(name = "Drone.findByName", query = "SELECT d FROM Drone d WHERE d.name = :name"),
    @NamedQuery(name = "Drone.findByXpos", query = "SELECT d FROM Drone d WHERE d.xpos = :xpos"),
    @NamedQuery(name = "Drone.findByYpos", query = "SELECT d FROM Drone d WHERE d.ypos = :ypos")})
public class Drone implements Serializable {

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
    @Column(name = "xpos")
    private Integer xpos;
    @Column(name = "ypos")
    private Integer ypos;

    public Drone() {
    }

    public Drone(Integer id) {
        this.id = id;
    }

    public Drone(Integer id, String name) {
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

    public Integer getXpos() {
        return xpos;
    }

    public void setXpos(Integer xpos) {
        this.xpos = xpos;
    }

    public Integer getYpos() {
        return ypos;
    }

    public void setYpos(Integer ypos) {
        this.ypos = ypos;
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
        if (!(object instanceof Drone)) {
            return false;
        }
        Drone other = (Drone) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.Drone[ id=" + id + " ]";
    }
    
}

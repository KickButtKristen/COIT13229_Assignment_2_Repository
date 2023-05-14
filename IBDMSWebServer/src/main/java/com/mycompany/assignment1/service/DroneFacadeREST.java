package com.mycompany.assignment1.service;

import com.mycompany.assignment1.Drone;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Kristen
 */
@Path("com.mycompany.assignment1.drone")
public class DroneFacadeREST extends AbstractFacade<Drone> {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_IBDMSWebServer_war_1.0-SNAPSHOTPU");
    private EntityManager em = emf.createEntityManager();

    public DroneFacadeREST() {
        super(Drone.class);
    }

    @POST
    @Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void create(Drone entity) {
        getEntityManager().getTransaction().begin();
        super.create(entity);
        getEntityManager().getTransaction().commit();
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void edit(@PathParam("id") Integer id, Drone entity) {
        getEntityManager().getTransaction().begin();
        super.edit(entity);
        getEntityManager().getTransaction().commit();
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        getEntityManager().getTransaction().begin();
        super.remove(super.find(id));
        getEntityManager().getTransaction().commit();
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Drone find(@PathParam("id") Integer id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Drone> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Drone> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    @PreDestroy
    public void onDestroy() {
        em.close();
        emf.close();
    }
}
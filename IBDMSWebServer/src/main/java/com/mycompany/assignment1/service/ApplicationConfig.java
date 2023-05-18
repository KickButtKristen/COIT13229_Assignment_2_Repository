package com.mycompany.assignment1.service;

import java.util.Set;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

/**
 *
 * @author Kristen
 */
@javax.ws.rs.ApplicationPath("webresources")
@Path("com.mycompany.assignment1.drone")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.mycompany.assignment1.service.ApplicationConfig.class);
        resources.add(com.mycompany.assignment1.service.DroneFacadeREST.class);
        resources.add(com.mycompany.assignment1.service.FireFacadeREST.class);
        resources.add(com.mycompany.assignment1.service.FiretrucksFacadeREST.class);
    }
    
}
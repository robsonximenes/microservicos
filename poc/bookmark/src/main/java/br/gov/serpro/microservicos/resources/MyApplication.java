package br.gov.serpro.microservicos.resources;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * @author Ken Finnigan
 */
@ApplicationPath("/rest/")
public class MyApplication extends Application {

    public MyApplication() {
    }
}
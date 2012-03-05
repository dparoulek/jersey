/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.impl;

import java.net.URI;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.server.impl.application.WebApplicationImpl;
import com.sun.jersey.spi.container.ContainerListener;
import com.sun.jersey.spi.container.ContainerNotifier;
import com.sun.jersey.spi.container.WebApplication;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.options;

import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.repositories;

import static org.junit.Assert.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@RunWith(JUnit4TestRunner.class)
public abstract class AbstractResourceTester implements ContainerListener {
    protected static final URI BASE_URI = URI.create("test:/base/");

    protected WebApplication w;

    @Configuration
    public static Option[] configuration() {

        Option[] options = options(
                //                systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),//"DEBUG"),
                // define maven repository
                repositories(
                "http://repo1.maven.org/maven2",
                "http://repository.apache.org/content/groups/snapshots-group",
                "http://repository.ops4j.org/maven2",
                "http://svn.apache.org/repos/asf/servicemix/m2-repo",
                "http://repository.springsource.com/maven/bundles/release",
                "http://repository.springsource.com/maven/bundles/external",
                "http://maven.java.net/content/repositories/snapshots"),

                // load jsr250-api jar
//                wrappedBundle(mavenBundle().groupId("javax.annotation").artifactId("jsr250-api").versionAsInProject()),

                wrappedBundle(mavenBundle().groupId("javax.mail").artifactId("mail").versionAsInProject()),

                // load Jersey bundles
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-core").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-server").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-servlet").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-client").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-atom").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-fastinfoset").versionAsInProject(),
                mavenBundle().groupId("com.sun.jersey").artifactId("jersey-json").versionAsInProject(),

                // jersey-json deps
                mavenBundle().groupId("org.codehaus.jackson").artifactId("jackson-core-asl").versionAsInProject(),
                mavenBundle().groupId("org.codehaus.jackson").artifactId("jackson-mapper-asl").versionAsInProject(),
                mavenBundle().groupId("org.codehaus.jackson").artifactId("jackson-jaxrs").versionAsInProject(),
                mavenBundle().groupId("org.codehaus.jettison").artifactId("jettison").versionAsInProject(),

                // jersey-atom deps
                wrappedBundle(mavenBundle().groupId("rome").artifactId("rome").versionAsInProject()),
                wrappedBundle(mavenBundle().groupId("jdom").artifactId("jdom").versionAsInProject()),

                // jersey-atom deps
                wrappedBundle(mavenBundle().groupId("com.sun.xml.fastinfoset").artifactId("FastInfoset").versionAsInProject()),

                // start felix framework
                felix());

        return options;
    }

    
    protected void initiateWebApplication(IoCComponentProviderFactory provider, ResourceConfig c) {
        w = createWebApplication(c, provider);
    }

    protected void initiateWebApplication(IoCComponentProviderFactory provider, Class... classes) {
        w = createWebApplication(provider, classes);
    }
    
    protected void initiateWebApplication(Class... classes) {
        w = createWebApplication(classes);
    }
    
    protected void initiateWebApplication(ResourceConfig c) {
        w = createWebApplication(c);
    }
    
    private WebApplication createWebApplication(Class... classes) {
        return createWebApplication(null, classes);
    }
    
    private WebApplication createWebApplication(IoCComponentProviderFactory provider, Class... classes) {
        ResourceConfig rc = new DefaultResourceConfig(classes);
        
        return createWebApplication(rc, provider);
    }
    
    private WebApplication createWebApplication(ResourceConfig c) {
        return createWebApplication(c, null);
    }
    
    private WebApplication createWebApplication(ResourceConfig c, IoCComponentProviderFactory provider) {
        Object o = c.getProperties().get(
                ResourceConfig.PROPERTY_CONTAINER_NOTIFIER);
        if (o instanceof ContainerNotifier) {
            ContainerNotifier crf = (ContainerNotifier)o;
            crf.addListener(this);
        }

        WebApplicationImpl a = new WebApplicationImpl();
        initiate(c, a);
        a.initiate(c, provider);
        return a;
    }

    protected void initiate(ResourceConfig c, WebApplication a) {}
    
    protected WebResource resource(String relativeUri) {
        return resource(relativeUri, true);
    }
    
    protected WebResource resource(String relativeUri, ClientConfig clientConfig) {
        return resource(relativeUri, true, clientConfig);
    }
    
    protected WebResource resource(String relativeUri, boolean checkStatus) {
        return resource(relativeUri, checkStatus, null);        
    }
    
    protected WebResource resource(String relativeUri, boolean checkStatus, 
            ClientConfig clientConfig) {
        Client c = (clientConfig == null) 
            ? new Client(new TstResourceClientHandler(BASE_URI, w))
            : new Client(new TstResourceClientHandler(BASE_URI, w), clientConfig);

        if (checkStatus) {
            c.addFilter(new ClientFilter() {
                public ClientResponse handle(ClientRequest ro) {
                    ClientResponse r = getNext().handle(ro);
                    assertTrue("Status: " + r.getStatus(), r.getStatus() < 300);
                    return r;
                }
            });
        }
        WebResource r = c.resource(createCompleteUri(BASE_URI, relativeUri));
        
        return r;
    }

    protected interface Closure {
        void f();
    }

    protected <T extends RuntimeException> T catches(Closure c, Class<T> rex) {
        T t = null;
        try {
            c.f();
        } catch(RuntimeException ex) {
            ex.printStackTrace();
            assertTrue(ex.getClass().getName() + " is not assignable to runtime exception " + rex.getName(),
                    ex.getClass().isAssignableFrom(rex));
            t = rex.cast(ex);
        }
        assertNotNull("No exception was caught of class " + rex.getName(), t);
        return t;
    }

    private URI createCompleteUri(URI baseUri, String relativeUri) {
        if (relativeUri.startsWith("/"))
            relativeUri = relativeUri.substring(1);
        
        return URI.create(baseUri.toString() + relativeUri);
    }

    // ContainerListener

    public void onReload() {
        w = w.clone();
    }
}

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

package com.sun.jersey.guice;

import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import junit.framework.TestCase;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.FilterRegistration;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractGuiceGrizzlyTest extends TestCase {
    private static final Logger LOGGER = Logger.getLogger(AbstractGuiceGrizzlyTest.class.getName());

    public static final String CONTEXT = "/test";

    public static int getEnvVariable(final String varName, int defaultValue) {
        if (null == varName) {
            return defaultValue;
        }
        String varValue = System.getenv(varName);
        if (null != varValue) {
            try {
                return Integer.parseInt(varValue);
            }catch (NumberFormatException e) {
                // will return default value bellow
            }
        }
        return defaultValue;
    }

    private final int port = getEnvVariable("JERSEY_HTTP_PORT", 9997);

    private final URI baseUri = getUri().build();

    private HttpServer httpServer;

    private GuiceFilter f;

    public UriBuilder getUri() {
        return UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT);
    }

    public <T extends GuiceServletContextListener> void startServer(Class<T> c) {
        LOGGER.info("Starting grizzly...");

        WebappContext context = new WebappContext("TestContext", baseUri.getRawPath());
        context.addListener(c.getName());
        f = new GuiceFilter();
        FilterRegistration reg = context.addFilter("guiceFilter", f);
        reg.addMappingForUrlPatterns(null, "/*");
        ServletRegistration sreg = context.addServlet("TestServlet", new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            }
        });
        sreg.addMapping("/test/*");
        

        try {
            httpServer = GrizzlyServerFactory.createHttpServer(baseUri, (HttpHandler) null);
            context.deploy(httpServer);
            httpServer.start();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Stop the embedded Grizzly server.
     * @throws java.lang.Exception
     */
    private void stopGrizzly() throws Exception {
        try {
            if (httpServer != null) {
                // Work around bug in Grizzly
                f.destroy();
                httpServer.stop();
                httpServer = null;
            }
        } catch( Exception e ) {
            LOGGER.log(Level.WARNING, "Could not stop grizzly...", e );
        }
    }
    
    @Override
    public void tearDown() throws Exception {
        LOGGER.info( "tearDown..." );
        stopGrizzly();
        LOGGER.info( "done..." );
    }

    public WebResource resource() {
        final Client c = Client.create();
        final WebResource rootResource = c.resource(getUri().build());
        return rootResource;
    }
}
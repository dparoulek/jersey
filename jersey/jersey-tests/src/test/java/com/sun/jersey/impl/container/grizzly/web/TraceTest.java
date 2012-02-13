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
package com.sun.jersey.impl.container.grizzly.web;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.ResourceConfig;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class TraceTest extends AbstractGrizzlyWebContainerTester {
    
    @Path("/root")
    public static class Resource {
        @POST
        public String post(String post) {
            return post;
        }

        @Path("sub-resource-method")
        @POST
        public String postSub(String post) {
            return post;
        }

        @Path("sub-resource-locator")
        public SubResource getSubLoc() {
            return new SubResource();
        }

        @Path("sub-resource-locator-null")
        public Object getSubLocNull() {
            return null;
        }

        @GET
        @Path("runtime-exception")
        public String getException() {
            throw new RuntimeException();
        }
    }
    
    @Path("/")
    public static class SubResource {
        @POST
        public String post(String post) {
            return post;
        }

        @Path("sub-resource-method")
        @POST
        public String postSub(String post) {
            return post;
        }
    }

    public TraceTest(String testName) {
        super(testName);
    }

    WebResource resource(String path) {
        return Client.create().resource(getUri().path(path).build());
    }
    
    public void testPostPerRequest() {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(ResourceConfig.FEATURE_TRACE_PER_REQUEST,
                "true");
        initParams.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        initParams.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                LoggingFilter.class.getName());
        startServer(initParams, Resource.class);

        WebResource r = resource("/root");

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        assertFalse(hasX_Jersey_Trace(cr));
        assertEquals("POST", cr.getEntity(String.class));

        cr = r.header("X-Jersey-Trace-Accept", "true").post(ClientResponse.class, "POST");
        assertTrue(hasX_Jersey_Trace(cr));
        assertEquals("POST", cr.getEntity(String.class));
    }

    private void init() {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(ResourceConfig.FEATURE_TRACE,
                "true");
        initParams.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        initParams.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                LoggingFilter.class.getName());
        startServer(initParams, Resource.class);
    }
    
    public void testRuntimeException() {
        init();
        
        WebResource r = resource("/root").path("runtime-exception");

        ClientResponse cr = r.get(ClientResponse.class);
        test(cr);
        assertEquals(500, cr.getStatus());
    }

    public void testPost() {
        init();

        WebResource r = resource("/root");

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        test(cr);
        assertEquals("POST", cr.getEntity(String.class));
    }

    public void testGet405() {
        init();

        WebResource r = resource("/root");

        ClientResponse cr = r.get(ClientResponse.class);
        test(cr);
        assertEquals(405, cr.getStatus());
    }

    public void testPostSubResourceMethod() {
        init();

        WebResource r = resource("/root").path("sub-resource-method");

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        test(cr);
        assertEquals("POST", cr.getEntity(String.class));
    }

    public void testPostSubResourceLocator() {
        init();

        WebResource r = resource("/root").path("sub-resource-locator");

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        test(cr);
        assertEquals("POST", cr.getEntity(String.class));
    }

    public void testPostSubResourceLocatorNull() {
        init();

        WebResource r = resource("/root").path("sub-resource-locator-null");

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        assertEquals(404, cr.getStatus());
    }

    public void testPostSubResourceLocatorSubResourceMethod() {
        init();

        WebResource r = resource("/root").path("sub-resource-locator").path("sub-resource-method");

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        test(cr);
        assertEquals("POST", cr.getEntity(String.class));
    }

    private void test(ClientResponse cr) {
        assertTrue(hasX_Jersey_Trace(cr));
    }

    private boolean hasX_Jersey_Trace(ClientResponse cr) {
        for (String k : cr.getHeaders().keySet()) {
            if (k.startsWith("X-Jersey-Trace-"))
                return true;
        }

        return false;
    }
}
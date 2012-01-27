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

package com.sun.jersey.impl.container.httpserver;

import javax.ws.rs.Path;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpServerAdaptorTest extends AbstractHttpServerTester {
    @Path("/{arg1}/{arg2}")
    public static class TestOneWebResource {
        @Context UriInfo info;
        
        @POST
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getMethod());
            
            assertEquals("a", info.getPathParameters().getFirst("arg1"));
            assertEquals("b", info.getPathParameters().getFirst("arg2"));
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-ONE", s);
            
            response.setResponse(Response.ok("RESOURCE-ONE").build());
        }
    }
    
    @Path("/{arg1}")
    public static class TestTwoWebResource {
        @Context UriInfo info;
        
        @POST
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getMethod());
            
            assertEquals("a", info.getPathParameters().getFirst("arg1"));
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-TWO", s);
            
            response.setResponse(Response.ok("RESOURCE-TWO").build());
        }
    }
    
    public HttpServerAdaptorTest(String testName) {
        super(testName);
    }
    
    public void testExplicitWebResourceReference() {
        startServer(TestOneWebResource.class, TestTwoWebResource.class);
        
        WebResource r = Client.create().resource(getUri().path("a").build());
        assertEquals("RESOURCE-TWO", r.post(String.class, "RESOURCE-TWO"));

        r = Client.create().resource(UriBuilder.fromUri(r.getURI()).path("b").build());
        assertEquals("RESOURCE-ONE", r.post(String.class, "RESOURCE-ONE"));
    }
    
    public void testResourceConfig() {
        startServer(new WebResources());
        
        WebResource r = Client.create().resource(getUri().path("a").build());
        assertEquals("RESOURCE-TWO", r.post(String.class, "RESOURCE-TWO"));

        r = Client.create().resource(UriBuilder.fromUri(r.getURI()).path("b").build());
        assertEquals("RESOURCE-ONE", r.post(String.class, "RESOURCE-ONE"));
    }
}

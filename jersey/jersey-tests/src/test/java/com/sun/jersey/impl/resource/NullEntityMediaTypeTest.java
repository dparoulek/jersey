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
package com.sun.jersey.impl.resource;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.impl.AbstractResourceTester;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.ws.rs.FormParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class NullEntityMediaTypeTest extends AbstractResourceTester {
    
    public NullEntityMediaTypeTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class NullResource {
        @GET
        @Path("string")
        public String getString() {
            return null;
        }
        
        @GET
        @Path("object")
        public Object getObject() {
            return null;
        }
        
        @GET
        @Path("response")
        public Response getResponse() {
            return null;
        }
        
        @GET
        @Path("response-entity")
        public Response getResponseEntity() {
            return Response.ok(null).build();
        }
    }
    
    public void testNull() throws IOException {
        initiateWebApplication(NullResource.class);
        WebResource r = resource("/");

        assertEquals(204, r.path("string").get(ClientResponse.class).
                getStatus());
        assertEquals(204, r.path("object").get(ClientResponse.class).
                getStatus());
        assertEquals(204, r.path("response").get(ClientResponse.class).
                getStatus());
        assertEquals(200, r.path("response-entity").get(ClientResponse.class).
                getStatus());
    }
    
    @Path("/")
    public static class NullFormResource {
        @POST
        @Path("string")
        public String getString(@FormParam("a") String a) {
            return null;
        }
        
        @POST
        @Path("object")
        public Object getObject(@FormParam("a") String a) {
            return null;
        }
        
        @POST
        @Path("response")
        public Response getResponse(@FormParam("a") String a) {
            return null;
        }
        
        @POST
        @Path("response-entity")
        public Response getResponseEntity(@FormParam("a") String a) {
            return Response.ok(null).build();
        }
    }
    
    public void testNullForm() throws IOException {
        initiateWebApplication(NullFormResource.class);
        WebResource r = resource("/");

        Form f = new Form();
        f.add("a", "a");
        assertEquals(204, r.path("string").post(ClientResponse.class, f).
                getStatus());
        assertEquals(204, r.path("object").post(ClientResponse.class, f).
                getStatus());
        assertEquals(204, r.path("response").post(ClientResponse.class, f).
                getStatus());
        assertEquals(200, r.path("response-entity").post(ClientResponse.class, f).
                getStatus());
    }
    
    @Path("/")
    public static class NullMediaTypeResource {
        @GET
        @Path("byte")
        public Response getByte() {
            byte[] b = new byte[1];
            b[0] = 65;
            return Response.ok(b, (MediaType)null).build();
        }
        
        @GET
        @Path("ByteArrayInputStream")
        public Response getByteArrayInputStream() {
            byte[] b = new byte[1];
            b[0] = 65;
            return Response.ok(new ByteArrayInputStream(b), (MediaType)null).build();
        }
        
        @GET
        @Path("String")
        public Response getString() {
            return Response.ok("content", (MediaType)null).build();
        }
    }
    
    public void testNullMediaType() throws IOException {
        initiateWebApplication(NullMediaTypeResource.class);
        WebResource r = resource("/");
        
        ClientResponse cr = r.path("byte").get(ClientResponse.class);
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_TYPE, cr.getType());        
        
        cr = r.path("ByteArrayInputStream").get(ClientResponse.class);
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_TYPE, cr.getType());        
        
        cr = r.path("String").get(ClientResponse.class);
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType());        
    }
    
    @Path("/")
    @Produces("text/plain")
    public static class NullWithProducesResource extends NullResource { }
    
    public void testNullWithProduces() throws IOException {
        initiateWebApplication(NullWithProducesResource.class);
        WebResource r = resource("/");

        assertEquals(204, r.path("string").get(ClientResponse.class).
                getStatus());
        assertEquals(204, r.path("object").get(ClientResponse.class).
                getStatus());
        assertEquals(204, r.path("response").get(ClientResponse.class).
                getStatus());
        assertEquals(200, r.path("response-entity").get(ClientResponse.class).
                getStatus());
    }
}
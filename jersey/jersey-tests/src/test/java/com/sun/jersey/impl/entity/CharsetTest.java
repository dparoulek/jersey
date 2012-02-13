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

package com.sun.jersey.impl.entity;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class CharsetTest extends AbstractTypeTester {
    private static String[] CHARSETS = {
        "US-ASCII", 
        "ISO-8859-1", 
        "UTF-8", 
        "UTF-16BE", 
        "UTF-16LE", 
        "UTF-16"
    };
    
    private static final String CONTENT = 
            "\u00A9 CONTENT \u00FF \u2200 \u22FF";
    
    public CharsetTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class StringCharsetResource {
        @Path("US-ASCII")
        @POST
        @Produces("text/plain;charset=US-ASCII")
        public String postUs_Ascii(String t) {
            return t;
        }
        
        @Path("ISO-8859-1")
        @POST
        @Produces("text/plain;charset=ISO-8859-1")
        public String postIso_8859_1(String t) {
            return t;
        }
        
        @Path("UTF-8")
        @POST
        @Produces("text/plain;charset=UTF-8")
        public String postUtf_8(String t) {
            return t;
        }
        
        @Path("UTF-16BE")
        @POST
        @Produces("text/plain;charset=UTF-16BE")
        public String postUtf_16be(String t) {
            return t;
        }
        
        @Path("UTF-16LE")
        @POST
        @Produces("text/plain;charset=UTF-16LE")
        public String postUtf_16le(String t) {
            return t;
        }
        
        @Path("UTF-16")
        @POST
        @Produces("text/plain;charset=UTF-16")
        public String postUtf_16(String t) {
            return t;
        }        
    }
    
    public void testStringCharsetResource() {
        initiateWebApplication(StringCharsetResource.class);
        
        String in = "\u00A9 CONTENT \u00FF \u2200 \u22FF";
        
        for (String charset : CHARSETS) {
            WebResource r = resource(charset);
            ClientResponse rib = r.type("text/plain;charset=" + charset).post(ClientResponse.class, in);

            byte[] inBytes = (byte[])
                    rib.getProperties().get("request.entity");
            byte[] outBytes = (byte[])
                    rib.getProperties().get("response.entity");

            _verify(inBytes, outBytes);            
        }
    }
    
    public static abstract class CharsetResource<T> {
        @Context HttpHeaders h;
        
        @POST
        public Response post(T t) {
            return Response.ok(t, h.getMediaType()).build();
        }
    }    
    
    @Path("/")
    public static class StringResource extends CharsetResource<String> { }
    
    public void testStringRepresentation() {
        _test(CONTENT, StringResource.class);
    }


    @Path("/")
    public static class FormMultivaluedMapResource extends CharsetResource<MultivaluedMap<String, String>> { }

    public void testFormMultivaluedMapRepresentation() {
        MultivaluedMap<String, String> map = new MultivaluedMapImpl();

        map.add("name", "\u00A9 CONTENT \u00FF \u2200 \u22FF");
        map.add("name", "� � �");
        _test(map, FormMultivaluedMapResource.class, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    }

    @Path("/")
    public static class FormResource extends CharsetResource<Form> { }

    public void testRepresentation() {
        Form map = new Form();

        map.add("name", "\u00A9 CONTENT \u00FF \u2200 \u22FF");
        map.add("name", "� � �");
        _test(map, FormResource.class, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    }

    @Path("/")
    public static class JSONObjectResource extends CharsetResource<JSONObject> {}

    public void testJSONObjectRepresentation() throws Exception {
        JSONObject object = new JSONObject();
        object.put("userid", 1234).
        put("username", CONTENT).
        put("email", "a@b").
        put("password", "****");

        _test(object, JSONObjectResource.class, MediaType.APPLICATION_JSON_TYPE);
    }

    @Path("/")
    public static class JSONOArrayResource extends CharsetResource<JSONArray> {}

    public void testJSONArrayRepresentation() throws Exception {
        JSONArray array = new JSONArray();
        array.put(CONTENT).put("Two").put("Three").put(1).put(2.0);

        _test(array, JSONOArrayResource.class, MediaType.APPLICATION_JSON_TYPE);
    }


    @Path("/")
    public static class JAXBBeanResource extends CharsetResource<JAXBBean> {}
    
    public void testJAXBBeanXMLRepresentation() {
        _test(new JAXBBean(CONTENT), JAXBBeanResource.class, MediaType.APPLICATION_XML_TYPE);
    }
    
    public void testJAXBBeanJSONRepresentation() {
        _test(new JAXBBean(CONTENT), JAXBBeanResource.class, MediaType.APPLICATION_JSON_TYPE);
    }
    
    @Provider
    public static class MyJAXBContextResolver implements ContextResolver<JAXBContext> {
        JAXBContext context;
        
        public MyJAXBContextResolver() throws Exception {
            context = new JSONJAXBContext(JAXBBean.class);
        }
        
        public JAXBContext getContext(Class<?> objectType) {
            return (objectType == JAXBBean.class) ? context : null;
        }
    }
    
    public void testJAXBBeanJSONRepresentationWithContextResolver() throws Exception {
        initiateWebApplication(JAXBBeanResource.class, MyJAXBContextResolver.class);
        
        JAXBBean in = new JAXBBean(CONTENT);
        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(MyJAXBContextResolver.class);
        WebResource r = resource("/", cc);
        for (String charset : CHARSETS) {
            ClientResponse rib = r.type("application/json;charset=" + charset).
                    post(ClientResponse.class, in);
            byte[] inBytes = (byte[])
                    rib.getProperties().get("request.entity");
            byte[] outBytes = (byte[])
                    rib.getProperties().get("response.entity");

            _verify(inBytes, outBytes);            
        }        
    }
    
    @Path("/")
    public static class ReaderResource extends CharsetResource<Reader> {}
    
    public void testReaderRepresentation() throws Exception {
        initiateWebApplication(ReaderResource.class);
        
        WebResource r = resource("/");
        for (String charset : CHARSETS) {
            ClientResponse rib = r.type("text/plain;charset=" + charset).
                    post(ClientResponse.class, new StringReader(CONTENT));
            byte[] inBytes = (byte[])
                    rib.getProperties().get("request.entity");
            byte[] outBytes = (byte[])
                    rib.getProperties().get("response.entity");

            _verify(inBytes, outBytes);            
        }
    }
    
    @Override
    public <T> void _test(T in, Class resource) {
        _test(in, resource, MediaType.TEXT_PLAIN_TYPE);
    }

    public <T> void _test(T in, Class resource, MediaType m) {
        initiateWebApplication(resource);

        WebResource r = resource("/");
        r.addFilter(new LoggingFilter());
        for (String charset : CHARSETS) {
            Map<String, String> p = new HashMap<String, String>();
            p.put("charset", charset);
            MediaType _m = new MediaType(m.getType(), m.getSubtype(), p);
            ClientResponse rib = r.type(_m).post(ClientResponse.class, in);
            byte[] inBytes = (byte[])
                    rib.getProperties().get("request.entity");
            byte[] outBytes = (byte[])
                    rib.getProperties().get("response.entity");
            _verify(inBytes, outBytes);
        }
    }
}
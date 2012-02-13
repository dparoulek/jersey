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

package com.sun.jersey.impl.json;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class Issue581Test extends AbstractResourceTester {
    public Issue581Test(String testName) {
        super(testName);
    }

    @Provider
    public static class JAXBContextResolver implements ContextResolver<JAXBContext> {

        private JAXBContext context;
        
        private final Class[] cTypes = {BugReport.class, Note.class};

        private final Set<Class> types;
        public JAXBContextResolver() {
            try {
                this.types = new HashSet<Class>(Arrays.asList(cTypes));
                this.context = new JSONJAXBContext(JSONConfiguration.natural().usePrefixesAtNaturalAttributes().build(), cTypes);
            } catch (JAXBException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        @Override
        public JAXBContext getContext(Class<?> c) {
            return types.contains(c) ? context : null;
        }
    }
    
    @Path("/")
    public static class BugReportResource {
        @POST @Consumes("application/json") @Produces("application/json")
        public BugReport get(BugReport b) {
            return b;
        }
    }
    

    public void testBugReportResource() throws Exception {
        JAXBContextResolver cr = new JAXBContextResolver();
        ResourceConfig rc = new DefaultResourceConfig(BugReportResource.class);
        rc.getSingletons().add(cr);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, LoggingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, LoggingFilter.class.getName());
        initiateWebApplication(rc);

        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(cr.getClass());
        cc.getSingletons().add(cr);
        WebResource r = resource("/", cc);
        BugReport br = new BugReport();
        br.description = "BR desc";
        br.title = "BR title";

        BugReport result = r.type(MediaType.APPLICATION_JSON).post(BugReport.class, br);

        assertEquals(br.description, result.description);
        assertEquals(br.title, result.title);
    }        
}
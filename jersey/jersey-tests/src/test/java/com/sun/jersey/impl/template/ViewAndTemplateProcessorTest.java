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

package com.sun.jersey.impl.template;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.template.TemplateProcessor;
import com.sun.jersey.spi.template.ViewProcessor;
import java.io.IOException;
import java.io.OutputStream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ViewAndTemplateProcessorTest extends AbstractResourceTester {
    
    public ViewAndTemplateProcessorTest(String testName) {
        super(testName);
    }

    public static class ViewImpl implements ViewProcessor<String> {

        public String resolve(String name) {
            if (name.endsWith(".vp"))
                return name;

            return null;
        }

        public void writeTo(String t, Viewable viewable, OutputStream out) throws IOException {
            out.write(t.getBytes());
        }
    }

    public static class TemplateImpl implements TemplateProcessor {

        public String resolve(String name) {
            if (name.endsWith(".tp"))
                return name;

            return null;
        }

        public void writeTo(String fullyQualifedName, Object model, OutputStream out) throws IOException {
            out.write(fullyQualifedName.getBytes());
        }
    }

    @Path("/")
    public static class TemplateResource {
        @Path("vp")
        @GET 
        public Viewable getVp() {
            return new Viewable("/view.vp", "get");
        }

        @Path("tp")
        @GET
        public Viewable getTp() {
            return new Viewable("/view.tp", "get");
        }
    }

    public void testExplicitTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(TemplateResource.class,
                ViewImpl.class, TemplateImpl.class);
        initiateWebApplication(rc);
        WebResource r = resource("/");

        assertEquals("/view.vp", r.path("vp").get(String.class));
        assertEquals("/view.tp", r.path("tp").get(String.class));
    }
}
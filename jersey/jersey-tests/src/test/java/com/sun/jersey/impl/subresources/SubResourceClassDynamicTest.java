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

package com.sun.jersey.impl.subresources;

import com.sun.jersey.impl.AbstractResourceTester;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.resource.Singleton;
import javax.ws.rs.GET;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class SubResourceClassDynamicTest extends AbstractResourceTester {
    
    public SubResourceClassDynamicTest(String testName) {
        super(testName);
    }

    @Path("/parent")
    static public class Parent { 
        @GET
        public String getMe() {
            return "parent";
        }
        
        @Path("child")
        public Class<Child> getChild() {
            return Child.class;
        }
    }
    
    static public class Child { 
        @GET
        public String getMe() {
            return "child";
        }
    }
    
    public void testSubResourceDynamic() {
        initiateWebApplication(Parent.class);
        
        assertEquals("parent", resource("/parent").get(String.class));
        assertEquals("child", resource("/parent/child").get(String.class));
    }    
    
    @Path("/{p}")
    static public class ParentWithTemplates { 
        @GET
        public String getMe(@PathParam("p") String p) {
            return p;
        }
        
        @Path("child/{c}")
        public Class<ChildWithTemplates> getChildWithTemplates() {
            return ChildWithTemplates.class;
        }
    }
    
    static public class ChildWithTemplates { 
        @GET
        public String getMe(@PathParam("c") String c) {
            return c;
        }
    }
    
    public void testSubResourceDynamicWithTemplates() {
        initiateWebApplication(ParentWithTemplates.class);
        
        assertEquals("parent", resource("/parent").get(String.class));
        assertEquals("first", resource("/parent/child/first").get(String.class));
    }    
    
    @Path("/{p}")
    static public class ParentWithTemplatesLifecycle { 
        @GET
        public String getMe(@PathParam("p") String p) {
            return p;
        }
        
        @Path("child/{c}")
        public Class<ChildWithTemplatesPerRequest> getChildWithTemplates() {
            return ChildWithTemplatesPerRequest.class;
        }
        
        @Path("child/singleton/{c}")
        public Class<ChildWithTemplatesSingleton> getChildWithTemplatesSingleton() {
            return ChildWithTemplatesSingleton.class;
        }
    }
    
    static public class ChildWithTemplatesPerRequest {
        private int i = 0;
        private String c;
        
        public ChildWithTemplatesPerRequest(@PathParam("c") String c) {
            this.c = c;
        }
        
        @GET
        public String getMe() {
            i++;
            return c + i;
        }
    }
    
    @Singleton
    static public class ChildWithTemplatesSingleton {
        private int i = 0;
        
        @GET
        public String getMe(@PathParam("c") String c) {
            i++;
            return c + i;
        }
    }
    
    public void testSubResourceDynamicWithTemplatesLifecycle() {
        initiateWebApplication(ParentWithTemplatesLifecycle.class);
        
        assertEquals("parent", resource("/parent").get(String.class));
        assertEquals("x1", resource("/parent/child/x").get(String.class));
        assertEquals("x1", resource("/parent/child/x").get(String.class));
        assertEquals("x1", resource("/parent/child/singleton/x").get(String.class));
        assertEquals("x2", resource("/parent/child/singleton/x").get(String.class));
    }    
    
}

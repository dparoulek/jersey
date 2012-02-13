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

package com.sun.jersey.impl.lifecycle;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.HttpContext;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Marc Hadley
 */
public class ProviderLifecycleTest extends AbstractResourceTester {
            
    public ProviderLifecycleTest(String testName) {
        super(testName);
    }

    public static class FileType {
    }
    
    public static abstract class AbstractFileReferenceWriter implements MessageBodyWriter<FileType> {
        List<File> files;

        @Context HttpContext hc;

        public boolean isWriteable(Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return FileType.class.isAssignableFrom(type);
        }

        public long getSize(FileType t, Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public void writeTo(FileType t, Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException, WebApplicationException {
            File f = File.createTempFile("jersey", null);
            assertNotNull(files);
            files.add(f);
            entityStream.write(f.getAbsolutePath().getBytes());
        }
    }

    @Provider
    public static class FileReferenceWriter extends AbstractFileReferenceWriter {
        @PostConstruct
        public void postConstruct() {
            assertNotNull(hc);
            this.files = new ArrayList<File>();
        }

        @PreDestroy
        public void preDestroy() {
            assertNotNull(files);
            for (File f : files) {
                f.delete();
            }
        }
    }

    @Provider
    public static class FileReferenceWriterPrivate extends AbstractFileReferenceWriter {
        @PostConstruct
        private void postConstruct() {
            assertNotNull(hc);
            this.files = new ArrayList<File>();
        }

        @PreDestroy
        private void preDestroy() {
            assertNotNull(files);
            for (File f : files) {
                f.delete();
            }
        }
    }

    @Provider
    public static class FileReferenceWriterProtected extends AbstractFileReferenceWriter {
        @PostConstruct
        protected void postConstruct() {
            assertNotNull(hc);
            this.files = new ArrayList<File>();
        }

        @PreDestroy
        protected void preDestroy() {
            assertNotNull(files);
            for (File f : files) {
                f.delete();
            }
        }
    }

    public static abstract class FileReferenceWriterPostConstruct extends AbstractFileReferenceWriter {
        @PostConstruct
        private void postConstruct() {
            assertNotNull(hc);
            this.files = new ArrayList<File>();
        }
    }

    @Provider
    public static class FileReferenceWriterPreDestroy extends FileReferenceWriterPostConstruct {
        @PreDestroy
        private void preDestroy() {
            assertNotNull(files);
            for (File f : files) {
                f.delete();
            }
        }
    }

    @Path("/")
    public static class FileTypeResource {
        @GET
        public FileType getFileName() {
            return new FileType();
        }
    }
    
    public void testProvider() {
        initiateWebApplication(FileReferenceWriter.class, FileTypeResource.class);
        WebResource r = resource("/");
        String s = r.get(String.class);
        File f = new File(s);
        assertTrue(f.exists());

        w.destroy();
        
        assertFalse(f.exists());
    }

    public void testProviderPrivate() {
        initiateWebApplication(FileReferenceWriterPrivate.class, FileTypeResource.class);
        WebResource r = resource("/");
        String s = r.get(String.class);
        File f = new File(s);
        assertTrue(f.exists());

        w.destroy();

        assertFalse(f.exists());
    }

    public void testProviderProtected() {
        initiateWebApplication(FileReferenceWriterProtected.class, FileTypeResource.class);
        WebResource r = resource("/");
        String s = r.get(String.class);
        File f = new File(s);
        assertTrue(f.exists());

        w.destroy();

        assertFalse(f.exists());
    }

    public void testProviderInherited() {
        initiateWebApplication(FileReferenceWriterPreDestroy.class, FileTypeResource.class);
        WebResource r = resource("/");
        String s = r.get(String.class);
        File f = new File(s);
        assertTrue(f.exists());

        w.destroy();

        assertFalse(f.exists());
    }
}
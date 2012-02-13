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
package com.sun.jersey.samples.viewclient.atompubsimple.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Service;

/**
 *
 * @author algermissen@acm.org
 */
@Provider
@Produces("application/atomsvc+xml")
public class AtomServiceProvider implements MessageBodyWriter,MessageBodyReader {

	private final static Abdera abdera = new Abdera();

   public static Abdera getAbdera() {
       return abdera;
   }


	public boolean isWriteable(Class cls, Type type, Annotation[] arg2, MediaType arg3) {
		return Service.class.isAssignableFrom(cls);
	}

	public long getSize(Object arg0, Class arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
		return -1;
	}

	public void writeTo(Object obj, Class arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap arg5, OutputStream outputStream) throws IOException, WebApplicationException {
		Service entry = (Service) obj;
		Document<Service> doc = entry.getDocument();
		doc.writeTo(outputStream);
	}

	public boolean isReadable(Class cls, Type type, Annotation[] arg2, MediaType arg3) {
		return cls.isAssignableFrom(Service.class);
	}

	public Object readFrom(Class arg0, Type arg1, Annotation[] arg2, MediaType arg3, MultivaluedMap arg4, InputStream inputStream) throws IOException, WebApplicationException {
		Document<Element> doc = getAbdera().getParser().parse(inputStream);
		return doc.getRoot();
	}
}

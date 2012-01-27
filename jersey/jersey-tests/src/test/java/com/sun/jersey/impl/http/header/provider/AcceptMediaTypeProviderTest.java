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

package com.sun.jersey.impl.http.header.provider;

import com.sun.jersey.core.header.AcceptableMediaType;
import com.sun.jersey.core.header.reader.HttpHeaderReader;
import junit.framework.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

public class AcceptMediaTypeProviderTest extends TestCase {
    
    public AcceptMediaTypeProviderTest(String testName) {
        super(testName);
    }

    public void testOneMediaType() throws Exception {
        String header = "application/xml";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);
        
        assertEquals(1, l.size());
        
        MediaType m = l.get(0);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
    }
    
    public void testOneMediaTypeWithParameters() throws Exception {
        String header = "application/xml;charset=utf8";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);
        
        assertEquals(1, l.size());
        
        MediaType m = l.get(0);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        assertTrue(m.getParameters().containsKey("charset"));
        assertEquals("utf8", m.getParameters().get("charset"));
    }
    
    public void testMultipleMediaType() throws Exception {
        String header = "application/xml, text/xml, text/html";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);
        
        assertEquals(3, l.size());
        
        MediaType m;
        m = l.get(0);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(0, m.getParameters().size());
    }
    
    public void testMultipleMediaTypeWithQuality() throws Exception {
        String header = "application/xml;q=0.1, text/xml;q=0.2, text/html;q=0.3";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);
        
        assertEquals(3, l.size());
        
        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(2);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }
    
    public void testHttpURLConnectionAcceptHeader() throws Exception {
        String header = "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);
        
        assertEquals(5, l.size());
        
        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("image", m.getType());
        assertEquals("gif", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("image", m.getType());
        assertEquals("jpeg", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(4);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }

    public void testFirefoxAcceptHeader() throws Exception {
        String header = "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);
        
        assertEquals(7, l.size());
        
        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("application", m.getType());
        assertEquals("xhtml+xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("image", m.getType());
        assertEquals("png", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(4);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(5);
        assertEquals("text", m.getType());
        assertEquals("plain", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(6);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }
    
    public void testWithStarAcceptHeader() throws Exception {
        String header = "application/xml;q=0.1, text/xml;q=0.2, *;q=0.3";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);
        
        assertEquals(3, l.size());
        
        MediaType m;
        m = l.get(0);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(2);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }
    
    public void testMediaTypeSpecifity() throws Exception {
        String header = "*/*, text/*, text/plain";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        assertEquals(3, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("plain", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
    }

    public void testMediaTypeSpecifityWithQuality() throws Exception {
        String header = "*/*, */*;q=0.5, text/*, text/*;q=0.5, text/plain, text/plain;q=0.5";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        assertEquals(6, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("plain", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("text", m.getType());
        assertEquals("plain", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(4);
        assertEquals("text", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(5);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }

    public void testMediaTypeSpecifityHTTPExample1() throws Exception {
        String header = "text/*, text/html, text/html;level=1, */*";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(2);
        assertEquals("text", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
    }

    public void testMediaTypeSpecifityHTTPExample2() throws Exception {
        String header = "text/*, text/html;level=1, text/html, */*";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header);

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("text", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(0, m.getParameters().size());
    }

    public void testHttpURLConnectionAcceptHeaderWithPrority() throws Exception {
        String header = "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";

        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header,
                HttpHeaderReader.readQualitySourceMediaType(MediaType.TEXT_HTML));

        assertEquals(5, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("image", m.getType());
        assertEquals("gif", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("image", m.getType());
        assertEquals("jpeg", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(4);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }

    public void testFirefoxAcceptHeaderWithPrority() throws Exception {
        String header = "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header,
                HttpHeaderReader.readQualitySourceMediaType("text/html;qs=2"));

        assertEquals(7, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("application", m.getType());
        assertEquals("xhtml+xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(4);
        assertEquals("image", m.getType());
        assertEquals("png", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(5);
        assertEquals("text", m.getType());
        assertEquals("plain", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(6);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }
    
    public void testFirefoxAcceptHeaderWithPrority2() throws Exception {
        String header = "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
        List<AcceptableMediaType> l = HttpHeaderReader.readAcceptMediaType(header,
                HttpHeaderReader.readQualitySourceMediaType(new String[]{"text/html;qs=2", "application/xml;qs=1.1"}));

        assertEquals(7, l.size());

        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(1);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("application", m.getType());
        assertEquals("xhtml+xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(4);
        assertEquals("image", m.getType());
        assertEquals("png", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(5);
        assertEquals("text", m.getType());
        assertEquals("plain", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(6);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }
}

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

package com.sun.jersey.impl.http.header;

import junit.framework.*;
import com.sun.jersey.core.impl.provider.header.CacheControlProvider;
import javax.ws.rs.core.CacheControl;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public class CacheControlImplTest extends TestCase {
    
    public CacheControlImplTest(String testName) {
        super(testName);
    }

    /**
     * Test of toString method, of class com.sun.jersey.api.response.CacheControl.
     */
    public void testToString() {
        CacheControlProvider p = new CacheControlProvider();
        CacheControl instance = new CacheControl();
        
        instance.setNoCache(true);
        String expResult = "no-cache, no-transform";
        String result = p.toString(instance);
        assertEquals(expResult, result);
        
        instance.setNoStore(true);
        expResult = "no-cache, no-store, no-transform";
        result = p.toString(instance);
        assertEquals(expResult, result);

        instance.setPrivate(true);
        expResult = "private, no-cache, no-store, no-transform";
        result = p.toString(instance);
        assertEquals(expResult, result);

        instance.getPrivateFields().add("Fred");
        expResult = "private=\"Fred\", no-cache, no-store, no-transform";
        result = p.toString(instance);
        assertEquals(expResult, result);
        instance.getPrivateFields().add("Bob");
        expResult = "private=\"Fred, Bob\", no-cache, no-store, no-transform";
        result = p.toString(instance);
        assertEquals(expResult, result);
        
        instance = new CacheControl();
        instance.getCacheExtension().put("key1","value1");
        expResult = "no-transform, key1=value1";
        result = p.toString(instance);
        assertEquals(expResult, result);
        instance.getCacheExtension().put("key1","value1 with spaces");
        expResult = "no-transform, key1=\"value1 with spaces\"";
        result = p.toString(instance);
        assertEquals(expResult, result);
        
        instance.setNoStore(true);
        expResult = "no-store, no-transform, key1=\"value1 with spaces\"";
        result = p.toString(instance);
        assertEquals(expResult, result);

        instance = new CacheControl();
        instance.getCacheExtension().put("key1",null);
        expResult = "no-transform, key1";
        result = p.toString(instance);
        assertEquals(expResult, result);
        
    }


    public void testRoundTrip() {
        _testRoundTrip("no-cache, no-transform");
        _testRoundTrip("no-cache, no-store, no-transform");
        _testRoundTrip("private, no-cache, no-store, no-transform");
        _testRoundTrip("private=\"Fred\", no-cache, no-store, no-transform");
        _testRoundTrip("private=\"Fred, Bob\", no-cache, no-store, no-transform");
        _testRoundTrip("no-transform, key1=value1");
        _testRoundTrip("no-transform, key1=\"value1 with spaces\"");
        _testRoundTrip("no-store, no-transform, key1=\"value1 with spaces\"");
        _testRoundTrip("no-transform, key1");
        _testRoundTrip("must-revalidate, proxy-revalidate");
        _testRoundTrip("max-age=1, s-maxage=1");
    }

    private void _testRoundTrip(String s) {
        CacheControlProvider p = new CacheControlProvider();

        CacheControl cc1 = p.fromString(s);
        CacheControl cc2 = p.fromString(cc1.toString());
        cc2.toString();

        cc1.equals(cc2);

        try {
            assertEquals(cc1, cc2);
        } catch (RuntimeException ex) {
            throw ex;
        }
    }
 }

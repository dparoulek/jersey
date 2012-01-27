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

package com.sun.jersey.multipart;

import javax.ws.rs.core.MediaType;
import junit.framework.TestCase;

/**
 * <p>Test case for {@link MultiPartImpl}.</p>
 */
public class MultiPartTest extends TestCase {
    
    public MultiPartTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        multiPart = new MultiPart();
    }

    @Override
    protected void tearDown() throws Exception {
        multiPart = null;
        super.tearDown();
    }

    protected MultiPart multiPart = null;

    @SuppressWarnings("empty-statement")
    public void testCreate() {
        if (multiPart instanceof FormDataMultiPart) {
            assertEquals("multipart/form-data", multiPart.getMediaType().toString());
            try {
                multiPart.setMediaType(new MediaType("multipart", "foo"));
                fail("Should have thrown IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // Expected result
            }
            multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
        } else {
            assertEquals("multipart/mixed", multiPart.getMediaType().toString());
            multiPart.setMediaType(new MediaType("multipart", "alternative"));
            assertEquals("multipart/alternative", multiPart.getMediaType().toString());
            try {
                multiPart.setMediaType(new MediaType("text", "xml"));
                fail("Should have thrown IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                ; // Expected result
            }
        }
    }

    @SuppressWarnings("empty-statement")
    public void testEntity() {
        try {
            multiPart.setEntity("foo bar baz");
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            ; // Expected result
        }
        try {
            assertEquals("foo bar baz", multiPart.getEntity());
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            ; // Expected result
        }
    }

}

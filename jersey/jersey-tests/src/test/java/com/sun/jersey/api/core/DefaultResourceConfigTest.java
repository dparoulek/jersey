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
package com.sun.jersey.api.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.MediaType;


/**
 * @author Paul.Sandoz@Sun.Com
 */
public class DefaultResourceConfigTest extends AbstractResourceConfigOrderTest {

    public void testClasses() {
        DefaultResourceConfig rc = new DefaultResourceConfig(
                LIST.toArray(new Class<?>[0]));

        assertEquals(LIST, new ArrayList(rc.getClasses()));
    }

    public void testSetClasses() {
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>(LIST);
        DefaultResourceConfig rc = new DefaultResourceConfig(classes);

        assertEquals(LIST, new ArrayList(rc.getClasses()));
    }

    public void testSingletons() {
        List l = getList(Arrays.asList(new One(), new Two(), new Three()));
        DefaultResourceConfig rc = new DefaultResourceConfig();
        rc.getSingletons().addAll(l);

        assertEquals(l, new ArrayList(rc.getSingletons()));
    }

    public static class Four {
    }

    public static class Five {
    }

    public static class Six {
    }

    public void testClone() {
        List rcSingletons = getList(Arrays.asList(new Four(), new Five(), new Six()));
        List<Class<?>> rcClasses = getList(Arrays.asList(Four.class, Five.class, Six.class));
        DefaultResourceConfig rc = new DefaultResourceConfig(
                rcClasses.toArray(new Class<?>[0]));
        rc.getSingletons().addAll(rcSingletons);
        rc.getMediaTypeMappings().put("xml", MediaType.APPLICATION_XML_TYPE);
        rc.getLanguageMappings().put("en", "en");
        rc.getExplicitRootResources().put("{test}", new One());
        rc.getProperties().put("X", "X");
        rc.getFeatures().put("X", Boolean.TRUE);
        
        ResourceConfig clone = rc.clone();

        assertEquals(rcClasses, new ArrayList(clone.getClasses()));

        assertEquals(rcSingletons, new ArrayList(clone.getSingletons()));

        assertEquals(MediaType.APPLICATION_XML_TYPE, clone.getMediaTypeMappings().get("xml"));

        assertEquals("en", clone.getLanguageMappings().get("en"));

        assertEquals(One.class, clone.getExplicitRootResources().get("{test}").getClass());

        assertEquals("X", rc.getProperties().get("X"));

        assertTrue(rc.getFeatures().get("X"));
    }

    public void testAdd() {
        List rc1Singletons = getList(Arrays.asList(new One(), new Two(), new Three()));
        DefaultResourceConfig rc1 = new DefaultResourceConfig(
                LIST.toArray(new Class<?>[0]));
        rc1.getSingletons().addAll(rc1Singletons);

        List rc2Singletons = getList(Arrays.asList(new Four(), new Five(), new Six()));
        List<Class<?>> rc2Classes = getList(Arrays.asList(Four.class, Five.class, Six.class));
        DefaultResourceConfig rc2 = new DefaultResourceConfig(
                rc2Classes.toArray(new Class<?>[0]));
        rc2.getSingletons().addAll(rc2Singletons);
        rc2.getMediaTypeMappings().put("xml", MediaType.APPLICATION_XML_TYPE);
        rc2.getLanguageMappings().put("en", "en");
        rc2.getExplicitRootResources().put("{test}", new One());
        rc2.getProperties().put("X", "X");
        rc2.getFeatures().put("X", Boolean.TRUE);

        rc1.add(rc2);


        List<Class<?>> classes = new ArrayList<Class<?>>(rc2Classes);
        classes.addAll(LIST);
        assertEquals(classes, new ArrayList(rc1.getClasses()));

        List singletons = new ArrayList(rc2Singletons);
        singletons.addAll(rc1Singletons);
        assertEquals(singletons, new ArrayList(rc1.getSingletons()));

        assertEquals(MediaType.APPLICATION_XML_TYPE, rc1.getMediaTypeMappings().get("xml"));

        assertEquals("en", rc1.getLanguageMappings().get("en"));

        assertEquals(One.class, rc1.getExplicitRootResources().get("{test}").getClass());

        assertEquals("X", rc1.getProperties().get("X"));

        assertTrue(rc1.getFeatures().get("X"));
    }

    public void testMediaTypeMapSingleString() {
        DefaultResourceConfig rc1 = new DefaultResourceConfig();
        Map<String, Object> propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_MEDIA_TYPE_MAPPINGS, "text:text/plain");

        rc1.setPropertiesAndFeatures(propertyMap);
        rc1.validate();
        
        assertNotNull(rc1.getMediaTypeMappings().get("text"));
        assertEquals(MediaType.TEXT_PLAIN_TYPE, rc1.getMediaTypeMappings().get("text"));
    }

    public void testMediaTypeMapSingleStringNullValue() {
        DefaultResourceConfig rc1 = new DefaultResourceConfig();
        Map<String, Object> propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_MEDIA_TYPE_MAPPINGS, null);

        rc1.setPropertiesAndFeatures(propertyMap);
        rc1.validate();

        assertTrue(rc1.getMediaTypeMappings().isEmpty());
    }

    public void testMediaTypeMapMultipleString() {
        DefaultResourceConfig rc1 = new DefaultResourceConfig();
        Map<String, Object> propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_MEDIA_TYPE_MAPPINGS, "text:text/plain,xml:application/xml");

        rc1.setPropertiesAndFeatures(propertyMap);
        rc1.validate();
        
        assertEquals(MediaType.TEXT_PLAIN_TYPE, rc1.getMediaTypeMappings().get("text"));
        assertEquals(MediaType.APPLICATION_XML_TYPE, rc1.getMediaTypeMappings().get("xml"));
    }

    public void testMediaTypeMapMultipleStringArray() {
        DefaultResourceConfig rc1 = new DefaultResourceConfig();
        Map<String, Object> propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_MEDIA_TYPE_MAPPINGS, new String[] {"text:text/plain", "xml:application/xml"});

        rc1.setPropertiesAndFeatures(propertyMap);
        rc1.validate();
        
        assertEquals(MediaType.TEXT_PLAIN_TYPE, rc1.getMediaTypeMappings().get("text"));
        assertEquals(MediaType.APPLICATION_XML_TYPE, rc1.getMediaTypeMappings().get("xml"));
    }

    public void testMediaTypeMapStringArrayNullValues() {
        DefaultResourceConfig rc1 = new DefaultResourceConfig();
        Map<String, Object> propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_MEDIA_TYPE_MAPPINGS, new String[] {null, "text:text/plain", "xml:application/xml"});

        rc1.setPropertiesAndFeatures(propertyMap);
        rc1.validate();

        assertEquals(MediaType.TEXT_PLAIN_TYPE, rc1.getMediaTypeMappings().get("text"));
        assertEquals(MediaType.APPLICATION_XML_TYPE, rc1.getMediaTypeMappings().get("xml"));
    }

    public void testMediaTypeMapInvalid() {
        boolean caught = false;
        DefaultResourceConfig rc1 = new DefaultResourceConfig();
        Map<String, Object> propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_MEDIA_TYPE_MAPPINGS, "text");

        rc1.setPropertiesAndFeatures(propertyMap);
        try {
            rc1.validate();
        } catch (IllegalArgumentException iae) {
            caught = true;
        }

        assertTrue(caught);

        caught = false;
        rc1 = new DefaultResourceConfig();
        propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_MEDIA_TYPE_MAPPINGS, "text/plain");

        rc1.setPropertiesAndFeatures(propertyMap);
        try {
            rc1.validate();
        } catch (IllegalArgumentException iae) {
            caught = true;
        }

        assertTrue(caught);

        caught = false;
        rc1 = new DefaultResourceConfig();
        propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_MEDIA_TYPE_MAPPINGS, ":text/plain");

        rc1.setPropertiesAndFeatures(propertyMap);
        try {
            rc1.validate();
        } catch (IllegalArgumentException iae) {
            caught = true;
        }

        assertTrue(caught);

        caught = false;
        rc1 = new DefaultResourceConfig();
        propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_MEDIA_TYPE_MAPPINGS, "text/plain:");

        rc1.setPropertiesAndFeatures(propertyMap);
        try {
            rc1.validate();
        } catch (IllegalArgumentException iae) {
            caught = true;
        }

        assertTrue(caught);

        caught = false;
        rc1 = new DefaultResourceConfig();
        propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_MEDIA_TYPE_MAPPINGS, "text:invalid/foo/bar");

        rc1.setPropertiesAndFeatures(propertyMap);
        try {
            rc1.validate();
        } catch (IllegalArgumentException iae) {
            caught = true;
        }

        assertTrue(caught);
    }


    public void testLanguageMapSingleString() {
        DefaultResourceConfig rc1 = new DefaultResourceConfig();
        Map<String, Object> propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_LANGUAGE_MAPPINGS, "english:en");

        rc1.setPropertiesAndFeatures(propertyMap);
        rc1.validate();

        assertNotNull(rc1.getLanguageMappings().get("english"));
        assertEquals("en", rc1.getLanguageMappings().get("english"));
    }

    public void testLangaugeMapSingleStringNullValue() {
        DefaultResourceConfig rc1 = new DefaultResourceConfig();
        Map<String, Object> propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_LANGUAGE_MAPPINGS, null);

        rc1.setPropertiesAndFeatures(propertyMap);
        rc1.validate();

        assertTrue(rc1.getMediaTypeMappings().isEmpty());
    }

    public void testLanguageMapMultipleString() {
        DefaultResourceConfig rc1 = new DefaultResourceConfig();
        Map<String, Object> propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_LANGUAGE_MAPPINGS, "english:en, czech:cz");

        rc1.setPropertiesAndFeatures(propertyMap);
        rc1.validate();

        assertEquals("en", rc1.getLanguageMappings().get("english"));
        assertEquals("cz", rc1.getLanguageMappings().get("czech"));
    }

    public void testLanguageMapMultipleStringArray() {
        DefaultResourceConfig rc1 = new DefaultResourceConfig();
        Map<String, Object> propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_LANGUAGE_MAPPINGS, new String[] {"english:en", "czech:cz"});

        rc1.setPropertiesAndFeatures(propertyMap);
        rc1.validate();

        assertEquals("en", rc1.getLanguageMappings().get("english"));
        assertEquals("cz", rc1.getLanguageMappings().get("czech"));
    }

    public void testLanguageMapMultipleStringArrayNullValues() {
        DefaultResourceConfig rc1 = new DefaultResourceConfig();
        Map<String, Object> propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_LANGUAGE_MAPPINGS, new String[] {null, "english:en", "czech:cz"});

        rc1.setPropertiesAndFeatures(propertyMap);
        rc1.validate();

        assertEquals("en", rc1.getLanguageMappings().get("english"));
        assertEquals("cz", rc1.getLanguageMappings().get("czech"));
    }

    public void testLanguageInvalid() {
        boolean caught = false;
        DefaultResourceConfig rc1 = new DefaultResourceConfig();
        Map<String, Object> propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_LANGUAGE_MAPPINGS, "english");

        rc1.setPropertiesAndFeatures(propertyMap);
        try {
            rc1.validate();
        } catch (IllegalArgumentException iae) {
            caught = true;
        }

        assertTrue(caught);

        caught = false;
        rc1 = new DefaultResourceConfig();
        propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_LANGUAGE_MAPPINGS, ":cz");

        rc1.setPropertiesAndFeatures(propertyMap);
        try {
            rc1.validate();
        } catch (IllegalArgumentException iae) {
            caught = true;
        }

        assertTrue(caught);

        caught = false;
        rc1 = new DefaultResourceConfig();
        propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_LANGUAGE_MAPPINGS, "english:");

        rc1.setPropertiesAndFeatures(propertyMap);
        try {
            rc1.validate();
        } catch (IllegalArgumentException iae) {
            caught = true;
        }

        assertTrue(caught);

        caught = false;
        rc1 = new DefaultResourceConfig();
        propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_LANGUAGE_MAPPINGS, "english:aa-");

        rc1.setPropertiesAndFeatures(propertyMap);
        try {
            rc1.validate();
        } catch (IllegalArgumentException iae) {
            caught = true;
        }

        assertTrue(caught);

        caught = false;
        rc1 = new DefaultResourceConfig();
        propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_LANGUAGE_MAPPINGS, "english:-aa");

        rc1.setPropertiesAndFeatures(propertyMap);
        try {
            rc1.validate();
        } catch (IllegalArgumentException iae) {
            caught = true;
        }

        assertTrue(caught);

        caught = false;
        rc1 = new DefaultResourceConfig();
        propertyMap = new HashMap<String, Object>();
        propertyMap.put(ResourceConfig.PROPERTY_LANGUAGE_MAPPINGS, "english:aabbccddee-aabbccddeef"); // second part has more than 8 characters

        rc1.setPropertiesAndFeatures(propertyMap);
        try {
            rc1.validate();
        } catch (IllegalArgumentException iae) {
            caught = true;
        }

        assertTrue(caught);
    }
}
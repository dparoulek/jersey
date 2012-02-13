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
package com.sun.jersey.server.impl.model.parameter.multivalued;

import com.sun.jersey.spi.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
abstract class CollectionStringReaderExtractor<V extends Collection>
        extends AbstractStringReaderExtractor {

    protected CollectionStringReaderExtractor(StringReader sr, String parameter, String defaultStringValue) {
        super(sr, parameter, defaultStringValue);
    }

    public Object extract(MultivaluedMap<String, String> parameters) {
        final List<String> stringList = parameters.get(parameter);
        if (stringList != null) {
            final V valueList = getInstance();
            for (String v : stringList) {
                valueList.add((v.length() == 0) ? null : sr.fromString(v));
            }

            return valueList;
        } else if (defaultStringValue != null) {
            final V valueList = getInstance();
            valueList.add(sr.fromString(defaultStringValue));
            return valueList;
        } else {
            return getInstance();
        }
    }

    protected abstract V getInstance();

    private static final class ListValueOf extends CollectionStringReaderExtractor<List> {

        ListValueOf(StringReader sr, String parameter, String defaultValueString) {
            super(sr, parameter, defaultValueString);
        }

        protected List getInstance() {
            return new ArrayList();
        }
    }

    private static final class SetValueOf extends CollectionStringReaderExtractor<Set> {

        SetValueOf(StringReader sr, String parameter, String defaultValueString) {
            super(sr, parameter, defaultValueString);
        }

        protected Set getInstance() {
            return new HashSet();
        }
    }

    private static final class SortedSetValueOf extends CollectionStringReaderExtractor<SortedSet> {

        SortedSetValueOf(StringReader sr, String parameter, String defaultValueString) {
            super(sr, parameter, defaultValueString);
        }

        protected SortedSet getInstance() {
            return new TreeSet();
        }
    }

    static MultivaluedParameterExtractor getInstance(Class c,
            StringReader sr, String parameter, String defaultValueString) {
        if (List.class == c) {
            return new ListValueOf(sr, parameter, defaultValueString);
        } else if (Set.class == c) {
            return new SetValueOf(sr, parameter, defaultValueString);
        } else if (SortedSet.class == c) {
            return new SortedSetValueOf(sr, parameter, defaultValueString);
        } else {
            throw new RuntimeException();
        }
    }
}

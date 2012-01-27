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

package com.sun.jersey.server.impl.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;

/**
 * Implements the CDI AnnotatedParameter interface.
 *
 * @author robc
 */
public class AnnotatedParameterImpl<T> extends AnnotatedImpl implements AnnotatedParameter<T> {

    private AnnotatedCallable<T> declaringCallable;
    private int position;

    public AnnotatedParameterImpl(Type baseType,
                           Set<Type> typeClosure,
                           Set<Annotation> annotations,
                           AnnotatedCallable<T> declaringCallable,
                           int position) {
        super(baseType, typeClosure, annotations);
        this.declaringCallable = declaringCallable;
        this.position = position;
    }

    public AnnotatedParameterImpl(AnnotatedParameter<? super T> param, AnnotatedCallable<T> declaringCallable) {
        this(param.getBaseType(),
             param.getTypeClosure(),
             param.getAnnotations(),
             declaringCallable,
             param.getPosition());
    }

    public AnnotatedParameterImpl(AnnotatedParameter<? super T> param, Set<Annotation> annotations, AnnotatedCallable<T> declaringCallable) {
        this(param.getBaseType(),
             param.getTypeClosure(),
             annotations,
             declaringCallable,
             param.getPosition());
    }

    public AnnotatedCallable<T> getDeclaringCallable() {
        return declaringCallable;
    }

    public int getPosition() {
        return position;
    }
}

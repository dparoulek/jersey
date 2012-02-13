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

package com.sun.jersey.server.impl.model.method.dispatch;

import com.sun.jersey.spi.container.JavaMethodInvokerFactory;
import com.sun.jersey.spi.container.JavaMethodInvoker;
import com.sun.jersey.spi.container.ResourceMethodCustomInvokerDispatchProvider;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import java.lang.reflect.InvocationTargetException;

/**
 * A provider for methods that are marked as void and receiving no args.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class VoidVoidDispatchProvider implements ResourceMethodDispatchProvider, ResourceMethodCustomInvokerDispatchProvider {
    
    public static final class VoidVoidMethodInvoker extends ResourceJavaMethodDispatcher {
        public VoidVoidMethodInvoker(AbstractResourceMethod abstractResourceMethod) {
            this(abstractResourceMethod, JavaMethodInvokerFactory.getDefault());
        }

        public VoidVoidMethodInvoker(AbstractResourceMethod abstractResourceMethod, JavaMethodInvoker invoker) {
            super(abstractResourceMethod, invoker);
        }

        @Override
        public void _dispatch(Object resource, HttpContext context) 
        throws IllegalAccessException, InvocationTargetException {
            invoker.invoke(method, resource);
        }
    }
    

    @Override
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        return this.create(abstractResourceMethod, JavaMethodInvokerFactory.getDefault());
    }

    @Override
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod, JavaMethodInvoker invoker) {
        if (!abstractResourceMethod.getParameters().isEmpty()) return null;
        if (abstractResourceMethod.getReturnType() != void.class) return null;
        
        return new VoidVoidMethodInvoker(abstractResourceMethod, invoker);
    }
}

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

package com.sun.jersey.server.impl.resource;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCDestroyable;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCProxiedComponentProvider;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.server.spi.component.ResourceComponentConstructor;
import com.sun.jersey.server.spi.component.ResourceComponentDestructor;
import com.sun.jersey.server.spi.component.ResourceComponentInjector;
import com.sun.jersey.server.spi.component.ResourceComponentProvider;
import com.sun.jersey.server.spi.component.ResourceComponentProviderFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;

/**
 * A simple provider that maintains a singleton resource class instance
 */
public final class SingletonFactory implements ResourceComponentProviderFactory  {
    private static final Logger LOGGER = Logger.getLogger(SingletonFactory.class.getName());

    private final ServerInjectableProviderContext sipc;

    public SingletonFactory(
            @Context ServerInjectableProviderContext sipc) {
        this.sipc = sipc;
    }

    public ComponentScope getScope(Class c) {
        return ComponentScope.Singleton;
    }

    public ResourceComponentProvider getComponentProvider(Class c) {
        return new Singleton();
    }
    
    public ResourceComponentProvider getComponentProvider(IoCComponentProvider icp, Class c) {
        if (icp instanceof IoCInstantiatedComponentProvider) {
            return new SingletonInstantiated((IoCInstantiatedComponentProvider)icp);
        } else if (icp instanceof IoCProxiedComponentProvider) {
            return new SingletonProxied((IoCProxiedComponentProvider)icp);
        }
        throw new IllegalStateException();
    }

    private abstract class AbstractSingleton implements ResourceComponentProvider {
        private ResourceComponentDestructor rcd;

        protected Object resource;
        
        public void init(AbstractResource abstractResource) {
            rcd = new ResourceComponentDestructor(abstractResource);
        }
        
        public final Object getInstance(HttpContext hc) {
            return resource;
        }

        public final Object getInstance() {
            return resource;
        }

        public final ComponentScope getScope() {
            return ComponentScope.Singleton;
        }
        
        public void destroy() {
            try {
                rcd.destroy(resource);
            } catch (IllegalAccessException ex) {
                LOGGER.log(Level.SEVERE, "Unable to destroy resource", ex);
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.SEVERE, "Unable to destroy resource", ex);
            } catch (InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, "Unable to destroy resource", ex);
            }
        }
    }

    private class Singleton extends AbstractSingleton {
        @Override
        public void init(AbstractResource abstractResource) {
            super.init(abstractResource);

            ResourceComponentConstructor rcc = new ResourceComponentConstructor(
                    sipc,
                    ComponentScope.Singleton,
                    abstractResource);

            try {
                this.resource = rcc.construct(null);
            } catch (InvocationTargetException ex) {
                throw new ContainerException("Unable to create resource", ex);
            } catch (InstantiationException ex) {
                throw new ContainerException("Unable to create resource", ex);
            } catch (IllegalAccessException ex) {
                throw new ContainerException("Unable to create resource", ex);
            }
        }
    }

    private class SingletonInstantiated extends AbstractSingleton {
        private final IoCInstantiatedComponentProvider iicp;
        
        private final IoCDestroyable destroyable;

        SingletonInstantiated(IoCInstantiatedComponentProvider iicp) {
            this.iicp = iicp;
            this.destroyable = (iicp instanceof IoCDestroyable)
                    ? (IoCDestroyable) iicp : null;
        }

        @Override
        public void init(AbstractResource abstractResource) {
            super.init(abstractResource);
            
            resource = iicp.getInstance();

            if (destroyable == null) {
                ResourceComponentInjector rci = new ResourceComponentInjector(
                        sipc,
                        ComponentScope.Singleton,
                        abstractResource);

                rci.inject(null, iicp.getInjectableInstance(resource));
            }
        }

        @Override
        public void destroy() {
            if (destroyable != null) {
                destroyable.destroy(resource);
            } else {
                super.destroy();
            }
        }
    }

    private class SingletonProxied extends AbstractSingleton {
        private final IoCProxiedComponentProvider ipcp;

        SingletonProxied(IoCProxiedComponentProvider ipcp) {
            this.ipcp = ipcp;
        }

        @Override
        public void init(AbstractResource abstractResource) {
            super.init(abstractResource);

            ResourceComponentConstructor rcc = new ResourceComponentConstructor(
                    sipc,
                    ComponentScope.Singleton,
                    abstractResource);

            try {
                Object o = rcc.construct(null);
                resource = ipcp.proxy(o);
            } catch (InvocationTargetException ex) {
                throw new ContainerException("Unable to create resource", ex);
            } catch (InstantiationException ex) {
                throw new ContainerException("Unable to create resource", ex);
            } catch (IllegalAccessException ex) {
                throw new ContainerException("Unable to create resource", ex);
            }
        }
    }
}
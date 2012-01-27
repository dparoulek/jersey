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

package com.sun.jersey.server.impl.model.method;

import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ResourceMethod {
    
    /**
     * Comparator for resource methods, comparing the consumed and produced
     * media types.
     * <p>
     * Defer to {@link MediaTypes#MEDIA_TYPE_LIST_COMPARATOR} for comparing
     * the list of media type that are comsumed and produced. The comparison of 
     * consumed media take precedence over the comparison of produced 
     * media.
     */
    static public final Comparator<ResourceMethod> COMPARATOR = new Comparator<ResourceMethod>() {
        @Override
        public int compare(ResourceMethod o1, ResourceMethod o2) {
            int i = MediaTypes.MEDIA_TYPE_LIST_COMPARATOR.
                    compare(o1.consumeMime, o2.consumeMime);
            if (i == 0)
                i = MediaTypes.MEDIA_TYPE_LIST_COMPARATOR.
                        compare(o1.produceMime, o2.produceMime);
            
            return i;
        }
    };
    
    private final String httpMethod;
    
    private final UriTemplate template;
    
    private final List<? extends MediaType> consumeMime;
    
    private final List<? extends MediaType> produceMime;

    private final boolean isProducesDeclared;

    private final RequestDispatcher dispatcher;
    
    private final List<ContainerRequestFilter> requestFilters;

    private final List<ContainerResponseFilter> responseFilters;

    public ResourceMethod(String httpMethod,
            UriTemplate template,
            List<? extends MediaType> consumeMime,
            List<? extends MediaType> produceMime,
            boolean isProducesDeclared,
            RequestDispatcher dispatcher) {
        this(httpMethod, template, consumeMime, produceMime, isProducesDeclared,
                dispatcher, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    public ResourceMethod(String httpMethod,
            UriTemplate template,
            List<? extends MediaType> consumeMime,
            List<? extends MediaType> produceMime,
            boolean isProducesDeclared,
            RequestDispatcher dispatcher,
            List<ContainerRequestFilter> requestFilters,
            List<ContainerResponseFilter> responseFilters) {
        this.httpMethod = httpMethod;
        this.template = template;
        this.consumeMime = consumeMime;
        this.produceMime = produceMime;
        this.isProducesDeclared = isProducesDeclared;
        this.dispatcher = dispatcher;
        this.requestFilters = requestFilters;
        this.responseFilters = responseFilters;
    }

    public final String getHttpMethod() {
        return httpMethod;
    }
    
    public final UriTemplate getTemplate() {
        return template;
    }
    
    public final List<? extends MediaType> getConsumes() {
        return consumeMime;
    }
    
    public final List<? extends MediaType> getProduces() {
        return produceMime;
    }

    public final boolean isProducesDeclared() {
        return isProducesDeclared;
    }
    
    public final RequestDispatcher getDispatcher() {
        return dispatcher;
    }

    public final List<ContainerRequestFilter> getRequestFilters() {
        return requestFilters;
    }

    public final List<ContainerResponseFilter> getResponseFilters() {
        return responseFilters;
    }
    
     /**
     * Ascertain if the method is capable of consuming an entity of a certain 
     * media type.
     *
     * @param contentType the media type of the entity that is to be consumed.
     * @return true if the method is capable of consuming the entity,
      *        otherwise false.
     */
    public final boolean consumes(MediaType contentType) {
        for (MediaType c : consumeMime) {
            if (c.getType().equals("*")) return true;
            
            if (contentType.isCompatible(c)) return true;
        }
        
        return false;
    }
        
    public final boolean consumesWild() {
        for (MediaType c : consumeMime) {
            if (c.getType().equals("*")) return true;
        }

        return false;
    }

    public final boolean mediaEquals(ResourceMethod that) {
        boolean v = consumeMime.equals(that.consumeMime);
        if (v == false)
            return false;
        
        return produceMime.equals(that.produceMime);
    }

    /**
     * Get the abstract resource method.
     * <p>
     * Extending classes may override this method to return an associated
     * abstract resource method.
     * 
     * @return the abstract resource method, otherwise null if there is no
     *         abstract resource method assocaiated with the resource method.
     */
    public AbstractResourceMethod getAbstractResourceMethod() {
        return null;
    }
}
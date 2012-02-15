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

package com.sun.jersey.server.linking.impl;

import com.sun.jersey.core.header.LinkHeader;
import com.sun.jersey.core.header.LinkHeader.LinkHeaderBuilder;
import com.sun.jersey.server.linking.Link;
import com.sun.jersey.server.linking.el.LinkBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

/**
 * Processes @Link and @LinkHeaders annotations on entity classes and
 * adds appropriate HTTP Link headers.
 * @author mh124079
 */
public class LinkProcessor<T> {
    private EntityDescriptor instanceDescriptor;

    public LinkProcessor(Class<T> c) {
        instanceDescriptor = EntityDescriptor.getInstance(c);
    }

    /**
     * Process any {@link Link} annotations on the supplied entity.
     * @param entity the entity object returned by the resource method
     * @param uriInfo the uriInfo for the request
     * @param headers the map into which the headers will be added
     */
    public void processLinkHeaders(T entity, UriInfo uriInfo, MultivaluedMap<String, Object> headers) {
        List<String> headerValues = getLinkHeaderValues(entity, uriInfo);
        for (String headerValue: headerValues) {
            headers.add("Link", headerValue);
        }
    }

    List<String> getLinkHeaderValues(Object entity, UriInfo uriInfo) {
        Object resource = uriInfo.getMatchedResources().get(0);
        List<String> headerValues = new ArrayList<String>();
        for (LinkDescriptor desc: instanceDescriptor.getLinkHeaders()) {
            if (LinkBuilder.evaluateCondition(desc.getCondition(), entity, resource, entity)) {
                String headerValue = getLinkHeaderValue(desc, entity, resource, uriInfo);
                headerValues.add(headerValue);
            }
        }
        return headerValues;
    }

    static String getLinkHeaderValue(LinkDescriptor desc, Object entity, Object resource, UriInfo uriInfo) {
        URI uri = LinkBuilder.buildURI(desc, entity, resource, entity, uriInfo);
        Link header = desc.getLinkHeader();
        LinkHeaderBuilder builder = LinkHeader.uri(uri);
        if (header.rel().length() != 0)
            builder = builder.rel(header.rel());
        if (header.rev().length() != 0)
            builder = builder.parameter("rev", header.rev());
        if (header.type().length() != 0)
            builder = builder.type(MediaType.valueOf(header.type()));
        if (header.title().length() != 0)
            builder = builder.parameter("title", header.title());
        if (header.anchor().length() != 0)
            builder = builder.parameter("anchor", header.anchor());
        if (header.media().length() != 0)
            builder = builder.parameter("media", header.media());
        if (header.hreflang().length() != 0)
            builder = builder.parameter("hreflang", header.hreflang());
        for (Link.Extension ext: header.extensions()) {
            builder = builder.parameter(ext.name(), ext.value());
        }
        return builder.build().toString();
    }

}

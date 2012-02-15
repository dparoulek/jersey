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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

/**
 * An abstraction of a HTTP response.
 * <p>
 * The default state is a HTTP response with a status code of 204 
 * (No Content) with no headers and entity.
 */
public interface HttpResponseContext {

    /**
     * Get the response that was set.
     * 
     * @return the response.
     */
    Response getResponse();
    
    /**
     * Set the response state from a Response instance. This replaces a 
     * pre-existing response state.
     *
     * @param response the response.
     */
    void setResponse(Response response);
    
    /**
     * Check if the response has been set using the setReponse methods.
     * 
     * @return true if the response has been set.
     */
    boolean isResponseSet();
    
    /**
     * Get the throwable (if any) that was mapped to a response.
     * 
     * @return the throwable that was mapped to a response, otherwise null
     *         if no throwable was mapped to a response.
     */
    Throwable getMappedThrowable();

    /**
     * @return the status type of the response
     */
    StatusType getStatusType();

    /**
     * Set the status type of the response.
     * @param statusType the status type.
     */
    void setStatusType(StatusType statusType);

    /**
     * @return the status of the response
     */
    int getStatus();
    
    /**
     * Set the status of the response.
     * @param status the status.
     */
    void setStatus(int status);
    
    /**
     * @return the entity of the response.
     */
    Object getEntity();
    
    /**
     * 
     * @return the type of the entity.
     */
    Type getEntityType();
    
    /**
     * Get the original entity instance that was set by
     * {@link #setEntity(java.lang.Object)}.
     */
    Object getOriginalEntity();

    /**
     * Set the entity of the response.
     * <p>
     * If the entity is an instance of {@link GenericEntity} then the entity
     * and entity type are set from the entity and type of that
     * {@link GenericEntity}. Otherwise, the entity is set from the entity 
     * parameter and the type is the class of that parameter.
     * <p>
     * If it is necessary to wrap an entity that may have been set with an
     * instance of {@link GenericEntity} then utilize the
     * {@link #getOriginalEntity() }, for example:
     * <blockquote><pre>
     *     HttpResponseContext r = ...
     *     r.setEntity(wrap(getOriginalEntity()));
     * </blockquote></pre>
     * 
     * @param entity the entity. 
     */
    void setEntity(Object entity);

    /**
     * Get the annotations associated with the response entity (if any).
     *
     * @return the annotations.
     */
    Annotation[] getAnnotations();

    /**
     * Set the annotations associated with the response entity (if any).
     * 
     * @param annotations the annotations.
     */
    void setAnnotations(Annotation[] annotations);

    /**
     * Get the HTTP response headers. The returned map is case-insensitive
     * with respect to the keys (header values). The method {@link #setResponse} 
     * will replace any headers previously set.
     *
     * @return a mutable map of headerd.
     */
    MultivaluedMap<String, Object> getHttpHeaders();

    /**
     * Get the media type of the response entity.
     *
     * @return the media type or null if there is no response entity.
     */
    MediaType getMediaType();

    /**
     * Get an {@link OutputStream} to which an entity may be written.
     * <p>
     * The first byte written will result in the writing of thethe status code 
     * and headers.
     *
     * @return the output stream
     * @throws java.io.IOException if an IO error occurs
     */
    OutputStream getOutputStream() throws IOException;
    
    /**
     * Ascertain if a response has been committed to the container.
     * <p>
     * A response is committed if the status code, headers have been
     * written to the container.
     *  
     * @return true if the response has been committed.
     */
    boolean isCommitted();
}
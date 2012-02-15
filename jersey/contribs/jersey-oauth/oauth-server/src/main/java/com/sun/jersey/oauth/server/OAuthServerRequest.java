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

package com.sun.jersey.oauth.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MultivaluedMap;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.oauth.signature.OAuthRequest;

/**
 * Wraps a Jersey {@link HttpRequestContext} object, implementing the
 * OAuth signature library {@link OAuthRequest} interface.
 *
 * @author Hubert A. Le Van Gong <hubert.levangong at Sun.COM>
 * @author Paul C. Bryan <pbryan@sun.com>
 */
public class OAuthServerRequest implements OAuthRequest {

    private HttpRequestContext context;

    private static HashSet<String> EMPTY_SET = new HashSet<String>();

    private static ArrayList<String> EMPTY_LIST = new ArrayList<String>();

    public OAuthServerRequest(HttpRequestContext context) {
        this.context = context;
    }

    @Override
    public String getRequestMethod() {
        return context.getMethod();
    }

    @Override
    public URL getRequestURL() {
        try {
            return context.getRequestUri().toURL();
        } catch (MalformedURLException ex) {
            Logger.getLogger(OAuthServerRequest.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private static Set<String> keys(MultivaluedMap<String, String> mvm) {
        if (mvm == null) {
            return EMPTY_SET;
        }
        Set<String> s = mvm.keySet();
        if (s == null) {
            return EMPTY_SET;
        }
        return s;
    }

    private static List<String> values(MultivaluedMap<String, String> mvm, String key) {
        if (mvm == null) {
            return EMPTY_LIST;
        }
        List<String> v = mvm.get(key);
        if (v == null) {
            return EMPTY_LIST;
        }
        return v;
    }

    @Override
    public Set<String> getParameterNames() {
        HashSet<String> n = new HashSet<String>();
        n.addAll(keys(context.getQueryParameters()));
        n.addAll(keys(context.getFormParameters()));
        return n;
    }

    @Override
    public List<String> getParameterValues(String name) {
        ArrayList<String> v = new ArrayList<String>();
        v.addAll(values(context.getQueryParameters(), name));
        v.addAll(values(context.getFormParameters(), name));
        return v;
    }

    @Override
    public List<String> getHeaderValues(String name) {
        return context.getRequestHeader(name);
    }

    @Override
    public void addHeaderValue(String name, String value) throws IllegalStateException {
        throw new IllegalStateException("Modifying OAuthServerRequest unsupported");
    }

}

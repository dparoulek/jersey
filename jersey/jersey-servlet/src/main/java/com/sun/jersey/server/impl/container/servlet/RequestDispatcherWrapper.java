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

package com.sun.jersey.server.impl.container.servlet;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.view.Viewable;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class RequestDispatcherWrapper implements RequestDispatcher {
    private final RequestDispatcher d;

    private final String basePath;

    private final HttpContext hc;

    private final Viewable v;

    public RequestDispatcherWrapper(RequestDispatcher d, String basePath, HttpContext hc, Viewable v) {
        this.d = d;
        this.basePath = basePath;
        this.hc = hc;
        this.v = v;
    }

    public void forward(ServletRequest req, ServletResponse rsp) throws ServletException, IOException {
        final Object oldIt = req.getAttribute("it");
        final Object oldResolvingClass = req.getAttribute("resolvingClass");

        req.setAttribute("resolvingClass", v.getResolvingClass());
        req.setAttribute("it", v.getModel());
        req.setAttribute("httpContext", hc);
        req.setAttribute("_basePath", basePath);
        req.setAttribute("_request", req);
        req.setAttribute("_response", rsp);

        d.forward(req,rsp);

        req.setAttribute("resolvingClass", oldResolvingClass);
        req.setAttribute("it", oldIt);
    }

    public void include(ServletRequest req, ServletResponse rsp) throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }
}
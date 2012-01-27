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

package com.sun.jersey.test.framework.impl.container.embedded.glassfish;

import com.sun.jersey.test.framework.web.jaxb.types.ContextParamType;
import com.sun.jersey.test.framework.web.jaxb.types.ListenerType;
import com.sun.jersey.test.framework.web.jaxb.types.ServletInitParamType;
import com.sun.jersey.test.framework.web.jaxb.types.ServletMappingType;
import com.sun.jersey.test.framework.web.jaxb.types.ServletType;
import com.sun.jersey.test.framework.web.jaxb.types.WebAppType;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Generates the web.xml on the fly based on the ApplicationDescriptor instance
 * passed.
 * @author Naresh (Srinivas.Bhimisetty@Sun.Com)
 */
public class WebXmlGenerator {

    /**
     * Binds the deployment descriptor data to web.xml
     */
    private WebAppType webAppType;

    public WebXmlGenerator(WebAppDescriptor applicationDescriptor) {
        webAppType = new WebAppType();
        // check if the deployment descriptor should have any context parameters
        Map<String, String> contextParams = applicationDescriptor.getContextParams();
        
        if( contextParams != null && contextParams.size() > 0 ) {

            List<ContextParamType> contextParameters = new ArrayList<ContextParamType>();
            Iterator<String> contextParamIterator = contextParams.keySet().iterator();
            String paramName = "";
            String paramValue;
            ContextParamType contextParam;
            
            while (contextParamIterator.hasNext()) {
                paramName = contextParamIterator.next();
                paramValue = contextParams.get(paramName);
                contextParam = new ContextParamType();
                contextParam.setParamName(paramName);
                contextParam.setParamValue(paramValue);
                contextParameters.add(contextParam);
            }
            
            webAppType.setContextParam(contextParameters);
        }

        // add any listeners to be registered
        List<ListenerType> listeners = new ArrayList<ListenerType>();

        for(Class<? extends EventListener> listenerClass :
               applicationDescriptor.getListeners()) {
            ListenerType listener = new ListenerType();
            listener.setListenerClass(listenerClass.getName());
            listeners.add(listener);
        }
      
        // add listeners only if atleast one is registerd.
        if(listeners.size() > 0) {
            webAppType.setListeners(listeners);
        }

        // add the servlet information to the deployment descriptor
        ServletType servlet = new ServletType();
        servlet.setServletName("Jersey Web Application");
        servlet.setServletClass(applicationDescriptor.getServletClass().getName());

        //any init params
        Map<String, String> initParams = applicationDescriptor.getInitParams();
        if(initParams != null) {
            List<ServletInitParamType> servletInitParams = new ArrayList<ServletInitParamType>();
            Iterator<String> initParamIterator = initParams.keySet().iterator();
            ServletInitParamType servletInitParam;
            String paramName;
            while (initParamIterator.hasNext()) {
                paramName = initParamIterator.next();
                servletInitParam = new ServletInitParamType(paramName, initParams.get(paramName));
                servletInitParams.add(servletInitParam);
            }
            servlet.setInitParam(servletInitParams);
            
        }

        // load-on-startup
        servlet.setLoadOnStartup("1");
        webAppType.setServletType(servlet);

        // add the servlet mapping info
        ServletMappingType servletMapping = new ServletMappingType();
        servletMapping.setServletName("Jersey Web Application");
        String urlPattern = normalizedUrlPattern(applicationDescriptor.getServletPath());
        servletMapping.setUrlPattern(urlPattern);
        webAppType.setServletMapping(servletMapping);
    }

    private String normalizedUrlPattern(String urlPattern) {
        String pattern =  (urlPattern != null && !urlPattern.equals(""))
                ? ((urlPattern.startsWith("/")) ? urlPattern : "/" + urlPattern)
                : "/*";
        pattern = (pattern.endsWith("/*")) ? pattern : pattern + "/*";
        return pattern;
    }

    /**
     * Writes the deployment descriptor.
     * @param out
     * @throws javax.xml.bind.JAXBException
     */
    public void marshalData(OutputStream out) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(WebAppType.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(webAppType, out);
    }
   
}
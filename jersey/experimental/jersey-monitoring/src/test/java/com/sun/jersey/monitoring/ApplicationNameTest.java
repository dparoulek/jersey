/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.monitoring;

import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import org.junit.Test;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.lang.management.ManagementFactory;

import static org.junit.Assert.assertEquals;

/**
 * @author pavel.bucek@oracle.com
 */
public class ApplicationNameTest extends JerseyTest {

    public static final String APP_NAME = "testAppName";

    @Path("appNameTest")
    public static class Hello {
        @GET
        public String get() {
            return "hi";
        }
    }

    public ApplicationNameTest() throws TestContainerException {
        super(new WebAppDescriptor.Builder(ApplicationNameTest.class.getPackage().getName())
                .initParam(MonitoringAdapter.PROPERTY_MONITORING_APP_NAME, APP_NAME)
                .build());
    }

    @Test
    public void appNameTest() throws InterruptedException, MalformedObjectNameException, InstanceNotFoundException, ReflectionException, AttributeNotFoundException, MBeanException {
        resource().path("appNameTest").get(String.class);

        final Object requestCount = ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("com.sun.jersey.monitoring:pp=/,type=com.sun.jersey.monitoring.JerseyJMXGlobalBean,name=" + APP_NAME), "RequestCount");

        assertEquals(requestCount.toString(), "1");
    }
}

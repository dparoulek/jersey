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
package com.sun.jersey.qe.monitoring.resources.bean;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author naresh
 */
@XmlRootElement(name="Resources")
public class ResourcesBean {

    @XmlAttribute(name="resourceClassHitCount-description")
    private String resourceClassHitCount_description;

    @XmlAttribute(name="rootResourceClassHitCount-lastsampletime")
    private String rootResourceClassHitCount_lastsampletime;

    @XmlAttribute(name="rootResourceClassHitCount")
    private String rootResourceClassHitCount;

    @XmlAttribute(name="resourceClassHitCount-starttime")
    private String resourceClassHitCount_starttime;

    @XmlAttribute(name="resourceClassHitCount-lastsampletime")
    private String resourceClassHitCount_lastsampletime;

    @XmlAttribute(name="rootResourceClassHitCount-starttime")
    private String rootResourceClassHitCount_starttime;

    @XmlAttribute(name="rootResourceClassHitCount-description")
    private String rootResourceClassHitCount_description;

    @XmlAttribute(name="resourceClassHitCount")
    private String resourceClassHitCount;

    @XmlElement(name="child-resources")
    private ResourcesBean[] child_resources;

    public String getResourceClassHitCount() {
        return resourceClassHitCount;
    }

    public String getResourceClassHitCount_description() {
        return resourceClassHitCount_description;
    }

    public String getResourceClassHitCount_lastsampletime() {
        return resourceClassHitCount_lastsampletime;
    }

    public String getResourceClassHitCount_starttime() {
        return resourceClassHitCount_starttime;
    }

    public String getRootResourceClassHitCount() {
        return rootResourceClassHitCount;
    }

    public String getRootResourceClassHitCount_description() {
        return rootResourceClassHitCount_description;
    }

    public String getRootResourceClassHitCount_lastsampletime() {
        return rootResourceClassHitCount_lastsampletime;
    }

    public String getRootResourceClassHitCount_starttime() {
        return rootResourceClassHitCount_starttime;
    }

    public ResourcesBean[] getChild_resources() {
        return child_resources;
    }
    
}
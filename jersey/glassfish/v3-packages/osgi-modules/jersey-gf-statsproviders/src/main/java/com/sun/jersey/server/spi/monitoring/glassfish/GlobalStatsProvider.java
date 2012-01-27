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

package com.sun.jersey.server.spi.monitoring.glassfish;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.jersey.server.impl.uri.rules.ResourceClassRule;
import com.sun.jersey.server.impl.uri.rules.ResourceObjectRule;
import com.sun.jersey.server.impl.uri.rules.SubLocatorRule;
import com.sun.jersey.server.spi.monitoring.glassfish.probes.UriRuleProbeProvider;
import com.sun.jersey.server.spi.monitoring.glassfish.ruleevents.AbstractRuleEvent;
import com.sun.jersey.server.spi.monitoring.glassfish.ruleevents.DummyRuleEvent;
import com.sun.jersey.server.spi.monitoring.glassfish.ruleevents.ResourceClassRuleEvent;
import com.sun.jersey.server.spi.monitoring.glassfish.ruleevents.ResourceObjectRuleEvent;
import com.sun.jersey.server.spi.monitoring.glassfish.ruleevents.SubLocatorRuleEvent;
import org.glassfish.api.monitoring.ContainerMonitoring;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.component.Habitat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pavel.bucek@sun.com
 */

public class GlobalStatsProvider {

    private Map<String, ApplicationStatsProvider> applicationStatsProviders;
    private static GlobalStatsProvider INSTANCE = null;

    private boolean registered = false;

    private ThreadLocal<List<AbstractRuleEvent>> ruleEvents = new ThreadLocal<List<AbstractRuleEvent>>() {
        @Override
        protected List<AbstractRuleEvent> initialValue() {
            return new ArrayList<AbstractRuleEvent>();
        }
    };

    private ThreadLocal<ApplicationStatsProvider> currentApplicationStatProvider
            = new ThreadLocal<ApplicationStatsProvider>();

    private GlobalStatsProvider() {
        applicationStatsProviders = new HashMap<String, ApplicationStatsProvider>();
    }

    public static synchronized GlobalStatsProvider getInstance() {
        if(INSTANCE == null)
            INSTANCE = new GlobalStatsProvider();

        return INSTANCE;
    }

    /**
     * checks whether GlobalStatsProvider is registered
     */
    public synchronized void register() {
        if(!registered) {
            StatsProviderManager.register(ContainerMonitoring.JERSEY, PluginPoint.SERVER, "", this);
            Logger.getLogger(GlassfishMonitoringServiceProvider.LOGGER_JERSEY_MONITORING).log(Level.INFO, "GlobalStatsProvider registered");
            registered = true;
        }
    }
    
    @ProbeListener("glassfish:jersey:server-hidden:requestStart")
    public void requestStart(@ProbeParam("requestUri") java.net.URI requestUri) {
        UriRuleProbeProvider.requestStart(requestUri.toString());

        // add application to applications (global "statistics")
        String applicationName = getApplicationName(requestUri.getPath());

        ApplicationStatsProvider applicationStatsProvider;

        if (!applicationStatsProviders.containsKey(applicationName)) {
            //register new ApplicationStatsProvider
            applicationStatsProvider = new ApplicationStatsProvider(applicationName);
            applicationStatsProviders.put(applicationName, applicationStatsProvider);

            // strange functionality of PluginPoint.APPLICATIONS; it causes to
            // managed object be registered as "server.server.applications.jersey..."
            // and we ant to have it as "server.applications
            // StatsProviderManager.register("glassfish", PluginPoint.APPLICATIONS,
            //        appName + "/jersey/resources", applicationStatsProvider);

            // workaround for ^^^
            StatsProviderManager.register(ContainerMonitoring.JERSEY,
                    PluginPoint.SERVER, "applications/" + applicationName + "/jersey/resources",
                    applicationStatsProvider);

            Logger.getLogger(GlassfishMonitoringServiceProvider.LOGGER_JERSEY_MONITORING).log(Level.INFO, "ApplicationStatsProvider for application \"" + applicationName + "\" registered");

        } else {
            applicationStatsProvider = applicationStatsProviders.get(applicationName);
        }

        currentApplicationStatProvider.set(applicationStatsProvider);
    }

    private String getApplicationName(String path) {
        Habitat habitat = Globals.getDefaultHabitat();

        Domain domain = habitat.getInhabitantByType(Domain.class).get();

        List<Application> applicationList = domain.getApplications().getApplications();

        // looks like path always ends with "/" .. but just to be sure..
        for(Application app : applicationList) {
            if( path.startsWith(app.getContextRoot() + "/") ||
                path.equals(app.getContextRoot()))
                return app.getName();
        }

        return null;
    }

    
    @ProbeListener("glassfish:jersey:server-hidden:ruleAccept")
    public void ruleAccept(
            @ProbeParam("ruleName") String ruleName,
            @ProbeParam("path") CharSequence path,
            @ProbeParam("resource") Object resource) {

        UriRuleProbeProvider.ruleAccept(ruleName, path.toString(), (resource == null ? "null" : resource.getClass().getName()));

        AbstractRuleEvent ruleEvent;

        if(ruleName.equals(ResourceClassRule.class.getSimpleName())) {
            ruleEvent = new ResourceClassRuleEvent(ruleName, path, resource, ruleEvents.get());
        } else if(ruleName.equals(SubLocatorRule.class.getSimpleName())) {
            ruleEvent = new SubLocatorRuleEvent(ruleName, path, resource, ruleEvents.get());
        } else if(ruleName.equals(ResourceObjectRule.class.getSimpleName())) {
            ruleEvent = new ResourceObjectRuleEvent(ruleName, path, resource, ruleEvents.get());
        } else {
            ruleEvent = new DummyRuleEvent(ruleName, path, resource);
        }
        
        ruleEvents.get().add(ruleEvent);
    }

    @ProbeListener("glassfish:jersey:server-hidden:requestEnd")
    public void requestEnd() {
        for(AbstractRuleEvent ruleEvent : ruleEvents.get()) {
            ruleEvent.process(currentApplicationStatProvider.get());
        }

        // clean ruleEvents list
        ruleEvents.get().clear();

        UriRuleProbeProvider.requestEnd();
    }
}

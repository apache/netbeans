/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.j2ee.weblogic9;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;

/**
 *
 * @author Petr Hejl
 */
public final class DomainSupport {

    private static final Logger LOGGER = Logger.getLogger(DomainSupport.class.getName());

    private DomainSupport() {
        super();
    }

    /**
     * Returns list of domains suitable for given Weblogic version
     * 
     * @param minimalWeblogicVersion weblogic version or null if any weblogic domain is acceptable
     * @return collection of domains
     */
    public static Collection<WLDomain> getUsableDomainInstances(Version minimalWeblogicVersion) {
        Set<WLDomain> domains = new TreeSet<WLDomain>();
        for (String url : Deployment.getDefault().getInstancesOfServer(WLDeploymentFactory.SERVER_ID)) {
            try {
                WLDeploymentManager dm = (WLDeploymentManager) WLDeploymentFactory.getInstance().getDisconnectedDeploymentManager(url);
                if (minimalWeblogicVersion == null || minimalWeblogicVersion.isBelowOrEqual(dm.getDomainVersion())) {
                    ServerInstance inst = Deployment.getDefault().getServerInstance(url);
                    domains.add(new WLDomain(inst.getDisplayName(),
                            url, dm.getDomainVersion()));
                }
            } catch (DeploymentManagerCreationException ex) {
                // noop ignore
                LOGGER.log(Level.FINE, null, ex);
            } catch (InstanceRemovedException ex) {
                // noop ignore
                LOGGER.log(Level.FINE, null, ex);
            }
        }
        return domains;
    }
    
    public static class WLDomain implements Comparable<WLDomain> {
        
        private final String name;
        
        private final String url;
        
        private final Version version;

        private WLDomain(String name, String url, Version version) {
            this.name = name;
            this.url = url;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(WLDomain o) {
            if (this.version == o.version) {
                return 0;
            }
            if (this.version == null && o.version != null) {
                return 1;
            }
            if (this.version != null && o.version == null) {
                return -1;
            }
            if (this.version.equals(o.version)) {
                return 0;
            }
            if (this.version.isBelowOrEqual(o.version)) {
                return 1;
            }
            return -1;
        }
    }
}

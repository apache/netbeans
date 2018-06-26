/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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

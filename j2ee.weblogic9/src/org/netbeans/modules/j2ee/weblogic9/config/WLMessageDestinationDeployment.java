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
package org.netbeans.modules.j2ee.weblogic9.config;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.netbeans.modules.j2ee.weblogic9.ProgressObjectSupport;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.CommandBasedDeployer;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class WLMessageDestinationDeployment implements MessageDestinationDeployment {

    private static final Logger LOGGER = Logger.getLogger(WLMessageDestinationDeployment.class.getName());

    private final WLDeploymentManager manager;

    public WLMessageDestinationDeployment(WLDeploymentManager manager) {
        this.manager = manager;
    }

    @Override
    public void deployMessageDestinations(Set<MessageDestination> destinations) throws ConfigurationException {
        Set<MessageDestination> deployedDestinations = getMessageDestinations();
        // for faster searching
        Map<String, MessageDestination> deployed = createMap(deployedDestinations);

        // will contain all ds which do not conflict with existing ones
        Map<String, WLMessageDestination> toDeploy = new HashMap<String, WLMessageDestination>();

        // resolve all conflicts
        LinkedList<MessageDestination> conflictJMS = new LinkedList<MessageDestination>();
        for (MessageDestination destination : destinations) {
            if (!(destination instanceof WLMessageDestination)) {
                LOGGER.log(Level.INFO, "Unable to deploy {0}", destination);
                continue;
            }

            WLMessageDestination wLMessageDestination = (WLMessageDestination) destination;
            // FIXME this is checking only JNDI name collisison, check also module name ???
            String name = wLMessageDestination.getName();
            if (deployed.keySet().contains(name)) { // conflicting ds found
                MessageDestination deployedMessageDestination = deployed.get(name);

                // name is same, but message dest differs
                if (!deployed.get(name).equals(wLMessageDestination)) {
                    // they differ, but both are app modules - ok to redeploy
                    if (!((WLMessageDestination)deployedMessageDestination).isSystem() && !wLMessageDestination.isSystem()) {
                        toDeploy.put(name, wLMessageDestination);
                    } else {
                        conflictJMS.add(deployed.get(name));
                    }
                } else {
                    // TODO try to start it
                }
            } else if (name != null) {
                toDeploy.put(name, wLMessageDestination);
            } else {
                LOGGER.log(Level.INFO, "JNDI name was null for {0}", destination);
            }
        }

        if (!conflictJMS.isEmpty()) {
            // TODO exception or nothing ?
        }

        CommandBasedDeployer deployer = new CommandBasedDeployer(manager);
        ProgressObject po = deployer.deployMessageDestinations(toDeploy.values(), manager.getDeployTargets());
        if (!ProgressObjectSupport.waitFor(po) || po.getDeploymentStatus().isFailed()) {
            String msg = NbBundle.getMessage(WLDatasourceManager.class, "MSG_FailedToDeployJMS", po.getDeploymentStatus().getMessage());
            throw new ConfigurationException(msg);
        }
    }

    @Override
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        if (manager.isRemote()) {
            // TODO remote not supported yet
            return Collections.emptySet();
        }

        String domainDir = manager.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);
        File domainPath = FileUtil.normalizeFile(new File(domainDir));
        FileObject domainConfig = WLPluginProperties.getDomainConfigFileObject(manager);
        return new HashSet<MessageDestination>(
                WLMessageDestinationSupport.getMessageDestinations(domainPath, domainConfig, true));
    }

    private Map<String, MessageDestination> createMap(Set<MessageDestination> destinations) {
        if (destinations.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, MessageDestination> map = new HashMap<String, MessageDestination>();
        for (MessageDestination destination : destinations) {
            map.put(destination.getName(), destination);
        }
        return map;
    }
}

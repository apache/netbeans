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
package org.netbeans.modules.tomcat5.ui.nodes;

import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import org.netbeans.modules.tomcat5.AuthorizationException;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.deploy.TomcatModule;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Factory for the children of the web module node (children are web apps).
 * Expects {@link TomcatManager} and {@link Target} in lookup to operate
 * correctly.
 * <p>
 * If the {@link ChildrenFactory} is thread safe, this class is thread safe too.
 *
 * @author Petr Hejl
 */
public class TomcatWebModuleChildrenFactory extends ChildFactory<TomcatWebModule> {

    private static final TomcatWebModule MODULE_WAITING_MARK = new TomcatWebModule(null, null, false);

    private static final Logger LOGGER = Logger.getLogger(TomcatWebModuleChildrenFactory.class.getName());

    private final Lookup lookup;

    /**
     * Constructs the factory.
     *
     * @param lookup lookup where the target and manager is available
     */
    public TomcatWebModuleChildrenFactory(Lookup lookup) {
        this.lookup = lookup;
    }

    /**
     * Updates the keys and refreshes nodes.
     */
    public void updateKeys() {
        refresh(false);
    }

    /**
     * {@inheriDoc}
     */
    @Override
    protected Node createNodeForKey(TomcatWebModule key) {
        if (key == MODULE_WAITING_MARK) {
            return createWaitNode();
        }

        TomcatWebModuleNode node = new TomcatWebModuleNode(key);
        key.setRepresentedNode(node);
        return node;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Asks the tomcat manager for modules available on the target. Manager
     * and target are fetched from lookup passed in constructor.
     */
    @Override
    protected boolean createKeys(List<TomcatWebModule> toPopulate) {
        DeploymentManager manager = lookup.lookup(DeploymentManager.class);
        Target target = lookup.lookup(Target.class);

        TreeSet<TomcatWebModule> list = new TreeSet<>(
                TomcatWebModule.TOMCAT_WEB_MODULE_COMPARATOR);

        if (manager instanceof TomcatManager && target != null) {
            TomcatManager tm = (TomcatManager) manager;

            if (tm.isSuspended() || !tm.isRunning(true)) {
                return true;
            }
            try {
                TargetModuleID[] modules = manager.getRunningModules(ModuleType.WAR, new Target[] {target});
                for (int i = 0; i < modules.length; i++) {
                    list.add(new TomcatWebModule(manager, (TomcatModule) modules[i], true));
                }

                modules = manager.getNonRunningModules(ModuleType.WAR, new Target[] {target});
                for (int i = 0; i < modules.length; i++) {
                    list.add(new TomcatWebModule(manager, (TomcatModule) modules[i], false));
                }

            } catch (IllegalStateException | TargetException e) {
                if (e.getCause() instanceof AuthorizationException) {
                    // connection to tomcat manager has not been allowed
                    String errMsg = NbBundle.getMessage(TomcatWebModuleChildrenFactory.class,
                            "MSG_AuthorizationFailed", tm.isAboveTomcat70() ? "manager-script" : "manager");
                    NotifyDescriptor notDesc = new NotifyDescriptor.Message(
                            errMsg, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(notDesc);
                } else {
                    LOGGER.log(Level.INFO, null, e);
                }
            }
        }
        toPopulate.addAll(list);
        return true;
    }

}

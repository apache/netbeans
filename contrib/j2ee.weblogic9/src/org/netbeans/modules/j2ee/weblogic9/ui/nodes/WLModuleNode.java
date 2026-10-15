/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.ControlModuleCookie;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.ModuleCookieSupport;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.OpenModuleUrlAction;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.OpenModuleUrlCookie;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.StartModuleAction;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.StopModuleAction;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.UndeployModuleAction;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.UndeployModuleCookie;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Node which describes Web Module.
 *
 * @author Petr Hejl
 */
public class WLModuleNode extends AbstractNode {

    private static final Logger LOGGER = Logger.getLogger(WLModuleNode.class.getName());

    private final ModuleType moduleType;

    private final boolean stopped;

    private final String name;

    private final String url;

    public WLModuleNode(String name, List<TargetModuleID> modules, Lookup lookup,
            ModuleType moduleType, boolean stopped) {
        super(Children.LEAF);
        assert !modules.isEmpty();
        this.moduleType = moduleType;
        this.stopped = stopped;
        // FIXME reolve url later ?
        this.url = modules.get(0).getWebURL();
        this.name = name;

        if (stopped) {
            setDisplayName(name + " " + "[" // NOI18N
                    + NbBundle.getMessage(WLModuleNode.class, "LBL_Stopped")
                    + "]"); // NOI18N
        } else {
            setDisplayName(name);
        }

        if (url != null) {
            getCookieSet().add(new OpenModuleUrlCookieImpl(url));
        }
        getCookieSet().add(new ControlModuleCookieImpl(modules.get(0), lookup, !stopped));
        getCookieSet().add(new UndeployModuleCookieImpl(modules.get(0), lookup));
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>(7);
        actions.add(SystemAction.get(StartModuleAction.class));
        actions.add(SystemAction.get(StopModuleAction.class));
        actions.add(null);
        actions.add(SystemAction.get(OpenModuleUrlAction.class));
        actions.add(null);
        actions.add(SystemAction.get(UndeployModuleAction.class));
        return actions.toArray(new Action[0]);
    }

    @Override
    public Image getIcon(int type) {
        if (ModuleType.EAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EAR_ARCHIVE);
        } else if (ModuleType.EJB.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EJB_ARCHIVE);
        } else if (ModuleType.WAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.WAR_ARCHIVE);
        } else {
            return super.getIcon(type);
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    private static class OpenModuleUrlCookieImpl implements OpenModuleUrlCookie {

        private final String url;

        public OpenModuleUrlCookieImpl(String url) {
            this.url = url;
        }

        @Override
        public void openUrl() {
            try {
                URLDisplayer.getDefault().showURL(new URL(url));
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
    }

    private static class UndeployModuleCookieImpl implements UndeployModuleCookie {

        private final ModuleCookieSupport support;

        public UndeployModuleCookieImpl(TargetModuleID module, Lookup lookup) {
            this.support = new ModuleCookieSupport(module, lookup);
        }

        @Override
        public void undeploy() {
            support.performAction(new ModuleCookieSupport.Action() {

                @Override
                public ProgressObject execute(DeploymentManager manager, TargetModuleID module) {
                    return manager.undeploy(new TargetModuleID[] {module});
                }
            });
        }
    }

    private static class ControlModuleCookieImpl implements ControlModuleCookie {

        private final ModuleCookieSupport support;

        private final boolean running;

        public ControlModuleCookieImpl(TargetModuleID module, Lookup lookup, boolean running) {
            this.support = new ModuleCookieSupport(module, lookup);
            this.running = running;
        }

        @Override
        public void start() {
            support.performAction(new ModuleCookieSupport.Action() {

                @Override
                public ProgressObject execute(DeploymentManager manager, TargetModuleID module) {
                    return manager.start(new TargetModuleID[] {module});
                }
            });
        }

        @Override
        public void stop() {
            support.performAction(new ModuleCookieSupport.Action() {

                @Override
                public ProgressObject execute(DeploymentManager manager, TargetModuleID module) {
                    return manager.stop(new TargetModuleID[] {module});
                }
            });
        }

        @Override
        public boolean isRunning() {
            return running;
        }
    }

}

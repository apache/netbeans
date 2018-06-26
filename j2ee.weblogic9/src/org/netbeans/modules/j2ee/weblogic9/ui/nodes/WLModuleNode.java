/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        return actions.toArray(new Action[actions.size()]);
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

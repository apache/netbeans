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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.deployment.impl.bridge;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstanceLookup;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.CustomizerAction;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.spi.server.ServerInstanceFactory;
import org.netbeans.api.server.CommonServerUIs;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Petr Hejl
 */
public class BridgingServerInstance implements ServerInstanceImplementation, Lookup.Provider, Node.Cookie {

    private final org.netbeans.modules.j2ee.deployment.impl.ServerInstance instance;

    private ServerInstance commonInstance;
    
    private BridgingServerInstance(org.netbeans.modules.j2ee.deployment.impl.ServerInstance instance) {
        assert instance != null : "ServerInstance must not be null"; // NOI18N
        this.instance = instance;
    }

    public static BridgingServerInstance createInstance(org.netbeans.modules.j2ee.deployment.impl.ServerInstance instance) {
        BridgingServerInstance created = new BridgingServerInstance(instance);
        synchronized (created) {
            created.commonInstance = ServerInstanceFactory.createServerInstance(created);
        }
        return created;
    }
    
    public String getDisplayName() {
        return instance.getDisplayName();
    }

    public String getServerDisplayName() {
        return instance.getServer().getDisplayName();
    }

    public Node getFullNode() {
        Node childNode;
        StartServer startServer = instance.getStartServer();
        if (startServer == null) {
            return null;
        }
        if (startServer.isAlsoTargetServer(null)) {
            childNode = instance.getServer().getNodeProvider().createInstanceTargetNode(instance);
        } else {
            childNode = instance.getServer().getNodeProvider().createInstanceNode(instance);
        }
        instance.refresh(); // detect the server instance status
        return new InstanceNode(childNode, this);
    }

    public Node getBasicNode() {
        Node j2eeNode = instance.getServer().getRegistryNodeFactory().getManagerNode(
                new ServerInstanceLookup(instance, instance.getServer().getDeploymentFactory(), null));

        return new ManagerNode(j2eeNode, instance.getDisplayName());
    }

    public JComponent getCustomizer() {
        Node node = getBasicNode();
        if (node == null || !node.hasCustomizer()) {
            return null;
        }
        Component customizer = node.getCustomizer();
        if (!(customizer instanceof JComponent)) {
            // TODO log
            return null;
        }
        return (JComponent) customizer;
    }

    public boolean isRemovable() {
        return !instance.isRemoveForbidden();
    }

    public void remove() {
        instance.remove();
    }

    @Override
    public Lookup getLookup() {
        // FIXME why is the platform written in such strange way ?
        J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(instance.getUrl());

        if (platform == null) { // can happen when J2EE is activated and J2SE is not !?@#
            return Lookups.singleton(instance.getInstanceProperties());
        } else {
            return new ProxyLookup(Lookups.fixed(platform, instance.getInstanceProperties()), Lookups.proxy(platform));
        }
    }

    public ServerInstance getCommonInstance() {
        synchronized (this) {
            return commonInstance;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BridgingServerInstance other = (BridgingServerInstance) obj;
        if (this.instance != other.instance && (this.instance == null || !this.instance.equals(other.instance))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    private static class ManagerNode extends FilterNode {

        public ManagerNode(Node node, String displayName) {
            super(node);
            disableDelegation(DELEGATE_SET_DISPLAY_NAME | DELEGATE_GET_DISPLAY_NAME
                    | DELEGATE_GET_ACTIONS | DELEGATE_GET_CONTEXT_ACTIONS);
            setDisplayName(displayName);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{};
        }
    }

    private static class InstanceNode extends FilterNode {

        private final BridgingServerInstance instance;

        public InstanceNode(Node original, BridgingServerInstance instance) {
            super(original);
            this.instance = instance;
        }

        @Override
        public org.openide.nodes.Node.Cookie getCookie(Class type) {
            if (BridgingServerInstance.class.isAssignableFrom(type)) {
                return instance;
            }
            return super.getCookie(type);
        }

        @Override
        public Action[] getActions(boolean context) {
            // replace the action with correct one
            boolean found = false;
            List<Action> freshActions = new ArrayList<Action>();
            Action[] actions = getOriginal().getActions(context);
            for (int i = 0; i < actions.length; i++) {
                if (actions[i] instanceof CustomizerAction) {
                    freshActions.add(SystemAction.get(BridgingCustomizerAction.class));
                    found = true;
                } else {
                    freshActions.add(actions[i]);
                }
            }
            if (!found) {
                freshActions.add(null);
                freshActions.add(SystemAction.get(BridgingCustomizerAction.class));
            }
            return freshActions.toArray(new Action[freshActions.size()]);
        }
    }

    private static class BridgingCustomizerAction extends NodeAction {

        public BridgingCustomizerAction() {
            super();
        }

        public void performAction(Node[] nodes) {
            BridgingServerInstance instance = (BridgingServerInstance) nodes[0].getCookie(BridgingServerInstance.class);
            CommonServerUIs.showCustomizer(instance.getCommonInstance());
        }

        protected boolean enable(Node[] nodes) {
            return true;
        }

        public String getName() {
            return NbBundle.getMessage(BridgingServerInstance.class, "LBL_Properties");
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        protected boolean asynchronous() {
            return false;
        }
    }
}

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
import java.awt.Image;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.openide.util.Lookup;
import org.netbeans.modules.tomcat5.ui.nodes.actions.RefreshWebModulesAction;
import org.netbeans.modules.tomcat5.ui.nodes.actions.RefreshWebModulesCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  Petr Pisl
 */

public class TomcatTargetNode extends AbstractNode {

    /** Creates a new instance of TomcatTargetNode */
    public TomcatTargetNode(final Lookup lookup) {
        super(Children.create(new ChildFactory<Class<WebModuleHolderNode>>() {

            @Override
            protected boolean createKeys(List<Class<WebModuleHolderNode>> toPopulate) {
                toPopulate.add(WebModuleHolderNode.class);
                return true;
            }

            @Override
            protected Node createNodeForKey(Class<WebModuleHolderNode> key) {
                return new WebModuleHolderNode(new TomcatWebModuleChildrenFactory(lookup));
            }   
        }, false));
    }

    @Override
    public Action[] getActions(boolean b) {
        return new Action[] {};
    }

    private static class WebModuleHolderNode extends AbstractNode {

        WebModuleHolderNode(TomcatWebModuleChildrenFactory factory) {
            super(Children.create(factory, true));
            setDisplayName(NbBundle.getMessage(TomcatTargetNode.class, "LBL_WebApps"));  // NOI18N
            getCookieSet().add(new RefreshWebModuleChildren(factory));
        }

        @Override
        public Image getIcon(int type) {
            return UISupport.getIcon(ServerIcon.WAR_FOLDER);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return UISupport.getIcon(ServerIcon.WAR_OPENED_FOLDER);
        }

        @Override
        public javax.swing.Action[] getActions(boolean context) {
            return new SystemAction[] {
                   SystemAction.get(RefreshWebModulesAction.class)
               };
        }
    }


    private static class RefreshWebModuleChildren implements RefreshWebModulesCookie {

        private final TomcatWebModuleChildrenFactory factory;

        RefreshWebModuleChildren(TomcatWebModuleChildrenFactory factory){
            this.factory = factory;
        }

        @Override
        public void refresh() {
            factory.updateKeys();
        }
    }
}

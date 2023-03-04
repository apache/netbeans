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

package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.awt.Image;
import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesAction;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

/**
 * Default Node which can have refresh action enabled and which has deafault icon.
 *
 * @author Michal Mocnak
 */
public class WLItemNode extends AbstractItemNode {

    private ModuleType moduleType;

    public WLItemNode(ChildFactory<? extends AbstractNode> childFactory, String name, ModuleType moduleType) {
        super(childFactory, name);
        this.moduleType = moduleType;
    }

    public WLItemNode(Children children, String name) {
        super(children);
        setDisplayName(name);
    }

    @Override
    public Image getIcon(int type) {
        if (ModuleType.WAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.WAR_FOLDER);
        } else if (ModuleType.EAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EAR_FOLDER);
        } else if (ModuleType.EJB.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EJB_FOLDER);
        } else {
            return getIconDelegate().getIcon(type);
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        if (ModuleType.WAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.WAR_OPENED_FOLDER);
        } else if (ModuleType.EAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EAR_OPENED_FOLDER);
        } else if (ModuleType.EJB.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EJB_OPENED_FOLDER);
        } else {
            return getIconDelegate().getOpenedIcon(type);
        }
    }

    @Override
    public javax.swing.Action[] getActions(boolean context) {
        if (getChildFactory() instanceof RefreshModulesCookie) {
            return new SystemAction[] {
                SystemAction.get(RefreshModulesAction.class)
            };
        }

        return new SystemAction[] {};
    }

    private Node getIconDelegate() {
        return DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
    }

}

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

package org.netbeans.modules.j2ee.jboss4.nodes;

import java.awt.Image;
import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.RefreshModulesAction;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.Refreshable;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

/**
 * Default Node which can have refresh action enabled and which has deafault icon.
 *
 * @author Michal Mocnak
 */
public class JBItemNode extends AbstractNode {
    
    private ModuleType moduleType;
    
    public JBItemNode(Children children, String name){
        super(children);
        setDisplayName(name);
        if(getChildren() instanceof Refreshable)
            getCookieSet().add(new RefreshModulesCookieImpl((Refreshable)getChildren()));
    }
    
    public JBItemNode(Children children, String name, ModuleType moduleType) {
        this(children, name);
        this.moduleType = moduleType;
    }
    
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
    
    private Node getIconDelegate() {
        return DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
    }
    
    public javax.swing.Action[] getActions(boolean context) {
        if(getChildren() instanceof Refreshable)
            return new SystemAction[] {
                SystemAction.get(RefreshModulesAction.class)
            };
        
        return new SystemAction[] {};
    }
    
    /**
     * Implementation of the RefreshModulesCookie
     */
    private static class RefreshModulesCookieImpl implements RefreshModulesCookie {
        Refreshable children;
        
        public RefreshModulesCookieImpl(Refreshable children){
            this.children = children;
        }
        
        public void refresh() {
            children.updateKeys();
        }
    }
}

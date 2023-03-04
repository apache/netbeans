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

import org.netbeans.modules.tomcat5.ui.nodes.actions.StopAction;
import org.netbeans.modules.tomcat5.ui.nodes.actions.UndeployAction;
import org.netbeans.modules.tomcat5.ui.nodes.actions.OpenURLAction;
import org.netbeans.modules.tomcat5.ui.nodes.actions.StartAction;
import org.netbeans.modules.tomcat5.ui.nodes.actions.ContextLogAction;
import java.awt.Image;
import java.util.LinkedList;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  Petr Pisl
 */


public class TomcatWebModuleNode extends AbstractNode {

    private TomcatWebModule module;

    /** Creates a new instance of TomcatWebModuleNode */
    public TomcatWebModuleNode(TomcatWebModule module) {
        super(Children.LEAF);
        this.module = module;
        setDisplayName(constructName());
        setShortDescription(module.getTomcatModule ().getWebURL());
        getCookieSet().add(module);
    }
    
    @Override
    public Action[] getActions(boolean context){
        TomcatManager tm = (TomcatManager)module.getDeploymentManager();
        java.util.List actions = new LinkedList();
        actions.add(SystemAction.get(StartAction.class));
        actions.add(SystemAction.get(StopAction.class));
        actions.add(null);
        actions.add(SystemAction.get(OpenURLAction.class));
        if (tm != null && tm.isTomcat50()) {
            actions.add(SystemAction.get(ContextLogAction.class));
        }
        actions.add(null);
        actions.add(SystemAction.get(UndeployAction.class));
        return (SystemAction[])actions.toArray(new SystemAction[0]);
    }
    
    
    @Override
    public Image getIcon(int type) {
        return UISupport.getIcon(ServerIcon.WAR_ARCHIVE);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
   
    private String constructName(){
        if (module.isRunning()) {
            return module.getTomcatModule ().getPath();
        } else {
            return module.getTomcatModule ().getPath() + " [" +
                    NbBundle.getMessage(TomcatWebModuleNode.class, "LBL_Stopped")  // NOI18N
                    + "]";
        }
    }
      
}

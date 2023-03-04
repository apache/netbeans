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

package org.netbeans.modules.j2ee.deployment.impl.ui;

import javax.swing.Action;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.netbeans.modules.j2ee.deployment.impl.*;


/**
 * Instance node is a base for any manager node. The behaviour of this base instance 
 * node can be customized/extended by the manager node provided by the plugin.
 *
 * @author George FinKlang
 */
public class InstanceNode extends AbstractNode implements ServerInstance.StateListener {
    
    protected ServerInstance instance;
    
    public InstanceNode(ServerInstance instance, boolean addStateListener) {
        super(new InstanceChildren(instance));
        this.instance = instance;
        setIconBase(instance.getServer().getIconBase());
        getCookieSet().add(instance);
        if (addStateListener) {
            instance.addStateListener(this);
        }
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public org.openide.nodes.Node.Cookie getCookie(Class type) {
        if (ServerInstance.class.isAssignableFrom(type)) {
            return instance;
        }
        return super.getCookie(type);
    }
    
    @Override
    public Action[] getActions(boolean b) {
        return new Action[] {};
    }
    
    // StateListener implementation -------------------------------------------
    
    public void stateChanged(int oldState, int newState) {
        if (instance.getServerState() != ServerInstance.STATE_WAITING
            && instance.getServerState() != ServerInstance.STATE_SUSPENDED) {
            setChildren(new InstanceChildren(instance));
            getChildren().getNodes(true);
        } else if (instance.getServerState() == ServerInstance.STATE_SUSPENDED) {
            setChildren(Children.LEAF);
        }
    }
    
     public static class InstanceChildren extends Children.Keys {
        ServerInstance serverInstance;
        public InstanceChildren(ServerInstance inst) {
            this.serverInstance = inst;
        }
        protected void addNotify() {
            setKeys(serverInstance.getTargets());
        }
        protected void removeNotify() {
            setKeys(java.util.Collections.EMPTY_SET);
        }
        protected org.openide.nodes.Node[] createNodes(Object obj) {
            ServerTarget child = (ServerTarget) obj;
            //return new Node[] { new TargetBaseNode(org.openide.nodes.Children.LEAF, child) };
            return new Node[] { serverInstance.getServer().
                                 getNodeProvider().createTargetNode(child) };
        }
    }
    
}

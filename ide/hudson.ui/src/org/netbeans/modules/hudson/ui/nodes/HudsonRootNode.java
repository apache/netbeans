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

package org.netbeans.modules.hudson.ui.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.ui.actions.AddInstanceAction;
import static org.netbeans.modules.hudson.ui.nodes.Bundle.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;

@ServicesTabNodeRegistration(name=HudsonRootNode.HUDSON_NODE_NAME, displayName="#LBL_HudsonNode", shortDescription="#TIP_HudsonNode", iconResource=HudsonRootNode.ICON_BASE, position=488)
@Messages({
    "LBL_HudsonNode=Jenkins Builders",
    "TIP_HudsonNode=Jenkins continuous integration servers, including Jenkins."
})
public class HudsonRootNode extends AbstractNode {

    public static final String HUDSON_NODE_NAME = "jenkins"; // NOI18N
    static final String ICON_BASE = "org/netbeans/modules/hudson/ui/resources/hudson.png"; // NOI18N
    

    private HudsonRootNode() {
        super(Children.create(new RootNodeChildren(), true));
        setName(HUDSON_NODE_NAME);
        setDisplayName(LBL_HudsonNode());
        setShortDescription(TIP_HudsonNode());
        setIconBaseWithExtension(ICON_BASE);
    }
    
    public @Override Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new AddInstanceAction());
        /* http://issues.hudson-ci.org/browse/HUDSON-8644
        if (HudsonManagerImpl.getDefault().getInstances().isEmpty()) {
            actions.add(new AddTestInstanceAction());
        }
         */
        return actions.toArray(new Action[0]);
    }
    
    private static class RootNodeChildren extends ChildFactory<HudsonInstance> implements HudsonChangeListener {
        
        public RootNodeChildren() {
            HudsonManager.addHudsonChangeListener(this);
        }

        protected @Override Node createNodeForKey(HudsonInstance key) {
            return new HudsonInstanceNode(key);
        }
        
        @Override
        protected boolean createKeys(List<HudsonInstance> toPopulate) {
            toPopulate.addAll(HudsonManager.getAllInstances());
            Collections.sort(toPopulate);
            return true;
        }

        @Override
        public void stateChanged() {}
        
        @Override
        public void contentChanged() {
            refresh(false);
        }

    }

}

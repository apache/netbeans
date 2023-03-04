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

import javax.swing.Action;

import org.netbeans.modules.j2ee.weblogic9.ui.nodes.ResourceNode.ResourceNodeType;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * A node that represents a concrete target for a particuler server instance.
 * As it gets filtered and does not appear in the registry we do not implement
 * anything special.
 *
 * @author Kirill Sorokin
 */
public class WLTargetNode extends AbstractNode {

    /**
     * Creates a new instance of the WSTargetNode.
     *
     * @param lookup a lookup object that contains the objects required for 
     *      node's customization, such as the deployment manager
     */
    public WLTargetNode(Lookup lookup) {
        super(new Children.Array());
        getChildren().add(new Node[] {new WLItemNode(
                new WLApplicationsChildren(lookup), 
                NbBundle.getMessage(WLTargetNode.class, "LBL_Apps")),   // NOI18N
                new ResourceNode(new ResourceChildren(lookup), 
                        ResourceNodeType.RESOURCE, 
                        NbBundle.getMessage(WLTargetNode.class, 
                        "LBL_Resources"))});
    }
    
    
    /**
     * A fake implementation of the Object's hashCode() method, in order to 
     * avoid FindBugsTool's warnings
     */
    public int hashCode() {
        return super.hashCode();
    }
    
    /**
     * A fake implementation of the Object's hashCode() method, in order to 
     * avoid FindBugsTool's warnings
     */
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    @Override
    public Action[] getActions(boolean b) {
        return new Action[] {};
    }
}

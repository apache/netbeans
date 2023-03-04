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
package org.netbeans.modules.javaee.wildfly.nodes;

import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Kirill Sorokin <Kirill.Sorokin@Sun.COM>
 */
public class WildflyTargetNode extends AbstractNode {

    public WildflyTargetNode(Lookup lookup) {
        super(new Children.Array());
        getChildren().add(new Node[] {
            new WildflyItemNode(new WildflyApplicationsChildren(lookup), NbBundle.getMessage(WildflyTargetNode.class, "LBL_Apps")),
            new WildflyResourcesItemNode(new WildflyResourcesChildren(lookup), NbBundle.getMessage(WildflyTargetNode.class, "LBL_Resources"), Util.RESOURCES_ICON)});
    }

    @Override
    public Action[] getActions(boolean b) {
        return new Action[] {};
    }
}

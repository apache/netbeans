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

package org.netbeans.modules.xml.multiview.ui;
import org.openide.nodes.NodeAdapter;

/** Node for section container
 *
 * @author mkuchtiak
 */
public class SectionContainerNode extends org.openide.nodes.AbstractNode {

    /** Creates a new instance of SectionContainerNode */
    public SectionContainerNode(org.openide.nodes.Children ch) {
        super(ch);
        int childrenSize = ch.getNodes().length;
        setIconBaseWithExtension("org/netbeans/modules/xml/multiview/resources/folder.gif"); //NOI18N
        addNodeListener(new NodeAdapter() {
            public void childrenAdded(org.openide.nodes.NodeMemberEvent ev) {
                if (SectionContainerNode.this.getChildren().getNodes().length==1) {
                    firePropertyChange(org.openide.nodes.Node.PROP_LEAF,Boolean.TRUE, Boolean.FALSE);
                }
            }
            public void childrenRemoved(org.openide.nodes.NodeMemberEvent ev) {
                if (SectionContainerNode.this.getChildren().getNodes().length==0) {
                    firePropertyChange(org.openide.nodes.Node.PROP_LEAF,Boolean.FALSE, Boolean.TRUE);
                }
            }
        });
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(getName());
    }
}

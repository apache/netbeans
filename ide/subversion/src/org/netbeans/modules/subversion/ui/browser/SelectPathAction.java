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
package org.netbeans.modules.subversion.ui.browser;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.subversion.Subversion;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class SelectPathAction extends AbstractAction {

    private final SVNUrl selectionUrl;
    private Node[] selectionNodes;
    private final Browser browser;
    private static final Node[] EMPTY_NODES = new Node[0];
    
    public SelectPathAction(Browser browser, SVNUrl selection) {
        this.browser = browser;
        this.selectionUrl = selection;                
        putValue(Action.NAME, org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "CTL_Action_SelectPath")); // NOI18N
        setEnabled(true);
    }       
    
    public void actionPerformed(ActionEvent e) {
        Node[] nodes = getSelectionNodes();
        
        if(nodes == null || nodes == EMPTY_NODES) {
            return;
        }
        try {            
            browser.getExplorerManager().setSelectedNodes(nodes);
        } catch (PropertyVetoException ex) {
            Subversion.LOG.log(Level.INFO, null, ex); // should not happen
        }
    }

    private Node[] getSelectionNodes() {
        if(selectionNodes == null) {
            String[] segments = selectionUrl.getPathSegments();
            Node node = (RepositoryPathNode) browser.getExplorerManager().getRootContext();            
            
            for (int i = 0; i < segments.length; i++) {
                Children children = node.getChildren();    
                node = children.findChild(segments[i]);
                if(node==null) {
                    break;
                }                    
            }            
            if(node == null) {
                selectionNodes = EMPTY_NODES;
            } else {
                selectionNodes = new Node[] {node};    
            }            
        }
        return selectionNodes;
    }    
    
}    

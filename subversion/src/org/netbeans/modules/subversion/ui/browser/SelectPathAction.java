/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    private final static Node[] EMPTY_NODES = new Node[0];
    
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

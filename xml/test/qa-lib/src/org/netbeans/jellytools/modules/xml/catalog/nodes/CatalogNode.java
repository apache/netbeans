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
package org.netbeans.jellytools.modules.xml.catalog.nodes;

/*
 * CatalogNode.java
 *
 * Created on 11/13/03 4:01 PM
 */

import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.modules.jndi.actions.RefreshAction;
import org.netbeans.jellytools.modules.xml.catalog.actions.UnmountCatalogAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.Bundle;
import javax.swing.tree.TreePath;
import java.awt.event.KeyEvent;
import org.netbeans.jemmy.operators.JTreeOperator;

/** CatalogNode Class
 * @author ms113234 */
public class CatalogNode extends AbstractNode {
    
    private static final Action refreshAction = new RefreshAction();
    private static final Action unmountCatalogAction = new UnmountCatalogAction();
    private static final Action customizeAction = new CustomizeAction();
    private static final Action propertiesAction = new PropertiesAction();

    /** creates new CatalogNode
     * @param tree JTreeOperator of tree
     * @param treePath String tree path */
    public CatalogNode(JTreeOperator tree, String treePath) {
        super(tree, treePath);
    }

    /** creates new CatalogNode
     * @param tree JTreeOperator of tree
     * @param treePath TreePath of node */
    public CatalogNode(JTreeOperator tree, TreePath treePath) {
        super(tree, treePath);
    }

    /** creates new CatalogNode
     * @param parent parent Node
     * @param treePath String tree path from parent Node */
    public CatalogNode(Node parent, String treePath) {
        super(parent, treePath);
    }

    /** tests popup menu items for presence */
    public void verifyPopup() {
        verifyPopup(new Action[]{
            refreshAction,
            unmountCatalogAction,
            customizeAction,
            propertiesAction
        });
    }

    /** performs RefreshAction with this node */
    public void refresh() {
        refreshAction.perform(this);
    }

    /** performs UnmountCatalogAction with this node */
    public void unmountCatalog() {
        unmountCatalogAction.perform(this);
    }

    /** performs CustomizeAction with this node */
    public void customize() {
        customizeAction.perform(this);
    }

    /** performs PropertiesAction with this node */
    public void properties() {
        propertiesAction.perform(this);
    }
    
    // LIB /////////////////////////////////////////////////////////////////////
        
   /** returns catalog entry node with given name or <code>null</code> */
    public CatalogEntryNode getCatalogEntry(String displayName) {
        return (CatalogEntryNode) getChild(displayName, CatalogEntryNode.class);
    }
}


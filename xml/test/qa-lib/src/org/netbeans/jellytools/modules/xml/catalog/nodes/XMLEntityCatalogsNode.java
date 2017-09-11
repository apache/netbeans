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

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.modules.xml.catalog.actions.MountCatalogAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTreeOperator;

/** XMLEntityCatalogsNode Class
 * @author ms113234 */
public class XMLEntityCatalogsNode extends AbstractNode {
    private static final String MY_PATH = Bundle
    .getString("org.netbeans.modules.xml.catalog.Bundle", "TEXT_catalog_root");  // NOI18N
    
    private static final Action mountCatalogAction = new MountCatalogAction();
    private static final Action propertiesAction = new PropertiesAction();

    /** creates new XMLEntityCatalogsNode
     * @param tree JTreeOperator of tree
     * @param treePath String tree path */
    public XMLEntityCatalogsNode(JTreeOperator tree, String treePath) {
        super(tree, treePath);
    }

    /** creates new XMLEntityCatalogsNode
     * @param tree JTreeOperator of tree
     * @param treePath TreePath of node */
    public XMLEntityCatalogsNode(JTreeOperator tree, TreePath treePath) {
        super(tree, treePath);
    }

    /** creates new XMLEntityCatalogsNode
     * @param parent parent Node
     * @param treePath String tree path from parent Node */
    public XMLEntityCatalogsNode(Node parent, String treePath) {
        super(parent, treePath);
    }

    /** tests popup menu items for presence */
    public void verifyPopup() {
        verifyPopup(new Action[]{
            mountCatalogAction,
            propertiesAction
        });
    }

    /** performs MountCatalogAction with this node */
    public void mountCatalog() {
        mountCatalogAction.perform(this);
    }

    /** performs PropertiesAction with this node */
    public void properties() {
        propertiesAction.perform(this);
    }
    
    // LIB /////////////////////////////////////////////////////////////////////
    
    /** returns default XML Entity Catalogs node instance */
    public static XMLEntityCatalogsNode getInstance() {
        return new XMLEntityCatalogsNode(AbstractNode.getRuntimeTab().tree(), MY_PATH);
    }
    
   /** returns catalog node with given name or <code>null</code> */
    public CatalogNode getCatalog(String displayName) {
        return (CatalogNode) getChild(displayName, CatalogNode.class);
    }
}


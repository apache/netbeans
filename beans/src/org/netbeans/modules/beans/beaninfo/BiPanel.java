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

package org.netbeans.modules.beans.beaninfo;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;


/**
* Search doc action.
*
* @author   Petr Hrebejk
*/
public final class BiPanel extends TopComponent implements ExplorerManager.Provider {
    public static final String BEANINFO_HELP = "beans.beaninfo.nodes"; // NOI18N

    private static ExplorerManager em;
    private BeanTreeView btv;

    static final long serialVersionUID =4088175782441275332L;

    public BiPanel( ) {
        Node waitNode = new BiNode.Wait();

        createContent( waitNode );
    }

    private void createContent ( Node biNode ) {

        btv = new BeanTreeView ();
        em = new ExplorerManager();
        
        PropertySheetView psv = new PropertySheetView ();

        try {
            psv.setSortingMode (PropertySheetView.UNSORTED);
        }
        catch (java.beans.PropertyVetoException e) {
        }

        btv.setMinimumSize(new Dimension(300, 200));
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, btv, psv);

        em.setRootContext ( biNode );
        em.setExploredContext( biNode );

        btv.setDefaultActionAllowed( true );

        setLayout (new BorderLayout());
        add (BorderLayout.CENTER, sp);
        
        initAccessibility();
    }

    public java.awt.Dimension getPreferredSize () {
        java.awt.Dimension sup = super.getPreferredSize ();
        return new java.awt.Dimension ( Math.max (sup.width, 450), Math.max (sup.height, 300 ));
    }

    protected void componentActivated() {
        super.componentActivated();
        ExplorerUtils.activateActions(em, true);
    }

    protected void componentDeactivated() {
        ExplorerUtils.activateActions(em, false);
        super.componentDeactivated();
    }

    void expandAll() {
        btv.expandAll();
    }

    static Node[] getSelectedNodes() {
        return em.getSelectedNodes();
    }

    void setContext( Node node ) {
        em.setRootContext ( node );
        em.setExploredContext( node );
    }

    public HelpCtx getHelpCtx(){
        return new HelpCtx(BiPanel.BEANINFO_HELP);
    }
    
    private void initAccessibility() {
        btv.getAccessibleContext().setAccessibleName((NbBundle.getBundle("org.netbeans.modules.beans.beaninfo.Bundle")).getString("ACSN_BeanInfoLeftTreeView"));
        btv.getAccessibleContext().setAccessibleDescription((NbBundle.getBundle("org.netbeans.modules.beans.beaninfo.Bundle")).getString("ACSD_BeanInfoLeftTreeView"));
    }

    public ExplorerManager getExplorerManager() {
        return em;
    }
}

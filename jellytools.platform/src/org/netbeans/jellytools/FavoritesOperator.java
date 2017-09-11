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

package org.netbeans.jellytools;

import java.awt.Component;
import org.netbeans.jellytools.actions.FavoritesAction;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Operator handling Favorites TopComponent.<p>
 * Functionality related to files tree is delegated to JTreeOperator (method
 * tree()) and nodes.<p>
 *
 * Example:<p>
 * <pre>
 *      FavoritesOperator fo = FavoritesOperator.invoke();
 *      // or when Favorites pane is already opened
 *      //FavoritesOperator fo = new FavoritesOperator();
 *      
 *      // get the tree if needed
 *      JTreeOperator tree = fo.tree();
 *      // work with nodes
 *      new Node(tree, "myNode|subnode").select();
 * </pre> 
 *
 * @see FavoritesAction
 */
public class FavoritesOperator extends TopComponentOperator {
    
    static final String FAVORITES_CAPTION = Bundle.getStringTrimmed(
                                            "org.netbeans.modules.favorites.Bundle", 
                                            "Favorites");
    private static final FavoritesAction viewAction = new FavoritesAction();
    
    private JTreeOperator _tree;
    
    /** Search for Favorites TopComponent within all IDE. */
    public FavoritesOperator() {
        super(waitTopComponent(null, FAVORITES_CAPTION, 0, new FavoritesTabSubchooser()));
    }

    /** invokes Favorites and returns new instance of FavoritesOperator
     * @return new instance of FavoritesOperator */
    public static FavoritesOperator invoke() {
        viewAction.performMenu();
        return new FavoritesOperator();
    }
    
    /** Getter for Favorites JTreeOperator
     * @return JTreeOperator of files tree */    
    public JTreeOperator tree() {
        makeComponentVisible();
        if(_tree==null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }
    
    /**
     * Collapse all nodes.
     */
    public void collapseAll() {
        JTreeOperator tree = tree();
        for (int i = tree.getRowCount() - 1; i >= 0; i--) {
            tree.collapseRow(i);
        }
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tree();
    }
    
    /** SubChooser to determine TopComponent is instance of 
     * org.netbeans.modules.favorites.Tab
     * Used in constructor.
     */
    private static final class FavoritesTabSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().equals("org.netbeans.modules.favorites.Tab");
        }
        
        public String getDescription() {
            return "org.netbeans.modules.favorites.Tab";
        }
    }
}

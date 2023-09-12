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

package org.netbeans.jellytools;

import java.awt.Component;
import org.netbeans.jellytools.actions.FavoritesAction;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Operator handling Favorites TopComponent.<p>
 * Functionality related to files tree is delegated to JTreeOperator (method
 * tree()) and nodes.<p>
 *
 * Example:
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

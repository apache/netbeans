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
package org.netbeans.modules.css.test.operator;

import java.awt.Component;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JTreeOperator;

/**Keeps methods to access navigator component.
 *
 * @author Jindrich Sedek
 */
public class CSSPreviewOperator extends TopComponentOperator{
    private JTreeOperator treeOperator;
    
    private static final String PREVIEW_TITLE =
            Bundle.getString("org.netbeans.modules.navigator.Bundle", "LBL_Navigator");
    
    /** NavigatorOperator is created for navigator window. 
     *  Navigator window must be displayed.
     */ 
    public CSSPreviewOperator(){
        super(waitTopComponent(null, PREVIEW_TITLE, 0, new NavigatorComponentChooser()));
    }
    
    /** This function dislays navigator window and returns operator for it
     * 
     *@return navigator operator
     * 
     */ 
    public static CSSPreviewOperator invokeNavigator() {
        new NavigatorAction().perform();
        return new CSSPreviewOperator();
    }
    
    /**Using navagation Tree you can access root node and then it's childen 
     * recursively
     * 
     * @return Operator of the navigation tree
     */ 
    public JTreeOperator getTree(){
        if (treeOperator == null){
            treeOperator = new JTreeOperator(this, 0);
        }
        return treeOperator;
    }
    
    private static final class NavigatorAction extends Action{
        private static final String navigatorActionName = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window")
                + "|" +
                Bundle.getString("org.netbeans.modules.navigator.Bundle", "Menu/Window/Navigator")
                + "|" +
                Bundle.getStringTrimmed("org.netbeans.modules.navigator.Bundle", "LBL_Action");
        
        public NavigatorAction() {
            super(navigatorActionName, null, "org.netbeans.modules.navigator.ShowNavigatorAction");
        }
    }
    
    private static final class NavigatorComponentChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp.getClass().getName().equals("org.netbeans.modules.navigator.NavigatorTC"));
        }
        
        public String getDescription() {
            return "Navigator Window";
        }
    }
    
    
    
    
}

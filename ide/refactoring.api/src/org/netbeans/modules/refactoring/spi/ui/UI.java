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

package org.netbeans.modules.refactoring.spi.ui;

import java.awt.Component;
import javax.swing.Action;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.impl.RefactoringPanel;
import org.openide.windows.TopComponent;

/**
 * Various static UI helper methods
 * @see RefactoringUI
 * @author Jan Becicka
 */
public final class UI {

    private UI() {
    }
    
    public static enum Constants {
        REQUEST_PREVIEW;
    }

    /**
     * Open Refactoring UI for specified RefactoringUI
     * @param ui 
     * @see RefactoringUI
     */
    public static void openRefactoringUI(RefactoringUI ui) {
        new RefactoringPanel(ui);
    }
    
    /**
     * Open Refactoring UI for specufied RefactoringUI from specified TopComponent. 
     * callerTC will get focus when refactoring is finished.
     * @param ui 
     * @param callerTC 
     * @see RefactoringUI
     */
    public static void openRefactoringUI(RefactoringUI ui, TopComponent callerTC) {
        new RefactoringPanel(ui, callerTC);
    }

    /**
     * Open Refactoring UI for specufied RefactoringUI from specified TopComponent. 
     * callerTC will get focus when refactoring is finished.
     * @param callback this action will be called when user clicks refresh button
     * @param callerTC which component will get focus when refactoring is finished
     * @param ui this RefactoringUI will open
     * @see RefactoringUI
     * @see RefactoringSession
     */
    public static void openRefactoringUI(RefactoringUI ui, RefactoringSession callerTC, Action callback) {
        new RefactoringPanel(ui, callerTC, callback).setVisible(true);
    }
    
    /**
     * use this method from RefactoringElementImplementation.showPreview
     * @param component is set as a preview component of RefactoringPanel
     * @return component was successfuly set
     */ 
    public static boolean setComponentForRefactoringPreview(Component component) {
        RefactoringPanel refactoringPanel = RefactoringPanel.getCurrentRefactoringPanel();
        if (refactoringPanel == null) 
            return false;
        if (component == null) {
            if (refactoringPanel.splitPane.getRightComponent() == null)
                return false;
        }
        refactoringPanel.storeDividerLocation();
        refactoringPanel.splitPane.setRightComponent(component);
        refactoringPanel.restoreDeviderLocation();
        return true;
        
    }
}

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

package org.netbeans.modules.refactoring.spi.impl;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * A CustomRefactoringUI subclass that represents Safe Delete
 * @author Bharath Ravikumar
 */
public class SafeDeleteUI<T> implements RefactoringUI{
    
    private final T[] elementsToDelete;
    
    private final SafeDeleteRefactoring refactoring;
    
    private SafeDeletePanel panel;
    
    private ResourceBundle bundle;
    
    /**
     * Creates a new instance of SafeDeleteUI
     * @param selectedElements An array of selected Elements that need to be 
     * safely deleted
     */
    public SafeDeleteUI(T[] selectedElements) {
        this.elementsToDelete = selectedElements;
        refactoring = new SafeDeleteRefactoring(Lookups.fixed(elementsToDelete));
    }
    
    /**
     * Delegates to the fastCheckParameters of the underlying
     * refactoring
     * @return Returns the result of fastCheckParameters of the
     * underlying refactoring
     */
    @Override
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        return refactoring.fastCheckParameters();
    }
    
    @Override
    public String getDescription() {
        return NbBundle.getMessage(SafeDeleteUI.class, "LBL_SafeDel"); // NOI18N
    }
    
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        
        return new HelpCtx(SafeDeleteUI.class.getName());
    }
    
    @Override
    public String getName() {
        
        return NbBundle.getMessage(SafeDeleteUI.class, "LBL_SafeDel"); // NOI18N
    }
    
    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if(panel == null)
            panel = new SafeDeletePanel();
        return panel;
    }
    
    @Override
    public AbstractRefactoring getRefactoring() {
        
        return refactoring;
    }
    
    @Override
    public boolean hasParameters() {
        
        return false;
    }
    /**
     * Returns false, since this refactoring is not a query.
     * @return false
     */
    @Override
    public boolean isQuery() {
        return false;
    }
    
    @Override
    public Problem setParameters() {
        return refactoring.checkParameters();
    }
    
    //Helper methods------------------
    
    private String getString(String key) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(SafeDeleteUI.class);
        }
        return bundle.getString(key);
    }
    
    private String getString(String key, Object value) {
        return new MessageFormat(getString(key)).format(new Object[] {value});
    }
    
    
}

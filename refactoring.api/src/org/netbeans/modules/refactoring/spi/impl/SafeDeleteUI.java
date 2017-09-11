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

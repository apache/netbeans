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

package org.netbeans.modules.cnd.refactoring.api.ui;

import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.refactoring.actions.ChangeParametersAction;
import org.netbeans.modules.cnd.refactoring.actions.EncapsulateFieldsAction;
import org.netbeans.modules.cnd.refactoring.actions.InlineAction;
import org.netbeans.modules.cnd.refactoring.actions.InstantRenamePerformer;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.netbeans.modules.cnd.refactoring.codegen.ConstructorGenerator;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.netbeans.spi.editor.hints.ChangeInfo;

/**
 * Factory class providing instances of refactoring actions.
 * <p><b>Usage:</b></p>
 * <pre>
 * Action a = CsmRefactoringActionsFactory.encapsulateFieldsAction().createContextAwareInstance(Lookup.fixed(node));
 * a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
 * </pre>
 *
 * For help on creating and registering actions
 * See <a href=http://wiki.netbeans.org/wiki/view/RefactoringFAQ>Refactoring FAQ</a>
 * 
 */
public final class CsmRefactoringActionsFactory {

    public static boolean supportRefactoring(CsmFile file) {
        // no refactorings for Fortran yet
        return file.getFileType() != CsmFile.FileType.SOURCE_FORTRAN_FILE;
    }
    
    private CsmRefactoringActionsFactory(){}
    
   /**
     * Factory method for EncapsulateFieldsAction
     * @return an instance of EncapsulateFieldsAction
     */
    public static ContextAwareAction encapsulateFieldsAction() {
        return EncapsulateFieldsAction.findObject(EncapsulateFieldsAction.class, true);
    }
    
    /**
     * Factory method for ChangeParametersAction
     * @return an instance of ChangeParametersAction
     */
    public static ContextAwareAction changeParametersAction() {
        return ChangeParametersAction.findObject(ChangeParametersAction.class, true);
    }
    
    public static void showConstructorsGenerator(Lookup context) {
        List<? extends CodeGenerator> ctorGensList = (new ConstructorGenerator.Factory()).create(context);
        if (!ctorGensList.isEmpty()) {
            ctorGensList.get(0).invoke();
        }
    }
    
    public static void performInstantRenameAction(JTextComponent target, ChangeInfo changeInfo) {
        try {
            InstantRenamePerformer.invokeInstantRename(target, changeInfo);
        } catch (BadLocationException ex) {
            ex.printStackTrace(System.err);
        }
    }
    
    public static ContextAwareAction inlineAction() {
        return InlineAction.findObject(InlineAction.class, true);
    }
}

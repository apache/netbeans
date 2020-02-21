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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

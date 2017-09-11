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

package org.netbeans.modules.refactoring.java.ui;

import org.netbeans.modules.refactoring.java.api.ui.JavaRefactoringActionsFactory;
import org.netbeans.modules.refactoring.java.spi.ui.JavaActionsImplementationProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.java.spi.ui.JavaActionsImplementationProvider.class, position=100)
public class JavaRefactoringActionsProvider extends JavaActionsImplementationProvider {
    
    public JavaRefactoringActionsProvider() {
    }

    @Override
    public void doExtractInterface(final Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, ExtractInterfaceRefactoringUI.factory());
        UIUtilities.runWhenScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.extractInterfaceAction()));
    }

    @Override
    public boolean canExtractInterface(Lookup lookup) {
        return ContextAnalyzer.canRefactorSingle(lookup, false, false);
    }

    @Override
    public void doExtractSuperclass(Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, ExtractSuperclassRefactoringUI.factory());
        UIUtilities.runWhenScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.extractSuperclassAction()));
    }

    @Override
    public boolean canExtractSuperclass(Lookup lookup) {
        return ContextAnalyzer.canRefactorSingle(lookup, false, false);
    }
    
    @Override
    public void doPushDown(final Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, PushDownRefactoringUI.factory());
        UIUtilities.runWhenScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.pushDownAction()));
    }

    @Override
    public boolean canPushDown(Lookup lookup) {
        return ContextAnalyzer.canRefactorSingle(lookup, false, false);
    }
    
    @Override
    public void doPullUp(final Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, PullUpRefactoringUI.factory());
        UIUtilities.runWhenScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.pullUpAction()));
    }

    @Override
    public boolean canPullUp(Lookup lookup) {
        return ContextAnalyzer.canRefactorSingle(lookup, false, false);
    }    

    @Override
    public boolean canUseSuperType(Lookup lookup) {
        return ContextAnalyzer.canRefactorSingle(lookup, false, false);    
    }

    @Override
    public void doUseSuperType(Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, UseSuperTypeRefactoringUI.factory());
        UIUtilities.runWhenScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.useSuperTypeAction()));
    }
    
    @Override
    public boolean canChangeParameters(Lookup lookup) {
        return ContextAnalyzer.canRefactorSingle(lookup, true, false);
    }

    @Override
    public void doChangeParameters(Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, ChangeParametersUI.factory(lookup));
        UIUtilities.runWhenScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.changeParametersAction()));
    }
    
    @Override
    public boolean canIntroduceParameter(Lookup lookup) {
        return ContextAnalyzer.canRefactorSingle(lookup, true, true);
    }

    @Override
    public void doIntroduceParameter(Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, IntroduceParameterUI.factory(lookup));
        UIUtilities.runWhenScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.changeParametersAction()));
    }    
    
    @Override
    public boolean canInnerToOuter(Lookup lookup) {
        return ContextAnalyzer.canRefactorSingle(lookup, true, false);
    }
    
    @Override
    public void doInnerToOuter(Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, InnerToOuterRefactoringUI.factory());
        UIUtilities.runWhenScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.innerToOuterAction()));
    }

    @Override
    public boolean canEncapsulateFields(Lookup lookup) {
        return ContextAnalyzer.canRefactorSingle(lookup, false, false);
    }

    @Override
    public void doEncapsulateFields(Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, EncapsulateFieldUI.factory(lookup));
        UIUtilities.runWhenScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.encapsulateFieldsAction()));
    }

    @Override
    public boolean canIntroduceLocalExtension(Lookup lookup) {
        return ContextAnalyzer.canRefactorSingle(lookup, true, false);
    }

    @Override
    public void doIntroduceLocalExtension(Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, IntroduceLocalExtensionUI.factory());
        UIUtilities.runWhenScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.introduceLocalExtensionAction()));
    }
    
    @Override
    public boolean canInline(Lookup lookup) {
        return ContextAnalyzer.canRefactorSingle(lookup, true, false);
    }

    @Override
    public void doInline(Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, InlineRefactoringUI.factory());
        UIUtilities.runWhenScanFinished(task, RefactoringActionsProvider.getActionName(JavaRefactoringActionsFactory.inlineAction()));
    }
}

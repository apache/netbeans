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

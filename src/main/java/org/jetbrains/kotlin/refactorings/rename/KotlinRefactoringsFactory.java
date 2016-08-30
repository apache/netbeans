/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.refactorings.rename;

import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Context;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexander.Baratynski
 */
@ServiceProvider(service = RefactoringPluginFactory.class, position = 100)
public class KotlinRefactoringsFactory implements RefactoringPluginFactory {

    @Override
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        Lookup look = refactoring.getRefactoringSource();// TreePathHandle
        Context context = refactoring.getContext();
        if (refactoring instanceof RenameRefactoring) {
            return new KotlinRenameRefactoring((RenameRefactoring) refactoring);
        }
        
        return null;
    }
    
}

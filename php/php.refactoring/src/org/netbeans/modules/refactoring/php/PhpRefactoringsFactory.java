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
package org.netbeans.modules.refactoring.php;

import org.netbeans.modules.refactoring.php.findusages.PhpWhereUsedQueryPlugin;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.php.delete.PhpDeleteRefactoringPlugin;
import org.netbeans.modules.refactoring.php.delete.SafeDeleteSupport;
import org.netbeans.modules.refactoring.php.findusages.WhereUsedSupport;
import org.netbeans.modules.refactoring.php.rename.PhpRenameRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.*;
import org.openide.util.Lookup;

/**
 * @author Radek Matous
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class, position=100)
public class PhpRefactoringsFactory implements RefactoringPluginFactory {
    @Override
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        if (refactoring instanceof WhereUsedQuery) {
            return createFindUsages((WhereUsedQuery)refactoring);
        } else if (refactoring instanceof RenameRefactoring) {
            return createRename((RenameRefactoring)refactoring);
        } else if (refactoring instanceof SafeDeleteRefactoring) {
            return createDelete((SafeDeleteRefactoring)refactoring);
	}
        return null;
    }
    
    private RefactoringPlugin createFindUsages(WhereUsedQuery refactoring) {
        Lookup look = refactoring.getRefactoringSource();
        WhereUsedSupport handle = look.lookup(WhereUsedSupport.class);
        return (handle != null) ? new PhpWhereUsedQueryPlugin(refactoring) : null;
    }

    private RefactoringPlugin createRename(RenameRefactoring refactoring) {
        Lookup look = refactoring.getRefactoringSource();
        WhereUsedSupport handle = look.lookup(WhereUsedSupport.class);
        return (handle != null) ? new PhpRenameRefactoringPlugin(refactoring) : null;
    }

    private RefactoringPlugin createDelete(SafeDeleteRefactoring refactoring) {
        Lookup look = refactoring.getRefactoringSource();
        SafeDeleteSupport handle = look.lookup(SafeDeleteSupport.class);
        return (handle != null) ? new PhpDeleteRefactoringPlugin(refactoring) : null;
    }
}

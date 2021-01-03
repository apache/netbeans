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
package org.netbeans.modules.python.editor.refactoring;

import org.netbeans.modules.python.source.PythonUtils;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

public class PythonRefactoringsFactory implements RefactoringPluginFactory {
    @Override
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        Lookup look = refactoring.getRefactoringSource();
        FileObject file = look.lookup(FileObject.class);
        PythonElementCtx handle = look.lookup(PythonElementCtx.class);
        if (refactoring instanceof WhereUsedQuery) {
            if (handle != null) {
//                return new PythonWhereUsedQueryPlugin((WhereUsedQuery)refactoring);
            }
        } else if (refactoring instanceof RenameRefactoring) {
            if (handle != null || ((file != null) && PythonUtils.canContainPython(file))) {
                //rename java file, class, method etc..
//                return new PythonRenameRefactoringPlugin((RenameRefactoring)refactoring);
            }
        }
        return null;
    }
}

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
package org.netbeans.modules.web.refactoring.rename;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;

/**
 * Handles package and folder rename in tld files.
 *
 * @author Erno Mononen
 */
public class TldPackageRename extends BaseTldRename{
    
    private final RenameRefactoring rename;
    private final FileObject pkg;
    
    public TldPackageRename(RenameRefactoring rename, WebModule webModule, FileObject pkg) {
        super(webModule);
        this.rename = rename;
        this.pkg = pkg;
    }

    
    protected List<RenameItem> getAffectedClasses() {
        List<FileObject> affectedClasses = new ArrayList<FileObject>();
        RefactoringUtil.collectChildren(pkg, affectedClasses);
        List<RenameItem> result = new ArrayList<RenameItem>();
        for (FileObject affected : affectedClasses){
            if (RefactoringUtil.isPackageInfo(affected)) {
                continue;
            }
            String oldName = JavaIdentifiers.getQualifiedName(affected);
            String newName = RefactoringUtil.constructNewName(affected, rename);
            result.add(new RenameItem(newName, oldName));
        }
        return result;
    }

    protected AbstractRefactoring getRefactoring() {
        return rename;
    }
    
}

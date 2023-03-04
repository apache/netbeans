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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Handles move folder refactoring in tld files.
 *
 * @author Erno Mononen
 */
//XXX almost identical to WebFolderRemove, needs refactoring
public class TldFolderMove extends BaseTldRename{

    private final MoveRefactoring move;
    private final FileObject folder;

    public TldFolderMove(WebModule webModule, FileObject folder, MoveRefactoring move) {
        super(webModule);
        this.folder = folder;
        this.move = move;
    }

    @Override
    protected List<RenameItem> getAffectedClasses() {
        List<RenameItem> result = new ArrayList<RenameItem>();
        List<FileObject> fos = new ArrayList<FileObject>();
        RefactoringUtil.collectChildren(folder, fos);
        for (FileObject each : fos){
            if (RefactoringUtil.isPackageInfo(each)) {
                continue;
            }
            String oldFqn = JavaIdentifiers.getQualifiedName(each);
            String targetPackageName = getTargetPackageName(each.getParent());
            String oldUnqualifiedName = JavaIdentifiers.unqualify(oldFqn);
            String newFqn =  targetPackageName.length() == 0 ? oldUnqualifiedName : targetPackageName + "." + oldUnqualifiedName;
            result.add(new RenameItem(newFqn, oldFqn));
        }
        return result;
    }

    private String getTargetPackageName(FileObject fo){
        String newPackageName = RefactoringUtil.getPackageName(move.getTarget().lookup(URL.class));
        String postfix = FileUtil.getRelativePath(this.folder.getParent(), fo).replace('/', '.');

        if (newPackageName.length() == 0){
            return postfix;
        }
        if (postfix.length() == 0){
            return newPackageName;
        }
        return newPackageName + "." + postfix;
    }


    @Override
    protected AbstractRefactoring getRefactoring() {
        return move;
    }

}

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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.refactoring.rename;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Handles move folder refactoring.

 * @author Erno Mononen
 */
//XXX almost identical to TldFolderRemove, needs refactoring
public class WebXmlFolderMove extends BaseWebXmlRename{

    private final MoveRefactoring move;
    private final FileObject folder;
    
    public WebXmlFolderMove(FileObject webDD, FileObject folder, MoveRefactoring move) {
        super(webDD);
        this.folder = folder;
        this.move = move;
    }

    @Override
    protected List<RenameItem> getRenameItems() {
        List<RenameItem> result = new ArrayList<RenameItem>();
        List<FileObject> fos = new ArrayList<FileObject>();
        RefactoringUtil.collectChildren(folder, fos);
        for (FileObject each : fos){
            // #142870 -- skip package-info, it is not needed in web.xml refactoring
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

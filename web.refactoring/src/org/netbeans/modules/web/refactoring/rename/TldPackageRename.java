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

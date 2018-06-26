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
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Handles package rename.
 *
 * @author Erno Mononen
 */
public class WebXmlPackageRename extends BaseWebXmlRename{
    
    /**
     * The folder or package being renamed.
     */
    private final FileObject pkg;
    private final RenameRefactoring rename;
    
    public WebXmlPackageRename(FileObject webDD, FileObject pkg, RenameRefactoring rename) {
        super(webDD);
        this.pkg = pkg;
        this.rename = rename;
    }
    
    protected List<RenameItem> getRenameItems() {
        List<RenameItem> result = new ArrayList<RenameItem>();
        List<FileObject> fos = new ArrayList<FileObject>();
        RefactoringUtil.collectChildren(pkg, fos);
        for (FileObject each : fos){
            // #142870 -- skip package-info, it is not needed in web.xml refactoring
            if (RefactoringUtil.isPackageInfo(each)) {
                continue;
            }
            String oldName = JavaIdentifiers.getQualifiedName(each);
            // #222734 -- skip files outside source packages
            if (oldName == null) {
                continue;
            }
            
            // #153294 - additional check before refactoring starts
            if ( JavaIdentifiers.isValidPackageName( oldName )){
                String newName = RefactoringUtil.constructNewName(each, rename);
                if (newName == null || "".equals(newName)) {
                    result.add(new RenameItem(new Problem(true, NbBundle.getMessage(WebXmlPackageRename.class, "TXT_ErrProblemWhenRenaming", oldName))));
                    continue;
                }

                result.add(new RenameItem(newName, oldName));
            }
            else {
                String packageName = oldName.substring(0, oldName.lastIndexOf("."));
                
                result.add(new RenameItem(new Problem(true, 
                        NbBundle.getMessage(WebXmlPackageRename.class, 
                                "TXT_ErrInvalidPackageName" , packageName))));
            }
            
        }
        return result;
    }
    
    protected AbstractRefactoring getRefactoring() {
        return rename;
    }

}

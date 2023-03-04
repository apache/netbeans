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

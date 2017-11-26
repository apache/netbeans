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

package org.netbeans.modules.hibernate.refactoring;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Dongmei Cao
 */
public class JavaPackageRenameTransaction extends RenameTransaction {
    
    public JavaPackageRenameTransaction(Set<FileObject> files, String oldName, String newName) {
        super(files, oldName, newName);
    }

    /**
     * Do the actual changes
     * 
     */
    public void doChanges() {
        
        // This is the thing that will do all the replacements
        JavaRenameChanger renamer = new JavaRenameChanger(true, origName, newName);
        
        for (FileObject mappingFileObject : getToBeModifiedFiles()) {
            
            OutputStream outs = null;
            try {
                InputStream is = mappingFileObject.getInputStream();
                HibernateMapping hbMapping = null;
                try {
                    hbMapping = HibernateMapping.createGraph(is);
                } catch (RuntimeException ex) {
                    //failed to create graph, corrupted mapping file
                    Logger.getLogger(JavaPackageRenameTransaction.class.getName()).log(Level.WARNING, "Failed to refactor in {0}, verify if xml document is well formed", mappingFileObject.getPath());//NOI18N
                }
                if(hbMapping !=null ) {
                    HibernateRefactoringUtil.ChangeTracker rewriteTrack = new HibernateRefactoringUtil.ChangeTracker();
                    hbMapping.addPropertyChangeListener(rewriteTrack);

                    // Change the package attribute <hibernate-mapping> tag
                    String pkgName = hbMapping.getAttributeValue("Package"); //NOI18N
                    if (pkgName != null && pkgName.equals(origName)) {
                        hbMapping.setAttributeValue("Package", newName);
                    }

                     // The class attribute of <import>s
                    renamer.refactoringImports(hbMapping);

                    // Change all the occurrences in <class> elements
                    renamer.refactoringMyClasses(hbMapping);

                    // Change all the occurrences in <subclass> elements
                    renamer.refactoringSubclasses(hbMapping.getSubclass());

                    // Change all the occurrences in <joined-subclass> elements
                    renamer.refactoringJoinedSubclasses(hbMapping.getJoinedSubclass());

                    // Change all the occurrences in <union-subclass> elements
                    renamer.refactoringUnionSubclasses(hbMapping.getUnionSubclass());

                    if(rewriteTrack.isChanged()){
                        outs = mappingFileObject.getOutputStream();
                        hbMapping.write(outs);
                    }
                    hbMapping.removePropertyChangeListener(rewriteTrack);
                }
            } catch (FileAlreadyLockedException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            } finally {
                try {
                    if(outs != null)
                        outs.close();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }
}

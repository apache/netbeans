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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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

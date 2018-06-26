/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.refactoring.rename;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.StringTokenizer;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.queries.VersioningQuery;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.groovy.refactoring.utils.IdentifiersUtil;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Implementation of package rename refactoring. 
 * 
 * It's responsible for validation of the created folder
 * and also for moving the files inside the new one.
 *
 * @author Martin Janicek
 */
public class RenamePackagePlugin implements RefactoringPlugin {

    private RenameRefactoring refactoring;
    private FileObject folder;

    public RenamePackagePlugin(FileObject folder, RenameRefactoring refactoring) {
        this.refactoring = refactoring;
        this.folder = folder;
    }


    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        final String newName = refactoring.getNewName();
        if (!IdentifiersUtil.isValidPackageName(newName)) {
            return createProblem("ERR_InvalidPackage", newName); //NOI18N
        }

        final ClassPath projectClassPath = ClassPath.getClassPath(folder, ClassPath.SOURCE);
        final FileObject fo = projectClassPath.findResource(newName.replace('.','/'));
        if (fo != null) {
            final FileObject ownerRoot = projectClassPath.findOwnerRoot(folder);
            if(ownerRoot != null && ownerRoot.equals(projectClassPath.findOwnerRoot(fo))) {
                if (fo.isFolder() && fo.getChildren().length == 1) {
                    final FileObject parent = fo.getChildren()[0];
                    final String relativePath = FileUtil.getRelativePath(parent, folder);
                    if (relativePath != null) {
                        return null;
                    }
                }
                return createProblem("ERR_PackageExists", newName); //NOI18N
            }
        }
        return null;
    }

    @Override
    public void cancelRequest() {
    }

    @Override
    public Problem prepare(RefactoringElementsBag elements) {
        elements.addFileChange(refactoring, new RenameNonRecursiveFolder(folder));
        return null;
    }

    private Problem createProblem(String problemID, String newName) {
        final String msg = new MessageFormat(NbBundle.getMessage(RenameRefactoringPlugin.class, problemID)).format(new Object[] {newName});
        return new Problem(true, msg);
    }


    private class RenameNonRecursiveFolder extends SimpleRefactoringElementImplementation {

        private FileObject folder;
        private String oldName;
        private FileObject root;
        private String currentName;


        public RenameNonRecursiveFolder(FileObject folder) {
            this.folder = folder;
            ClassPath classPath = ClassPath.getClassPath(folder, ClassPath.SOURCE);

            if (classPath != null) {
                this.currentName = classPath.getResourceName(folder, '.', false); //NOI18N
                this.oldName = this.currentName;
                this.root = classPath.findOwnerRoot(folder);
            }
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(RenamePackagePlugin.class, "TXT_RenamePackage") + folder.getNameExt(); //NOI18N
        }

        @Override
        public String getDisplayText() {
            return getText();
        }

        @Override
        public void performChange() {
            atomicSetName(refactoring.getNewName());
        }

        @Override
        public void undoChange() {
            atomicSetName(oldName);
        }

        private void atomicSetName(final String name) {
            try {
                folder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                    @Override
                    public void run() throws IOException {
                        setName(name);
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public Lookup getLookup() {
            return Lookups.singleton(folder.getParent());
        }

        @Override
        public FileObject getParentFile() {
            return folder.getParent();
        }

        @Override
        public PositionBounds getPosition() {
            return null;
        }

        public void setName(String name) {
            if (currentName.equals(name)) {
                return;
            }
            name = name.replace('.','/')+'/';           //NOI18N
            currentName = currentName.replace('.','/')+'/';     //NOI18N
            int i;
            for (i=0; i<currentName.length() && i< name.length(); i++) {
                if (currentName.charAt(i) != name.charAt(i)) {
                    break;
                }
            }
            i--;
            int index = currentName.lastIndexOf('/',i);     //NOI18N
            String commonPrefix = index == -1 ? null : currentName.substring(0,index);
            String toCreate = (index+1 == name.length()) ? "" : name.substring(index+1);    //NOI18N
            try {
                FileObject commonFolder = commonPrefix == null ? this.root : this.root.getFileObject(commonPrefix);
                FileObject destination = commonFolder;
                StringTokenizer dtk = new StringTokenizer(toCreate,"/");    //NOI18N
                while (dtk.hasMoreTokens()) {
                    String pathElement = dtk.nextToken();
                    FileObject tmp = destination.getFileObject(pathElement);
                    if (tmp == null) {
                        tmp = destination.createFolder(pathElement);
                    }
                    destination = tmp;
                }
                if (!this.folder.isValid()) {
                    this.folder = FileUtil.toFileObject(new java.io.File(this.folder.getPath()));
                }
                FileObject folder = this.folder;
                FileUtil.toFileObject(new java.io.File(this.folder.getPath()));
                DataFolder sourceFolder = DataFolder.findFolder(folder);
                DataFolder destinationFolder = DataFolder.findFolder(destination);
                DataObject[] children = sourceFolder.getChildren();
                for (int j=0; j<children.length; j++) {
                    if (children[j].getPrimaryFile().isData()) {
                        children[j].move(destinationFolder);
                    }
                }
                while (!commonFolder.equals(folder)) {
                    if (isEmpty(folder)) {
                        FileObject tmp = folder;
                        folder = folder.getParent();
                        tmp.delete();
                    } else {
                        break;
                    }
                }
                this.folder = destinationFolder.getPrimaryFile();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
            this.currentName = name;
        }

        private boolean isEmpty(FileObject folder) {
            if (VersioningQuery.isManaged(folder.toURI())) {
                for (FileObject child : folder.getChildren()) {
                    if (VisibilityQuery.getDefault().isVisible(child)) {
                        return false;
                    }
                }
                return true;
            } else {
                return folder.getChildren().length == 0;
            }
        }
    }
}

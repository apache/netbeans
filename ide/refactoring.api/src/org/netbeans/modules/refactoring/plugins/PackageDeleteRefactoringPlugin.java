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

package org.netbeans.modules.refactoring.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Bharath Ravi Kumar
 */
public class PackageDeleteRefactoringPlugin implements RefactoringPlugin{

    private final SafeDeleteRefactoring refactoring;
    static final String JAVA_EXTENSION = "java";//NOI18N
    
    public PackageDeleteRefactoringPlugin(SafeDeleteRefactoring safeDeleteRefactoring) {
       refactoring = safeDeleteRefactoring;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Lookup lkp = refactoring.getRefactoringSource();
        NonRecursiveFolder folder = lkp.lookup(NonRecursiveFolder.class);
        if (folder != null) {
            return preparePackageDelete(folder, refactoringElements);
        }
        
        FileObject fileObject = lkp.lookup(FileObject.class);
        if (fileObject != null && fileObject.isFolder()) {
            return prepareFolderDelete(fileObject, refactoringElements);
        }
        return null;
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
        return null;
    }

    @Override
    public void cancelRequest() {
    }

    //Private methods
    
    private Problem prepareFolderDelete(FileObject fileObject, RefactoringElementsBag refactoringElements) {
            addDataFilesInFolder(fileObject, refactoringElements);
            refactoringElements.addFileChange(refactoring, new FolderDeleteElem(fileObject));
            return null;
    }

    private Problem preparePackageDelete(NonRecursiveFolder folder, RefactoringElementsBag refactoringElements) {
        DataFolder dataFolder = DataFolder.findFolder(folder.getFolder());
        // First; delete all files except packages
        DataObject children[] = dataFolder.getChildren();
        boolean empty = true;
        for( int i = 0; children != null && i < children.length; i++ ) {
            FileObject fileObject = children[i].getPrimaryFile();
            if ( !fileObject.isFolder() ) {
                refactoringElements.addFileChange(refactoring, new DeleteFile(fileObject, refactoringElements));
            }
            else {
                empty = false;
            }
        }

        // If empty delete itself
        if ( empty ) {
            refactoringElements.addFileChange(refactoring, new PackageDeleteElem(folder));
        }
            
        return null;
    }

    private void addDataFilesInFolder(FileObject folderFileObject, RefactoringElementsBag refactoringElements) {
        for (FileObject childFileObject : folderFileObject.getChildren()) {
            if (!childFileObject.isFolder()) {
                refactoringElements.addFileChange(refactoring, new DeleteFile(childFileObject, refactoringElements));
            }
            else if (childFileObject.isFolder()) {
                addDataFilesInFolder(childFileObject, refactoringElements);
            }
        }
    }
    
    //Copied from BackupFacility
    private static void createNewFolder(File f) throws IOException {
        if (!f.exists()) {
            File parent = f.getParentFile();
            if (parent != null) {
                createNewFolder(parent);
            }
            f.mkdir();
        }
    }

    private static class FolderDeleteElem extends SimpleRefactoringElementImplementation{
        
        private final FileObject dirFileObject;
        private File dir;
        
        private FolderDeleteElem(FileObject folder){
            dirFileObject = folder;
            dir = FileUtil.toFile(dirFileObject);
        }

        @Override
        public void performChange() {
            try {
                dirFileObject.delete();
            } catch (IOException ioException) {
                ErrorManager.getDefault().notify(ioException);
            }
        }

        @Override
        public void undoChange() {
            try {
                createNewFolder(dir);
            } catch (IOException ioException) {
                ErrorManager.getDefault().notify(ioException);
            }
        }
        
        @Override
        public String getText() {
            return NbBundle.getMessage(FileDeletePlugin.class, "TXT_DeleteFolder", 
                    dirFileObject.getNameExt());
        }

        @Override
        public String getDisplayText() {
            return getText();
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public FileObject getParentFile() {
            return URLMapper.findFileObject(dirFileObject.toURL());
        }

        @Override
        public PositionBounds getPosition() {
            return null;
        }

    }
    
    private static class PackageDeleteElem extends SimpleRefactoringElementImplementation{

        private final URL res;
        private final NonRecursiveFolder folder;
        
        private File dir;
        private SourceGroup srcGroup;
        
        private PackageDeleteElem(NonRecursiveFolder folder) {
            this.folder = folder;
            dir = FileUtil.toFile(folder.getFolder());
            res = folder.getFolder().toURL();
            srcGroup = getSourceGroup(folder.getFolder(), JAVA_EXTENSION);
            if (srcGroup == null) {
                srcGroup = getSourceGroup(folder.getFolder(), Sources.TYPE_GENERIC);
            }
        }
        
        @Override
        public void performChange() {
            FileObject root = srcGroup.getRootFolder();
            FileObject parent = folder.getFolder().getParent();
            dir = FileUtil.toFile(folder.getFolder());
            try {
                folder.getFolder().delete();
                while( !parent.equals( root ) && parent.getChildren().length == 0  ) {
                    FileObject newParent = parent.getParent();
                    parent.delete();
                    parent = newParent;
                }
            } catch (IOException ioException) {
                ErrorManager.getDefault().notify(ioException);
            }
        }

        @Override
        public void undoChange() {
            try {
                createNewFolder(dir);
            } catch (IOException ioException) {
                ErrorManager.getDefault().notify(ioException);
            }
        }
        
        @Override
        public String getText() {
            return NbBundle.getMessage(FileDeletePlugin.class, "TXT_DeletePackage", dir.getName());
        }

        @Override
        public String getDisplayText() {
            return getText();
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public FileObject getParentFile() {
            return URLMapper.findFileObject(res);
        }

        @Override
        public PositionBounds getPosition() {
            return null;
        }

        private SourceGroup getSourceGroup(FileObject file, String type) {
            Project prj = FileOwnerQuery.getOwner(file);
            if (prj == null) {
                return null;
            }
            Sources src = ProjectUtils.getSources(prj);
            SourceGroup[] javagroups = src.getSourceGroups(type);

            for (SourceGroup javaSourceGroup : javagroups) {
                if (javaSourceGroup.getRootFolder().equals(file) || FileUtil.isParentOf(javaSourceGroup.getRootFolder(), file)) {
                    return javaSourceGroup;
                }
            }
            return null;
        }

        
    }

}

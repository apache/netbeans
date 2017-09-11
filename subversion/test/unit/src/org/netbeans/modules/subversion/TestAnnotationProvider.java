/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.subversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.masterfs.providers.BaseAnnotationProvider;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions.DeleteHandler;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public class TestAnnotationProvider extends BaseAnnotationProvider {

    static TestAnnotationProvider instance = null;
    List<String> events = new ArrayList<String>();
    FilesystemInterceptor interceptor = new FilesystemInterceptor();
    
    public TestAnnotationProvider() {
        instance = this;
    }

    void init() {
        Set filesystems = getRootFilesystems();
        for (Iterator i = filesystems.iterator(); i.hasNext();) {
            FileSystem fileSystem = (FileSystem) i.next();
            fileSystem.addFileChangeListener(interceptor);
        }
        events.clear();
    }
    
    void reset() {
        Set filesystems = getRootFilesystems();
        for (Iterator i = filesystems.iterator(); i.hasNext();) {
            FileSystem fileSystem = (FileSystem) i.next();
            fileSystem.removeFileChangeListener(interceptor);
        }
        events.clear();
    }
    
    @Override
    public String annotateName(String name, Set files) {
        return "";
    }

    @Override
    public String annotateNameHtml(String name, Set files) {
        return "";
    }

    @Override
    public InterceptionListener getInterceptionListener() {
        return interceptor;
    }

    private class FilesystemInterceptor extends ProvidedExtensions implements FileChangeListener {

        @Override
        public void beforeChange(FileObject fo) {
            events.add("beforeChange " + fo);
            super.beforeChange(fo);
        }

        @Override
        public boolean canWrite(File f) {
            events.add("canWrite " + f);
            return super.canWrite(f);
        }

        @Override
        public void fileLocked(FileObject fo) throws IOException {
            events.add("fileLocked " + fo);
            super.fileLocked(fo);
        }

        @Override
        public void fileUnlocked(FileObject fo) {
            events.add("fileUnlocked " + fo);
            super.fileUnlocked(fo);
        }

        @Override
        public IOHandler getMoveHandler(File from, File to) {
            events.add("getMoveHandler " + from +  " -> " + to);
            return super.getMoveHandler(from, to);
        }

        @Override
        public IOHandler getRenameHandler(File from, String newName) {
            events.add("getMoveHandler " + from +  " -> " + newName);
            return super.getRenameHandler(from, newName);
        }
        
        // create
        @Override
        public void beforeCreate(FileObject parent, String name, boolean isFolder) {
            events.add("beforeCreate " + parent + " " + name + " " + isFolder);
        }
        
        @Override
        public void createFailure(FileObject parent, String name, boolean isFolder) {
            events.add("createFailure " + parent + " " + name + " " + isFolder);
        }

        @Override
        public void createSuccess(FileObject fo) {
            events.add("createSuccess " + fo);
        }
                
        public void fileFolderCreated(FileEvent fe) {
            events.add("fileFolderCreated " + fe.getFile());
        }

        public void fileDataCreated(FileEvent fe) {
            events.add("fileDataCreated " + fe.getFile());
        }

        // delete

        @Override
        public void beforeDelete(FileObject fo) {
            events.add("beforeDelete " + fo);
        }        
        
        public void fileDeleted(FileEvent fe) {
            events.add("fileDeleted " + fe.getFile());
        }

        @Override
        public void deleteFailure(FileObject fo) {
            events.add("deleteFailure " + fo);
        }

        @Override
        public void deleteSuccess(FileObject fo) {
            events.add("deleteSuccess " + fo);
        }

        @Override
        public DeleteHandler getDeleteHandler(File f) {
            events.add("getDeleteHandler " + f);
            return new DeleteHandler() {
                public boolean delete(File file) {
                    events.add("getDeleteHandler.delete " + file);
                    deleteRecursively(file);
                    return true;
                }            
            };
        }
                                
        public void fileChanged(FileEvent fe) { 
            events.add("fileChanged " + fe.getFile());
        }
        public void fileRenamed(FileRenameEvent fe) { 
            events.add("fileRenamed " + fe.getFile());
        }
        public void fileAttributeChanged(FileAttributeEvent fe) { 
            events.add("fileAttributeChanged " + fe.getFile());
        }
   
    }    

    private Set<FileSystem> getRootFilesystems() {
        Set<FileSystem> filesystems = new HashSet<FileSystem>();
        File [] roots = File.listRoots();
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(root));
            if (fo == null) continue;
            try {
                filesystems.add(fo.getFileSystem());
            } catch (FileStateInvalidException e) {
                // ignore invalid filesystems
            }
        }
        return filesystems;
    }      
    
    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }    
}

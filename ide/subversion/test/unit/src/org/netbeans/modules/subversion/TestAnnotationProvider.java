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

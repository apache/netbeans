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

package org.netbeans.modules.versioning;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
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
public class DeleteCreateTestAnnotationProvider extends AnnotationProvider {

    static DeleteCreateTestAnnotationProvider instance = null;
    List<String> events = new ArrayList<String>();
    FilesystemInterceptor interceptor = new FilesystemInterceptor();
    
    public DeleteCreateTestAnnotationProvider() {
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
    public Image annotateIcon(Image icon, int iconType, Set files) {
        return null;
    }

    @Override
    public String annotateNameHtml(String name, Set files) {
        return "";
    }

    @Override
    public Action[] actions(Set files) {
        return new Action[]{};
    }
    
    @Override
    public InterceptionListener getInterceptionListener() {
        return interceptor;
    }

    private class FilesystemInterceptor extends ProvidedExtensions implements FileChangeListener {
        
        // create
        @Override
        public void beforeCreate(FileObject parent, String name, boolean isFolder) {
            events.add("beforeCreate");
        }
        
        @Override
        public void createFailure(FileObject parent, String name, boolean isFolder) {
            events.add("createFailure");
        }

        @Override
        public void createSuccess(FileObject fo) {
            events.add("createSuccess");
        }
                
        public void fileFolderCreated(FileEvent fe) {
            // ignore this
        }

        public void fileDataCreated(FileEvent fe) {
            // ignore this
        }

        // delete

        @Override
        public void beforeDelete(FileObject fo) {
            events.add("beforeDelete");
        }        
        
        public void fileDeleted(FileEvent fe) {
            // ignore this
        }

        @Override
        public void deleteFailure(FileObject fo) {
            events.add("deleteFailure");
        }

        @Override
        public void deleteSuccess(FileObject fo) {
            events.add("deleteSuccess");
        }

        @Override
        public DeleteHandler getDeleteHandler(File f) {
            events.add("getDeleteHandler");
            return new DeleteHandler() {
                public boolean delete(File file) {
                    events.add("getDeleteHandler.delete");
                    file.delete();
                    return true;
                }            
            };
        }
                                
        public void fileChanged(FileEvent fe) { }
        public void fileRenamed(FileRenameEvent fe) { }
        public void fileAttributeChanged(FileAttributeEvent fe) { }
   
        /**
         * WARNING! need this to return -1 otherwise other VCSInterceptor implementations in this test will be skipped
         */
        @Override
        public long refreshRecursively(File dir, long lastTimeStamp, List<? super File> children) {
            events.add("refreshRecursively");
            return -1;
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
}

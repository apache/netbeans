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
package org.netbeans.modules.jshell.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

/**
 *
 * @author sdedic
 */
public class SnippetsFolder implements PersistentSnippets {
    private FileObject parentFolder;
    private ChangeSupport supp = new ChangeSupport(this);
    private FileChangeListener fcl;
    private FileChangeListener weakFCL;
    private boolean attached;
    private FileObject  root;
    private Callable<FileObject>    storageCreator;
    
    public SnippetsFolder(FileObject parentFolder) {
        this.parentFolder = parentFolder;
    }
    
    public SnippetsFolder(FileObject parentFolder, Callable<FileObject> creator) {
        this.parentFolder = parentFolder;
        this.storageCreator = creator;
    }

    @Override
    public boolean isValid() {
        return parentFolder.isValid();
    }
    
    protected void attach(FileObject basedir) {
        synchronized (this) {
            if (attached) {
                return;
            }
            attached = true;
        }
        this.root = basedir;
        fcl = new FileChangeAdapter() {
            @Override
            public void fileDataCreated(FileEvent fe) {
                if (fe.getFile().getParent() == root) {
                    fireChange();
                }
            }

            @Override
            public void fileFolderCreated(FileEvent fe) {
                if (fe.getFile() == null) {
                    return;
                }
                if (fe.getFile().getParent() == root) {
                    fireChange();
                    return;
                }
            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {
                if (fe.getFile().getParent() == root) {
                    fireChange();
                }
            }

            @Override
            public void fileDeleted(FileEvent fe) {
                if (fe.getFile().getParent() == root) {
                    fireChange();
                }
            }

            @Override
            public void fileChanged(FileEvent fe) {
                if (fe.getFile().getParent() == root || fe.getFile() == root) {
                    fireChange();
                }
            }
        };
        basedir.addFileChangeListener(weakFCL = WeakListeners.create(FileChangeListener.class, fcl, basedir));
    }
    
    private void fireChange() {
        supp.fireChange();
    }

    @Override
    public Collection<FileObject> getSavedClasses(String folder) {
        attach(parentFolder);
        FileObject dir = savedClassFolder(null);
        if (dir == null) {
            return Collections.emptyList();
        }
        if (folder != null) {
            dir = dir.getFileObject(folder);
        }
        return dir == null ? Collections.emptyList() :
                Arrays.asList(dir.getChildren());
    }

    @Override
    public FileObject savedClassFolder(String name) {
        attach(parentFolder);
        FileObject dir = root;
        return dir;
    }
    
    private static final String PATH_SNIPPETS = "jshell-snippets"; // NOI18N

    @Override
    public FileObject saveClass(String name, String description, InputStream contents) throws IOException {
        if (storageCreator != null) {
            try {
                storageCreator.call();
            } catch (IOException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }
        FileObject pdir = root;
        FileObject target = FileUtil.createFolder(pdir, PATH_SNIPPETS);
        
        try(OutputStream ostm = target.createAndOpen(name + ".java")) {
            FileUtil.copy(contents, ostm);
        } 
        FileObject snipFile = target.getFileObject(name + ".java");
        return snipFile;
    }

    @Override
    public String getDescription(FileObject saved) {
        return null;
    }

    @Override
    public void setDescription(FileObject saved, String desc) {
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        supp.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        supp.removeChangeListener(l);
    }

    @Override
    public Collection<FileObject> startupSnippets(String runAction) {
        return getSavedClasses("startup");
    }
 
}

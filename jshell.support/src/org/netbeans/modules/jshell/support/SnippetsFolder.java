/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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

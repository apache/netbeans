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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Handles source dir list for a freeform project.
 * XXX will not correctly unregister released external source roots
 * @author Jesse Glick
 */
final class FreeformSources implements Sources, AntProjectListener {
    
    private final FreeformProject project;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public FreeformSources(FreeformProject project) {
        this.project = project;
        project.helper().addAntProjectListener(this);
        initSources(); // have to register external build roots eagerly
    }
    
    private volatile Sources delegate;
    private final Map<File,FileChangeListener> listenOnFiles = Collections.synchronizedMap(new HashMap<File, FileChangeListener>());
    private final ChangeSupport cs = new ChangeSupport(this);
    
    @Override
    public SourceGroup[] getSourceGroups(final String type) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
            @Override
            public SourceGroup[] run() {
                if (delegate == null) {
                    delegate = initSources();
                }
                return delegate.getSourceGroups(type);
            }
        });
    }
    
    private Sources initSources() {
        SourcesHelper h = new SourcesHelper(project, project.helper(), project.evaluator());
        Element genldata = project.getPrimaryConfigurationData();
        Element foldersE = XMLUtil.findElement(genldata, "folders", FreeformProjectType.NS_GENERAL); // NOI18N
        if (foldersE != null) {
            final List<File> newFiles = new ArrayList<File>();
            for (Element folderE : XMLUtil.findSubElements(foldersE)) {
                Element locationE = XMLUtil.findElement(folderE, "location", FreeformProjectType.NS_GENERAL); // NOI18N
                final String location = XMLUtil.findText(locationE);
                String locationEval = project.evaluator().evaluate(location);
                if (locationEval != null) {
                    newFiles.add(project.helper().resolveFile(locationEval));
                }
                if (folderE.getLocalName().equals("build-folder")) { // NOI18N
                    h.addNonSourceRoot(location);
                } else if (folderE.getLocalName().equals("build-file")) { // NOI18N
                    h.addOwnedFile(location);
                } else {
                    assert folderE.getLocalName().equals("source-folder") : folderE;
                    Element nameE = XMLUtil.findElement(folderE, "label", FreeformProjectType.NS_GENERAL); // NOI18N
                    String name = XMLUtil.findText(nameE);
                    Element typeE = XMLUtil.findElement(folderE, "type", FreeformProjectType.NS_GENERAL); // NOI18N
                    String includes = null;
                    Element includesE = XMLUtil.findElement(folderE, "includes", FreeformProjectType.NS_GENERAL); // NOI18N
                    if (includesE != null) {
                        includes = XMLUtil.findText(includesE);
                    }
                    String excludes = null;
                    Element excludesE = XMLUtil.findElement(folderE, "excludes", FreeformProjectType.NS_GENERAL); // NOI18N
                    if (excludesE != null) {
                        excludes = XMLUtil.findText(excludesE);
                    }
                    if (typeE != null) {
                        String type = XMLUtil.findText(typeE);
                        h.addTypedSourceRoot(location, includes, excludes, type, name, null, null);
                    } else {
                        h.addPrincipalSourceRoot(location, includes, excludes, name, null, null);
                    }
                }
            }
            updateFileListeners(newFiles);
        }
        h.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        return h.createSources();
    }
    
    @Override
    public void addChangeListener(ChangeListener changeListener) {
        cs.addChangeListener(changeListener);
    }
    
    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        cs.removeChangeListener(changeListener);
    }
    
    @Override
    public void configurationXmlChanged(AntProjectEvent ev) {
        reset();
    }
    
    @Override
    public void propertiesChanged(AntProjectEvent ev) {
        // ignore
    }

    private void updateFileListeners(final List<? extends File> newFiles) {       
        synchronized (listenOnFiles) {
            final Set<File> toRemove = new HashSet<File>(listenOnFiles.keySet());
            toRemove.removeAll(newFiles);
            final Set<File> toAdd = new HashSet<File>(newFiles);
            toAdd.removeAll(listenOnFiles.keySet());
            for (final File file : toRemove) {
                final FileChangeListener fcl = listenOnFiles.remove(file);
                FileUtil.removeFileChangeListener(fcl, file);
            }

            for (File file : toAdd) {
                final FileChangeListener wl = new WeakFileListener(this, file);
                listenOnFiles.put(file, wl);
                FileUtil.addFileChangeListener(wl, file);
            }
        }
    }

    private void resetIfNeeded(final FileObject fo) {
        final FileObject parent = fo.getParent();
        if (parent != null) {
            final File parentFile = FileUtil.toFile(parent);
            if (parentFile != null) {
                if (listenOnFiles.containsKey(parentFile)) {
                    return;
                }
            }
        }
        reset();
    }

    private void reset() {
        delegate = null;
        cs.fireChange();
    }

    private class WeakFileListener extends WeakReference<FreeformSources> implements Runnable, FileChangeListener {

        private final File file;

        private WeakFileListener (
                final FreeformSources source,
                final File file) {
            super (source, Utilities.activeReferenceQueue());
            this.file = file;
        }

        @Override
        public void run() {
            FileUtil.removeFileChangeListener(this, file);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            resetIfNeeded(fe.getFile());
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            // ignore
        }

        @Override
        public void fileChanged(FileEvent fe) {
            // ignore
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            resetIfNeeded(fe.getFile());
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            resetIfNeeded(fe.getFile());
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore
        }

    }
    
}

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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

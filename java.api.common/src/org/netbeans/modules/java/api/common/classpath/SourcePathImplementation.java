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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.java.api.common.classpath;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.net.URL;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PathMatcher;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Implementation of a single classpath that is derived from one Ant property.
 */
final class SourcePathImplementation implements ClassPathImplementation, PropertyChangeListener {

    // TODO: if needed these parameters can be configurable via constructor parameter:
    private static final String BUILD_DIR = "build.dir"; // NOI18N
    private static final String BUILD_GENERATED_DIR = "build.generated.sources.dir"; // NOI18N

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List<PathResourceImplementation> resources;
    private final SourceRoots sourceRoots;
    private final AntProjectHelper projectHelper;
    private final PropertyEvaluator evaluator;
    private File buildGeneratedDir = null;
    private final FileChangeListener buildGeneratedDirListener = new FileChangeAdapter() {
        public @Override void fileFolderCreated(FileEvent fe) {
            // XXX could do this asynch like SourceRootScannerTask, but would need to do synch during unit test
            invalidate();
        }
        public @Override void fileDeleted(FileEvent fe) {
            invalidate();
        }
        public @Override void fileRenamed(FileRenameEvent fe) {
            invalidate();
        }
    };
    
    /**
     * Construct the implementation.
     * @param sourceRoots used to get the roots information and events
     * @param projectHelper used to obtain the project root
     */
    SourcePathImplementation(SourceRoots sourceRoots, AntProjectHelper projectHelper, PropertyEvaluator evaluator) {
        assert sourceRoots != null && projectHelper != null && evaluator != null;
        this.sourceRoots = sourceRoots;
        this.sourceRoots.addPropertyChangeListener (this);
        this.projectHelper=projectHelper;
        this.evaluator = evaluator;
        evaluator.addPropertyChangeListener(this);
    }
    
    private void invalidate() {
        synchronized (this) {
            this.resources = null;
        }
        this.support.firePropertyChange (PROP_RESOURCES, null, null);        
    } 
    
    public List<PathResourceImplementation> getResources() {
        synchronized (this) {
            if (this.resources != null) {
                return this.resources;
            }
        }
        final URL[] roots = this.sourceRoots.getRootURLs();
        final List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>(roots.length);
        for (final URL root : roots) {
            class PRI implements FilteringPathResourceImplementation, PropertyChangeListener {

                PropertyChangeSupport pcs = new PropertyChangeSupport(PRI.this);
                PathMatcher matcher;

                PRI() {
                    evaluator.addPropertyChangeListener(WeakListeners.propertyChange(PRI.this, evaluator));
                }

                public URL[] getRoots() {
                    return new URL[]{root};
                }

                public boolean includes(URL root, String resource) {
                    if (matcher == null) {
                        matcher = new PathMatcher(
                                evaluator.getProperty(ProjectProperties.INCLUDES),
                                evaluator.getProperty(ProjectProperties.EXCLUDES), Utilities.toFile(URI.create(root.toExternalForm())));
                    }
                    return matcher.matches(resource, true);
                }

                public ClassPathImplementation getContent() {
                    return null;
                }

                public void addPropertyChangeListener(PropertyChangeListener listener) {
                    pcs.addPropertyChangeListener(listener);
                }

                public void removePropertyChangeListener(PropertyChangeListener listener) {
                    pcs.removePropertyChangeListener(listener);
                }

                public void propertyChange(PropertyChangeEvent ev) {
                    String prop = ev.getPropertyName();
                    if (prop == null || prop.equals(ProjectProperties.INCLUDES) || prop.equals(ProjectProperties.EXCLUDES)) {
                        matcher = null;
                        PropertyChangeEvent ev2 = new PropertyChangeEvent(PRI.this, FilteringPathResourceImplementation.PROP_INCLUDES, null, null);
                        ev2.setPropagationId(ev);
                        pcs.firePropertyChange(ev2);
                    }
                }
            }
            result.add(new PRI());
        }
        // add build/generated-sources subfolders to source roots
        try {
            final File buildGeneratedDir = getBuildGeneratedDir();
            if (buildGeneratedDir != null) {
                final String apSourcesDirS = evaluator.getProperty(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT);
                final File apSourcesDir = apSourcesDirS != null ? projectHelper.resolveFile(apSourcesDirS) : null;
                if (buildGeneratedDir.isDirectory()) { // #105645
                    final File[] children = buildGeneratedDir.listFiles();
                    if (children != null) {
                        for (File root : children) {
                            if (!root.isDirectory()) {
                                continue;
                            }
                            if (root.equals(apSourcesDir)) {
                                continue;
                            }
                            result.add(ClassPathSupport.createResource(Utilities.toURI(root).toURL()));
                        }
                    }
                }
            }
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        synchronized (this) {
            if (this.resources == null) {
                this.resources = Collections.unmodifiableList(result);
            }
        }
        return this.resources;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener (listener);
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if (SourceRoots.PROP_ROOTS.equals (evt.getPropertyName())) {
            invalidate();
        } else if (this.evaluator != null && evt.getSource() == this.evaluator &&
                (evt.getPropertyName() == null || BUILD_DIR.equals(evt.getPropertyName()))) {
            invalidate();
        }
    }

    private File getBuildGeneratedDir() {
        final String buildGeneratedDirS = evaluator.getProperty(BUILD_GENERATED_DIR);
        final File _buildGeneratedDir = buildGeneratedDirS == null ? null : projectHelper.resolveFile(buildGeneratedDirS);
        synchronized (this) {
            if (_buildGeneratedDir == null || !_buildGeneratedDir.equals(buildGeneratedDir)) {
                if (buildGeneratedDir != null) {
                    FileUtil.removeFileChangeListener(buildGeneratedDirListener, buildGeneratedDir);
                }
                buildGeneratedDir = _buildGeneratedDir;
                if (buildGeneratedDir != null) {
                    FileUtil.addFileChangeListener(buildGeneratedDirListener, buildGeneratedDir);
                }
            }
            return buildGeneratedDir;
        }
    }

}

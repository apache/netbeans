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

package org.netbeans.modules.php.project.classpath;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.SourceRoots;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.project.support.ant.PathMatcher;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Source class path implementation.
 * @author Tor Norbye
 * @author Tomas Zezula
 */
final class SourcePathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final PhpProject project;
    private final PropertyEvaluator evaluator;
    private List<PathResourceImplementation> resources;
    private final SourceRoots sources;
    private final SourceRoots tests;
    private final SourceRoots selenium;


    public SourcePathImplementation(PhpProject project, SourceRoots sources) {
        this(project, sources, null, null);
    }

    public SourcePathImplementation(PhpProject project, SourceRoots sources, SourceRoots tests, SourceRoots selenium) {
        assert project != null;
        assert sources != null;

        this.project = project;
        evaluator = ProjectPropertiesSupport.getPropertyEvaluator(project);
        this.sources = sources;
        sources.addPropertyChangeListener(WeakListeners.propertyChange(this, sources));

        this.tests = tests;
        this.selenium = selenium;
    }

    @Override
    public List<PathResourceImplementation> getResources() {
        synchronized (this) {
            if (resources != null) {
                return Collections.unmodifiableList(resources);
            }
        }
        final URL[] urls = sources.getRootURLs();
        synchronized (this) {
            if (resources == null) {
                List<PathResourceImplementation> result = new ArrayList<>(urls.length);
                for (URL root : urls) {
                    result.add(new FilteringPathResource(project, root));
                }
                resources = Collections.unmodifiableList(result);
            }
            return Collections.unmodifiableList(resources);
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if (SourceRoots.PROP_ROOTS.equals(evt.getPropertyName())) {
            invalidate();
        } else if (evt.getSource() == evaluator && evt.getPropertyName() == null) {
            invalidate();
        }
    }

    private void invalidate() {
        synchronized (this) {
            resources = null;
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }

    String computeExcludes(File root) {
        StringBuilder buffer = new StringBuilder(100);
        for (File file : project.getIgnoredFiles()) {
            String relPath = PropertyUtils.relativizeFile(root, file);
            if (isUnderneath(relPath)) {
                assert relPath != null;
                // #170570 & #185607 - no way to escape space in file path
                String pattern = relPath.replace(" ", "*"); // NOI18N
                if (file.isDirectory()) {
                    pattern += "/"; // NOI18N
                }
                if (buffer.length() > 0) {
                    buffer.append(","); // NOI18N
                }
                buffer.append(pattern);
            }
        }

        ignoreTests(buffer, root, tests);
        ignoreTests(buffer, root, selenium);

        return buffer.toString();
    }

    private void ignoreTests(StringBuilder buffer, File root, SourceRoots tests) {
        if (tests == null) {
            return;
        }
        assert tests.isTest() : "Not test source roots provided: " + Arrays.toString(tests.getRoots());

        for (FileObject fo : tests.getRoots()) {
            File test = FileUtil.toFile(fo);
            if (test != null) {
                String relPath = PropertyUtils.relativizeFile(root, test);
                if (isUnderneath(relPath)) {
                    String pattern = relPath + "/"; // NOI18N
                    if (buffer.length() > 0) {
                        buffer.append(","); // NOI18N
                    }
                    buffer.append(pattern);
                }
            }
        }
    }

    private boolean isUnderneath(String relativePath) {
        return relativePath != null
                    && !relativePath.equals(".") // NOI18N
                    && !relativePath.startsWith("../"); // NOI18N
    }

    private final class FilteringPathResource implements FilteringPathResourceImplementation, PropertyChangeListener, ChangeListener {

        final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        volatile PathMatcher matcher;
        private final URL root;

        FilteringPathResource(PhpProject project, URL root) {
            assert project != null;
            assert root != null;

            this.root = root;
            ProjectPropertiesSupport.addWeakPropertyEvaluatorListener(project, this);
            ProjectPropertiesSupport.addWeakIgnoredFilesListener(project, this);
        }

        @Override
        public URL[] getRoots() {
            return new URL[]{root};
        }

        @Override
        public boolean includes(URL root, String resource) {
            if (matcher == null) {
                // #246390
                File rootFile = FileUtil.normalizeFile(Utilities.toFile(URI.create(root.toExternalForm())));
                matcher = new PathMatcher(
                        null,
                        computeExcludes(rootFile),
                        rootFile);
            }
            return matcher.matches(resource, true);
        }

        @Override
        public ClassPathImplementation getContent() {
            return null;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            String prop = ev.getPropertyName();
            if (prop == null
                    || prop.startsWith(PhpProjectProperties.TEST_SRC_DIR)
                    || prop.equals(PhpProjectProperties.SELENIUM_SRC_DIR)) {
                fireChange(ev);
            }
        }

        private void fireChange(PropertyChangeEvent event) {
            matcher = null;
            PropertyChangeEvent ev = new PropertyChangeEvent(this, FilteringPathResourceImplementation.PROP_INCLUDES, null, null);
            if (event != null) {
                ev.setPropagationId(event);
            }
            pcs.firePropertyChange(ev);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            // #215880 - change in ignored files from frameworks
            fireChange(null);
        }

    }

}

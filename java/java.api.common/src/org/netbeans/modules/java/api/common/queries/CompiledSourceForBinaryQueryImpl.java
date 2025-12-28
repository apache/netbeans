/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.api.common.queries;

import org.netbeans.modules.java.api.common.SourceRoots;
import java.io.File;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import java.net.URL;
import java.net.MalformedURLException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Default implementation of {@link SourceForBinaryQueryImplementation}.
 * @author Jesse Glick, Tomas Zezula
 */
class CompiledSourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation {

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private final Map<URL, SourceForBinaryQuery.Result> cache = new HashMap<URL, SourceForBinaryQuery.Result>();
    private final String[] binaryProperties;
    private final String[] testBinaryProperties;

    public CompiledSourceForBinaryQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots srcRoots,
            SourceRoots testRoots, String[] binaryProperties, String[] testBinaryProperties) {
        assert helper != null;
        assert evaluator != null;
        assert srcRoots != null;
        assert binaryProperties != null && binaryProperties.length > 0;
        assert testRoots == null ? testBinaryProperties == null : (testBinaryProperties != null && testBinaryProperties.length > 0);
        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = srcRoots;
        this.testRoots = testRoots;
        this.binaryProperties = binaryProperties;
        this.testBinaryProperties = testBinaryProperties;
    }

    @Override
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        if (FileUtil.getArchiveFile(binaryRoot) != null) {
            binaryRoot = FileUtil.getArchiveFile(binaryRoot);
            // XXX check whether this is really the root
        }
        SourceForBinaryQuery.Result res = cache.get(binaryRoot);
        if (res != null) {
            return res;
        }
        SourceRoots src = null;
        for (String property : binaryProperties) {
            if (hasSources(binaryRoot, property)) {
                src = sourceRoots;
                break;
            }
        }
        if (src == null) {
            if (testBinaryProperties != null) {
                for (String property : testBinaryProperties) {
                    if (hasSources(binaryRoot, property)) {
                        src = testRoots;
                        break;
                    }
                }
            }
        }
        if (src == null) {
            return null;
        }
        res = new Result(src, src == sourceRoots);
        cache.put(binaryRoot, res);
        return res;
    }

    private boolean hasSources(URL binaryRoot, String binaryProperty) {
        try {
            String outDir = evaluator.getProperty(binaryProperty);
            if (outDir != null) {
                File f = helper.resolveFile(outDir);
                URL url = Utilities.toURI(f).toURL();
                if (!f.exists() && !f.getPath().toLowerCase().endsWith(".jar")) { // NOI18N
                    // non-existing
                    assert !url.toExternalForm().endsWith("/") : f; // NOI18N
                    url = new URL(url.toExternalForm() + "/"); // NOI18N
                }
                if (url.equals(binaryRoot)) {
                    return true;
                }
            }
        } catch (MalformedURLException malformedURL) {
            Exceptions.printStackTrace(malformedURL);
        }
        return false;
    }

    private class Result implements SourceForBinaryQuery.Result, PropertyChangeListener {

        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private SourceRoots sourceRoots;
        private final boolean gensrc;

        @SuppressWarnings("LeakingThisInConstructor")
        public Result(SourceRoots sourceRoots, boolean gensrc) {
            this.sourceRoots = sourceRoots;
            this.sourceRoots.addPropertyChangeListener(this);
            this.gensrc = gensrc;
        }

        @Override
        public FileObject[] getRoots() {
            if (gensrc) { // #105645
                String buildGeneratedDirS = evaluator.getProperty("build.generated.sources.dir"); // NOI18N
                if (buildGeneratedDirS != null) {
		    final String apSourcesDirS = evaluator.getProperty(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT);
		    final FileObject apSourcesDir = apSourcesDirS != null ? helper.resolveFileObject(apSourcesDirS) : null;
                    FileObject buildGeneratedDir = helper.resolveFileObject(buildGeneratedDirS);
                    if (buildGeneratedDir != null) {
                        List<FileObject> roots = new ArrayList<FileObject>(Arrays.asList(sourceRoots.getRoots()));
                        for (FileObject root : buildGeneratedDir.getChildren()) {
			    if (root.equals(apSourcesDir)) {
				continue;
			    }
                            if (root.isFolder()) {
                                roots.add(root);
                            }
                        }
                        return roots.toArray(new FileObject[0]);
                    }
                }
            }
            return this.sourceRoots.getRoots(); // no need to cache it, SourceRoots does
        }

        @Override
        public void addChangeListener (ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener (ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (SourceRoots.PROP_ROOTS.equals(evt.getPropertyName())) {
                changeSupport.fireChange();
            }
        }
    }
}

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

package org.netbeans.modules.web.clientproject.api.jstesting;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.codecoverage.api.CoverageManager;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProviderHelper;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.netbeans.modules.web.clientproject.spi.jstesting.CoverageImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * Basic implementation of {@link CoverageProvider} and {@link CoverageImplementation}.
 * @since 1.58
 */
public final class CoverageProviderImpl implements CoverageProvider, CoverageImplementation {

    private static final Logger LOGGER = Logger.getLogger(CoverageProviderImpl.class.getName());
    private static final Set<String> MIME_TYPES = Collections.singleton("text/javascript"); // NOI18N

    private final Project project;
    private final Map<String, Coverage.File> files = new ConcurrentHashMap<>();
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    // @GuardedBy("this")
    private Boolean enabled = null;


    /**
     * Creates new instance.
     * @param project project to be used for coverage information
     */
    public CoverageProviderImpl(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        this.project = project;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsHitCounts() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsAggregation() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAggregating() {
        throw new IllegalStateException("Aggregating is not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAggregating(boolean aggregating) {
        throw new IllegalStateException("Aggregating is not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getMimeTypes() {
        return MIME_TYPES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isEnabled() {
        if (enabled == null) {
            enabled = CoverageProviderHelper.isEnabled(project);
            CoverageManager.INSTANCE.setEnabled(project, enabled);
        }
        return enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(boolean on) {
        synchronized (this) {
            if (enabled != null
                    && on == enabled) {
                return;
            }
            enabled = on;
        }
        CoverageProviderHelper.setEnabled(project, on);
        propertyChangeSupport.firePropertyChange(PROP_ENABLED, !on, on);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        files.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileCoverageDetails getDetails(FileObject fo, Document doc) {
        Coverage.File file = files.get(FileUtil.toFile(fo).getAbsolutePath());
        if (file == null) {
            return null;
        }
        return new FileCoverageDetailsImpl(fo, file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileCoverageSummary> getResults() {
        Collection<Coverage.File> allFiles = files.values();
        List<FileCoverageSummary> result = new ArrayList<>(allFiles.size());
        for (Coverage.File file : allFiles) {
            result.add(getFileCoverageSummary(file));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTestAllAction() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFiles(List<Coverage.File> files) {
        assert files != null;
        synchronized (this.files) {
            this.files.clear();
            for (Coverage.File file : files) {
                this.files.put(file.getPath(), file);
            }
        }
        CoverageManager.INSTANCE.resultsUpdated(project, this);
    }

    static FileCoverageSummary getFileCoverageSummary(Coverage.File file) {
        assert file != null;
        FileObject fo = FileUtil.toFileObject(new File(file.getPath()));
        return new FileCoverageSummary(
                fo,
                fo.getNameExt(),
                file.getMetrics().getStatements(),
                file.getMetrics().getCoveredStatements(),
                -1,
                -1);
    }

}

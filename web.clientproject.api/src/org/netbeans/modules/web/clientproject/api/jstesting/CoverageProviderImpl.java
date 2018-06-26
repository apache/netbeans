/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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

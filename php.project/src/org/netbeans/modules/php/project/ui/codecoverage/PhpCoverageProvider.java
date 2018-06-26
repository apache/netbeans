/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.codecoverage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.codecoverage.api.CoverageManager;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProviderHelper;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.spi.testing.coverage.Coverage;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

// XXX coverage data could be fetched lazily but... not sure what is more performance friendly
/**
 * @author Tomas Mysik
 */
public final class PhpCoverageProvider implements CoverageProvider {
    private static final Logger LOGGER = Logger.getLogger(PhpCoverageProvider.class.getName());
    private static final Set<String> MIME_TYPES = Collections.singleton(FileUtils.PHP_MIME_TYPE);

    private final Object lock = new Object();
    private final PhpProject project;
    private final PhpVisibilityQuery phpVisibilityQuery;

    // GuardedBy(this)
    private Boolean enabled = null;
    // GuardedBy(lock)
    private Coverage coverage = null;

    public PhpCoverageProvider(PhpProject project) {
        assert project != null;

        this.project = project;
        phpVisibilityQuery = PhpVisibilityQuery.forProject(project);
    }

    public void setCoverage(Coverage coverage) {
        assert coverage != null;
        assert isEnabled() : "Coverage provider must be enabled";
        synchronized (lock) {
            this.coverage = new CoverageImpl(coverage);
        }
        CoverageManager.INSTANCE.resultsUpdated(project, this);
    }

    public void updateCoverage(Coverage partialCoverage) {
        assert partialCoverage != null;
        assert isEnabled() : "Coverage provider must be enabled";

        Coverage newCoverage = getCoverage();
        if (newCoverage == null) {
            setCoverage(partialCoverage);
            return;
        }

        List<Coverage.File> originalFiles = newCoverage.getFiles();
        for (Coverage.File file : partialCoverage.getFiles()) {
            boolean newFile = true;
            for (int i = 0; i < originalFiles.size(); ++i) {
                if (file.getPath().equals(originalFiles.get(i).getPath())) {
                    originalFiles.set(i, file);
                    newFile = false;
                    break;
                }
            }
            if (newFile) {
                originalFiles.add(file);
            }
        }

        setCoverage(newCoverage);
    }

    public static void notifyProjectOpened(Project project) {
        CoverageManager.INSTANCE.setEnabled(project, true);
    }

    @Override
    public boolean supportsHitCounts() {
        return true;
    }

    @Override
    public boolean supportsAggregation() {
        return false;
    }

    @Override
    public synchronized boolean isEnabled() {
        if (enabled == null) {
            enabled = CoverageProviderHelper.isEnabled(project);
        }
        return enabled;
    }

    @Override
    public synchronized void setEnabled(boolean on) {
        if (enabled != null && on == enabled) {
            return;
        }
        enabled = on;
        CoverageProviderHelper.setEnabled(project, on);
    }

    @Override
    public boolean isAggregating() {
        throw new IllegalStateException("Aggregating is not supported");
    }

    @Override
    public void setAggregating(boolean on) {
        throw new IllegalStateException("Aggregating is not supported");
    }

    @Override
    public Set<String> getMimeTypes() {
        return MIME_TYPES;
    }

    @Override
    public void clear() {
        synchronized (lock) {
            coverage = null;
        }
    }

    @Override
    public FileCoverageDetails getDetails(FileObject fo, Document doc) {
        assert fo != null;
        Coverage cov = getCoverage();
        if (cov == null) {
            return null;
        }
        if (!isUnderneathSourcesOnlyAndVisible(fo)) {
            return null;
        }

        // XXX optimize - hold files in a linked hash map
        String path = FileUtil.toFile(fo).getAbsolutePath();
        for (Coverage.File file : cov.getFiles()) {
            if (path.equals(file.getPath())) {
                return new PhpFileCoverageDetails(fo, file);
            }
        }
        return null;
    }

    @Override
    public List<FileCoverageSummary> getResults() {
        Coverage cov = getCoverage();
        if (cov == null) {
            return null;
        }

        List<FileCoverageSummary> result = new ArrayList<>(cov.getFiles().size());
        for (Coverage.File file : cov.getFiles()) {
            if (isUnderneathSourcesOnlyAndVisible(file.getPath())) {
                result.add(getFileCoverageSummary(file));
            }
        }
        return result;
    }

    @Override
    public String getTestAllAction() {
        return null;
    }

    private Coverage getCoverage() {
        Coverage cov;
        synchronized (lock) {
            cov = coverage;
        }
        return cov;
    }

    static FileCoverageSummary getFileCoverageSummary(Coverage.File file) {
        assert file != null;
        FileObject fo = FileUtil.toFileObject(new File(file.getPath()));
        // #250315
        String displayName = fo != null ? fo.getNameExt() : "???"; // NOI18N
        return new FileCoverageSummary(
                fo,
                displayName,
                file.getMetrics().getStatements(),
                file.getMetrics().getCoveredStatements(),
                -1,
                -1);
    }

    private boolean isUnderneathSourcesOnlyAndVisible(String path) {
        return isUnderneathSourcesOnlyAndVisible(FileUtil.toFileObject(new File(path)));
    }

    private boolean isUnderneathSourcesOnlyAndVisible(FileObject fo) {
        return fo != null
                && fo.isValid()
                && CommandUtils.isUnderSources(project, fo)
                && !CommandUtils.isUnderTests(project, fo, false)
                && !CommandUtils.isUnderSelenium(project, fo, false)
                && phpVisibilityQuery.isVisible(fo);
    }

    //~ Inner classes

    private static final class CoverageImpl implements Coverage {

        private final List<File> files;


        public CoverageImpl(Coverage original) {
            this.files = new ArrayList<>(original.getFiles());
        }

        @Override
        public List<File> getFiles() {
            return files;
        }

    }

}

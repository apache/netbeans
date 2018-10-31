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

package org.netbeans.modules.gradle.java.coverage;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IncompatibleExecDataVersionException;
import org.jacoco.core.tools.ExecFileLoader;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.codecoverage.api.CoverageManager;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.CoverageType;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

import static org.netbeans.modules.gsf.codecoverage.api.CoverageType.*;
import static org.netbeans.modules.gradle.java.coverage.Bundle.*;
import javax.swing.JLabel;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.netbeans.modules.gradle.java.api.ProjectSourcesClassPathProvider;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = CoverageProvider.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/jacoco")
public class GradleCoverageProvider implements CoverageProvider {

    private static final Logger LOG = Logger.getLogger(GradleCoverageProvider.class.getName());

    private static final String JACOCO_VERSION = "0.7.6.201602180812"; //NOI18N
    private Set<File> reports;
    private final Map<FileObject, ISourceFileCoverage> fileCoverage = new HashMap<>();
    private long lastUpdate;
    Notification versionNotification;
    boolean enabled;
    final Project project;

    public GradleCoverageProvider(Project project) {
       this.project = project;
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
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isAggregating() {
        return false;
    }

    @Override
    public void setAggregating(boolean aggregating) {
    }

    @Override
    public Set<String> getMimeTypes() {
        return Collections.singleton("text/x-java"); //NOI18N
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        GradleJavaProject gjp = GradleJavaProject.get(project);
        if (enabled && (gjp != null)) {
            reports = gjp.getCoverageData();
            for (File report : reports) {
                FileUtil.addFileChangeListener(listener, report);
            }
            readJacocoExec();
        } else {
            if (versionNotification != null) {
                versionNotification.clear();
                versionNotification = null;
            }
            if (reports != null) {
                for (File report : reports) {
                    FileUtil.removeFileChangeListener(listener, report);
                }
                reports = null;
            }
            fileCoverage.clear();
        }
    }

    @Override
    public synchronized void clear() {
        fileCoverage.clear();
    }

    @Override
    public FileCoverageDetails getDetails(FileObject fo, Document doc) {
        ISourceFileCoverage coverage = fileCoverage.get(fo);
        return coverage != null ? new GradleFileCoverageDetails(fo, coverage, lastUpdate) : null;

    }

    @Override
    public List<FileCoverageSummary> getResults() {
        List<FileCoverageSummary> ret = new ArrayList<>(fileCoverage.size());
        for (Map.Entry<FileObject, ISourceFileCoverage> entry : fileCoverage.entrySet()) {
            ret.add(createSummary(entry.getKey(), entry.getValue()));
        }
        return ret;
    }

    @Override
    public String getTestAllAction() {
        return "test"; //NOI18N
    }

    private static FileCoverageSummary createSummary(FileObject fo, ISourceFileCoverage fileCoverage) {
        ICounter lineCounter = fileCoverage.getLineCounter();
        int lines = lineCounter.getTotalCount();
        int covered = lineCounter.getCoveredCount();
        int missed = lineCounter.getMissedCount();

        return new FileCoverageSummary(fo, fo.getNameExt(), lines, covered, 0, lines - covered - missed);
    }

    private void readJacocoExec() {
        boolean hasVersionProblem = false;
        lastUpdate = System.currentTimeMillis();
        ExecFileLoader loader = new ExecFileLoader();
        for (File execFile : reports) {
            if (execFile.canRead()) {
                try {
                    loader.load(execFile);
                    lastUpdate = Math.min(lastUpdate, execFile.lastModified());
                } catch (IncompatibleExecDataVersionException vex) {
                    hasVersionProblem = true;
                    LOG.log(Level.INFO, "Incompatible JaCoCo execution data in: " + execFile, vex); //NOI18N
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Can't load JaCoCo execution details from: " + execFile, ex); //NOI18N
                }
            }
        }

        updateVersionNotification(hasVersionProblem);

        ExecutionDataStore dataStore = loader.getExecutionDataStore();
        CoverageBuilder builder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(dataStore, builder);

        GradleJavaProject javaProject = GradleJavaProject.get(project);
        Set<File> testClassesRoots = javaProject.getTestClassesRoots();
        for (GradleJavaSourceSet sourceSet : javaProject.getSourceSets().values()) {
            for (File dir : sourceSet.getOutputClassDirs()) {
                if (!testClassesRoots.contains(dir) && dir.isDirectory()) {
                    try {
                        // Run the analysis on existing non test output dirs.
                        analyzer.analyzeAll(dir);
                    } catch (IOException ex) {
                        //TODO: Report
                    }
                }
            }
        }
        Collection<ISourceFileCoverage> sourceFiles = builder.getSourceFiles();
        ClassPath sourceClassPath = ClassPathSupport.createProxyClassPath(project.getLookup().lookup(ProjectSourcesClassPathProvider.class).getProjectClassPath(ClassPath.SOURCE));
        for (ISourceFileCoverage sourceFile : sourceFiles) {
            String fname = sourceFile.getPackageName() + "/" + sourceFile.getName();
            FileObject fo = sourceClassPath.findResource(fname);
            if (fo != null) {
                fileCoverage.put(fo, sourceFile);
            }
        }
    }

    @Messages({
            "STATUS_INCOMPATIBLE=Old JaCoCo execution data format.",
            "# {0} - Actual JaCoCo version",
            "GRADLE_HINT=<html>Use at least the version <b>0.7.5</b> of JaCoCo in your"
                    + " build to get code coverage metrics recognized.<br/><br/>"
                    + "Add the following line to your build script:<br/>"
                    + "jacoco.toolVersion = ''{0}''<br/><br/>"
                    + "then run a Clean and Test build to update the statistics.",
    })
    private void updateVersionNotification(boolean hasVersionProblem) {
        if (!hasVersionProblem && (versionNotification != null)) {
            //Clear notification if the problem has been resolved
            versionNotification.clear();
            versionNotification = null;
        }

        if (hasVersionProblem && (versionNotification == null)) {
            versionNotification = NotificationDisplayer.getDefault().notify(
                    STATUS_INCOMPATIBLE(),
                    NbGradleProject.getWarningIcon(),
                    new JLabel(STATUS_INCOMPATIBLE()),
                    new JLabel(GRADLE_HINT(JACOCO_VERSION)),
                    NotificationDisplayer.Priority.NORMAL,
                    NotificationDisplayer.Category.WARNING);
        }
    }

    private final FileChangeListener listener = new FileChangeAdapter() {
        @Override
        public void fileDataCreated(FileEvent fe) {
            fireChange();
        }
        @Override
        public void fileDeleted(FileEvent fe) {
            fireChange();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            fireChange();
        }


        private void fireChange() {
            readJacocoExec();
            CoverageManager.INSTANCE.resultsUpdated(project, GradleCoverageProvider.this);
        }
    };

    private static class GradleFileCoverageDetails implements FileCoverageDetails {

        private static final CoverageType[] STATUS_MAP = new CoverageType[] {
            UNKNOWN, NOT_COVERED, COVERED, PARTIAL
        };

        final FileObject file;
        final ISourceFileCoverage cov;
        final long lastUpdated;

        public GradleFileCoverageDetails(FileObject file, ISourceFileCoverage cov, long lastUpdated) {
            this.file = file;
            this.cov = cov;
            this.lastUpdated = lastUpdated;
        }

        @Override
        public FileObject getFile() {
            return file;
        }

        @Override
        public int getLineCount() {
            return cov.getLastLine();
        }

        @Override
        public boolean hasHitCounts() {
            return false;
        }

        @Override
        public long lastUpdated() {
            return lastUpdated;
        }

        @Override
        public FileCoverageSummary getSummary() {
            return createSummary(file, cov);
        }

        @Override
        public CoverageType getType(int lineNo) {
            ILine line = cov.getLine(lineNo + 1);
            return STATUS_MAP[line.getStatus()];
        }

        @Override
        public int getHitCount(int lineNo) {
            return 1;
        }

    }
}

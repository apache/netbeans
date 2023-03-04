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
package org.netbeans.modules.gsf.codecoverage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.gsf.codecoverage.api.CoverageManager;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbPreferences;

/**
 * Manage code coverage collection; delegate to providers, etc.
 *
 * @author Tor Norbye
 */
public class CoverageManagerImpl implements CoverageManager {

    public static final String COVERAGE_INSTANCE_FILE = "coverage.instance"; // NOI18N
    private static final String MIME_TYPE = "mimeType"; // NOI18N
    private static final String COVERAGE_DOC_PROPERTY = "coverage"; // NOI18N
    private static final String PREF_EDITOR_BAR = "editorBar"; // NOI18N
    private final Set<String> enabledMimeTypes = new HashSet<>();
    private final Map<Project, CoverageReportTopComponent> showingReports = new HashMap<>();
    private Boolean showEditorBar;

    static CoverageManagerImpl getInstance() {
        return (CoverageManagerImpl) CoverageManager.INSTANCE;
    }

    @Override
    public void setEnabled(final Project project, final boolean enabled) {
        final CoverageProvider provider = getProvider(project);
        if (provider == null) {
            return;
        }

        final Set<String> mimeTypes = provider.getMimeTypes();
        if (enabled) {
            enabledMimeTypes.addAll(mimeTypes);
        } else {
            enabledMimeTypes.removeAll(mimeTypes);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    for (JTextComponent target : EditorRegistry.componentList()) {
                        Document document = target.getDocument();
                        FileObject fileForDocument = GsfUtilities.findFileObject(document);
                        // show/hide code coverage toolbar in all open file editors belonging only to this project
                        if (fileForDocument != null && project.equals(FileOwnerQuery.getOwner(fileForDocument))) {
                            CoverageSideBar sb = CoverageSideBar.getSideBar(target);
                            if (sb != null) {
                                sb.showCoveragePanel(enabled);
                            }
                        }
                    }
                    // code coverage is being disabled, so close the report window for this project
                    if (!enabled) {
                        CoverageReportTopComponent report = showingReports.get(project);
                        if (report != null) {
                            report.close();
                        }
                    }

                }
            });
        }
        provider.setEnabled(enabled);
    }

    @Override
    public boolean isAggregating(Project project) {
        CoverageProvider provider = getProvider(project);
        if (provider != null) {
            return provider.isAggregating();
        }

        return false;
    }

    public void setAggregating(Project project, boolean aggregating) {
        CoverageProvider provider = getProvider(project);
        if (provider == null) {
            return;
        }

        provider.setAggregating(aggregating);
    }

    void focused(FileObject fo, JTextComponent target) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (project != null) {
            CoverageProvider provider = getProvider(project);
            if (provider != null && provider.isEnabled()) {
                try {
                    EditorCookie ec = DataLoadersBridge.getDefault().getCookie(fo, EditorCookie.class);
                    if (ec != null) {
                        Document doc = ec.getDocument();
                        if (doc == null) {
                            return;
                        }

                        doc.putProperty(COVERAGE_DOC_PROPERTY, null);
                        CoverageHighlightsContainer container = CoverageHighlightsLayerFactory.getContainer(target);
                        if (container != null) {
                            container.refresh();
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    void showFile(Project project, FileCoverageSummary result) {
        FileObject fo = result.getFile();
        if (fo == null) {
            String display = result.getDisplayName();
            File file = new File(display);
            if (file.exists()) {
                fo = FileUtil.toFileObject(file);
            } else {
                fo = project.getProjectDirectory().getFileObject(display.replace('\\', '/'));
            }
        }
        if (fo != null) {
            GsfUtilities.open(fo, -1, null);
        }
    }

    static CoverageProvider getProvider(Project project) {
        if (project != null) {
            return project.getLookup().lookup(CoverageProvider.class);
        } else {
            return null;
        }
    }

    @Override
    public boolean isEnabled(Project project) {
        CoverageProvider provider = getProvider(project);
        if (provider != null) {
            return provider.isEnabled();
        }

        return false;
    }

    public boolean isEnabled(String mimeType) {
        return enabledMimeTypes.contains(mimeType);
    }

    FileCoverageDetails getDetails(Project project, FileObject fileObject, JTextComponent component) {
        if (project != null) {
            CoverageProvider provider = getProvider(project);
            if (provider != null && provider.isEnabled()) {
                Document doc = component.getDocument();
                FileCoverageDetails hitCounts = (FileCoverageDetails) doc.getProperty(COVERAGE_DOC_PROPERTY);
                if (hitCounts == null) {
                    hitCounts = provider.getDetails(fileObject, doc);
                    doc.putProperty(COVERAGE_DOC_PROPERTY, hitCounts);

                    if (getShowEditorBar()) {
                        CoverageSideBar sb = CoverageSideBar.getSideBar(component);
                        if (sb != null) {
                            sb.showCoveragePanel(true);
                            sb.setCoverage(hitCounts);
                        }
                    }

                    return hitCounts;
                }

                return hitCounts;
            }
        }

        return null;
    }

    @Override
    public void resultsUpdated(Project project, CoverageProvider provider) {
        Set<String> mimeTypes = provider.getMimeTypes();
        for (JTextComponent target : EditorRegistry.componentList()) {
            Document document = target.getDocument();
            String mimeType = (String) document.getProperty(MIME_TYPE);
            if (mimeType != null && mimeTypes.contains(mimeType)) {
                FileObject fo = GsfUtilities.findFileObject(document);
                if (fo != null && FileOwnerQuery.getOwner(fo) == project) {
                    FileCoverageDetails hitCounts = (FileCoverageDetails) document.getProperty(COVERAGE_DOC_PROPERTY);
                    if (hitCounts == null) {
                        document.putProperty(COVERAGE_DOC_PROPERTY, null); // ehh... what?
                    }
                    if (isEnabled(project)) {
                        focused(fo, target);
                    } else {
                        CoverageHighlightsContainer container = CoverageHighlightsLayerFactory.getContainer(target);
                        if (container != null) {
                            container.refresh();
                        }
                    }
                }
            }
        }

        final CoverageReportTopComponent report = showingReports.get(project);
        if (report != null) {
            final List<FileCoverageSummary> coverage = provider.getResults();
            Mutex.EVENT.readAccess(new Runnable() {
                public @Override
                void run() {
                    report.updateData(coverage);
                }
            });
        }
    }

    public void clear(Project project) {
        CoverageProvider provider = getProvider(project);
        if (provider != null) {
            provider.clear();
            resultsUpdated(project, provider);
        }
    }

    void closedReport(Project project) {
        showingReports.remove(project);
    }

    public void showReport(Project project) {
        // TODO - keep references to one per project and when open just show
        // the existing one?
        CoverageProvider provider = getProvider(project);
        if (provider != null) {
            List<FileCoverageSummary> results = provider.getResults();

            CoverageReportTopComponent report = showingReports.get(project);
            if (report == null) {
                report = new CoverageReportTopComponent(project, results);
                showingReports.put(project, report);
                report.open();
            }
            report.toFront();
            report.requestVisible();
        }
    }

    public boolean getShowEditorBar() {
        if (showEditorBar == null) {
            showEditorBar = NbPreferences.forModule(CoverageManager.class).getBoolean(PREF_EDITOR_BAR, true);
        }

        return showEditorBar == Boolean.TRUE;
    }

    public void setShowEditorBar(boolean on) {
        this.showEditorBar = on;
        NbPreferences.forModule(CoverageManager.class).putBoolean(PREF_EDITOR_BAR, on);

        // Update existing editors
        for (JTextComponent target : EditorRegistry.componentList()) {
            Document document = target.getDocument();
            CoverageSideBar sb = CoverageSideBar.getSideBar(target);
            if (sb != null) {
                sb.showCoveragePanel(on);
            }
        }
    }
}

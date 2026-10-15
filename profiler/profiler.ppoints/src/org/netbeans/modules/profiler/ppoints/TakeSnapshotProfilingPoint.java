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

package org.netbeans.modules.profiler.ppoints;

import java.awt.BorderLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import org.netbeans.lib.profiler.TargetAppRunner;
import org.netbeans.lib.profiler.client.RuntimeProfilingPoint;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.modules.profiler.LoadedSnapshot;
import org.netbeans.modules.profiler.ResultsManager;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.ProfilerStorage;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.ppoints.ui.ProfilingPointReport;
import org.netbeans.modules.profiler.ppoints.ui.TakeSnapshotCustomizer;
import org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel;
import org.netbeans.modules.profiler.v2.SnapshotsWindow;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


/**
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "TakeSnapshotProfilingPoint_NoDataAvailableMsg=no data available",
    "TakeSnapshotProfilingPoint_RemoteUnsupportedMsg=no data available: not supported for remote profiling",
    "TakeSnapshotProfilingPoint_NoDataJdkMsg=no data available: JDK 1.6, 1.7 or 1.5.0_12 is required",
    "TakeSnapshotProfilingPoint_OneHitString=<b>1 hit</b> at {0}, <a href='#'>report</a>",
    "TakeSnapshotProfilingPoint_NHitsString=<b>{0} hits</b>, last at {1}, <a href='#'>report</a>",
    "TakeSnapshotProfilingPoint_NoResultsString=No results available",
    "TakeSnapshotProfilingPoint_ReportAccessDescr=Report of {0}",
    "TakeSnapshotProfilingPoint_HeaderTypeString=<b>Type:</b> {0}",
    "TakeSnapshotProfilingPoint_HeaderEnabledString=<b>Enabled:</b> {0}",
    "TakeSnapshotProfilingPoint_HeaderProjectString=<b>Project:</b> {0}",
    "TakeSnapshotProfilingPoint_HeaderLocationString=<b>Location:</b> <a href='#'>{0}, line {1}</a>",
    "TakeSnapshotProfilingPoint_HeaderModeDataString=<b>Snapshot type:</b> profiling data",
    "TakeSnapshotProfilingPoint_HeaderModeDumpString=<b>Snapshot type:</b> heap dump",
    "TakeSnapshotProfilingPoint_HeaderTargetProjectString=<b>Save to:</b> project",
    "TakeSnapshotProfilingPoint_HeaderTargetCustomString=<b>Save to:</b> {0}",
    "TakeSnapshotProfilingPoint_HeaderResetResultsString=<b>Reset results:</b> {0}",
    "TakeSnapshotProfilingPoint_HeaderHitsString=<b>Hits:</b> {0}",
    "TakeSnapshotProfilingPoint_OpenSnapshotString=open snapshot",
    "TakeSnapshotProfilingPoint_HitString=<b>{0}.</b> hit at <b>{1}</b>, {2}",
    "TakeSnapshotProfilingPoint_SnapshotNotAvailableMsg=Saved snapshot is no longer available.",
    "TakeSnapshotProfilingPoint_DataString=Data:"
})
public final class TakeSnapshotProfilingPoint extends CodeProfilingPoint.Single implements PropertyChangeListener {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class Annotation extends CodeProfilingPoint.Annotation {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        public String getAnnotationType() {
            return TakeSnapshotProfilingPoint.this.isEnabled() ? ANNOTATION_ENABLED : ANNOTATION_DISABLED;
        }

        @Override
        public String getShortDescription() {
            return getName();
        }

        @Override
        public CodeProfilingPoint profilingPoint() {
            return TakeSnapshotProfilingPoint.this;
        }
    }

    private class Report extends ProfilingPointReport {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private HTMLTextArea dataArea;
        private HTMLTextArea headerArea;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Report() {
            initDefaults();
            initComponents();
            refresh();
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        protected void refresh() {
            StringBuilder headerAreaTextBuilder = new StringBuilder();

            headerAreaTextBuilder.append(getHeaderName());
            headerAreaTextBuilder.append("<br>"); // NOI18N
            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            headerAreaTextBuilder.append(getHeaderType());
            headerAreaTextBuilder.append("<br>"); // NOI18N
            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            headerAreaTextBuilder.append(getHeaderEnabled());
            headerAreaTextBuilder.append("<br>"); // NOI18N
            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            headerAreaTextBuilder.append(getHeaderProject());
            headerAreaTextBuilder.append("<br>"); // NOI18N
            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            headerAreaTextBuilder.append(getHeaderLocation());
            headerAreaTextBuilder.append("<br>"); // NOI18N
            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            headerAreaTextBuilder.append(getHeaderMode());
            headerAreaTextBuilder.append("<br>"); // NOI18N
            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            headerAreaTextBuilder.append(getHeaderTarget());
            headerAreaTextBuilder.append("<br>"); // NOI18N
            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            headerAreaTextBuilder.append(getHeaderResetResults());
            headerAreaTextBuilder.append("<br>"); // NOI18N
            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            headerAreaTextBuilder.append(getHeaderHitsCount()); // NOI18N

            headerArea.setText(headerAreaTextBuilder.toString());

            StringBuilder dataAreaTextBuilder = new StringBuilder();

            synchronized(resultsSync) {
                if (results.isEmpty()) {
                    dataAreaTextBuilder.append(ProfilingPointReport.getNoDataHint(TakeSnapshotProfilingPoint.this));
                } else {
                    for (int i = 0; i < results.size(); i++) {
                        dataAreaTextBuilder.append("&nbsp;&nbsp;");
                        dataAreaTextBuilder.append(getDataResultItem(i));
                        dataAreaTextBuilder.append("<br>"); // NOI18N
                    }
                    ProfilingPointsManager m = ProfilingPointsManager.getDefault();
                    if (!m.belowMaxHits(results.size()))
                        dataAreaTextBuilder.append(m.getTruncatedResultsText());
                }
            }

            dataArea.setText(dataAreaTextBuilder.toString());
        }

        void refreshProperties() {
            setName(TakeSnapshotProfilingPoint.this.getName());
            setIcon(((ImageIcon) TakeSnapshotProfilingPoint.this.getFactory().getIcon()).getImage());
            getAccessibleContext().setAccessibleDescription(Bundle.TakeSnapshotProfilingPoint_ReportAccessDescr(getName()));
        }

        private String getDataResultItem(int index) {
            synchronized(resultsSync) {
                Result result = results.get(index);

                // TODO: enable once thread name by id is available
                //String threadName = Utils.getThreadName(result.getThreadID());
                //String threadClassName = Utils.getThreadClassName(result.getThreadID());
                //String threadInformation = (threadName == null ? "&lt;unknown thread&gt;" : (threadClassName == null ? threadName : threadName + " (" + threadClassName + ")"));
                String resultString = result.getResultString();
                String snapshotInformation = resultString.startsWith(SNAPSHOT_LOCATION_URLMASK)
                                             ? ("<a href='" + resultString + "'>" + Bundle.TakeSnapshotProfilingPoint_OpenSnapshotString() + "</a>") : resultString; // NOI18N
                //return "<b>" + (index + 1) + ".</b> hit at <b>" + Utils.formatProfilingPointTimeHiRes(result.getTimestamp()) + "</b> by " + threadInformation + ", " + snapshotInformation;

                return Bundle.TakeSnapshotProfilingPoint_HitString(
                            (index + 1), Utils.formatProfilingPointTimeHiRes(result.getTimestamp()),
                            snapshotInformation);
            }
        }

        private String getHeaderEnabled() {
            return Bundle.TakeSnapshotProfilingPoint_HeaderEnabledString(TakeSnapshotProfilingPoint.this.isEnabled());
        }

        private String getHeaderHitsCount() {
            synchronized(resultsSync) {
                return Bundle.TakeSnapshotProfilingPoint_HeaderHitsString(results.size());
            }
        }

        private String getHeaderLocation() {
            CodeProfilingPoint.Location location = TakeSnapshotProfilingPoint.this.getLocation();
            String shortFileName = new File(location.getFile()).getName();
            int lineNumber = location.getLine();

            return Bundle.TakeSnapshotProfilingPoint_HeaderLocationString(shortFileName, lineNumber);
        }

        private String getHeaderMode() {
            return TakeSnapshotProfilingPoint.this.getSnapshotType().equals(TYPE_PROFDATA_KEY) ? 
                        Bundle.TakeSnapshotProfilingPoint_HeaderModeDataString()
                        : Bundle.TakeSnapshotProfilingPoint_HeaderModeDumpString();
        }

        private String getHeaderName() {
            return "<h2><b>" + TakeSnapshotProfilingPoint.this.getName() + "</b></h2>"; // NOI18N
        }

        private String getHeaderProject() {
            return Bundle.TakeSnapshotProfilingPoint_HeaderProjectString(
                        ProjectUtilities.getDisplayName(TakeSnapshotProfilingPoint.this.getProject()));
        }

        private String getHeaderResetResults() {
            return Bundle.TakeSnapshotProfilingPoint_HeaderResetResultsString(
                        TakeSnapshotProfilingPoint.this.getResetResults());
        }

        private String getHeaderTarget() {
            return TakeSnapshotProfilingPoint.this.getSnapshotTarget().equals(TARGET_PROJECT_KEY) ? 
                        Bundle.TakeSnapshotProfilingPoint_HeaderTargetProjectString()
                        : Bundle.TakeSnapshotProfilingPoint_HeaderTargetCustomString(TakeSnapshotProfilingPoint.this.getSnapshotFile());
        }

        private String getHeaderType() {
            return Bundle.TakeSnapshotProfilingPoint_HeaderTypeString(TakeSnapshotProfilingPoint.this.getFactory().getType());
        }

        private void initComponents() {
            setLayout(new BorderLayout());

            JPanel contentsPanel = new JPanel(new BorderLayout());
            contentsPanel.setBackground(UIUtils.getProfilerResultsBackground());
            contentsPanel.setOpaque(true);
            contentsPanel.setBorder(BorderFactory.createMatteBorder(0, 15, 15, 15, UIUtils.getProfilerResultsBackground()));

            headerArea = new HTMLTextArea() {
                    protected void showURL(URL url) {
                        Utils.openLocation(TakeSnapshotProfilingPoint.this.getLocation());
                    }
                };

            JScrollPane headerAreaScrollPane = new JScrollPane(headerArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            headerAreaScrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 15, 0, UIUtils.getProfilerResultsBackground()));
            headerAreaScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
            contentsPanel.add(headerAreaScrollPane, BorderLayout.NORTH);

            dataArea = new HTMLTextArea() {
                    protected void showURL(URL url) {
                        File resolvedFile = null;

                        try {
                            resolvedFile = new File(url.toURI());
                        } catch (URISyntaxException ex) {
                            ex.printStackTrace();
                        }

                        final File snapshotFile = resolvedFile;

                        if ((snapshotFile != null) && snapshotFile.exists()) {
                            String type = TakeSnapshotProfilingPoint.this.getSnapshotType();
                            if (type.equals(TYPE_PROFDATA_KEY) || type.equals(TYPE_HEAPDUMP_KEY)) {
                                ResultsManager.getDefault().openSnapshot(snapshotFile);
                            }
                        } else {
                            ProfilerDialogs.displayWarning(
                                    Bundle.TakeSnapshotProfilingPoint_SnapshotNotAvailableMsg());
                        }
                    }
                };

            JScrollPane dataAreaScrollPane = new JScrollPane(dataArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                             JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            TitledBorder tb = new TitledBorder(Bundle.TakeSnapshotProfilingPoint_DataString());
            tb.setTitleFont(Utils.getTitledBorderFont(tb).deriveFont(Font.BOLD));
            tb.setTitleColor(javax.swing.UIManager.getColor("Label.foreground")); // NOI18N
            dataAreaScrollPane.setBorder(tb);
            dataAreaScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
            dataAreaScrollPane.setBackground(UIUtils.getProfilerResultsBackground());
            contentsPanel.add(dataAreaScrollPane, BorderLayout.CENTER);

            add(contentsPanel, BorderLayout.CENTER);
        }

        private void initDefaults() {
            refreshProperties();
            setFocusable(true);
        }
    }

    private static class Result {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private final String resultString;
        private final int threadId;
        private final long timestamp;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Result(long timestamp, int threadId, String resultString) {
            this.timestamp = timestamp;
            this.threadId = threadId;
            this.resultString = resultString;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public String getResultString() {
            return resultString;
        }

        public int getThreadID() {
            return threadId;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    static final String PROPERTY_TYPE = "p_snapshot"; // NOI18N
    public static final String TYPE_PROFDATA_KEY = "profdata"; // NOI18N
    public static final String TYPE_HEAPDUMP_KEY = "heapdump"; // NOI18N
    static final String PROPERTY_TARGET = "p_target"; // NOI18N
    public static final String TARGET_PROJECT_KEY = "project"; // NOI18N
    public static final String TARGET_CUSTOM_KEY = "custom"; // NOI18N
    static final String PROPERTY_CUSTOM_FILE = "p_file"; // NOI18N
    static final String PROPERTY_RESET_RESULTS = "p_reset_results"; // NOI18N

    // --- Implementation --------------------------------------------------------
    private static final String SNAPSHOT_LOCATION_URLMASK = "file:"; // NOI18N
    private static final String ANNOTATION_ENABLED = "takeSnapshotProfilingPoint"; // NOI18N
    private static final String ANNOTATION_DISABLED = "takeSnapshotProfilingPointD"; // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Annotation annotation;
    private List<Result> results = new ArrayList<>();
    private final Object resultsSync = new Object();
    private String snapshotFile = System.getProperty("java.io.tmpdir"); // NOI18N
    private String snapshotTarget = TARGET_PROJECT_KEY;
    private String snapshotType = TYPE_PROFDATA_KEY;
    private WeakReference<Report> reportReference;
    private boolean resetResults = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    public TakeSnapshotProfilingPoint(String name, Location location, Lookup.Provider project, ProfilingPointFactory factory) {
        super(name, location, project, factory);
        getChangeSupport().addPropertyChangeListener(this);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setResetResults(boolean resetResults) {
        if (this.resetResults == resetResults) {
            return;
        }

        this.resetResults = resetResults;
        getChangeSupport().firePropertyChange(PROPERTY_RESET_RESULTS, !this.resetResults, this.resetResults);
    }

    public boolean getResetResults() {
        return resetResults;
    }

    public void setSnapshotFile(String snapshotFile) {
        if ((snapshotFile == null) || !new File(snapshotFile).exists()) {
            return;
        }

        if ((this.snapshotFile != null) && new File(this.snapshotFile).equals(new File(snapshotFile))) {
            return;
        }

        String oldSnapshotFile = this.snapshotFile;
        this.snapshotFile = snapshotFile;
        getChangeSupport().firePropertyChange(PROPERTY_CUSTOM_FILE, oldSnapshotFile, snapshotFile);
    }

    public String getSnapshotFile() {
        return snapshotFile;
    }

    public void setSnapshotTarget(String snapshotTarget) {
        if (!snapshotTarget.equals(TARGET_PROJECT_KEY) && !snapshotTarget.equals(TARGET_CUSTOM_KEY)) {
            throw new IllegalArgumentException("Invalid snapshot target category: " + snapshotTarget); // NOI18N
        }

        if (this.snapshotTarget.equals(snapshotTarget)) {
            return;
        }

        String oldSnapshotTarget = this.snapshotTarget;
        this.snapshotTarget = snapshotTarget;
        getChangeSupport().firePropertyChange(PROPERTY_TARGET, oldSnapshotTarget, snapshotTarget);
    }

    public String getSnapshotTarget() {
        return snapshotTarget;
    }

    public void setSnapshotType(String snapshotType) {
        if ((snapshotType == null) || !(snapshotType.equals(TYPE_PROFDATA_KEY) || snapshotType.equals(TYPE_HEAPDUMP_KEY))) {
            throw new IllegalArgumentException("Invalid snapshot type: " + snapshotType); // NOI18N
        }

        if (this.snapshotType.equals(snapshotType)) {
            return;
        }

        String oldSnapshotType = this.snapshotType;
        this.snapshotType = snapshotType;
        getChangeSupport().firePropertyChange(PROPERTY_TYPE, oldSnapshotType, snapshotType);
    }

    public String getSnapshotType() {
        return snapshotType;
    }

    public boolean hasResults() {
        synchronized(resultsSync) {
            return !results.isEmpty();
        }
    }

    public void hideResults() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Report report = getReport(false);
                if (report != null) report.close();
            }
        });
    }

    public void propertyChange(PropertyChangeEvent evt) {
        Report report = getReport(false);
        if (report != null) {
            if (evt.getPropertyName() == PROPERTY_NAME) {
                report.refreshProperties();
            }

            report.refresh();
        }
    }

    public void showResults(URL url) {
        TopComponent topComponent = getReport(true);
        topComponent.open();
        topComponent.requestActive();
    }

    protected CodeProfilingPoint.Annotation getAnnotation() {
        if (annotation == null) {
            annotation = new Annotation();
        }

        return annotation;
    }

    protected String getResultsText() {
        synchronized(resultsSync) {
            if (hasResults()) {
                int size = results.size();
                long timeStamp = results.get(size - 1).getTimestamp();
                String time = Utils.formatProfilingPointTime(timeStamp);
                
                return (size == 1)
                       ? Bundle.TakeSnapshotProfilingPoint_OneHitString(time)
                       : Bundle.TakeSnapshotProfilingPoint_NHitsString(size, time);
            } else {
                return Bundle.TakeSnapshotProfilingPoint_NoResultsString();
            }
        }
    }

    protected void updateCustomizer(ValidityAwarePanel c) {
        TakeSnapshotCustomizer customizer = (TakeSnapshotCustomizer) c;
        customizer.setPPName(getName());
        customizer.setPPLocation(getLocation());
        customizer.setPPType(TYPE_PROFDATA_KEY.equals(getSnapshotType()));
        customizer.setPPTarget(TARGET_PROJECT_KEY.equals(getSnapshotTarget()));
        customizer.setPPFile(getSnapshotFile());
        customizer.setPPResetResults(getResetResults());
    }

    String getServerHandlerClassName() {
        if (getSnapshotType().equals(TYPE_HEAPDUMP_KEY)) {
            return "org.netbeans.lib.profiler.server.TakeHeapdumpProfilingPointHandler"; // NOI18N
        }

        if (getResetResults()) {
            return "org.netbeans.lib.profiler.server.TakeSnapshotWithResetProfilingPointHandler"; // NOI18N
        }

        return "org.netbeans.lib.profiler.server.TakeSnapshotProfilingPointHandler"; // NOI18N
    }

    String getServerInfo() {
        if (getSnapshotType().equals(TYPE_HEAPDUMP_KEY)) {
            try {
                return FileUtil.toFile(getSnapshotDirectory()).getAbsolutePath();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            }
        }

        return null;
    }

    FileObject getSnapshotDirectory() throws IOException {
        if (snapshotTarget.equals(TARGET_PROJECT_KEY)) {
            return ProfilerStorage.getProjectFolder(getProject(), true);
        } else {
            File f = new File(snapshotFile);
            f.mkdirs();

            return FileUtil.toFileObject(FileUtil.normalizeFile(f));
        }
    }

    void setValues(ValidityAwarePanel c) {
        TakeSnapshotCustomizer customizer = (TakeSnapshotCustomizer) c;
        setName(customizer.getPPName());
        setLocation(customizer.getPPLocation());
        setSnapshotType(customizer.getPPType() ? TYPE_PROFDATA_KEY : TYPE_HEAPDUMP_KEY);
        setSnapshotTarget(customizer.getPPTarget() ? TARGET_PROJECT_KEY : TARGET_CUSTOM_KEY);
        setSnapshotFile(customizer.getPPFile());
        setResetResults(customizer.getPPResetResults());
        
        Utils.checkLocation(this);
    }

    void hit(RuntimeProfilingPoint.HitEvent hitEvent, int index) {
        String snapshotFilename;

        if (snapshotType.equals(TYPE_HEAPDUMP_KEY)) {
            TargetAppRunner runner = Profiler.getDefault().getTargetAppRunner();

            if (runner.getProfilingSessionStatus().remoteProfiling) {
                snapshotFilename = Bundle.TakeSnapshotProfilingPoint_RemoteUnsupportedMsg();
            } else if (!runner.hasSupportedJDKForHeapDump()) {
                snapshotFilename = Bundle.TakeSnapshotProfilingPoint_NoDataJdkMsg();
            } else {
                snapshotFilename = takeHeapdumpHit(hitEvent.getTimestamp());
            }
        } else {
            snapshotFilename = takeSnapshotHit();
        }

        synchronized(resultsSync) {
            if (ProfilingPointsManager.getDefault().belowMaxHits(results.size()))
                results.add(new Result(hitEvent.getTimestamp(), hitEvent.getThreadId(), snapshotFilename));
        }
        getChangeSupport().firePropertyChange(PROPERTY_RESULTS, false, true);
    }

    void reset() {
        synchronized(resultsSync) {
            boolean change = hasResults();
            results.clear();

            if (change) {
                getChangeSupport().firePropertyChange(PROPERTY_RESULTS, false, true);
            }
        }
    }

    private Report getReport(boolean create) {
        Report report = reportReference == null ? null : reportReference.get();
        if (report == null && create) {
            report = new Report();
            reportReference = new WeakReference<Report>(report);
        }
        return report;
    }

    private File constructHeapDumpFile(long time) throws IOException {
        String name = ResultsManager.getDefault().getDefaultHeapDumpFileName(time) + "." + ResultsManager.HEAPDUMP_EXTENSION;   // NOI18N
        File dir = FileUtil.toFile(getSnapshotDirectory());
        
        return new File(dir.getAbsoluteFile(), name);
    }

    private String takeHeapdumpHit(long time) {
        try {
            File heapdumpFile = constructHeapDumpFile(time);

            if (heapdumpFile.exists()) {
                File fixedHeapdumpFile = constructHeapDumpFile(Utils.getTimeInMillis(time));
                heapdumpFile.renameTo(fixedHeapdumpFile);
                FileObject folder = FileUtil.toFileObject(fixedHeapdumpFile.getParentFile());
                SnapshotsWindow.instance().refreshFolder(folder, true);
//                if (ProfilerControlPanel2.hasDefault())
//                    ProfilerControlPanel2.getDefault().refreshSnapshotsList();

                return fixedHeapdumpFile.toURI().toURL().toExternalForm();
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);

            return Bundle.TakeSnapshotProfilingPoint_NoDataAvailableMsg();
        }

        return Bundle.TakeSnapshotProfilingPoint_NoDataAvailableMsg();
    }

    private static LoadedSnapshot takeSnapshot() throws CPUResultsSnapshot.NoDataAvailableException {
        return ResultsManager.getDefault().prepareSnapshot();
    }

    private String takeSnapshotHit() {
        LoadedSnapshot loadedSnapshot = null;
        String snapshotFilename = null;

        try {
            loadedSnapshot = takeSnapshot();
        } catch (CPUResultsSnapshot.NoDataAvailableException e) {
            //ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            // NOTE: this is actually a supported state - taking snapshots when no data are available, resultString remains null
        }

        if (loadedSnapshot != null) {
            try {
                FileObject snapshotDirectory = getSnapshotDirectory();
                FileObject profFile = snapshotDirectory.createData(ResultsManager.getDefault()
                                                                                 .getDefaultSnapshotFileName(loadedSnapshot),
                                                                   ResultsManager.SNAPSHOT_EXTENSION);
                ResultsManager.getDefault().saveSnapshot(loadedSnapshot, profFile);
                snapshotFilename = FileUtil.toFile(profFile).toURI().toURL().toExternalForm();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            }
        }

        return (snapshotFilename == null) ? Bundle.TakeSnapshotProfilingPoint_NoDataAvailableMsg() : snapshotFilename;
    }
}

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

package org.netbeans.modules.profiler.ppoints;

import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.TargetAppRunner;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.modules.profiler.LoadedSnapshot;
import org.netbeans.modules.profiler.ResultsManager;
import org.netbeans.modules.profiler.ppoints.ui.TimedTakeSnapshotCustomizer;
import org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import java.awt.BorderLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
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
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.ProfilerStorage;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.ppoints.ui.ProfilingPointReport;
import org.netbeans.modules.profiler.v2.SnapshotsWindow;
import org.openide.ErrorManager;
import org.openide.util.Lookup;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "TimedTakeSnapshotProfilingPoint_NoDataAvailableMsg=no data available",
    "TimedTakeSnapshotProfilingPoint_NoDataRemoteMsg=no data available: not supported for remote profiling",
    "TimedTakeSnapshotProfilingPoint_NoDataJdkMsg=no data available: JDK 1.6, 1.7 or 1.5.0_12 is required",
    "TimedTakeSnapshotProfilingPoint_OneHitString=<b>1 hit</b> at {0}, <a href='#'>report</a>",
    "TimedTakeSnapshotProfilingPoint_NHitsString=<b>{0} hits</b>, last at {1}, <a href='#'>report</a>",
    "TimedTakeSnapshotProfilingPoint_NoResultsString=No results available",
    "TimedTakeSnapshotProfilingPoint_ReportAccessDescr=Report of {0}",
    "TimedTakeSnapshotProfilingPoint_HeaderTypeString=<b>Type:</b> {0}",
    "TimedTakeSnapshotProfilingPoint_HeaderEnabledString=<b>Enabled:</b> {0}",
    "TimedTakeSnapshotProfilingPoint_HeaderProjectString=<b>Project:</b> {0}",
    "TimedTakeSnapshotProfilingPoint_HeaderModeDataString=<b>Snapshot type:</b> profiling data",
    "TimedTakeSnapshotProfilingPoint_HeaderModeDumpString=<b>Snapshot type:</b> heap dump",
    "TimedTakeSnapshotProfilingPoint_HeaderTargetProjectString=<b>Save to:</b> project",
    "TimedTakeSnapshotProfilingPoint_HeaderTargetCustomString=<b>Save to:</b> {0}",
    "TimedTakeSnapshotProfilingPoint_HeaderResetResultsString=<b>Reset results:</b> {0}",
    "TimedTakeSnapshotProfilingPoint_HeaderHitsString=<b>Hits:</b> {0}",
    "TimedTakeSnapshotProfilingPoint_OpenSnapshotString=open snapshot",
    "TimedTakeSnapshotProfilingPoint_HitString=<b>{0}.</b> hit at <b>{1}</b>, {2}",
    "TimedTakeSnapshotProfilingPoint_SnapshotNotAvailableMsg=Saved snapshot is no longer available.",
    "TimedTakeSnapshotProfilingPoint_DataString=Data:"
})
public final class TimedTakeSnapshotProfilingPoint extends TimedGlobalProfilingPoint implements PropertyChangeListener {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

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
                    dataAreaTextBuilder.append(ProfilingPointReport.getNoDataHint(TimedTakeSnapshotProfilingPoint.this));
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
            setName(TimedTakeSnapshotProfilingPoint.this.getName());
            setIcon(((ImageIcon) TimedTakeSnapshotProfilingPoint.this.getFactory().getIcon()).getImage());
            getAccessibleContext().setAccessibleDescription(Bundle.TimedTakeSnapshotProfilingPoint_ReportAccessDescr(getName()));
        }

        private String getDataResultItem(int index) {
            synchronized(resultsSync) {
                Result result = results.get(index);
                String resultString = result.getResultString();
                String snapshotInformation = resultString.startsWith(SNAPSHOT_LOCATION_URLMASK)
                                             ? ("<a href='" + resultString + "'>" + Bundle.TimedTakeSnapshotProfilingPoint_OpenSnapshotString() + "</a>") : resultString; // NOI18N

                return Bundle.TimedTakeSnapshotProfilingPoint_HitString(
                        (index + 1), Utils.formatLocalProfilingPointTime(result.getTimestamp()),
                        snapshotInformation);
            }
        }

        private String getHeaderEnabled() {
            return Bundle.TimedTakeSnapshotProfilingPoint_HeaderEnabledString(TimedTakeSnapshotProfilingPoint.this.isEnabled());
        }

        private String getHeaderHitsCount() {
            synchronized(resultsSync) {
                return Bundle.TimedTakeSnapshotProfilingPoint_HeaderHitsString(results.size());
            }
        }

        private String getHeaderMode() {
            return TimedTakeSnapshotProfilingPoint.this.getSnapshotType().equals(TYPE_PROFDATA_KEY) ? 
                        Bundle.TimedTakeSnapshotProfilingPoint_HeaderModeDataString()
                        : Bundle.TimedTakeSnapshotProfilingPoint_HeaderModeDumpString();
        }

        private String getHeaderName() {
            return "<h2><b>" + TimedTakeSnapshotProfilingPoint.this.getName() + "</b></h2>"; // NOI18N
        }

        private String getHeaderProject() {
            return Bundle.TimedTakeSnapshotProfilingPoint_HeaderProjectString(
                        ProjectUtilities.getDisplayName(TimedTakeSnapshotProfilingPoint.this.getProject()));
        }

        private String getHeaderResetResults() {
            return Bundle.TimedTakeSnapshotProfilingPoint_HeaderResetResultsString(
                        TimedTakeSnapshotProfilingPoint.this.getResetResults());
        }

        private String getHeaderTarget() {
            return TimedTakeSnapshotProfilingPoint.this.getSnapshotTarget().equals(TARGET_PROJECT_KEY)
                   ? Bundle.TimedTakeSnapshotProfilingPoint_HeaderTargetProjectString()
                   : Bundle.TimedTakeSnapshotProfilingPoint_HeaderTargetCustomString(
                        TimedTakeSnapshotProfilingPoint.this.getSnapshotFile());
        }

        private String getHeaderType() {
            return Bundle.TimedTakeSnapshotProfilingPoint_HeaderTypeString(
                        TimedTakeSnapshotProfilingPoint.this.getFactory().getType());
        }

        private void initComponents() {
            setLayout(new BorderLayout());

            JPanel contentsPanel = new JPanel(new BorderLayout());
            contentsPanel.setBackground(UIUtils.getProfilerResultsBackground());
            contentsPanel.setOpaque(true);
            contentsPanel.setBorder(BorderFactory.createMatteBorder(0, 15, 15, 15, UIUtils.getProfilerResultsBackground()));

            headerArea = new HTMLTextArea();

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
                            String type = TimedTakeSnapshotProfilingPoint.this.getSnapshotType();
                            if (type.equals(TYPE_PROFDATA_KEY) || type.equals(TYPE_HEAPDUMP_KEY)) {
                                ResultsManager.getDefault().openSnapshot(snapshotFile);
                            }
                        } else {
                            ProfilerDialogs.displayWarning(
                                    Bundle.TimedTakeSnapshotProfilingPoint_SnapshotNotAvailableMsg());
                        }
                    }
                };

            JScrollPane dataAreaScrollPane = new JScrollPane(dataArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                             JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            TitledBorder tb = new TitledBorder(Bundle.TimedTakeSnapshotProfilingPoint_DataString());
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
        private final long timestamp;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Result(long timestamp, String resultString) {
            this.timestamp = timestamp;
            this.resultString = resultString;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public String getResultString() {
            return resultString;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final String TAKEN_HEAPDUMP_PREFIX = "heapdump-"; // NOI18N // should differ from generated OOME heapdumps not to be detected as OOME
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
    private static final String NO_DATA_AVAILABLE_MESSAGE = Bundle.TimedTakeSnapshotProfilingPoint_NoDataAvailableMsg();

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private List<Result> results = new ArrayList<>();
    private final Object resultsSync = new Object();
    private String snapshotFile = System.getProperty("java.io.tmpdir"); // NOI18N
    private String snapshotTarget = TARGET_PROJECT_KEY;
    private String snapshotType = TYPE_PROFDATA_KEY;
    private WeakReference<Report> reportReference;
    private boolean resetResults = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    public TimedTakeSnapshotProfilingPoint(String name, Lookup.Provider project, ProfilingPointFactory factory) {
        super(name, project, factory);
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

    protected String getResultsText() {
        synchronized(resultsSync) {
            if (hasResults()) {
                int size = results.size();
                long timeStamp = results.get(size - 1).getTimestamp();
                String time = Utils.formatLocalProfilingPointTime(timeStamp);

                return (size == 1)
                       ? Bundle.TimedTakeSnapshotProfilingPoint_OneHitString(time)
                       : Bundle.TimedTakeSnapshotProfilingPoint_NHitsString(size, time);
            } else {
                return Bundle.TimedTakeSnapshotProfilingPoint_NoResultsString();
            }
        }
    }

    protected void updateCustomizer(ValidityAwarePanel c) {
        TimedTakeSnapshotCustomizer customizer = (TimedTakeSnapshotCustomizer) c;
        customizer.setPPName(getName());
        customizer.setPPType(TYPE_PROFDATA_KEY.equals(getSnapshotType()));
        customizer.setPPTarget(TARGET_PROJECT_KEY.equals(getSnapshotTarget()));
        customizer.setPPFile(getSnapshotFile());
        customizer.setPPResetResults(getResetResults());
        customizer.setTimeCondition(getCondition());
    }

    // ---
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
        TimedTakeSnapshotCustomizer customizer = (TimedTakeSnapshotCustomizer) c;
        setName(customizer.getPPName());
        setSnapshotType(customizer.getPPType() ? TYPE_PROFDATA_KEY : TYPE_HEAPDUMP_KEY);
        setSnapshotTarget(customizer.getPPTarget() ? TARGET_PROJECT_KEY : TARGET_CUSTOM_KEY);
        setSnapshotFile(customizer.getPPFile());
        setResetResults(customizer.getPPResetResults());
        setCondition(customizer.getTimeCondition());
    }

    void hit(long hitValue) {
        String snapshotFilename;
        long currentTime = System.currentTimeMillis();

        if (snapshotType.equals(TYPE_HEAPDUMP_KEY)) {
            snapshotFilename = takeHeapdumpHit();
        } else {
            snapshotFilename = takeSnapshotHit();

            if (getResetResults()) {
                try {
                    ResultsManager.getDefault().reset();
                    
                    TargetAppRunner runner = Profiler.getDefault().getTargetAppRunner();

                    if (runner.targetJVMIsAlive()) {
                        runner.resetTimers();
                    }
                } catch (ClientUtils.TargetAppOrVMTerminated targetAppOrVMTerminated) {
                } // ignore
            }
        }

        synchronized(resultsSync) {
            if (ProfilingPointsManager.getDefault().belowMaxHits(results.size()))
                results.add(new Result(currentTime, snapshotFilename));
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

    private String getCurrentHeapDumpFilename() {
        try {
            String fileName = TAKEN_HEAPDUMP_PREFIX + System.currentTimeMillis();
            FileObject folder = getSnapshotDirectory();

            //      FileObject folder = targetFolder == null ? IDEUtils.getProjectSettingsFolder(NetBeansProfiler.getDefaultNB().getProfiledProject()) : FileUtil.toFileObject(new File(targetFolder));            
            return FileUtil.toFile(folder).getAbsolutePath() + File.separator
                   + FileUtil.findFreeFileName(folder, fileName, ResultsManager.HEAPDUMP_EXTENSION) + "."  // NOI18N
                   + ResultsManager.HEAPDUMP_EXTENSION; // NOI18N
        } catch (IOException e) {
            return null;
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

    private String takeHeapdumpHit() {
        TargetAppRunner runner = Profiler.getDefault().getTargetAppRunner();

        if (runner.getProfilingSessionStatus().remoteProfiling) {
            return Bundle.TimedTakeSnapshotProfilingPoint_NoDataRemoteMsg();
        }

        if (!runner.hasSupportedJDKForHeapDump()) {
            return Bundle.TimedTakeSnapshotProfilingPoint_NoDataJdkMsg();
        }

        String dumpFileName = getCurrentHeapDumpFilename();

        if (dumpFileName == null) {
            return NO_DATA_AVAILABLE_MESSAGE;
        }

        boolean heapdumpTaken = false;

        try {
            heapdumpTaken = runner.getProfilerClient().takeHeapDump(dumpFileName);
        } catch (Exception ex) {
            ProfilerLogger.log(ex);
        }

        if (heapdumpTaken) {
//            if (ProfilerControlPanel2.hasDefault())
//                ProfilerControlPanel2.getDefault().refreshSnapshotsList();

            try {
                File file = new File(dumpFileName);
                FileObject folder = FileUtil.toFileObject(file.getParentFile());
                SnapshotsWindow.instance().refreshFolder(folder, true);
                return file.toURI().toURL().toExternalForm();
            } catch (MalformedURLException ex) {
                ProfilerLogger.log(ex);

                return NO_DATA_AVAILABLE_MESSAGE;
            }
        } else {
            return NO_DATA_AVAILABLE_MESSAGE;
        }
    }

    private static LoadedSnapshot takeSnapshot() {
        return ResultsManager.getDefault().prepareSnapshot();
    }

    private String takeSnapshotHit() {
        LoadedSnapshot loadedSnapshot = null;
        String snapshotFilename = null;
        loadedSnapshot = takeSnapshot();

        if (loadedSnapshot != null) {
            try {
                FileObject snapshotDirectory = getSnapshotDirectory();
                FileObject profFile = snapshotDirectory.createData(ResultsManager.getDefault()
                                                                                 .getDefaultSnapshotFileName(loadedSnapshot),
                                                                   ResultsManager.SNAPSHOT_EXTENSION);
                ResultsManager.getDefault().saveSnapshot(loadedSnapshot, profFile); // Also updates list of snapshots in ProfilerControlPanel2
                snapshotFilename = FileUtil.toFile(profFile).toURI().toURL().toExternalForm();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            }
        }

        return (snapshotFilename == null) ? NO_DATA_AVAILABLE_MESSAGE : snapshotFilename;
    }
}

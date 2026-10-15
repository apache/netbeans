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

import org.netbeans.lib.profiler.client.RuntimeProfilingPoint.HitEvent;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.modules.profiler.ppoints.ui.LoadGeneratorCustomizer;
import org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel;
import org.netbeans.modules.profiler.spi.LoadGenPlugin;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import java.awt.BorderLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.ppoints.ui.ProfilingPointReport;


/**
 *
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "LoadGenProfilingPoint_OneHitString=<b>1 hit</b> at {0}, <a href='#'>report</a>",
    "LoadGenProfilingPoint_NHitsString=<b>{0} hits</b>, last at {1}, <a href='#'>report</a>",
    "LoadGenProfilingPoint_NoResultsString=No results available",
    "LoadGenProfilingPoint_AnnotationStartString={0} (start)",
    "LoadGenProfilingPoint_AnnotationEndString={0} (end)",
    "LoadGenProfilingPoint_ReportAccessDescr=Report of {0}",
    "LoadGenProfilingPoint_HeaderTypeString=<b>Type:</b> {0}",
    "LoadGenProfilingPoint_HeaderEnabledString=<b>Enabled:</b> {0}",
    "LoadGenProfilingPoint_HeaderProjectString=<b>Project:</b> {0}",
    "LoadGenProfilingPoint_HeaderLocationString=<b>Location:</b> {0}, line {1}",
    "LoadGenProfilingPoint_HeaderStartLocationString=<b>Start location:</b> {0}, line {1}",
    "LoadGenProfilingPoint_HeaderEndLocationString=<b>Stop location:</b> {0}, line {1}",
    "LoadGenProfilingPoint_HeaderHitsString=<b>Hits:</b> {0}",
    "LoadGenProfilingPoint_HitSuccessString=<b>{0}.</b> hit at <b>{1}</b>",
    "LoadGenProfilingPoint_HitFailedString=<b>{0}.</b> hit at <b>{1}</b>, <b>action failed!</b>",
    "LoadGenProfilingPoint_DataString=Data:"
})
public class LoadGenProfilingPoint extends CodeProfilingPoint.Paired implements PropertyChangeListener {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class Annotation extends CodeProfilingPoint.Annotation {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private boolean isStartAnnotation;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Annotation(boolean isStartAnnotation) {
            this.isStartAnnotation = isStartAnnotation;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        public String getAnnotationType() {
            return LoadGenProfilingPoint.this.isEnabled() ? ANNOTATION_ENABLED : ANNOTATION_DISABLED;
        }

        @Override
        public String getShortDescription() {
            if (!usesEndLocation()) {
                return getName();
            }

            return isStartAnnotation ? Bundle.LoadGenProfilingPoint_AnnotationStartString(getName())
                                     : Bundle.LoadGenProfilingPoint_AnnotationEndString(getName());
        }

        @Override
        public CodeProfilingPoint profilingPoint() {
            return LoadGenProfilingPoint.this;
        }
    }

    private static class Result {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private final int threadId;
        private final long timestamp;
        private boolean success = false;
        private long endTimestamp = -1;
        private long startTime;
        private long stopTime;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Result(long timestamp, int threadId, boolean success) {
            this.timestamp = timestamp;
            this.threadId = threadId;
            this.success = success;
            this.startTime = System.currentTimeMillis();
            this.stopTime = 0L;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public long getDuration() {
            return (stopTime > startTime) ? (stopTime - startTime) : (-1L);
        }

        public void setEndTimestamp(long endTimestamp) {
            this.endTimestamp = endTimestamp;
        }

        public long getEndTimestamp() {
            return endTimestamp;
        }

        public void setStopTime() {
            this.stopTime = System.currentTimeMillis();
        }

        public boolean isSuccess() {
            return success;
        }

        public int getThreadID() {
            return threadId;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private class Report extends ProfilingPointReport {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        private static final String START_LOCATION_URLMASK = "file:/1"; // NOI18N
        private static final String END_LOCATION_URLMASK = "file:/2"; // NOI18N

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
            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;"); // NOI18N
            headerAreaTextBuilder.append(getHeaderType());
            headerAreaTextBuilder.append("<br>"); // NOI18N
            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;"); // NOI18N
            headerAreaTextBuilder.append(getHeaderEnabled());
            headerAreaTextBuilder.append("<br>"); // NOI18N
            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;"); // NOI18N
            headerAreaTextBuilder.append(getHeaderProject());
            headerAreaTextBuilder.append("<br>"); // NOI18N
            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;"); // NOI18N
            headerAreaTextBuilder.append(getHeaderStartLocation());
            headerAreaTextBuilder.append("<br>"); // NOI18N

            if (LoadGenProfilingPoint.this.usesEndLocation()) {
                headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;"); // NOI18N
                headerAreaTextBuilder.append(getHeaderEndLocation());
                headerAreaTextBuilder.append("<br>"); // NOI18N
            }

            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;"); // NOI18N
            headerAreaTextBuilder.append(getHeaderHitsCount());

            headerArea.setText(headerAreaTextBuilder.toString());

            StringBuilder dataAreaTextBuilder = new StringBuilder();

            synchronized(resultsSync) {
                if (results.isEmpty()) {
                    dataAreaTextBuilder.append(ProfilingPointReport.getNoDataHint(LoadGenProfilingPoint.this));
                } else {
                    if (results.size() > 1) {
                        results.sort((o1, o2) -> Long.compare(o1.getTimestamp(), o2.getTimestamp()));
                    }

                    for (int i = 0; i < results.size(); i++) {
                        dataAreaTextBuilder.append("&nbsp;&nbsp;"); // NOI18N
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
            setName(LoadGenProfilingPoint.this.getName());
            setIcon(((ImageIcon) LoadGenProfilingPoint.this.getFactory().getIcon()).getImage());
            getAccessibleContext().setAccessibleDescription(Bundle.LoadGenProfilingPoint_ReportAccessDescr(getName()));
        }

        private String getDataResultItem(int index) {
            synchronized(resultsSync) {
                Result result = results.get(index);

                // TODO: enable once thread name by id is available
                //String threadName = Utils.getThreadName(result.getThreadID());
                //String threadClassName = Utils.getThreadClassName(result.getThreadID());
                //String threadInformation = (threadName == null ? "&lt;unknown thread&gt;" : (threadClassName == null ? threadName : threadName + " (" + threadClassName + ")"));
                String hitTime = Utils.formatProfilingPointTimeHiRes(result.getTimestamp());

                //      if (!LoadGenProfilingPoint.this.usesEndLocation()) {
                //return "<b>" + (index + 1) + ".</b> hit at <b>" + hitTime + "</b> by " + threadInformation;
                if (result.isSuccess()) {
                    return Bundle.LoadGenProfilingPoint_HitSuccessString((index + 1), hitTime);
                } else {
                    return Bundle.LoadGenProfilingPoint_HitFailedString((index + 1), hitTime);
                }

                //      } else if (result.getEndTimestamp() == -1) {
                //        //return "<b>" + (index + 1) + ".</b> hit at <b>" + hitTime + "</b>, duration pending..., thread " + threadInformation;
                //        return "<b>" + (index + 1) + ".</b> hit at <b>" + hitTime + "</b>, duration <b>" + (result.getDuration() / 1000d) + "s</b>(pending...)";
                //      } else {
                //        //return "<b>" + (index + 1) + ".</b> hit at <b>" + hitTime + "</b>, duration <b>" + Utils.getDurationInMicroSec(result.getTimestamp(),result.getEndTimestamp()) + " &micro;s</b>, thread " + threadInformation;
                //        return "<b>" + (index + 1) + ".</b> hit at <b>" + hitTime + "</b>, duration <b>" + (result.getDuration() / 1000d) + " s</b>";
                //      }
            }
        }

        private String getHeaderEnabled() {
            return Bundle.LoadGenProfilingPoint_HeaderEnabledString(LoadGenProfilingPoint.this.isEnabled());
        }

        private String getHeaderEndLocation() {
            CodeProfilingPoint.Location location = LoadGenProfilingPoint.this.getEndLocation();
            File file = new File(location.getFile());
            String shortFileName = file.getName();
            int lineNumber = location.getLine();
            String locationUrl = "<a href='" + END_LOCATION_URLMASK + "'>"; // NOI18N

            return Bundle.LoadGenProfilingPoint_HeaderEndLocationString(locationUrl + shortFileName, lineNumber)
                   + "</a>"; // NOI18N
        }

        private String getHeaderHitsCount() {
            synchronized(resultsSync) {
                return Bundle.LoadGenProfilingPoint_HeaderHitsString(results.size());
            }
        }

        private String getHeaderName() {
            return "<h2><b>" + LoadGenProfilingPoint.this.getName() + "</b></h2>"; // NOI18N
        }

        private String getHeaderProject() {
            return Bundle.LoadGenProfilingPoint_HeaderProjectString(
                        ProjectUtilities.getDisplayName(LoadGenProfilingPoint.this.getProject()));
        }

        private String getHeaderStartLocation() {
            CodeProfilingPoint.Location location = LoadGenProfilingPoint.this.getStartLocation();
            File file = new File(location.getFile());
            String shortFileName = file.getName();
            int lineNumber = location.getLine();
            String locationUrl = "<a href='" + START_LOCATION_URLMASK + "'>"; // NOI18N

            return LoadGenProfilingPoint.this.usesEndLocation()
                   ? (Bundle.LoadGenProfilingPoint_HeaderStartLocationString(locationUrl + shortFileName, lineNumber)
                   + "</a>")
                   : (Bundle.LoadGenProfilingPoint_HeaderLocationString(locationUrl + shortFileName, lineNumber)
                   + "</a>"); // NOI18N
        }

        private String getHeaderType() {
            return Bundle.LoadGenProfilingPoint_HeaderTypeString(LoadGenProfilingPoint.this.getFactory().getType());
        }

        private void initComponents() {
            setLayout(new BorderLayout());

            JPanel contentsPanel = new JPanel(new BorderLayout());
            contentsPanel.setBackground(UIUtils.getProfilerResultsBackground());
            contentsPanel.setOpaque(true);
            contentsPanel.setBorder(BorderFactory.createMatteBorder(0, 15, 15, 15, UIUtils.getProfilerResultsBackground()));

            headerArea = new HTMLTextArea() {
                    protected void showURL(URL url) {
                        String urlString = url.toString();

                        if (START_LOCATION_URLMASK.equals(urlString)) {
                            Utils.openLocation(LoadGenProfilingPoint.this.getStartLocation());
                        } else if (LoadGenProfilingPoint.this.usesEndLocation()) {
                            Utils.openLocation(LoadGenProfilingPoint.this.getEndLocation());
                        }
                    }
                };

            JScrollPane headerAreaScrollPane = new JScrollPane(headerArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            headerAreaScrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 15, 0, UIUtils.getProfilerResultsBackground()));
            headerAreaScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
            contentsPanel.add(headerAreaScrollPane, BorderLayout.NORTH);

            dataArea = new HTMLTextArea();

            JScrollPane dataAreaScrollPane = new JScrollPane(dataArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                             JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            TitledBorder tb = new TitledBorder(Bundle.LoadGenProfilingPoint_DataString());
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

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final Logger LOGGER = Logger.getLogger(LoadGenProfilingPoint.class.getName());
    public static final String PROPERTY_SCRIPTNAME = "p_ScriptName"; // NOI18N
    private static final String ANNOTATION_ENABLED = "loadgenProfilingPoint"; // NOI18N
    private static final String ANNOTATION_DISABLED = "loadgenProfilingPointD"; // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Annotation endAnnotation;
    private Annotation startAnnotation;
    private final List<Result> results = new ArrayList<LoadGenProfilingPoint.Result>();
    private final Object resultsSync = new Object();
    private String scriptFileName;
    private WeakReference<Report> reportReference;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of LoadGenProfilingPoint */
    public LoadGenProfilingPoint(String name, Location startLocation, Location endLocation, Lookup.Provider project, ProfilingPointFactory factory) {
        super(name, startLocation, endLocation, project, factory);
        getChangeSupport().addPropertyChangeListener(this);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    @Override
    public void setEnabled(boolean value) {
        LoadGenPlugin lg = Lookup.getDefault().lookup(LoadGenPlugin.class);

        if (lg != null) {
            super.setEnabled(value);
        } else {
            LOGGER.warning("Can not enable the Load Generator profiling point. The appropriate modules are not installed."); // NOI18N
        }
    }

    @Override
    public boolean isEnabled() {
        boolean retValue;

        retValue = super.isEnabled();

        if (retValue) {
            LoadGenPlugin lg = Lookup.getDefault().lookup(LoadGenPlugin.class);
            retValue &= (lg != null);
        }

        return retValue;
    }

    public String getScriptFileName() {
        return (scriptFileName != null) ? scriptFileName : ""; // NOI18N
    }

    public void setSriptFileName(String fileName) {
        scriptFileName = fileName;
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

    protected CodeProfilingPoint.Annotation getEndAnnotation() {
        if (!usesEndLocation()) {
            endAnnotation = null;
        } else if (endAnnotation == null) {
            endAnnotation = new Annotation(false);
        }

        return endAnnotation;
    }

    protected String getResultsText() {
        synchronized(resultsSync) {
            if (hasResults()) {
                int size = results.size();
                long timeStamp = results.get(size - 1).getTimestamp();
                String time = Utils.formatProfilingPointTime(timeStamp);

                return (size == 1)
                       ? Bundle.LoadGenProfilingPoint_OneHitString(time)
                       : Bundle.LoadGenProfilingPoint_NHitsString(size, time);
            } else {
                return Bundle.LoadGenProfilingPoint_NoResultsString();
            }
        }
    }

    protected CodeProfilingPoint.Annotation getStartAnnotation() {
        if (startAnnotation == null) {
            startAnnotation = new Annotation(true);
        }

        return startAnnotation;
    }

    protected void updateCustomizer(ValidityAwarePanel c) {
        LoadGeneratorCustomizer customizer = (LoadGeneratorCustomizer) c;
        customizer.setPPName(getName());
        customizer.setPPStartLocation(getStartLocation());
        customizer.setPPEndLocation(getEndLocation());
        customizer.setScriptFile(getScriptFileName());
        customizer.setProject(getProject());
    }

    protected boolean usesEndLocation() {
        return getEndLocation() != null;
    }

    void setValues(ValidityAwarePanel c) {
        LoadGeneratorCustomizer customizer = (LoadGeneratorCustomizer) c;
        setName(customizer.getPPName());
        setStartLocation(customizer.getPPStartLocation());
        setEndLocation(customizer.getPPEndLocation());
        setSriptFileName(customizer.getScriptFile());
        
        Utils.checkLocation(this);
    }

    void hit(final HitEvent hitEvent, int index) {
        LoadGenPlugin lg = Lookup.getDefault().lookup(LoadGenPlugin.class);
        synchronized(resultsSync) {
            if (usesEndLocation() && (index == 1)) {
                if (lg != null) {
                    lg.stop(getScriptFileName());

                    for (Result result : results) {
                        if ((result.getEndTimestamp() == -1) && (result.getThreadID() == hitEvent.getThreadId())) {
                            result.setEndTimestamp(hitEvent.getTimestamp());

                            break;
                        }
                    }
                }
            } else {
                if (lg != null) {
                    lg.start(getScriptFileName(),
                             new LoadGenPlugin.Callback() {
                            private long correlationId = hitEvent.getTimestamp();

                            public void afterStart(LoadGenPlugin.Result result) {
                                if (ProfilingPointsManager.getDefault().belowMaxHits(results.size())) {
                                    Result rslt = new Result(hitEvent.getTimestamp(), hitEvent.getThreadId(),
                                                            result == LoadGenPlugin.Result.SUCCESS);
                                    results.add(rslt);
                                    correlationId = hitEvent.getTimestamp();
                                    getChangeSupport().firePropertyChange(PROPERTY_RESULTS, false, true);
                                }
                            }

                            public void afterStop(LoadGenPlugin.Result result) {
                                for (Result rslt : results) {
                                    if (rslt.getTimestamp() == correlationId) {
                                        rslt.setEndTimestamp(correlationId);
                                        rslt.setStopTime();

                                        break;
                                    }
                                }

                                getChangeSupport().firePropertyChange(PROPERTY_RESULTS, false, true);
                            }
                        });
                }
            }
        }
    }

    void reset() {
        synchronized(resultsSync) {
            boolean change = results.size() > 0;
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
}

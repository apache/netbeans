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

import org.netbeans.lib.profiler.client.RuntimeProfilingPoint;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.modules.profiler.ppoints.ui.StopwatchCustomizer;
import org.netbeans.modules.profiler.ppoints.ui.ValidityAwarePanel;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import java.awt.BorderLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.ppoints.ui.ProfilingPointReport;
import org.openide.util.Lookup;


/**
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "StopwatchProfilingPoint_OneHitString=<b>1 hit</b> at {0}, <a href='#'>report</a>",
    "StopwatchProfilingPoint_NHitsString=<b>{0} hits</b>, last at {1}, <a href='#'>report</a>",
    "StopwatchProfilingPoint_NoResultsString=No results available",
    "StopwatchProfilingPoint_ReportAccessDescr=Report of {0}",
    "StopwatchProfilingPoint_HeaderTypeString=<b>Type:</b> {0}",
    "StopwatchProfilingPoint_HeaderEnabledString=<b>Enabled:</b> {0}",
    "StopwatchProfilingPoint_HeaderProjectString=<b>Project:</b> {0}",
    "StopwatchProfilingPoint_HeaderLocationString=<b>Location:</b> {0}, line {1}",
    "StopwatchProfilingPoint_HeaderStartLocationString=<b>Start location:</b> {0}, line {1}",
    "StopwatchProfilingPoint_HeaderEndLocationString=<b>Stop location:</b> {0}, line {1}",
    "StopwatchProfilingPoint_HeaderMeasureDurationString=<b>Measure:</b> Timestamp and duration",
    "StopwatchProfilingPoint_HeaderMeasureTimestampString=<b>Measure:</b> Timestamp",
    "StopwatchProfilingPoint_HeaderHitsString=<b>Hits:</b> {0}",
    "StopwatchProfilingPoint_HitString=<b>{0}.</b> hit at <b>{1}</b>",
    "StopwatchProfilingPoint_HitDurationPendingString=<b>{0}.</b> hit at <b>{1}</b>, duration pending...",
    "StopwatchProfilingPoint_HitDurationKnownString=<b>{0}.</b> hit at <b>{1}</b>, duration <b>{2} &micro;s</b>",
    "StopwatchProfilingPoint_DataString=Data:",
    "StopwatchProfilingPoint_AnnotationStartString={0} (start)",
    "StopwatchProfilingPoint_AnnotationEndString={0} (end)"
})
public final class StopwatchProfilingPoint extends CodeProfilingPoint.Paired implements PropertyChangeListener {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class Annotation extends CodeProfilingPoint.Annotation {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private boolean isStartAnnotation;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Annotation(boolean isStartAnnotation) {
            super();
            this.isStartAnnotation = isStartAnnotation;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        public String getAnnotationType() {
            return StopwatchProfilingPoint.this.isEnabled() ? ANNOTATION_ENABLED : ANNOTATION_DISABLED;
        }

        @Override
        public String getShortDescription() {
            if (!usesEndLocation()) {
                return getName();
            }

            return isStartAnnotation ? Bundle.StopwatchProfilingPoint_AnnotationStartString(getName())
                                     : Bundle.StopwatchProfilingPoint_AnnotationEndString(getName());
        }

        @Override
        public CodeProfilingPoint profilingPoint() {
            return StopwatchProfilingPoint.this;
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

//        public int getPersistenceType() {
//            return TopComponent.PERSISTENCE_NEVER;
//        }
//
//        protected String preferredID() {
//            return this.getClass().getName();
//        }

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
            headerAreaTextBuilder.append(getHeaderStartLocation());
            headerAreaTextBuilder.append("<br>"); // NOI18N

            if (StopwatchProfilingPoint.this.usesEndLocation()) {
                headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                headerAreaTextBuilder.append(getHeaderEndLocation());
                headerAreaTextBuilder.append("<br>");
            } // NOI18N

            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            headerAreaTextBuilder.append(getHeaderMeasureLocation());
            headerAreaTextBuilder.append("<br>"); // NOI18N
            headerAreaTextBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            headerAreaTextBuilder.append(getHeaderHitsCount()); // NOI18N

            headerArea.setText(headerAreaTextBuilder.toString());

            StringBuilder dataAreaTextBuilder = new StringBuilder();

            synchronized(resultsSync) {
                if (results.isEmpty()) {
                    dataAreaTextBuilder.append(ProfilingPointReport.getNoDataHint(StopwatchProfilingPoint.this));
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
            setName(StopwatchProfilingPoint.this.getName());
            setIcon(((ImageIcon) StopwatchProfilingPoint.this.getFactory().getIcon()).getImage());
            getAccessibleContext().setAccessibleDescription(Bundle.StopwatchProfilingPoint_ReportAccessDescr(getName()));
        }

        private String getDataResultItem(int index) {
            synchronized(resultsSync) {
                Result result = results.get(index);

                // TODO: enable once thread name by id is available
                //String threadName = Utils.getThreadName(result.getThreadID());
                //String threadClassName = Utils.getThreadClassName(result.getThreadID());
                //String threadInformation = (threadName == null ? "&lt;unknown thread&gt;" : (threadClassName == null ? threadName : threadName + " (" + threadClassName + ")"));
                String hitTime = Utils.formatProfilingPointTimeHiRes(result.getTimestamp());

                if (!StopwatchProfilingPoint.this.usesEndLocation()) {
                    //return "<b>" + (index + 1) + ".</b> hit at <b>" + hitTime + "</b> by " + threadInformation;
                    return Bundle.StopwatchProfilingPoint_HitString((index + 1), hitTime);
                } else if (result.getEndTimestamp() == -1) {
                    //return "<b>" + (index + 1) + ".</b> hit at <b>" + hitTime + "</b>, duration pending..., thread " + threadInformation;
                    return Bundle.StopwatchProfilingPoint_HitDurationPendingString((index + 1), hitTime);
                } else {
                    //return "<b>" + (index + 1) + ".</b> hit at <b>" + hitTime + "</b>, duration <b>" + Utils.getDurationInMicroSec(result.getTimestamp(),result.getEndTimestamp()) + " &micro;s</b>, thread " + threadInformation;
                    return Bundle.StopwatchProfilingPoint_HitDurationKnownString(
                                (index + 1), 
                                hitTime,
                                Utils.getDurationInMicroSec(result.getTimestamp(),
                                    result.getEndTimestamp() - result.getTimeAdjustment()));
                }
            }
        }

        private String getHeaderEnabled() {
            return Bundle.StopwatchProfilingPoint_HeaderEnabledString(StopwatchProfilingPoint.this.isEnabled());
        }

        private String getHeaderEndLocation() {
            CodeProfilingPoint.Location location = StopwatchProfilingPoint.this.getEndLocation();
            File file = new File(location.getFile());
            String shortFileName = file.getName();
            int lineNumber = location.getLine();
            String locationUrl = "<a href='" + END_LOCATION_URLMASK + "'>"; // NOI18N

            return Bundle.StopwatchProfilingPoint_HeaderEndLocationString(locationUrl + shortFileName, lineNumber)
                   + "</a>"; // NOI18N
        }

        private String getHeaderHitsCount() {
            synchronized(resultsSync) {
                return Bundle.StopwatchProfilingPoint_HeaderHitsString(results.size());
            }
        }

        private String getHeaderMeasureLocation() {
            return StopwatchProfilingPoint.this.usesEndLocation() ?
                        Bundle.StopwatchProfilingPoint_HeaderMeasureDurationString() : 
                        Bundle.StopwatchProfilingPoint_HeaderMeasureTimestampString();
        }

        private String getHeaderName() {
            return "<h2><b>" + StopwatchProfilingPoint.this.getName() + "</b></h2>"; // NOI18N
        }

        private String getHeaderProject() {
            return Bundle.StopwatchProfilingPoint_HeaderProjectString(
                        ProjectUtilities.getDisplayName(StopwatchProfilingPoint.this.getProject()));
        }

        private String getHeaderStartLocation() {
            CodeProfilingPoint.Location location = StopwatchProfilingPoint.this.getStartLocation();
            File file = new File(location.getFile());
            String shortFileName = file.getName();
            int lineNumber = location.getLine();
            String locationUrl = "<a href='" + START_LOCATION_URLMASK + "'>"; // NOI18N

            return StopwatchProfilingPoint.this.usesEndLocation()
                   ? (Bundle.StopwatchProfilingPoint_HeaderStartLocationString(locationUrl + shortFileName, lineNumber)
                   + "</a>")
                   : (Bundle.StopwatchProfilingPoint_HeaderLocationString(locationUrl + shortFileName, lineNumber)
                   + "</a>"); // NOI18N
        }

        private String getHeaderType() {
            return Bundle.StopwatchProfilingPoint_HeaderTypeString(StopwatchProfilingPoint.this.getFactory().getType());
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
                            Utils.openLocation(StopwatchProfilingPoint.this.getStartLocation());
                        } else if (StopwatchProfilingPoint.this.usesEndLocation()) {
                            Utils.openLocation(StopwatchProfilingPoint.this.getEndLocation());
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
            TitledBorder tb = new TitledBorder(Bundle.StopwatchProfilingPoint_DataString());
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

        private final int threadId;
        private final long timestamp;
        private long endTimestamp = -1;
        private long timeAdjustment;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        private Result(long timestamp, int threadId) {
            this.timestamp = timestamp;
            this.threadId = threadId;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        private void setEndTimestamp(long endTimestamp) {
            this.endTimestamp = endTimestamp;
        }

        private long getEndTimestamp() {
            return endTimestamp;
        }

        private int getThreadID() {
            return threadId;
        }

        private long getTimeAdjustment() {
            return timeAdjustment;
        }

        private long getTimestamp() {
            return timestamp;
        }

        private void timeAdjust(long timeDiff) {
            timeAdjustment += timeDiff;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // --- Implementation --------------------------------------------------------
    private static final String ANNOTATION_ENABLED = "stopwatchProfilingPoint"; // NOI18N
    private static final String ANNOTATION_DISABLED = "stopwatchProfilingPointD"; // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Annotation endAnnotation;
    private Annotation startAnnotation;
    private List<Result> results = new ArrayList<>();
    private final Object resultsSync = new Object();
    private WeakReference<Report> reportReference;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    public StopwatchProfilingPoint(String name, Location startLocation, Location endLocation, Lookup.Provider project, ProfilingPointFactory factory) {
        super(name, startLocation, endLocation, project, factory);
        getChangeSupport().addPropertyChangeListener(this);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean hasResults() {
        synchronized(resultsSync) {
            return results.size() > 0;
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
                       ? Bundle.StopwatchProfilingPoint_OneHitString(time)
                       : Bundle.StopwatchProfilingPoint_NHitsString(size, time);
            } else {
                return Bundle.StopwatchProfilingPoint_NoResultsString();
            }
        }
    }

    protected CodeProfilingPoint.Annotation getStartAnnotation() {
        if (startAnnotation == null) {
            startAnnotation = new Annotation(true);
        }

        return startAnnotation;
    }

    protected void timeAdjust(final int threadId, final long timeDiff0, final long timeDiff1) {
        if (usesEndLocation()) { // we have start and stop StopwatchProfilingPoint
            synchronized(resultsSync) {
                for (Result result : results) {
                    if ((result.getEndTimestamp() == -1) && (result.getThreadID() == threadId)) {
                        //System.out.println("Time adjust thread "+threadId+" time "+Long.toHexString(timeDiff1)+ " diff "+Long.toHexString(timeDiff0));
                        result.timeAdjust(timeDiff0);
                    }
                }
            }
        }
    }

    protected void updateCustomizer(ValidityAwarePanel c) {
        StopwatchCustomizer customizer = (StopwatchCustomizer) c;
        customizer.setPPName(getName());
        customizer.setPPStartLocation(getStartLocation());
        customizer.setPPEndLocation(getEndLocation());
    }

    protected boolean usesEndLocation() {
        return getEndLocation() != null;
    }

    void setValues(ValidityAwarePanel c) {
        StopwatchCustomizer customizer = (StopwatchCustomizer) c;
        setName(customizer.getPPName());
        setStartLocation(customizer.getPPStartLocation());
        setEndLocation(customizer.getPPEndLocation());
        
        Utils.checkLocation(this);
    }

    void hit(RuntimeProfilingPoint.HitEvent hitEvent, int index) {
        synchronized(resultsSync) {
            if (!usesEndLocation() || (index == 0)) {
                // TODO: should endpoint hit before startpoint hit be processed somehow?
                if (ProfilingPointsManager.getDefault().belowMaxHits(results.size()))
                    results.add(new Result(hitEvent.getTimestamp(), hitEvent.getThreadId()));

                //System.out.println("Time start  thread "+hitEvent.getThreadId()+" time "+Long.toHexString(hitEvent.getTimestamp()));
            } else {
                for (Result result : results) {
                    if ((result.getEndTimestamp() == -1) && (result.getThreadID() == hitEvent.getThreadId())) {
                        result.setEndTimestamp(hitEvent.getTimestamp());

                        //System.out.println("Time end    thread "+hitEvent.getThreadId()+" time "+Long.toHexString(hitEvent.getTimestamp()));
                        break;
                    }
                }

                // TODO: endpoind hit without startpoint hit, what to do?
            }
        }

        getChangeSupport().firePropertyChange(PROPERTY_RESULTS, false, true);
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

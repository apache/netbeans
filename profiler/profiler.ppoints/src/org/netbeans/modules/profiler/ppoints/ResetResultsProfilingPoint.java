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

import org.netbeans.lib.profiler.client.RuntimeProfilingPoint;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.modules.profiler.ppoints.ui.ResetResultsCustomizer;
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
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ResetResultsProfilingPoint_OneHitString=<b>1 hit</b> at {0}, <a href='#'>report</a>",
    "ResetResultsProfilingPoint_NHitsString=<b>{0} hits</b>, last at {1}, <a href='#'>report</a>",
    "ResetResultsProfilingPoint_NoResultsString=No results available",
    "ResetResultsProfilingPoint_ReportAccessDescr=Report of {0}",
    "ResetResultsProfilingPoint_NoHitsString=no hits",
    "ResetResultsProfilingPoint_HeaderTypeString=<b>Type:</b> {0}",
    "ResetResultsProfilingPoint_HeaderEnabledString=<b>Enabled:</b> {0}",
    "ResetResultsProfilingPoint_HeaderProjectString=<b>Project:</b> {0}",
    "ResetResultsProfilingPoint_HeaderLocationString=<b>Location:</b> <a href='#'>{0}, line {1}</a>",
    "ResetResultsProfilingPoint_HeaderHitsString=<b>Hits:</b> {0}",
    "ResetResultsProfilingPoint_HitSuccessString=<b>{0}.</b> hit at <b>{1}</b>",
    "ResetResultsProfilingPoint_DataString=Data:"
})
public final class ResetResultsProfilingPoint extends CodeProfilingPoint.Single implements PropertyChangeListener {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class Annotation extends CodeProfilingPoint.Annotation {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        public String getAnnotationType() {
            return ResetResultsProfilingPoint.this.isEnabled() ? ANNOTATION_ENABLED : ANNOTATION_DISABLED;
        }

        @Override
        public String getShortDescription() {
            return getName();
        }

        @Override
        public CodeProfilingPoint profilingPoint() {
            return ResetResultsProfilingPoint.this;
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
            headerAreaTextBuilder.append(getHeaderHitsCount()); // NOI18N

            headerArea.setText(headerAreaTextBuilder.toString());

            StringBuilder dataAreaTextBuilder = new StringBuilder();

            synchronized(resultsSync) {
                if (results.isEmpty()) {
                    dataAreaTextBuilder.append(ProfilingPointReport.getNoDataHint(ResetResultsProfilingPoint.this));
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
            setName(ResetResultsProfilingPoint.this.getName());
            setIcon(((ImageIcon) ResetResultsProfilingPoint.this.getFactory().getIcon()).getImage());
            getAccessibleContext().setAccessibleDescription(Bundle.ResetResultsProfilingPoint_ReportAccessDescr(getName()));
        }

        private String getDataResultItem(int index) {
            synchronized(resultsSync) {
                Result result = results.get(index);

                // TODO: enable once thread name by id is available
                //String threadName = Utils.getThreadName(result.getThreadID());
                //String threadClassName = Utils.getThreadClassName(result.getThreadID());
                //String threadInformation = (threadName == null ? "&lt;unknown thread&gt;" : (threadClassName == null ? threadName : threadName + " (" + threadClassName + ")"));
                //return "<b>" + (index + 1) + ".</b> hit at <b>" + Utils.formatProfilingPointTimeHiRes(result.getTimestamp()) + "</b> by " + threadInformation;
                return Bundle.ResetResultsProfilingPoint_HitSuccessString((index + 1), Utils.formatProfilingPointTimeHiRes(result.getTimestamp()));
            }
        }

        private String getHeaderEnabled() {
            return Bundle.ResetResultsProfilingPoint_HeaderEnabledString(ResetResultsProfilingPoint.this.isEnabled());
        }

        private String getHeaderHitsCount() {
            synchronized(resultsSync) {
                return Bundle.ResetResultsProfilingPoint_HeaderHitsString(results.size());
            }
        }

        private String getHeaderLocation() {
            CodeProfilingPoint.Location location = ResetResultsProfilingPoint.this.getLocation();
            String shortFileName = new File(location.getFile()).getName();
            int lineNumber = location.getLine();

            return Bundle.ResetResultsProfilingPoint_HeaderLocationString(shortFileName, lineNumber);
        }

        private String getHeaderName() {
            return "<h2><b>" + ResetResultsProfilingPoint.this.getName() + "</b></h2>"; // NOI18N
        }

        private String getHeaderProject() {
            return Bundle.ResetResultsProfilingPoint_HeaderProjectString(
                        ProjectUtilities.getDisplayName(ResetResultsProfilingPoint.this.getProject()));
        }

        private String getHeaderType() {
            return Bundle.ResetResultsProfilingPoint_HeaderTypeString(
                        ResetResultsProfilingPoint.this.getFactory().getType());
        }

        private void initComponents() {
            setLayout(new BorderLayout());

            JPanel contentsPanel = new JPanel(new BorderLayout());
            contentsPanel.setBackground(UIUtils.getProfilerResultsBackground());
            contentsPanel.setOpaque(true);
            contentsPanel.setBorder(BorderFactory.createMatteBorder(0, 15, 15, 15, UIUtils.getProfilerResultsBackground()));

            headerArea = new HTMLTextArea() {
                    protected void showURL(URL url) {
                        Utils.openLocation(ResetResultsProfilingPoint.this.getLocation());
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
            TitledBorder tb = new TitledBorder(Bundle.ResetResultsProfilingPoint_DataString());
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

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Result(long timestamp, int threadId) {
            this.timestamp = timestamp;
            this.threadId = threadId;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public int getThreadID() {
            return threadId;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // --- Implementation --------------------------------------------------------
    private static final String ANNOTATION_ENABLED = "resetResultsProfilingPoint"; // NOI18N
    private static final String ANNOTATION_DISABLED = "resetResultsProfilingPointD"; // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Annotation annotation;
    private List<Result> results = new ArrayList<>();
    private final Object resultsSync = new Object();
    private WeakReference<Report> reportReference;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    public ResetResultsProfilingPoint(String name, Location location, Lookup.Provider project, ProfilingPointFactory factory) {
        super(name, location, project, factory);
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
                       ? Bundle.ResetResultsProfilingPoint_OneHitString(time)
                       : Bundle.ResetResultsProfilingPoint_NHitsString(size, time);
            } else {
                return Bundle.ResetResultsProfilingPoint_NoResultsString();
            }
        }
    }

    protected void updateCustomizer(ValidityAwarePanel c) {
        ResetResultsCustomizer customizer = (ResetResultsCustomizer) c;
        customizer.setPPName(getName());
        customizer.setPPLocation(getLocation());
    }

    void setValues(ValidityAwarePanel c) {
        ResetResultsCustomizer customizer = (ResetResultsCustomizer) c;
        setName(customizer.getPPName());
        setLocation(customizer.getPPLocation());
        
        Utils.checkLocation(this);
    }

    void hit(RuntimeProfilingPoint.HitEvent hitEvent, int index) {
        synchronized(resultsSync) {
            if (ProfilingPointsManager.getDefault().belowMaxHits(results.size()))
                results.add(new Result(hitEvent.getTimestamp(), hitEvent.getThreadId()));
        }
        //    ResultsManager.getDefault().reset();
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

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

package org.netbeans.modules.profiler.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import org.openide.util.NbBundle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.global.CalibrationDataFileIO;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.spi.JavaPlatformManagerProvider;
import org.netbeans.modules.profiler.spi.JavaPlatformProvider;
import org.netbeans.modules.profiler.utilities.ProfilerUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


/**
 * Provisionary action to explicitely run Profiler calibration.
 *
 * @author Ian Formanek
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "LBL_RunCalibrationAction=&Manage Calibration Data",
    "HINT_RunCalibrationAction=Manage Calibration Data",
    "HINT_CalibrateDisabled=Calibration is done Automatically for remote platforms",
    "LBL_JavaPlatform=Java Platform",
    "LBL_LastCalibrated=Last Calibrated",
    "LBL_NotCalibrated=Not calibrated",
    "LBL_JavaPlatformsForProfiling=&Java platforms available for profiling:",
    "LBL_Calibrate=&Calibrate",
    "LBL_JavaPlatforms=Java &Platforms...",
    "MSG_CalibrationOnProfile=Profiling session is currently in progress.\nDo you want to stop the current session and perform the calibration?",
    "MSG_CalibrationOnAttach=Profiling session is currently in progress\nDo you want to detach from the target application and perform the calibration?",
    "MSG_CalibrationFailed=Calibration failed.\nPlease check your setup and run the calibration again.",
    "TTP_PlatformName=Java platform name",
    "TTP_CalibrationDate=Date of last calibration"
})
@ActionID(category="Profile", id="org.netbeans.modules.profiler.actions.RunCalibrationAction")
//@ActionRegistration(displayName="#LBL_RunCalibrationAction")
//@ActionReference(path="Menu/Profile/Advanced", position=100)
public final class RunCalibrationAction extends AbstractAction {
    
    private static final HelpCtx HELP_CTX = new HelpCtx("ManageCalibration.HelpCtx"); // NOI18N
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public RunCalibrationAction() {
        putValue(Action.NAME, Bundle.LBL_RunCalibrationAction());
        putValue(Action.SHORT_DESCRIPTION, Bundle.HINT_RunCalibrationAction());
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(final ActionEvent e) {
        List<JavaPlatform> platforms = JavaPlatform.getPlatforms();
        String[] columnNames = new String[] { Bundle.LBL_JavaPlatform(),
                                              Bundle.LBL_LastCalibrated() };
        Object[][] columnData = new Object[platforms.size()][2];
        for (int i = 0; i < platforms.size(); i++)
            columnData[i] = new Object[] { platforms.get(i), null };
        final TableModel model = new DefaultTableModel(columnData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { displayUI(model); }
        });
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() { refreshTimes(model); }
        });
    }
    
    private void displayUI(final TableModel model) {
        final ProfilerTable table = new ProfilerTable(model, false, true, null);
        table.getColumnModel().getColumn(1).setCellRenderer(new CalibrationDateCellRenderer());
        table.setDefaultColumnWidth(getColumnWidth());
        table.setSortColumn(0);
        table.setPreferredScrollableViewportSize(new Dimension(400, 10));
        table.setVisibleRows(6);
        table.setColumnToolTips(new String[] { Bundle.TTP_PlatformName(),
                                               Bundle.TTP_CalibrationDate() });
        
        ProfilerTableContainer container = new ProfilerTableContainer(table, true, null);
        container.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label, Bundle.LBL_JavaPlatformsForProfiling());
        label.setLabelFor(table);
        label.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        
        final JButton calibrate = new JButton() {
            @Override
            protected void fireActionPerformed(ActionEvent e) { calibrate(table); }
        };
        Mnemonics.setLocalizedText(calibrate, Bundle.LBL_Calibrate());
        JButton platforms = new JButton() {
            @Override
            protected void fireActionPerformed(ActionEvent e) {
                JavaPlatform.showCustomizer();
                refreshModel(table);
            }
        };
        Mnemonics.setLocalizedText(platforms, Bundle.LBL_JavaPlatforms());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 10));
        buttons.add(calibrate);
        buttons.add(platforms);
        
        table.setDefaultAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) { calibrate(table); }
        });
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (table.getSelectedRow() != -1) {
                    boolean remote = isRemotePlatform((JavaPlatform)table.getValueAt(table.getSelectedRow(), table.convertColumnIndexToView(0)));
                    if (remote) {
                        calibrate.setToolTipText(Bundle.HINT_CalibrateDisabled());
                    } else {
                        calibrate.setToolTipText(""); //NOI18N
                    }
                    calibrate.setEnabled(!remote);
                }
            }
        });
        calibrate.setEnabled(false);
        table.clearSelection();
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(container, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        
        DialogDescriptor dd = new DialogDescriptor(panel,
                Bundle.HINT_RunCalibrationAction(), true,
                new Object[] { DialogDescriptor.CLOSED_OPTION },
                DialogDescriptor.CLOSED_OPTION, 0, HELP_CTX, null);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }
    
    private int getColumnWidth() {
        int width = new JLabel(Bundle.LBL_LastCalibrated() + "XXX") // NOI18N
                    .getPreferredSize().width;
        width = Math.max(width, new JLabel(Bundle.LBL_NotCalibrated() + "XXX") // NOI18N
                    .getPreferredSize().width);
        width = Math.max(width, new JLabel(DateFormat.getDateInstance()
                    .format(new Date()) + "XXX").getPreferredSize().width); // NOI18N
        width = Math.max(width, new JLabel(DateFormat.getTimeInstance()
                    .format(new Date()) + "XXX").getPreferredSize().width); // NOI18N
        return width;
    }
    
    private void refreshTimes(final TableModel model) {
        for (int i = 0; i < model.getRowCount(); i++) {
            JavaPlatform platform = (JavaPlatform)model.getValueAt(i, 0);
            boolean remote = isRemotePlatform(platform);
            String version = platform.getPlatformJDKVersion();
            Long modified = null;
            if (remote) {
                try {
                    File f = new File(CalibrationDataFileIO.getCalibrationDataFileName(version)+"."+platform.getProperties().get("platform.host")); //NOI18N
                    if (f.isFile()) modified = Long.valueOf(f.lastModified());
                } catch (Exception e) {}
            } else {
                try {
                    File f = new File(CalibrationDataFileIO.getCalibrationDataFileName(version));
                    if (f.isFile()) modified = Long.valueOf(f.lastModified());
                } catch (Exception e) {}
            }
            final int index = i;
            final Long _modified = modified;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() { model.setValueAt(_modified, index, 1); }
            });
        }
    }
    
    private void refreshModel(final ProfilerTable table) {
        Object selected = null;
        Set original = new HashSet();
        int selrow = table.getSelectedRow();
        int column = table.convertColumnIndexToView(0);
        for (int row = 0; row < table.getRowCount(); row++) {
            Object value = table.getValueAt(row, column);
            original.add(value);
            if (row == selrow) selected = value;
        }
        
        final DefaultTableModel model = (DefaultTableModel)table.getModel();
        Vector data = model.getDataVector();
        data.clear();
        
        for (JavaPlatform platform : JavaPlatform.getPlatforms()) {
            data.add(new Vector(Arrays.asList(platform, null)));
            if (!original.contains(platform)) selected = platform;
        }
        
        table.clearSelection();
        model.fireTableDataChanged();
        
        if (selected != null) table.selectValue(selected, column, true);
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() { refreshTimes(model); }
        });
    }
    
    
    private void calibrate(ProfilerTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;

        int col = table.convertColumnIndexToView(0);
        final JavaPlatform platform = (JavaPlatform)table.getValueAt(row, col);
        final DefaultTableModel model = (DefaultTableModel)table.getModel();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { calibrate(platform, model); }
        });
    }
    
    
    private static boolean isRemotePlatform(final JavaPlatform platform) {
        JavaPlatformManagerProvider impl = Lookup.getDefault().lookup(JavaPlatformManagerProvider.class);
        if (impl == null) {
            ProfilerUtils.getProfilerErrorManager().log(Level.WARNING.intValue(), "No instance of JavaPlatformManagerProvider found in Lookup");  //NOI18N
            return false;
        }
        for (JavaPlatformProvider jpp : impl.getPlatforms()) {
            if ( (platform.getPlatformId() != null) && (platform.getPlatformId().equals(jpp.getPlatformId())) && (platform.getProperties().containsKey("platform.host")) ) {//NOI18N
                return true;
                
            }
        }
        return false;
    }
    
    private void calibrate(final JavaPlatform platform, final TableModel model) {
        final int state = Profiler.getDefault().getProfilingState();
        final int mode = Profiler.getDefault().getProfilingMode();
        boolean terminate = false;
        boolean detach = false;

        if ((state == Profiler.PROFILING_PAUSED) || (state == Profiler.PROFILING_RUNNING)) {
            if (mode == Profiler.MODE_PROFILE) {
                if (!ProfilerDialogs.displayConfirmation(
                    Bundle.MSG_CalibrationOnProfile(), 
                    Bundle.CAPTION_Question())) {
                    return;
                }
                terminate = true;
            } else {
                if (!ProfilerDialogs.displayConfirmation(
                    Bundle.MSG_CalibrationOnAttach(), 
                    Bundle.CAPTION_Question())) { 
                    return;
                }
                detach = true;
            }
        }

        final boolean doDetach = detach;
        final boolean doStop = terminate;

        ProfilerUtils.getProfilerRequestProcessor().post(new Runnable() {
            public void run() {
                if (doDetach) {
                    Profiler.getDefault().detachFromApp();
                } else if (doStop) {
                    Profiler.getDefault().stopApp();
                }
                boolean calibrated = Profiler.getDefault().runCalibration(false,
                                              platform.getPlatformJavaFile(),
                                              platform.getPlatformJDKVersion(),
                                              platform.getPlatformArchitecture());
                refreshTimes(model);
                if (!calibrated) ProfilerDialogs.displayError(Bundle.MSG_CalibrationFailed());
            }
        }, 0, Thread.MAX_PRIORITY);
    }
    
    
    private static class CalibrationDateCellRenderer extends DefaultTableCellRenderer {
    
        private static Calendar CALENDAR;
        private static int REF_DAY_OF_YEAR = -1;
        private static int DAY_OF_YEAR = -1;
        private static int YEAR = -1;
        private static int ERA = -1;

        private static Date DATE;
        private static DateFormat FORMAT_TIME;
        private static DateFormat FORMAT_DATE;
        
        CalibrationDateCellRenderer() {
            setHorizontalAlignment(TRAILING);
        }

        @Override
        protected void setValue(Object value) {
            if (value == null) {
                setText(Bundle.LBL_NotCalibrated());
            } else {
                long time = ((Long)value).longValue();
                setValue(time, isToday(time));
            }
        }

        private void setValue(long time, boolean today) {
            DateFormat format;
            if (today) {
                if (FORMAT_TIME == null) FORMAT_TIME = DateFormat.getTimeInstance();
                format = FORMAT_TIME;
            } else {
                if (FORMAT_DATE == null) FORMAT_DATE = DateFormat.getDateInstance();
                format = FORMAT_DATE;
            }

            if (DATE == null) DATE = new Date();
            DATE.setTime(time);

            setText(format.format(DATE));
        }

        private static boolean isToday(long time) {
            if (REF_DAY_OF_YEAR != -1 && CALENDAR.get(Calendar.DAY_OF_YEAR)
                != REF_DAY_OF_YEAR) CALENDAR = null;

            if (CALENDAR == null) initializeCalendar();
            CALENDAR.setTimeInMillis(time);

            return DAY_OF_YEAR == CALENDAR.get(Calendar.DAY_OF_YEAR) &&
                   YEAR == CALENDAR.get(Calendar.YEAR) &&
                   ERA == CALENDAR.get(Calendar.ERA);
        }

        private static void initializeCalendar() {
            CALENDAR = Calendar.getInstance();
            DAY_OF_YEAR = CALENDAR.get(Calendar.DAY_OF_YEAR);
            YEAR = CALENDAR.get(Calendar.YEAR);
            ERA = CALENDAR.get(Calendar.ERA);
            if (REF_DAY_OF_YEAR == -1) REF_DAY_OF_YEAR = DAY_OF_YEAR;
        }

    }
    
}

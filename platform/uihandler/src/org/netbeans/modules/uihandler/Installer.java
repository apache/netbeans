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
package org.netbeans.modules.uihandler;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.lib.uihandler.LogRecords;
import org.netbeans.lib.uihandler.PasswdEncryption;
import org.netbeans.modules.exceptions.ExceptionsSettings;
import org.netbeans.modules.exceptions.ReportPanel;
import org.netbeans.modules.exceptions.ReporterResultTopComponent;
import org.netbeans.modules.uihandler.api.Activated;
import org.netbeans.modules.uihandler.api.Deactivated;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInfo;
import org.openide.modules.ModuleInstall;
import org.openide.modules.Places;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.io.NullOutputStream;
import org.openide.windows.WindowManager;
import org.xml.sax.SAXException;

/**
 * Registers and unregisters loggers.
 */
public class Installer extends ModuleInstall implements Runnable {
    static final String IDE_STARTUP = "IDE_STARTUP";
    static final long serialVersionUID = 1L;

    static final String USER_CONFIGURATION = "UI_USER_CONFIGURATION";   // NOI18N
    private static UIHandler ui = new UIHandler(false);
    private static UIHandler handler = new UIHandler(true);
    private static MetricsHandler metrics = new MetricsHandler();
    private static Logger uiLogger;
    private static Logger allLogger;
    private static Logger metricsLogger;
    static final Logger LOG = Logger.getLogger(Installer.class.getName());
    public static final RequestProcessor RP = new RequestProcessor("UI Gestures"); // NOI18N
    public static final RequestProcessor RP_UI = new RequestProcessor("UI Gestures - Create Dialog"); // NOI18N
    public static final RequestProcessor RP_SUBMIT = new RequestProcessor("UI Gestures - Submit Data", 2); // NOI18N
    public static RequestProcessor RP_OPT = null;
    private static final Preferences prefs = NbPreferences.forModule(Installer.class);
    private static OutputStream logStream;
    private static OutputStream logStreamMetrics;
    private static int logsSize;
    private static int logsSizeMetrics;
    private static long logsFirstDateMetric;
    private static URL hintURL;
    private static Object[] selectedExcParams;

    private static boolean logMetricsEnabled = false;
    /** Flag to store status of last metrics upload */
    private static boolean logMetricsUploadFailed = false;
    
    /** Log records currently displaying/uploading */
    private static final ThreadLocal<List<LogRecord>> logRecords = new ThreadLocal<>();
    
    private static final  String USAGE_STATISTICS_ENABLED          = "usageStatisticsEnabled"; // NOI18N
    private static final String USAGE_STATISTICS_SET_BY_IDE       = "usageStatisticsSetByIde"; // NOI18N
    private static final String USAGE_STATISTICS_NB_OF_IDE_STARTS = "usageStatisticsNbOfIdeStarts"; // NOI18N
    private static final String FIRST_DATE_METRICS_PROP = "firstDateMetric";    // NOI18N
    private static final String CORE_PREF_NODE = "org/netbeans/core"; // NOI18N
    private static final Preferences corePref = NbPreferences.root().node (CORE_PREF_NODE);

    private static final String CMD_METRICS_ENABLE = "MetricsEnable";   // NOI18N
    private static final String CMD_METRICS_CANCEL = "MetricsCancel";   // NOI18N
    
    private static final String MIXED_CT_BOUNDARY = "--------konec<>bloku";     // NOI18N
    // End of block pattern
    private static final String END_OF_BLOCK = "--"+MIXED_CT_BOUNDARY;          // NOI18N
    // End of data block pattern
    private static final String END_OF_DATA_BLOCK = "\n\n"+END_OF_BLOCK;        // NOI18N
    
    private final AtomicBoolean restored = new AtomicBoolean(false);

    /** Action listener for Usage Statistics Reminder dialog */
    private ActionListener l = new ActionListener () {
        @Override
        public void actionPerformed (ActionEvent ev) {
            cmd = ev.getActionCommand();
            if (CMD_METRICS_ENABLE.equals(cmd)) {
                corePref.putBoolean(USAGE_STATISTICS_ENABLED, true);
            } else if (CMD_METRICS_CANCEL.equals(cmd)) {
                corePref.putBoolean(USAGE_STATISTICS_ENABLED, false);
            }
            corePref.putBoolean(USAGE_STATISTICS_SET_BY_IDE, true);
        }
    };

    private String cmd;

    static final String METRICS_LOGGER_NAME = NbBundle.getMessage(Installer.class, "METRICS_LOGGER_NAME");
    static final String UI_LOGGER_NAME = NbBundle.getMessage(Installer.class, "UI_LOGGER_NAME");
    static final String UI_PERFORMANCE_LOGGER_NAME = NbBundle.getMessage(Installer.class, "UI_PERFORMANCE_LOGGER_NAME");
    private static Pattern ENCODING = Pattern.compile(
        "<meta.*http-equiv=['\"]Content-Type['\"]" +
        ".*content=.*charset=([A-Za-z0-9\\-]+)['\"]>", Pattern.CASE_INSENSITIVE
    ); // NOI18N

    static boolean preferencesWritable = false;
    static final String preferencesWritableKey = "uihandler.preferences.writable.check"; // NOI18N
    static {
        // #131128 - suppress repetitive exceptions when config/Preferences/org/netbeans/modules/uihandler.properties
        // is not writable for some reason
        long checkTime = System.currentTimeMillis();
        try {
            prefs.putLong(preferencesWritableKey, checkTime);
        } catch (IllegalArgumentException iae) {
            // #164580 - ignore IllegalArgumentException: Malformed \\uxxxx encoding.
            // prefs are now empty, so put again and rewrite broken content.
            prefs.putLong(preferencesWritableKey, checkTime);
        }
        try {
            prefs.flush();
            prefs.sync();
            if(checkTime == prefs.getLong(preferencesWritableKey, 0)) {  //NOI18N
                preferencesWritable = true;
            }
        } catch (BackingStoreException e) {
            // immediatelly show dialog with exception (usually Access is denied)
            Exceptions.printStackTrace(e);
        }
    }

    /**
     * Used to synchronize access to ui log files to avoid writing to/deleting/renaming file
     * which is being parsed in another thread.
     */
    private static final Object UIGESTURE_LOG_LOCK = new Object();

    /**
     * Used to synchronize access to metrics log files to avoid writing to/deleting/renaming file
     * which is being parsed in another thread.
     */
    private static final Object METRICS_LOG_LOCK = new Object();

    static enum DataType {
        DATA_UIGESTURE,
        DATA_METRICS
    };

    @Override
    public void restored() {
        EarlyHandler eh = Lookup.getDefault().lookup(EarlyHandler.class);
        restored(eh.earlyRecords);
    }
    
    void restored(java.util.Queue<LogRecord> earlyRecords) {
        synchronized (restored) {
            if (!restored.getAndSet(true)) {
                restoredOnce(earlyRecords);
            } else {
                logEarlyRecords(earlyRecords);
            }
        }
    }
    
    private void restoredOnce(java.util.Queue<LogRecord> earlyRecords) {
        TimeToFailure.logAction();
        Logger log = Logger.getLogger(UI_LOGGER_NAME);
        log.setUseParentHandlers(false);
        log.setLevel(Level.FINEST);
        log.addHandler(ui);
        uiLogger = log; // To prevent from GC
        Logger all = Logger.getLogger("");
        all.addHandler(handler);
        allLogger = all;    // To prevent from GC
        logsSize = prefs.getInt("count", 0);
        logsSizeMetrics = prefs.getInt("countMetrics", 0);
        logsFirstDateMetric = prefs.getLong(FIRST_DATE_METRICS_PROP, -1);
        logMetricsUploadFailed = prefs.getBoolean("metrics.upload.failed", false); // NOI18N
        corePref.addPreferenceChangeListener(new PrefChangeListener());

        if (!Boolean.getBoolean("netbeans.full.hack") && !Boolean.getBoolean("netbeans.close")) {
            usageStatisticsReminder();
        }
        
        System.setProperty("nb.show.statistics.ui",USAGE_STATISTICS_ENABLED);
        logMetricsEnabled = corePref.getBoolean(USAGE_STATISTICS_ENABLED, false);
        if (logMetricsEnabled) {
            //Handler for metrics
            log = Logger.getLogger(METRICS_LOGGER_NAME);
            log.setUseParentHandlers(true);
            log.setLevel(Level.FINEST);
            log.addHandler(metrics);
            metricsLogger = log;    // To prevent from GC
            try {
                LogRecord userData = getUserData(log);
                LogRecords.write(logStreamMetrics(), userData);
                List<LogRecord> enabledRec = new ArrayList<>();
                List<LogRecord> disabledRec = new ArrayList<>();
                getModuleList(log, enabledRec, disabledRec);
                for (LogRecord rec : enabledRec) {
                    LogRecords.write(logStreamMetrics(), rec);
                }
                for (LogRecord rec : disabledRec) {
                    LogRecords.write(logStreamMetrics(), rec);
                }
                LogRecord clusterRec = EnabledModulesCollector.getClusterList(log);
                LogRecords.write(logStreamMetrics(), clusterRec);
                LogRecord userInstalledRec = EnabledModulesCollector.getUserInstalledModules(log);
                LogRecords.write(logStreamMetrics(), userInstalledRec);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        EarlyHandler.disable();
        CPUInfo.logCPUInfo();
        ScreenSize.logScreenSize();
        logIdeStartup();
        logEarlyRecords(earlyRecords);
        for (Activated a : Lookup.getDefault().lookupAll(Activated.class)) {
            a.activated(log);
        }

        if (logsSize >= UIHandler.MAX_LOGS) {
            WindowManager.getDefault().invokeWhenUIReady(this);
        }
    }
    
    private void logEarlyRecords(java.util.Queue<LogRecord> earlyRecords) {
        if (earlyRecords != null) {
            List<LogRecord> allRecords;
            synchronized (earlyRecords) {
                allRecords = new ArrayList<>(earlyRecords);
                earlyRecords.clear();
            }
            List<LogRecord> uiRecords = extractRecords(allRecords, UI_LOGGER_NAME);
            ui.publishEarlyRecords(uiRecords);
            handler.publishEarlyRecords(allRecords);
            if (logMetricsEnabled) {
                List<LogRecord> metricsRecords = extractRecords(allRecords, METRICS_LOGGER_NAME);
                metrics.publishEarlyRecords(metricsRecords);
            }
        }
    }
    
    private List<LogRecord> extractRecords(List<LogRecord> records, String logger) {
        List<LogRecord> records2 = new ArrayList<>();
        for (LogRecord r : records) {
            if (r.getLoggerName().startsWith(logger)) {
                records2.add(r);
            }
        }
        return records2;
    }
    
    /** Accessed from tests. */
    static int getLogsSizeTest() {
        return logsSize;
    }
    
    private void logIdeStartup() {
        Logger.getLogger(UI_LOGGER_NAME).log(new LogRecord(Level.CONFIG, IDE_STARTUP));
    }

    private void usageStatisticsReminder () {
        //Increment number of IDE starts, stop at 4 because we are interested at second start
        long nbOfIdeStarts = corePref.getLong(USAGE_STATISTICS_NB_OF_IDE_STARTS, 0);
        nbOfIdeStarts++;
        if (nbOfIdeStarts < 4) {
            corePref.putLong(USAGE_STATISTICS_NB_OF_IDE_STARTS, nbOfIdeStarts);
        }
        boolean setByIde = corePref.getBoolean(USAGE_STATISTICS_SET_BY_IDE, false);
        boolean usageEnabled = corePref.getBoolean(USAGE_STATISTICS_ENABLED, false);

        //If "usageStatisticsEnabled" was set by IDE do not ask again.
        if (setByIde) {
            return;
        }
        //If "usageStatisticsEnabled" was not set by IDE, it is false and it is second start ask again
        if (!setByIde && !usageEnabled && (nbOfIdeStarts == 2)) {
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {
                    showDialog();
                }
            });
        }
    }
    
    private void showDialog () {
        final JPanel panel = new ReminderPanel();
        JButton metricsEnable = new JButton();
        metricsEnable.addActionListener(l);
        metricsEnable.setActionCommand(CMD_METRICS_ENABLE);
        //registerNow.setText(NbBundle.getMessage(RegisterAction.class,"LBL_RegisterNow"));
        Mnemonics.setLocalizedText(metricsEnable, NbBundle.getMessage(
                Installer.class, "LBL_MetricsEnable"));
        metricsEnable.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(Installer.class,"ACSN_MetricsEnable"));
        metricsEnable.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(Installer.class,"ACSD_MetricsEnable"));

        JButton metricsCancel = new JButton();
        metricsCancel.addActionListener(l);
        metricsCancel.setActionCommand(CMD_METRICS_CANCEL);
        //registerLater.setText(NbBundle.getMessage(RegisterAction.class,"LBL_RegisterLater"));
        Mnemonics.setLocalizedText(metricsCancel, NbBundle.getMessage(
                Installer.class, "LBL_MetricsCancel"));
        metricsCancel.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(Installer.class,"ACSN_MetricsCancel"));
        metricsCancel.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(Installer.class,"ACSD_MetricsCancel"));

        DialogDescriptor descriptor = new DialogDescriptor(
            panel,
            NbBundle.getMessage(Installer.class, "Metrics_title"),
            true,
                new Object[] {metricsEnable, metricsCancel},
                null,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            final Dialog d = dlg;
            dlg.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    BufferedImage offImg;
                    offImg = (BufferedImage) panel.createImage(1000,1000);
                    Graphics g = offImg.createGraphics();
                    panel.paint(g);
                    int height = d.getPreferredSize().height;
                    Dimension size = d.getSize();
                    size.height = height;
                    d.setSize(size);
                }

            });
            dlg.setResizable(false);
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
        //NbConnection.updateStatus(cmd,NbInstaller.PRODUCT_ID);
    }

    @Override
    public void run() {
        if (RP.isRequestProcessorThread()) {
            displaySummary("INIT_URL", false, false, false); // NOI18N
        } else {
            RP.post(this);
        }
    }

    @Override
    public void uninstalled() {
        doClose();
    }

    @Override
    public final void close() {
        UIHandler.flushImmediatelly();
        closeLogStream();
        MetricsHandler.flushImmediatelly();
        closeLogStreamMetrics();
    }

    public final void doClose() {
        Logger log = Logger.getLogger(UI_LOGGER_NAME);
        log.removeHandler(ui);
        uiLogger = null;
        Logger all = Logger.getLogger(""); // NOI18N
        all.removeHandler(handler);
        allLogger = null;
        log = Logger.getLogger(METRICS_LOGGER_NAME);
        log.removeHandler(metrics);
        metricsLogger = null;

        closeLogStream();
        closeLogStreamMetrics();
        synchronized (restored) {
            restored.set(false);
        }
    }
    
    static boolean isImmediateWriteOut(LogRecord r) {
        List<LogRecord> preferredLog = logRecords.get();
        if (preferredLog != null) {
            preferredLog.add(r);
            return true;
        } else {
            return false;
        }
    }

    static void writeOut(LogRecord r) {
        try {
            boolean logOverflow;
            boolean logSizeControl;
            List<LogRecord> logs = null;
            synchronized (UIGESTURE_LOG_LOCK) {
                LogRecords.write(logStream(), r);
                logsSize++;
                logOverflow = logsSize > UIHandler.MAX_LOGS;
                if (preferencesWritable) {
                    if (logOverflow) {
                        prefs.putInt("count", UIHandler.MAX_LOGS);
                    } else if (prefs.getInt("count", 0) < logsSize) {
                        prefs.putInt("count", logsSize);
                    }
                }
                if (logOverflow) {
                    closeLogStream();
                    if (isHintsMode()) {
                        logs = new ArrayList<>(getLogs());
                    }
                }
                logSizeControl = (logsSize % 100) == 0 && !logOverflow;
            }
            if (logOverflow) {
                if (isHintsMode()) {
                    final List<LogRecord> recs = logs;
                    class Auto implements Runnable {
                        @Override
                        public void run() {
                            displaySummary("WELCOME_URL", true, true,true, DataType.DATA_UIGESTURE, recs, null);
                        }
                    }
                    RP.post(new Auto()).waitFinished();
                }
                synchronized (UIGESTURE_LOG_LOCK) {
                    File f = logFile(0);
                    File f1 = logFile(1);
                    if (f1.exists()) {
                        f1.delete();
                    }
                    f.renameTo(f1);
                    logsSize = 0;
                }
            }
            if (logSizeControl) {
                synchronized (UIGESTURE_LOG_LOCK) {
                    //This is fallback to avoid growing log file over any limit.
                    File f = logFile(0);
                    File f1 = logFile(1);
                    if (f.exists() && (f.length() > UIHandler.MAX_LOGS_SIZE)) {
                        LOG.log(Level.INFO, "UIGesture Collector log file size is over limit. It will be deleted."); // NOI18N
                        LOG.log(Level.INFO, "Log file:{0} Size:{1} Bytes", new Object[]{f, f.length()}); // NOI18N
                        closeLogStream();
                        logsSize = 0;
                        if (preferencesWritable) {
                            prefs.putInt("count", logsSize);
                        }
                        f.delete();
                    }
                    if (f1.exists() && (f1.length() > UIHandler.MAX_LOGS_SIZE)) {
                        LOG.log(Level.INFO, "UIGesture Collector backup log file size is over limit. It will be deleted."); // NOI18N
                        LOG.log(Level.INFO, "Log file:{0} Size:{1} Bytes", new Object[]{f1, f1.length()}); // NOI18N
                        f1.delete();
                    }
                }
            }
        } catch (IOException ex) {
            // bug #183331 don't log throwable here since it causes recursive writeOut invocation
            LOG.log(Level.INFO, "UIGesture Collector logging has failed: {0}", ex.getMessage()); // NOI18N
        }
    }

    private static LogRecord getUserData (Logger logger) {
        LogRecord userData;
        ArrayList<String> params = new ArrayList<>();
        params.add(Submit.getOS());
        params.add(Submit.getVM());
        params.add(Submit.getVersion());
        List<String> buildInfo = BuildInfo.logBuildInfo();
        if (buildInfo != null) {
            params.addAll(buildInfo);
        }
        userData = new LogRecord(Level.INFO, "USG_SYSTEM_CONFIG");
        userData.setParameters(params.toArray());
        userData.setLoggerName(logger.getName());
        return userData;
    }
    
    private static boolean uploadMetricsTest() {
        if (logsSizeMetrics >= MetricsHandler.MAX_LOGS) {
            return true;
        }
        int daysSinceFirstMetric = (int) ((System.currentTimeMillis() - logsFirstDateMetric)/(1000*60*60*24));
        if (daysSinceFirstMetric > MetricsHandler.MAX_DAYS) {
            return true;
        }
        return false;
    }

    static void writeOutMetrics (LogRecord r) {
        try {
            boolean upload;
            synchronized (METRICS_LOG_LOCK) {
                LogRecords.write(logStreamMetrics(), r);
                logsSizeMetrics++;
                boolean firstDateMetric = logsFirstDateMetric < 0;
                if (firstDateMetric) {
                    logsFirstDateMetric = System.currentTimeMillis();
                }
                if (preferencesWritable) {
                    prefs.putInt("countMetrics", logsSizeMetrics);
                    if (firstDateMetric) {
                        prefs.putLong(FIRST_DATE_METRICS_PROP, logsFirstDateMetric);
                    }
                }
                upload = uploadMetricsTest();
                if (upload) {
                    MetricsHandler.waitFlushed();
                    closeLogStreamMetrics();
                    File f = logFileMetrics(0);
                    File f1 = logFileMetrics(1);
                    if (f1.exists()) {
                        if (logMetricsUploadFailed) {
                            //If last metrics upload failed first check size of backup file
                            if (f1.length() > MetricsHandler.MAX_LOGS_SIZE) {
                                //Size is over limit delete file
                                f1.delete();
                                if (!f.renameTo(f1)) {
                                    LOG.log(Level.INFO, "Failed to rename file:{0} to:{1}", new Object[]{f, f1}); // NOI18N
                                }
                            } else {
                                //Size is below limit, append data
                                appendFile(f, f1);
                            }
                        } else {
                            f1.delete();
                            if (!f.renameTo(f1)) {
                                LOG.log(Level.INFO, "Failed to rename file:{0} to:{1}", new Object[]{f, f1}); // NOI18N
                            }
                        }
                    } else {
                        if (!f.renameTo(f1)) {
                            LOG.log(Level.INFO, "Failed to rename file:{0} to:{1}", new Object[]{f, f1}); // NOI18N
                        }
                    }
                    logsSizeMetrics = 0;
                    logsFirstDateMetric = System.currentTimeMillis();
                    if (preferencesWritable) {
                        prefs.putInt("countMetrics", logsSizeMetrics);
                        prefs.putLong(FIRST_DATE_METRICS_PROP, logsFirstDateMetric);
                    }
                }
            }
            if (upload) {
                //Task to upload metrics data
                final List<LogRecord> recs = null;
                class Auto implements Runnable {
                    @Override
                    public void run() {
                        displaySummary("METRICS_URL", true, true, true, DataType.DATA_METRICS, recs, null);
                    }
                }
                //Must be performed out of lock because it calls getLogsMetrics
                RP.post(new Auto()).waitFinished();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** Append content of source to target */
    private static void appendFile (File source, File target) {
        byte[] buf = new byte[8192];
        FileInputStream is = null;
        FileOutputStream os = null;
        long targetSize = -1;
        try {
            is = new FileInputStream(source);
            targetSize = target.length();
            os = new FileOutputStream(target, true);

            int l;
            while ((l = is.read(buf)) != -1) {
                os.write(buf, 0, l);
            }
            os.flush();
        } catch (IOException ex) {
            if (os != null) {
                // Write failed, to assure consistency of data, truncate the file back to the original size:
                DataConsistentFileOutputStream.truncateFileToConsistentSize(os, targetSize);
            }
            Exceptions.printStackTrace(ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {}
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {}
            }
        }
    }

    static void getModuleList (Logger logger, List<LogRecord> enabledRec, List<LogRecord> disabledRec) {
        List<ModuleInfo> enabled = new ArrayList<>();
        List<ModuleInfo> disabled = new ArrayList<>();
        for (ModuleInfo m : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (m.isEnabled()) {
                enabled.add(m);
            } else {
                disabled.add(m);
            }
        }
        if (!enabled.isEmpty()) {
            LogRecord rec = new LogRecord(Level.INFO, "USG_ENABLED_MODULES");
            String[] enabledNames = EnabledModulesCollector.getModuleNames(enabled);
            rec.setParameters(enabledNames);
            rec.setLoggerName(logger.getName());
            enabledRec.add(rec);
        }
        if (!disabled.isEmpty()) {
            LogRecord rec = new LogRecord(Level.INFO, "USG_DISABLED_MODULES");
            String[] disabledNames = EnabledModulesCollector.getModuleNames(disabled);
            rec.setParameters(disabledNames);
            rec.setLoggerName(logger.getName());
            disabledRec.add(rec);
        }
    }

    public static URL hintsURL() {
        return hintURL;
    }
    public static boolean isHintsMode() {
        return prefs.getBoolean("autoSubmitWhenFull", false);
    }

    static int timesSubmitted() {
        return prefs.getInt("submitted", 0);
    }

    public static int getLogsSize() {
        UIHandler.waitFlushed();
        synchronized (UIGESTURE_LOG_LOCK) {
            return prefs.getInt("count", 0); // NOI18N
        }
    }

    static void readLogs(Handler handler){
        UIHandler.waitFlushed();
        synchronized (UIGESTURE_LOG_LOCK) {

            File f = logFile(0);
            if (f == null || !f.exists()) {
                return ;
            }
            closeLogStream();

            File f1 = logFile(1);
            if (logsSize < UIHandler.MAX_LOGS && f1 != null && f1.exists()) {
                scan(f1, handler);
            }
            scan(f, handler);
        }
    }
    
    private static String reportFileContent(File f) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
            StringWriter sw = new StringWriter();
            String line;
            while ((line = br.readLine()) != null) {
                sw.write(line);
                sw.write('\n');
            }
            sw.close();
            return sw.toString();
        } catch (IOException ioex) {
            return ioex.toString();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {}
            }
        }
    }

    private static boolean fileContentReported;
    
    private static void scan(File f, Handler handler){
        try {
            LogRecords.scan(f, handler);
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Broken uilogs file, not all UI actions will be submitted", ex);
            if (!fileContentReported) {
                try {
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.log(Level.INFO, "Problematic file content = {0}", reportFileContent(f));
                    }
                } finally {
                    fileContentReported = true;
                }
            }
        }
    }
    
    static List<LogRecord> getLogs() {
        class H extends Handler {
            List<LogRecord> logs = new LinkedList<>();

            @Override
            public void publish(LogRecord r) {
                logs.add(r);
                if (logs.size() > UIHandler.MAX_LOGS) {
                    logs.remove(0);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        }
        H hndlr = new H();
        readLogs(hndlr);
        return hndlr.logs;
    }

    public static List<LogRecord> getLogsMetrics() {
        synchronized (METRICS_LOG_LOCK) {
        
            class H extends Handler {
                List<LogRecord> logs = new LinkedList<>();

                @Override
                public void publish(LogRecord r) {
                    logs.add(r);
                }

                @Override
                public void flush() {
                }

                @Override
                public void close() throws SecurityException {
                }
            }
            H hndlr = new H();

            File f1 = logFileMetrics(1);
            if ((f1 != null) && f1.exists()) {
                try {
                    LogRecords.scan(f1, hndlr);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(
                        Exceptions.attachMessage(ex, "Broken metrics log file, not all metrics data will be submitted")
                    );
                    if (!fileContentReported) {
                        try {
                            if (LOG.isLoggable(Level.INFO)) {
                                LOG.log(Level.INFO, "Problematic file content = {0}", reportFileContent(f1));
                            }
                        } finally {
                            fileContentReported = true;
                        }
                    }
                }
            }
            
            return hndlr.logs;
        }
    }

    static File getDeadlockDumpFile(){
        return new File(logsDirectory(), "deadlock.dump");
    }

    static File logsDirectory(){
        
        File userDir = Places.getUserDirectory();
        if (userDir != null) {
            return new File(new File(userDir, "var"), "log");                   // NOI18N
        } else {
            return null;
        }
    }

    private static File logFile(int revision) {
        File logDir = logsDirectory();
        if (logDir == null){
            return null;
        }
        String suffix = revision == 0 ? "" : "." + revision;

        File logFile = new File(logDir, "uigestures" + suffix);
        return logFile;
    }

    private static File logFileMetrics (int revision) {
        File logDir = logsDirectory();
        if (logDir == null){
            return null;
        }
        String suffix = revision == 0 ? "" : "." + revision;
        File logFile = new File(logDir, "metrics" + suffix);                    // NOI18N
        return logFile;
    }

    private static OutputStream logStream() throws FileNotFoundException {
        synchronized (Installer.class) {
            if (logStream != null) {
                return logStream;
            }
        }

        OutputStream os;
        File logFile = logFile(0);
        if (logFile != null) {
            logFile.getParentFile().mkdirs();
            os = new DataConsistentFileOutputStream(logFile, true);
        } else {
            os = new NullOutputStream();
        }

        synchronized (Installer.class) {
            if (logStream != null) {
                try {
                    os.close();
                } catch (IOException ex) {}
                return logStream;
            }
            logStream = os;
            return logStream;
        }
    }

    private static OutputStream logStreamMetrics () throws FileNotFoundException {
        synchronized (METRICS_LOG_LOCK) {
            if (logStreamMetrics != null) {
                return logStreamMetrics;
            }
        }

        OutputStream os;
        File logFile = logFileMetrics(0);
        if (logFile != null) {
            logFile.getParentFile().mkdirs();
            os = new DataConsistentFileOutputStream(logFile, true);
        } else {
            os = new NullOutputStream();
        }

        synchronized (METRICS_LOG_LOCK) {
            if (logStreamMetrics != null) {
                try {
                    os.close();
                } catch (IOException ex) {}
                return logStreamMetrics;
            }
            logStreamMetrics = os;
            return logStreamMetrics;
        }
    }

    private static void closeLogStream() {
        OutputStream os;
        synchronized (Installer.class) {
            os = logStream;
            logStream = null;
        }
        if (os == null) {
            return;
        }

        try {
            os.close();
        } catch (IOException ex) {}
    }

    private static void closeLogStreamMetrics() {
        OutputStream os;
        synchronized (METRICS_LOG_LOCK) {
            os = logStreamMetrics;
            logStreamMetrics = null;
        }
        if (os == null) {
            return;
        }

        try {
            os.close();
        } catch (IOException ex) {}
    }

    static void clearLogs() {
        synchronized (UIGESTURE_LOG_LOCK) {
            closeLogStream();

            for (int i = 0; ; i++) {
                File f = logFile(i);
                if (f == null || !f.exists()) {
                    break;
                }
                f.delete();
            }

            logsSize = 0;
            if (preferencesWritable) {
                prefs.putInt("count", 0);
            }
        }
        UIHandler.SUPPORT.firePropertyChange(null, null, null);
    }

    @Override
    public boolean closing() {
        return true;
    }

    static void logDeactivated(){
        Logger log = Logger.getLogger(UI_LOGGER_NAME);
        for (Deactivated a : Lookup.getDefault().lookupAll(Deactivated.class)) {
            a.deactivated(log);
        }
    }
    
    private static AtomicReference<String> DISPLAYING = new AtomicReference<>();

    static boolean displaySummary(String msg, boolean explicit, boolean auto,
                                  boolean connectDialog, DataType dataType,
                                  List<LogRecord> recs, SlownessData slownData) {
        return displaySummary(msg, explicit, auto, connectDialog, dataType, recs, slownData, false);
    }
    
    static boolean displaySummary(String msg, boolean explicit, boolean auto,
                                  boolean connectDialog, DataType dataType,
                                  List<LogRecord> recs, SlownessData slownData,
                                  boolean isAfterRestart) {
        if (!DISPLAYING.compareAndSet(null, msg)) {
            return true;
        }

        boolean v = true;
        try {
            if (!explicit) {
                boolean dontAsk = prefs.getBoolean("ask.never.again." + msg, false); // NOI18N
                if (dontAsk) {
                    LOG.log(Level.INFO, "UI Gesture Collector's ask.never.again.{0} is true, exiting", msg); // NOI18N
                    return true;
                }
            }

            Submit submit = auto ? new SubmitAutomatic(msg, Button.SUBMIT, dataType, recs) : new SubmitInteractive(msg, connectDialog, dataType, recs, slownData, isAfterRestart);
            submit.doShow(dataType);
            v = submit.okToExit;
        } finally {
            DISPLAYING.set(null);
        }
        return v;
    }

    public static boolean displaySummary(String msg, boolean explicit, boolean auto, boolean connectDialog) {
        return displaySummary(msg, explicit, auto, connectDialog, DataType.DATA_UIGESTURE, null, null);
    }

    static boolean displaySummary(String msg, boolean explicit, boolean auto, boolean connectDialog, SlownessData slownessData) {
        return displaySummary(msg, explicit, auto, connectDialog, DataType.DATA_UIGESTURE, null, slownessData);
    }

    /** used only in tests - low performance - should use read logs*/
    static Throwable getThrown() {
        return getThrown(getLogs());
    }

    private static Throwable getThrown(List<LogRecord> recs) {
        LogRecord log = getThrownLog(recs);
        if (log == null){
            return null;
        }else{
            return log.getThrown();
        }
    }

    private static LogRecord getThrownLog(List<LogRecord> list) {
        String firstLine = null;
        String message = null;
        if (selectedExcParams != null){
            if (selectedExcParams[0] instanceof String){
                message = (String)selectedExcParams[0];
            }
            if (selectedExcParams[1] instanceof String){
                firstLine = (String)selectedExcParams[1];
            }
        }
        ListIterator<LogRecord> it = list.listIterator(list.size());
        Throwable thr = null;
        LogRecord result;
        while (it.hasPrevious()){
            result = it.previous();
            if (result != null &&
                result.getLevel().intValue() >= Level.WARNING.intValue()) {
                
                thr = result.getThrown();// ignore info messages
                if ((thr != null) && (message != null)) {
                    if (!thr.getMessage().equals(message)){
                        thr = null;//different messages
                    }
                }
                if ((thr != null) && (firstLine != null)) {
                    StackTraceElement[] elems = thr.getStackTrace();
                    if (!(elems == null) && !(elems.length == 0)){
                        StackTraceElement elem = elems[0];
                        String thrLine = elem.getClassName() + "." + elem.getMethodName();
                        if (! thrLine.equals(firstLine)){
                            thr = null;//different first lines
                        }
                    }
                }
            }
            // find first exception from end
            if (thr != null) {
                return result;
            }
        }
        return null;// no throwable found
    }

    protected static void setSelectedExcParams(Object[] params){
        selectedExcParams = params;
    }

    /** Tries to parse a list of buttons provided by given page.
     * @param is the input stream to read the page from
     * @param defaultButton the button to add always to the list
     */
    static void parseButtons(InputStream is, final Object defaultButton, final DialogDescriptor dd)
            throws IOException, ParserConfigurationException, SAXException, InterruptedException, InvocationTargetException {
        final ButtonsHTMLParser bp = new ButtonsHTMLParser(is);
        final IOException[] ioExPtr = new IOException[] { null };
        Runnable buttonsCreation = new Runnable() {
            @Override
            public void run() {
                try {
                    bp.parse();
                } catch (IOException ioex) {
                    ioExPtr[0] = ioex;
                    return ;
                }
                bp.createButtons();
                List<Object> options = bp.getOptions();
                if (!bp.containsExitButton() && (defaultButton != null)){
                    options.add(defaultButton);
                }
                dd.setOptions(options.toArray());
                dd.setAdditionalOptions(bp.getAdditionalOptions().toArray());
                if (bp.getTitle() != null){
                    dd.setTitle(bp.getTitle());
                }
            }
        };
        
        if (EventQueue.isDispatchThread()){
            buttonsCreation.run();
        }else{
            EventQueue.invokeAndWait(buttonsCreation);
        }
        if (ioExPtr[0] != null) {
            throw ioExPtr[0];
        }
    }
    
    static String decodeButtons(Object res, URL[] url) {
        return decodeButtons(res, url, DataType.DATA_UIGESTURE);
    }

    private static String decodeButtons(Object res, URL[] url, DataType dataType) {
        if (res instanceof JButton) {
            JButton b = (JButton)res;
            Object post = b.getClientProperty("url"); // NOI18N
            if (post instanceof String) {
                String replace = null;
                if (dataType == DataType.DATA_UIGESTURE) {
                    replace = System.getProperty("org.netbeans.modules.uihandler.Submit"); // NOI18N
                } else if (dataType == DataType.DATA_METRICS) {
                    replace = System.getProperty("org.netbeans.modules.uihandler.Metrics"); // NOI18N
                }
                if (replace != null) {
                    post = replace;
                }
                try {
                    url[0] = new URL((String) post);
                } catch (MalformedURLException ex) {
                    LOG.log(Level.INFO, "Cannot decode URL: " + post, ex); // NOI18N
                    url[0] = null;
                    return null;
                }
            }
            return b.getActionCommand();
        }
        return res instanceof String ? (String)res : null;
    }

    private static URL uploadLogs(URL postURL, String id, Map<String,String> attrs,
            List<LogRecord> recs, DataType dataType, boolean isErrorReport,
            SlownessData slownData, boolean isOOM, boolean isAfterRestart) throws IOException {
        ProgressHandle h = null;
        //Do not show progress UI for metrics upload
        if (dataType != DataType.DATA_METRICS) {
            h = ProgressHandleFactory.createHandle(NbBundle.getMessage(Installer.class, "MSG_UploadProgressHandle"));
        }
        try {
            return uLogs(h, postURL, id, attrs, recs, dataType, isErrorReport, slownData, isOOM, isAfterRestart);
        } finally {
            if (h != null) {
                h.finish();
            }
        }
    }
    
    static URL uploadLogs(URL postURL, String id, Map<String,String> attrs, List<LogRecord> recs, boolean isErrorReport) throws IOException {
        return uploadLogs(postURL, id, attrs, recs, DataType.DATA_UIGESTURE, isErrorReport, null, false, false);
    }

    private static URL uLogs
    (ProgressHandle h, URL postURL, String id, Map<String,String> attrs, List<LogRecord> recs,
            DataType dataType, boolean isErrorReport, SlownessData slownData, boolean isOOM,
            boolean isAfterRestart) throws IOException {
        if (dataType != DataType.DATA_METRICS) {
            int workUnits = isOOM ? 1100 : 100;
            h.start(workUnits + recs.size());
            h.progress(NbBundle.getMessage(Installer.class, "MSG_UploadConnecting")); // NOI18N
        }
        
        boolean fileProtocol = "file".equals(postURL.getProtocol()); // For tests or other non-http usages
        
        LOG.log(Level.FINE, "uploadLogs, postURL = {0}", postURL); // NOI18N
        URLConnection conn;
        if (fileProtocol) {
            conn = null;
        } else {
            conn = postURL.openConnection();
        }

        if (dataType != DataType.DATA_METRICS) {
            h.progress(10);
        }
        
        if (!fileProtocol) {
            assert conn != null;
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).setChunkedStreamingMode(0);
            }
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+MIXED_CT_BOUNDARY);
            conn.setRequestProperty("Pragma", "no-cache");
            conn.setRequestProperty("Cache-control", "no-cache");
            conn.setRequestProperty("User-Agent", "NetBeans");
        }
        
        if (dataType != DataType.DATA_METRICS) {
            h.progress(NbBundle.getMessage(Installer.class, "MSG_UploadSending"), 20);
        }
        LOG.log(Level.FINE, "uploadLogs, header sent"); // NOI18N

        final PrintStream os;
        final String eol;
        if (fileProtocol) {
            os = new PrintStream(new FileOutputStream(postURL.getFile()));
            eol = System.getProperty("line.separator");         // NOI18N
        } else {
            assert conn != null;
            os = new PrintStream(conn.getOutputStream());
            eol = "\r\n";   // Content transfer line ending     // NOI18N
        }
        /*
        os.println("POST " + postURL.getPath() + " HTTP/1.1");
        os.println("Pragma: no-cache");
        os.println("Cache-control: no-cache");
        os.println("Content-Type: multipart/form-data; boundary=--------konec<>bloku");
        os.println();
         */
        for (Map.Entry<String, String> en : attrs.entrySet()) {
            os.print(END_OF_BLOCK+eol);
            os.print("Content-Disposition: form-data; name=\"" + en.getKey() + "\""+eol);
            os.print(eol);
            os.print(en.getValue().getBytes());
            os.print(eol);
        }
        LOG.log(Level.FINE, "uploadLogs, attributes sent"); // NOI18N
        
        if (dataType != DataType.DATA_METRICS) {
            h.progress(30);
        }

        os.print(END_OF_BLOCK+eol);

        if (id == null) {
            id = "uigestures"; // NOI18N
        }

        if (dataType != DataType.DATA_METRICS && isErrorReport) {
            os.print("Content-Disposition: form-data; name=\"messages\"; filename=\"" + id + "_messages.gz\""+eol);
            os.print("Content-Type: x-application/log"+eol+eol);
            boolean fromLastRun = recs.size() > 0 && isAfterRestart;
            uploadMessagesLog(os, fromLastRun);
            os.print(END_OF_DATA_BLOCK+eol);
        }

        if (slownData != null){
            assert slownData.getNpsContent() != null: "nps param should be not null";
            assert slownData.getNpsContent().length > 0 : "nps param should not be empty";
            os.print("Content-Disposition: form-data; name=\"slowness\"; filename=\"" + id + "_slowness.gz\""+eol);
            os.print("Content-Type: x-application/nps"+eol+eol);
            os.write(slownData.getNpsContent());
            os.print(END_OF_DATA_BLOCK+eol);
        }

        if (dataType != DataType.DATA_METRICS) {
            h.progress(70);
        }

        if (isOOM){
            File f = getHeapDump(recs, isAfterRestart);
            assert (f != null);
            assert (f.exists() && f.canRead());
            assert f.length() != 0 : "Heapdump has zero size!";
            long progressUnit = f.length() / 1000;
            if (progressUnit == 0) progressUnit = 1; //prevent #196630
            long alreadyWritten = 0;
            os.print("Content-Disposition: form-data; name=\"heapdump\"; filename=\"" + id + "_heapdump.gz\""+eol);
            os.print("Content-Type: x-application/heap"+eol+eol);
            GZIPOutputStream gzip = new GZIPOutputStream(os);
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {
                byte[] heapDumpData = new byte[8192];
                int read;
                int workunit;
                while ((read = bis.read(heapDumpData)) != -1){
                    gzip.write(heapDumpData, 0, read);
                    alreadyWritten += read;
                    workunit = (int)(alreadyWritten / progressUnit);
                    if(workunit < 1000) {
                        h.progress(70 + workunit);
                    }
                }
            }
            gzip.finish();
            os.print(END_OF_DATA_BLOCK+eol);

            h.progress(1070);
        }
        File deadlockFile = getDeadlockFile(recs);
        if (deadlockFile != null) {
            os.print("Content-Disposition: form-data; name=\"deadlock\"; filename=\"" + id + "_deadlock.gz\""+eol);
            os.print("Content-Type: x-application/log"+eol+eol);
            uploadGZFile(os, deadlockFile);
            os.print(END_OF_DATA_BLOCK+eol);
        }

        os.print("Content-Disposition: form-data; name=\"logs\"; filename=\"" + id + "\""+eol);
        os.print("Content-Type: x-application/gzip"+eol+eol);
        GZIPOutputStream gzip = new GZIPOutputStream(os);
        DataOutputStream data = new DataOutputStream(gzip);
        data.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("utf-8")); // NOI18N
        data.write("<uigestures version='1.0'>\n".getBytes("utf-8")); // NOI18N

        int cnt = isOOM ? 1080 : 80;
        LOG.log(Level.FINE, "uploadLogs, sending records"); // NOI18N
        for (LogRecord r : recs) {
            if (dataType != DataType.DATA_METRICS) {
                h.progress(cnt++);
            }
            if (r != null) {
                LogRecords.write(data, r);
            }
        }
        data.write("</uigestures>\n".getBytes("utf-8")); // NOI18N
        LOG.log(Level.FINE, "uploadLogs, flushing"); // NOI18N
        data.flush();
        gzip.finish();
        os.print(END_OF_DATA_BLOCK+"--"+eol);   // "--" nothing more will come
        os.close();

        if (dataType != DataType.DATA_METRICS) {
            h.progress(NbBundle.getMessage(Installer.class, "MSG_UploadReading"), cnt + 10);
        }

        //System.err.println("DONE: Uploaded logs to "+postURL+" in "+Thread.currentThread());
        
        StringBuffer redir = null;
        Matcher m = null;
        if (!fileProtocol) {
            assert conn != null;
            LOG.log(Level.FINE, "uploadLogs, reading reply"); // NOI18N
            if (conn instanceof HttpURLConnection) {
                int responseCode = ((HttpURLConnection) conn).getResponseCode();
                String responseMessage = ((HttpURLConnection) conn).getResponseMessage();
                LOG.log(Level.FINE, "upload logs: Response Code = {0}, message = {1}", new Object[]{responseCode, responseMessage});
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new ResponseException(responseMessage);
                }
            }
            try (InputStream is = conn.getInputStream()) {
                redir = new StringBuffer();
                for (;;) {
                    int ch = is.read();
                    if (ch == -1) {
                        break;
                    }
                    redir.append((char)ch);
                }
            }

            if (dataType != DataType.DATA_METRICS) {
                h.progress(cnt + 20);
            }

            LOG.log(Level.FINE, "uploadLogs, Reply from uploadLogs: {0}", redir);

            Pattern p = Pattern.compile("<meta\\s*http-equiv=.Refresh.\\s*content.*url=['\"]?([^'\" ]*)\\s*['\"]", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            m = p.matcher(redir);
        }

        if (isOOM){
            FileObject fo = FileUtil.createData(getHeapDump(recs, isAfterRestart));
            FileObject folder = fo.getParent();
            String submittedName = fo.getName() + "_submitted"; // NOI18N
            FileObject submittedFO = folder.getFileObject(submittedName, fo.getExt());
            if (submittedFO != null){
                submittedFO.delete();
            }
            FileLock lock = fo.lock();
            fo.rename(lock, submittedName, fo.getExt());
            lock.releaseLock();
        }
        if (deadlockFile != null) {
            //deadlockFile.delete();
        }

        if (!fileProtocol) {
            assert m != null;
            if (m.find()) {
                LOG.log(Level.FINE, "uploadLogs, found url = {0}", m.group(1)); // NOI18N
                return new URL(m.group(1));
            } else {
                assert redir != null;
                File f = Files.createTempFile("uipage", "html").toFile();
                f.deleteOnExit();
                try (FileWriter w = new FileWriter(f)) {
                    w.write(redir.toString());
                }
                LOG.log(Level.FINE, "uploadLogs, temporary url = {0}", Utilities.toURI(f)); // NOI18N
                return Utilities.toURI(f).toURL();
            }
        } else {
            return null;
        }
    }

    private static boolean isAfterRestartRecord(List<LogRecord> recs) {
        for (int i = recs.size() - 1; i >= 0; i--) {
            LogRecord r = recs.get(i);
            if (r.getThrown() != null) {
                return AfterRestartExceptions.isAfterRestartRecord(r);
            }
        }
        return false;
    }

    private static File getMessagesLog(boolean fromLastRun) {
        File userDir = Places.getUserDirectory();
        if (userDir == null) {
            return null;
        }
        File log = null;
        if (fromLastRun) {
            log = new File(userDir, NbBundle.getMessage(Installer.class, "LOG_FILE_LAST"));
        }
        if (log == null || !log.exists()) {
            log = new File(userDir, NbBundle.getMessage(Installer.class, "LOG_FILE"));
        }
        return log;
    }
    
    private static File getDeadlockFile(List<LogRecord> recs) {
        LogRecord thrownLog = getThrownLog(recs);
        if (thrownLog == null ||
            !thrownLog.getThrown().getClass().getName().endsWith("Detector$DeadlockDetectedException")) { // NOI18N
            
            return null;
        }
        String path = thrownLog.getMessage();
        File f = new File(path);
        if (f.exists() && f.canRead()) {
            return f;
        } else {
            return null;
        }
    }
    
    private static File getHeapDump(List<LogRecord> recs, boolean isAfterRestart) {
        LogRecord thrownLog = getThrownLog(recs);
        if (isAfterRestart) {
            Object[] parameters = thrownLog.getParameters();
            if (parameters != null && parameters.length > 0) {
                String heapDumpPath = (String) parameters[parameters.length - 1];
                File hdf = new File(heapDumpPath);
                if (!hdf.exists()) {
                    heapDumpPath += ".old";
                    hdf = new File(heapDumpPath);
                }
                return hdf;
            }
        }
        return getHeapDump();
    }

    static File getHeapDump() {
        String heapDumpPath = null;
        RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
        List<String> lst = RuntimemxBean.getInputArguments();
        for (String arg : lst) {
            if (arg.contains("XX:HeapDumpPath")){
                int index = arg.indexOf('=');
                heapDumpPath = arg.substring(index+1);
            }
        }

        if (heapDumpPath == null){
            LOG.info("XX:HeapDumpPath parametter not specified");
            return null;
        }
        File heapDumpFile = new File(heapDumpPath);
        if (heapDumpFile.exists() && heapDumpFile.canRead() && heapDumpFile.length() > 0) {
            return heapDumpFile;
        }
        LOG.log(Level.INFO, "heap dump was not created at {0}", heapDumpPath);
        LOG.log(Level.INFO, "heapdump file: exists():{0}, canRead():{1}, length:{2}",new Object[] 
                {heapDumpFile.exists(), heapDumpFile.canRead(), heapDumpFile.length()});
        // no heap dump file found - this can happen in case of OOME: unable to create new native thread
        // try to create heap dump 
        dumpHeap(heapDumpFile.getAbsolutePath());
        if (heapDumpFile.exists() && heapDumpFile.canRead() && heapDumpFile.length() > 0) {
            return heapDumpFile;
        }        
        LOG.log(Level.INFO, "heap dump failed for {0}", heapDumpPath);
        return null;
    }
    
    private static void uploadMessagesLog(PrintStream os, boolean fromLastRun) throws IOException {
        flushSystemLogs();
        File messagesLog = getMessagesLog(fromLastRun);
        if (messagesLog == null){
            return;
        }
        uploadGZFile(os, messagesLog);
    }
    
    private static void uploadGZFile(PrintStream os, File f) throws IOException {
        GZIPOutputStream gzip = new GZIPOutputStream(os);
        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(f))) {
            byte[] buffer = new byte[4096];
            int readLength = is.read(buffer);
            while (readLength != -1){
                gzip.write(buffer, 0, readLength);
                readLength = is.read(buffer);
            }
        } finally {
            gzip.finish();
        }
    }

    private static void flushSystemLogs(){
        System.out.flush();
        System.err.flush();
    }
    
    private static void dumpHeap(String path) {
        LOG.log(Level.INFO, "DUMPING HEAP"); // NOI18N
        Method m = null;
        Class c = null;
        HotSpotDiagnosticMXBean hdmxb = null;
        try {
            c = Class.forName("sun.management.ManagementFactoryHelper");    //NOI18N
        } catch (ClassNotFoundException exc) {
            Exceptions.printStackTrace(exc);
        }
        if (c != null) {
            try {
                m = c.getMethod("getDiagnosticMXBean");  //NOI18N
            } catch (NoSuchMethodException exc) {
                Exceptions.printStackTrace(exc);
            } catch (SecurityException exc) {
                Exceptions.printStackTrace(exc);
            }
        }
        if (m != null) {
            try {
                hdmxb = (HotSpotDiagnosticMXBean)m.invoke(null);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            LOG.log(Level.INFO, "Creating heap dump to "+path); // NOI18N
            try {
                hdmxb.dumpHeap(path, true);
                LOG.log(Level.INFO, "Heap dump successfully created in: "+path);    // NOI18N
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
        
    public static String findIdentity() {
        Preferences p = NbPreferences.root().node("org/netbeans/modules/autoupdate"); // NOI18N
        String id = p.get("qualifiedId", null);
        //Strip id prefix
        if (id != null){
            int ind = id.indexOf("0");
            if (ind != -1) {
                id = id.substring(ind + 1);
            }
        }
        LOG.log(Level.INFO, "findIdentity: {0}", id);
        return id;
    }

    static final class Form extends Object {
        final String url;
        String submitValue;

        public Form(String u) {
            url = u;
        }
    }

    static void copyWithEncoding(InputStream inputStream, OutputStream os,
                                 Map<String, String> replacements) throws IOException {
        byte[] arr = new byte[4096];

        final String encUTF8 = "utf-8";
        PushbackInputStream pin = new PushbackInputStream(inputStream, 4096);
        int len = pin.read(arr);
        if (len < 0) {
            // Nothing to read => nothing to copy
            return ;
        }
        pin.unread(arr, 0, len);
        String enc = findEncoding(arr, len);
        boolean replaceEnc = !enc.equalsIgnoreCase(encUTF8);
        BufferedReader br = new BufferedReader(new InputStreamReader(pin, enc));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, encUTF8));
        String text;
        while ((text = br.readLine()) != null) {
            if (replaceEnc) {
                Matcher replace = ENCODING.matcher(text);
                while (replace.find()) {
                    if (!encUTF8.equalsIgnoreCase(replace.group(1))) {
                        text = text.substring(0, replace.start(1)) + encUTF8 + text.substring(replace.end(1));
                        replace = ENCODING.matcher(text);
                    }
                }
            }
            for (Entry<String, String> re : replacements.entrySet()) {
                text = text.replace(re.getKey(), re.getValue());
            }
            bw.write(text);
            bw.newLine();
        }
        bw.flush();
    }
    
    private static String findEncoding(byte[] arr, int len) throws UnsupportedEncodingException {
        String enc = "utf-8";
        String text = new String(arr, 0, len, enc);
        Matcher m = ENCODING.matcher(text);
        if (m.find()) {
            enc = m.group(1);
            LOG.log(Level.FINE, "Downloaded with encoding ''{0}'':\n{1}", new Object[]{enc, text});
        } else {
            LOG.log(Level.FINE, "Downloaded with utf-8:\n{0}", text);
        }
        return enc;
    }

    private abstract static class Submit implements ActionListener, Runnable {

        private enum DialogState {

            NON_CREATED, CREATED, FAILED
        };
        private AtomicBoolean isSubmiting;// #114505 , report is sent two times
        protected String exitMsg;
        private DialogDescriptor dd;
        private final Object ddLock = new Object();
        protected String msg;
        protected final boolean report;//property tells me wheather I'm in report mode
        protected boolean okToExit;
        protected ReportPanel reportPanel;
        private URL url;
        private DialogState dialogState = DialogState.NON_CREATED;
        private boolean checkingResult, refresh = false;
        protected boolean errorPage = false;
        protected String errorURL = null;
        protected String errorMessage = null;
        protected DataType dataType = DataType.DATA_UIGESTURE;
        protected final List<LogRecord> recs;
        protected boolean isOOM = false;
        protected boolean isAfterRestart = false;
        protected ExceptionsSettings settings;
        protected JProgressBar jpb = new JProgressBar();
        
        public Submit(String msg) {
            this(msg,DataType.DATA_UIGESTURE, null);
        }

        public Submit(String msg, DataType dataType, List<LogRecord> recs) {
            this.msg = msg;
            this.dataType = dataType;
            isSubmiting = new AtomicBoolean(false);
            if (recs != null) {
                this.recs = recs;
            } else {
                if (dataType == DataType.DATA_METRICS) {
                    this.recs = getLogsMetrics();
                } else {
                    this.recs = new ArrayList<>(getLogs());
                }
            }
            if ("ERROR_URL".equals(msg)) { // NOI18N
                report = true;
            } else {
                report = false;
            }
        }

        protected abstract void createDialog();
        protected abstract Object showDialogAndGetValue(DialogDescriptor dd);
        protected abstract void closeDialog();
        protected abstract void alterMessage(DialogDescriptor dd);
        protected abstract void viewData();
        protected abstract void assignInternalURL(URL u);
        protected abstract void addMoreLogs(List<? super String> params, boolean openPasswd);
        protected abstract void showURL(URL externalURL, boolean inIDE);
        protected abstract SlownessData getSlownessData();
        
        protected final DialogDescriptor findDD() {
            synchronized (ddLock) {
                if (dd == null) {
                    if (report) {
                        dd = new DialogDescriptor(null, NbBundle.getMessage(Installer.class, "ErrorDialogTitle"));
                    } else {
                        dd = new DialogDescriptor(null, NbBundle.getMessage(Installer.class, "MSG_SubmitDialogTitle"));
                    }
                }
                return dd;
            }
        }

        public void doShow(DataType dataType) {
            //System.err.println("doShow("+dataType+")");
            //Thread.dumpStack();
            if (dataType == DataType.DATA_UIGESTURE) {
                try {
                    logRecords.set(recs);
                    logDeactivated();
                } finally {
                    logRecords.remove();
                }
            }
            findDD();
            
            exitMsg = NbBundle.getMessage(Installer.class, "MSG_" + msg + "_EXIT"); // NOI18N

            String defaultURI = NbBundle.getMessage(Installer.class, msg);
            String replace = System.getProperty("org.netbeans.modules.uihandler.LoadURI"); // NOI18N
            if (replace != null) {
                defaultURI = replace;
            }
            LOG.log(Level.FINE, "doShow, exitMsg = {0}, defaultURI = {1}", new Object[] { exitMsg, defaultURI }); // NOI18N
            if (defaultURI == null || defaultURI.length() == 0) {
                okToExit = true;
                return;
            }

            synchronized (this) {
                RP_UI.post(this);
                while (dialogState == DialogState.NON_CREATED) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                notifyAll();
            }
            if (dialogState == DialogState.FAILED) {
                return;
            }

            LOG.log(Level.FINE, "doShow, dialog has been created"); // NOI18N
            boolean firstRound = true;
            //StringBuilder sb = new StringBuilder(1024);
            for (;;) {
                String connURL = defaultURI;
                File tmp = null;
                try {
                    if (url == null) {
                        url = new URL(defaultURI); // NOI18N
                    }

                    LOG.log(Level.FINE, "doShow, reading from = {0}", url);
                    //sb.append("doShow reading from: ").append(url).append("\n");
                    URLConnection conn = url.openConnection();
                    conn.setRequestProperty("User-Agent", "NetBeans");
                    conn.setConnectTimeout(5000);
                    tmp = Files.createTempFile("uigesture", ".html").toFile();
                    tmp.deleteOnExit();
                    try (FileOutputStream os = new FileOutputStream(tmp)) {
                        Map<String, String> replacements = new HashMap<>();
                        String errURL = (errorURL == null) ? "" : errorURL;
                        replacements.put("{org.netbeans.modules.uihandler.LoadURL}", errURL);
                        String errMsg = (errorMessage == null) ? "" : errorMessage;
                        replacements.put("{org.netbeans.modules.uihandler.LoadError}", errMsg);
                        if(conn instanceof HttpsURLConnection){
                            Installer.initSSL((HttpsURLConnection) conn);
                        }
                        //for HTTP or HTTPS: conenct and read response - redirection or not?
                        if (conn instanceof HttpURLConnection){
                            conn.connect();
                            if (((HttpURLConnection) conn).getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP ) {
                                // in case of redirection, try to obtain new URL
                                String redirUrl = conn.getHeaderField("Location"); //NOI18N
                                if( null != redirUrl && !redirUrl.isEmpty() ) {
                                    //create connection to redirected url and substitute original conn
                                    URL redirectedUrl = new URL(redirUrl);
                                    URLConnection connRedir = redirectedUrl.openConnection();
                                    connRedir.setRequestProperty("User-Agent", "NetBeans");
                                    connRedir.setConnectTimeout(5000);
                                    conn = (HttpURLConnection) connRedir;
                                }
                            }
                        }
                        copyWithEncoding(conn.getInputStream(), os, replacements);
                        connURL = conn.getURL().toExternalForm(); // URL could change by redirect
                    }
                    conn.getInputStream().close();
                    LOG.log(Level.FINE, "doShow, all read from = {0}", connURL); // NOI18N
                    /* //Temporary logging to investigate #141497
                    InputStream is = new FileInputStream(tmp);
                    byte [] arr = new byte [is.available()];
                    is.read(arr);
                    sb.append("Content:\n").append(new String(arr)).append("\nEnd of Content");
                    is.close();
                    *///End
                    try (InputStream is = new FileInputStream(tmp)) {
                        DialogDescriptor dd = findDD();
                        parseButtons(is, exitMsg, dd);
                        LOG.log(Level.FINE, "doShow, parsing buttons: {0}", Arrays.toString(dd.getOptions())); // NOI18N
                        alterMessage(dd);
                    }
                    url = Utilities.toURI(tmp).toURL();
                } catch (InterruptedException |
                         InvocationTargetException |
                         ParserConfigurationException ex) {
                    LOG.log(Level.WARNING, null, ex);
                } catch (SAXException ex) {
                    boolean doContinue = catchParsingProblem(ex, connURL);
                    //LOG.log(Level.INFO, sb.toString());
                    if (doContinue) {
                        if (tmp != null) {
                            tmp.delete();
                        }
                        continue;
                    }
                } catch (IllegalStateException |
                         java.net.SocketTimeoutException |
                         UnknownHostException |
                         NoRouteToHostException |
                         ConnectException ex) {
                    boolean doContinue = catchConnectionProblem(ex);
                    if (doContinue) {
                        if (tmp != null) {
                            tmp.delete();
                        }
                        continue;
                    }
                } catch (IOException ex) {
                    if (firstRound) {
                        boolean doContinue = catchConnectionProblem(ex);
                        if (doContinue) {
                            if (tmp != null) {
                                tmp.delete();
                            }
                            firstRound = false;
                            continue;
                        }
                    } else {// preventing from deadlock while reading error page
                        LOG.log(Level.WARNING, url.toExternalForm(), ex);
                    }
                }
                firstRound = true;
                LOG.log(Level.FINE, "doShow, assignInternalURL = {0}", url);
                assignInternalURL(url);
                refresh = false;
                synchronized (this) {
                    while (dialogState == DialogState.CREATED && !refresh) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                if (refresh){
                    url=null;
                    continue;
                }
                break;
            }
            LOG.log(Level.FINE, "doShow, dialogCreated, exiting");
        }

        protected final synchronized void doCloseDialog() {
            dialogState = DialogState.NON_CREATED;
            closeDialog();
            notifyAll();
            LOG.log(Level.FINE, "doCloseDialog");
        }

        private boolean catchConnectionProblem(Exception exception){
            LOG.log(Level.INFO, url.toExternalForm(), exception);
            errorURL = url.toExternalForm();
            errorMessage = exception.getLocalizedMessage();
            URL newURL = getUnknownHostExceptionURL();
            boolean doContinue = !newURL.equals(url); // Prevent from an infinite loop
            url = newURL;
            jpb.setVisible(false);
            errorPage = true;
            return doContinue;
        }

        private URL getUnknownHostExceptionURL() {
            try {
                URL resource = new URL("nbresloc:/org/netbeans/modules/uihandler/UnknownHostException.html"); // NOI18N
                return resource;
            } catch (MalformedURLException ex) {
                LOG.log(Level.SEVERE, ex.getMessage(), ex);
            }
            return getClass().getResource("UnknownHostException.html"); // NOI18N
        }
        
        private boolean catchParsingProblem(Exception exception, String connectionURL) {
            LOG.log(Level.INFO, connectionURL, exception);
            errorURL = connectionURL;
            errorMessage = exception.getLocalizedMessage();
            URL newURL = getParsingProblemURL();
            boolean doContinue = !newURL.equals(url); // Prevent from an infinite loop
            url = newURL;
            jpb.setVisible(false);
            errorPage = true;
            return doContinue;
        }

        private URL getParsingProblemURL() {
            try {
                URL resource = new URL("nbresloc:/org/netbeans/modules/uihandler/SAXException.html"); // NOI18N
                return resource;
            } catch (MalformedURLException ex) {
                LOG.log(Level.SEVERE, ex.getMessage(), ex);
            }
            return getClass().getResource("SAXException.html"); // NOI18N
        }
        
        @Override
        public void run() {
            DialogState newState = DialogState.CREATED;
            try{
                createDialog();
            }catch (RuntimeException e){
                newState = DialogState.FAILED;
                throw e;
            } finally {
                synchronized (this) {
                    dialogState = newState;
                    // dialog created let the code go on
                    notifyAll();


                    try {
                        // wait till the other code runs
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            LOG.log(Level.FINE, "run, showDialogAndGetValue");
            Object res = showDialogAndGetValue(findDD());
            LOG.log(Level.FINE, "run, showDialogAndGetValue, res = {0}", res);

            if (res == exitMsg) {
                okToExit = true;
            }
            LOG.log(Level.FINE, "run, okToExit = {0}", okToExit);
            doCloseDialog();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final URL[] universalResourceLocator = new URL[1];
            String actionURL = decodeButtons(e.getSource(), universalResourceLocator, dataType);

            LOG.log(Level.FINE, "actionPerformed: command = {0}", e.getActionCommand()); // NOI18N
            //System.err.println("actionPerformed: command = "+e.getActionCommand()); // NOI18N
            //Thread.dumpStack();

            boolean submit = Button.SUBMIT.isCommand(actionURL);
            if (Button.AUTO_SUBMIT.isCommand(e.getActionCommand())) {
                submit = true;
                if(preferencesWritable) {
                    prefs.putBoolean("autoSubmitWhenFull", true); // NOI18N
                }
            }

            if (submit) { // NOI18N
                JButton button = null;
                if (e.getSource() instanceof JButton){
                    button = (JButton) e.getSource();
                    button.setEnabled(false);
                }
                final JButton submitButton = button;
                if (isSubmiting.getAndSet(true)) {
                    LOG.info("ALREADY SUBMITTING"); // NOI18N
                    return;
                }
		if (report){
                    reportPanel.showCheckingPassword();
		}
                RP_SUBMIT.post(new Runnable() {

                    @Override
                    public void run() {
                        if (dataType == DataType.DATA_UIGESTURE) {
                            LogRecord userData = getUserData(true, reportPanel);
                            LogRecord thrownLog = getThrownLog(recs);
                            if (thrownLog != null) {
                                recs.add(thrownLog);//exception selected by user
                            }
                            recs.add(BuildInfo.logBuildInfoRec());
                            SlownessData sd = getSlownessData();
                            if (sd != null){
                                recs.add(sd.getLogRec());
                            } else {
                                recs.add(TimeToFailure.logFailure());
                            }
                            recs.add(userData);
                            if ((report) && (!reportPanel.asAGuest())) {
                                if (!checkUserName(reportPanel)) {
                                    EventQueue.invokeLater(new Runnable(){
                                        @Override
                                        public void run() {
                                            submitButton.setEnabled(true);
                                            reportPanel.showWrongPassword();
                                        }
                                    });
                                    isSubmiting.set(false);
                                    return;
                                }
                            }
                            LOG.fine("posting upload UIGESTURES");// NOI18N
                        } else if (dataType == DataType.DATA_METRICS) {
                            LOG.fine("posting upload METRICS");// NOI18N
                        }
                        final List<LogRecord> recsFinal = recs;
                        RP_SUBMIT.post(new Runnable() {
                            @Override
                            public void run() {
                                uploadAndPost(recsFinal, universalResourceLocator[0], dataType, getSlownessData());
                            }
                        });
                        okToExit = false;
                        // this should close the descriptor
                        EventQueue.invokeLater(new Runnable(){
                            @Override
                            public void run() {
                                doCloseDialog();
                            }
                        });
                    }
                });
                return;
            }

            if (Button.REDIRECT.isCommand(e.getActionCommand())){
                if (universalResourceLocator[0] != null) {
                    showURL(universalResourceLocator[0], false);
                }
                doCloseDialog();
                return ;
            }

            if (Button.PROXY.isCommand(e.getActionCommand())){
                if (RP_OPT == null){
                    RP_OPT = new RequestProcessor("UI Gestures - Show Options");
                }
                //show Tools/Options dialog
                RP_OPT.post(new Runnable() {
                    @Override
                    public void run() {
                        OptionsDisplayer.getDefault().open("General"); // NOI18N
                    }
                });
            }

            if (Button.REFRESH.isCommand(e.getActionCommand())){
                refresh = true;
                errorPage = false;
                errorURL = null;
                errorMessage = null;
                synchronized(this){
                    notifyAll();
                }
                return;
            }

            if (Button.VIEW_DATA.isCommand(e.getActionCommand())) { // NOI18N
                viewData();
                return;
            }

            if (Button.NEVER_AGAIN.isCommand(e.getActionCommand())) { // NOI18N
                LOG.log(Level.FINE, "Assigning ask.never.again.{0} to true", msg); // NOI18N
                NbPreferences.forModule(Installer.class).putBoolean("ask.never.again." + msg, true); // NOI18N
                okToExit = true;
                // this should close the descriptor
                doCloseDialog();
                return;
            }

            if (Button.EXIT.isCommand(e.getActionCommand())) {
                // this should close the descriptor
                doCloseDialog();
            }
        }

        private boolean checkUserName(ReportPanel panel) {
            checkingResult = true;
            try {
                String login = URLEncoder.encode(panel.getUserName(), "UTF-8");
                String encryptedPasswd = URLEncoder.encode(PasswdEncryption.encrypt(new String(panel.getPasswdChars())), "UTF-8");
                char[] array = new char[100];
                URL checkingServerURL = new URL(NbBundle.getMessage(Installer.class, "CHECKING_SERVER_URL", login, encryptedPasswd));
                URLConnection connection = checkingServerURL.openConnection();
                connection.setRequestProperty("User-Agent", "NetBeans");
                connection.setReadTimeout(20000);
                Reader reader = new InputStreamReader(connection.getInputStream());
                int length = reader.read(array);
                checkingResult = Boolean.valueOf(new String(array, 0, length));
            } catch (UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
            } catch (Exception exception) {
                Logger.getLogger(Installer.class.getName()).log(Level.INFO, "Checking password failed", exception); // NOI18N
            }
            return checkingResult;
        }

        private void uploadAndPost(List<LogRecord> recs, URL u, DataType dataType, SlownessData slownData) {
            URL nextURL = null;

            if(preferencesWritable) {
                prefs.putInt("submitted", 1 + prefs.getInt("submitted", 0)); // NOI18N
            }

            try {
                if (dataType == DataType.DATA_METRICS) {
                    logMetricsUploadFailed = false;
                }
                nextURL = uploadLogs(u, findIdentity(), Collections.<String,String>emptyMap(),
                                     recs, dataType, report, slownData, isOOM, isAfterRestart);
            } catch (IOException ex) {
                LOG.log(Level.INFO, null, ex);
                if (dataType == DataType.DATA_METRICS) {
                    logMetricsUploadFailed = true;
                }
                if (dataType != DataType.DATA_METRICS) {
                    String txt;
                    String logFile;
                    boolean fromLastRun = recs.size() > 0 && isAfterRestart;
                    File log = getMessagesLog(fromLastRun);
                    if (log != null) {
                        logFile = log.getAbsolutePath();
                    } else {
                        logFile = NbBundle.getMessage(Installer.class, "LOG_FILE");
                    }
                    String responseMessage = null;
                    if (ex instanceof ResponseException) {
                        responseMessage = ex.getLocalizedMessage();
                    }
                    if (!report) {
                        if (responseMessage != null) {
                            txt = NbBundle.getMessage(Installer.class, "MSG_ConnetionFailedWithResponse", u.getHost(), u.toExternalForm(), logFile, responseMessage);
                        } else {
                            txt = NbBundle.getMessage(Installer.class, "MSG_ConnetionFailed", u.getHost(), u.toExternalForm(), logFile);
                        }
                    } else {
                        if (responseMessage != null) {
                            txt = NbBundle.getMessage(Installer.class, "MSG_ConnetionFailedReportWithResponse", u.getHost(), u.toExternalForm(), logFile, responseMessage);
                        } else {
                            txt = NbBundle.getMessage(Installer.class, "MSG_ConnetionFailedReport", u.getHost(), u.toExternalForm(), logFile);
                        }
                    }
                    Object dlg = ConnectionErrorDlg.get(txt);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(dlg, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(nd);
                }
            }
            if (dataType == DataType.DATA_METRICS) {
                if (preferencesWritable) {
                    prefs.putBoolean("metrics.upload.failed", logMetricsUploadFailed); // NOI18N
                }
            }
            if (nextURL != null) {
                clearLogs();
                showURL(nextURL, report);
            }
        }

        protected final LogRecord getUserData(boolean openPasswd, ReportPanel panel) {
            LogRecord userData;
            ArrayList<String> params = new ArrayList<>(6);
            params.add(getOS());
            params.add(getVM());
            params.add(getVersion());
            if (panel != null){
                if (panel.asAGuest()){
                    params.add("");
                }else{
                    params.add(panel.getUserName());
                }
            } else if (settings != null) {
                params.add(settings.getUserName());
            }
            addMoreLogs(params, openPasswd);
            userData = new LogRecord(Level.CONFIG, USER_CONFIGURATION);
            userData.setResourceBundle(NbBundle.getBundle(Installer.class));
            userData.setResourceBundleName(Installer.class.getPackage().getName()+".Bundle");
            userData.setParameters(params.toArray());
            return userData;
        }

        private static String getOS(){
            String unknown = "unknown";                                   // NOI18N
            String str = System.getProperty("os.name", unknown)+", "+     // NOI18N
                    System.getProperty("os.version", unknown)+", "+       // NOI18N
                    System.getProperty("os.arch", unknown);               // NOI18N
            return str;
        }

        private static String getVersion(){
            String str = ""; // NOI18N
            String versionResourceBundle = NbBundle.getMessage(Installer.class, "ApplicationVersionResourceBundle");    // NOI18N
            String versionResourceKey = NbBundle.getMessage(Installer.class, "ApplicationVersionResourceKey");          // NOI18N
            String sysPropertyVersionResourceArg = NbBundle.getMessage(Installer.class, "ApplicationVersionSysPropertyResourceArg");// NOI18N
            try {
                str = MessageFormat.format(
                        NbBundle.getBundle(versionResourceBundle).getString(versionResourceKey),
                        new Object[] {System.getProperty(sysPropertyVersionResourceArg)}
                );
            } catch (MissingResourceException ex) {
                LOG.log(Level.FINE, ex.getMessage(), ex);
            }
            return str;
        }

        private static String getVM(){
            return System.getProperty("java.vm.name", "unknown") + ", " // NOI18N
                 + System.getProperty("java.vm.version", "") + ", " // NOI18N
                 + System.getProperty("java.runtime.name", "unknown") + ", " // NOI18N
                 + System.getProperty("java.runtime.version", ""); // NOI18N
        }

    } // end of Submit

    protected static String createMessage(Throwable thr){
        //ignore causes with empty stacktraces -> they are just annotations
        while ((thr.getCause() != null) && (thr.getCause().getStackTrace().length != 0)){
            thr = thr.getCause();
        }
        String message = thr.toString();
        if (message.startsWith("java.lang.")){
            message = message.substring(10);
        }
        int indexClassName = message.indexOf(':');
        if (indexClassName == -1){ // there is no message after className
            if (thr.getStackTrace().length != 0){
                StackTraceElement elem = thr.getStackTrace()[0];
                return message + " at " + elem.getClassName()+"."+elem.getMethodName();
            }
        }
        return message;
    }

    static final class SubmitInteractive extends Submit
    implements HyperlinkListener {
        private boolean connectDialog;
        private Dialog d;
        private SubmitPanel panel;
        private JEditorPane browser;
        private boolean urlAssigned;
        private SlownessData slownData = null;
        
        public SubmitInteractive(String msg, boolean connectDialog) {
            this(msg, connectDialog, DataType.DATA_UIGESTURE);
        }

        public SubmitInteractive(String msg, boolean connectDialog, DataType dataType) {
            this(msg, connectDialog, dataType, null, null);
        }

        private SubmitInteractive(String msg, boolean connectDialog, DataType dataType,
                                  List<LogRecord> recs, SlownessData slownData) {
            this(msg, connectDialog, dataType, recs, slownData, false);
        }
        
        private SubmitInteractive(String msg, boolean connectDialog, DataType dataType,
                                  List<LogRecord> recs, SlownessData slownData,
                                  boolean isAfterRestart) {
            super(msg, dataType, recs);
            this.connectDialog = connectDialog;
            this.slownData = slownData;
            this.isAfterRestart = isAfterRestart;
        }

        @Override
        protected void createDialog() {
            String message = null;
            if (slownData != null) {
                String time = Long.toString(slownData.getTime());
                if (slownData.getSlownessType() != null){
                    message = String.format("%1$s took %2$s ms.", slownData.getSlownessType(), time);// NOI18N
                }else if (slownData.getLatestActionName() != null) {
                    message = String.format("Invoking %1$s took %2$s ms.", slownData.getLatestActionName(), time);// NOI18N
                } else {
                    message = String.format("AWT thread blocked for %1$s ms.", time); // NOI18N
                }
            } else {
                Throwable t = getThrown(recs);
                if (t != null) {
                    message = createMessage(t);
                    if (message.contains("OutOfMemoryError") && getHeapDump(recs, isAfterRestart) != null) {
                        isOOM = true;
                    }
                }
            }
            final String summary = message;
            settings = new ExceptionsSettings();
            try {
                EventQueue.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        LOG.log(Level.FINE, "Window system initialized:", WindowManager.getDefault().getMainWindow().isVisible());
                        if (reportPanel==null) {
                            reportPanel = new ReportPanel(isOOM, settings);
                        }
                        if (summary != null){
                            reportPanel.setSummary(summary);
                        }
                        Dimension dim = new Dimension(350, 50);
                        if ("ERROR_URL".equals(msg)) {
                            dim = new Dimension(370, 200);
                        }
                        browser = new JEditorPane();
                        try {
                            browser.setEditable(false);
                            try {
                                URL resource = new URL("nbresloc:/org/netbeans/modules/uihandler/Connecting.html"); // NOI18N
                                browser.setPage(resource); // NOI18N
                                browser.putClientProperty(javax.swing.JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
                            } catch (IOException ex) {
                                LOG.log(Level.SEVERE, ex.getMessage(), ex);
                            }
                            browser.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 0, 8));
                            browser.setPreferredSize(dim);
                            // browser.setEditorKit(new HTMLEditorKit()); // needed up to nb5.5
                            browser.setBackground(new JLabel().getBackground());
                            browser.addHyperlinkListener(SubmitInteractive.this);
                        } catch (NullPointerException x) {
                            LOG.log(Level.WARNING, "Java bug #7050995?", x);
                        } catch (ArrayIndexOutOfBoundsException x) {
                            LOG.log(Level.WARNING, "Java bug ?", x);
                        }
                        JScrollPane p = new JScrollPane();
                        p.setViewportView(browser);
                        p.setBorder(BorderFactory.createEmptyBorder());
                        p.setPreferredSize(dim);
                        DialogDescriptor descr = findDD();
                        JPanel jp = new JPanel();
                        BoxLayout l = new BoxLayout(jp, BoxLayout.Y_AXIS);
                        jp.setLayout(l);
                        jpb.setVisible(true);
                        jpb.setIndeterminate(true);
                        jp.add(p);
                        jp.add(jpb);
                        descr.setMessage(jp);
                       //        AbstractNode root = new AbstractNode(new Children.Array());
                        //        root.setName("root"); // NOI18N
                        //        root.setDisplayName(NbBundle.getMessage(Installer.class, "MSG_RootDisplayName", recs.size(), new Date()));
                        //        root.setIconBaseWithExtension("org/netbeans/modules/uihandler/logs.gif");
                        //        for (LogRecord r : recs) {
                        //            root.getChildren().add(new Node[] { UINode.create(r) });
                        //        }
                        //
                        //        panel.getExplorerManager().setRootContext(root);
                        Object[] arr = new Object[]{exitMsg};
                        descr.setOptions(arr);
                        descr.setClosingOptions(arr);
                        descr.setButtonListener(SubmitInteractive.this);
                        descr.setModal(true);
                        d = DialogDisplayer.getDefault().createDialog(descr);
                    }
                });
            } catch (InterruptedException | InvocationTargetException ex) {
                throw new IllegalStateException(ex);
            }
            assert d != null;
        }

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                showURL(e.getURL(), false);
            }
        }

        @Override
        protected void closeDialog() {
            if (d == null) {
                return;
            }
            reportPanel.saveUserData();
            RP_UI.post(new Runnable() {

                @Override
                public void run() {
                    settings.save();
                }
            });
            findDD().setValue(DialogDescriptor.CLOSED_OPTION);
            d.setVisible(false);
            d.dispose(); // fix the issue #137714
            d = null;
        }

        private void procesLog(LogRecord r, LinkedList<Node> nodes, StringBuilder builder){
            Node n = UINode.create(r);
            nodes.add(n);
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                int offset = builder.length();
                n.setValue("offset", offset); // NOI18N
                LogRecords.write(os, r);
                builder.append(os.toString("UTF-8")); // NOI18N
            } catch (IOException ex) {
                Installer.LOG.log(Level.WARNING, null, ex);
            }
        }

        private class DataLoader implements Runnable{
            private final StringBuilder panelContent = new StringBuilder();
            private final AbstractNode root = new AbstractNode(new Children.Array());
            @Override
            public void run() {
                if (EventQueue.isDispatchThread()){
                    panel.setText(panelContent.toString());
                    panel.getExplorerManager().setRootContext(root);
                } else {
                    List<LogRecord> displayedRecords = new ArrayList<>(recs);
                    LinkedList<Node> nodes = new LinkedList<>();
                    root.setName("root"); // NOI18N
                    root.setDisplayName(NbBundle.getMessage(Installer.class, "MSG_RootDisplayName", displayedRecords.size() + 1, new Date()));
                    root.setIconBaseWithExtension("org/netbeans/modules/uihandler/logs.gif");
                    for (LogRecord r : displayedRecords) {
                        procesLog(r, nodes, panelContent);
                    }
                    procesLog(getUserData(false, reportPanel), nodes, panelContent);
                    root.getChildren().add(nodes.toArray(new Node[0]));
                    EventQueue.invokeLater(this);
                }
            }
        }
        
        @Override
        protected void viewData() {
            if (panel == null) {
                TimeToFailure.logAction();
                panel = new SubmitPanel();
                RequestProcessor.getDefault().post(new DataLoader());
                panel.setText(NbBundle.getMessage(Installer.class, "LOADING_TEXT"));
                panel.getExplorerManager().setRootContext(Node.EMPTY);
            }
            DialogDescriptor viewDD;
            if (!report){
                 viewDD = new DialogDescriptor(panel, NbBundle.getMessage(Installer.class, "VIEW_DATA_TILTE"));
            } else {
                flushSystemLogs();
                JTabbedPane tabs = new JTabbedPane();
                tabs.addTab(org.openide.util.NbBundle.getMessage(Installer.class, "UI_TAB_TITLE"), panel);
                tabs.setPreferredSize(panel.getPreferredSize());
                boolean fromLastRun = recs.size() > 0 && isAfterRestart;
                File messagesLog = getMessagesLog(fromLastRun);
                try {
                    JEditorPane pane = new JEditorPane(Utilities.toURI(messagesLog).toURL());
                    pane.setEditable(false);
                    pane.setPreferredSize(panel.getPreferredSize());
                    tabs.addTab(org.openide.util.NbBundle.getMessage(Installer.class, "IDE_LOG_TAB_TITLE"), new JScrollPane(pane));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                viewDD = new DialogDescriptor(tabs, NbBundle.getMessage(Installer.class, "VIEW_DATA_TILTE"));
            }
            viewDD.setModal(false);
            Object[] closingOption = new Object[] { DialogDescriptor.CANCEL_OPTION  };
            viewDD.setOptions(closingOption);
            viewDD.setClosingOptions(closingOption);
            List<Object> additionalButtons = new ArrayList<>();
            if (slownData != null){
                JButton slownButton = new JButton();
                org.openide.awt.Mnemonics.setLocalizedText(slownButton, 
                        NbBundle.getMessage(Installer.class, "SubmitPanel.profileData.text"));
                slownButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showProfilerSnapshot(e);
                    }
                });
                additionalButtons.add(slownButton);
            }
            if (isOOM){
                JButton heapDumpButton = new JButton();
                org.openide.awt.Mnemonics.setLocalizedText(heapDumpButton,
                        NbBundle.getMessage(Installer.class, "SubmitPanel.heapDump.text"));
                heapDumpButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showHeapDump(e);
                    }
                });
//                additionalButtons.add(heapDumpButton);
            }
            viewDD.setAdditionalOptions(additionalButtons.toArray());
            Dialog view = DialogDisplayer.getDefault().createDialog(viewDD);
            view.setVisible(true);
        }

        private void showProfilerSnapshot(ActionEvent e){
             File tempFile = null;
             try { 
                 tempFile = Files.createTempFile("selfsampler", ".npss").toFile(); // NOI18N
                 tempFile = FileUtil.normalizeFile(tempFile);
                 try (OutputStream os = new FileOutputStream(tempFile)) {
                     os.write(slownData.getNpsContent());
                 }

                 File varLogs = logsDirectory();
                 File gestures = (varLogs != null) ? new File(varLogs, "uigestures") : null; // NOI18N

                 SelfSampleVFS fs;
                 if (gestures != null && gestures.exists()) {
                     fs = new SelfSampleVFS(
                             new String[]{"selfsampler.npss", "selfsampler.log"},
                             new File[]{tempFile, gestures});
                 } else {
                     fs = new SelfSampleVFS(
                             new String[]{"selfsampler.npss"},
                             new File[]{tempFile});
                 }
                 FileObject fo = fs.findResource("selfsampler.npss");
                 final Node obj = DataObject.find(fo).getNodeDelegate();
                 Action a = obj.getPreferredAction();
                 if (a instanceof ContextAwareAction) {
                     a = ((ContextAwareAction)a).createContextAwareInstance(obj.getLookup());
                 }
                 a.actionPerformed(e);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (tempFile != null) tempFile.deleteOnExit();
            }
        }

        private void showHeapDump(ActionEvent e){
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override protected void assignInternalURL(final URL u) {
            if (browser != null) {
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        try {
                            browser.setPage(u);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
            markAssigned();
        }

        private synchronized void markAssigned(){
            urlAssigned = true;
            notifyAll();
        }

        @Override
        protected void showURL(URL u, boolean inIDE) {
            LOG.log(Level.FINE, "opening URL: {0}", u); // NOI18N
            if (inIDE){
                ReporterResultTopComponent.showUploadDone(u);
            }else{
                HtmlBrowser.URLDisplayer.getDefault().showURL(u);
            }
        }

        @Override
        protected void addMoreLogs(List<? super String> params, boolean openPasswd) {
            if ((reportPanel != null) && (report)){
                params.add(reportPanel.getSummary());
                params.add(reportPanel.getComment());
                try {
                    char[] passwd = reportPanel.getPasswdChars();
                    if ((openPasswd) && (passwd.length != 0) && (!reportPanel.asAGuest())){
                        String pwd = new String(passwd);
                        pwd = PasswdEncryption.encrypt(pwd);
                        params.add(pwd);
                    } else {
                        params.add("*********");// NOI18N
                    }
                } catch (GeneralSecurityException | IOException exc) {
                    LOG.log(Level.WARNING, "PASSWORD ENCRYPTION ERROR", exc);// NOI18N
                }
            }
        }

        @Override
        protected Object showDialogAndGetValue(DialogDescriptor dd) {
            if (!connectDialog) {
                synchronized (this) {
                    while (!urlAssigned) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
            d.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosed(WindowEvent e) {
                    doCloseDialog();
                }
            });
            try {
                EventQueue.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        if (d != null) {
                            d.setModal(false);
                            d.setVisible(true);
                        }
                    }
                });
            } catch (InterruptedException | InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            synchronized (this){
                while (d != null && !dontWaitForUserInputInTests){
                    try{
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return dd.getValue();
        }
        
        @Override
        protected void alterMessage(final DialogDescriptor dd) {
            if ((dd.getOptions().length > 1) && ("ERROR_URL".equals(msg))){
                Object obj = dd.getOptions()[0];
                AbstractButton abut = null;
                String rptr = null;
                if (obj instanceof AbstractButton ) {
                    abut = (AbstractButton) obj;
                    if(abut.getText().equalsIgnoreCase("send")){
                        // disable sen button initialy, report panel can later enable it
                        abut.setEnabled(false);
                        abut.setToolTipText(NbBundle.getMessage(Installer.class, "ReportPanel.sendButton.tooltip"));//NOI18N
                        reportPanel.setSendButton(abut);
                    }
                }
                if (abut != null) {
                    rptr = (String) abut.getClientProperty("alt");
                }
                if (reportPanel != null && "reportDialog".equals(rptr)&&!errorPage) {
                    EventQueue.invokeLater(new Runnable(){

                        @Override
                        public void run() {
                            dd.setMessage(reportPanel);
                            reportPanel.setInitialFocus();
                        }
                    });
                }
            } else if("ERROR_URL".equals(msg)) {
                jpb.setVisible(false);
            }
        }

        @Override
        protected SlownessData getSlownessData() {
            return slownData;
        }
    } // end SubmitInteractive

    private static final class SubmitAutomatic extends Submit {
        final Button def;
        private boolean urlComputed;

        public SubmitAutomatic(String msg, Button def, DataType dataType, List<LogRecord> recs) {
            super(msg, dataType, recs);
            this.def = def;
        }

        public SubmitAutomatic(String msg, Button def) {
            this(msg, def, DataType.DATA_UIGESTURE, null);
        }

        @Override
        protected void createDialog() {
        }

        @Override
        protected void closeDialog() {
        }

        @Override
        protected void viewData() {
            assert false;
        }
        @Override
        protected synchronized void assignInternalURL(URL u) {
            urlComputed = true;
            notifyAll();
        }
        @Override
        protected void showURL(URL u, boolean inIDE) {
            hintURL = u;
        }

        @Override
        protected void addMoreLogs(List<? super String> params, boolean openPasswd) {
        }
        @Override
        protected Object showDialogAndGetValue(final DialogDescriptor dd) {
            //System.err.println("showDialogAndGetValue()");
            //Thread.dumpStack();
            while (!urlComputed) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            final JButton[] buttonPtr = new JButton[] { null };
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        Object[] options = dd.getOptions();
                        if (options == null) {
                            return ;
                        }
                        for (Object o : options) {
                            if (o instanceof JButton) {
                                JButton b = (JButton)o;
                                if (def.isCommand(b.getActionCommand())) {
                                    actionPerformed(new ActionEvent(b, 0, b.getActionCommand()));
                                    buttonPtr[0] = b;
                                    break;
                                }
                            }
                        }
                    }
                });
            } catch (InterruptedException | InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            return DialogDescriptor.CLOSED_OPTION;
        }
        @Override
        protected void alterMessage(DialogDescriptor dd) {
        }

        @Override
        protected SlownessData getSlownessData() {
            return null;
        }
    } // end SubmitAutomatic
    private static final class PrefChangeListener implements PreferenceChangeListener {

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (corePref.equals(evt.getNode()) && USAGE_STATISTICS_ENABLED.equals(evt.getKey())) {
                boolean newVal = Boolean.parseBoolean(evt.getNewValue());
                if (newVal != logMetricsEnabled) {
                    corePref.putBoolean(USAGE_STATISTICS_SET_BY_IDE, true);
                    logMetricsEnabled = newVal;
                    Logger log = Logger.getLogger(METRICS_LOGGER_NAME);
                    if (logMetricsEnabled) {
                        log.setUseParentHandlers(true);
                        log.setLevel(Level.FINEST);
                        log.addHandler(metrics);
                        MetricsHandler.setFlushOnRecord(false);
                    } else {
                        MetricsHandler.flushImmediatelly();
                        closeLogStreamMetrics();
                        log.removeHandler(metrics);
                    }
                }
            }
        }
    }

    static enum Button {
        EXIT("exit"),
        NEVER_AGAIN("never-again"),
        VIEW_DATA("view-data"),
        REDIRECT("redirect"),
        AUTO_SUBMIT("auto-submit"),
        SUBMIT("submit"),
        REFRESH("refresh"),
        PROXY("proxy");

        private final String name;
        Button(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean isCommand(String s) {
            return name.equals(s);
        }

        public static boolean isKnown(String n) {
            for (Button b : Button.values()) {
                if (n.equals(b.getName())) {
                    return true;
                }
            }
            return false;
        }
        public static boolean isSubmitTrigger(String n) {
            return SUBMIT.isCommand(n) || AUTO_SUBMIT.isCommand(n);
        }
    } // end of Buttons

    //  JUST FOR TESTS //
    private static boolean dontWaitForUserInputInTests = false;
    static void dontWaitForUserInputInTests(){
        dontWaitForUserInputInTests = true;
    }
      public static void initSSL( HttpURLConnection httpCon ) throws IOException {
        if( httpCon instanceof HttpsURLConnection ) {
            HttpsURLConnection https = ( HttpsURLConnection ) httpCon;

            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted( X509Certificate[] certs, String authType ) {
                        }

                        @Override
                        public void checkServerTrusted( X509Certificate[] certs, String authType ) {
                        }
                    } };
                SSLContext sslContext = SSLContext.getInstance( "SSL" ); //NOI18N
                sslContext.init( null, trustAllCerts, new SecureRandom() );
                https.setHostnameVerifier( new HostnameVerifier() {
                    @Override
                    public boolean verify( String hostname, SSLSession session ) {
                        return true;
                    }
                } );
                https.setSSLSocketFactory( sslContext.getSocketFactory() );
            } catch( Exception ex ) {
                throw new IOException( ex );
            }
        }
    }

    private static class ResponseException extends IOException {
        
        public ResponseException(String responseMessage) {
            super(responseMessage);
        }
    }
}

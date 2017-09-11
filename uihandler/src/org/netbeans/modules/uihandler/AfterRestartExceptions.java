/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uihandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.LogRecord;
import javax.swing.JButton;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.io.NbObjectInputStream;

/**
 *
 * @author Martin Entlicher
 */
class AfterRestartExceptions implements Runnable {
    
    private static final Set<String> scheduledThrowableClasses = new HashSet<>(
            Arrays.asList(new String[] {
                OutOfMemoryError.class.getName(),
                "org.netbeans.modules.deadlock.detector.Detector$DeadlockDetectedException",
            }));
    private static final Object IOLock = new Object();
    private static volatile Set<LogRecord> afterRestartRecords;
    
    private static boolean ignoreOOME;
    
    private AfterRestartExceptions() {}
    
    static void setIgnoreOOME(boolean ignoreOOME) {
        AfterRestartExceptions.ignoreOOME = ignoreOOME;
    }
    
    static boolean willSchedule(LogRecord record) {
        return getScheduledThrownClassName(record) != null;
    }
    
    private static String getScheduledThrownClassName(LogRecord record) {
        Throwable thrown = record.getThrown();
        if (thrown == null) {
            return null;
        }
        Throwable cause;
        while (((cause = thrown.getCause()) != null) && (cause.getStackTrace().length != 0)){
            thrown = cause;
        }
        String thrownClassName = thrown.getClass().getName();
        if (scheduledThrowableClasses.contains(thrownClassName)) {
            return thrownClassName;
        } else {
            return null;
        }
    }
    
    static boolean schedule(LogRecord record) {
        String thrownClassName = getScheduledThrownClassName(record);
        if (thrownClassName != null) {
            if (OutOfMemoryError.class.getName().equals(thrownClassName)) {
                if (ignoreOOME) {
                    return true;    // Simulate scheduling, but ignore
                }
                addHeapDump(record);
            }
            return save(record);
        } else {
            return false;
        }
    }
    
    private static File getLogRecordsFile() {
        File varLog = Installer.logsDirectory();
        if (varLog == null) {
            return null;
        }
        return new File(varLog, AfterRestartExceptions.class.getSimpleName());
    }
    
    private static void addHeapDump(LogRecord record) {
        File heapDump = Installer.getHeapDump();
        if (heapDump != null) {
            String heapDumpPath = heapDump.getAbsolutePath();
            Object[] parameters = record.getParameters();
            if (parameters == null) {
                parameters = new Object[] { heapDumpPath };
            } else {
                Object[] newParams = new Object[parameters.length + 1];
                System.arraycopy(parameters, 0, newParams, 0, parameters.length);
                newParams[parameters.length] = heapDumpPath;
                parameters = newParams;
            }
            record.setParameters(parameters);
        }
    }
    
    private static boolean save(LogRecord record) {
        File exceptionsFile = getLogRecordsFile();
        ObjectOutputStream os = null;
        synchronized (IOLock) {
            try {
                os = new ObjectOutputStream(new FileOutputStream(exceptionsFile, true));
                os.writeObject(record);
                return true;
            } catch (IOException ioex) {
                return false;
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ex) {}
                }
            }
        }
    }
    
    static void report() {
        if (readRecords()) {
            org.openide.windows.WindowManager.getDefault().invokeWhenUIReady(new AfterRestartExceptions());
        }
    }
    
    static boolean isAfterRestartRecord(LogRecord record) {
        Set<LogRecord> records = afterRestartRecords;
        return records != null && records.contains(record);
    }
    
    private static boolean readRecords() {
        File logRecords = getLogRecordsFile();
        if (logRecords == null || !logRecords.exists()) {
            return false;
        }
        Set<LogRecord> records = new LinkedHashSet<>();
        ObjectInputStream in = null;
        synchronized (IOLock) {
            try {
                in = new NbObjectInputStream(new FileInputStream(logRecords));
                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof LogRecord) {
                        records.add((LogRecord) obj);
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ioex) {}
                }
                logRecords.delete();
            }
        }
        if (records.isEmpty()) {
            return false;
        }
        afterRestartRecords = records;
        return true;
    }
    
    @NbBundle.Messages({ "TTL_AfterRestartReport=Unexpected Exception on Last Run",
                         "MSG_AfterRestartReportQuestion=There was an error during the last run of NetBeans IDE.\nCan you please report the problem?",
                         "BTN_ReviewAndReport=&Review and Report Problem" })
    @Override
    public void run() {
        
        final Set<LogRecord> records = afterRestartRecords;
        
        String msg = Bundle.MSG_AfterRestartReportQuestion();
        String title = Bundle.TTL_AfterRestartReport();
        int optionType = NotifyDescriptor.QUESTION_MESSAGE;
        JButton reportOption = new JButton();
        Mnemonics.setLocalizedText(reportOption, Bundle.BTN_ReviewAndReport());
        NotifyDescriptor confMessage = new NotifyDescriptor(msg, title, optionType,
                                                            NotifyDescriptor.QUESTION_MESSAGE,
                                                            new Object[] { reportOption, NotifyDescriptor.CANCEL_OPTION },
                                                            reportOption);
        Object ret = DialogDisplayer.getDefault().notify(confMessage);
        if (ret == reportOption) {
            Installer.RP.post(new Runnable() {
                @Override
                public void run() {
                    Installer.displaySummary("ERROR_URL", true, false, true,
                                             Installer.DataType.DATA_UIGESTURE,
                                             new ArrayList<>(records), null, true);
                    Installer.setSelectedExcParams(null);
                    afterRestartRecords = null;
                }
            });
        }
        
    }
    
}

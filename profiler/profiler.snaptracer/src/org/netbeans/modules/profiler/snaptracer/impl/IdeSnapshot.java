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
package org.netbeans.modules.profiler.snaptracer.impl;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogRecord;
import javax.swing.Icon;
import org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode;
import org.netbeans.modules.profiler.LoadedSnapshot;
import org.netbeans.modules.profiler.SampledCPUSnapshot;
import org.netbeans.modules.profiler.snaptracer.logs.LogReader;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/** Reads xml log and npss snapshot from file.
 *
 * @author Tomas Hurka
 */
public final class IdeSnapshot {

    private final SampledCPUSnapshot cpuSnapshot;
    private LogReader xmlLogs;
    private LogRecord lastRecord;
    private Map<Integer, LogRecord> recordsMap;
    private Map<Integer, LogRecordInfo> infosMap;
    private final FileObject npssFileObject;

    public IdeSnapshot(FileObject npssFO, FileObject uigestureFO) throws IOException {
        cpuSnapshot = new SampledCPUSnapshot(npssFO);
        npssFileObject = npssFO;
        if (uigestureFO != null) {
            xmlLogs = new LogReader(uigestureFO);
            xmlLogs.load();
            recordsMap = new HashMap<>();
            infosMap = new HashMap<>();
        }
    }

    int getSamplesCount() {
        return cpuSnapshot.getSamplesCount();
    }

    long getTimestamp(int sampleIndex) throws IOException {
        return cpuSnapshot.getTimestamp(sampleIndex);
    }

    FileObject getNpssFileObject() {
        return npssFileObject;
    }

    public boolean hasUiGestures() {
        return xmlLogs != null;
    }

    LoadedSnapshot getCPUSnapshot(int startIndex, int endIndex) throws IOException {
        return cpuSnapshot.getCPUSnapshot(startIndex, endIndex);
    }

    public long getValue(int sampleIndex, int valIndex) throws IOException {
        if (valIndex == 0) {
            return cpuSnapshot.getValue(sampleIndex, valIndex);
        } else if (xmlLogs != null) {
            Integer val = getLogRecordValue(sampleIndex);
            if (val != null) {
                return val.intValue();
            }
        }
        return 0;
    }

    public LogRecordInfo getLogInfoForValue(long loggerValue) {
        if (xmlLogs == null || loggerValue == 0) {
            return null;
        }
        Integer index = new Integer((int) loggerValue);
        LogRecordInfo info = infosMap.get(index);

        if (info == null) {
            LogRecord rec = recordsMap.get(index);

            assert rec != null : "Null record for value "+index;        // NOI18N
            info = new LogRecordInfo(rec);
            LogRecordDecorator.decorate(info);
            infosMap.put(index, info);
        }
        return info;
    }

    private Integer getLogRecordValue(int sampleIndex) throws IOException {
        long timestamp = getTimestamp(sampleIndex);
        LogRecord rec = xmlLogs.getRecordFor(timestamp / 1000000);
        if (rec != null) {
            long startTime = cpuSnapshot.getStartTime();
            long endTime = getTimestamp(getSamplesCount() - 1);
            long recTime = rec.getMillis() * 1000000;
            if (recTime > startTime && recTime < endTime) {
                if (rec != lastRecord) {
                    Integer index = new Integer(sampleIndex+1);
                    lastRecord = rec;
                    recordsMap.put(index, rec);
                    return index;
                }
            }
        }
        return null;
    }

    String getThreadDump(int sampleIndex) throws IOException {
        return cpuSnapshot.getThreadDump(sampleIndex);
    }

    List<Integer> getIntervals(int start, int end, PrestimeCPUCCTNode node) throws IOException {
        return cpuSnapshot.getIntervals(start,end,node);
    }

    public static final class LogRecordInfo {
        private static final int MAX_DISPLAY_NAME = 40;
        
        private String name;
        private String displayName;
        private String toolTip;
        private Icon icon;
        private LogRecord record;

        LogRecordInfo(LogRecord rec) {
            record = rec;
        }

        void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            if (displayName == null) {
                String message = record.getMessage();
                
                if (message != null && message.length() > 0) {
                    StringBuilder sb = new StringBuilder(message);
                    Object[] arr = record.getParameters();
                    
                    if (arr != null && arr.length > 0) {
                        String sep = " (";  // NOI18N
                        
                        for (Object par : arr) {
                            sb.append(sep);
                            sb.append(par);
                            sep = ", ";     // NOI18N
                            if (sb.length() > MAX_DISPLAY_NAME) {
                                return sb.substring(0,MAX_DISPLAY_NAME).concat(" ..."); // NOI18N
                            }
                        }
                        sb.append(")");     // NOI18N
                    }
                    return sb.toString();
                }
            }
            return displayName;
        }

        void setToolTip(String toolTip) {
            this.toolTip = toolTip;
        }

        public String getToolTip() {
            return toolTip;
        }

        void setIcon(Icon icon) {
            this.icon = icon;
        }

        public Icon getIcon() {
            return icon;
        }
    }

    private static final class LogRecordDecorator implements InvocationHandler {

        private static final String DECORATIONS_CLASS = "org.netbeans.lib.uihandler.Decorations";   // NOI18N
        private static final String DECORABLE_CLASS = "org.netbeans.lib.uihandler.Decorable";       // NOI18N
        private static final String DECORATE_METHOD = "decorate";                                   // NOI18N
        private static final String DECORABLE_SETNAME_METHOD = "setName";                           // NOI18N
        private static final String DECORABLE_SETDISPLAYNAME_METHOD = "setDisplayName";             // NOI18N
        private static final String DECORABLE_SETICONBASE_METHOD = "setIconBaseWithExtension";      // NOI18N
        private static final String DECORABLE_SETSHORTDESCRIPTOR_METHOD = "setShortDescription";    // NOI18N
        private LogRecordInfo recInfo;
        private LogRecord rec;

        LogRecordDecorator(LogRecordInfo info) {
            recInfo = info;
            rec = info.record;
        }

        private void decorateRecord() {
            try {
                ClassLoader c = Lookup.getDefault().lookup(ClassLoader.class);
                Class decorationClass = Class.forName(DECORATIONS_CLASS, true, c);
                Class decorableClass = Class.forName(DECORABLE_CLASS, true, c);
                Object decorable = Proxy.newProxyInstance(c, new Class[]{decorableClass}, this);
                Method decorate = decorationClass.getDeclaredMethod(DECORATE_METHOD, LogRecord.class, decorableClass);
                decorate.setAccessible(true);
                decorate.invoke(null, rec, decorable);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if (DECORABLE_SETNAME_METHOD.equals(methodName)) {
                recInfo.setName((String) args[0]);
            }
            if (DECORABLE_SETDISPLAYNAME_METHOD.equals(methodName)) {
                recInfo.setDisplayName((String) args[0]);
            }
            if (DECORABLE_SETSHORTDESCRIPTOR_METHOD.equals(methodName)) {
                recInfo.setToolTip((String) args[0]);
            }
            if (DECORABLE_SETICONBASE_METHOD.equals(methodName)) {
                String iconBase = (String) args[0];
                recInfo.setIcon(ImageUtilities.loadImageIcon(iconBase, true));
            }
            return null;
        }

        static void decorate(LogRecordInfo info) {
            new LogRecordDecorator(info).decorateRecord();
        }
    }
}

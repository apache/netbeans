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
package org.netbeans.modules.cnd.repository.testbench;

import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 */
public final class Stats {

    private Stats() {
    }
    public static final int debugPut = 0;
    public static final int debugGot = 0;
    public static final int debugReadFromFile = 0;
    public static final int debugNotFound = 0;
    public static final int debugGotFromHardCache = 0;
    public static final int nullDataTriggered = 0;
    public static final boolean monitorRemovedKeys = CndUtils.getBoolean("cnd.repository.monitor.removed.keys", false); //NOI18N
    public static final boolean isDebug = CndUtils.getBoolean("cnd.repository.use.dev", false); //NOI18N
    public static final boolean verbosePut = CndUtils.getBoolean("cnd.repository.verbose.put", false); //NOI18N
    public static final boolean validatePut = CndUtils.getBoolean("cnd.repository.validate.put", false); //NOI18N
    public static final boolean rememberKeys = CndUtils.getBoolean("cnd.repository.remember.keys", false); //NOI18N
    public static final boolean useNullWorkaround = CndUtils.getBoolean("cnd.repository.workaround.nulldata", false); //NOI18N
    public static final boolean queueTiming = CndUtils.getBoolean("cnd.repository.queue.timing", false); //NOI18N
    public static final boolean queueTrace = CndUtils.getBoolean("cnd.repository.queue.trace", false); //NOI18N
    public static final boolean queueUseTicking = CndUtils.getBoolean("cnd.repository.queue.ticking", true); //NOI18N
    public static final int fileStatisticsLevel = getInteger("cnd.repository.file.stat", 0); //NOI18N
    public static final int fileStatisticsRanges = getInteger("cnd.repository.file.stat.ranges", 10); //NOI18N
    public static final boolean writeStatistics = CndUtils.getBoolean("cnd.repository.write.stat", false); //NOI18N
    public static final boolean multyFileStatistics = CndUtils.getBoolean("cnd.repository.mf.stat", false); //NOI18N
    public static final boolean memoryCacheHitStatistics = CndUtils.getBoolean("cnd.repository.mem.cache.stat", false); //NOI18N
    public static final boolean dumoFileOnExit = CndUtils.getBoolean("cnd.repository.dump.on.exit", false); //NOI18N
    public static final int maintenanceInterval = getInteger("cnd.repository.queue.maintenance", 500); //NOI18N
    public static final boolean allowMaintenance = CndUtils.getBoolean("cnd.repository.defragm", true); //NOI18N
    public static final int fileRWAccess = getInteger("cnd.repository.rw", 0); //NOI18N
    public static final int bufSize = getInteger("cnd.repository.bufsize", -1); //NOI18N
    public static final boolean useCompactIndex = CndUtils.getBoolean("cnd.repository.compact.index", true); //NOI18N
    public static final String traceKeyName = System.getProperty("cnd.repository.trace.key"); //NOI18N
    public static final boolean traceKey = (traceKeyName != null); //NOI18N
    public static final boolean traceDefragmentation = CndUtils.getBoolean("cnd.repository.trace.defragm", false); //NOI18N
    public static final int defragmentationThreashold = getInteger("cnd.repository.defragm.threshold", 50); //NOI18N
    public final static String ENCODING = System.getProperty("file.encoding"); // NOI18N
    public final static boolean TRACE_REPOSITORY_TRANSLATOR = CndUtils.getBoolean("cnd.repository.trace.translator", false); //NOI18N
    public final static boolean TRACE_UNIT_DELETION = CndUtils.getBoolean("cnd.repository.trace.unit.deletion", false); //I18N
    public static final boolean TRACE_IZ_215449 = CndUtils.getBoolean("trace.iz.215449", true); //I18N
    public static final boolean TRACE_IZ_224249 = CndUtils.getBoolean("trace.iz.224249", false); //I18N

    public static boolean isTraceKey(LayerKey key) {
        if (traceKey) {
            if (key != null) {
                for (int i = 0; i < key.getDepth(); i++) {
                    if (traceKeyName.equals(key.getAt(i))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static int getInteger(String name, int result) {
        String text = System.getProperty(name);
        if (text != null) {
            result = Integer.parseInt(text);
        }
        return result;
    }

    public static void report(String st) {
        log(
                "Put: " + debugPut + //NOI18N
                "; Got: " + debugGot + //NOI18N
                "; Read: " + debugReadFromFile + //NOI18N
                "; N/A: " + debugNotFound //NOI18N
                + "; Hard: " + debugGotFromHardCache //NOI18N
                + st);
    }

    public static void report() {
        report("");
    }

    public static void report(int hard, int soft) {
        report("; in Hard cache: " + hard + "; in Soft cache <" + soft); // NOI18N
    }

    public static void log(String st) {
        if (useNullWorkaround) {
            st += "; NULL: " + nullDataTriggered; // NOI18N
        }
        if (isDebug) {
            System.err.println("DEBUG [Repository] " + st); // NOI18N
        }
    }
}

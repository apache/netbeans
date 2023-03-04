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

package org.netbeans.modules.uihandler;

import java.util.List;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author Marek Slama
 *
 */
public class MetricsHandler extends Handler {
    private static Task lastRecord = Task.EMPTY;
    private static RequestProcessor FLUSH = new RequestProcessor("Flush Metrics Logs"); // NOI18N
    private static boolean flushOnRecord;
    static final int MAX_LOGS = 400;
    /** Maximum number of days, after which we send the report. 40 days ± one week.*/
    static final int MAX_DAYS = 33 + new Random(System.currentTimeMillis()).nextInt(14);
    /** Maximum allowed size of backup log file 10MB */
    static final long MAX_LOGS_SIZE = 10L * 1024L * 1024L;
    
    public MetricsHandler() {
        setLevel(Level.FINEST);
    }

    @Override
    public void publish(LogRecord record) {

        class WriteOut implements Runnable {
            public LogRecord r;
            @Override
            public void run() {
                Installer.writeOutMetrics(r);
                r = null;
            }
        }
        WriteOut wo = new WriteOut();
        wo.r = record;
        lastRecord = FLUSH.post(wo);

        if (flushOnRecord) {
            waitFlushed();
        }
    }

    @Override
    public void flush() {
        waitFlushed();
    }
    
    static void flushImmediatelly() {
        flushOnRecord = true;
    }

    static void setFlushOnRecord (boolean flushOnRecord) {
        MetricsHandler.flushOnRecord = flushOnRecord;
    }
    
    static void waitFlushed() {
        try {
            lastRecord.waitFinished(1000);
        } catch (InterruptedException ex) {
            Installer.LOG.log(Level.FINE, null, ex);
        }
    }

    @Override
    public void close() throws SecurityException {
    }

    void publishEarlyRecords(List<LogRecord> earlyRecords) {
        for (LogRecord r : earlyRecords) {
            publish(r);
        }
    }
    
}

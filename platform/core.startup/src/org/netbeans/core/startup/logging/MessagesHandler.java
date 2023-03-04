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
package org.netbeans.core.startup.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class MessagesHandler extends StreamHandler {
    
    /** Do not flush repeat message when the count exceeds this number. */
    static final int MAX_REPEAT_COUNT_FLUSH = 10;
    
    private final File dir;
    private final File[] files;
    private final long limit;
    private final boolean filterRepeats;
    private LogRecord lastLogRecord;
    private LogRecord repeatingRecord;
    private long repeatCounter;
    private long lastRecordAllRepeatsCounter = 1;
    
    MessagesHandler(File dir) {
        this(dir, -1, 1024 * 1024);
    }
    
    MessagesHandler(File dir, int count, long limit) {
        this.dir = dir;
    
        if (count == -1) {
            count = Integer.getInteger("org.netbeans.log.numberOfFiles", 3); // NOI18N
            if (count < 3) {
                count = 3;
            }
        }
        File[] arr = new File[count];
        arr[0] = new File(dir, "messages.log"); // NOI18N
        for (int i = 1; i < arr.length; i++) {
            arr[i] = new File(dir, "messages.log." + i); // NOI18N
        }
        this.files = arr;
        this.limit = limit;
        this.filterRepeats = !Boolean.getBoolean("org.netbeans.log.disableRepeatingMessagesFilter"); // NOI18N
        setFormatter(NbFormatter.FORMATTER);
        setLevel(Level.ALL);
        
        checkRotate(true);
        initStream();
    }
    
    private boolean checkRotate(boolean always) {
        if (!always && files[0].length() < limit) {
            return false;
        }
        flush();
        doRotate();
        return true;
    }
    
    private void initStream() {
        try {
            setOutputStream(new FileOutputStream(files[0], false));
        } catch (FileNotFoundException ex) {
            setOutputStream(System.err);
        }
    }

    @Override
    public synchronized void publish(LogRecord record) {
        if (filterRepeats) {
            if (lastLogRecord != null) {
                if (compareRepeating(lastLogRecord, record)) {
                    repeatCounter++;
                    lastRecordAllRepeatsCounter++;
                    LogRecord rr = createRepeatingRecord(record, repeatCounter);
                    if (rr != null) {
                        repeatingRecord = rr;
                    }
                    return ;
                } else if (repeatCounter > 0 || lastRecordAllRepeatsCounter > 1) {
                    if (lastRecordAllRepeatsCounter > MAX_REPEAT_COUNT_FLUSH) {
                        repeatingRecord = createAllRepeatsRecord(lastLogRecord, lastRecordAllRepeatsCounter);
                    }
                    flushRepeatCounter();
                    lastRecordAllRepeatsCounter = 1;
                }
            }
            lastLogRecord = record;
        }
        super.publish(record);
        if (checkRotate(false)) {
            initStream();
        }
    }

    @Override
    public synchronized void flush() {
        flushRepeatCounter();
        super.flush();
    }
    
    private synchronized void doRotate() {
        close();
        int n = files.length;
        if (files[n - 1].exists()) {
            files[n - 1].delete();
        }
        for (int i = n - 2; i >= 0; i--) {
            if (files[i].exists()) {
                files[i].renameTo(files[i + 1]);
            }
        }
    }

    private synchronized void flushRepeatCounter() {
        if (repeatingRecord != null) {
            super.publish(repeatingRecord);
            repeatingRecord = null;
        }
        repeatCounter = 0;
    }

    private boolean compareRepeating(LogRecord r1, LogRecord r2) {
        return r1.getLevel().equals(r2.getLevel()) &&
               Objects.equals(r1.getLoggerName(), r2.getLoggerName()) &&
               Objects.equals(r1.getMessage(), r2.getMessage()) &&
               Objects.deepEquals(r1.getParameters(), r2.getParameters()) &&
               Objects.equals(r1.getResourceBundle(), r2.getResourceBundle()) &&
               Objects.equals(r1.getResourceBundleName(), r2.getResourceBundleName()) &&
               Objects.equals(r1.getSourceClassName(), r2.getSourceClassName()) &&
               Objects.equals(r1.getSourceMethodName(), r2.getSourceMethodName()) &&
               //r1.getThreadID() == r2.getThreadID() &&
               compareThrown(r1.getThrown(), r2.getThrown());
    }

    private boolean compareThrown(Throwable t1, Throwable t2) {
        if (t1 == null) {
            return t2 == null;
        }
        if (t2 == null) {
            return false;
        }
        return t1.getClass().equals(t2.getClass()) &&
               Objects.equals(t1.getMessage(), t2.getMessage()) &&
               Objects.equals(t1.getLocalizedMessage(), t2.getLocalizedMessage()) &&
               Arrays.deepEquals(t1.getStackTrace(), t2.getStackTrace()) &&
               compareThrown(t1.getCause(), t2.getCause());
    }

    private LogRecord createRepeatingRecord(LogRecord r, long rc) {
        if (lastRecordAllRepeatsCounter <= (MAX_REPEAT_COUNT_FLUSH+1)) {
            return new LogRecord(r.getLevel(), getRepeatingMessage(rc, lastRecordAllRepeatsCounter));
        } else {
            return null;
        }
    }

    private LogRecord createAllRepeatsRecord(LogRecord r, long allRc) {
        return new LogRecord(r.getLevel(), getAllRepeatsMessage(allRc));
    }

    static String getRepeatingMessage(long rc, long allRc) {
        String msg;
        if (allRc > MAX_REPEAT_COUNT_FLUSH) {
            msg = "Last record repeated more than "+MAX_REPEAT_COUNT_FLUSH+" times, "+
                  "further logs of this record are ignored until the log record changes.";
        } else {
            if (rc == 1) {
                msg = "Last record repeated again.";
            } else {
                msg = "Last record repeated "+rc+" more times.";
            }
        }
        return msg;
    }

    static String getAllRepeatsMessage(long allRc) {
        return "Last record repeated "+allRc+" times in total.";
    }
}

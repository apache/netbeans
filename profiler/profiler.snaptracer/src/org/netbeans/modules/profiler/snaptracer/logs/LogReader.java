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

package org.netbeans.modules.profiler.snaptracer.logs;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;

/** Reads log records from file.
 *
 * @author Tomas Hurka
 */
public final class LogReader {
 
    private static final Logger LOG = Logger.getLogger(LogRecords.class.getName());

    private FileObject logFile;
//    private int records;
//    private long startTime;
    private NavigableMap<Long,LogRecord> recordList;

    public LogReader(FileObject f) {
        logFile = f;
        recordList = new TreeMap();
    }


    public void load() throws IOException {
        InputStream is = new BufferedInputStream(logFile.getInputStream(),32768);
        try {
            LogRecords.scan(is, new LogHandler());
        } finally {
            is.close();
        }
    }

    public LogRecord getRecordFor(long time) {
        Map.Entry<Long,LogRecord> entry = recordList.floorEntry(time);
        
        if (entry != null) {
            return entry.getValue();
        }
        return null;
    }
    
    class LogHandler extends Handler {

        @Override
        public void publish(LogRecord record) {
//            System.out.println("Record "+ records++);
//            if (startTime == 0) {
//                startTime = record.getMillis();
//                System.out.println("Start date: "+new Date(startTime));
//            } else {
//                System.out.println("Time: "+(record.getMillis()-startTime));
//            }
//            System.out.println(record.getMessage());
            recordList.put(record.getMillis(), record);
        }

        @Override
        public void flush() {
//            System.out.println("Flush");
        }

        @Override
        public void close() throws SecurityException {
//           System.out.println("Close");
        }

    }
}

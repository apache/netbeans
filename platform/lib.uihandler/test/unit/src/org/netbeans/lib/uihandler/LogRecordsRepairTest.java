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
package org.netbeans.lib.uihandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Martin Entlicher
 */
public class LogRecordsRepairTest extends NbTestCase {
    
    public LogRecordsRepairTest(String testName) {
        super(testName);
    }
    
    public void testScanWithRepair1() throws Exception {
        File f = createTempFileFrom("File651659.xml");
        RecordsHandler rh = new RecordsHandler();
        LogRecords.scan(f, rh);
        List<LogRecord> records = rh.getRecords();
        assertEquals("One record should be read", 1, records.size());
        assertEquals("Wrong record message.", "CPU INFO", records.get(0).getMessage());
    }
    
    public void testScanWithRepair2() throws Exception {
        File f = createTempFileFrom("File664736.xml");
        RecordsHandler rh = new RecordsHandler();
        LogRecords.scan(f, rh);
        List<LogRecord> records = rh.getRecords();
        assertEquals("Records count", 206, records.size());
        assertEquals("Sequence number of the first record", 165, records.get(0).getSequenceNumber());
    }
    
    public void testScanTruncated() throws Exception {
        File f = createTempFileFrom("FileTruncated.xml");
        RecordsHandler rh = new RecordsHandler();
        LogRecords.scan(f, rh);
        List<LogRecord> records = rh.getRecords();
        assertEquals("Records count", 2, records.size());
        assertEquals("Sequence number of the last record", 965, records.get(1).getSequenceNumber());
    }
    
    private File createTempFileFrom(String resource) throws IOException {
        InputStream is = getClass().getResourceAsStream("resources/"+resource);
        File f = File.createTempFile("LR_", resource);
        f.deleteOnExit();
        OutputStream out = new FileOutputStream(f);
        try {
            byte[] buffer = new byte[1024];
            int l;
            while((l = is.read(buffer)) > 0) {
                out.write(buffer, 0, l);
            }
        } finally {
            is.close();
            out.close();
        }
        return f;
    }
    
    private static class RecordsHandler extends Handler {
        
        private List<LogRecord> records = new ArrayList<LogRecord>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {
        }
        
        public List<LogRecord> getRecords() {
            return records;
        }
        
    }
    
}

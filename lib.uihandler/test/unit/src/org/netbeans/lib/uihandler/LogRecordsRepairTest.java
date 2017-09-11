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

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

package org.netbeans.lib.uihandlerserver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.uihandler.LogRecords;
import org.netbeans.lib.uihandler.TestHandler;

/**
 *
 * @author Jaroslav Tulach
 */
public class ReadBigDataTest extends NbTestCase {
    private Logger LOG;

    public ReadBigDataTest(String testName) {
        super(testName);
    }

    protected Level logLevel() {
        return Level.FINEST;
    }

    protected void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
    }

    protected void tearDown() throws Exception {
    }

    public void testAntonsOutOfMemExc() throws Exception {
        String what = "antons.gz";

        InputStream is = new GZIPInputStream(getClass().getResourceAsStream(what));

        class H extends Handler {
            int cnt;
            LogRecord first;

            public void publish(LogRecord record) {
                if (cnt == 0) {
                    first = record;
                }
                cnt++;
                if (record.getParameters() != null && record.getParameters().length > 1500) {
                    fail("Too many parameters: " + record.getParameters().length);
                }
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }

        H h = new H();
        is = new GZIPInputStream(getClass().getResourceAsStream(what));
        LogRecords.scan(is, h);
        is.close();

        if (h.cnt != 322) {
            fail("Invalid number of records: " + h.cnt);
        }
    }

    public void testWriteAndRead() throws Exception {
        File dir = new File(new File(System.getProperty("user.dir")), "ui");
        if (!dir.exists()) {
            return;
        }

        File[] arr = dir.listFiles();
        if (arr == null) {
            return;
        }

        int[] cnts = new int[arr.length];
        int err1 = readAsAStream(cnts, arr, 0);
        int err2 = readAsSAX(cnts, 0, arr);

        assertEquals("No errors: " + err1 + " and no " + err2, 0, err1 + err2);
    }

    private int readAsSAX(final int[] cnts, int err, final File[] arr) throws IOException, FileNotFoundException {
        class H extends Handler {
            int cnt;

            public void publish(LogRecord record) {
                cnt++;
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }
        
        int i = -1;
        for (File f : arr) {
            LOG.log(Level.WARNING, "scanning {0}", f.getPath());
            i++;
            InputStream is = new BufferedInputStream(new FileInputStream(f));
            H h = new H();
            try {
                LogRecords.scan(is, h);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
                err++;
                continue;
            } finally {
                is.close();
            }
            
            assertEquals("The same amount for " + f, h.cnt, cnts[i]);
        }
        return err;
    }

    private int readAsAStream(final int[] cnts, final File[] arr, int err) throws IOException, FileNotFoundException {
        int i = -1;
        for (File f : arr) {
            LOG.log(Level.WARNING, "reading {0}", f.getPath());
            i++;
            InputStream is = new BufferedInputStream(new FileInputStream(f));
            int cnt = 0;
            TestHandler records = new TestHandler(is);
            try {
                while (records.read() != null) {
                    cnt++;
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, null, ex);
                err++;
                continue;
            } finally {
                cnts[i] = cnt;
                is.close();
            }
            is.close();
        }
        return err;
    }
}

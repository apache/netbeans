/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2006 Sun Microsystems, Inc.
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

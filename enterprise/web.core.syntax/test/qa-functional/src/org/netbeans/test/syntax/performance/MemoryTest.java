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
package org.netbeans.test.syntax.performance;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.text.StyledDocument;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditorCookie.Observable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jindrich Sedek
 */
public class MemoryTest extends NbTestCase {

    private static final int ITERATIONS_COUNT = 10;
    private static final int TIMEOUT = 2000;
    private TestHandler handler;

    public MemoryTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Logger logger = Logger.getLogger("TIMER.j2ee.parser");
        logger.setLevel(Level.FINEST);
        handler = new TestHandler();
        logger.addHandler(handler);
    }

    public static Test suite() {
        return NbModuleSuite.allModules(MemoryTest.class);
    }

    public void testHTML() throws Exception {
        PerformanceTest.openNavigator();
        handler.setProcessedMessage("HTML parse result");
        processTest("performance.html", "<table></table>\n");
    }

    public void testCSSInHTML() throws Exception {
        PerformanceTest.openNavigator();
        handler.setProcessedMessage("CSS parse result");
        processTest("performance.html", "<style>h1{color:green}</style>\n");
    }

    public void testCSS() throws Exception {
        PerformanceTest.openNavigator();
        handler.setProcessedMessage("CSS parse result");
        processTest("performance.css", "selector{color:green}\n");
    }

    private void processTest(String testName, String insertionText) throws Exception {
        File testFile = new File(getDataDir(), testName);
        FileObject testObject = FileUtil.createData(testFile);
        DataObject dataObj = DataObject.find(testObject);
        EditorCookie.Observable ed = dataObj.getCookie(Observable.class);
        handler.params.clear();
        handler.latest = null;
        StyledDocument doc = ed.openDocument();
        ed.open();
        Thread.sleep(TIMEOUT);
        handler.params.clear();

        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            doc.insertString(0, insertionText, null);
            Thread.sleep(TIMEOUT);
        }
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            doc.remove(0, insertionText.length());
            Thread.sleep(TIMEOUT);
        }

        ed.saveDocument();
        ed.close();

        assertClean();
    }

    private void assertClean() {
        ArrayList<WeakReference> refs = new ArrayList<WeakReference>(handler.params);
        Iterator<WeakReference> it = refs.iterator();
        while (it.hasNext()) {
            if (isFree(it.next())) {
                it.remove();
            }
        }
        int size = refs.size();
        if (size > 3) {
            System.err.println("there are " + size + " pending references to parse results");
            assertGC("there are " + size + " parse results still accessible", refs.get(2));
        }
    }

    private boolean isFree(Reference<?> ref) {
        List<byte[]> alloc = new ArrayList<byte[]>();
        int size = 100000;
        for (int i = 0; i < 50; i++) {
            if (ref.get() == null) {
                return true;
            }
            try {
                System.gc();
            } catch (OutOfMemoryError error) {
                // OK
            }
            try {
                System.runFinalization();
            } catch (OutOfMemoryError error) {
                // OK
            }
            try {
                alloc.add(new byte[size]);
                size = (int) (((double) size) * 1.3);
            } catch (OutOfMemoryError error) {
                size = size / 2;
            }
            try {
                if (i % 3 == 0) {
                    Thread.sleep(321);
                }
            } catch (InterruptedException t) {
                // ignore
            }
        }
        return false;
    }

    private class TestHandler extends Handler {

        String message;
        List<WeakReference> params = new LinkedList<WeakReference>();
        WeakReference latest;

        @Override
        public void publish(LogRecord record) {
            if ((message != null) && (!record.getMessage().equals(message))) {
                return;
            }
            if (latest != null) {
                params.add(latest);
            }
            Object[] pars = record.getParameters();
            WeakReference<Object> ref = new WeakReference<Object>(pars[0]);
            latest = ref;
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        private void setProcessedMessage(String message) {
            this.message = message;
        }
    }
}

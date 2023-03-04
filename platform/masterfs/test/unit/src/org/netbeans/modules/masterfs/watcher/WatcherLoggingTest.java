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
package org.netbeans.modules.masterfs.watcher;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.providers.Notifier;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author jhavlin
 */
public class WatcherLoggingTest extends NbTestCase {

    private WatcherLoggingTest.FailingNotifier notify;
    private WatcherLoggingTest.L listener;

    public WatcherLoggingTest(String s) {
        super(s);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockServices.setServices(WatcherLoggingTest.FailingNotifier.class);
        listener = new WatcherLoggingTest.L();
        notify = Lookup.getDefault().lookup(
                WatcherLoggingTest.FailingNotifier.class);
        notify.start();
    }

    public void testLimitAddWatchLogs() throws IOException {
        FileObject root = FileUtil.toFileObject(getWorkDir());
        int total = 100;

        Logger log = Logger.getLogger(Watcher.class.getName());
        Log.enable(log.getName(), Level.FINEST);
        LogCountHandler h = new LogCountHandler();

        log.addHandler(h);
        try {
            for (int i = 0; i < total; i++) {
                root.addFileChangeListener(listener);
            }
        } finally {
            log.removeHandler(h);
        }

        assertEquals("2 warnings", 2, h.warning);
        assertEquals("10 infos", 10, h.info);
        assertEquals("The rest messages are of fine level", 88, h.fine);
    }

    private static final class L extends FileChangeAdapter {
    }

    private static class LogCountHandler extends Handler {

        public int warning = 0;
        public int info = 0;
        public int fine = 0;
        public int other = 0;

        @Override
        public void publish(LogRecord record) {
            if ("Cannot add filesystem watch for {0}: {1}"
                    .equals(record.getMessage())) {
                Level f = record.getLevel();
                if (Level.WARNING.equals(f)) {
                    warning++;
                } else if (Level.INFO.equals(f)) {
                    info++;
                } else if (Level.FINE.equals(f)) {
                    fine++;
                } else {
                    other++;
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    public static final class FailingNotifier extends Notifier {

        @Override
        protected Object addWatch(String path) throws IOException {
            throw new IOException("FailingNotifier-add");
        }

        @Override
        protected void removeWatch(Object key) throws IOException {
            throw new IOException("FailingNotifier-remove");
        }

        @Override
        protected String nextEvent() throws IOException, InterruptedException {
            synchronized (this) {
                this.wait();
            }
            return null;
        }

        @Override
        protected void start() throws IOException {
        }
    }
}

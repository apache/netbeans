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

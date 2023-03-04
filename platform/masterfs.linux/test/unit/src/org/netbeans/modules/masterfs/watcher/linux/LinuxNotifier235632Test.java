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
package org.netbeans.modules.masterfs.watcher.linux;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Test for bug 235632.
 *
 * @author jhavlin
 */
public class LinuxNotifier235632Test extends NbTestCase {

    private File folder1;
    private File folder1text1Txt;
    private FileObject folder2FO;
    private FileObject folder2text2TxtFO;

    public LinuxNotifier235632Test(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        Log.enable(FolderObj.class.getName(), Level.FINEST);
        clearWorkDir();
    }

    @Override
    public boolean canRun() {
        return super.canRun() && Utilities.getOperatingSystem() == Utilities.OS_LINUX;
    }

    /**
     * Prepare folders and files for testing.
     *
     * <pre>
     * - folder1          (only java.io.File)
     *    - text1.txt     (only java.io.File)
     * - folder2          (FileObject)
     *    - text2.txt     (FileObject, with registered listener)
     * </pre>
     *
     * @throws IOException
     */
    private void prepareFiles() throws IOException {
        folder1 = new File(getWorkDir(), "folder1");
        folder1.mkdir();
        folder1text1Txt = new File(folder1, "text1.txt");
        folder1text1Txt.createNewFile();

        File folder2 = new File(getWorkDir(), "folder2");
        folder2.mkdir();
        folder2FO = FileUtil.toFileObject(folder2);
        folder2text2TxtFO = folder2FO.createData("text2.txt");
        // Causes the path to be registered in the notifier.
        folder2text2TxtFO.addFileChangeListener(new FileChangeAdapter());
        folder2FO.refresh();
    }

    /**
     * Test of nextEvent method, of class LinuxNotifier.
     *
     * @throws java.lang.Exception
     */
    public void testNextEvent() throws Exception {

        prepareFiles();

        final AtomicBoolean folder2refreshed = new AtomicBoolean(false);
        Logger log = Logger.getLogger(FolderObj.class.getName());

        Handler h = createHandler(folder2refreshed);
        log.addHandler(h);
        try {
            FileChangeListener l = new FileChangeAdapter();
            FileUtil.addFileChangeListener(l, folder1text1Txt);
            // This causes an IN_IGNORED native event.
            FileUtil.removeFileChangeListener(l, folder1text1Txt);
            // Native listeners may need some time.
            Thread.sleep(2000);
        } finally {
            log.removeHandler(h);
        }
        assertFalse("Folder folder2 should not be refreshed.",
                folder2refreshed.get());
    }

    /**
     * Create a logging handler that sets value in an AtomicBoolean to true if
     * folder2 or text2.txt is refreshed.
     *
     * @param refreshedFlag The AtomicBoolean to be set to true if incorrect
     * refreshing was triggered.
     * @return The new logging handler.
     */
    private Handler createHandler(final AtomicBoolean refreshedFlag) {
        Handler h = new Handler() {

            @Override
            public void publish(LogRecord record) {
                if (record.getMessage() != null
                        && record.getMessage().startsWith("refreshImpl for ")
                        && record.getParameters() != null
                        && record.getParameters().length > 0
                        && (record.getParameters()[0] == folder2FO
                        || record.getParameters()[0] == folder2text2TxtFO)) {
                    refreshedFlag.set(true);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };
        return h;
    }
}

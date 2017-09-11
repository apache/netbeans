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

        if (Utilities.getOperatingSystem() != Utilities.OS_LINUX) {
            System.out.println("Skipping linux-only test: " + getName());
            return;
        }

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

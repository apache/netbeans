/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mercurial;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author ondra
 */
public class HgCommandTest extends AbstractHgTestCase {

    public HgCommandTest(String arg0) throws IOException {
        super(arg0);
        System.setProperty("netbeans.user", new File(getWorkDir().getParentFile(), "userdir").getAbsolutePath());
    }

    @Override
    protected void setUp () throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {
            MercurialVCS.class});
    }
    
    public void testNumericTagname () throws Exception {
        commit(getWorkTreeDir());
        // this should demonstrate the correct behavior
        String tagName = "tag";
        // create tag
        HgCommand.createTag(getWorkTreeDir(), tagName, "tag message", null, true, NULL_LOGGER);
        HgLogMessage[] logs = HgCommand.getLogMessages(getWorkTreeDir(), null, tagName, tagName, 
                true, false, true, 1, Collections.<String>emptyList(), NULL_LOGGER, true);
        // hg log should return the tagged revision info
        assertEquals(1, logs.length);
        
        // now a numeric tagname
        String last = HgCommand.getLastRevision(getWorkTreeDir(), null);
        tagName = Integer.toString(Integer.parseInt(last) + 1);
        // create tag
        HgCommand.createTag(getWorkTreeDir(), tagName, "tag message", null, true, NULL_LOGGER);
        logs = HgCommand.getLogMessages(getWorkTreeDir(), null, tagName, tagName, 
                true, false, true, 1, Collections.<String>emptyList(), NULL_LOGGER, true);
        // hg log should return the tagged revision info
        assertEquals(1, logs.length);
    }
    
    public void testToGreaterThanLast () throws Exception {
        commit(getWorkTreeDir());
        String last = HgCommand.getLastRevision(getWorkTreeDir(), null);
        String to = Integer.toString(Integer.parseInt(last) + 100);
        HgLogMessage[] logs = HgCommand.getLogMessages(getWorkTreeDir(), null, last, to, 
                true, false, true, 1, Collections.<String>emptyList(), NULL_LOGGER, true);
        // hg log should return the tagged revision info
        assertEquals(1, logs.length);
    }

    public void testDisabledIndexing () throws Exception {
        CommandHandler handler = new CommandHandler();
        Mercurial.LOG.addHandler(handler);
        Mercurial.LOG.setLevel(Level.ALL);
        final File newRepo = new File(getTempDir(), "repo");
        List<File> repoAsList = Collections.singletonList(newRepo);
        handler.reset(repoAsList);
        commit(getWorkTreeDir());
        HgUtils.runWithoutIndexing(new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                return null;
            }
        }, newRepo);
        handler.assertResults(1);
        
        handler.reset();
        handler.reset(Collections.<File>emptyList());
        HgUtils.runWithoutIndexing(new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                return null;
            }
        });
        handler.assertResults(1);
        
        handler.reset();
        handler.reset(Arrays.asList(newRepo, newRepo, newRepo));
        HgUtils.runWithoutIndexing(new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                return null;
            }
        }, newRepo, newRepo, newRepo);
        handler.assertResults(1);
        
        // recursive call should not be a problem
        handler.reset();
        final File f = new File(newRepo, "aaa");
        handler.reset(Arrays.asList(f));
        HgUtils.runWithoutIndexing(new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                return HgUtils.runWithoutIndexing(new Callable<Void>() {
                    @Override
                    public Void call () throws Exception {
                        return null;
                    }
                }, f);
            }
        }, f);
        handler.assertResults(1);
        
        // recursive call does not permit different roots
        handler.reset();
        handler.reset(Arrays.asList(newRepo));
        HgUtils.runWithoutIndexing(new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                return HgUtils.runWithoutIndexing(new Callable<Void>() {
                    @Override
                    public Void call () throws Exception {
                        return null;
                    }
                }, f);
            }
        }, newRepo);
        handler.assertResults(1);
        
        HgUtils.runWithoutIndexing(new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                boolean error = false;
                try {
                    return HgUtils.runWithoutIndexing(new Callable<Void>() {
                        @Override
                        public Void call () throws Exception {
                            return null;
                        }
                    }, newRepo);
                } catch (AssertionError err) {
                    assertTrue(err.getMessage().startsWith("Recursive call does not permit different roots"));
                    error = true;
                }
                assertTrue(error);
                return null;
            }
        }, f);
        
    }

    public void testDisableIBInFSEvents () throws Exception {
        CommandHandler handler = new CommandHandler();
        Mercurial.LOG.addHandler(handler);
        Mercurial.LOG.setLevel(Level.ALL);
        File file = createFile(getWorkTreeDir(), "aaa");
        commit(file);

        FileObject fo = FileUtil.toFileObject(file);

        fo.delete();
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, HgCommand.getStatus(getWorkTreeDir(), Collections.<File>singletonList(file), null, null).get(file).getStatus());
        assertFalse(handler.commandInvoked);

        fo.getParent().createData(file.getName());
        assertNull(HgCommand.getStatus(getWorkTreeDir(), Collections.<File>singletonList(file), null, null).get(file));
        assertFalse(handler.commandInvoked);

        File copy = new File(file.getParentFile(), "copy");
        fo.copy(fo.getParent(), copy.getName(), "");
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, HgCommand.getStatus(getWorkTreeDir(), Collections.<File>singletonList(copy), null, null).get(copy).getStatus());
        assertFalse(handler.commandInvoked);

        File renamed = new File(file.getParentFile(), "renamed");
        FileLock lock = fo.lock();
        fo.move(lock, fo.getParent(), renamed.getName(), "");
        lock.releaseLock();
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, HgCommand.getStatus(getWorkTreeDir(), Collections.<File>singletonList(renamed), null, null).get(renamed).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, HgCommand.getStatus(getWorkTreeDir(), Collections.<File>singletonList(file), null, null).get(file).getStatus());
        assertFalse(handler.commandInvoked);
    }

    private class CommandHandler extends Handler {

        private int occurrences;
        private boolean commandInvoked;
        List files;
        List<File> expectedFiles;

        public void reset (List<File> expectedFiles) {
            reset();
            this.expectedFiles = expectedFiles;
        }

        public void reset () {
            occurrences = 0;
            commandInvoked = false;
        }

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().startsWith("Running block with disabled indexing:")) {
                ++occurrences;
                if (record.getParameters() != null && record.getParameters().length > 0) {
                    commandInvoked = true;
                    if (record.getParameters()[0] instanceof List) {
                        files = (List) record.getParameters()[0];
                    }
                }
            }
        }

        @Override
        public void flush() {
            //
        }

        @Override
        public void close() throws SecurityException {
            //
        }

        public void assertResults (int occurrences) {
            assertTrue(occurrences == this.occurrences);
            assertTrue(occurrences == 0 || this.commandInvoked);
            assertEquals(files, expectedFiles);
        }
    }

    private static File tmp = null;
    private File getTempDir() {
        if(tmp == null) {
            File tmpDir;
            try {
                tmpDir = getWorkDir();
                tmp = new File(tmpDir, "gtmt-" + Long.toString(System.currentTimeMillis()));
                tmp.deleteOnExit();
            } catch (IOException ex) {

            }
        }
        return tmp;
    }

    private class RefreshProbe {
        private final FileObject fo;
        private long lastModified;

        public RefreshProbe(FileObject fo) {
            this.fo = fo;
        }

        void reset () {
            lastModified = fo.lastModified().getTime();
        }

        void checkRefresh (boolean refreshAllowed) throws Exception {
            boolean refreshed = false;
            for (int i = 0; i < 5; ++i) {
                Thread.sleep(1000);
                if (fo.lastModified().getTime() > lastModified) {
                    refreshed = true;
                    break;
                }
            }
            assert refreshed == refreshAllowed;
        }
    }
}

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

package org.netbeans.modules.mercurial.api;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.netbeans.modules.mercurial.AbstractHgTestCase;
import org.netbeans.modules.mercurial.util.HgSearchHistorySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.test.MockLookup;

/**
 *
 * @author ondra
 */
public class SearchHistoryTestCase extends AbstractHgTestCase {

    private File dataRootDir;
    private File wc;
    private File repoDir;

    public SearchHistoryTestCase(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
       super.setUp();
        MockLookup.setLayersAndInstances();
        // create
        FileObject fo = FileUtil.toFileObject(getWorkTreeDir());
        wc = getWorkTreeDir();
        System.setProperty("netbeans.user", getWorkDir() + "/cache");
        new File(getWorkDir(), "cache").createNewFile();
    }

    public void testBoundariesShowFileHistory () throws Throwable {
        boolean showing;
        File f;

        // non-existant file
        f = new File("/tmp/testShowFileHistory.file");
        
        // folder
        showing = HgSearchHistorySupport.getInstance(f.getParentFile()).searchHistory(1);
        assertFalse(showing);

        // unversioned file
        f.createNewFile();
        showing = HgSearchHistorySupport.getInstance(f).searchHistory(1);
        assertFalse(showing);

        // AWT
        final File file = f;
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    HgSearchHistorySupport.getInstance(file).searchHistory(1);
                    fail("AWT test failed");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (AssertionError err) {
                    
                }
            }
        });
    }

    public void testDiffView () throws Throwable {
        // create a file and initial commit
        File file = new File(getWorkTreeDir(), "file.txt");
        file.createNewFile();

        // chain of change & commit
        StringBuilder content = new StringBuilder();
        for (int i = 1; i < 10; ++i) {
            for (int j = 1; j < 20; ++j) {
                content.append("File change number ").append(i).append("_").append(j).append("\n");
            }
            write(file, content.toString());
            System.out.println("Commit nbr. " + i);
            commit(wc);
        }

        // local changes
        // changes every few lines
        int pos = content.indexOf("\n");
        while (pos != -1) {
            int nextPos = content.indexOf("\n", pos + 30);
            if (nextPos == -1) {
                pos = -1;
            } else {
                String replaceString = "Local change \nLocal change \nLocal change \n";
                content.replace(pos + 1, nextPos, replaceString);
                pos = nextPos + nextPos - pos + replaceString.length();
                // every 5 next lines
                for (int i = 0; i < 5 && pos != -1; ++i) {
                    pos = content.indexOf("\n", pos + 1);
                }
            }
        }
        write(file, content.toString());

        boolean showing = HgSearchHistorySupport.getInstance(file).searchHistory(100);
        assertTrue(showing);

        JDialog d = new JDialog((JFrame)null, "Close dialog");
        d.setModal(false);
        d.setVisible(true);
        while (d.isVisible()) {
            Thread.sleep(1000);
        }
    }

//    public void testDiffViewNotInRepository () throws Throwable {
//        // create a file and initial commit
//        File file = new File(wc, "file.txt");
//        file.createNewFile();
//
//        TestKit.add(file);
//        //TestKit.commit(file.getParentFile());
//
//        boolean showing = Subversion.showFileHistory(file.getAbsolutePath(), 100);
//        assertTrue(showing);
//
//        JDialog d = new JDialog((JFrame)null, "Close dialog");
//        d.setModal(false);
//        d.setVisible(true);
//        while (d.isVisible()) {
//            Thread.sleep(1000);
//        }
//    }

    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.diff.DiffProvider.class)
    public static class DummyBuiltInDiffProvider extends BuiltInDiffProvider {
        public DummyBuiltInDiffProvider() {
        }
    }
}

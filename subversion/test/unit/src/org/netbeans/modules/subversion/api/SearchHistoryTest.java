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

package org.netbeans.modules.subversion.api;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.netbeans.modules.subversion.SubversionVCS;
import org.netbeans.modules.subversion.TestKit;
import org.netbeans.modules.subversion.util.SvnSearchHistorySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author ondra
 */
public class SearchHistoryTest extends NbTestCase {

    private File dataRootDir;
    private SVNUrl repoUrl;
    private File wc;
    private File repoDir;

    public SearchHistoryTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockServices.setServices(new Class[] {
            SubversionVCS.class});
        dataRootDir = getWorkDir();
        wc = new File(dataRootDir, getName() + "_wc");
        repoDir = new File(dataRootDir, "repo");
        String repoPath = repoDir.getAbsolutePath();
        if(repoPath.startsWith("/")) repoPath = repoPath.substring(1, repoPath.length());
        repoUrl = new SVNUrl("file:///" + repoPath);

        System.setProperty("netbeans.user", System.getProperty("data.root.dir") + "/cache");
        cleanUpWC();
        initRepo();
        wc.mkdirs();
        FileUtil.createFolder(new File(getWorkDir(), "cache/config/svn"));
        svnimport();
    }

    public void testBoundariesShowFileHistory () throws Throwable {
        boolean showing;
        File f;

        // non-existant file
        f = new File(wc, "testShowFileHistory.file");
        
        // folder
        showing = SvnSearchHistorySupport.getInstance(f.getParentFile()).searchHistory(1);
        assertFalse(showing);

        // unversioned file
        f.createNewFile();
        showing = SvnSearchHistorySupport.getInstance(f).searchHistory(1);
        assertTrue(showing);

        // AWT
        final File file = f;
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    SvnSearchHistorySupport.getInstance(file).searchHistory(1);
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
        File file = new File(wc, "file.txt");
        file.createNewFile();

        // chain of change & commit
        StringBuilder content = new StringBuilder();
        for (int i = 1; i < 10; ++i) {
            for (int j = 1; j < 20; ++j) {
                content.append("File change number ").append(i).append("_").append(j).append("\n");
            }
            TestKit.write(file, content.toString());
            System.out.println("Commit nbr. " + i);
            TestKit.commit(wc);
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
        TestKit.write(file, content.toString());

        boolean showing = SvnSearchHistorySupport.getInstance(file).searchHistory(100);
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

    private void cleanUpWC() throws IOException {
        if(wc.exists()) {
            File[] files = wc.listFiles();
            if(files != null) {
                for (File file : files) {
                    if(!file.getName().equals("cache")) { // do not delete the cache
                        FileObject fo = FileUtil.toFileObject(file);
                        if (fo != null) {
                            fo.delete();
                        }
                    }
                }
            }
        }
    }

    private void initRepo() throws MalformedURLException, IOException, InterruptedException, SVNClientException {
        TestKit.initRepo(repoDir, wc);
    }

    private void svnimport() throws SVNClientException, MalformedURLException {
        TestKit.svnimport(repoDir, wc);
    } 
}

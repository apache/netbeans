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
package org.netbeans.modules.subversion.ui.diff;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.subversion.AbstractSvnTestCase;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.TestKit;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.Context;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Ondrej Vrabec
 */
public class RevisionSetupSupportTest extends AbstractSvnTestCase {

    private File workDir;
    private SVNUrl repoUrl;
    private File wc;
    private File repoDir;
    
    public RevisionSetupSupportTest (String name) throws Exception {
        super(name);
    }

    @Override
    protected void setUp () throws Exception {
        super.setUp();
        workDir = getWorkDir();
        clearWorkDir();
        FileUtil.refreshFor(workDir);
        wc = new File(workDir, getName() + "_wc");
        repoDir = new File(workDir, "repo");
        String repoPath = repoDir.getAbsolutePath();
        if(repoPath.startsWith("/")) repoPath = repoPath.substring(1, repoPath.length());
        repoUrl = new SVNUrl("file:///" + repoPath);

        System.setProperty("netbeans.user", getDataDir() + "/userdir");
        cleanUpWC(wc);
        TestKit.initRepo(repoDir, wc);
        TestKit.svnimport(repoDir, wc);
        
        wc.mkdirs();
    }
    
    public void testDiffSame () throws Exception {
        // init
        File folder = new File(wc, "folder");
        File file = new File(folder, "file");
        folder.mkdirs();
        file.createNewFile();
        
        add(folder);
        commit(folder);
        
        RepositoryFile left = new RepositoryFile(repoUrl, wc.getName() + "/folder", SVNRevision.HEAD);
        RepositoryFile right = new RepositoryFile(repoUrl, wc.getName() + "/folder", SVNRevision.HEAD);
        final RevisionSetupsSupport revSupp = new RevisionSetupsSupport(left, right, repoUrl, new Context(folder));
        final AtomicReference<Setup[]> ref = new AtomicReference<>();
        new SvnProgressSupport() {
            @Override
            protected void perform () {
                ref.set(revSupp.computeSetupsBetweenRevisions(this));
            }
        }.start(RequestProcessor.getDefault(), repoUrl, "bbb").waitFinished();
        Setup[] setups = ref.get();
        assertNotNull(setups);
        assertEquals(0, setups.length);
    }
    
    public void testDiffNoChange () throws Exception {
        // init
        File trunk = new File(wc, "trunk");
        File folder = new File(trunk, "folder");
        File file = new File(folder, "file");
        folder.mkdirs();
        file.createNewFile();
        
        add(trunk);
        commit(trunk);
        
        RepositoryFile left = new RepositoryFile(repoUrl, wc.getName() + "/trunk/folder", SVNRevision.HEAD);
        RepositoryFile right = new RepositoryFile(repoUrl, wc.getName() + "/branches/B/folder", SVNRevision.HEAD);
        getClient().copy(left.getFileUrl(), right.getFileUrl(), "copying...", SVNRevision.HEAD, true);
        
        final RevisionSetupsSupport revSupp = new RevisionSetupsSupport(left, right, repoUrl, new Context(folder));
        final AtomicReference<Setup[]> ref = new AtomicReference<>();
        new SvnProgressSupport() {
            @Override
            protected void perform () {
                ref.set(revSupp.computeSetupsBetweenRevisions(this));
            }
        }.start(RequestProcessor.getDefault(), repoUrl, "bbb").waitFinished();
        Setup[] setups = ref.get();
        assertNotNull(setups);
        assertEquals(0, setups.length);
    }
    
    public void testDiffModifiedFile () throws Exception {
        // init
        File trunk = new File(wc, "trunk");
        File folder = new File(trunk, "folder");
        File file = new File(folder, "file");
        folder.mkdirs();
        file.createNewFile();
        
        add(trunk);
        commit(trunk);
        
        RepositoryFile left = new RepositoryFile(repoUrl, wc.getName() + "/trunk/folder", SVNRevision.HEAD);
        RepositoryFile right = new RepositoryFile(repoUrl, wc.getName() + "/branches/B/folder", SVNRevision.HEAD);
        getClient().copy(left.getFileUrl(), right.getFileUrl(), "copying...", SVNRevision.HEAD, true);
        
        TestKit.write(file, "modification");
        commit(file);
        
        final RevisionSetupsSupport revSupp = new RevisionSetupsSupport(left, right, repoUrl, new Context(folder));
        final AtomicReference<Setup[]> ref = new AtomicReference<>();
        new SvnProgressSupport() {
            @Override
            protected void perform () {
                ref.set(revSupp.computeSetupsBetweenRevisions(this));
            }
        }.start(RequestProcessor.getDefault(), repoUrl, "bbb").waitFinished();
        Setup[] setups = ref.get();
        assertNotNull(setups);
        assertEquals(1, setups.length);
        assertEquals(file, setups[0].getBaseFile());
        assertEquals(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY, setups[0].getInfo().getStatus());
    }
    
    public void testDiffModifiedMultiFile () throws Exception {
        // init
        File trunk = new File(wc, "trunk");
        File folder = new File(trunk, "folder");
        final File file = new File(folder, "file.a");
        final File file2 = new File(folder, "file.b");
        folder.mkdirs();
        file.createNewFile();
        file2.createNewFile();
        
        add(trunk);
        commit(trunk);
        
        RepositoryFile left = new RepositoryFile(repoUrl, wc.getName() + "/trunk/folder/" + file.getName(), SVNRevision.HEAD);
        RepositoryFile right = new RepositoryFile(repoUrl, wc.getName() + "/branches/B/folder/" + file.getName(), SVNRevision.HEAD);
        getClient().copy(left.getFileUrl(), right.getFileUrl(), "copying...", SVNRevision.HEAD, true);
        left = new RepositoryFile(repoUrl, wc.getName() + "/trunk/folder/" + file2.getName(), SVNRevision.HEAD);
        right = new RepositoryFile(repoUrl, wc.getName() + "/branches/B/folder/" + file2.getName(), SVNRevision.HEAD);
        getClient().copy(left.getFileUrl(), right.getFileUrl(), "copying...", SVNRevision.HEAD, true);
        
        TestKit.write(file, "modification");
        TestKit.write(file2, "modification");
        commit(folder);
        
        final RevisionSetupsSupport revSupp = new RevisionSetupsSupport(left, right, repoUrl, new Context(new File[] { file, file2 })) {
            @Override
            protected File[] getRoots () {
                return new File[] { file, file2 };
            }
        };
        final AtomicReference<Setup[]> ref = new AtomicReference<>();
        new SvnProgressSupport() {
            @Override
            protected void perform () {
                ref.set(revSupp.computeSetupsBetweenRevisions(this));
            }
        }.start(RequestProcessor.getDefault(), repoUrl, "bbb").waitFinished();
        Setup[] setups = ref.get();
        assertNotNull(setups);
        assertEquals(2, setups.length);
        assertEquals(new HashSet<>(Arrays.asList(file, file2)),
                new HashSet<>(Arrays.asList(setups[0].getBaseFile(), setups[1].getBaseFile())));
        assertEquals(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY, setups[0].getInfo().getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY, setups[1].getInfo().getStatus());
    }
    
    public void testDiffModifiedDifferentNames () throws Exception {
        // init
        File project = new File(wc, "project");
        File trunk = new File(project, "trunk");
        final File file = new File(trunk, "file");
        trunk.mkdirs();
        file.createNewFile();
        
        add(project);
        commit(project);
        
        RepositoryFile left = new RepositoryFile(repoUrl, wc.getName() + "/project/trunk", SVNRevision.HEAD);
        RepositoryFile right = new RepositoryFile(repoUrl, wc.getName() + "/project/branches/B", SVNRevision.HEAD);
        getClient().copy(left.getFileUrl(), right.getFileUrl(), "copying...", SVNRevision.HEAD, true);
        
        TestKit.write(file, "modification");
        commit(trunk);
        
        final RevisionSetupsSupport revSupp = new RevisionSetupsSupport(left, right, repoUrl, new Context(new File[] { trunk }));
        final AtomicReference<Setup[]> ref = new AtomicReference<>();
        new SvnProgressSupport() {
            @Override
            protected void perform () {
                ref.set(revSupp.computeSetupsBetweenRevisions(this));
            }
        }.start(RequestProcessor.getDefault(), repoUrl, "bbb").waitFinished();
        Setup[] setups = ref.get();
        assertNotNull(setups);
        assertEquals(1, setups.length);
        assertEquals(file, setups[0].getBaseFile());
        assertEquals(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY, setups[0].getInfo().getStatus());
    }
    
    // proper test should run with svnkit
    public void testDiffURLs_Issue239010 () throws Exception {
        // init
        File project = new File(wc, "project");
        File trunk = new File(project, "trunk");
        final File file = new File(trunk, "file");
        final File fileDelete = new File(trunk, "deletedFolder/deleted");
        final File fileAdded = new File(trunk, "added");
        trunk.mkdirs();
        file.createNewFile();
        fileDelete.getParentFile().mkdir();
        fileDelete.createNewFile();
        
        add(project);
        commit(project);
        
        SVNRevision rev = getClient().getInfoFromWorkingCopy(file).getRevision();
        RepositoryFile left = new RepositoryFile(repoUrl, wc.getName() + "/project/trunk", rev);
        RepositoryFile right = new RepositoryFile(repoUrl, wc.getName() + "/project/trunk", SVNRevision.BASE);
        
        TestKit.write(file, "modification");
        fileAdded.createNewFile();
        add(fileAdded);
        delete(fileDelete);
        commit(trunk);
        
        update(fileDelete.getParentFile());
        
        final RevisionSetupsSupport revSupp = new RevisionSetupsSupport(left, right, repoUrl, new Context(new File[] { trunk }));
        final AtomicReference<Setup[]> ref = new AtomicReference<>();
        new SvnProgressSupport() {
            @Override
            protected void perform () {
                ref.set(revSupp.computeSetupsBetweenRevisions(this));
            }
        }.start(RequestProcessor.getDefault(), repoUrl, "bbb").waitFinished();
        Setup[] setups = ref.get();
        assertNotNull(setups);
        assertEquals(3, setups.length);
        Map<File, Setup> setupMap = new HashMap<>();
        for (Setup s : setups) {
            setupMap.put(s.getBaseFile(), s);
        }
        assertEquals(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY, setupMap.get(file).getInfo().getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, setupMap.get(fileAdded).getInfo().getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, setupMap.get(fileDelete).getInfo().getStatus());
    }
    
    private void cleanUpWC(File wc) throws IOException {
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
    
    private ISVNClientAdapter getClient() throws SVNClientException  {
        return TestKit.getClient();
    }
}

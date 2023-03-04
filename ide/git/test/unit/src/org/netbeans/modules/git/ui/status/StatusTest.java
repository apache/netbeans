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

package org.netbeans.modules.git.ui.status;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JTable;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.git.AbstractGitTestCase;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.FileStatusCache.ChangedEvent;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitVCS;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.masterfs.VersioningAnnotationProvider;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.status.VCSStatusTable;
import org.netbeans.modules.versioning.util.status.VCSStatusTableModel;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author ondra
 */
public class StatusTest extends AbstractGitTestCase {

    public StatusTest (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {
            VersioningAnnotationProvider.class,
            GitVCS.class});
        Git.STATUS_LOG.setLevel(Level.ALL);
    }

    public void testVersioningPanel () throws Exception {
        final JTable tables[] = new JTable[1];
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                GitVersioningTopComponent tc = GitVersioningTopComponent.findInstance();
                VCSContext ctx = VCSContext.forNodes(new Node[] {
                    new AbstractNode(Children.LEAF, Lookups.singleton(repositoryLocation))
                });
                tc.setContentTitle(Utils.getContextDisplayName(ctx));
                tc.setContext(ctx);
		Field f;
                try {
                    f = GitVersioningTopComponent.class.getDeclaredField("controller");
                    f.setAccessible(true);
                    VersioningPanelController controller = (VersioningPanelController) f.get(tc);
                    f = VersioningPanelController.class.getDeclaredField("fileListComponent");
                    f.setAccessible(true);
                    GitStatusTable table = (GitStatusTable) f.get(controller);
                    f = VCSStatusTable.class.getDeclaredField("table");
                    f.setAccessible(true);
                    tables[0] = (JTable) f.get(table);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        JTable table = tables[0];
        assertNotNull(table);
        assertTable(table, Collections.<File>emptySet());
        File file = new File(repositoryLocation, "file");

        file.createNewFile();
        File[] files = new File[] { repositoryLocation };
        getCache().refreshAllRoots(files);
        assertTable(table, Collections.singleton(file));

        add();
        commit();
        getCache().refreshAllRoots(files);
        assertTable(table, Collections.<File>emptySet());

        write(file, "blabla");
        add(file);
        getCache().refreshAllRoots(files);
        assertTable(table, Collections.singleton(file));

        commit();
        getCache().refreshAllRoots(files);
        assertTable(table, Collections.<File>emptySet());

        delete(false, file);
        getCache().refreshAllRoots(files);
        assertTable(table, Collections.singleton(file));
    }

    public void testBranchName () throws Exception {
        final GitVersioningTopComponent tcs[] = new GitVersioningTopComponent[1];
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                GitVersioningTopComponent tc = GitVersioningTopComponent.findInstance();
                tcs[0] = tc;
                VCSContext ctx = VCSContext.forNodes(new Node[] {
                    new AbstractNode(Children.LEAF, Lookups.singleton(FileUtil.toFileObject(FileUtil.normalizeFile(repositoryLocation))))
                });
                tc.setContentTitle(Utils.getContextDisplayName(ctx));
                tc.setContext(ctx);
            }
        });
        GitVersioningTopComponent tc = tcs[0];
        assertNotNull(tc);
        assertName(tc, "Git - work - (no branch)");
        File f = new File(repositoryLocation, "f");
        f.createNewFile();
        add();
        commit();
        RepositoryInfo.getInstance(repositoryLocation).refresh();
        assertName(tc, "Git - work - master");
    }
    
    public void test193781 () throws Exception {
        File file = new File(repositoryLocation, "f");
        file.createNewFile();
        add();
        commit();
        
        RequestProcessor.Task refreshNodesTask;
        RequestProcessor.Task changeTask;
        final VersioningPanelController[] controllers = new VersioningPanelController[1];
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run () {
                controllers[0] = new VersioningPanelController();
            }
        });
        VersioningPanelController controller = controllers[0];
        Field f = VersioningPanelController.class.getDeclaredField("refreshNodesTask");
        f.setAccessible(true);
        refreshNodesTask = (Task) f.get(controller);
        f = VersioningPanelController.class.getDeclaredField("changeTask");
        f.setAccessible(true);
        changeTask = (Task) f.get(controller);
        f = VersioningPanelController.class.getDeclaredField("fileListComponent");
        f.setAccessible(true);
        GitStatusTable statusTable = (GitStatusTable) f.get(controller);
        f = VCSStatusTable.class.getDeclaredField("tableModel");
        f.setAccessible(true);
        VCSStatusTableModel model = (VCSStatusTableModel) f.get(statusTable);
        f = VersioningPanelController.class.getDeclaredField("changes");
        f.setAccessible(true);
        Map<File, FileStatusCache.ChangedEvent> changes = (Map<File, ChangedEvent>) f.get(controller);
        final boolean barrier[] = new boolean[1];
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                while (!barrier[0]) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
        
        // do a modification
        write(file, "modification");
        getCache().refreshAllRoots(new File[] { file });
        // and simultaneously refresh all nodes ...
        controller.setContext(VCSContext.forNodes(new Node[] { new AbstractNode(Children.LEAF, Lookups.fixed(file)) }));
        // ... and simulate parallel change event from cache
        refreshNodesTask.waitFinished();
        synchronized (changes) {
            FileInformation fi = getCache().getStatus(file);
            assertTrue(fi.containsStatus(FileInformation.Status.MODIFIED_HEAD_WORKING_TREE));
            changes.put(file, new ChangedEvent(file, null, fi));
        }
        changeTask.schedule(0);
        for (;;) {
            synchronized (changes) {
                if (changes.isEmpty()) {
                    break;
                }
            }
            Thread.sleep(100);
        }
        assertEquals(0, model.getNodes().length);
        barrier[0] = true;
        for (int i = 0; i < 100 && model.getNodes().length == 0; ++i) {
            Thread.sleep(100);
        }
        changeTask.waitFinished();
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
            }
        });
        assertEquals(1, model.getNodes().length);
    }

    private void assertTable (final JTable table, Set<File> files) throws Exception {
        Thread.sleep(5000);
        final Set<File> displayedFiles = new HashSet<File>();
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < table.getRowCount(); ++i) {
                    String path = table.getValueAt(i, 2).toString();
                    displayedFiles.add(new File(repositoryLocation, path));
                }
            }
        });
        assertEquals(files, displayedFiles);
    }

    private void assertName (GitVersioningTopComponent tc, String expected) throws InterruptedException {
        for (int i = 0; i < 100; ++i) {
            if (expected.equals(tc.getName())) {
                break;
            }
            Thread.sleep(100);
        }
        assertEquals(expected, tc.getName());
    }
}

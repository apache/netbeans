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

package org.netbeans.modules.git.ui.diff;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.git.AbstractGitTestCase;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.FileStatusCache.ChangedEvent;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.ui.repository.Revision;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.status.VCSStatusTable;
import org.netbeans.modules.versioning.util.status.VCSStatusTableModel;
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
public class DiffTest extends AbstractGitTestCase {

    public DiffTest (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Git.STATUS_LOG.setLevel(Level.ALL);
    }

    public void test193781 () throws Exception {
        final File file = new File(repositoryLocation, "f");
        file.createNewFile();
        add();
        commit();
        
        RequestProcessor.Task refreshNodesTask;
        RequestProcessor.Task changeTask;
        final MultiDiffPanelController[] controllers = new MultiDiffPanelController[1];
        // do a modification
        write(file, "modification");
        getCache().refreshAllRoots(new File[] { file });

        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run () {
                controllers[0] = new MultiDiffPanelController(VCSContext.forNodes(new Node[] {
                    new AbstractNode(Children.LEAF, Lookups.fixed(file)) }), Revision.HEAD, Revision.LOCAL);
            }
        });
        MultiDiffPanelController controller = controllers[0];
        Field f = MultiDiffPanelController.class.getDeclaredField("refreshNodesRPTask");
        f.setAccessible(true);
        refreshNodesTask = (Task) f.get(controller);
        f = MultiDiffPanelController.class.getDeclaredField("changeRPTask");
        f.setAccessible(true);
        changeTask = (Task) f.get(controller);
        f = MultiDiffPanelController.class.getDeclaredField("fileListComponent");
        f.setAccessible(true);
        DiffFileTable statusTable = (DiffFileTable) f.get(controller);
        f = VCSStatusTable.class.getDeclaredField("tableModel");
        f.setAccessible(true);
        VCSStatusTableModel model = (VCSStatusTableModel) f.get(statusTable);
        f = MultiDiffPanelController.class.getDeclaredField("changes");
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
}

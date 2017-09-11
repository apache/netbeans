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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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

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
package org.netbeans.modules.search.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.search.MatchingObject;
import org.netbeans.modules.search.ResultModel;
import org.netbeans.modules.search.SearchTestUtils;
import org.netbeans.modules.search.ui.ResultsOutlineSupport.FolderTreeItem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author jhavlin
 */
public class ResultsOutlineSupportTest {

    public ResultsOutlineSupportTest() {
    }

    @Test
    public void testDeleteParent() throws IOException {
        ResultModel rm =
                SearchTestUtils.createResultModelWithOneMatch();
        MatchingObject mo = rm.getMatchingObjects().get(0);
        //create files
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject parent1 = root.createFolder("parent1");
        FileObject parent2 = parent1.createFolder("parent2");
        //create data objects
        DataObject parent1Dob = DataObject.find(parent1);
        DataObject parent2Dob = DataObject.find(parent2);
        //create folder tree items structure
        FolderTreeItem parent1Item = new FolderTreeItem(parent1Dob, null);
        FolderTreeItem parent2Item = new FolderTreeItem(
                parent2Dob, parent1Item);
        parent1Item.addChild(parent2Item);
        FolderTreeItem matchItem = new FolderTreeItem(mo, parent2Item);
        parent2Item.addChild(matchItem);

        //add listener, create value holder
        final AtomicBoolean deleted = new AtomicBoolean(false);
        mo.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (MatchingObject.PROP_REMOVED.equals(evt.getPropertyName())) {
                    deleted.set(true);
                }
            }
        });
        parent1Item.remove();
        // check that the matching object was removed, too
        assertTrue("Matching object should be removed if its parent is removed",
                deleted.get());
        assertTrue(rm.getMatchingObjects().isEmpty());
    }

    @Test
    public void testDeleteChild() throws IOException {
        ResultModel rm =
                SearchTestUtils.createResultModelWithOneMatch();
        MatchingObject mo = rm.getMatchingObjects().get(0);
        //create files
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject parent1 = root.createFolder("parent1");
        FileObject parent2a = parent1.createFolder("parent2a");
        FileObject parent2b = parent1.createFolder("parent2b");
        //create data objects
        DataObject parent1Dob = DataObject.find(parent1);
        DataObject parent2aDob = DataObject.find(parent2a);
        DataObject parent2bDob = DataObject.find(parent2b);
        //create folder tree items structure
        FolderTreeItem parent1Item = new FolderTreeItem(parent1Dob, null);
        FolderTreeItem parent2aItem = new FolderTreeItem(parent2aDob,
                parent1Item);
        parent1Item.addChild(parent2aItem);
        FolderTreeItem parent2bItem = new FolderTreeItem(parent2bDob,
                parent1Item);
        parent1Item.addChild(parent2bItem);
        FolderTreeItem matchItem = new FolderTreeItem(mo, parent2aItem);
        parent2aItem.addChild(matchItem);

        //add listener, create value holder
        final AtomicBoolean notified = new AtomicBoolean(false);
        parent1Item.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (FolderTreeItem.PROP_CHILDREN.equals(
                        evt.getPropertyName())) {
                    notified.set(true);
                }
            }
        });
        mo.remove();
        // check that the matching object was removed, too
        assertTrue("Parent2b should be removed and parent1 notified about "
                + "change in its children", notified.get());
        assertTrue(rm.getMatchingObjects().isEmpty());
    }

    @Test
    public void testUpdatingOfTotalMatchesCound() throws IOException {

        ResultModel rm = SearchTestUtils.createResultModelWithSampleData(true);
        List<MatchingObject> mos = rm.getMatchingObjects();

        FolderTreeItem root = new FolderTreeItem((DataObject) null, null);
        FolderTreeItem p1 = new FolderTreeItem((DataObject) null, root);
        FolderTreeItem p2 = new FolderTreeItem((DataObject) null, p1);
        FolderTreeItem p3 = new FolderTreeItem((DataObject) null, p2);
        FolderTreeItem p4 = new FolderTreeItem((DataObject) null, p3);

        root.addChild(p1);

        p1.addChild(new FolderTreeItem(mos.get(0), p1));
        p1.addChild(p2);

        p2.addChild(new FolderTreeItem(mos.get(1), p2));
        p2.addChild(new FolderTreeItem(mos.get(2), p2));
        p2.addChild(p3);

        p3.addChild(new FolderTreeItem(mos.get(3), p3));
        p3.addChild(new FolderTreeItem(mos.get(4), p3));
        p3.addChild(new FolderTreeItem(mos.get(5), p3));
        p3.addChild(p4);

        p4.addChild(new FolderTreeItem(mos.get(6), p4));
        p4.addChild(new FolderTreeItem(mos.get(7), p4));
        p4.addChild(new FolderTreeItem(mos.get(8), p4));
        p4.addChild(new FolderTreeItem(mos.get(9), p4));

        assertEquals(55, rm.getSelectedMatchesCount());

        mos.get(0).getTextDetails().get(0).setSelected(false);
        assertEquals(54, rm.getSelectedMatchesCount());

        mos.get(9).remove();
        assertEquals(44, rm.getSelectedMatchesCount());

        p4.remove();
        assertEquals(20, rm.getSelectedMatchesCount());

        p3.remove();
        assertEquals(5, rm.getSelectedMatchesCount());

        p2.remove();
        assertEquals(0, rm.getSelectedMatchesCount());

        p1.remove();
        assertEquals(0, rm.getSelectedMatchesCount());
    }

    /**
     * Test for bug 244044.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testDeadlock244044() throws IOException {
        final ResultModel rm = SearchTestUtils.createResultModelWithSampleData(false);
        final ResultsOutlineSupport ros[] = new ResultsOutlineSupport[1];
        try {
            EventQueue.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    ros[0] = new ResultsOutlineSupport(false, false, rm,
                            null, Node.EMPTY);
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }

        assertNotNull(ros[0]);
        final FileSystem fs = FileUtil.createMemoryFileSystem();

        @SuppressWarnings("PackageVisibleField")
        class FinishFlag {
            volatile boolean thread1Finished = false;
            volatile boolean thread2Finished = false;
            boolean areThreadsFinished() {
                return thread1Finished && thread2Finished;
            }
        }

        for (int i = 0; i < 100; i++) {

            final int fi = i;
            final FinishFlag finishFlag = new FinishFlag();

            final String fileName = "file" + fi + ".txt";
            try {
                fs.getRoot().createData(fileName);
                rm.objectFound(fs.getRoot().getFileObject(fileName), Charset.defaultCharset(), null);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            // Add a new file ...
            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    ros[0].update();
                    ros[0].getResultsNode().getChildren().getNodes();
                    finishFlag.thread1Finished = true;
                }
            });

            // ... and close the model at the same time.
            Thread t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    // simulate ResultsOutlineSupport.clean() - synchronize and
                    // call ResultModel.close()
                    synchronized (ros[0]) {
                        rm.close();
                        ros[0].getResultsNode().getChildren().getNodes();
                    }
                    finishFlag.thread2Finished = true;
                }
            });
            t1.start();
            t2.start();
            try {
                t1.join(10000);
                t2.join(10000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (!finishFlag.areThreadsFinished()) {
                fail("Deadlock occured at iteration " + fi);
            }
        }
    }
}

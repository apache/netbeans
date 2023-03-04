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
        mo.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (MatchingObject.PROP_REMOVED.equals(evt.getPropertyName())) {
                deleted.set(true);
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
        parent1Item.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (FolderTreeItem.PROP_CHILDREN.equals(
                    evt.getPropertyName())) {
                notified.set(true);
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
            EventQueue.invokeAndWait(() -> ros[0] = new ResultsOutlineSupport(false, false, rm, null, Node.EMPTY));
        } catch (InterruptedException |
                InvocationTargetException ex) {
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
            Thread t1 = new Thread(() -> {
                ros[0].update();
                ros[0].getResultsNode().getChildren().getNodes();
                finishFlag.thread1Finished = true;
            });

            // ... and close the model at the same time.
            Thread t2 = new Thread(() -> {
                // simulate ResultsOutlineSupport.clean() - synchronize and
                // call ResultModel.close()
                synchronized (ros[0]) {
                    rm.close();
                    ros[0].getResultsNode().getChildren().getNodes();
                }
                finishFlag.thread2Finished = true;
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

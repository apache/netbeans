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

package org.openide.loaders;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Enumerations;
import org.openide.util.test.TestFileUtils;

/** Does a change in order on folder fire the right properties?
 *
 * @author  Jaroslav Tulach, Jiri Skrivanek
 */
public class DataFolderTimeOrderTest extends NbTestCase implements PropertyChangeListener {

    private DataFolder aa;
    private final ArrayList<String> events = new ArrayList<String>();
    private static FileSystem lfs;
    
    public DataFolderTimeOrderTest (String name) {
        super (name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp () throws Exception {
        clearWorkDir();

        MockServices.setServices(Pool.class);
        
        String fsstruct [] = new String [] {
            "AA/X.txt",
            "AA/Y.txt",
        };
        
        lfs = TestUtilHid.createLocalFileSystem (getWorkDir (), fsstruct);

        final FileObject x = lfs.findResource("AA/X.txt");
        assertNotNull("X.txt", x);
        Thread.sleep(5);
        final FileObject y = lfs.findResource("AA/Y.txt");
        assertNotNull("Y.txt", y);
        OutputStream os = y.getOutputStream();
        os.write("Ahoj".getBytes());
        os.close();
        
        org.openide.filesystems.test.TestFileUtils.touch(y, x);

        aa = DataFolder.findFolder (lfs.findResource ("AA"));
        aa.addPropertyChangeListener (this);
    }

    @Override
    protected void tearDown () throws Exception {
        final DataLoader l = DataLoader.getLoader(DataObjectInvalidationTest.SlowDataLoader.class);
        
        aa.removePropertyChangeListener (this);
    }

    public void testLastModifiedOrderUpdatedAfterFileIsTouched() throws Exception {
        aa.setSortMode(DataFolder.SortMode.LAST_MODIFIED);

        Node n = aa.getNodeDelegate().cloneNode();
        Node[] nodes = n.getChildren().getNodes(true);
        assertEquals ("Two nodes", 2, nodes.length);

        waitEvents();
        assertEquals("Sort mode not changed and children not refreshed: " + events, 2, events.size());
        assertTrue(DataFolder.PROP_SORT_MODE + " change not fired", events.contains(DataFolder.PROP_SORT_MODE));
        assertTrue(DataFolder.PROP_CHILDREN + " change not fired", events.contains(DataFolder.PROP_CHILDREN));
        assertEquals("Y.txt", nodes[0].getName()); // Y is newer
        assertEquals("X.txt", nodes[1].getName()); // X is older
        events.clear();

        final FileObject orig = lfs.findResource("AA/Y.txt");
        final FileObject touch = lfs.findResource("AA/X.txt");

        // After touching, X.txt will be newer than Y.txt.
        TestFileUtils.touch(FileUtil.toFile(touch), FileUtil.toFile(orig));
        // It's not enough to wait only for DataFolder event
        // because of number of RP tasks run before node children are updated
        // must wait for reorder fired by node itself.
        final CountDownLatch barrier = new CountDownLatch(1);
        NodeListener nodeList = new NodeAdapter() {

            @Override
            public void childrenReordered (NodeReorderEvent ev) {
                barrier.countDown();
            }
          
        };
        n.addNodeListener(nodeList);
        try {
            touch.refresh();
            waitEvents();
            // wait for node reorder event
            barrier.await(10, TimeUnit.SECONDS);
        } finally {
            n.removeNodeListener(nodeList);
        }
        assertEquals(0, barrier.getCount());
        assertTrue(DataFolder.PROP_CHILDREN + " change not fired", events.contains(DataFolder.PROP_CHILDREN));

        Node[] newNodes = n.getChildren().getNodes(true);
        assertEquals("Node " + nodes[1].getName() + " expected first.", newNodes[0], nodes[1]);
        assertEquals("Node " + nodes[0].getName() + " expected second.", newNodes[1], nodes[0]);
    }

    /** Wait for events list not empty. */
    private void waitEvents() throws Exception {
        for (int delay = 1; delay < 3000; delay *= 2) {
            Thread.sleep(delay);
            if (!events.isEmpty()) {
                break;
            }
        }
    }

    @Override
    public synchronized void propertyChange (PropertyChangeEvent evt) {
        events.add (evt.getPropertyName ());
    }

    
    public static final class Pool extends DataLoaderPool {
        
        @Override
        protected Enumeration<? extends org.openide.loaders.DataLoader> loaders() {
            return Enumerations.singleton(DataLoader.getLoader(DataObjectInvalidationTest.SlowDataLoader.class));
        }
        
    } // end of Pool
}

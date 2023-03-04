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

import java.util.concurrent.CountDownLatch;
import junit.framework.Test;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Mutex;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FolderChildrenInEQTest extends FolderChildrenTest {

    public FolderChildrenInEQTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return new FolderChildrenInEQTest("testCountNumberOfNodesWhenUsingFormLikeLoader");
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testDeadlockWaitingForDelayedNode() throws Exception {
        Pool.setLoader(FormKitDataLoader.class);
        
        FileUtil.createFolder(FileUtil.getConfigRoot(), "FK/A");
        
        FileObject bb = FileUtil.getConfigFile("/FK");
        final DataFolder folder = DataFolder.findFolder(bb);
        final Node node = folder.getNodeDelegate();
        
        
        Node[] one = node.getChildren().getNodes(true);
        assertNodes(one, "A");
        
        FormKitDataLoader.waiter = new CountDownLatch(1);
        FileUtil.createData(FileUtil.getConfigRoot(), "FK/B");
        Node[] arr = Children.MUTEX.readAccess(new Mutex.ExceptionAction<Node[]>() {
            @Override
            public Node[] run() throws Exception {
                // don't deadlock
                return node.getChildren().getNodes(true);
            }
        });
        
        FormKitDataLoader.waiter.countDown();
        arr = node.getChildren().getNodes(true);
        
        assertNotNull("We have data object now", arr[1].getLookup().lookup(DataObject.class));
        
        assertFalse("No leaf", arr[0].isLeaf());
        assertTrue("File B is leaf", arr[1].isLeaf());
    }

    @RandomlyFails // NB-Core-Build #6728: Accepts only Ahoj expected:<1> but was:<2>
    @Override public void testChildrenCanGC() throws Exception {
        super.testChildrenCanGC();
    }
    
}

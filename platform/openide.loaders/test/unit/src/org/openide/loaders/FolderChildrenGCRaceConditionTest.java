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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

public class FolderChildrenGCRaceConditionTest extends NbTestCase {
    private Logger LOG;
    
    public FolderChildrenGCRaceConditionTest() {
        super("");
    }
    
    public FolderChildrenGCRaceConditionTest(java.lang.String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        LOG = Logger.getLogger(FolderChildrenGCRaceConditionTest.class.getName());

        FileObject[] arr = FileUtil.getConfigRoot().getChildren();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete();
        }
    }

    @RandomlyFails // NB-Core-Build #1087
    public void testChildrenCanBeSetToNullIfGCKicksIn () throws Exception {
        FileObject f = FileUtil.createData(FileUtil.getConfigRoot(), "folder/node.txt");
        
        DataFolder df = DataFolder.findFolder(f.getParent());
        Node n = df.getNodeDelegate();
        
        Node[] arr = n.getChildren().getNodes(true);
        assertEquals("Ok, one", 1, arr.length);
        final Reference<?> ref = new WeakReference<Node>(arr[0]);
        arr = null;
        
        class R implements Runnable {
            @Override
            public void run() {
                LOG.info("Ready to GC");
                assertGC("Node can go away in the worst possible moment", ref);
                LOG.info("Gone");
            }
        }
        R r = new R();
        RequestProcessor.Task t = new RequestProcessor("Inter", 1, true).post(r);
        
        Log.controlFlow(Logger.getLogger("org.openide.loaders"), null,
            "THREAD:FolderChildren_Refresh MSG:Children computed" +
            "THREAD:FolderChildren_Refresh MSG:notifyFinished.*" +
            "THREAD:Inter MSG:Gone.*" +
            "THREAD:Finalizer MSG:RMV.*" +
            "THREAD:FolderChildren_Refresh MSG:Clearing the ref.*" +
            "", 200);
        
        LOG.info("Before getNodes(true");
        int cnt = n.getChildren().getNodes(true).length;
        LOG.info("Children are here: " + cnt);
        t.cancel();
        LOG.info("Cancel done");
        assertEquals("Count is really one", 1, cnt);
    }
   
}

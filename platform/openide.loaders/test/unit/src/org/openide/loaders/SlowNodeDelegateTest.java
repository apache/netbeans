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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class SlowNodeDelegateTest extends NbTestCase {
    private FileObject dir;
    private FileObject a;
    private FileObject b;

    public SlowNodeDelegateTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(SlowL.class);
        clearWorkDir();
        dir = FileUtil.toFileObject(getWorkDir());
        a = dir.createData("slow.a");
        b = dir.createData("slow.b");
    }

    public void testAIsBlockedButBCanBeCreated() throws Exception {
        final SlowND objA = (SlowND) DataObject.find(a);
        SlowND objB = (SlowND) DataObject.find(b);
        
        final RequestProcessor RP = new RequestProcessor("Node for A");
        final Node[] nodeA = { null };
        RequestProcessor.Task task = RP.post(new Runnable() {
            @Override
            public void run() {
                nodeA[0] = objA.getNodeDelegate();
            }
        });
        assertFalse("Did not finish yet", task.waitFinished(500));
        assertNull("Node not yet created", nodeA[0]);
        
        Node nodeB = objB.getNodeDelegate();
        assertNotNull("Meanwhile other nodes can be created", nodeB);
        
        
        assertNull("Node A still not created", nodeA[0]);
        synchronized (objA) {
            objA.notifyAll();
        }
        task.waitFinished();
        
        assertNotNull("Node A also created", nodeA[0]);
    }
    

    public static final class SlowL extends UniFileLoader {
        public SlowL() {
            super(SlowND.class);
            getExtensions().addExtension("a");
            getExtensions().addExtension("b");
        }
        
        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SlowND(primaryFile, this);
        }
    }
    
    private static final class SlowND extends MultiDataObject {
        private final AtomicInteger beingCreated = new AtomicInteger(0);
        public SlowND(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
            super(fo, loader);
        }

        @Override
        protected Node createNodeDelegate() {
            assertEquals("Only one call to createNodeDelegate for " + getPrimaryFile(), 1, beingCreated.incrementAndGet());
            
            if (getPrimaryFile().hasExt("a")) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
            return super.createNodeDelegate();
        }
        
        
    }
}

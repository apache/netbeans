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
package org.openide.nodes;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbTestCase;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

public class ChildrenInitializedMeanwhileTest extends NbTestCase {
    private static final RequestProcessor RP = new RequestProcessor("Test");
    private volatile boolean wasNotified;

    public ChildrenInitializedMeanwhileTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        EntrySupportLazy.LOGGER.addHandler(new java.util.logging.Handler() {
            {
                setLevel(Level.FINER);
                EntrySupportLazy.LOGGER.setLevel(Level.FINER);
            }
            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().startsWith("setEntries():")) {
                    notifyRecordIsHere();
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            
        });
    }
    
    final CountDownLatch initializing = new CountDownLatch(1);
    final void notifyRecordIsHere() {
        try {
            initializing.await();
            wasNotified = true;
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    
    public void testLongInitParallelSet() throws Exception {
        
        class K extends Children.Keys<String> {
            public K() {
                super(true);
            }
            
            @Override
            protected void addNotify() {
            }

            void oneKey() {
                setKeys(new String[] { "1" });
            }

            @Override
            protected Node[] createNodes(String key) {
                AbstractNode an = new AbstractNode(Children.LEAF);
                an.setName(key);
                return new Node[] { an };
            }
        }
        
        final K k = new K();
        final Node root = new AbstractNode(k);
        
        Task task = RP.post(new Runnable() {
            @Override
            public void run() {
                k.oneKey();
            }
        });
        
        task.waitFinished(100);
        assertFalse("Not finished after waiting a bit", task.isFinished());
        
        initializing.countDown();
        
        Node[] after = root.getChildren().getNodes();
        assertEquals("One", 1, after.length);
        
        assertTrue("The message has been logged", wasNotified);
    }
}

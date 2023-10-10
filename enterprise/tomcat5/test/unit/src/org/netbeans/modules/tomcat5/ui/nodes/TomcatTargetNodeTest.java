/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.tomcat5.ui.nodes;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class TomcatTargetNodeTest extends NbTestCase {

    private static RequestProcessor processor = new RequestProcessor("Deadlock Test", 2);

    public TomcatTargetNodeTest(String name) {
        super(name);
    }

    public void testDeadlock191535() {
        final CountDownLatch latch = new CountDownLatch(2);
        final CountDownLatch finish = new CountDownLatch(1);

        Runnable blocking = () -> {
            Children.MUTEX.postWriteRequest(new Runnable() {
                @Override
                public void run() {
                    latch.countDown();
                    try {
                        latch.await();
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        };

        Runnable testing = () -> {
            latch.countDown();
            try {
                latch.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            try {
                TomcatTargetNode node = new TomcatTargetNode(Lookup.EMPTY);
            } finally {
                finish.countDown();
            }
        };
        
        processor.post(blocking);
        processor.post(testing);

        try {
            assertTrue("Deadlock detected", finish.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException ex) {
            fail("Test interrupted"); 
        }
        
    }
}

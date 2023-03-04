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
package org.netbeans;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Task;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class TaskFutureTest {
    
    public TaskFutureTest() {
    }

    @Test
    public void testTrueEmptyTask() throws Exception {
        TaskFuture tf = new TaskFuture(true, Task.EMPTY);
        assertTrue("Really true", tf.get());
    }

    @Test
    public void testFalseEmptyTask() throws Exception {
        TaskFuture tf = new TaskFuture(false, Task.EMPTY);
        assertFalse("Really false", tf.get());
    }

    @Test
    public void testRealTask() throws Exception {
        class T extends Task {
            public T() {
                notifyRunning();
            }
            public void done() {
                notifyFinished();
            }
        }
        
        T t = new T();
        assertFalse("Not finished yet", t.isFinished());
        
        TaskFuture tf = new TaskFuture(false, t);
        
        
        try {
            tf.get(100, TimeUnit.MILLISECONDS);
            fail("Should time out");
        } catch (TimeoutException timeoutException) {
            // OK
        }
        t.done();
        
        assertFalse("Really false", tf.get());
    }
}

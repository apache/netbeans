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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure.Queue;

/**
 *
 * @author Andrei Badea
 */
public class TableClosureQueueTest extends TestCase {

    public void testInitialElements() {
        Set<String> initial = new HashSet<>(Arrays.asList("a", "b"));
        Queue<String> queue = new Queue<>(initial);
        Set<String> dequeued = new HashSet<>();
        while (!queue.isEmpty()) {
            dequeued.add(queue.poll());
        }
        assertNull("The poll() method should return null at the end of the queue.", queue.poll());
        assertEquals("Should dequeue the initial elements.", initial, dequeued);
    }

    public void testCannotAddEvenDequeuedElements() {
        Set<String> initial = Collections.emptySet();
        Queue<String> queue = new Queue<>(initial);
        queue.offer("a");
        queue.offer("b");
        queue.offer("c");
        // add some existing elements
        queue.offer("a");
        queue.offer("c");
        assertEquals("Should poll 'a'.", "a", queue.poll());
        assertEquals("Should poll 'b'.", "b", queue.poll());
        // add some already dequeued elements
        queue.offer("b");
        queue.offer("a");
        assertEquals("Should not poll already dequeued elements.", "c", queue.poll());
        assertNull("The queue should be empty.", queue.poll());
    }
}

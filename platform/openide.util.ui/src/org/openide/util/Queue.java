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

package org.openide.util;

import java.util.LinkedList;

/** Queue of objects. When there is no object in the queue the process
* is suspended till some arrives.
* Implementation appears to be LIFO.
*
* @author Jaroslav Tulach
* @deprecated Use {@link java.util.concurrent.BlockingQueue} instead.
*/
@Deprecated
public class Queue<T> extends Object {
    /** Queue enumeration */
    private LinkedList<T> queue = new LinkedList<T>();

    /** Adds new item.
    * @param o object to add
    */
    public synchronized void put(T o) {
        queue.add(o);
        notify();
    }

    /** Gets an object from the queue. If there is no such object the
    * thread is suspended until some object arrives
    *
    * @return object from the queue
    */
    public synchronized T get() {
        for (;;) {
            if (queue.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                }
            } else {
                break;
            }
        }

        return queue.removeFirst();
    }
}

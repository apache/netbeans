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

package org.netbeans.installer.downloader.dispatcher.impl;

import static java.lang.Thread.State.NEW;
import java.util.Queue;
import java.util.LinkedList;

/**
 * @author Danila_Dugurov
 */
public class WorkersPool {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private final int capacity;

    private int inUse;
    private Queue<Worker> freeWorkers = new LinkedList<Worker>();

    public WorkersPool(final int poolCapacity) {
        this.capacity = poolCapacity;
    }

    public int capacity() {
        return capacity;
    }

    public synchronized int remaining() {
        return capacity - inUse;
    }

    //noblocking
    public synchronized Worker tryAcquire() {
        if (inUse == capacity) {
            return null;
        }
        inUse++;
        final Worker worker = freeWorkers.poll();
        return worker != null && worker.isAlive() ? worker : new Worker();
    }

    public synchronized Worker acquire() throws InterruptedException {
        while (true) {
            final Worker worker = tryAcquire();
            if (worker == null) {
                wait();
            } else {
                return worker;
            }
        }
    }

    public synchronized void release(final Worker worker) {
        inUse--;
        if (worker.isAlive()) {
            freeWorkers.offer(worker);
        } else if (NEW == worker.getState()) {
            freeWorkers.offer(worker);
        }
        notify();
    }

    public synchronized void stopWaitingWorkers() {
    }
}

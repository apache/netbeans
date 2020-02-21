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
package org.netbeans.modules.cnd.remote.support;

import java.util.concurrent.CountDownLatch;

/**
 *
 */
public abstract class ParallelWorker implements Runnable {
    
    private final String name;
    private final CountDownLatch latch;

    public ParallelWorker(String name, CountDownLatch latch) {
        this.name = name;
        this.latch = latch;
    }

    @Override
    public final void run() {
        String oldName = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName(getName());
            runImpl();
        } finally {
            Thread.currentThread().setName(oldName);
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    public String getName() {
        return name;
    }

    protected abstract void runImpl();
    
}

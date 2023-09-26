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
package org.openide.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Tim Boudreau
 */
public class RequestProcessor226051Test {

    private static final long DELAY = 2000;

    @Test
    public void testAwaitTermination() throws InterruptedException {
        if (true) return;
        
        int count = 5;
        RequestProcessor rp = new RequestProcessor(getClass().getSimpleName(), count+1, false);
        CountDownLatch latch = new CountDownLatch(count);
        List<R> rs = new LinkedList<R>();
        for (int i = 0; i < count; i++) {
            R r = new R(latch);
            rs.add(r);
            rp.post(r);
        }
        rp.shutdown();
        boolean res = rp.awaitTermination(DELAY * (count + 1), TimeUnit.MILLISECONDS);
        for (R r : rs) {
            assertTrue(r.ran.get());
        }
        assertTrue(res);
    }

    static class R implements Runnable {

        private final CountDownLatch exitLatch;
        private final AtomicBoolean ran = new AtomicBoolean();

        R(CountDownLatch exitLatch) {
            this.exitLatch = exitLatch;
        }

        @Override
        public void run() {
            try {
                boolean done = false;
                while (!done) {
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        done = true;
                    }
                }
            } finally {
                ran.set(true);
            }
            exitLatch.countDown();
        }
    }
}

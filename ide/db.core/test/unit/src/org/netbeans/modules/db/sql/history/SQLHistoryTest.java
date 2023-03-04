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
package org.netbeans.modules.db.sql.history;

import java.util.Date;
import org.junit.Test;

public class SQLHistoryTest {

    public SQLHistoryTest() {
    }

    @Test
    public void testConcurrency() {
        // Enforce error condition by writing from multiple threads as fast
        // as possible -- this test is an approximation but at creation time
        // reprocuced the bug
        //
        // https://netbeans.org/bugzilla/show_bug.cgi?id=256238
        
        SQLHistory hist = new SQLHistory();
        hist.setHistoryLimit(10);

        int threadCount = 10;
        int inserts = 1000;

        Worker[] writer = new Worker[threadCount];
        
        for(int i = 0; i < writer.length; i++) {
            writer[i] = new Worker(threadCount, inserts, hist);
        }
        
        for(int i = 0; i < writer.length; i++) {
            writer[i].start();
        }
        
        for (int i = 0; i < writer.length; i++) {
            try {
                writer[i].join(10 * 1000);
                assert writer[i].isNormalEnd();
            } catch (InterruptedException ex) {
                assert false : "Was interrupted";
            }
        }
    }

    private static class Worker extends Thread {

        private final int threadId;
        private final int writes;
        private final SQLHistory history;
        private boolean normalEnd = false;

        public Worker(int threadId, int writes, SQLHistory history) {
            this.threadId = threadId;
            this.writes = writes;
            this.history = history;
        }

        @Override
        public void run() {
            for (int i = 0; i < writes; i++) {
                SQLHistoryEntry sqe = new SQLHistoryEntry("DemoURL " + threadId, "SQL "
                        + i, new Date());
                history.add(sqe);
            }
            normalEnd = true;
        }

        public boolean isNormalEnd() {
            return normalEnd;
        }
    }
}

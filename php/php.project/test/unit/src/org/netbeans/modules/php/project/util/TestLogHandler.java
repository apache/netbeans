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
package org.netbeans.modules.php.project.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Log handler which is able to wait for a log message.
 */
public final class TestLogHandler extends Handler {

    final class R {

        private final Queue<String> toExpect;


        private R(final String... toExpect) {
            this.toExpect = new LinkedList<>(Arrays.asList(toExpect));
        }

    }

    private R r;


    public synchronized void expect(final String... expect) {
        r = new R(expect);
    }

    public synchronized R await(long timeOut) throws InterruptedException {
        final long st = System.currentTimeMillis();
        while (!r.toExpect.isEmpty()) {
            if (System.currentTimeMillis() - st > timeOut) {
                return null;
            }
            wait(timeOut);
        }
        return r;
    }

    @Override
    public synchronized void publish(LogRecord record) {
        if (record.getMessage() != null && record.getMessage().equals(r.toExpect.peek())) {
            r.toExpect.poll();
        }
        if (r.toExpect.isEmpty()) {
            notifyAll();
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

}

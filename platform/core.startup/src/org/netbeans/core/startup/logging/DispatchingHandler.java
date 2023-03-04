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
package org.netbeans.core.startup.logging;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.openide.util.RequestProcessor;

/** Non closing handler which dispatches messagtes in different thread.
 */
final class DispatchingHandler extends Handler implements Runnable {
    private static final int LIMIT = 1024;
    private static RequestProcessor RP = new RequestProcessor("Logging Flush", 1, false, false); // NOI18N
    private static ThreadLocal<Boolean> FLUSHING = new ThreadLocal<Boolean>();
    private final Handler delegate;
    private final BlockingQueue<LogRecord> queue = new LinkedBlockingQueue<LogRecord>(LIMIT);
    private RequestProcessor.Task flush;
    private int delay;

    DispatchingHandler(Handler h, int delay) {
        delegate = h;
        flush = RP.create(this, true);
        flush.setPriority(Thread.MIN_PRIORITY);
        this.delay = delay;
    }

    @Override
    public void setFormatter(Formatter newFormatter) throws SecurityException {
        delegate.setFormatter(newFormatter);
    }
    
    @Override
    public void publish(LogRecord record) {
        if (RP.isRequestProcessorThread()) {
            return;
        }
        boolean empty = queue.isEmpty();
        if (!queue.offer(record)) {
            for (;;) {
                try {
                    // queue is full, schedule its clearing
                    if (!schedule(true)) {
                        return;
                    }
                    queue.put(record);
                    Thread.yield();
                    break;
                } catch (InterruptedException ex) {
                    // OK, ignore and try again
                }
            }
        }
        Throwable t = record.getThrown();
        if (t != null) {
            StackTraceElement[] tStack = t.getStackTrace();
            StackTraceElement[] hereStack = new Throwable().getStackTrace();
            for (int i = 1; i <= Math.min(tStack.length, hereStack.length); i++) {
                if (!tStack[tStack.length - i].equals(hereStack[hereStack.length - i])) {
                    NbFormatter.registerCatchIndex(t, tStack.length - i);
                    break;
                }
            }
        }
        if (empty) {
            schedule(false);
        }
    }

    private boolean schedule(boolean now) {
        if (!Boolean.TRUE.equals(FLUSHING.get())) {
            try {
                FLUSHING.set(true);
                int d;
                if (now) {
                    d = 0;
                } else {
                    int emptySpace = LIMIT - queue.size();
                    d = delay * emptySpace / LIMIT;
                    assert d <= delay : "d: " + d + " delay: " + delay;
                    assert d >= 0 : "d: " + d;
                }
                flush.schedule(d);
            } finally {
                FLUSHING.set(false);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void flush() {
        flush.cancel();
        flush.waitFinished();
        run();
    }

    @Override
    public void close() throws SecurityException {
        flush();
        delegate.flush();
    }

    final void doClose() throws SecurityException {
        flush();
        delegate.close();
    }

    @Override
    public Formatter getFormatter() {
        return delegate.getFormatter();
    }

    static Handler getInternal(Handler h) {
        if (h instanceof DispatchingHandler) {
            return ((DispatchingHandler) h).delegate;
        }
        return h;
    }

    @Override
    public void run() {
        if (queue.isEmpty()) {
            return;
        }
        for (;;) {
            LogRecord r = queue.poll();
            if (r == null) {
                schedule(false);
                break;
            }
            delegate.publish(r);
        }
        delegate.flush();
    }
}

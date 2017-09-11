/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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

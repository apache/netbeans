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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 */
public final class ParserThreadManager {

    private static ParserThreadManager instance;
    private static final String threadNameBase = "Code Model Parser"; // NOI18N
    private RequestProcessor processor;
    private final Set<Wrapper> wrappers = new CopyOnWriteArraySet<>();
    private int currThread = 0;
    private boolean started = false;

    private class Wrapper implements Runnable {

        private final ParserThread delegate;
        private Thread thread;

        public Wrapper(ParserThread delegate) {
            this.delegate = delegate;
        }

        public void stop() {
            assert this.delegate != null;
            this.delegate.stop();
        }

        public boolean isStoped() {
            assert this.delegate != null;
            return this.delegate.isStoped();
        }

        @Override
        public void run() {
            try {
                thread = Thread.currentThread();
                thread.setName(threadNameBase + ' ' + currThread++);
                wrappers.add(this);
                delegate.run();
            } catch (Throwable thr) {
                DiagnosticExceptoins.register(thr);
            } finally {
                wrappers.remove(this);
            }
        }
    }

    private ParserThreadManager() {
    }

    public static synchronized ParserThreadManager instance() {
        if (instance == null) {
            instance = new ParserThreadManager();
        }
        return instance;
    }

    public boolean isStandalone() {
        return (processor == null);
    }

    // package-local
    synchronized void startup(boolean standalone) {

        if (started) {
            shutdown();
        }

        ParserQueue.instance().startup();

//        int threadCount = Integer.getInteger("cnd.modelimpl.parser.wrappers",
//                Math.max(Runtime.getRuntime().availableProcessors()-1, 1)).intValue();

        int threadCount = CndUtils.getNumberCndWorkerThreads();

        if (!standalone) {
            processor = new RequestProcessor(threadNameBase, threadCount);
        }
        for (int i = 0; i < threadCount; i++) {
            Runnable r = new Wrapper(new ParserThread());
            if (standalone) {
                new Thread(r).start();
            } else {
                processor.post(r);
            }
        }
        started = true;
    }

    // package-local
    synchronized void shutdown() {
        if (TraceFlags.TRACE_MODEL_STATE) {
            System.err.println("=== ParserThreadManager.shutdown");
        }

        for (Wrapper wrapper : wrappers) {
            wrapper.stop();
        }
        ParserQueue.instance().shutdown();
        for (Wrapper wrapper : wrappers) {
            while (true) {
                if (wrapper.isStoped()) {
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        currThread = 0;
        started = false;
    }

    public boolean isParserThread() {
        if (isStandalone()) {
            Thread current = Thread.currentThread();

            for (Wrapper wrapper : wrappers) {
                if (wrapper.thread == current) {
                    return true;
                }
            }
            return false;
        } else {
            return processor.isRequestProcessorThread();
        }
    }

    public void waitEmptyProjectQueue(ProjectBase prj) {
        ParserQueue.instance().waitEmpty(prj);
    }
}

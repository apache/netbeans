/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

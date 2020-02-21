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

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.services.CsmStandaloneFileProvider;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 */
public final class ParserThread implements Runnable {

    private volatile boolean stopped = false;
    private boolean isStoped = false;

    /*package-local*/ ParserThread() {
    }

    public void stop() {
        this.stopped = true;
    }

    @Override
    public void run() {
        try {
            _run();
        } finally {
            isStoped = true;
        }
    }

    public boolean isStoped() {
        return isStoped;
    }

    private void _run() {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            trace("started"); // NOI18N
        }
        ParserQueue queue = ParserQueue.instance();
        while (!stopped) {
            if (TraceFlags.TRACE_PARSER_QUEUE) {
                trace("polling queue"); // NOI18N
            }
            try {
                ParserQueue.Entry entry = queue.poll();
                if (entry == null) {
                    if (TraceFlags.TRACE_PARSER_QUEUE) {
                        trace("waiting"); // NOI18N
                    }
                    isStoped = true;
                    queue.waitReady();
                    isStoped = false;
                } else {
                    Thread currentThread = Thread.currentThread();
                    String oldThreadName = currentThread.getName();
                    FileImpl file = entry.getFile();
                    if (TraceFlags.TRACE_PARSER_QUEUE) {
                        trace("parsing started: " + entry.toString(TraceFlags.TRACE_PARSER_QUEUE_DETAILS)); // NOI18N
                    }
                    Diagnostic.StopWatch stw = TraceFlags.TIMING ? new Diagnostic.StopWatch() : null;
                    String parseCase = ": Parsing "; // NOI18N
                    ProjectBase project = null;
                    try {
                        Collection<PreprocHandler.State> states = entry.getPreprocStates();
                        Collection<PreprocHandler> preprocHandlers = new ArrayList<>(states.size());
                        project = file.getProjectImpl(true);
                        for (PreprocHandler.State state : states) {
                            if (!project.isDisposing()) { // just in case check
                                if (state == FileImpl.DUMMY_STATE) {
                                    CndUtils.assertTrueInConsole(states.size() == 1, "Dummy state sould never be mixed with normal states \n", states); //NOI18N
                                    preprocHandlers = FileImpl.DUMMY_HANDLERS;
                                    parseCase = ": ONE FILE Reparsing "; // NOI18N
                                    break;
                                } else if (state == FileImpl.PARTIAL_REPARSE_STATE) {
                                    CndUtils.assertTrueInConsole(states.size() == 1, "reparse Dummy state sould never be mixed with normal states \n", states); //NOI18N
                                    preprocHandlers = FileImpl.PARTIAL_REPARSE_HANDLERS;
                                    parseCase = ": PARTIAL Reparsing "; // NOI18N
                                    break;
                                }
                                PreprocHandler preprocHandler = project.createPreprocHandlerFromState(file.getAbsolutePath(), state);
                                if (TraceFlags.TRACE_PARSER_QUEUE) {
                                    System.err.println("before ensureParse on " + file.getAbsolutePath() +
                                            ParserQueue.tracePreprocState(state));
                                }
                                preprocHandlers.add(preprocHandler);
                            }
                        }
                        if (!project.isDisposing()) {
                            currentThread.setName(oldThreadName + parseCase + file.getAbsolutePath()); // NOI18N
                            if (TraceFlags.SUSPEND_PARSE_FILE_TIME > 0) {
                                try {
                                    System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_FILE_TIME + "ms before parsing " + file.getAbsolutePath());
                                    Thread.sleep(TraceFlags.SUSPEND_PARSE_FILE_TIME);
                                    System.err.println("awoke after sleep");
                                } catch (InterruptedException ex) {
                                    // do nothing
                                }
                            }
                            file.ensureParsed(preprocHandlers);
                        }
                    } catch (Throwable thr) {
                        DiagnosticExceptoins.register(thr);
                    } finally {
                        if (stw != null) {
                            long parseTime;
                            if (TraceFlags.TIMING_PARSE_PER_FILE_FLAT) {
                                String standalone = CsmStandaloneFileProvider.getDefault().isStandalone(file) ? "STANDALONE " : ""; // NOI18N
                                parseTime = stw.stopAndReport(parseCase + standalone + file.getBuffer().getUrl()); // NOI18N
                            } else {
                                parseTime = stw.stop();
                            }
                            ParserQueue.instance().addParseStatistics(project, file, parseTime);
                        }
                        try {
                            queue.onFileParsingFinished(file);
                            if (TraceFlags.TRACE_PARSER_QUEUE) {
                                trace("parsing done for " + file.getAbsolutePath() + " took " + file.getLastParseTime() + "ms"); // NOI18N
                            }
                            Notificator.instance().flush();
                            if (TraceFlags.TRACE_PARSER_QUEUE) {
                                trace("model event flushed"); // NOI18N
                            }
                        } catch (Throwable thr) {
                            thr.printStackTrace(System.err);
                        }
                        currentThread.setName(oldThreadName);
                    }
                }
            } catch (InterruptedException ex) {
                if (TraceFlags.TRACE_PARSER_QUEUE) {
                    trace("interrupted"); // NOI18N
                }
                break;
            }
        }
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            trace(stopped ? "stopped" : "finished"); // NOI18N
        }
    }

    private void trace(String text) {
        System.err.println(Thread.currentThread().getName() + ": " + text);
    }
}

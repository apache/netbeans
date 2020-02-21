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

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

package org.netbeans.modules.csl.hints.infrastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.core.CancelSupportImplementation;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.core.SchedulerTaskCancelSupportImpl;
import org.netbeans.modules.csl.core.SpiSupportAccessor;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tor Norbye
 */
public final class SelectionHintsTask extends ParserResultTask<ParserResult> {
    
    private static final Logger LOG = Logger.getLogger(SelectionHintsTask.class.getName());
    private final CancelSupportImplementation cancel = SchedulerTaskCancelSupportImpl.create(this);
    
    /**
     * Tracks the HintsProvider being executed, so it can be cancelled.
     */
    private volatile HintsProvider pendingProvider;

    public SelectionHintsTask() {
    }
    
    public @Override void run(ParserResult result, SchedulerEvent event) {

        final FileObject fileObject = result.getSnapshot().getSource().getFileObject();
        if (fileObject == null || cancel.isCancelled()) {
            return;
        }

        if (!(event instanceof CursorMovedSchedulerEvent) || cancel.isCancelled()) {
            return;
        }

        SpiSupportAccessor.getInstance().setCancelSupport(cancel);
        try {
            // Do we have a selection? If so, don't do suggestions
            CursorMovedSchedulerEvent evt = (CursorMovedSchedulerEvent) event;
            final int[] range = new int [] {
                Math.min(evt.getMarkOffset(), evt.getCaretOffset()),
                Math.max(evt.getMarkOffset(), evt.getCaretOffset())
            };
            if (range == null || range.length != 2 || range[0] == -1 || range[1] == -1 || range[0] == range[1]) {
                HintsController.setErrors(fileObject, SelectionHintsTask.class.getName(), Collections.<ErrorDescription>emptyList());
                return;
            }

            try {
                ParserManager.parse(Collections.singleton(result.getSnapshot().getSource()), new UserTask() {
                    public @Override void run(ResultIterator resultIterator) throws Exception {
                        Parser.Result r = resultIterator.getParserResult(range[0]);
                        if(!(r instanceof ParserResult)) {
                            return ;
                        }
                        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(r.getSnapshot().getMimeType());
                        if (language == null || cancel.isCancelled()) {
                            return;
                        }

                        HintsProvider provider = language.getHintsProvider();
                        if (provider == null || cancel.isCancelled()) {
                            return;
                        }

                        GsfHintsManager manager = language.getHintsManager();
                        if (manager == null || cancel.isCancelled()) {
                            return;
                        }

                        List<ErrorDescription> description = new ArrayList<ErrorDescription>();
                        List<Hint> hints = new ArrayList<Hint>();

                        RuleContext ruleContext = manager.createRuleContext((ParserResult) r, language, -1, range[0], range[1]);
                        if (ruleContext != null) {
                            try {
                                synchronized (this) {
                                    pendingProvider = provider;
                                    if (cancel.isCancelled()) {
                                        return;
                                    }
                                }
                                provider.computeSelectionHints(manager, ruleContext, hints, range[0], range[1]);
                            } finally {
                                pendingProvider = null;
                            }

                            for (int i = 0; i < hints.size(); i++) {
                                Hint hint= hints.get(i);

                                if (cancel.isCancelled()) {
                                    return;
                                }

                                ErrorDescription desc = manager.createDescription(hint, ruleContext, false, i == hints.size()-1);
                                description.add(desc);
                            }
                        }

                        if (cancel.isCancelled()) {
                            return;
                        }

                        HintsController.setErrors(fileObject, SelectionHintsTask.class.getName(), description);
                    }
                });
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
            }
        } finally {
            SpiSupportAccessor.getInstance().removeCancelSupport(cancel);
        }
    }

    public @Override int getPriority() {
        return Integer.MAX_VALUE;
    }

    public @Override Class<? extends Scheduler> getSchedulerClass() {
        // XXX: this should be in fact EDITOR_SELECTION_TASK_SCHEDULER, but we dont' have this
        // in Parsing API yet
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    public @Override void cancel() {
        final HintsProvider p = pendingProvider;
        if (p != null) {
            p.cancel();
        }
    }
}

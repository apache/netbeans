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
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.core.CancelSupportImplementation;
import org.netbeans.modules.csl.core.SchedulerTaskCancelSupportImpl;
import org.netbeans.modules.csl.core.SpiSupportAccessor;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;

/**
 * Task which delegates to the language plugins for actual hints-computation
 * 
 * @author Tor Norbye
 */
public final class HintsTask extends ParserResultTask<ParserResult> {

    private static final Logger LOG = Logger.getLogger(HintsTask.class.getName());
    private final CancelSupportImplementation cancel = SchedulerTaskCancelSupportImpl.create(this);

    /**
     * Tracks the HintsProvider being executed, so it can be cancelled.
     */
    private volatile HintsProvider pendingProvider;

    public HintsTask() {
    }
    
    public @Override void run(ParserResult result, SchedulerEvent event) {
        final List<ErrorDescription> descriptions = new ArrayList<>();
        SpiSupportAccessor.getInstance().setCancelSupport(cancel);
        try {
            ParserManager.parse(Collections.singleton(result.getSnapshot().getSource()), new UserTask() {
                public @Override void run(ResultIterator resultIterator) throws ParseException {
                    if (resultIterator == null) {
                        return;
                    }
                    
                    for(Embedding e : resultIterator.getEmbeddings()) {
                        run(resultIterator.getResultIterator(e));
                    }
                    
                    Language language = LanguageRegistry.getInstance().getLanguageByMimeType(resultIterator.getSnapshot().getMimeType());
                    if (language == null) {
                        return;
                    }

                    HintsProvider provider = language.getHintsProvider();

                    if (provider == null) {
                        return;
                    }

                    GsfHintsManager manager = language.getHintsManager();
                    if (manager == null) {
                        return;
                    }
                    
                    Parser.Result r = resultIterator.getParserResult();
                    if (!(r instanceof ParserResult)) {
                        return;
                    }

                    ParserResult parserResult = (ParserResult) r;
                    RuleContext ruleContext = manager.createRuleContext(parserResult, language, -1, -1, -1);
                    if (ruleContext == null) {
                        return;
                    }
                    
                    List<Hint> hints = new ArrayList<Hint>();
                    try {
                        synchronized (HintsTask.this) {
                            pendingProvider = provider;
                            if (cancel.isCancelled()) {
                                return;
                            }
                        }
                        provider.computeHints(manager, ruleContext, hints);
                    } finally {
                        pendingProvider = null;
                    }

                    if (cancel.isCancelled()) {
                        return;
                    }

                    for (int i = 0; i < hints.size(); i++) {
                        Hint hint = hints.get(i);
                        ErrorDescription desc = manager.createDescription(hint, ruleContext, false, i == hints.size()-1);
                        descriptions.add(desc);
                    }
                }
            });
        } catch (ParseException e) {
            LOG.log(Level.WARNING, null, e);
        } finally {
            SpiSupportAccessor.getInstance().removeCancelSupport(cancel);
        }
        HintsController.setErrors(result.getSnapshot().getSource().getFileObject(), HintsTask.class.getName(), descriptions);
    }

    public @Override int getPriority() {
        return Integer.MAX_VALUE;
    }

    public @Override Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    public @Override void cancel() {
        final HintsProvider proc = pendingProvider;
        if (proc != null) {
            proc.cancel();
        }
    }
}

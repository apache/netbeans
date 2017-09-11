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

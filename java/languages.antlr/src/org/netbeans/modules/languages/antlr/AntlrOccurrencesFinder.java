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
package org.netbeans.modules.languages.antlr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 *
 * @author lkishalmi
 */
public class AntlrOccurrencesFinder extends OccurrencesFinder<AntlrParserResult> {

    private int caretPosition;
    private boolean cancelled;
    private final Map<OffsetRange, ColoringAttributes> occurrences = new HashMap<>();

    @Override
    public void setCaretPosition(int position) {
        caretPosition = position;
    }

    @Override
    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return occurrences;
    }

    @Override
    public void run(AntlrParserResult result, SchedulerEvent event) {
        occurrences.clear();
        if (checkAndResetCancel()) {
            return;
        }
        computeOccurrences(result);
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }


    private boolean checkAndResetCancel() {
        if (cancelled) {
            cancelled = false;
            return true;
        }
        return false;
    }

    private void computeOccurrences(AntlrParserResult<?> result) {
        TokenHierarchy<?> tokenHierarchy = result.getSnapshot().getTokenHierarchy();
        TokenSequence<?> ts = tokenHierarchy.tokenSequence();
        ts.move(caretPosition);
        if (ts.movePrevious()) {
            ts.moveNext();
            Token<?> token = ts.token();
            if (token.id() == AntlrTokenId.RULE || token.id() == AntlrTokenId.TOKEN) {
                String refName = String.valueOf(token.text());
                for (OffsetRange occurance : result.getOccurrences(refName)) {
                    occurrences.put(occurance, ColoringAttributes.MARK_OCCURRENCES);
                }
            }
        }
    }
}

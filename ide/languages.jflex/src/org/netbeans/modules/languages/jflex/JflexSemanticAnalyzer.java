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
package org.netbeans.modules.languages.jflex;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.languages.jflex.parsing.JflexParserResult;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

public class JflexSemanticAnalyzer extends SemanticAnalyzer<JflexParserResult> {

    private boolean cancelled;
    private final Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights = new HashMap<>();
    public static final EnumSet<ColoringAttributes> MARK_OCCURENCES_SET = EnumSet.of(ColoringAttributes.CUSTOM1);

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    @Override
    public void run(JflexParserResult result, SchedulerEvent se) {
        resume();

        if (isCancelled()) {
            return;
        }

        if (result == null) {
            return;
        }
        semanticHighlights.clear();
        for (OffsetRange javaEmbededRange : result.getEmbededJavaCodeOffsets()) {
            semanticHighlights.put(javaEmbededRange, MARK_OCCURENCES_SET);
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }
}

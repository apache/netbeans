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
package org.netbeans.modules.languages.hcl.terraform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.languages.hcl.ast.HCLBlock;
import org.netbeans.modules.languages.hcl.ast.HCLIdentifier;
import org.netbeans.modules.languages.hcl.ast.SourceRef;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 *
 * @author lkishalmi
 */
public final class TerraformSemanticAnalyzer extends SemanticAnalyzer<TerraformParserResult> {

    private volatile boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    @Override
    public void run(TerraformParserResult result, SchedulerEvent event) {
        resume();

        if (isCancelled()) {
            return;
        }
        Map<OffsetRange, Set<ColoringAttributes>> highlights = new HashMap<>();
        SourceRef refs = result.getReferences();
        for (HCLBlock block : result.getDocument().getBlocks()) {
            List<HCLIdentifier> decl = block.getDeclaration();
            HCLIdentifier type = decl.get(0);

            TerraformParserResult.BlockType bt = TerraformParserResult.BlockType.get(type.id());
            if (bt != null) {
                refs.getOffsetRange(type).ifPresent((range) -> highlights.put(range, ColoringAttributes.METHOD_SET));
            }
        }
        semanticHighlights = highlights;
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

    
}

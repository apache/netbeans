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
package org.netbeans.modules.languages.hcl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.languages.hcl.ast.HCLAttribute;
import org.netbeans.modules.languages.hcl.ast.HCLBlock;
import org.netbeans.modules.languages.hcl.ast.HCLCollection;
import org.netbeans.modules.languages.hcl.ast.HCLElement;
import org.netbeans.modules.languages.hcl.ast.HCLFunction;
import org.netbeans.modules.languages.hcl.ast.HCLIdentifier;
import org.netbeans.modules.languages.hcl.ast.HCLTreeWalker;
import org.netbeans.modules.languages.hcl.ast.HCLVariable;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 *
 * @author lkishalmi
 */
public class HCLSemanticAnalyzer extends SemanticAnalyzer<HCLParserResult> {
    private volatile boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> highlights = Collections.emptyMap();
    
    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return highlights;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    @Override
    public final void run(HCLParserResult result, SchedulerEvent event) {
        resume();

        Highlighter h = createHighlighter(result);
        highlights = h.process(result.getDocument());
    }

    protected Highlighter createHighlighter(HCLParserResult result) {
        return new DefaultHighlighter(result.getReferences());
    }
    
    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }
    
    protected abstract class Highlighter {
        protected final Map<OffsetRange, Set<ColoringAttributes>> work = new HashMap<>();

        protected final SourceRef refs;
        protected Highlighter(SourceRef refs) {
            this.refs = refs;
        }

        protected abstract void highlight(HCLTreeWalker.Step step);

        private boolean cancellableHighlight(HCLTreeWalker.Step step) {
            if (isCancelled()) {
                return false;
            }
            highlight(step);
            return true;
        }

        public Map<OffsetRange, Set<ColoringAttributes>> process(HCLElement element) {
            HCLTreeWalker.depthFirst(element, this::cancellableHighlight);
            return work;
        }
        
        protected final void mark(HCLElement e, Set<ColoringAttributes> attrs) {
            refs.getOffsetRange(e).ifPresent((range) -> work.put(range, attrs));
        }

    }
    
    protected class DefaultHighlighter extends Highlighter {

        public DefaultHighlighter(SourceRef refs) {
            super(refs);
        }
        
        @Override
        protected void highlight(HCLTreeWalker.Step step) {

            // TODO: Can use record patterns from Java 21
            HCLElement e = step.node();
            if (e instanceof HCLBlock block && step.depth() == 1) {
                List<HCLIdentifier> decl = block.declaration();
                HCLIdentifier type = decl.get(0);

                mark(type, ColoringAttributes.CLASS_SET);
                if (decl.size() > 1) {
                    for (int i = 1; i < decl.size(); i++) {
                        HCLIdentifier id = decl.get(i);
                        mark(id, ColoringAttributes.CONSTRUCTOR_SET);
                    }
                }
            } else if (e instanceof HCLAttribute attr) {
                mark(attr.name(), ColoringAttributes.FIELD_SET);
            } else if (e instanceof HCLFunction func) {
                mark(func.name(), ColoringAttributes.CONSTRUCTOR_SET);
            } else if (e instanceof HCLCollection.Object obj) {
                for (HCLCollection.ObjectElement oe : obj.elements()) {
                    if (oe.key() instanceof HCLVariable) {
                        mark(oe.key(), ColoringAttributes.FIELD_SET);
                    }
                }
            }
        }        
    }
}

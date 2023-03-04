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
package org.netbeans.modules.languages.yaml;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 * Semantic Analyzer for YAML
 *
 * @author Tor Norbye
 */
public class YamlSemanticAnalyzer extends SemanticAnalyzer {

    private boolean cancelled;
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
    public void cancel() {
        cancelled = true;
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
    public void run(Result result, SchedulerEvent event) {
        resume();

        if (isCancelled()) {
            return;
        }

        YamlParserResult ypr = (YamlParserResult) result;
        if (ypr == null || ypr.getItems().isEmpty()) {
            this.semanticHighlights = Collections.emptyMap();
            return;
        }

        Map<OffsetRange, Set<ColoringAttributes>> highlights =
                new HashMap<OffsetRange, Set<ColoringAttributes>>(100);

        for (StructureItem item : ypr.getItems()) {
            YamlStructureItem yamlItem = (YamlStructureItem) item;
            addHighlights(yamlItem, highlights);
        }

        this.semanticHighlights = highlights;
    }

    private void addHighlights(YamlStructureItem item, Map<OffsetRange, Set<ColoringAttributes>> highlights) {
        switch (item.getType()) {
            case MAP:                
            case SEQUENCE:
                YamlStructureItem.Collection coll = (YamlStructureItem.Collection) item;
                for (YamlStructureItem child : coll.getChildren()) {
                    addHighlights(child, highlights);
                }
                break;
            case MAPPING:
                YamlStructureItem.MapEntry entry = (YamlStructureItem.MapEntry) item;
                if (entry.keyItem.getType() == YamlStructureItem.NodeType.SCALAR) {
                    highlights.put(getAstRange(entry.keyItem), ColoringAttributes.METHOD_SET);
                } else {
                    addHighlights(entry.keyItem, highlights);
                }
                addHighlights(entry.valueItem, highlights);
                break;
        }
    }

    private static OffsetRange getAstRange(YamlStructureItem item) {
        int s = (int) item.getPosition();
        int e = (int) item.getEndPosition();
        return new OffsetRange(s, e);
    }

}

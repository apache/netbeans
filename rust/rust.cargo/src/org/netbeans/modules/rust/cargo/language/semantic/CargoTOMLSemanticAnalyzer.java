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
package org.netbeans.modules.rust.cargo.language.semantic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.rust.cargo.language.CargoTOMLLanguageParser;
import org.netbeans.modules.rust.cargo.language.antlr4.TOMLParser;
import org.netbeans.modules.rust.cargo.language.antlr4.TOMLParser.KeyContext;

/**
 *
 * @author antonio
 */
public class CargoTOMLSemanticAnalyzer extends SemanticAnalyzer<CargoTOMLLanguageParser.CargoTOMLLanguageParserResult> {

    private boolean cancelled = false;
    Map<OffsetRange, Set<ColoringAttributes>> highlights = new HashMap<>();

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return highlights;
    }

    @Override
    public void run(CargoTOMLLanguageParser.CargoTOMLLanguageParserResult result, SchedulerEvent event) {
        synchronized (this) {
            if (cancelled) {
                return;
            }
        }
        TOMLParser.DocumentContext document = result.getDocument();
        if (document == null) {
            return;
        }
        highlights.clear();
        for (TOMLParser.ExpressionContext expression : document.expression()) {
            synchronized (this) {
                if (cancelled) {
                    break;
                }
            }
            // We only highlight keys in [tables] and [[arrays]]
            TOMLParser.TableContext tableContext = expression.table();
            if (tableContext != null) {
                // [[key]]
                TOMLParser.Array_tableContext arrayTable = tableContext.array_table();
                if (arrayTable != null) {
                    KeyContext key = arrayTable.key();
                    if (key != null) {
                        addHilightForKey(key);
                    }
                }
                // [key]
                TOMLParser.Standard_tableContext standardTable = tableContext.standard_table();
                if (standardTable != null) {
                    KeyContext key = standardTable.key();
                    if (key != null) {
                        addHilightForKey(key);
                    }
                }
            }
        }
    }

    private void addHilightForKey(KeyContext key) {
        int startIndex = key.start.getStartIndex();
        int stopIndex = key.stop.getStopIndex() + 1;
        OffsetRange range = new OffsetRange(startIndex, stopIndex);
        Set<ColoringAttributes> keyColoring = new HashSet<>();
        highlights.put(range, ColoringAttributes.GLOBAL_SET);
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
        synchronized (this) {
            cancelled = true;
        }
    }

}

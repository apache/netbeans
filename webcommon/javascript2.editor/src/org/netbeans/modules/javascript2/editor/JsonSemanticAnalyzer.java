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
package org.netbeans.modules.javascript2.editor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.javascript2.editor.parser.JsonParserResult;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.JsReference;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

public class JsonSemanticAnalyzer extends SemanticAnalyzer<JsonParserResult> {

    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;
    private volatile boolean canceled;

    public JsonSemanticAnalyzer() {
        this.canceled = false;
        this.semanticHighlights = Collections.emptyMap();
    }

    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    @Override
    public void run(JsonParserResult result, SchedulerEvent event) {
        canceled = false;
        
        if (canceled) {
            return;
        }

        Map<OffsetRange, Set<ColoringAttributes>> highlights = new HashMap<>(100);
        Model model = Model.getModel(result, false);
        highlights = count(result, model.getGlobalObject(), highlights, new HashSet<>());

        assert highlights != null;

        semanticHighlights = highlights;
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private Map<OffsetRange, Set<ColoringAttributes>> count (JsonParserResult result, JsObject parent, Map<OffsetRange, Set<ColoringAttributes>> highlights, Set<String> processedObjects) {
        if (ModelUtils.wasProcessed(parent, processedObjects)) {
            return highlights;
        }
        for (JsObject object : parent.getProperties().values()) {
            if (object.getDeclarationName() != null) {
                switch (object.getJSKind()) {
                    case OBJECT_LITERAL -> {
                        if(object.getDeclarationName() != null) {
                            addColoring(result, highlights, object.getDeclarationName().getOffsetRange(), ColoringAttributes.FIELD_SET);
                        }
                    }
                    case PROPERTY -> addColoring(result, highlights, object.getDeclarationName().getOffsetRange(), ColoringAttributes.FIELD_SET);
                }
            }
            if (canceled) {
                highlights = Collections.emptyMap();
                break;
            }
            if (!(object instanceof JsReference jr && ModelUtils.isDescendant(object, jr.getOriginal()))) {
                highlights = count(result, object, highlights, processedObjects);
            }
        }

        return highlights;
    }

    private void addColoring(JsonParserResult result, Map<OffsetRange, Set<ColoringAttributes>> highlights, OffsetRange astRange, Set<ColoringAttributes> coloring) {
        int start = result.getSnapshot().getOriginalOffset(astRange.getStart());
        int end = result.getSnapshot().getOriginalOffset(astRange.getEnd());
        if (start > -1 && end > -1 && start < end) {
            OffsetRange range = start == astRange.getStart() ? astRange : new OffsetRange(start, end);
            highlights.put(range, coloring);
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
        this.canceled = true;
    }

}

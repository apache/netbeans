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
package org.netbeans.modules.php.blade.editor;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.php.blade.editor.directives.CustomDirectives;
import org.netbeans.modules.php.blade.editor.parser.BladeParserResult;
import org.netbeans.modules.php.blade.editor.parser.BladeParserResult.Reference;
import org.netbeans.modules.php.blade.project.ProjectUtils;
import org.openide.filesystems.FileObject;

/**
 * coloring configured in fonts and colors
 *
 * @author bhaidu
 */
public class BladeSemanticAnalyzer extends SemanticAnalyzer<BladeParserResult> {

    private boolean cancelled;
    public static final EnumSet<ColoringAttributes> UNDEFINED_FIELD_SET = EnumSet.of(ColoringAttributes.UNDEFINED);
    public static final EnumSet<ColoringAttributes> CUSTOM_DIRECTIVE_SET = EnumSet.of(ColoringAttributes.DECLARATION);
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
    public void run(BladeParserResult parserResult, SchedulerEvent event) {
        resume();

        if (isCancelled()) {
            return;
        }

        Map<OffsetRange, Set<ColoringAttributes>> highlights
                = new HashMap<>();
        FileObject fo = parserResult.getFileObject();
        Project project = ProjectUtils.getMainOwner(fo);
        CustomDirectives ct = CustomDirectives.getInstance(project);
        for (Map.Entry<OffsetRange, Reference> entry : parserResult.customDirectivesReferences.entrySet()) {
            if (entry.getValue().type == BladeParserResult.ReferenceType.POSSIBLE_DIRECTIVE && ct.customDirectiveConfigured(entry.getValue().identifier) ) {
                highlights.put(entry.getKey(), CUSTOM_DIRECTIVE_SET);
                continue;
            }
            highlights.put(entry.getKey(), UNDEFINED_FIELD_SET);
        }

//        List<? extends org.netbeans.modules.csl.api.Error> errorList = parserResult.getDiagnostics();
//        for (org.netbeans.modules.csl.api.Error error : errorList) {
//            OffsetRange range = new OffsetRange(error.getStartPosition(), error.getEndPosition());
//            highlights.put(range, UNDEFINED_FIELD_SET);
//        }
        this.semanticHighlights = highlights;
    }

}

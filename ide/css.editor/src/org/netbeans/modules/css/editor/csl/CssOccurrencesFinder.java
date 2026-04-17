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

package org.netbeans.modules.css.editor.csl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FeatureCancel;
import org.netbeans.modules.css.editor.options.MarkOccurencesSettings;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 * @author mfukala@netbeans.org
 */
public class CssOccurrencesFinder extends OccurrencesFinder<CssParserResult> {

    private int caretDocumentPosition;
    private Map<OffsetRange, ColoringAttributes> occurrencesMap = Collections.emptyMap();
    private FeatureCancel featureCancel = new FeatureCancel();

    @Override
    public void setCaretPosition(int position) {
        caretDocumentPosition = position;
    }

    @Override
    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return occurrencesMap;
    }

    @Override
    public void cancel() {
        if(featureCancel != null) {
            featureCancel.cancel();
        }
    }

    private void resume() {
        featureCancel = new FeatureCancel();
    }

    @Override
    public void run(CssParserResult parserResultWrapper, SchedulerEvent event) {
        resume();

        try {
            EditorFeatureContext context = new EditorFeatureContext(parserResultWrapper, caretDocumentPosition);
            Set<OffsetRange> occurrences = CssModuleSupport.getMarkOccurrences(context, featureCancel);

            if(featureCancel.isCancelled()) {
                return ;
            }

            Map<OffsetRange, ColoringAttributes> occurrencesMapLocal = new HashMap<>();
            for(OffsetRange range : occurrences) {
                occurrencesMapLocal.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }

            occurrencesMap = occurrencesMapLocal;
        } finally {
            featureCancel = null;
        }
    }

    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return null;
    }

    @Override
    public boolean isKeepMarks() {
        return MarkOccurencesSettings
                .getCurrentNode()
                .getBoolean(MarkOccurencesSettings.KEEP_MARKS, true);
    }

    @Override
    public boolean isMarkOccurrencesEnabled() {
        return MarkOccurencesSettings
                .getCurrentNode()
                .getBoolean(MarkOccurencesSettings.ON_OFF, true);
    }

}

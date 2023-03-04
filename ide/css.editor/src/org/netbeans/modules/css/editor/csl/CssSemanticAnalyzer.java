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
package org.netbeans.modules.css.editor.csl;

import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.module.spi.FeatureCancel;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssSemanticAnalyzer extends SemanticAnalyzer {

    private FeatureCancel featureCancel = new FeatureCancel();
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    @Override
    public void cancel() {
        if(featureCancel != null) {
            featureCancel.cancel();
        }
    }

    @Override
    public void run(Result result, SchedulerEvent event) {
        resume();
        
        try {
            CssParserResult wrappedResult = (CssParserResult) result;
            FeatureContext featureContext = new FeatureContext(wrappedResult);
            semanticHighlights = CssModuleSupport.getSemanticHighlights(featureContext, featureCancel);
        } finally {
            featureCancel = null;
        }
    }

    @Override
    public int getPriority() {
        return 500; //higher means less important
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return null; 
    }

    private void resume() {
        featureCancel = new FeatureCancel();
    }
}

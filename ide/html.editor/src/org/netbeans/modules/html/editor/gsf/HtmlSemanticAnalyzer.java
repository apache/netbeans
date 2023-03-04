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
package org.netbeans.modules.html.editor.gsf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.html.editor.HtmlExtensions;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.web.common.api.WebPageMetadata;

/**
 *
 * @author marek
 */
public class HtmlSemanticAnalyzer extends SemanticAnalyzer {

    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public void run(Result result, SchedulerEvent event) {
        cancelled = false; //resume
        final Map<OffsetRange, Set<ColoringAttributes>> highlights = new HashMap<>();
        HtmlParserResult htmlResult = (HtmlParserResult) result;

        String sourceMimetype = WebPageMetadata.getContentMimeType(result, true);
        //process extensions
        for (HtmlExtension ext : HtmlExtensions.getRegisteredExtensions(sourceMimetype)) {
            if (cancelled) {
                return;
            }
            highlights.putAll(ext.getHighlights(htmlResult, event));
        }

        semanticHighlights = highlights;

    }

    @Override
    public int getPriority() {
        return 500; //XXX find out some reasonable number
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return null; //todo  what to return????
    }
}

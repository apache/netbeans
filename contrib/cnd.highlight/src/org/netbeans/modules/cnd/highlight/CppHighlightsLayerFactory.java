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

package org.netbeans.modules.cnd.highlight;

import org.netbeans.modules.cnd.highlight.semantic.MarkOccurrencesHighlighter;
import org.netbeans.modules.cnd.highlight.semantic.SemanticHighlighter;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 */
public class CppHighlightsLayerFactory implements HighlightsLayerFactory {
    private final static boolean SEMANTIC_DISABLED = Boolean.getBoolean("cnd.semantic.disabled"); // NOI18N
    
    @Override
    public HighlightsLayer[] createLayers(Context context) {
        if (SEMANTIC_DISABLED) {
            return new HighlightsLayer[]{
                        HighlightsLayer.create(
                        MarkOccurrencesHighlighter.class.getName(),
                        ZOrder.CARET_RACK.forPosition(1000),
                        true,
                        MarkOccurrencesHighlighter.getHighlightsBag(context.getDocument())),};
        } else {
        return 
            new HighlightsLayer[] {
                HighlightsLayer.create(
                    SemanticHighlighter.class.getName() + "Slow", // NOI18N
                    ZOrder.SYNTAX_RACK.forPosition(2000),
                    true,
                    SemanticHighlighter.getHighlightsBag(context.getDocument(), false)),
                HighlightsLayer.create(
                    SemanticHighlighter.class.getName() + "Fast", // NOI18N
                    ZOrder.SYNTAX_RACK.forPosition(1500),
                    true,
                    SemanticHighlighter.getHighlightsBag(context.getDocument(), true)),
                HighlightsLayer.create(
                    MarkOccurrencesHighlighter.class.getName(), 
                    ZOrder.CARET_RACK.forPosition(1000),
                    true,
                    MarkOccurrencesHighlighter.getHighlightsBag(context.getDocument())),
            };
        }
    }
}

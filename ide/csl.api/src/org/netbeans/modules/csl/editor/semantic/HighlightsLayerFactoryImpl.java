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
package org.netbeans.modules.csl.editor.semantic;

import org.netbeans.modules.csl.editor.InstantRenamePerformer;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory.Context;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Jan Lahoda
 */
public class HighlightsLayerFactoryImpl implements HighlightsLayerFactory {
    
    @Override
    public HighlightsLayer[] createLayers(Context context) {
        //LexerBasedHighlightLayer semantic = LexerBasedHighlightLayer.getLayer(SemanticHighlighter.class, context.getDocument());
        GsfSemanticLayer semantic = GsfSemanticLayer.getLayer(SemanticHighlighter.class, context.getDocument());
        GsfSemanticLayer occurrences = (GsfSemanticLayer)MarkOccurrencesHighlighter.getHighlightsBag(context.getDocument());
        semantic.clearColoringCache();
        
        return new HighlightsLayer[] {
            HighlightsLayer.create(SemanticHighlighter.class.getName() + "-1", ZOrder.SYNTAX_RACK.forPosition(1000), false, semantic),
            //the mark occurrences layer should be "above" current row and "below" the search layers:
            HighlightsLayer.create(MarkOccurrencesHighlighter.class.getName(), ZOrder.CARET_RACK.forPosition(50), false, occurrences),
            //"above" mark occurrences, "below" search layers:
            HighlightsLayer.create(InstantRenamePerformer.class.getName(), ZOrder.CARET_RACK.forPosition(75), false, InstantRenamePerformer.getHighlightsBag(context.getDocument())),
        };
    }

}

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

package org.netbeans.modules.editor.lib2.highlighting;

import java.util.ArrayList;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 * The factory for editor default highlighting layers.
 * 
 * @author Vita Stejskal
 */
public class Factory implements HighlightsLayerFactory {

    /** A unique identifier of the block search layer type. */
    public static final String BLOCK_SEARCH_LAYER = "org.netbeans.modules.editor.lib2.highlighting.BlockHighlighting/BLOCK_SEARCH"; //NOI18N
    
    /** A unique identifier of the incremental search layer type. */
    public static final String INC_SEARCH_LAYER = "org.netbeans.modules.editor.lib2.highlighting.BlockHighlighting/INC_SEARCH"; //NOI18N
    
    /** Creates a new instance of Factory */
    public Factory() {
    }

    public HighlightsLayer[] createLayers(HighlightsLayerFactory.Context context) {
        ArrayList<HighlightsLayer> layers = new ArrayList<HighlightsLayer>();
        
        layers.add(HighlightsLayer.create(
            ReadOnlyFilesHighlighting.LAYER_TYPE_ID,
            ZOrder.BOTTOM_RACK.forPosition(-1000),
            true,
            new ReadOnlyFilesHighlighting(context.getDocument()))
        );

        layers.add(HighlightsLayer.create(
            CaretBasedBlockHighlighting.CaretRowHighlighting.LAYER_TYPE_ID,
            ZOrder.CARET_RACK,
            true,
            new CaretBasedBlockHighlighting.CaretRowHighlighting(context.getComponent()))
        );

        layers.add(HighlightsLayer.create(
            INC_SEARCH_LAYER, 
            ZOrder.SHOW_OFF_RACK.forPosition(300),
            true,
            new BlockHighlighting(INC_SEARCH_LAYER, context.getComponent()))
        );

        layers.add(HighlightsLayer.create(
            CaretBasedBlockHighlighting.TextSelectionHighlighting.LAYER_TYPE_ID,
            ZOrder.SHOW_OFF_RACK.forPosition(500), 
            true, 
            new CaretBasedBlockHighlighting.TextSelectionHighlighting(context.getComponent()))
        );

        // If there is a lexer for the document create lexer-based syntax highlighting
        if (TokenHierarchy.get(context.getDocument()) != null) {
            layers.add(HighlightsLayer.create(
                SyntaxHighlighting.LAYER_TYPE_ID,
                ZOrder.SYNTAX_RACK,
                true,
                new SyntaxHighlighting(context.getDocument()))
            );
        }
        
        layers.add(HighlightsLayer.create(
            WhitespaceHighlighting.LAYER_TYPE_ID,
            ZOrder.CARET_RACK.forPosition(-100), // Below CaretRowHighlighting
            true, // fixed size
            new WhitespaceHighlighting(context.getComponent()))
        );
        
        layers.add(HighlightsLayer.create(
            CaretOverwriteModeHighlighting.LAYER_TYPE_ID,
            ZOrder.TOP_RACK.forPosition(100),
            true, // fixed size
            new CaretOverwriteModeHighlighting(context.getComponent()))
        );

        return layers.toArray(new HighlightsLayer[0]);
    }
    
}

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
package org.netbeans.modules.merge.builtin.visualizer;

import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 * Provides highlighting in Merge dialogs.
 * 
 * @author Ondra Vrabec
 */
public class MergeHighlightsLayerFactory implements HighlightsLayerFactory {

    static final String HIGHLITING_LAYER_ID = "org.netbeans.modules.merge.builtin.visualizer.MergePanel"; // NOI18N
    
    @Override
    public HighlightsLayer[] createLayers(Context context) {
        MergePane master = (MergePane) context.getComponent().getClientProperty(HIGHLITING_LAYER_ID);
        if (master == null) return null;
        
        HighlightsLayer [] layers = new HighlightsLayer[1];
        layers[0] = HighlightsLayer.create(HIGHLITING_LAYER_ID, ZOrder.DEFAULT_RACK, true, master);
        return layers;
    }
}

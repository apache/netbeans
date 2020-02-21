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


package org.netbeans.modules.cnd.asm.core.assistance;


import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

public class CodeHighlightFactory implements HighlightsLayerFactory  {
    
    private static final String ASM_FAST_HIGHLIGHT_LAYER_ID =
            "asm-fast-highlight-layer-id"; // NOI18N
    
    public HighlightsLayer[] createLayers(Context ctx) {
        JTextComponent pane = ctx.getComponent();
        Document doc = ctx.getDocument();
        
        HighlightsLayer layer = HighlightsLayer.create(ASM_FAST_HIGHLIGHT_LAYER_ID, 
                   ZOrder.TOP_RACK, false, new RegisterHighlightAssistance(pane, doc));
        
        return new HighlightsLayer[] { layer };
    }            
}

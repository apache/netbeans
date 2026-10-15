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

package org.netbeans.modules.editor.impl.highlighting;

import java.util.ArrayList;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 * @author Vita Stejskal
 */
public final class HLFactory implements HighlightsLayerFactory {
    
    /** Creates a new instance of HLFactory */
    public HLFactory() {
    }

    public HighlightsLayer[] createLayers(HighlightsLayerFactory.Context context) {
        ArrayList<HighlightsLayer> layers = new ArrayList<HighlightsLayer>();
        
        final Document d = context.getDocument();
        final JTextComponent c = context.getComponent();
        final String mimeType = getMimeType(c, d);
        
        layers.add(HighlightsLayer.create(
            GuardedBlocksHighlighting.LAYER_TYPE_ID, 
            ZOrder.BOTTOM_RACK, 
            true,  // fixedSize
            new GuardedBlocksHighlighting(d, mimeType)
        ));
        
        layers.add(HighlightsLayer.create(
            ComposedTextHighlighting.LAYER_TYPE_ID, 
            ZOrder.TOP_RACK, 
            true,  // fixedSize
            new ComposedTextHighlighting(c, d, mimeType)
        ));

        layers.add(HighlightsLayer.create(
            AnnotationsHighlighting.LAYER_TYPE_ID,
            ZOrder.DEFAULT_RACK,
            true,  // fixedSize
            new AnnotationsHighlighting(d)
        ));


        if (!new TokenHierarchyActiveRunnable(context.getDocument()).isActive()) {
            // There is no lexer yet, we will use this layer for backwards compatibility
            layers.add(HighlightsLayer.create(
                NonLexerSyntaxHighlighting.LAYER_TYPE_ID, 
                ZOrder.SYNTAX_RACK, 
                true,  // fixedSize
                new NonLexerSyntaxHighlighting(d, mimeType)
            ));
        }
        
        return layers.toArray(new HighlightsLayer[0]);
    }
    
    private static String getMimeType(JTextComponent c, Document d) {
        String mimeType = (String) d.getProperty("mimeType"); //NOI18N
        
        if (mimeType == null) {
            mimeType = c.getUI().getEditorKit(c).getContentType();
        }
        
        return mimeType == null ? "" : mimeType; //NOI18N
    }

    private static final class TokenHierarchyActiveRunnable implements Runnable {

        private Document doc;

        private boolean tokenHierarchyActive;

        TokenHierarchyActiveRunnable(Document doc) {
            this.doc = doc;
        }

        boolean isActive() {
            doc.render(this);
            return tokenHierarchyActive;
        }

        public void run() {
            tokenHierarchyActive = TokenHierarchy.get(doc).isActive();
        }

    }

}

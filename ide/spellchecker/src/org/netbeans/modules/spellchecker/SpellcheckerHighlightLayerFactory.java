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
package org.netbeans.modules.spellchecker;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;

/**
 *
 * @author Jan Lahoda
 */
public class SpellcheckerHighlightLayerFactory implements HighlightsLayerFactory {
    
    public SpellcheckerHighlightLayerFactory() {
    }
    
    public HighlightsLayer[] createLayers(Context ctx) {
        OffsetsBag bag = getBag(ctx.getComponent());
        return new HighlightsLayer[] {
            HighlightsLayer.create(SpellcheckerHighlightLayerFactory.class.getName(), ZOrder.CARET_RACK.forPosition(30), true, bag),
        };
    }
    
    public static synchronized OffsetsBag getBag(JTextComponent component) {
        Document doc = component.getDocument();
        OffsetsBag bag = null;
        if (doc != null) {
            bag = (OffsetsBag) doc.getProperty(SpellcheckerHighlightLayerFactory.class);
            if (bag == null) {
                doc.putProperty(SpellcheckerHighlightLayerFactory.class, bag = new OffsetsBag(doc));
            }
        }
        Spellchecker.register (component);
        
        return bag;
    }
    
}

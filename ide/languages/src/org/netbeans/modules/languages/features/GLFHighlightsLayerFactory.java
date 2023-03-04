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
package org.netbeans.modules.languages.features;

import javax.swing.text.Document;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 * @author Jan Jancura
 */
public class GLFHighlightsLayerFactory implements HighlightsLayerFactory {

    public HighlightsLayer[] createLayers (Context context) {
        Document document = context.getDocument ();
//        try {
//            if (LanguagesManager.getDefault ().getLanguage (mimeType).getParser () == null)
//                return null;
            return new HighlightsLayer[] {
                HighlightsLayer.create (
                    "GLF Semantic Coloring", 
                    ZOrder.SYNTAX_RACK.forPosition (10), 
                    false, 
                    new SemanticHighlightsLayer (document)
                ),
                HighlightsLayer.create (
                    "GLF Languages Coloring", 
                    ZOrder.SYNTAX_RACK.forPosition (11), 
                    false, 
                    new LanguagesHighlightsLayer (document)
                ),
                HighlightsLayer.create (
                    "GLF Token Highlighting", 
                    ZOrder.SHOW_OFF_RACK.forPosition (0), 
                    false, 
                    new TokenHighlightsLayer (document)
                )
            };
//        } catch (LanguageDefinitionNotFoundException ex) {
//            return null;
//        }
    }
}

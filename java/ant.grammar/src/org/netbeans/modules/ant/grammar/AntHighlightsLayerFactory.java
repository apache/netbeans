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

package org.netbeans.modules.ant.grammar;

import javax.swing.text.AbstractDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 * Highlights Ant property values.
 */
@MimeRegistration(service=HighlightsLayerFactory.class, mimeType="text/x-ant+xml")
public class AntHighlightsLayerFactory implements HighlightsLayerFactory {

    public @Override HighlightsLayer[] createLayers(final Context context) {
        HighlightsLayer highlighter = HighlightsLayer.create(
            "ant",  // NOI18N
            ZOrder.SYNTAX_RACK.forPosition(10), 
            true, 
            new AntHighlightsContainer(
                (AbstractDocument) context.getDocument(),
                MimeLookup.getLookup("text/x-jsp").lookup(FontColorSettings.class).getTokenFontColors("expression-language") // NOI18N
            )
        );
        return new HighlightsLayer[] { highlighter };
    }

}

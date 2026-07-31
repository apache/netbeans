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
package org.netbeans.modules.php.blade.editor.highlighting;

import javax.swing.text.AbstractDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;

import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;


/**
 * hack to fix the highlighting issue on javascript properties vs blade paths
 * "@include('my.path') - my.path should be fully selected on double click
 * window.test - should not be fully selected on double click
 * 
 * @author bhaidu
 */
@MimeRegistration(service=HighlightsLayerFactory.class, mimeType=BladeLanguage.MIME_TYPE, position=200)
public class BladeHighlightsLayerFactory implements HighlightsLayerFactory {

    public @Override HighlightsLayer[] createLayers(final Context context) {
        return new HighlightsLayer[] {HighlightsLayer.create("blade", ZOrder.SYNTAX_RACK.forPosition(10), true,
                new BladeHighlightsContainer((AbstractDocument) context.getDocument()))}; 
    }

}

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
package org.netbeans.modules.cnd.editor.folding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;

/**
 * This fake factory is needed to add property change listener to {@link EditorRegistry}.
 * There is no better way to do it so far. See bug 173002.
 *
 * With the listener registered, every C/C++/H editor has code folding
 * controls right after it's opened. Without the listener, code folding
 * controls used to appear only after editing a file.
 *
 */
public final class FakeHighlightsFactory implements HighlightsLayerFactory, PropertyChangeListener {

    private static final HighlightsLayer[] EMPTY = {};

    public FakeHighlightsFactory() {
        EditorRegistry.addPropertyChangeListener(this);
    }

    @Override
    public HighlightsLayer[] createLayers(Context context) {
        return EMPTY;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Document docToReparse = null;
        if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
            docToReparse = ((JTextComponent) evt.getNewValue()).getDocument();
        } else if (EditorRegistry.FOCUSED_DOCUMENT_PROPERTY.equals(evt.getPropertyName())) {
            docToReparse = (Document) evt.getNewValue();
        }
        if (docToReparse != null && MIMENames.isHeaderOrCppOrC(DocumentUtilities.getMimeType(docToReparse))) {
            CppMetaModel.getDefault().scheduleParsing(docToReparse);
        }
    }
}

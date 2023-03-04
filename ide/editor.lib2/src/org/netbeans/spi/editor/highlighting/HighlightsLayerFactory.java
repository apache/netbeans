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

package org.netbeans.spi.editor.highlighting;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * Factory for producing <code>HighlightsLayer</code>s. Modules can implement
 * this interface and register their implementations among the mime-type specific
 * services under the <code>Editors</code> folder on the system filesystem.
 *
 * <p>The infrastructure uses <code>HighlightsLayerFactory</code> instances
 * registered in the mime-type specific registry to create <code>HighlightsLayer</code>s,
 * which will participate in rendering a document. All factories that the infrastructure
 * considers relelvant for rendering a document will be asked to create
 * <code>HighlightsLayer</code>s for that document.
 *
 * @author Miloslav Metelka
 * @author Vita Stejskal
 * @version 1.00
 */
public interface HighlightsLayerFactory {

    /**
     * The context passed to a factory when it is asked to create <code>HighlightLayer</code>s.
     * This context provides essential information such as a <code>Document</code> and
     * <code>JTextComponent</code> for which <code>HighlightLayer</code>s should be created.
     */
    public static final class Context {
        private Document document;
        private JTextComponent component;

        /**
         * TODO: we might need a MimePath here as well. Remember the layers can
         * be created/discarded depending on the appearance of embedded mime types
         * as the user changes the document. Such a layer (or the factory) might be interested in
         * knowing its mime-path, because it might need to load some settings at
         * the creation time when no other information (eg. lexer's language path)
         * is available.
         */
        /* package */ Context(Document document, JTextComponent component) {
            this.document = document;
            this.component = component;
        }

        /**
         * Gets the document for which the highlight layers are created.
         */
        public Document getDocument() {
            return document;
        }

        /**
         * Gets the text component for which the highlight layers are created.
         */
        public JTextComponent getComponent() {
            return component;
        }
    } // End of Context class
    
    /**
     * Creates <code>HighlightLayer</code>s appropriate for the context passed in.
     *
     * @param context    The context that should be used for creating the layer.
     *
     * @return The array of <code>HighlightLayer</code>s that should be used
     *  for rendering the document in the <code>Context</code>. This method can
     *  return <code>null</code> if there are no <code>HighlightLayer</code>s available.
     */
    HighlightsLayer[] createLayers(Context context);

}

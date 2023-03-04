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
package org.netbeans.spi.editor.fold;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Callback interface, which is called to extract description for a fold.
 * The Reader will be called to produce a preview text to be displayed in the
 * folded view. The reader will be called under document lock, although the fold
 * hierarchy will not be locked. You may query the fold properties, but may not
 * traverse the fold hierarchy.
 * <p/>
 * An instance of ContentReader is used repeatedly, i.e. if the same FoldTemplate
 * is assigned to multiple folds. It is advised that the implementation of ContentReader
 * is stateless.
 * 
 * @author sdedic
 * @since 1.35
 */
public interface ContentReader {
    /**
     * Acquires text for fold content.
     * The method is executed under <i>read lock</i> on the Document. However, the Fold Hierarchy
     * is not locked. Accessing fold offsets should be safe, but relationships with other Folds
     * (parents, children, root) are not guarded.
     * <p/>
     * If the ContentReader cannot extract the contents (i.e. it does not want to handle the fold), 
     * {@code null} may be returned. If more ContentReaders are registered, some other instance might
     * handle the fold properly. If not, the placeholder will be retained and presented in the fold 
     * text.
     * 
     * @param d the document to read from
     * @param f fold, whose contents should be read
     * @param ft the original fold template, if available
     * @return content to be displayed in the folded view, or {@code null} if unwilling to retrieve the content.
     */
    public CharSequence read(Document d, Fold f, FoldTemplate ft) throws BadLocationException;

    /**
     * Factory, which produces ContentReader instance(s) appropriate for the fold type.
     * The returned instance may be used to read contents for all folds of the given type, in 
     * different documents (of the same mime type).
     */
    @MimeLocation(subfolderName = "FoldManager")
    public interface Factory {
        /**
         * @param ft the fold type
         * @return ContentReader instance or {@code null}, if content should not be presented in the fold preview.
         */
        public ContentReader    createReader(FoldType ft);
    }
}

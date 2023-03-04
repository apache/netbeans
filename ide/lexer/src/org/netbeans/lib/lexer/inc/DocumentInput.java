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

package org.netbeans.lib.lexer.inc;

import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.lexer.LanguageManager;
import org.netbeans.lib.lexer.LexerApiPackageAccessor;
import org.netbeans.spi.lexer.*;

/**
 * Control structure for managing of the lexer for a given document.
 * <br>
 * There is one structure for a document. It can be obtained by
 * {@link #get(Document)}.
 * <br>
 * Each document that wants to use the lexer framework
 * must be initialized by using {@link #init(Document, boolean, Object)}.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class DocumentInput<D extends Document>
extends MutableTextInput<D> implements DocumentListener {

    // -J-Dorg.netbeans.lib.lexer.TokenHierarchyOperation.level=FINE
    private static final Logger LOG = Logger.getLogger(org.netbeans.lib.lexer.TokenHierarchyOperation.class.getName());

    private static final String PROP_MIME_TYPE = "mimeType"; //NOI18N
    
    public static synchronized <D extends Document> DocumentInput<D> get(D doc) {
        @SuppressWarnings("unchecked")
        DocumentInput<D> di = (DocumentInput<D>)doc.getProperty(MutableTextInput.class);
        if (di == null) {
            di = new DocumentInput<D>(doc);
            doc.putProperty(MutableTextInput.class, di);
        }
        return di;
    }
    
    private D doc;
    
    private CharSequence text;
    
    public DocumentInput(D doc) {
        this.doc = doc;
        this.text = DocumentUtilities.getText(doc);
        // Add document listener with the appropriate priority (if priority listening is supported)
        DocumentUtilities.addDocumentListener(doc, this, DocumentListenerPriority.LEXER);
    }
    
    @Override
    protected Language<?> language() {
        Language<?> lang = (Language<?>)doc.getProperty(Language.class);
        if (lang == null) {
            String mimeType = (String) doc.getProperty(PROP_MIME_TYPE);
            if (mimeType != null) {
                lang = LanguageManager.getInstance().findLanguage(mimeType);
            }
        }
        return lang;
    }
    
    @Override
    protected CharSequence text() {
        return text;
    }
    
    @Override
    protected InputAttributes inputAttributes() {
        return (InputAttributes)doc.getProperty(InputAttributes.class);
    }

    @Override
    protected D inputSource() {
        return doc;
    }
    
    @Override
    protected boolean isReadLocked() {
        return DocumentUtilities.isReadLocked(doc);
    }

    @Override
    protected boolean isWriteLocked() {
        return DocumentUtilities.isWriteLocked(doc);
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void insertUpdate(DocumentEvent e) {
        textModified(e.getOffset(), 0, null, e.getLength());
    }

    public void removeUpdate(DocumentEvent e) {
        textModified(e.getOffset(), e.getLength(),
                DocumentUtilities.getModificationText(e), 0);
    }

    private void textModified(int offset, int length, CharSequence removedText,
    int insertedLength) {
        try {
            tokenHierarchyControl().textModified(offset, length, removedText, insertedLength);
        } catch (RuntimeException e) {
            // Log the exception and attempt to recover by recreating the token hierarchy
            throw LexerApiPackageAccessor.get().tokenHierarchyOperation(tokenHierarchyControl().tokenHierarchy()).recreateAfterError(e);
        }
    }

}

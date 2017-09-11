/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

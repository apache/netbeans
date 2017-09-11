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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.editor;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JTextField;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.editor.lib2.DialogBindingTokenId;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 *
 * @author Dusan Balek
 */
public final class DialogBinding {

    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    /**
     * Bind given component and given file together.
     *
     * @param fileObject to bind
     * @param offset position at which content of the component will be virtually placed
     * @param length how many characters replace from the original file
     * @param component component to bind
     */
    public static void bindComponentToFile(FileObject fileObject, int offset, int length, JTextComponent component) {
        Parameters.notNull("fileObject", fileObject); //NOI18N
        Parameters.notNull("component", component); //NOI18N
        if (!fileObject.isValid() || !fileObject.isData()) {
            return;
        }
        if (offset < 0 || offset > fileObject.getSize()) {
            throw new IllegalArgumentException("Invalid offset=" + offset + "; file.size=" + fileObject.getSize()); //NOI18N
        }
        if (length < 0 || offset + length > fileObject.getSize()) {
            throw new IllegalArgumentException("Invalid lenght=" + length + "; offset=" + offset + ", file.size=" + fileObject.getSize()); //NOI18N
        }
        bind(component, null, fileObject, offset, -1, -1, length, fileObject.getMIMEType());
    }

    /**
     * Bind given component and given document together.
     *
     * @param document to bind
     * @param offset position at which content of the component will be virtually placed
     * @param length how many characters replace from the original document
     * @param component component to bind
     */
    public static void bindComponentToDocument(Document document, int offset, int length, JTextComponent component) {
        Parameters.notNull("document", document); //NOI18N
        Parameters.notNull("component", component); //NOI18N
        if (offset < 0 || offset > document.getLength()) {
            throw new IllegalArgumentException("Invalid offset=" + offset + "; file.size=" + document.getLength()); //NOI18N
        }
        if (length < 0 || offset + length > document.getLength()) {
            throw new IllegalArgumentException("Invalid lenght=" + length + "; offset=" + offset + ", file.size=" + document.getLength()); //NOI18N
        }
        bind(component, document, null, offset, -1, -1, length, (String)document.getProperty("mimeType")); //NOI18N
    }

    /**
     * Bind given component and given file together.
     * 
     * @param fileObject to bind
     * @param line an index (zero based) of the line where to place the component's content
     * @param column position (number of characters) at the line where to place the content
     * @param length how many characters replace from the original file
     * @param component component to bind
     *
     * @since 1.24
     */
    public static void bindComponentToFile(FileObject fileObject, int line, int column, int length, JTextComponent component) {
        Parameters.notNull("fileObject", fileObject); //NOI18N
        Parameters.notNull("component", component); //NOI18N
        if (!fileObject.isValid() || !fileObject.isData()) {
            return;
        }
        if (line < 0 || column < 0) {
            throw new IllegalArgumentException("Invalid line=" + line + " or column=" + column); //NOI18N
        }
        bind(component, null, fileObject, -1, line, column, length, fileObject.getMIMEType());
    }

    /**
     * Bind given component and given document together.
     *
     * @param document to bind
     * @param line an index (zero based) of the line where to place the component's content
     * @param column position (number of characters) at the line where to place the content
     * @param length how many characters replace from the original document
     * @param component component to bind
     *
     * @since 1.24
     */
    public static void bindComponentToDocument(Document document, int line, int column, int length, JTextComponent component) {
        Parameters.notNull("document", document); //NOI18N
        Parameters.notNull("component", component); //NOI18N
        if (line < 0 || column < 0) {
            throw new IllegalArgumentException("Invalid line=" + line + " or column=" + column); //NOI18N
        }
        bind(component, document, null, -1, line, column, length, (String)document.getProperty("mimeType")); //NOI18N
    }

    // -----------------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------------

    // -J-Dorg.netbeans.api.editor.DialogBinding.level=FINE
    private static final Logger LOG = Logger.getLogger(DialogBinding.class.getName());
    
    private static void bind(
        JTextComponent component,
        Document document,
        FileObject fileObject,
        int offset,
        int line,
        int column,
        int length,
        final String mimeType
    ) {
        if (component instanceof JEditorPane) {
            ((JEditorPane) component).setEditorKit(MimeLookup.getLookup(mimeType).lookup(EditorKit.class));
            ((JEditorPane) component).setBackground(new JTextField().getBackground());
        }
        Document doc = component.getDocument();
        doc.putProperty("mimeType", DialogBindingTokenId.language().mimeType()); //NOI18N
        InputAttributes inputAttributes = new InputAttributes();
        Language language = MimeLookup.getLookup(DialogBindingTokenId.language().mimeType()).lookup(Language.class); //NOI18N
        inputAttributes.setValue(language, "dialogBinding.document", document, true); //NOI18N
        inputAttributes.setValue(language, "dialogBinding.fileObject", fileObject, true); //NOI18N
        inputAttributes.setValue(language, "dialogBinding.offset", offset, true); //NOI18N
        inputAttributes.setValue(language, "dialogBinding.line", line, true); //NOI18N
        inputAttributes.setValue(language, "dialogBinding.column", column, true); //NOI18N
        inputAttributes.setValue(language, "dialogBinding.length", length, true); //NOI18N
        doc.putProperty(InputAttributes.class, inputAttributes);

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "\njtc={0}\ndoc={1}\nfile={2}\noffset={3}\nline={4}\ncolumn={5}\nlength={6}\nmimeType={7}\n", new Object [] {
                component,
                document,
                fileObject,
                offset,
                line,
                column,
                length,
                mimeType
            });
        }
    }
}

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

package org.netbeans.api.xml.parsers;

import java.io.*;
import java.net.URL;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.xml.sax.InputSource;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Integrate NetBeans widely used Swing's {@link Document} with SAX API's.
 * Let it look like {@link InputSource}.
 *
 * @author  Petr Kuzel
 */
public final class DocumentInputSource extends InputSource {

    private static final Logger LOG = Logger.getLogger(DocumentInputSource.class.getName());

    private final Document doc;
     
    /** 
     * Creates new instance of <code>DocumentInputSource</code>. Client should
     * set system ID if available otherwise default one is derived.
     * @param doc Swing document used to be wrapped
     * @see   #getSystemId()
     */
    public DocumentInputSource(Document doc) {
        this.doc = doc;
    }

    // inherit JavaDoc
    public Reader getCharacterStream() {
        String text = documentToString(doc);
        return new StringReader(text);
    }

    /**
     * This <code>InputSource</code> is backended by Swing's <code>Document</code>.
     * Consequently its character stream is read-only, it
     * always reads content of associted <code>Document</code>.
     */
    public final void setCharacterStream(Reader reader) {
        // do nothing
    }

    /**
     * Get InputSource system ID. Use ordered logic:
     * <ul>
     *  <li>use client's <code>setSystemId()</code>, or
     *  <li>try to derive it from <code>Document</code>
     *      <p>e.g. look at <code>Document.StreamDescriptionProperty</code> for
     *      {@link DataObject} and use URL of its primary file.
     * </ul>
     * @return entity system Id or <code>null</code>
     */
    public String getSystemId() {
        
        String system = super.getSystemId();
        
        // XML module specifics property, promote into this API
//        String system = (String) doc.getProperty(TextEditorSupport.PROP_DOCUMENT_URL);        
        
        if (system == null) {
            final FileObject fo = EditorDocumentUtils.getFileObject(doc);
            if (fo != null) {
                URL url = fo.toURL();
                system = url.toExternalForm();
            } else {
                LOG.info("XML:DocumentInputSource:No FileObject in stream description.");   //NOI18N
            }
        }
        return system;
    }
        
    
    /**
     * @return current state of Document as string
     */
    private static String documentToString(final Document doc) {
        
        final String[] str = new String[1];

        // safely take the text from the document
        Runnable run = new Runnable() {
            public void run () {
                try {
                    str[0] = doc.getText(0, doc.getLength());
                } catch (javax.swing.text.BadLocationException e) {
                    // impossible
                    Exceptions.printStackTrace(e);
                }
            }
        };

        doc.render(run);
        return str[0];
        
    }
    
    /**
     * For debugging purposes only.
     */
    public String toString() {
        return "DocumentInputSource SID:" + getSystemId();
    }
}

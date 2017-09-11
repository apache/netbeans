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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

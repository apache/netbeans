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

package org.netbeans.modules.web.core.syntax.completion;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.java.preprocessorbridge.spi.ImportProcessor;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class JspTagLibImportProcessor implements ImportProcessor {

    public void addImport(Document document, final String fqn) {
        final BaseDocument doc = (BaseDocument)document;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                doc.runAtomic(new Runnable() {
                    public void run() {
                        try {
                            processDocument(doc, fqn);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        });
    }

    protected abstract String createImportDirective(String fqn);

    private void processDocument(BaseDocument doc, final String fqn) throws BadLocationException {
        int insertPos = Util.findPositionForJspDirective(doc);
        doc.insertString(insertPos, createImportDirective(fqn), null);
    }

    public static class JspImportProcessor extends JspTagLibImportProcessor{

        @Override
        protected String createImportDirective(String fqn) {
            return "<%@page import=\"" + fqn + "\"%>\n";
        }
    }

    public static class TagImportProcessor extends JspTagLibImportProcessor{

        @Override
        protected String createImportDirective(String fqn) {
            return "<%@tag import=\"" + fqn + "\"%>\n";
        }
    }
}

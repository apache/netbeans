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
package org.netbeans.modules.jshell.editor;

import java.awt.image.ImageObserver;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import jdk.jshell.Snippet;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.java.preprocessorbridge.spi.ImportProcessor;
import org.netbeans.modules.jshell.model.ConsoleContents;
import org.netbeans.modules.jshell.model.ConsoleModel;
import org.netbeans.modules.jshell.model.SnippetHandle;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.Exceptions;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType = "text/x-repl", service = ImportProcessor.class)
public class JShellImports implements ImportProcessor {
    @Override
    public void addImport(Document doc, String fullyQualifiedClassName) {
        Source src = Source.create(doc);
        final int[] retOffset = new int[1];
        final boolean[] retNewline = new boolean[1];
        try {
            ParserManager.parse(Collections.singleton(src), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ConsoleContents contents = ConsoleContents.get(resultIterator);
                    ConsoleSection in = contents.getSectionModel().getInputSection();
                    retOffset[0] = -1;
                    if (in == null || contents == null) {
                        return;
                    }
                    List<SnippetHandle> snips = contents.getHandles(in);
                    SnippetHandle lastImport = null;

                    for (SnippetHandle sh : snips) {
                        if (sh.getKind() == Snippet.Kind.IMPORT) {
                            lastImport = sh;
                        }
                    }
                    final int offset;
                    final boolean addNewline;
                    if (lastImport == null) {
                        addNewline = false;
                        offset = in.getPartBegin();
                    } else {
                        addNewline = true;
                        offset = in.getSnippetBounds(snips.indexOf(lastImport)).end;
                    }
                    retOffset[0] = offset;
                    retNewline[0] = addNewline;
                }
                
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        ConsoleModel model = ConsoleModel.get(doc);
        ShellSession session = ShellSession.get(doc);
        if (model == null || session == null) {
            return;
        }

        final int offset = retOffset[0];
        if (offset == -1) {
            return;
        }
        
        final boolean addNewline = retNewline[0];
        AtomicLockDocument ad = LineDocumentUtils.as(doc, AtomicLockDocument.class);
        LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
        
        ad.runAtomic(() -> {
            int o = offset;
            Reformat rf = Reformat.get(doc);
            try {
                if (addNewline) {
                    doc.insertString(o, "\n", null);
                    o++;
                }
                // generate the import
                doc.insertString(o, 
                        "import " + fullyQualifiedClassName + ";\n", null);
                int eo = LineDocumentUtils.getLineEndOffset(ld, o);
                rf.lock();
                rf.reformat(o, eo);
            } catch (BadLocationException ex) {
                
            } finally {
                rf.unlock();
            }
        });
        
    }
}


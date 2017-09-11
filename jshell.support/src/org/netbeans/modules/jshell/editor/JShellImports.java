/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
                int eo = LineDocumentUtils.getLineEnd(ld, o);
                rf.lock();
                rf.reformat(o, eo);
            } catch (BadLocationException ex) {
                
            } finally {
                rf.unlock();
            }
        });
        
    }
}


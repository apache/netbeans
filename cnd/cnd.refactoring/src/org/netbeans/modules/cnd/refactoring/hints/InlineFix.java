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
package org.netbeans.modules.cnd.refactoring.hints;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.refactoring.plugins.InlinePlugin;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 */
public class InlineFix implements Fix {
    private final BaseDocument doc;
    private final CsmReference ref;
    private final CsmFile file;
    private final String replacement;
    
    public InlineFix(CsmReference ref, Document doc, CsmFile file, String replacement) {
        this.doc = (BaseDocument) doc;
        this.ref = ref;
        this.file = file;
        this.replacement = replacement;
    }
    
    @Override
    public String getText() {
        return NbBundle.getMessage(InlineFix.class, "FIX_Inline"); //NOI18N
    }
    
    @Override
    public ChangeInfo implement() throws Exception {
        doc.runAtomicAsUser(new Runnable() {
            @Override
            public void run() {
                try {
                    Position startPos = NbDocument.createPosition(doc, ref.getStartOffset(), Position.Bias.Forward);
                    Position endPos = NbDocument.createPosition(doc
                                                               ,ref.getEndOffset() + InlinePlugin.getMacroParametersEndOffset(file, (CsmMacro) ref.getReferencedObject(), ref.getEndOffset())
                                                               ,Position.Bias.Backward);
                    doc.remove(startPos.getOffset(), endPos.getOffset()-startPos.getOffset());
                    final int start = startPos.getOffset();
                    doc.insertString(start, replacement, null);
                    Reformat format = Reformat.get(doc);
                    format.lock();
                    try {
                        format.reformat(start, start + replacement.length() + 1);
                    } finally {
                        format.unlock();
                    }                    
                } catch (BadLocationException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        });
        return null;
    }
    
}

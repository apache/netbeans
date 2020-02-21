/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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

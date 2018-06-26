/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.nodejs.editor;

import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.editor.spi.DeclarationFinder;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
@DeclarationFinder.Registration(priority = 16)
public class NodeJsDeclarationFinder implements DeclarationFinder {
    
    
    
    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        Snapshot snapshot = info.getSnapshot();
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(snapshot, caretOffset);
        Token<? extends JsTokenId> pathToken = getModeluPath(ts, caretOffset);
        if (pathToken != null) {
            String module = pathToken.text().toString();
            FileObject moduleFO = NodeJsUtils.findModuleFile(snapshot.getSource().getFileObject(), module);
            if (moduleFO != null) {
                if (NodeJsDataProvider.getDefault(moduleFO).getRuntimeModules().contains(module)) {
                    ElementHandle eh = new NodeJsElement(moduleFO, module, NodeJsDataProvider.getDefault(moduleFO).getDocForModule(module), ElementKind.FILE);
                    return new DeclarationLocation(moduleFO, 0, eh);
                }
                
                return new DeclarationLocation(moduleFO, 0);
            }
        }
        return DeclarationLocation.NONE;
    }

    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
        final OffsetRange[] value = new OffsetRange[1];
        value[0] = OffsetRange.NONE;

        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, caretOffset);
                Token<? extends JsTokenId> path = getModeluPath(ts, caretOffset);
                if (path != null) {
                    value[0] = new OffsetRange(ts.offset(), ts.offset() + ts.token().length());
                }
            }
        });
        return value[0];
    }

    private Token<? extends JsTokenId> getModeluPath(TokenSequence<? extends JsTokenId> ts, final int offset) {
        Token<? extends JsTokenId> path = null;
        if (ts != null && NodeJsContext.findContext(ts, offset) == NodeJsContext.MODULE_PATH) {
            ts.move(offset);
            ts.moveNext();
            Token<? extends JsTokenId> token = ts.token();
            if (token.id() == JsTokenId.STRING_END) {
                ts.movePrevious();
                token = ts.token();
            }
            if (token.id() == JsTokenId.STRING) {
                String text = token.text().toString();
                if (text != null && !text.isEmpty()) {
                    path = token;
                }
            }
        }
        return path;
    }
}

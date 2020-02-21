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

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/**
 *
 */
public class LineHyperlinkProvider implements HyperlinkProviderExt {
    private LineTarget jumpToken;
    private Cancellable hyperLinkTask;

    public LineHyperlinkProvider() {
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        LineTarget lineTarget = getLineDirective(doc, offset);
        if (lineTarget != null && lineTarget.fileToken != null) {
            return true;
        }
        return false;
    }

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        LineTarget lineTarget = getLineDirective(doc, offset);
        if (lineTarget != null && lineTarget.fileToken != null) {
            jumpToken = lineTarget;
            return new int[]{lineTarget.directiveToken.offset(), lineTarget.fileToken.offset() + lineTarget.fileToken.length()};
        }
        return null;
    }

    @Override
    public void performClickAction(final Document doc, final int offset, final HyperlinkType type) {
        UIGesturesSupport.submit("USG_CND_LINE_HYPERLINK", type); //NOI18N
        goToLine(doc, Utilities.getFocusedComponent(), offset, type);
    }
    
    public boolean goToLine(final Document doc, final JTextComponent target, final int offset, final HyperlinkType type) {
        if (target == null || target.getDocument() != doc) {
            return false;
        }

        if (!isHyperlinkPoint(doc, offset, type)) {
            return false;
        }
        Runnable run = new Runnable() {

            @Override
            public void run() {
                performAction(doc, target, offset, type);
            }
        };
        if (hyperLinkTask != null) {
            hyperLinkTask.cancel();
        }
        hyperLinkTask = CsmModelAccessor.getModel().enqueue(run, "Following hyperlink");// NOI18N
        return true;
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        if (doc == null || offset < 0 || offset > doc.getLength()) {
            return null;
        }
        LineTarget token = jumpToken;
        if (token == null || token.fileToken == null) {
            token = getLineDirective(doc, offset);
        }
        if (token != null && token.fileToken != null) {
            FileObject toOpen = getTargetFileObject(token, doc);
            if (toOpen != null) {
                return NbBundle.getMessage(LineHyperlinkProvider.class, "LineDirective", ""+token.line, toOpen.getPath()); // NOI18N
            }
        }
        return null;
    }

    private LineTarget getLineDirective(final Document doc, final int offset) {
        final AtomicReference<LineTarget> out = new AtomicReference<LineTarget>();
        doc.render(new Runnable() {
            @Override
            public void run() {
                TokenSequence<TokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, offset, true, true);
                if (cppTokenSequence == null) {
                    return;
                }
                if (!cppTokenSequence.language().equals(CppTokenId.languagePreproc())) {
                    return;
                }
                cppTokenSequence.moveStart();
                if (cppTokenSequence.moveNext()) {
                    while(cppTokenSequence.moveNext()) {
                        final Token<TokenId> token = cppTokenSequence.token();
                        final TokenId id = token.id();
                        if(id instanceof CppTokenId) {
                            if (CppTokenId.PREPROCESSOR_KEYWORD_DIRECTIVE_CATEGORY.equals(((CppTokenId)id).primaryCategory())) {
                                if (id == CppTokenId.PREPROCESSOR_LINE) {
                                    final int tokenOffset = cppTokenSequence.offset();
                                    TokenItem lineDirective = new TokenItemImpl(id, tokenOffset, token);
                                    int lineNumber = -1;
                                    String fileName = null;
                                    TokenItem fileToken = null;
                                    while(cppTokenSequence.moveNext()) {
                                        Token<TokenId> t = cppTokenSequence.token();
                                        final TokenId kind = t.id();
                                        if (kind == CppTokenId.INT_LITERAL) {
                                            try {
                                                lineNumber = Integer.parseInt(t.text().toString());
                                            } catch (NumberFormatException ex) {
                                                //
                                            }
                                        } else if (kind == CppTokenId.STRING_LITERAL) {
                                            fileName = t.text().toString();
                                            if (fileName.startsWith("\"") && fileName.endsWith("\"") && fileName.length()>=2) { //NOI18N
                                                fileName = fileName.substring(1, fileName.length()-1);
                                                fileToken = new TokenItemImpl(kind, cppTokenSequence.offset(), t);
                                            }
                                        }
                                    }
                                    out.set(new LineTarget(lineDirective, fileToken, lineNumber, fileName));
                                }
                                return;
                            }
                        }
                    }
                }
            }
        });
        return out.get();
    }

    private void performAction(final Document doc, final JTextComponent target, final int offset, final HyperlinkType type) {
        UIGesturesSupport.submit("USG_CND_INCLUDE_HYPERLINK", type); //NOI18N
        LineTarget item = getLineDirective(doc, offset);
        FileObject toOpen = getTargetFileObject(item, doc);
        if (toOpen != null && toOpen.isValid()) {
            CsmUtilities.openSource(toOpen, item.line, 0);
        }
    }
    
    private FileObject getTargetFileObject(LineTarget item, Document doc) {
        if (item != null && item.file != null) {
            CsmFile csmFile = CsmUtilities.getCsmFile(doc, true, false);
            if (csmFile != null) {
                FileObject fileObject = csmFile.getFileObject();
                if (fileObject != null) {
                    String path = item.file;
                    try {
                        FSPath fs;
                        FileSystem fileSystem = fileObject.getFileSystem();
                        if (CndPathUtilities.isAbsolute(fileSystem, path)) {
                            fs = new FSPath(fileSystem, path);
                        } else {
                            fs = new FSPath(fileObject.getFileSystem(), fileObject.getParent().getPath()+"/"+path); //NOI18N
                        }
                        FileObject toOpen = fs.getFileObject();
                        if (toOpen != null && toOpen.isValid()) {
                            return toOpen;
                        }
                    } catch (FileStateInvalidException ex) {
                    }
                }
            }
        }
        return null;
    }
    
    private static class TokenItemImpl implements TokenItem {

        private final TokenId id;
        private final int tokenOffset;
        private final Token<TokenId> token;

        public TokenItemImpl(TokenId id, int tokenOffset, Token<TokenId> token) {
            this.id = id;
            this.tokenOffset = tokenOffset;
            this.token = token;
        }

        @Override
        public TokenId id() {
            return id;
        }

        @Override
        public int offset() {
            return tokenOffset;
        }

        @Override
        public CharSequence text() {
            return token.text();
        }

        @Override
        public int index() {
            return 0;
        }

        @Override
        public int length() {
            return text().length();
        }

        @Override
        public PartType partType() {
            return null;
        }
    }
    
    private static final class LineTarget {
        private final TokenItem<TokenId> directiveToken;
        private final TokenItem<TokenId> fileToken;
        private final int line;
        private final String file;

        public LineTarget(TokenItem<TokenId> directiveToken, TokenItem<TokenId> fileToken, int line, String file) {
            this.directiveToken = directiveToken;
            this.fileToken = fileToken;
            this.line = line;
            this.file = file;
        }
        
    }
}

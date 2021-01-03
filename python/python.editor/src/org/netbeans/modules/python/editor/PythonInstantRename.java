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
package org.netbeans.modules.python.editor;

import org.netbeans.modules.python.source.AstPath;
import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.PythonAstUtils;
import java.util.Collections;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.python.source.lexer.PythonTokenId;
import org.netbeans.modules.python.source.lexer.PythonCommentTokenId;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Name;

public class PythonInstantRename implements InstantRenamer {
    
    @Override
    public boolean isRenameAllowed(ParserResult info, int caretOffset, String[] explanationRetValue) {
        if (findVarName(info, caretOffset) != null) {
            return true;
        }

        PythonTree root = PythonAstUtils.getRoot(info);
        if (root == null) {
            return false;
        }

        AstPath path = AstPath.get(root, caretOffset);
        PythonTree leaf = path.leaf();

        if (PythonAstUtils.isNameNode(leaf) || leaf instanceof Attribute) {
            return true;
        }

        if ((leaf instanceof FunctionDef || leaf instanceof Call || leaf instanceof ClassDef) &&
                PythonAstUtils.getNameRange(null, leaf).containsInclusive(caretOffset)) {
            return true;
        }

        return false;
    }

    private TokenSequence<PythonCommentTokenId> findVarName(ParserResult info, int caretOffset) {
        Document document = info.getSnapshot().getSource().getDocument(false);
        if (document != null) {
            BaseDocument doc = (BaseDocument)document;
            TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPositionedSequence(doc, caretOffset);
            if (ts != null && ts.token().id() == PythonTokenId.COMMENT) {
                TokenSequence<PythonCommentTokenId> embedded = ts.embedded(PythonCommentTokenId.language());
                if (embedded != null) {
                    embedded.move(caretOffset);
                    if (embedded.moveNext() || embedded.movePrevious()) {
                        Token<PythonCommentTokenId> token = embedded.token();
                        PythonCommentTokenId id = token.id();
                        if (id == PythonCommentTokenId.SEPARATOR && caretOffset == embedded.offset() && embedded.movePrevious()) {
                            token = embedded.token();
                            id = token.id();
                        }
                        if (id == PythonCommentTokenId.VARNAME) {
                            return embedded;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Set<OffsetRange> getRenameRegions(ParserResult info, int caretOffset) {
        TokenSequence<PythonCommentTokenId> embedded = findVarName(info, caretOffset);
        if (embedded != null) {
            Token<PythonCommentTokenId> token = embedded.token();
            String name = token.text().toString();
            Set<OffsetRange> offsets = PythonAstUtils.getAllOffsets((PythonParserResult) info, null, caretOffset, name, true);
            if (offsets != null) {
                return offsets;
            }

            return Collections.emptySet();
        }

        PythonParserResult parseResult = PythonAstUtils.getParseResult(info);
        PythonTree root = parseResult.getRoot();
        if (root != null) {
            AstPath path = AstPath.get(root, caretOffset);
            PythonTree leaf = path.leaf();
            String name = null;
            if (leaf instanceof Name) {
                name = ((Name)leaf).getInternalId();
                Set<OffsetRange> offsets = PythonAstUtils.getAllOffsets((PythonParserResult) info, path, caretOffset, name, true);
                if (offsets != null) {
                    return offsets;
                }
            }
        }

        return Collections.emptySet();
    }
}

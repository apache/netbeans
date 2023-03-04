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

package org.netbeans.modules.groovy.editor.language;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.FindTypeUtils;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.groovy.editor.occurrences.VariableScopeVisitor;
import org.openide.util.NbBundle;

/**
 *
 * @author schmidtm
 */
public class GroovyInstantRenamer implements InstantRenamer {

    private static final Logger LOG = Logger.getLogger(GroovyInstantRenamer.class.getName());

    public GroovyInstantRenamer() {
        super();
    }

    @Override
    public boolean isRenameAllowed(ParserResult info, int caretOffset, String[] explanationRetValue) {
        LOG.log(Level.FINEST, "isRenameAllowed()"); //NOI18N

        final AstPath path = getPathUnderCaret(ASTUtils.getParseResult(info), caretOffset);

        if (path != null) {
            final ASTNode closest = path.leaf();
            if (closest instanceof Variable) {
                final BaseDocument doc = LexUtilities.getDocument(ASTUtils.getParseResult(info), false);
                if (!FindTypeUtils.isCaretOnClassNode(path, doc, caretOffset)) {
                    return true;
                } else {
                    explanationRetValue[0] = NbBundle.getMessage(GroovyInstantRenamer.class, "NoInstantRenameOnClassNode");
                    return false;
                }
            } else {
                explanationRetValue[0] = NbBundle.getMessage(GroovyInstantRenamer.class, "OnlyRenameLocalVars");
                return false;
            }
        }

        return false;
    }

    @Override
    public Set<OffsetRange> getRenameRegions(ParserResult info, int caretOffset) {
        LOG.log(Level.FINEST, "getRenameRegions()"); //NOI18N

        GroovyParserResult gpr = ASTUtils.getParseResult(info);
        BaseDocument doc = LexUtilities.getDocument(gpr, false);
        if (doc == null) {
            return Collections.emptySet();
        }

        AstPath path = getPathUnderCaret(gpr, caretOffset);
        return markOccurences(path, doc, caretOffset);
    }

    private AstPath getPathUnderCaret(GroovyParserResult info, int caretOffset) {
        ModuleNode rootNode = ASTUtils.getRoot(info);
        if (rootNode == null) {
            return null;
        }
        int astOffset = ASTUtils.getAstOffset(info, caretOffset);
        if (astOffset == -1) {
            return null;
        }
        BaseDocument document = LexUtilities.getDocument(info, false);
        if (document == null) {
            LOG.log(Level.FINEST, "Could not get BaseDocument. It's null"); //NOI18N
            return null;
        }

        return new AstPath(rootNode, astOffset, document);

    }

    private static Set<OffsetRange> markOccurences(AstPath path, BaseDocument document, int cursorOffset) {
        final Set<OffsetRange> regions = new HashSet<OffsetRange>();
        final ASTNode root = path.root();
        assert root instanceof ModuleNode;
        final ModuleNode moduleNode = (ModuleNode) root;

        VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(moduleNode.getContext(), path, document, cursorOffset);
        scopeVisitor.collect();
        for (ASTNode astNode : scopeVisitor.getOccurrences()) {
            regions.add(ASTUtils.getRange(astNode, document));
        }
        return regions;
    }
}

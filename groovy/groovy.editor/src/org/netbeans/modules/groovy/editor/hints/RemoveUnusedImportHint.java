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

package org.netbeans.modules.groovy.editor.hints;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.FinderFactory;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.hints.infrastructure.GroovyAstRule;
import org.netbeans.modules.groovy.editor.hints.infrastructure.GroovyHintsProvider;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Sven Reimers
 */
public class RemoveUnusedImportHint extends GroovyAstRule {

    @Override
    @NbBundle.Messages("UnusedImport=Unused import")
    public void computeHints(GroovyHintsProvider.GroovyRuleContext context, List<Hint> result) {
        final ModuleNode moduleNode = context.getGroovyParserResult().getRootElement().getModuleNode();
        if (null == moduleNode) {
            return;
        }
        List<ImportNode> importNodes = moduleNode.getImports();
        for (ImportNode importNode : importNodes) {
            
            String alias = importNode.getAlias();
            
            try {
                int find = 0;
                final FinderFactory.StringFwdFinder stringFwdFinder = new FinderFactory.StringFwdFinder(alias, true);
                
                while(-1 != (find = context.doc.find(stringFwdFinder, find+1, -1)) && skipUsage(find, context.doc));
                
                if (-1 == find) {
                    result.add(new Hint(this, Bundle.UnusedImport(), 
                            NbEditorUtilities.getFileObject(context.doc), 
                            ASTUtils.getRangeFull(importNode, context.doc), 
                            Collections.<HintFix>singletonList(new RemoveUnusedImportFix(Bundle.RemoveUnusedImportHintDescription(), context.doc, importNode)), 1));
                }             
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private boolean skipUsage(int position, BaseDocument doc) {
        int lineNo = NbEditorUtilities.getLine(doc, position, true).getLineNumber();
        if (0 == lineNo) {
            return true;
        }
        GroovyTokenId tokenIdAtPosition = LexUtilities.getToken(doc, position).id();
        return GroovyTokenId.LITERAL_import == 
                        LexUtilities.getToken(doc, ASTUtils.getOffset(doc, lineNo+1, 2)).id()
                || GroovyTokenId.SH_COMMENT == tokenIdAtPosition
                || GroovyTokenId.SL_COMMENT == tokenIdAtPosition
                || GroovyTokenId.LINE_COMMENT == tokenIdAtPosition
                || GroovyTokenId.BLOCK_COMMENT == tokenIdAtPosition;
    }

    @Override
    public Set<?> getKinds() {
        return new HashSet<>(Arrays.asList(new String[]{"Import Hints"}));
    }

    @Override
    public String getId() {
        return "imports.unused.hint";
    }

    @Override
    @NbBundle.Messages("RemoveUnusedImportHintDescription=Remove unused import")
    public String getDescription() {
        return Bundle.RemoveUnusedImportHintDescription();
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        return context instanceof GroovyHintsProvider.GroovyRuleContext;
    }

    @Override
    public String getDisplayName() {
        return Bundle.RemoveUnusedImportHintDescription();
    }

    @Override
    public boolean showInTasklist() {
        return false;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.INFO;
    }

    private static class RemoveUnusedImportFix implements PreviewableFix {

        final BaseDocument baseDoc;
        final String desc;
        final ImportNode importNode;

        private RemoveUnusedImportFix(String desc, BaseDocument baseDoc, ImportNode importNode) {
            this.desc = desc;
            this.baseDoc = baseDoc;
            this.importNode = importNode;
        }

        @Override
        public String getDescription() {
            return desc;
        }

        @Override
        public void implement() throws Exception {
            getEditList().apply();
        }

        @Override
        public EditList getEditList() throws Exception {
            EditList edits = new EditList(baseDoc);
            int offset =  ASTUtils.getOffset(baseDoc,importNode.getLineNumber(), 1);
            int removeLen =  ASTUtils.getOffset(baseDoc,importNode.getLineNumber()+1, 1)-offset;
            edits.replace(offset, removeLen, "", true, 0);
            return edits;
        }

        @Override
        public boolean isSafe() {
            return false;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

        @Override
        public boolean canPreview() {
            return true;
        }
    }
}

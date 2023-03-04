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
package org.netbeans.modules.javascript2.editor.hints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.editor.hints.JsHintsProvider.JsRuleContext;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.model.api.Index;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class GlobalIsNotDefined extends JsAstRule {

    private static final List<String> KNOWN_GLOBAL_OBJECTS = Arrays.asList(
            "super",  "$", "jQuery",  //NOI18N
            Type.ARRAY, Type.OBJECT, Type.BOOLEAN, Type.NULL, Type.NUMBER,
            Type.REGEXP, Type.STRING, Type.UNDEFINED, Type.UNRESOLVED);

    @Override
    void computeHints(JsRuleContext context, List<Hint> hints, int offset, HintsProvider.HintsManager manager) throws BadLocationException {
        if (!JsTokenId.JAVASCRIPT_MIME_TYPE.equals(context.getJsParserResult().getSnapshot().getMimePath().getPath())) {
            // compute this hint just for the js files.
            return;
        }
        JsObject globalObject = Model.getModel(context.getJsParserResult(), false).getGlobalObject();
        Collection<? extends JsObject> variables = ModelUtils.getVariables((DeclarationScope)globalObject);
        FileObject fo = context.parserResult.getSnapshot().getSource().getFileObject();
        Index jsIndex = Index.get(fo);
        Set<String> namesFromFrameworks = new HashSet<>();
        for (JsObject globalFiles:  ModelUtils.getExtendingGlobalObjects(context.getJsParserResult().getSnapshot().getSource().getFileObject())) {
            for (JsObject global : globalFiles.getProperties().values()) {
                namesFromFrameworks.add(global.getName());
            }
        }
        Collection<String> jsHintGlobalDefinition = findJsHintGlobalDefinition(context.getJsParserResult().getSnapshot());
        for (JsObject variable : variables) {
            String varName = variable.getName();
            if(!variable.isDeclared()
                    && !KNOWN_GLOBAL_OBJECTS.contains(varName)
                    && !namesFromFrameworks.contains(varName)
                    && !jsHintGlobalDefinition.contains(varName)
                    && (variable.getJSKind() == JsElement.Kind.VARIABLE
                    || variable.getJSKind() == JsElement.Kind.OBJECT)) {

                if (context.isCancelled()) {
                    return;
                }

                // check whether is defined as window property or defined in classpath
                Collection<? extends IndexResult> findByFqnOnWindow = jsIndex.findByFqn("window." + varName, Index.FIELD_BASE_NAME);
                Collection<? extends IndexResult> findByFqnPlain = jsIndex.findByFqn(varName, Index.FIELD_BASE_NAME);
                if (findByFqnOnWindow.isEmpty() && findByFqnPlain.isEmpty()) {
                    if (variable.getOccurrences().isEmpty()) {
                        addHint(context, hints, offset, varName, variable.getOffsetRange());
                    } else {
                        for(Occurrence occurrence : variable.getOccurrences()) {
                            addHint(context, hints, offset, varName, occurrence.getOffsetRange());
                        }
                    }
                }
            }
        }
    }

    private void addHint(JsRuleContext context, List<Hint> hints, int offset, String name, OffsetRange range) throws BadLocationException {
        boolean add = false;
        Document document = context.getJsParserResult().getSnapshot().getSource().getDocument(false);
        if (offset > -1) {
            LineDocument ld = LineDocumentUtils.as(document, LineDocument.class);
            if (ld != null) {
                int lineOffset = LineDocumentUtils.getLineIndex(ld, offset);
                int lineOffsetRange = LineDocumentUtils.getLineIndex(ld, range.getStart());
                add = lineOffset == lineOffsetRange;
            }
        } else {
            add = true;
            if (document != null) {
                ((AbstractDocument)document).readLock();
                try {
                    TokenSequence ts = LexerUtils.getTokenSequence(document, range.getStart(), JsTokenId.javascriptLanguage(), true);
                    ts.move(range.getStart());
                    if (ts.moveNext()) {
                        add = ts.token().id() != JsTokenId.DOC_COMMENT;
                    }
                } finally {
                    ((AbstractDocument) document).readUnlock();
                }

            }
        }

        if (add) {
            List<HintFix> fixes;
            fixes = new ArrayList<>();
            fixes.add(new AddJsHintFix(context.getJsParserResult().getSnapshot(), offset, name));
            hints.add(new Hint(this, Bundle.JsGlobalIsNotDefinedHintDesc(name),
                    context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                    ModelUtils.documentOffsetRange(context.getJsParserResult(),
                    range.getStart(), range.getEnd()), fixes, 500));
        }
    }

    @Override
    public Set<?> getKinds() {
        return Collections.singleton(JsAstRule.JS_OTHER_HINTS);
    }

    @Override
    public String getId() {
        return "jsglobalisnotdefined.hint";
    }

    @NbBundle.Messages({
            "JsGlobalIsNotDefinedDesc=The global variable is not declared.",
            "# {0} - name of global variable",
            "JsGlobalIsNotDefinedHintDesc=The global variable \"{0}\" is not declared."})
    @Override
    public String getDescription() {
        return Bundle.JsGlobalIsNotDefinedDesc();
    }

    @NbBundle.Messages("JsGlobalIsNotDefinedDN=The global variable is not declared")
    @Override
    public String getDisplayName() {
        return Bundle.JsGlobalIsNotDefinedDN();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

    private Collection<String> findJsHintGlobalDefinition(Snapshot snapshot) {
        ArrayList<String> names = new ArrayList<>();
        Collection<Identifier> definedGlobal = ModelUtils.getDefinedGlobal(snapshot, 0);
        for (Identifier identifier: definedGlobal) {
            names.add(identifier.getName());
        }
        return names;
    }

    static class AddJsHintFix implements HintFix {

        private final Snapshot snapshot;
        private final String name;
        private final int offset;

        public AddJsHintFix(final Snapshot snapshot, final int offset, final String name) {
            this.snapshot = snapshot;
            this.name = name;
            this.offset = offset;
        }


        @Override
        @NbBundle.Messages({"AddGlobalJsHint_Description=Generate JsHint global directive for variable {0}"})
        public String getDescription() {
            return Bundle.AddGlobalJsHint_Description(this.name);
        }

        @Override
        public void implement() throws Exception {
            JSHintSupport.addGlobalInline(snapshot, offset, name);
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

    }

}

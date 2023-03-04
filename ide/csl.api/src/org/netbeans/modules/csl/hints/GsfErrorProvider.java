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
package org.netbeans.modules.csl.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lsp.CodeAction;
import org.netbeans.api.lsp.Diagnostic;
import org.netbeans.api.lsp.TextDocumentEdit;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.core.ApiAccessor;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.hints.infrastructure.GsfHintsManager;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.lsp.ErrorProvider;
import org.openide.util.Exceptions;
import org.openide.util.Union2;

/**
 *
 * @author Dusan Balek
 */
public class GsfErrorProvider implements ErrorProvider {

    @Override
    public List<? extends Diagnostic> computeErrors(Context context) {
        final List<Hint> hints = new ArrayList<>();
        final List<Error> errors = new ArrayList<>();
        try {
            ParserManager.parse(Collections.singletonList(Source.create(context.file())), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result result = resultIterator.getParserResult(context.getOffset());
                    if(result instanceof ParserResult) {
                        ParserResult parserResult = (ParserResult) result;
                        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(resultIterator.getSnapshot().getMimeType());
                        if (language != null) {
                            HintsProvider hintsProvider = language.getHintsProvider();
                            if (hintsProvider != null) {
                                GsfHintsManager hintsManager = language.getHintsManager();
                                RuleContext ruleContext = hintsManager.createRuleContext(parserResult, language, context.getOffset(), -1, -1);
                                if (ruleContext != null) {
                                    switch (context.errorKind()) {
                                        case ERRORS:
                                            hintsProvider.computeErrors(hintsManager, ruleContext, hints, errors);
                                            break;
                                        case HINTS:
                                            hintsProvider.computeHints(hintsManager, ruleContext, hints);
                                            hintsProvider.computeSuggestions(hintsManager, ruleContext, hints, context.getOffset());
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        StyledDocument doc = DataLoadersBridge.getDefault().getDocument(context.file());
        LineDocument lineDocument = doc != null ? LineDocumentUtils.as(doc, LineDocument.class) : null;
        List<Diagnostic> diagnostics = new ArrayList<>(hints.size() + errors.size());
        int idx = 0;
        for (Hint hint : hints) {
            diagnostics.add(hint2Diagnostic(hint, ++idx));
        }
        for (Error error : errors) {
            diagnostics.add(error2Diagnostic(error, lineDocument, ++idx));
        }
        return diagnostics;
    }

    private Diagnostic error2Diagnostic(Error error, LineDocument lineDocument, int idx) {
        Diagnostic.Builder diagBuilder = Diagnostic.Builder.create(() -> {
            if (lineDocument != null && error.getStartPosition() >= error.getEndPosition()) {
                try {
                    return LineDocumentUtils.getLineFirstNonWhitespace(lineDocument, error.getStartPosition());
                } catch (BadLocationException ex) {}
            }
            return error.getStartPosition();
        }, () -> {
            if (lineDocument != null && error.getStartPosition() >= error.getEndPosition()) {
                try {
                    return LineDocumentUtils.getLineLastNonWhitespace(lineDocument, error.getEndPosition());
                } catch (BadLocationException ex) {}
            }
            return error.getEndPosition();
        }, error.getDescription() != null ? error.getDescription() : error.getDisplayName());
        switch (error.getSeverity()) {
            case FATAL:
            case ERROR:
                diagBuilder.setSeverity(Diagnostic.Severity.Error);
                break;
            case WARNING:
                diagBuilder.setSeverity(Diagnostic.Severity.Warning);
                break;
            case INFO:
                diagBuilder.setSeverity(Diagnostic.Severity.Information);
                break;
        }
        String id = "errors:" + idx + "-" + error.getKey();
        diagBuilder.setCode(id);
        return diagBuilder.build();
    }

    private Diagnostic hint2Diagnostic(Hint hint, int idx) {
        final OffsetRange range = hint.getRange();
        Diagnostic.Builder diagBuilder = Diagnostic.Builder.create(() -> range.getStart(), () -> range.getEnd(), hint.getDescription());
        switch (hint.getRule().getDefaultSeverity()) {
            case ERROR:
                diagBuilder.setSeverity(Diagnostic.Severity.Error);
                break;
            case CURRENT_LINE_WARNING:
            case WARNING:
                diagBuilder.setSeverity(Diagnostic.Severity.Warning);
                break;
            case INFO:
                diagBuilder.setSeverity(Diagnostic.Severity.Information);
                break;
        }
        String id = "hints:" + idx + "-" + hint.getRule().getDisplayName();
        diagBuilder.setCode(id);
        diagBuilder.addActions(errorReporter -> convertFixes(hint, errorReporter));
        return diagBuilder.build();
    }

    private static List<CodeAction> convertFixes(Hint hint, Consumer<Exception> errorReporter) {
        List<CodeAction> result = new ArrayList<>();
        for (HintFix fix : hint.getFixes()) {
            if (fix instanceof PreviewableFix) {
                try {
                    List<TextEdit> edits = new ArrayList<>();
                    for (EditList.Edit edit : ApiAccessor.getInstance().getEdits(((PreviewableFix) fix).getEditList())) {
                        String newText = edit.getInsertText();
                        edits.add(new TextEdit(edit.getOffset(), edit.getOffset() + edit.getRemoveLen(), newText != null ? newText : ""));
                    }
                    TextDocumentEdit te = new TextDocumentEdit(hint.getFile().toURI().toString(), edits);
                    result.add(new CodeAction(fix.getDescription(), new WorkspaceEdit(Collections.singletonList(Union2.createFirst(te)))));
                } catch (Exception ex) {
                    errorReporter.accept(ex);
                }
            }
        }
        return result;
    }
}

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
package org.netbeans.modules.java.hints.infrastructure;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.util.TreePath;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lsp.CodeAction;
import org.netbeans.api.lsp.Command;
import org.netbeans.api.lsp.Diagnostic;
import org.netbeans.api.lsp.Diagnostic.Builder;
import org.netbeans.api.lsp.LazyCodeAction;
import org.netbeans.api.lsp.ResourceOperation;
import org.netbeans.api.lsp.ResourceOperation.CreateFile;
import org.netbeans.api.lsp.TextDocumentEdit;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.modules.editor.tools.storage.api.ToolPreferences;
import org.netbeans.modules.java.hints.errors.ModificationResultBasedFix;
import org.netbeans.modules.java.hints.errors.ImportClass;
import org.netbeans.modules.java.hints.project.IncompleteClassPath;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.modules.java.hints.spiimpl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.lsp.ErrorProvider;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Union2;

/**
 *
 * @author lahvac
 */
@MimeRegistration(mimeType="text/x-java", service=ErrorProvider.class)
public class JavaErrorProvider implements ErrorProvider {
    
    public static final String HINTS_TOOL_ID = "hints";
    public static Consumer<ErrorProvider.Kind> computeDiagsCallback; //for tests

    @Override
    public List<? extends Diagnostic> computeErrors(Context context) {
        List<Diagnostic> result = new ArrayList<>();

        try {
            ParserManager.parse(Collections.singletonList(Source.create(context.file())), new UserTask() {
                @Override
                public void run(ResultIterator it) throws Exception {
                    CompilationController cc = CompilationController.get(it.getParserResult());
                    if (cc != null) {
                        if (computeDiagsCallback != null) {
                            computeDiagsCallback.accept(context.errorKind());
                        }
                        cc.toPhase(JavaSource.Phase.RESOLVED);
                        switch (context.errorKind()) {
                            case ERRORS:
                                ErrorHintsProvider ehp = new ErrorHintsProvider();
                                context.registerCancelCallback(() -> ehp.cancel());
                                result.addAll(convert2Diagnostic(context.errorKind(), ehp.computeErrors(cc, cc.getSnapshot().getSource().getDocument(true), "text/x-java"), err -> true));
                                break;
                            case HINTS:
                                Set<Severity> disabled = org.netbeans.modules.java.hints.spiimpl.Utilities.disableErrors(cc.getFileObject());
                                if (disabled.size() != Severity.values().length) {
                                    AtomicBoolean cancel = new AtomicBoolean();
                                    context.registerCancelCallback(() -> cancel.set(true));
                                    HintsSettings settings;
                                    
                                    if (context.getHintsConfigFile() != null) {
                                        Preferences hintSettings = ToolPreferences.from(context.getHintsConfigFile().toURI()).getPreferences(HINTS_TOOL_ID, "text/x-java");
                                        settings = HintsSettings.createPreferencesBasedHintsSettings(hintSettings, true, null);
                                    } else {
                                        settings = HintsSettings.getGlobalSettings();
                                    }
                                    result.addAll(convert2Diagnostic(context.errorKind(), new HintsInvoker(settings, context.getOffset(), cancel).computeHints(cc), ed -> !disabled.contains(ed.getSeverity())));
                                    
                                }
                                break;
                        }
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

    public static List<Diagnostic> convert2Diagnostic(Kind errorKind, List<ErrorDescription> errors, Predicate<ErrorDescription> filter) {
        if (errors == null) {
            return Collections.emptyList();
        }

        int idx = 0;
        List<Diagnostic> result = new ArrayList<>();

        for (ErrorDescription err : errors) {
            if (!filter.test(err)) continue;

            PositionBounds range = err.getRange();
            Builder diagBuilder = Builder.create(() -> range.getBegin().getOffset(), () -> range.getEnd().getOffset(), err.getDescription());

            switch (err.getSeverity()) {
                case ERROR: diagBuilder.setSeverity(Diagnostic.Severity.Error); break;
                case VERIFIER:
                case WARNING: diagBuilder.setSeverity(Diagnostic.Severity.Warning); break;
                case HINT: diagBuilder.setSeverity(Diagnostic.Severity.Hint); break;
                default: diagBuilder.setSeverity(Diagnostic.Severity.Information); break;
            }

            String rangeString;
            try {
                rangeString = (range.getBegin().getLine()+1) + ":" + (range.getBegin().getColumn()+1) + "-" + (range.getEnd().getLine()+1) + ":" + (range.getEnd().getColumn()+1);
            } catch (IOException ex) {
                rangeString = null;
            }
            String id = key(errorKind) + "(" + ++idx + "): " + (rangeString != null ? rangeString : "");

            diagBuilder.setCode(id);
            diagBuilder.addActions(errorReporter -> convertFixes(err, errorReporter));
            result.add(diagBuilder.build());
        }

        return result;
    }

    private static String key(ErrorProvider.Kind errorKind) {
        return errorKind.name().toLowerCase(Locale.ROOT);
    }

    private static List<CodeAction> convertFixes(ErrorDescription err, Consumer<Exception> errorReporter) {
        FileObject file = err.getFile();
        JavaSource js = JavaSource.forFileObject(file);
        TreePathHandle[] topLevelHandle = new TreePathHandle[1];
        LazyFixList lfl = err.getFixes();

        if (lfl instanceof CreatorBasedLazyFixList) {
            try {
                js.runUserActionTask(cc -> {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    ((CreatorBasedLazyFixList) lfl).compute(cc, new AtomicBoolean());
                    topLevelHandle[0] = TreePathHandle.create(new TreePath(cc.getCompilationUnit()), cc);
                }, true);
            } catch (IOException ex) {
                //TODO: include stack trace:
                errorReporter.accept(ex);
            }
        }

        List<Fix> fixes = sortFixes(lfl.getFixes());
        List<CodeAction> result = new ArrayList<>();

        for (Fix f : fixes) {
            if (f instanceof IncompleteClassPath.ResolveFix) {
                // We know that this is a project problem and that the problems reported by ProjectProblemsProvider should be resolved
                CodeAction action = new CodeAction(f.getText(), new Command(f.getText(), "nbls.java.project.resolveProjectProblems"));
                result.add(action);
            }
            if (f instanceof org.netbeans.modules.java.hints.errors.EnablePreview.ResolveFix) {
                org.netbeans.modules.java.hints.errors.EnablePreview.ResolveFix rf = (org.netbeans.modules.java.hints.errors.EnablePreview.ResolveFix) f;
                List<Object> params = rf.getNewSourceLevel() != null ? Arrays.asList(rf.getNewSourceLevel())
                                                                     : Collections.emptyList();
                CodeAction action = new CodeAction(f.getText(), new Command(f.getText(), "nbls.java.project.enable.preview", params));
                result.add(action);
            }
            if (f instanceof ImportClass.FixImport) {
                //TODO: FixImport is not a JavaFix, create one. Is there a better solution?
                String text = f.getText();
                CharSequence sortText = ((ImportClass.FixImport) f).getSortText();
                ElementHandle<Element> toImport = ((ImportClass.FixImport) f).getToImport();
                f = new JavaFix(topLevelHandle[0], sortText != null ? sortText.toString() : null) {
                    @Override
                    protected String getText() {
                        return text;
                    }
                    @Override
                    protected void performRewrite(JavaFix.TransformationContext ctx) throws Exception {
                        Element resolved = toImport.resolve(ctx.getWorkingCopy());
                        if (resolved == null) {
                            return ;
                        }
                        WorkingCopy copy = ctx.getWorkingCopy();
                        CompilationUnitTree cut = GeneratorUtilities.get(copy).addImports(
                            copy.getCompilationUnit(),
                            Collections.singleton(resolved)
                        );
                        copy.rewrite(copy.getCompilationUnit(), cut);
                    }
                }.toEditorFix();
            }
            if (f instanceof JavaFixImpl) {
                JavaFix jf = ((JavaFixImpl) f).jf;
                CodeAction action = new LazyCodeAction(f.getText(), () -> {
                    try {
                        List<TextEdit> edits = modify2TextEdits(js, wc -> {
                            wc.toPhase(JavaSource.Phase.RESOLVED);
                            Map<FileObject, byte[]> resourceContentChanges = new HashMap<>();
                            JavaFixImpl.Accessor.INSTANCE.process(jf, wc, true, resourceContentChanges, /*Ignored in editor:*/new ArrayList<>());
                        });
                        TextDocumentEdit te = new TextDocumentEdit(file.toURI().toString(), edits);
                        return new WorkspaceEdit(Collections.singletonList(Union2.createFirst(te)));
                    } catch (IOException ex) {
                        //TODO: include stack trace:
                        errorReporter.accept(ex);
                        return null;
                    }
                });
                result.add(action);
            }
            if (f instanceof ModificationResultBasedFix) {
                ModificationResultBasedFix cf = (ModificationResultBasedFix) f;
                CodeAction codeAction = new LazyCodeAction(f.getText(), () -> {
                    try {
                        List<Union2<TextDocumentEdit, ResourceOperation>> documentChanges = new ArrayList<>();
                        for (ModificationResult changes : cf.getModificationResults()) {
                            Set<File> newFiles = changes.getNewFiles();
                            if (newFiles.size() > 1) {
                                throw new IllegalStateException();
                            }
                            String newFilePath = null;
                            for (File newFile : newFiles) {
                                newFilePath = newFile.toURI().toString();
                                documentChanges.add(Union2.createSecond(new CreateFile(newFilePath)));
                            }
                            outer: for (FileObject fileObject : changes.getModifiedFileObjects()) {
                                List<? extends ModificationResult.Difference> diffs = changes.getDifferences(fileObject);
                                if (diffs != null) {
                                    List<TextEdit> edits = new ArrayList<>();
                                    for (ModificationResult.Difference diff : diffs) {
                                        String newText = diff.getNewText();
                                        if (diff.getKind() == ModificationResult.Difference.Kind.CREATE) {
                                            if (newFilePath != null) {
                                                documentChanges.add(Union2.createFirst(new TextDocumentEdit(newFilePath,
                                                        Collections.singletonList(new TextEdit(0, 0,
                                                                newText != null ? newText : "")))));
                                            }
                                            continue outer;
                                        } else {
                                            edits.add(new TextEdit(diff.getStartPosition().getOffset(),
                                                                   diff.getEndPosition().getOffset(),
                                                                   newText != null ? newText : ""));
                                        }
                                    }
                                    documentChanges.add(Union2.createFirst(new TextDocumentEdit(fileObject.toURI().toString(), edits))); //XXX: toURI
                                }
                            }
                        }
                        return new WorkspaceEdit(documentChanges);
                    } catch (IOException ex) {
                        errorReporter.accept(ex);
                        return null;
                    }
                });
                result.add(codeAction);
            }
        }

        return result;
    }

    //TODO: copied from spi.editor.hints/.../FixData:
    private static List<Fix> sortFixes(Collection<Fix> fixes) {
        List<Fix> result = new ArrayList<Fix>(fixes);

        result.sort(new FixComparator());

        return result;
    }

    private static final String DEFAULT_SORT_TEXT = "\uFFFF";

    private static CharSequence getSortText(Fix f) {
        if (f instanceof EnhancedFix) {
            return ((EnhancedFix) f).getSortText();
        } else {
            return DEFAULT_SORT_TEXT;
        }
    }
    private static final class FixComparator implements Comparator<Fix> {
        public int compare(Fix o1, Fix o2) {
            return compareText(getSortText(o1), getSortText(o2));
        }
    }

    private static int compareText(CharSequence text1, CharSequence text2) {
        int len = Math.min(text1.length(), text2.length());
        for (int i = 0; i < len; i++) {
            char ch1 = text1.charAt(i);
            char ch2 = text2.charAt(i);
            if (ch1 != ch2) {
                return ch1 - ch2;
            }
        }
        return text1.length() - text2.length();
    }
    //end copied

    private static List<TextEdit> modify2TextEdits(JavaSource js, Task<WorkingCopy> task) throws IOException {
        FileObject[] file = new FileObject[1];
        LineMap[] lm = new LineMap[1];
        ModificationResult changes = js.runModificationTask(wc -> {
            task.run(wc);
            file[0] = wc.getFileObject();
            lm[0] = wc.getCompilationUnit().getLineMap();
        });
        return fileModifications(changes, file[0], lm[0]);
    }

    private static List<TextEdit> fileModifications(ModificationResult changes, FileObject file, LineMap lm) {
        //TODO: full, correct and safe edit production:
        List<? extends ModificationResult.Difference> diffs = changes.getDifferences(file);
        if (diffs == null) {
            return Collections.emptyList();
        }
        List<TextEdit> edits = new ArrayList<>();

        for (ModificationResult.Difference diff : diffs) {
            String newText = diff.getNewText();
            edits.add(new TextEdit(diff.getStartPosition().getOffset(),
                                   diff.getEndPosition().getOffset(),
                                   newText != null ? newText : ""));
        }
        return edits;
    }
}

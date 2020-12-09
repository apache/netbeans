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
package org.netbeans.modules.java.lsp.server.protocol;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.services.LanguageClient;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import static org.netbeans.modules.java.lsp.server.protocol.TextDocumentServiceImpl.fromUri;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 *
 * @author lahvac
 */
public class GetterSetterGenerator {

    private static final String ERROR = "<error>"; //NOI18N

    public static Pair<Set<VariableElement>, Set<VariableElement>> findMissingGettersSetters(CompilationInfo info, Range range, boolean all) {
        TreePath tp = info.getTreeUtilities().pathFor(getOffset(info, range.getStart()));

        while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
            tp = tp.getParentPath();
        }

        if (tp == null) {
            return Pair.of(Collections.emptySet(), Collections.emptySet());
        }

        TypeElement type = (TypeElement) info.getTrees().getElement(tp);

        if (type == null) {
            return Pair.of(Collections.emptySet(), Collections.emptySet());
        }

        int selectionStart = getOffset(info, range.getStart());
        int selectionEnd   = getOffset(info, range.getEnd());

        ClassTree clazz = (ClassTree) tp.getLeaf();
        Set<VariableElement> selectedFields = new HashSet<>();

        for (Tree m : clazz.getMembers()) {
            if (m.getKind() != Tree.Kind.VARIABLE) continue;
            int start = (int) info.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), m);
            int end   = (int) info.getTrees().getSourcePositions().getEndPosition(tp.getCompilationUnit(), m);

            if (all || intersects(start, end, selectionStart, selectionEnd)) {
                selectedFields.add((VariableElement) info.getTrees().getElement(new TreePath(tp, m)));
            }
        }

        Pair<Set<VariableElement>, Set<VariableElement>> pair = GetterSetterGenerator.findMissingGettersSetters(info, type);

        pair.first().retainAll(selectedFields);
        pair.second().retainAll(selectedFields);

        return pair;
    }

    private static boolean intersects(int fieldStart, int fieldEnd, int selectionStart, int selectionEnd) {
        return selectionStart <= fieldEnd && selectionEnd >= fieldStart;
    }

    private static Pair<Set<VariableElement>, Set<VariableElement>> findMissingGettersSetters(CompilationInfo info, TypeElement type) {
        Set<VariableElement> missingGetters = new LinkedHashSet<>();
        Set<VariableElement> missingSetters = new LinkedHashSet<>();
        ElementUtilities eu = info.getElementUtilities();
        CodeStyle codeStyle = CodeStyle.getDefault(info.getFileObject());

        for (VariableElement variableElement : ElementFilter.fieldsIn(info.getElements().getAllMembers(type))) {
            if (ERROR.contentEquals(variableElement.getSimpleName())) {
                continue;
            }
            boolean hasGetter = eu.hasGetter(type, variableElement, codeStyle);
            boolean hasSetter = variableElement.getModifiers().contains(Modifier.FINAL) ||
                                eu.hasSetter(type, variableElement, codeStyle);
            if (!hasGetter) {
                missingGetters.add(variableElement);
            }
            if (!hasSetter) {
                missingSetters.add(variableElement);
            }
        }

        return Pair.of(missingGetters, missingSetters);
    }

    public static void generateGettersSetters(LanguageClient client, String uri, GenKind kind, Range range, boolean all) throws MalformedURLException, IOException {
        FileObject file = fromUri(uri);
        JavaSource js = JavaSource.forFileObject(file);

        List<TextEdit> edits = TextDocumentServiceImpl.modify2TextEdits(js, wc -> {
            wc.toPhase(JavaSource.Phase.RESOLVED);
            Pair<Set<VariableElement>, Set<VariableElement>> missingGettersSetters = findMissingGettersSetters(wc, range, all);
            Set<VariableElement> fields = new LinkedHashSet<>();
            fields.addAll(missingGettersSetters.first());
            fields.addAll(missingGettersSetters.second());
            if (!fields.isEmpty()) {
                TreePath tp = wc.getTrees().getPath(fields.iterator().next().getEnclosingElement());
                GeneratorUtils.generateGettersAndSetters(wc, tp, fields, kind.type, getOffset(wc, range.getStart()));
            }
        });

        client.applyEdit(new ApplyWorkspaceEditParams(new WorkspaceEdit(Collections.singletonMap(uri, edits))));
    }

    private static int getOffset(CompilationInfo info, Position pos) {
        LineMap lm = info.getCompilationUnit().getLineMap();
        return (int) lm.getPosition(pos.getLine() + 1, pos.getCharacter() + 1);
    }

    public enum GenKind {
        GETTERS(GeneratorUtils.GETTERS_ONLY),
        SETTERS(GeneratorUtils.SETTERS_ONLY),
        GETTERS_SETTERS(0);

        private final int type;

        private GenKind(int type) {
            this.type = type;
        }
    }
}

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

package org.netbeans.modules.nbcode.integration.java;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.annotation.processing.Processor;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.lsp.CompletionCollector;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dusan Balek
 */
public class AnnotationProcessorCompletionCollector implements CompletionCollector {

    private static final Lookup HARDCODED_PROCESSORS = Lookups.forPath("Editors/text/x-java/AnnotationProcessors");
    private static final String COMPLETION_CASE_SENSITIVE = "completion-case-sensitive";
    private static final boolean COMPLETION_CASE_SENSITIVE_DEFAULT = true;
    private static final String JAVA_COMPLETION_SUBWORDS = "javaCompletionSubwords";
    private static final boolean JAVA_COMPLETION_SUBWORDS_DEFAULT = false;
    private static final PreferenceChangeListener preferencesTracker = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || COMPLETION_CASE_SENSITIVE.equals(settingName)) {
                caseSensitive = preferences.getBoolean(COMPLETION_CASE_SENSITIVE, COMPLETION_CASE_SENSITIVE_DEFAULT);
            }
            if (settingName == null || JAVA_COMPLETION_SUBWORDS.equals(settingName)) {
                javaCompletionSubwords = preferences.getBoolean(JAVA_COMPLETION_SUBWORDS, JAVA_COMPLETION_SUBWORDS_DEFAULT);
            }
        }
    };
    private static final AtomicBoolean inited = new AtomicBoolean(false);

    private static Preferences preferences;
    private static boolean caseSensitive = COMPLETION_CASE_SENSITIVE_DEFAULT;
    private static boolean javaCompletionSubwords = JAVA_COMPLETION_SUBWORDS_DEFAULT;
    private static String cachedPrefix = null;
    private static Pattern cachedCamelCasePattern = null;
    private static Pattern cachedSubwordsPattern = null;

    @Override
    public boolean collectCompletions(Document doc, int offset, Completion.Context context, Consumer<Completion> consumer) {
        try {
            ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {
                private String prefix;
                private int anchorOffset;

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    CompilationController cc = CompilationController.get(resultIterator.getParserResult(offset));
                    if (cc != null) {
                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        int tokenOffset = anchorOffset = offset;
                        prefix = "";
                        TokenSequence<JavaTokenId> ts = cc.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                        if (ts.move(anchorOffset) == 0 || !ts.moveNext()) {
                            ts.movePrevious();
                        }
                        int len = anchorOffset - ts.offset();
                        if (len > 0 && ts.token().length() >= len) {
                            if (ts.token().id() == JavaTokenId.IDENTIFIER || ts.token().id().primaryCategory().startsWith("keyword") ||
                                    ts.token().id().primaryCategory().equals("literal")) {
                                prefix = ts.token().text().toString().substring(0, len);
                                tokenOffset = anchorOffset = ts.offset();
                            } else if (ts.token().id() == JavaTokenId.STRING_LITERAL) {
                                prefix = ts.token().text().toString().substring(1, Math.min(len, ts.token().length() - 1));
                                tokenOffset = ts.offset();
                                anchorOffset = tokenOffset + 1;
                            } else if (ts.token().id() == JavaTokenId.MULTILINE_STRING_LITERAL) {
                                prefix = ts.token().text().toString().substring(3, len);
                                tokenOffset = ts.offset();
                                anchorOffset = tokenOffset + 3;
                            }
                        }
                        TreeUtilities treeUtilities = cc.getTreeUtilities();
                        Trees trees = cc.getTrees();
                        SourcePositions sp = trees.getSourcePositions();
                        TreePath path = treeUtilities.pathFor(tokenOffset);
                        switch (path.getLeaf().getKind()) {
                            case ANNOTATION:
                            case TYPE_ANNOTATION:
                                Tree annotationType = ((AnnotationTree) path.getLeaf()).getAnnotationType();
                                int typeEndPos = (int) sp.getEndPosition(cc.getCompilationUnit(), annotationType);
                                if (tokenOffset > typeEndPos) {
                                    TokenSequence<JavaTokenId> last = findLastNonWhitespaceToken(cc, typeEndPos, tokenOffset);
                                    if (last != null && last.token().id() == JavaTokenId.LPAREN) {
                                        Element annTypeElement = trees.getElement(new TreePath(path, annotationType));
                                        ExecutableElement valueElement = null;
                                        for (Element e : ((TypeElement) annTypeElement).getEnclosedElements()) {
                                            if (e.getKind() == ElementKind.METHOD) {
                                                String name = e.getSimpleName().toString();
                                                if ("value".equals(name)) {
                                                    valueElement = (ExecutableElement) e;
                                                } else if (((ExecutableElement) e).getDefaultValue() == null) {
                                                    valueElement = null;
                                                }
                                            }
                                        }
                                        if (valueElement != null) {
                                            Element el = null;
                                            TreePath parentPath = path.getParentPath();
                                            if (parentPath.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                                                el = trees.getElement(parentPath);
                                            } else {
                                                parentPath = parentPath.getParentPath();
                                                Tree.Kind pKind = parentPath.getLeaf().getKind();
                                                if (TreeUtilities.CLASS_TREE_KINDS.contains(pKind) || pKind == Tree.Kind.METHOD || pKind == Tree.Kind.VARIABLE) {
                                                    el = trees.getElement(parentPath);
                                                }
                                            }
                                            if (el != null) {
                                                AnnotationMirror annotation = null;
                                                for (AnnotationMirror am : el.getAnnotationMirrors()) {
                                                    if (annTypeElement == am.getAnnotationType().asElement()) {
                                                        annotation = am;
                                                        break;
                                                    }
                                                }
                                                if (annotation != null) {
                                                    addAttributeValues(cc, el, annotation, valueElement);
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                            case ASSIGNMENT:
                                TreePath parentPath = path.getParentPath();
                                if (parentPath.getLeaf().getKind() == Tree.Kind.ANNOTATION) {
                                    ExpressionTree var = ((AssignmentTree) path.getLeaf()).getVariable();
                                    if (var.getKind() == Tree.Kind.IDENTIFIER) {
                                        int varEndPos = (int) sp.getEndPosition(cc.getCompilationUnit(), var);
                                        if (varEndPos > -1) {
                                            Tree expr = unwrapErrTree(((AssignmentTree) path.getLeaf()).getExpression());
                                            if (expr == null || tokenOffset <= (int) sp.getStartPosition(cc.getCompilationUnit(), expr)) {
                                                String asText = cc.getText().substring(varEndPos, tokenOffset);
                                                int eqPos = asText.indexOf('=');
                                                if (eqPos > -1) {
                                                    insideAnnotationAttribute(cc, parentPath, ((IdentifierTree) var).getName());
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                        }
                    }
                }

                private void insideAnnotationAttribute(CompilationController controller, TreePath annotationPath, Name attributeName) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    Trees trees = controller.getTrees();
                    AnnotationTree at = (AnnotationTree) annotationPath.getLeaf();
                    Element annTypeElement = trees.getElement(new TreePath(annotationPath, at.getAnnotationType()));
                    Element el = null;
                    TreePath pPath = annotationPath.getParentPath();
                    if (pPath.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                        el = trees.getElement(pPath);
                    } else {
                        pPath = pPath.getParentPath();
                        Tree.Kind pKind = pPath.getLeaf().getKind();
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(pKind) || pKind == Tree.Kind.METHOD || pKind == Tree.Kind.VARIABLE) {
                            el = trees.getElement(pPath);
                        }
                    }
                    if (el != null && annTypeElement != null && annTypeElement.getKind() == ElementKind.ANNOTATION_TYPE) {
                        ExecutableElement memberElement = null;
                        for (Element e : ((TypeElement) annTypeElement).getEnclosedElements()) {
                            if (e.getKind() == ElementKind.METHOD && attributeName.contentEquals(e.getSimpleName())) {
                                memberElement = (ExecutableElement) e;
                                break;
                            }
                        }
                        if (memberElement != null) {
                            AnnotationMirror annotation = null;
                            for (AnnotationMirror am : el.getAnnotationMirrors()) {
                                if (annTypeElement == am.getAnnotationType().asElement()) {
                                    annotation = am;
                                    break;
                                }
                            }
                            if (annotation != null) {
                                addAttributeValues(controller, el, annotation, memberElement);
                            }
                        }
                    }
                }

                private void addAttributeValues(CompilationController controller, Element element, AnnotationMirror annotation, ExecutableElement member) throws IOException {
                    for (javax.annotation.processing.Completion completion : getAttributeValueCompletions(element, annotation, member, prefix)) {
                        String value = completion.getValue().trim();
                        if (value.length() > 0 && startsWith(value, prefix)) {
                            consumer.accept(createAttributeValueItem(controller, value, completion.getMessage()));
                        }
                    }
                }

                private List<? extends javax.annotation.processing.Completion> getAttributeValueCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
                    List<javax.annotation.processing.Completion> completions = new LinkedList<>();
                    String fqn = ((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName().toString();
                    for (Processor processor : HARDCODED_PROCESSORS.lookupAll(Processor.class)) {
                        boolean match = false;
                        for (String sat : processor.getSupportedAnnotationTypes()) {
                            if ("*".equals(sat)) {
                                match = true;
                                break;
                            } else if (sat.endsWith(".*")) {
                                sat = sat.substring(0, sat.length() - 1);
                                if (fqn.startsWith(sat)) {
                                    match = true;
                                    break;
                                }
                            } else if (fqn.equals(sat)) {
                                match = true;
                                break;
                            }
                        }
                        if (match) {
                            try {
                                for (javax.annotation.processing.Completion c : processor.getCompletions(element, annotation, member, userText)) {
                                    completions.add(c);
                                }
                            } catch (Exception e) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                    }
                    return completions;
                }

                private Completion createAttributeValueItem(CompilationController controller, String value, String documentation) {
                    String label = value;
                    TextEdit textEdit = null;
                    if (value.startsWith("\"")) {
                        TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                        ts.move(offset);
                        if (ts.moveNext() && ts.offset() <= offset) {
                            switch (ts.token().id()) {
                                case STRING_LITERAL:
                                    int end = ts.offset() + ts.token().length() == offset + 1 ? offset + 1 : offset;
                                    textEdit = new TextEdit(ts.offset(), end, value);
                                    break;
                                case MULTILINE_STRING_LITERAL:
                                    String[] tokenLines = ts.token().text().toString().split("\n");
                                    String[] lines = value.split("\n");
                                    int cnt = 0;
                                    for (int i = 0; i < lines.length; i++) {
                                        if (i < tokenLines.length) {
                                            if (tokenLines[i].equals(lines[i])) {
                                                cnt += tokenLines[i].length() + 1;
                                            } else if (i == lines.length - 1) {
                                                label = lines[i].trim();
                                                textEdit = new TextEdit(ts.offset() + cnt, offset, lines[i]);
                                            }
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                    Builder builder = CompletionCollector.newBuilder(label)
                            .kind(Completion.Kind.Value)
                            .sortText(value)
                            .insertTextFormat(Completion.TextFormat.PlainText)
                            .documentation(documentation);
                    if (textEdit != null) {
                        builder.textEdit(textEdit);
                    }
                    return builder.build();
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return true;
    }

    private static TokenSequence<JavaTokenId> findLastNonWhitespaceToken(CompilationController controller, int start, int pos) {
        TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        ts.move(pos);
        while (ts.movePrevious() && ts.offset() >= start) {
            switch (ts.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                case JAVADOC_COMMENT:
                    break;
                default:
                    return ts;
            }
        }
        return null;
    }

    private static Tree unwrapErrTree(Tree tree) {
        if (tree != null && tree.getKind() == Tree.Kind.ERRONEOUS) {
            Iterator<? extends Tree> it = ((ErroneousTree) tree).getErrorTrees().iterator();
            tree = it.hasNext() ? it.next() : null;
        }
        return tree;
    }

    private static boolean startsWith(String theString, String prefix) {
        if (theString.startsWith("\"")) {
            theString = theString.substring(1);
        }
        return isCamelCasePrefix(prefix) ? isCaseSensitive()
                ? startsWithCamelCase(theString, prefix)
                : startsWithCamelCase(theString, prefix) || startsWithPlain(theString, prefix)
                : startsWithPlain(theString, prefix);
    }

    private static boolean isCamelCasePrefix(String prefix) {
        if (prefix == null || prefix.length() < 2 || prefix.charAt(0) == '"') {
            return false;
        }
        for (int i = 1; i < prefix.length(); i++) {
            if (Character.isUpperCase(prefix.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCaseSensitive() {
        lazyInit();
        return caseSensitive;
    }

    private static boolean isSubwordSensitive() {
        lazyInit();
        return javaCompletionSubwords;
    }

    private static boolean startsWithPlain(String theString, String prefix) {
        if (theString == null || theString.length() == 0) {
            return false;
        }
        if (prefix == null || prefix.length() == 0) {
            return true;
        }
        if (isSubwordSensitive()) {
            if (!prefix.equals(cachedPrefix)) {
                cachedCamelCasePattern = null;
                cachedSubwordsPattern = null;
            }
            if (cachedSubwordsPattern == null) {
                cachedPrefix = prefix;
                String patternString = createSubwordsPattern(prefix);
                cachedSubwordsPattern = patternString != null ? Pattern.compile(patternString) : null;
            }
            if (cachedSubwordsPattern != null && cachedSubwordsPattern.matcher(theString).matches()) {
                return true;
            }
        }
        return isCaseSensitive() ? theString.startsWith(prefix) : theString.toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH));
    }

    private static String createSubwordsPattern(String prefix) {
        StringBuilder sb = new StringBuilder(3 + 8 * prefix.length());
        sb.append(".*?");
        for (int i = 0; i < prefix.length(); i++) {
            char charAt = prefix.charAt(i);
            if (!Character.isJavaIdentifierPart(charAt)) {
                return null;
            }
            if (Character.isLowerCase(charAt)) {
                sb.append("[");
                sb.append(charAt);
                sb.append(Character.toUpperCase(charAt));
                sb.append("]");
            } else {
                //keep uppercase characters as beacons
                // for example: java.lang.System.sIn -> setIn
                sb.append(charAt);
            }
            sb.append(".*?");
        }
        return sb.toString();
    }

    private static boolean startsWithCamelCase(String theString, String prefix) {
        if (theString == null || theString.length() == 0 || prefix == null || prefix.length() == 0) {
            return false;
        }
        if (!prefix.equals(cachedPrefix)) {
            cachedCamelCasePattern = null;
            cachedSubwordsPattern = null;
        }
        if (cachedCamelCasePattern == null) {
            StringBuilder sb = new StringBuilder();
            int lastIndex = 0;
            int index;
            do {
                index = findNextUpper(prefix, lastIndex + 1);
                String token = prefix.substring(lastIndex, index == -1 ? prefix.length() : index);
                sb.append(token);
                sb.append(index != -1 ? "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*");
                lastIndex = index;
            } while (index != -1);
            cachedPrefix = prefix;
            cachedCamelCasePattern = Pattern.compile(sb.toString());
        }
        return cachedCamelCasePattern.matcher(theString).matches();
    }

    private static int findNextUpper(String text, int offset) {
        for (int i = offset; i < text.length(); i++) {
            if (Character.isUpperCase(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static void lazyInit() {
        if (inited.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, preferencesTracker, preferences));
            preferencesTracker.preferenceChange(null);
        }
    }
}

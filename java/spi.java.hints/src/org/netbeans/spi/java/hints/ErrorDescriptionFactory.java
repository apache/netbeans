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

package org.netbeans.spi.java.hints;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.analysis.api.CodeAnalysis;
import org.netbeans.modules.analysis.spi.Analyzer.WarningDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.spiimpl.Hacks.InspectAndTransformOpener;
import org.netbeans.modules.java.hints.spiimpl.SPIAccessor;
import org.netbeans.modules.java.hints.spiimpl.SyntheticFix;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.settings.FileHintPreferences;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Parameters;

/**
 *
 * @author Jan Lahoda
 */
public class ErrorDescriptionFactory {
    private static final Logger LOG = Logger.getLogger(ErrorDescriptionFactory.class.getName());
    
    private ErrorDescriptionFactory() {
    }

    public static ErrorDescription forTree(HintContext context, TreePath tree, String text, Fix... fixes) {
        return forTree(context, tree.getLeaf(), text, fixes);
    }
    
    public static ErrorDescription forTree(HintContext context, Tree tree, String text, Fix... fixes) {
        int start;
        int end;
        int javacEnd;
        
        if (context.getHintMetadata().kind == Hint.Kind.INSPECTION) {
            start = (int) context.getInfo().getTrees().getSourcePositions().getStartPosition(context.getInfo().getCompilationUnit(), tree);
            javacEnd = (int) context.getInfo().getTrees().getSourcePositions().getEndPosition(context.getInfo().getCompilationUnit(), tree);
            end = Math.min(javacEnd, findLineEnd(context.getInfo(), start));
        } else {
            start = javacEnd = end = context.getCaretLocation();
        }

        if (start != (-1) && end != (-1)) {
            if (start > end) {
                LOG.log(Level.WARNING, "Wrong positions reported for tree (start = {0}, end = {1}): {2}",
                    new Object[] {
                        start, end, 
                        tree
                    }
                );
            }
            LazyFixList fixesForED = org.netbeans.spi.editor.hints.ErrorDescriptionFactory.lazyListForFixes(resolveDefaultFixes(context, fixes));
            return org.netbeans.spi.editor.hints.ErrorDescriptionFactory.createErrorDescription("text/x-java:" + context.getHintMetadata().id, context.getSeverity(), text, context.getHintMetadata().description, fixesForED, context.getInfo().getFileObject(), start, end);
        }
        return null;
    }
    
    /**Create a new {@link ErrorDescription}. Severity is automatically inferred from the {@link HintContext},
     * and the {@link ErrorDescription} is created to be consistent with {@link ErrorDescription}s created
     * by the other factory methods in this class.
     * 
     * @param context from which the {@link org.netbeans.spi.editor.hints.Severity} and other properties are inferred.
     * @param start start of the warning
     * @param end end of the warning
     * @param text the warning text
     * @param fixes one or more {@link Fix}es to show shown to the user.
     * @return a standard {@link ErrorDescription} for use in Java source
     * @since 1.9
     */
    public static ErrorDescription forSpan(HintContext context, int start, int end, String text, Fix... fixes) {
        if (context.getHintMetadata().kind != Hint.Kind.INSPECTION) {
            start = end = context.getCaretLocation();
        }

        if (start != (-1) && end != (-1)) {
            LazyFixList fixesForED = org.netbeans.spi.editor.hints.ErrorDescriptionFactory.lazyListForFixes(resolveDefaultFixes(context, fixes));
            return org.netbeans.spi.editor.hints.ErrorDescriptionFactory.createErrorDescription("text/x-java:" + context.getHintMetadata().id, context.getSeverity(), text, context.getHintMetadata().description, fixesForED, context.getInfo().getFileObject(), start, end);
        }

        return null;
    }
    
    public static ErrorDescription forName(HintContext context, TreePath tree, String text, Fix... fixes) {
        return forName(context, tree.getLeaf(), text, fixes);
    }

    public static ErrorDescription forName(HintContext context, Tree tree, String text, Fix... fixes) {
        int[] span;
        
        if (context.getHintMetadata().kind == Hint.Kind.INSPECTION) {
            span = computeNameSpan(tree, context);
        } else {
            span = new int[] {context.getCaretLocation(), context.getCaretLocation()};
        }
        
        if (span != null && span[0] != (-1) && span[1] != (-1)) {
            LazyFixList fixesForED = org.netbeans.spi.editor.hints.ErrorDescriptionFactory.lazyListForFixes(resolveDefaultFixes(context, fixes));
            return org.netbeans.spi.editor.hints.ErrorDescriptionFactory.createErrorDescription("text/x-java:" + context.getHintMetadata().id, context.getSeverity(), text, context.getHintMetadata().description, fixesForED, context.getInfo().getFileObject(), span[0], span[1]);
        }

        return null;
    }

    private static int[] computeNameSpan(Tree tree, HintContext context) {
        CompilationInfo info = context.getInfo();
        switch (tree.getKind()) {
            case LABELED_STATEMENT -> {
                return info.getTreeUtilities().findNameSpan((LabeledStatementTree) tree);
            }
            case METHOD -> {
                return info.getTreeUtilities().findNameSpan((MethodTree) tree);
            }
            case ANNOTATION_TYPE, CLASS, ENUM, INTERFACE -> {
                return info.getTreeUtilities().findNameSpan((ClassTree) tree);
            }
            case VARIABLE -> {
                return info.getTreeUtilities().findNameSpan((VariableTree) tree);
            }
            case MEMBER_SELECT -> {
                //XXX:
                MemberSelectTree mst = (MemberSelectTree) tree;
                int[] span = info.getTreeUtilities().findNameSpan(mst);

                if (span == null) {
                    int end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree);
                    span = new int[] {end - mst.getIdentifier().length(), end};
                }
                return span;
            }
            case METHOD_INVOCATION -> {
                return computeNameSpan(((MethodInvocationTree) tree).getMethodSelect(), context);
            }
            default -> {
                if (tree.getKind() == Kind.BLOCK) {
                    Collection<? extends TreePath> prefix = context.getMultiVariables().get("$$1$");
                    if (prefix != null) {
                        BlockTree bt = (BlockTree) tree;
                        if (bt.getStatements().size() > prefix.size()) {
                            return computeNameSpan(bt.getStatements().get(prefix.size()), context);
                        }
                    }
                }
                int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree);
                if (StatementTree.class.isAssignableFrom(tree.getKind().asInterface())
                        && tree.getKind() != Kind.EXPRESSION_STATEMENT
                        && tree.getKind() != Kind.BLOCK) {
                    TokenSequence<?> ts = info.getTokenHierarchy().tokenSequence();
                    ts.move(start);
                    if (ts.moveNext()) {
                        return new int[] {ts.offset(), ts.offset() + ts.token().length()};
                    }
                }
                return new int[] {
                    start,
                    Math.min((int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree),
                             findLineEnd(info, start)),
                };
            }

        }
    }

    private static int findLineEnd(CompilationInfo info, int start) {
        String text = info.getText();

        for (int i = start + 1; i < text.length(); i++) {
            if (text.charAt(i) == '\n') return i;
        }

        return text.length();
    }

    static List<Fix> resolveDefaultFixes(HintContext ctx, Fix... provided) {
        List<Fix> auxiliaryFixes = new LinkedList<>();
        HintMetadata hm = SPIAccessor.getINSTANCE().getHintMetadata(ctx);

        if (hm != null) {
            Set<String> suppressWarningsKeys = new LinkedHashSet<>();

            for (String key : hm.suppressWarnings) {
                if (key == null || key.isEmpty()) {
                    break;
                }

                suppressWarningsKeys.add(key);
            }

            auxiliaryFixes.add(new DisableConfigure(ctx.getInfo().getFileObject(), hm, true, SPIAccessor.getINSTANCE().getHintSettings(ctx)));
            auxiliaryFixes.add(new DisableConfigure(ctx.getInfo().getFileObject(), hm, false, null));

            if (hm.kind == Hint.Kind.INSPECTION && !hm.options.contains(Options.NO_BATCH)) {
                auxiliaryFixes.add(new InspectFix(hm, false));
                if (!hm.options.contains(Options.QUERY)) {
                    auxiliaryFixes.add(new InspectFix(hm, true));
                }
            }
            
            if (!suppressWarningsKeys.isEmpty()) {
                auxiliaryFixes.addAll(createSuppressWarnings(ctx.getInfo(), ctx.getPath(), suppressWarningsKeys.toArray(String[]::new)));
            }

            List<Fix> result = new LinkedList<>();

            for (Fix f : provided != null ? provided : new Fix[0]) {
                if (f == null) continue;
                
                result.add(org.netbeans.spi.editor.hints.ErrorDescriptionFactory.attachSubfixes(f, auxiliaryFixes));
            }

            if (result.isEmpty()) {
                result.add(org.netbeans.spi.editor.hints.ErrorDescriptionFactory.attachSubfixes(new TopLevelConfigureFix(ctx.getInfo().getFileObject(), hm), auxiliaryFixes));
            }

            return result;
        }

        return Arrays.asList(provided);
    }

    private static class DisableConfigure implements Fix, SyntheticFix {
        private final @NonNull FileObject file;
        private final @NonNull HintMetadata metadata;
        private final boolean disable;
        private final HintsSettings hintsSettings;

        DisableConfigure(@NonNull FileObject file, @NonNull HintMetadata metadata, boolean disable, HintsSettings hintsSettings) {
            this.file = file;
            this.metadata = metadata;
            this.disable = disable;
            this.hintsSettings = hintsSettings;
        }

        @Override
        public String getText() {
            String displayName = metadata.displayName;
            String key = switch (metadata.kind) {
                case INSPECTION -> disable ? "FIX_DisableHint" : "FIX_ConfigureHint";
                case ACTION -> disable ? "FIX_DisableSuggestion" : "FIX_ConfigureSuggestion";
            };
            return NbBundle.getMessage(ErrorDescriptionFactory.class, key, displayName);
        }

        @Override
        public ChangeInfo implement() throws Exception {
            if (disable) {
                hintsSettings.setEnabled(metadata, false);
                //XXX: re-run hints task
            } else {
                FileHintPreferences.openFilePreferences(file, "text/x-java", metadata.id);
            }

            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final DisableConfigure other = (DisableConfigure) obj;
            if (this.metadata != other.metadata && (this.metadata == null || !this.metadata.equals(other.metadata))) {
                return false;
            }
            if (this.disable != other.disable) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 43 * hash + (this.metadata != null ? this.metadata.hashCode() : 0);
            hash = 43 * hash + (this.disable ? 1 : 0);
            return hash;
        }


    }

    private static final class TopLevelConfigureFix extends DisableConfigure implements EnhancedFix {

        public TopLevelConfigureFix(@NonNull FileObject file, @NonNull HintMetadata metadata) {
            super(file, metadata, false, null);
        }

        @Override
        public CharSequence getSortText() {
            return "\uFFFFzz";
        }
        
    }

    private record InspectFix(@NonNull HintMetadata metadata, boolean transform) implements Fix, SyntheticFix {

        @Override
        @Messages({
            "DN_InspectAndTransform=Run Inspect&Transform on...",
            "DN_Inspect=Run Inspect on..."
        })
        public String getText() {
            return transform ? Bundle.DN_InspectAndTransform() : Bundle.DN_Inspect();
        }

        @Override
        public ChangeInfo implement() throws Exception {
            SwingUtilities.invokeLater(() -> {
                if (transform) {
                    final InspectAndTransformOpener o = Lookup.getDefault().lookup(InspectAndTransformOpener.class);
                    
                    if (o != null) {
                        o.openIAT(metadata);
                    } else {
                        //warn
                    }
                } else {
                    CodeAnalysis.open(WarningDescription.create("text/x-java:" + metadata.id, null, null, null));
                }
            });
            
            return null;
        }

    }
    
    /** Creates a fix, which when invoked adds @SuppresWarnings(keys) to
     * nearest declaration.
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a tree. The method will find nearest outer
     *        declaration. (type, method, field or local variable)
     * @param keys keys to be contained in the SuppresWarnings annotation. E.g.
     *        @SuppresWarnings( "key" ) or @SuppresWarnings( {"key1", "key2", ..., "keyN" } ).
     * @throws IllegalArgumentException if keys are null or empty or id no suitable element
     *         to put the annotation on is found (e.g. if TreePath to CompilationUnit is given")
     */
    static Fix createSuppressWarningsFix(CompilationInfo compilationInfo, TreePath treePath, String... keys ) {
        Parameters.notNull("compilationInfo", compilationInfo);
        Parameters.notNull("treePath", treePath);
        Parameters.notNull("keys", keys);

        if (keys.length == 0) {
            throw new IllegalArgumentException("key must not be empty"); // NOI18N
        }

        if (!isSuppressWarningsSupported(compilationInfo)) {
            return null;
        }

        while (treePath.getLeaf().getKind() != Kind.COMPILATION_UNIT && !DECLARATION.contains(treePath.getLeaf().getKind())) {
            treePath = treePath.getParentPath();
        }

        if (treePath.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
            return new FixImpl(TreePathHandle.create(treePath, compilationInfo), compilationInfo.getFileObject(), keys);
        } else {
            return null;
        }
    }

    /** Creates a fix, which when invoked adds @SuppresWarnings(keys) to
     * nearest declaration.
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a tree. The method will find nearest outer
     *        declaration. (type, method, field or local variable)
     * @param keys keys to be contained in the SuppresWarnings annotation. E.g.
     *        @SuppresWarnings( "key" ) or @SuppresWarnings( {"key1", "key2", ..., "keyN" } ).
     * @throws IllegalArgumentException if keys are null or empty or id no suitable element
     *         to put the annotation on is found (e.g. if TreePath to CompilationUnit is given")
     */
    static List<Fix> createSuppressWarnings(CompilationInfo compilationInfo, TreePath treePath, String... keys ) {
        Parameters.notNull("compilationInfo", compilationInfo);
        Parameters.notNull("treePath", treePath);
        Parameters.notNull("keys", keys);

        if (keys.length == 0) {
            throw new IllegalArgumentException("key must not be empty"); // NOI18N
        }

        Fix f = createSuppressWarningsFix(compilationInfo, treePath, keys);
        return f != null ? List.of(f) : List.of();
    }

    private static boolean isSuppressWarningsSupported(CompilationInfo info) {
        //cannot suppress if there is no SuppressWarnings annotation in the platform:
        if (info.getElements().getTypeElement("java.lang.SuppressWarnings") == null)
            return false;

        return info.getSourceVersion().compareTo(SourceVersion.RELEASE_5) >= 0;
    }

    private static final Set<Kind> DECLARATION = EnumSet.of(Kind.ANNOTATION_TYPE, Kind.CLASS, Kind.ENUM, Kind.INTERFACE, Kind.METHOD, Kind.VARIABLE);

    private static final class FixImpl implements Fix, SyntheticFix {

        private final String keys[];
        private final TreePathHandle handle;
        private final FileObject file;

        public FixImpl(TreePathHandle handle, FileObject file, String... keys) {
            this.keys = keys;
            this.handle = handle;
            this.file = file;
        }

        @Override
        public String getText() {
            StringBuilder keyNames = new StringBuilder();
            for (int i = 0; i < keys.length; i++) {
                String string = keys[i];
                keyNames.append(string);
                if ( i < keys.length - 1) {
                    keyNames.append(", "); // NOI18N
                }
            }

            return NbBundle.getMessage(ErrorDescriptionFactory.class, "LBL_FIX_Suppress_Waning",  keyNames.toString() );  // NOI18N
        }

        @Override
        public ChangeInfo implement() throws IOException {
            JavaSource js = JavaSource.forFileObject(file);

            js.runModificationTask((WorkingCopy copy) -> {
                copy.toPhase(Phase.RESOLVED); //XXX: performance
                TreePath path = handle.resolve(copy);

                while (path != null && path.getLeaf().getKind() != Kind.COMPILATION_UNIT && !DECLARATION.contains(path.getLeaf().getKind())) {
                    path = path.getParentPath();
                }

                if (path == null || path.getLeaf().getKind() == Kind.COMPILATION_UNIT) {
                    return ;
                }

                Tree top = path.getLeaf();
                TreePath lambdaPath = null;

                ModifiersTree modifiers = switch (top.getKind()) {
                    case ANNOTATION_TYPE, CLASS, ENUM, INTERFACE -> ((ClassTree) top).getModifiers();
                    case METHOD -> ((MethodTree) top).getModifiers();
                    case VARIABLE -> {
                        TreePath parent = path.getParentPath();
                        if (parent != null && parent.getLeaf().getKind() == Tree.Kind.LAMBDA_EXPRESSION) {
                            // check if the variable is an implict parameter. If so, it must be turned into explicit
                            TreePath typePath = TreePath.getPath(parent, ((VariableTree)top).getType());
                            if (copy.getTreeUtilities().isSynthetic(typePath)) {
                                lambdaPath = parent;
                            }
                        }
                        yield ((VariableTree) top).getModifiers();
                    }
                    default -> {
                        assert false : "Unhandled Tree.Kind";  // NOI18N
                        yield null;
                    }
                };

                if (modifiers == null) {
                    return ;
                }

                TypeElement el = copy.getElements().getTypeElement("java.lang.SuppressWarnings");  // NOI18N

                if (el == null) {
                    return ;
                }

                LiteralTree[] keyLiterals = new LiteralTree[keys.length];

                for (int i = 0; i < keys.length; i++) {
                    keyLiterals[i] = copy.getTreeMaker().
                            Literal(keys[i]);
                }

                if (lambdaPath != null) {
                    LambdaExpressionTree let = (LambdaExpressionTree)lambdaPath.getLeaf();
                    for (VariableTree var : let.getParameters()) {
                        TreePath typePath = TreePath.getPath(lambdaPath, var.getType());
                        if (copy.getTreeUtilities().isSynthetic(typePath)) {
                            Tree imported = copy.getTreeMaker().Type(copy.getTrees().getTypeMirror(typePath));
                            copy.rewrite(var.getType(), imported);
                        }
                    }
                }

                ModifiersTree nueMods = GeneratorUtilities.get(copy).appendToAnnotationValue(modifiers, el, "value", keyLiterals);

                copy.rewrite(modifiers, nueMods);
            }).commit();

            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FixImpl other = (FixImpl) obj;
            if (!Arrays.deepEquals(this.keys, other.keys)) {
                return false;
            }
            if (this.handle != other.handle && (this.handle == null || !this.handle.equals(other.handle))) {
                return false;
            }
            if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + Arrays.deepHashCode(this.keys);
            hash = 79 * hash + (this.handle != null ? this.handle.hashCode() : 0);
            hash = 79 * hash + (this.file != null ? this.file.hashCode() : 0);
            return hash;
        }
    }
}

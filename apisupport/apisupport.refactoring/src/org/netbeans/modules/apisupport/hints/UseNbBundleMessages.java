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

package org.netbeans.modules.apisupport.hints;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import static org.netbeans.modules.apisupport.hints.Bundle.*;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;

// XXX add new hint to supply documentation for undocumented format params

@Hint(category="apisupport", displayName="#UseNbBundleMessages.displayName", description="#UseNbBundleMessages.description", severity=Severity.HINT)
@Messages({
    "UseNbBundleMessages.displayName=Use @NbBundle.Messages",
    "UseNbBundleMessages.description=Use @NbBundle.Messages in preference to Bundle.properties plus NbBundle.getMessage(...)."
})
public class UseNbBundleMessages {

    // XXX rewrite METHOD_INVOCATION branch to use @TriggerPattern
    @TriggerTreeKind({Kind.METHOD_INVOCATION, Kind.ASSIGNMENT})
    @Messages({
        "UseNbBundleMessages.error_text=Use of external bundle key",
        "UseNbBundleMessages.only_class_const=Use of NbBundle.getMessage without ThisClass.class syntax",
        "# {0} - top-level class name", "UseNbBundleMessages.wrong_class_name=Expected argument to be {0}.class",
        "UseNbBundleMessages.only_string_const=Use of NbBundle.getMessage with nonconstant key",
        "# {0} - resource path", "UseNbBundleMessages.no_such_bundle=Could not locate {0} in source path",
        "# {0} - bundle key", "UseNbBundleMessages.no_such_key=Bundle.properties does not contain any key ''{0}''",
        "UseNbBundleMessages.save_bundle=Save modifications to Bundle.properties before using this hint"
    })
    public static List<ErrorDescription> run(HintContext context) {
        final CompilationInfo compilationInfo = context.getInfo();
        TreePath treePath = context.getPath();
        Tree tree = treePath.getLeaf();
        int[] span;
        final String key;
        final FileObject src = compilationInfo.getFileObject();
        MethodInvocationTree mit;
        if (tree.getKind() == Kind.METHOD_INVOCATION) {
            mit = (MethodInvocationTree) tree;
            ExpressionTree methodSelect = mit.getMethodSelect();
            if (methodSelect.getKind() != Kind.MEMBER_SELECT) {
                return null;
            }
            MemberSelectTree mst = (MemberSelectTree) methodSelect;
            if (!mst.getIdentifier().contentEquals("getMessage")) {
                return null;
            }
            TypeMirror invoker = compilationInfo.getTrees().getTypeMirror(new TreePath(treePath, mst.getExpression()));
            if (!String.valueOf(invoker).equals("org.openide.util.NbBundle")) {
                return null;
            }
            FileObject file = compilationInfo.getFileObject();
            if (file != null && file.getNameExt().equals("Bundle.java")) {
                return null;
            }
            span = compilationInfo.getTreeUtilities().findNameSpan(mst);
            if (span == null) {
                return null;
            }
            List<? extends ExpressionTree> args = mit.getArguments();
            if (args.size() < 2) {
                return null; // something unexpected
            }
            if (args.get(0).getKind() != Kind.MEMBER_SELECT) {
                return warning(UseNbBundleMessages_only_class_const(), span, compilationInfo);
            }
            MemberSelectTree thisClassMST = (MemberSelectTree) args.get(0);
            if (!thisClassMST.getIdentifier().contentEquals("class")) {
                return warning(UseNbBundleMessages_only_class_const(), span, compilationInfo);
            }
            if (thisClassMST.getExpression().getKind() != Kind.IDENTIFIER) {
                return warning(UseNbBundleMessages_only_class_const(), span, compilationInfo);
            }
            if (!((IdentifierTree) thisClassMST.getExpression()).getName().contentEquals(src.getName())) {
                return warning(UseNbBundleMessages_wrong_class_name(src.getName()), span, compilationInfo);
            }
            if (args.get(1).getKind() != Kind.STRING_LITERAL) {
                return warning(UseNbBundleMessages_only_string_const(), span, compilationInfo);
            }
            key = ((LiteralTree) args.get(1)).getValue().toString();
        } else {
            if (treePath.getParentPath().getLeaf().getKind() != Kind.ANNOTATION) {
                return null;
            }
            final AssignmentTree at = (AssignmentTree) tree;
            if (at.getExpression().getKind() != Kind.STRING_LITERAL) {
                return null;
            }
            String literal = ((LiteralTree) at.getExpression()).getValue().toString();
            if (!literal.startsWith("#")) {
                return null;
            }
            key = literal.substring(1);
            // at.variable iof IdentifierTree, not VariableTree, so TreeUtilities.findNameSpan cannot be used
            SourcePositions sp = compilationInfo.getTrees().getSourcePositions();
            span = new int[] {(int) sp.getStartPosition(compilationInfo.getCompilationUnit(), tree), (int) sp.getEndPosition(compilationInfo.getCompilationUnit(), tree)};
            mit = null;
        }
        if (compilationInfo.getClasspathInfo().getClassPath(PathKind.COMPILE).findResource("org/openide/util/NbBundle$Messages.class") == null) {
            // Using an older version of NbBundle.
            return null;
        }
        final boolean isAlreadyRegistered = isAlreadyRegistered(treePath, key);
        final FileObject bundleProperties;
        if (isAlreadyRegistered) {
            if (mit == null) {
                return null; // nothing to do
            } // else still need to convert getMessage call
            bundleProperties = null;
        } else {
            String bundleResource = compilationInfo.getCompilationUnit().getPackageName().toString().replace('.', '/') + "/Bundle.properties";
            bundleProperties = compilationInfo.getClasspathInfo().getClassPath(PathKind.SOURCE).findResource(bundleResource);
            if (bundleProperties == null) {
                return warning(UseNbBundleMessages_no_such_bundle(bundleResource), span, compilationInfo);
            }
            EditableProperties ep = new EditableProperties(true);
            try {
                if (DataObject.find(bundleProperties).isModified()) {
                    // Using EditorCookie.document is quite difficult here due to encoding issues. Keep it simple.
                    // XXX consider proceeding anyway, since TransformationContext should load from modified content
                    return warning(UseNbBundleMessages_save_bundle(), span, compilationInfo);
                }
                InputStream is = bundleProperties.getInputStream();
                try {
                    ep.load(is);
                } finally {
                    is.close();
                }
            } catch (IOException x) {
                Exceptions.printStackTrace(x);
                return null;
            }
            if (!ep.containsKey(key)) {
                return warning(UseNbBundleMessages_no_such_key(key), span, compilationInfo);
            }
        }
        return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, UseNbBundleMessages_error_text(), Collections.<Fix>singletonList(new UseMessagesFix(compilationInfo, treePath, isAlreadyRegistered, key, bundleProperties).toEditorFix()), compilationInfo.getFileObject(), span[0], span[1]));
    }

    private static class UseMessagesFix extends JavaFix {

        private final boolean isAlreadyRegistered;
        private final String key;
        private final FileObject bundleProperties;

        public UseMessagesFix(CompilationInfo compilationInfo, TreePath treePath, boolean isAlreadyRegistered, String key, FileObject bundleProperties) {
            super(compilationInfo, treePath);
            this.isAlreadyRegistered = isAlreadyRegistered;
            this.key = key;
            this.bundleProperties = bundleProperties;
        }

            @Override protected String getText() {
                return UseNbBundleMessages_displayName();
            }

            @Override protected void performRewrite(JavaFix.TransformationContext ctx) throws Exception {
                WorkingCopy wc = ctx.getWorkingCopy();
                TreePath treePath = ctx.getPath();
                        TreeMaker make = wc.getTreeMaker();
                        if (treePath.getLeaf().getKind() == Kind.METHOD_INVOCATION) {
                            MethodInvocationTree mit = (MethodInvocationTree) treePath.getLeaf();
                            CompilationUnitTree cut = wc.getCompilationUnit();
                            boolean imported = false;
                            String importBundleStar = cut.getPackageName() + ".Bundle.*";
                            for (ImportTree it : cut.getImports()) {
                                if (it.isStatic() && it.getQualifiedIdentifier().toString().equals(importBundleStar)) {
                                    imported = true;
                                    break;
                                }
                            }
                            if (!imported) {
                                wc.rewrite(cut, make.addCompUnitImport(cut, make.Import(make.Identifier(importBundleStar), true)));
                            }
                            List<? extends ExpressionTree> args = mit.getArguments();
                            List<? extends ExpressionTree> params;
                            if (args.size() == 3 && args.get(2).getKind() == Kind.NEW_ARRAY) {
                                params = ((NewArrayTree) args.get(2)).getInitializers();
                            } else {
                                params = args.subList(2, args.size());
                            }
                            wc.rewrite(mit, make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier(toIdentifier(key)), params));
                        } // else annotation value, nothing to change
                        if (!isAlreadyRegistered) {
                            EditableProperties ep = new EditableProperties(true);
                            InputStream is = ctx.getResourceContent(bundleProperties);
                            try {
                                ep.load(is);
                            } finally {
                                is.close();
                            }
                            List<ExpressionTree> lines = new ArrayList<ExpressionTree>();
                            for (String comment : ep.getComment(key)) {
                                lines.add(make.Literal(comment));
                            }
                            lines.add(make.Literal(key + '=' + ep.remove(key)));
                            TypeElement nbBundleMessages = wc.getElements().getTypeElement("org.openide.util.NbBundle.Messages");
                            if (nbBundleMessages == null) {
                                throw new IllegalArgumentException("cannot resolve org.openide.util.NbBundle.Messages");
                            }
                            GeneratorUtilities gu = GeneratorUtilities.get(wc);
                            Tree enclosing = findEnclosingElement(wc, treePath);
                            Tree modifiers;
                            Tree nueModifiers;
                            ExpressionTree[] linesA = lines.toArray(new ExpressionTree[0]);
                            switch (enclosing.getKind()) {
                            case METHOD:
                                modifiers = wc.resolveRewriteTarget(((MethodTree) enclosing).getModifiers());
                                nueModifiers = gu.appendToAnnotationValue((ModifiersTree) modifiers, nbBundleMessages, "value", linesA);
                                break;
                            case VARIABLE:
                                modifiers = wc.resolveRewriteTarget(((VariableTree) enclosing).getModifiers());
                                nueModifiers = gu.appendToAnnotationValue((ModifiersTree) modifiers, nbBundleMessages, "value", linesA);
                                break;
                            case COMPILATION_UNIT:
                                modifiers = wc.resolveRewriteTarget(enclosing);
                                nueModifiers = gu.appendToAnnotationValue((CompilationUnitTree) modifiers, nbBundleMessages, "value", linesA);
                                break;
                            default:
                                modifiers = wc.resolveRewriteTarget(((ClassTree) enclosing).getModifiers());
                                nueModifiers = gu.appendToAnnotationValue((ModifiersTree) modifiers, nbBundleMessages, "value", linesA);
                            }
                            wc.rewrite(modifiers, nueModifiers);
                        // XXX remove NbBundle import if now unused
                        OutputStream os = ctx.getResourceOutput(bundleProperties);
                        try {
                            ep.store(os);
                        } finally {
                            os.close();
                        }
                    }
                // XXX after JavaFix rewrite, Savable.save (on DataObject.find(src)) no longer works (JG13 again)
            }
                    private static Tree findEnclosingElement(WorkingCopy wc, TreePath treePath) {
                        Tree leaf = treePath.getLeaf();
                        Kind kind = leaf.getKind();
                        switch (kind) {
                        case CLASS:
                        case ENUM:
                        case INTERFACE:
                        case ANNOTATION_TYPE:
                        case METHOD: // (or constructor)
                        case VARIABLE:
                            Element e = wc.getTrees().getElement(treePath);
                            if (e != null && !wc.getElementUtilities().isLocal(e)) {
                                TypeElement type = TreeUtilities.CLASS_TREE_KINDS.contains(kind) ? (TypeElement) e : wc.getElementUtilities().enclosingTypeElement(e);
                                if (type == null || !wc.getElementUtilities().isLocal(type)) {
                                    return leaf;
                                } // else part of an inner class
                            }
                            break;
                        case COMPILATION_UNIT:
                            return leaf;
                        }
                        TreePath parentPath = treePath.getParentPath();
                        if (parentPath == null) {
                            return null;
                        }
                        return findEnclosingElement(wc, parentPath);
                    }
        }

    private static List<ErrorDescription> warning(String text, int[] span, CompilationInfo compilationInfo) {
        return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, text, Collections.<Fix>emptyList(), compilationInfo.getFileObject(), span[0], span[1]));
    }

    // Copied from NbBundleProcessor
    private static String toIdentifier(String key) {
        if (Utilities.isJavaIdentifier(key)) {
            return key;
        } else {
            String i = key.replaceAll("[^\\p{javaJavaIdentifierPart}]+", "_");
            if (Utilities.isJavaIdentifier(i)) {
                return i;
            } else {
                return "_" + i;
            }
        }
    }

    private static boolean isAlreadyRegistered(TreePath treePath, String key) {
        ModifiersTree modifiers;
        Tree tree = treePath.getLeaf();
        switch (tree.getKind()) {
        case METHOD:
            modifiers = ((MethodTree) tree).getModifiers();
            break;
        case VARIABLE:
            modifiers = ((VariableTree) tree).getModifiers();
            break;
        case CLASS:
        case ENUM:
        case INTERFACE:
        case ANNOTATION_TYPE:
            modifiers = ((ClassTree) tree).getModifiers();
            break;
        default:
            modifiers = null;
        }
        if (modifiers != null) {
            for (AnnotationTree ann : modifiers.getAnnotations()) {
                Tree annotationType = ann.getAnnotationType();
                if (annotationType.toString().matches("((org[.]openide[.]util[.])?NbBundle[.])?Messages")) { // XXX see above
                    List<? extends ExpressionTree> args = ann.getArguments();
                    if (args.size() != 1) {
                        continue; // ?
                    }
                    AssignmentTree assign = (AssignmentTree) args.get(0);
                    if (!assign.getVariable().toString().equals("value")) {
                        continue; // ?
                    }
                    ExpressionTree arg = assign.getExpression();
                    if (arg.getKind() == Tree.Kind.STRING_LITERAL) {
                        if (isRegistered(key, arg)) {
                            return true;
                        }
                    } else if (arg.getKind() == Tree.Kind.NEW_ARRAY) {
                        for (ExpressionTree elt : ((NewArrayTree) arg).getInitializers()) {
                            if (isRegistered(key, elt)) {
                                return true;
                            }
                        }
                    } else {
                        // ?
                    }
                }
            }
        }
        TreePath parentPath = treePath.getParentPath();
        if (parentPath == null) {
            return false;
        }
        // XXX better to check all sources in the same package
        return isAlreadyRegistered(parentPath, key);
    }
    private static boolean isRegistered(String key, ExpressionTree expr) {
        return expr.getKind() == Kind.STRING_LITERAL && ((LiteralTree) expr).getValue().toString().startsWith(key + "=");
    }

    private UseNbBundleMessages() {}

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.javadoc.hints;

import com.sun.javadoc.Doc;
import com.sun.javadoc.MethodDoc;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import static com.sun.source.tree.Tree.Kind.ANNOTATION_TYPE;
import static com.sun.source.tree.Tree.Kind.CLASS;
import static com.sun.source.tree.Tree.Kind.ENUM;
import static com.sun.source.tree.Tree.Kind.INTERFACE;
import static com.sun.source.tree.Tree.Kind.METHOD;
import static com.sun.source.tree.Tree.Kind.VARIABLE;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Pokorsky
 */
public class JavadocUtilities {
    private static final String ERROR_IDENT = "<error>"; //NOI18N
    
    private JavadocUtilities() {
    }

    public static boolean isDeprecated(CompilationInfo javac, Element elm) {
        return findDeprecated(javac, elm) != null;
    }
        
    public static AnnotationMirror findDeprecated(CompilationInfo javac, Element elm) {
        TypeElement deprAnn = javac.getElements().getTypeElement("java.lang.Deprecated"); //NOI18N
        if (deprAnn == null) {
            String msg = String.format("Even though the source level of %s" + //NOI18N
                    " is set to JDK5 or later, java.lang.Deprecated cannot" + //NOI18N
                    " be found on the bootclasspath: %s", //NOI18N
                    javac.getClasspathInfo().getClassPath(PathKind.SOURCE),
                    javac.getClasspathInfo().getClassPath(PathKind.BOOT));
            Logger.getLogger(JavadocUtilities.class.getName()).warning(msg);
            return null;
        }
        for (AnnotationMirror annotationMirror : javac.getElements().getAllAnnotationMirrors(elm)) {
            if (deprAnn.equals(annotationMirror.getAnnotationType().asElement())) {
                return annotationMirror;
            }
        }
        return null;
    }
    
    public static boolean hasInheritedDoc(CompilationInfo javac, Element elm) {
        return findInheritedDoc(javac, elm) != null;
    }

    public static MethodDoc findInheritedDoc(CompilationInfo javac, Element elm) {
        if (elm.getKind() == ElementKind.METHOD) {
            TypeElement clazz = (TypeElement) elm.getEnclosingElement();
            return searchInInterfaces(javac, clazz, clazz,
                    (ExecutableElement) elm, new HashSet<TypeElement>());
        }
        return null;
    }
    
    /**
     * <a href="http://java.sun.com/javase/6/docs/technotes/tools/solaris/javadoc.html#inheritingcomments">
     * Algorithm for Inheriting Method Comments
     * </a>
     * <p>Do not use MethodDoc.overriddenMethod() instead since it fails for
     * interfaces!
     */
    private static MethodDoc searchInInterfaces(
            CompilationInfo javac, TypeElement class2query, TypeElement overriderClass,
            ExecutableElement overrider, Set<TypeElement> exclude) {
        
        // Step 1
        for (TypeMirror ifceMirror : class2query.getInterfaces()) {
            if (ifceMirror.getKind() == TypeKind.DECLARED) {
                TypeElement ifceEl = (TypeElement) ((DeclaredType) ifceMirror).asElement();
                if (exclude.contains(ifceEl)) {
                    continue;
                }
                // check methods
                MethodDoc jdoc = searchInMethods(javac, ifceEl, overriderClass, overrider);
                if (jdoc != null) {
                    return jdoc;
                }
                exclude.add(ifceEl);
            }
        }
        // Step 2
        for (TypeMirror ifceMirror : class2query.getInterfaces()) {
            if (ifceMirror.getKind() == TypeKind.DECLARED) {
                TypeElement ifceEl = (TypeElement) ((DeclaredType) ifceMirror).asElement();
                MethodDoc jdoc = searchInInterfaces(javac, ifceEl, overriderClass, overrider, exclude);
                if (jdoc != null) {
                    return jdoc;
                }
            }
        }
        // Step 3
        return searchInSuperclass(javac, class2query, overriderClass, overrider, exclude);
    }
    
    private static MethodDoc searchInSuperclass(
            CompilationInfo javac, TypeElement class2query, TypeElement overriderClass,
            ExecutableElement overrider, Set<TypeElement> exclude) {
        
        // Step 3a
        TypeMirror superclassMirror = class2query.getSuperclass();
        if (superclassMirror.getKind() != TypeKind.DECLARED) {
            return null;
        }
        TypeElement superclass = (TypeElement) ((DeclaredType) superclassMirror).asElement();
        // check methods
        MethodDoc jdoc = searchInMethods(javac, superclass, overriderClass, overrider);
        if (jdoc != null) {
            return jdoc;
        }
        
        // Step 3b
        return searchInInterfaces(javac, superclass, overriderClass, overrider, exclude);
    }
    
    private static MethodDoc searchInMethods(
            CompilationInfo javac, TypeElement class2query,
            TypeElement overriderClass, ExecutableElement overrider) {
        
        for (Element elm : class2query.getEnclosedElements()) {
            if (elm.getKind() == ElementKind.METHOD &&
                    javac.getElements().overrides(overrider, (ExecutableElement) elm, overriderClass)) {
                Doc jdoc = javac.getElementUtilities().javaDocFor(elm);
                return (jdoc != null && jdoc.getRawCommentText().length() > 0)?
                    (MethodDoc) jdoc: null;
            }
        }
        return null;
    }
    
    static boolean isValid(CompilationInfo javac, TreePath path, Severity severity, Access access, int caret) {
        Tree leaf = path.getLeaf();
        boolean onLine = severity == Severity.HINT && caret > -1;
        switch (leaf.getKind()) {
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                return access.isAccessible(javac, path, false) && (!onLine || isInHeader(javac, (ClassTree) leaf, caret));
            case METHOD:
                return access.isAccessible(javac, path, false) && (!onLine || isInHeader(javac, (MethodTree) leaf, caret));
            case VARIABLE:
                return access.isAccessible(javac, path, false);
        }
        return false;
    }
    
    public static boolean isGuarded(Tree node, CompilationInfo javac, Document doc) {
        GuardedSectionManager guards = GuardedSectionManager.getInstance((StyledDocument) doc);
        if (guards != null) {
            try {
                final int startOff = (int) javac.getTrees().getSourcePositions().
                        getStartPosition(javac.getCompilationUnit(), node);
                final Position startPos = doc.createPosition(startOff);

                for (GuardedSection guard : guards.getGuardedSections()) {
                    if (guard.contains(startPos, false)) {
                        return true;
                    }
                }
            } catch (BadLocationException ex) {
                Logger.getLogger(Analyzer.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                // consider it as guarded
                return true;
            }
        }
        return false;
    }
    
    /**
     * has syntax errors preventing to generate javadoc?
     */
    public static boolean hasErrors(Tree leaf) {
        switch (leaf.getKind()) {
            case METHOD:
                MethodTree mt = (MethodTree) leaf;
                Tree rt = mt.getReturnType();
                if (rt != null && rt.getKind() == Tree.Kind.ERRONEOUS) {
                    return true;
                }
                if (ERROR_IDENT.contentEquals(mt.getName())) {
                    return true;
                }
                for (VariableTree vt : mt.getParameters()) {
                    if (ERROR_IDENT.contentEquals(vt.getName())) {
                        return true;
                    }
                }
                for (Tree t : mt.getThrows()) {
                    if (t.getKind() == Tree.Kind.ERRONEOUS ||
                            (t.getKind() == Tree.Kind.IDENTIFIER && ERROR_IDENT.contentEquals(((IdentifierTree) t).getName()))) {
                        return true;
                    }
                }
                break;

            case VARIABLE:
                VariableTree vt = (VariableTree) leaf;
                return vt.getType().getKind() == Tree.Kind.ERRONEOUS
                        || ERROR_IDENT.contentEquals(vt.getName());

            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                ClassTree ct = (ClassTree) leaf;
                if (ERROR_IDENT.contentEquals(ct.getSimpleName())) {
                    return true;
                }
                for (TypeParameterTree tpt : ct.getTypeParameters()) {
                    if (ERROR_IDENT.contentEquals(tpt.getName())) {
                        return true;
                    }
                }
                break;

        }
        return false;
    }
        
    private static boolean isInHeader(CompilationInfo info, ClassTree tree, int offset) {
        CompilationUnitTree cut = info.getCompilationUnit();
        SourcePositions sp = info.getTrees().getSourcePositions();
        long lastKnownOffsetInHeader = sp.getStartPosition(cut, tree);
        
        List<? extends Tree> impls = tree.getImplementsClause();
        List<? extends TypeParameterTree> typeparams;
        if (impls != null && !impls.isEmpty()) {
            lastKnownOffsetInHeader= sp.getEndPosition(cut, impls.get(impls.size() - 1));
        } else if ((typeparams = tree.getTypeParameters()) != null && !typeparams.isEmpty()) {
            lastKnownOffsetInHeader= sp.getEndPosition(cut, typeparams.get(typeparams.size() - 1));
        } else if (tree.getExtendsClause() != null) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, tree.getExtendsClause());
        } else if (tree.getModifiers() != null) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, tree.getModifiers());
        }
        
        TokenSequence<JavaTokenId> ts = info.getTreeUtilities().tokensFor(tree);
        
        ts.move((int) lastKnownOffsetInHeader);
        
        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.LBRACE) {
                return offset < ts.offset();
            }
        }
        
        return false;
    }
    
    private static boolean isInHeader(CompilationInfo info, MethodTree tree, int offset) {
        CompilationUnitTree cut = info.getCompilationUnit();
        SourcePositions sp = info.getTrees().getSourcePositions();
        long lastKnownOffsetInHeader = sp.getStartPosition(cut, tree);
        
        List<? extends ExpressionTree> throwz;
        List<? extends VariableTree> params;
        List<? extends TypeParameterTree> typeparams;
        
        if ((throwz = tree.getThrows()) != null && !throwz.isEmpty()) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, throwz.get(throwz.size() - 1));
        } else if ((params = tree.getParameters()) != null && !params.isEmpty()) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, params.get(params.size() - 1));
        } else if ((typeparams = tree.getTypeParameters()) != null && !typeparams.isEmpty()) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, typeparams.get(typeparams.size() - 1));
        } else if (tree.getReturnType() != null) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, tree.getReturnType());
        } else if (tree.getModifiers() != null) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, tree.getModifiers());
        }
        
        TokenSequence<JavaTokenId> ts = info.getTreeUtilities().tokensFor(tree);
        
        ts.move((int) lastKnownOffsetInHeader);
        
        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.LBRACE || ts.token().id() == JavaTokenId.SEMICOLON) {
                return offset < ts.offset();
            }
        }
        
        return false;
    }
    
    /**
     * creates start and end positions of the tree
     */
    public static int[] createSignaturePositions(final Tree t, final CompilationInfo javac) {
        int[] span = null;
        if (t.getKind() == Tree.Kind.METHOD) { // method + constructor
            span = javac.getTreeUtilities().findNameSpan((MethodTree) t);
        } else if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
            span = javac.getTreeUtilities().findNameSpan((ClassTree) t);
        } else if (Tree.Kind.VARIABLE == t.getKind()) {
            span = javac.getTreeUtilities().findNameSpan((VariableTree) t);
        }
        return span;
    }

    public static SourceVersion resolveSourceVersion(FileObject file) {
        String sourceLevel = SourceLevelQuery.getSourceLevel(file);
        if (sourceLevel == null) {
            return SourceVersion.latest();
        } else if (sourceLevel.startsWith("1.6")) {
            return SourceVersion.RELEASE_6;
        } else if (sourceLevel.startsWith("1.5")) {
            return SourceVersion.RELEASE_5;
        } else if (sourceLevel.startsWith("1.4")) {
            return SourceVersion.RELEASE_4;
        } else if (sourceLevel.startsWith("1.3")) {
            return SourceVersion.RELEASE_3;
        } else if (sourceLevel.startsWith("1.2")) {
            return SourceVersion.RELEASE_2;
        } else if (sourceLevel.startsWith("1.1")) {
            return SourceVersion.RELEASE_1;
        } else if (sourceLevel.startsWith("1.0")) {
            return SourceVersion.RELEASE_0;
        }
        
        return SourceVersion.latest();
    }
}

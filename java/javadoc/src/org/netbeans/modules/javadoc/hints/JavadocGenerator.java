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

package org.netbeans.modules.javadoc.hints;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Elements.DocCommentKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.openide.filesystems.FileObject;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Pokorsky
 */
public final class JavadocGenerator {
    
    private final SourceVersion srcVersion;
    private String author = System.getProperty("user.name"); // NOI18N
        
    /**
     * Updates settings used by this generator. It should be called outside locks.
     * @param file a file where the generated content will be added
     */
    public void updateSettings(FileObject file) {
        DataObject dobj = null;
        DataFolder folder = null;
        try {
            dobj = DataObject.find(file);
            folder = dobj.getFolder();
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (dobj == null || folder == null) {
            return;
        }
        for (CreateFromTemplateAttributesProvider provider
                : Lookup.getDefault().lookupAll(CreateFromTemplateAttributesProvider.class)) {
            Map<String, ?> attrs = provider.attributesFor(dobj, folder, "XXX"); // NOI18N
            if (attrs == null) {
                continue;
            }
            Object aName = attrs.get("user"); // NOI18N
            if (aName != null) {
                author = aName.toString();
                break;
            }
        }
    }
    
    /** Creates a new instance of JavadocGenerator */
    public JavadocGenerator(SourceVersion version) {
        this.srcVersion = version;
    }
    
    public String generateComment(TypeElement clazz, CompilationInfo javac) {
        StringBuilder builder = new StringBuilder(
//                "/**\n" + // NOI18N
                "\n" // NOI18N
                );
        
        if (clazz.getNestingKind() == NestingKind.TOP_LEVEL) {
            builder.append("@author ").append(author).append("\n"); // NOI18N
        }
        
        if (SourceVersion.RELEASE_5.compareTo(srcVersion) <= 0) {
            for (TypeParameterElement param : clazz.getTypeParameters()) {
                builder.append("@param <").append(param.getSimpleName().toString()).append("> \n"); // NOI18N
            }
        }
        
        if (SourceVersion.RELEASE_5.compareTo(srcVersion) <= 0 &&
                JavadocUtilities.isDeprecated(javac, clazz)) {
            builder.append("@deprecated\n"); // NOI18N
        }
        
//        builder.append("*/\n"); // NOI18N

        return builder.toString();
    }
    
    public String generateComment(ExecutableElement method, CompilationInfo javac) {
        StringBuilder builder = new StringBuilder(
//                "/**\n" + // NOI18N
                "\n" // NOI18N
                );
        
        for (TypeParameterElement param : method.getTypeParameters()) {
            builder.append("@param <").append(param.getSimpleName().toString()).append("> \n"); // NOI18N
        }
        
        for (VariableElement param : method.getParameters()) {
            builder.append("@param ").append(param.getSimpleName().toString()).append(" \n"); // NOI18N
        }
        
        if (method.getReturnType().getKind() != TypeKind.VOID) {
            builder.append("@return \n"); // NOI18N
        }

        MethodTree tree = javac.getTrees().getTree(method);
        List<? extends ExpressionTree> throwTrees = tree != null ? tree.getThrows() : null;
        int i = 0;
        for (TypeMirror exceptionType : method.getThrownTypes()) {
            CharSequence name;
            if (TypeKind.DECLARED == exceptionType.getKind() || TypeKind.ERROR == exceptionType.getKind()) {
                TypeElement exception = (TypeElement) ((DeclaredType) exceptionType).asElement();
                name = exception.getQualifiedName();
                if (throwTrees != null) {
                    name = resolveThrowsName(exception, name.toString(), throwTrees.get(i));
                }
            } else if (TypeKind.TYPEVAR == exceptionType.getKind()) {
                // ExceptionType of throws clause may contain TypeVariable see JLS 8.4.6
                TypeParameterElement exception = (TypeParameterElement) ((TypeVariable) exceptionType).asElement();
                name = exception.getSimpleName();
            } else {
                throw new IllegalStateException("Illegal kind: " + exceptionType.getKind()); // NOI18N
            }
            builder.append("@throws ").append(name).append(" \n"); // NOI18N
            i++;
        }
        
        if (SourceVersion.RELEASE_5.compareTo(srcVersion) <= 0 &&
                JavadocUtilities.isDeprecated(javac, method)) {
            builder.append("@deprecated\n"); // NOI18N
        }

//        builder.append("*/\n"); // NOI18N
        
        return builder.toString();
    }
    
    public String generateComment(VariableElement field, CompilationInfo javac) {
        StringBuilder builder = new StringBuilder(
//                "/**\n" + // NOI18N
                "\n" // NOI18N
                );
        
        if (SourceVersion.RELEASE_5.compareTo(srcVersion) <= 0 &&
                JavadocUtilities.isDeprecated(javac, field)) {
            builder.append("@deprecated\n"); // NOI18N
        }
        

//        builder.append("*/\n"); // NOI18N
        
        return builder.toString();
    }
    
    public String generateComment(Element elm, CompilationInfo javac) {
        switch(elm.getKind()) {
            case CLASS:
            case ENUM:
            case INTERFACE:
            case ANNOTATION_TYPE:
                return generateComment((TypeElement) elm, javac);
            case CONSTRUCTOR:
            case METHOD:
                return generateComment((ExecutableElement) elm, javac);
            case FIELD:
            case ENUM_CONSTANT:
                return generateComment((VariableElement) elm, javac);
            default:
                return null;
        }
    }
    
    public DocCommentTree generateComment(Element elm, CompilationInfo javac, TreeMaker make) {
        List<DocTree> firstSentence = new LinkedList<DocTree>();
        List<DocTree> body = new LinkedList<DocTree>();
        List<DocTree> tags = new LinkedList<DocTree>();
        switch(elm.getKind()) {
            case CLASS:
            case ENUM:
            case INTERFACE:
            case ANNOTATION_TYPE:
                TypeElement clazz = (TypeElement) elm;
                if (clazz.getNestingKind() == NestingKind.TOP_LEVEL) {
                    tags.add(make.Author(Collections.singletonList(make.Text(author))));
                }

                if (SourceVersion.RELEASE_5.compareTo(srcVersion) <= 0) {
                    for (TypeParameterElement param : clazz.getTypeParameters()) {
                        tags.add(make.Param(true, make.DocIdentifier(param.getSimpleName()), Collections.emptyList()));
                    }
                }
                break;
            case CONSTRUCTOR:
            case METHOD:
                ExecutableElement method = (ExecutableElement) elm;
                for (TypeParameterElement param : method.getTypeParameters()) {
                    tags.add(make.Param(true, make.DocIdentifier(param.getSimpleName()), Collections.emptyList()));
                }

                for (VariableElement param : method.getParameters()) {
                    tags.add(make.Param(false, make.DocIdentifier(param.getSimpleName()), Collections.emptyList()));
                }

                if (method.getReturnType().getKind() != TypeKind.VOID) {
                    tags.add(make.DocReturn(Collections.emptyList()));
                }
                for (TypeMirror exceptionType : method.getThrownTypes()) {
                    Element exception;
                    if (TypeKind.DECLARED == exceptionType.getKind() || TypeKind.ERROR == exceptionType.getKind()) {
                        exception = (TypeElement) ((DeclaredType) exceptionType).asElement();
                    } else if (TypeKind.TYPEVAR == exceptionType.getKind()) {
                        // ExceptionType of throws clause may contain TypeVariable see JLS 8.4.6
                        exception = (TypeParameterElement) ((TypeVariable) exceptionType).asElement();
                    } else {
                        throw new IllegalStateException("Illegal kind: " + exceptionType.getKind()); // NOI18N
                    }
                    ExpressionTree ident = make.QualIdent(exception);
                    tags.add(make.Throws(make.Reference(ident, null, null), Collections.emptyList()));
                }
                break;
            case FIELD:
            case ENUM_CONSTANT:
                break;
            default:
                throw new UnsupportedOperationException(elm.getKind() +
                        ", " + elm.getClass() + ": " + elm.toString()); // NOI18N
        }
        if (SourceVersion.RELEASE_5.compareTo(srcVersion) <= 0
                && JavadocUtilities.isDeprecated(javac, elm)) {
            tags.add(make.Deprecated(Collections.emptyList()));
        }

        boolean[] useMarkdown = new boolean[1];
        TreePath tp = javac.getTrees().getPath(elm);

        if (tp != null) {
            new TreePathScanner<Void, Void>() {
                private boolean seenJavadoc;
                @Override
                public Void scan(Tree tree, Void p) {
                    if (seenJavadoc) {
                        return null;
                    }

                    return super.scan(tree, p);
                }

                @Override
                public Void visitVariable(VariableTree node, Void p) {
                    checkJavadoc();
                    return super.visitVariable(node, p);
                }

                @Override
                public Void visitMethod(MethodTree node, Void p) {
                    checkJavadoc();
                    return super.visitMethod(node, p);
                }

                @Override
                public Void visitClass(ClassTree node, Void p) {
                    checkJavadoc();
                    return super.visitClass(node, p);
                }
                private void checkJavadoc() {
                    DocCommentKind kind = javac.getDocTrees().getDocCommentKind(getCurrentPath());
                    if (kind != null) {
                        useMarkdown[0] = kind == DocCommentKind.END_OF_LINE;
                        seenJavadoc |= useMarkdown[0];
                    }
                }
            }.scan(tp.getCompilationUnit(), null);
        }

        if (useMarkdown[0]) {
            return make.MarkdownDocComment(firstSentence, body, tags);
        } else {
            return make.DocComment(firstSentence, body, tags);
        }
    }
    
    /**
     * computes name of throws clause to work around
     * <a href="http://www.netbeans.org/issues/show_bug.cgi?id=160414">issue 160414</a>.
     */
    private static String resolveThrowsName(Element el, String fqn, ExpressionTree throwTree) {
        boolean nestedClass = ElementKind.CLASS == el.getKind()
                && NestingKind.TOP_LEVEL != ((TypeElement) el).getNestingKind();
        String insertName = nestedClass ? fqn : throwTree.toString();
        return insertName;
    }
}

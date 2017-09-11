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

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
                        tags.add(make.Param(true, make.DocIdentifier(param.getSimpleName()), Collections.EMPTY_LIST));
                    }
                }
                break;
            case CONSTRUCTOR:
            case METHOD:
                ExecutableElement method = (ExecutableElement) elm;
                for (TypeParameterElement param : method.getTypeParameters()) {
                    tags.add(make.Param(true, make.DocIdentifier(param.getSimpleName()), Collections.EMPTY_LIST));
                }

                for (VariableElement param : method.getParameters()) {
                    tags.add(make.Param(false, make.DocIdentifier(param.getSimpleName()), Collections.EMPTY_LIST));
                }

                if (method.getReturnType().getKind() != TypeKind.VOID) {
                    tags.add(make.DocReturn(Collections.EMPTY_LIST));
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
                    tags.add(make.Throws(make.Reference(ident, null, null), Collections.EMPTY_LIST));
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
            tags.add(make.Deprecated(Collections.EMPTY_LIST));
        }
        return make.DocComment(firstSentence, body, tags);
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

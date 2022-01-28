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
package org.netbeans.modules.editor.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lsp.StructureElement;
import org.netbeans.spi.lsp.StructureProvider;

/**
 * Implementation of StructureProvider from LSP API. It's used for displaying
 * outline view and GoTo File Symbols in VSCode.
 * @author Petr Pisl
 */
@MimeRegistration(mimeType = "text/x-java", service = StructureProvider.class)
public class JavaStructureProvider implements StructureProvider {
    
    private static final Logger LOGGER = Logger.getLogger(JavaStructureProvider.class.getName());
    
    /**
     * One element in the structure. The bulk of properties is counted lazily. 
     */
    private static class JavaStructureElement implements StructureElement {

        private final Element original;
        private final CompilationInfo ci;
        private String name;
        private String detail;
        private int expandedStart;
        private int expandedEnd;
        private int selectionStart;
        private int selectionEnd;
        private List<JavaStructureElement> children;

        public JavaStructureElement(CompilationInfo ci, Element original) {
            this.original = original;
            this.ci = ci;
            this.name = null;
            this.detail = null;
            this.children = null;
            setOffsets();
        }

        @Override
        public String getName() {
            if (name == null) {
                name = createName();
            }
            return name;
        }

        @Override
        public int getSelectionStartOffset() {
            return selectionStart;
        }

        @Override
        public int getSelectionEndOffset() {
            return selectionEnd;
        }

        @Override
        public int getExpandedStartOffset() {
            return expandedStart;
        }

        @Override
        public int getExpandedEndOffset() {
            return expandedEnd;
        }

        @Override
        public StructureElement.Kind getKind() {
            switch (original.getKind()) {
                case PACKAGE:
                    return StructureElement.Kind.Package;
                case ENUM:
                    return StructureElement.Kind.Enum;
                case CLASS:
                    return StructureElement.Kind.Class;
                case ANNOTATION_TYPE:
                    return StructureElement.Kind.Interface;
                case INTERFACE:
                    return StructureElement.Kind.Interface;
                case ENUM_CONSTANT:
                    return StructureElement.Kind.EnumMember;
                case FIELD:
                    return StructureElement.Kind.Field; //TODO: constant
                case PARAMETER:
                    return StructureElement.Kind.Variable;
                case LOCAL_VARIABLE:
                    return StructureElement.Kind.Variable;
                case EXCEPTION_PARAMETER:
                    return StructureElement.Kind.Variable;
                case METHOD:
                    return StructureElement.Kind.Method;
                case CONSTRUCTOR:
                    return StructureElement.Kind.Constructor;
                case TYPE_PARAMETER:
                    return StructureElement.Kind.TypeParameter;
                case RESOURCE_VARIABLE:
                    return StructureElement.Kind.Variable;
                case MODULE:
                    return StructureElement.Kind.Module;
                case STATIC_INIT:
                case INSTANCE_INIT:
                case OTHER:
                default:
                    return StructureElement.Kind.File; //XXX: what here?
            }
        }

        @Override
        public Set<StructureElement.Tag> getTags() {
            if (ci.getElements().isDeprecated(original)) {
                return Collections.singleton(StructureElement.Tag.Deprecated);
            }
            return null;
        }

        @Override
        public String getDetail() {
            ElementKind kind = original.getKind();
            if (detail == null && (kind == ElementKind.FIELD || kind == ElementKind.METHOD)) {
                StringBuilder sb = new StringBuilder();
                if (kind == ElementKind.FIELD) {
                    sb.append(": ");
                    sb.append(Utilities.getTypeName(ci, original.asType(), false));
                    detail = sb.toString();
                } else {
                    // METHOD   
                    TypeMirror rt = ((ExecutableElement) original).getReturnType();
                    if (rt.getKind() == TypeKind.VOID) {
                        sb.append(": void");
                    } else {
                        sb.append(": ");
                        sb.append(Utilities.getTypeName(ci, rt, false));
                    }
                }
                detail = sb.toString();
            }
            return detail;
        }

        @Override
        public List<? extends StructureElement> getChildren() {
            if (children == null) {
                children = new ArrayList<>();
                for (Element child : original.getEnclosedElements()) {
                    JavaStructureElement jse = element2StructureElement(ci, child);
                    if (jse != null) {
                        children.add(jse);
                    }
                }
                TreePath path = ci.getTrees().getPath(original);
                if (path.getLeaf().getKind() == Tree.Kind.METHOD || path.getLeaf().getKind() == Tree.Kind.VARIABLE) {
                    children.addAll(getAnonymousInnerClasses(ci, path));
                }
            }
            return children;
        }

        private static List<JavaStructureElement> getAnonymousInnerClasses(CompilationInfo info, TreePath path) {
            List<JavaStructureElement> inner = new ArrayList<>();
            new TreePathScanner<Void, Void>() {
                @Override
                public Void visitNewClass(NewClassTree node, Void p) {
                    if (node.getClassBody() != null) {
                        Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getClassBody()));
                        if (e != null) {
                            Element te = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getIdentifier()));
                            if (te != null) {
                                JavaStructureElement jse = element2StructureElement(info, te);
                                if (jse != null) {
                                    inner.add(new JavaStructureElement(info, te));
                                }
                            }
                        }
                    }
                    return null;
                }
            }.scan(path, null);
            return inner;
        }

        private void setOffsets() {
            TreePath path = ci.getTrees().getPath(original);
            Tree tree = path.getLeaf();
            long start = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), tree);
            expandedStart = (int) start;
            long end = ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), tree);
            if (end == -1) {
                end = start;
            }
            expandedEnd = (int) end;
            int[] span = null;
            switch (tree.getKind()) {
                case CLASS:
                    span = ci.getTreeUtilities().findNameSpan((ClassTree) tree);
                    break;
                case METHOD:
                    span = ci.getTreeUtilities().findNameSpan((MethodTree) tree);
                    break;
                case VARIABLE:
                    span = ci.getTreeUtilities().findNameSpan((VariableTree) tree);
                    break;
            }
            if (span == null) {
                selectionStart = expandedStart;
                selectionEnd = expandedEnd;
            } else {
                selectionStart = span[0];
                selectionEnd = span[1];
            }
        }

        private String createName() {
            switch (original.getKind()) {
                case PACKAGE:
                    PackageElement pe = (PackageElement) original;
                    return pe.getSimpleName().toString();
                case CLASS:
                case INTERFACE:
                case ENUM:
                case ANNOTATION_TYPE:
                    TypeElement te = (TypeElement) original;
                    StringBuilder sb = new StringBuilder();
                    sb.append(te.getSimpleName());
                    List<? extends TypeParameterElement> typeParams = te.getTypeParameters();
                    if (typeParams != null && !typeParams.isEmpty()) {
                        sb.append("<"); // NOI18N
                        for (Iterator<? extends TypeParameterElement> it = typeParams.iterator(); it.hasNext();) {
                            TypeParameterElement tp = it.next();
                            sb.append(tp.getSimpleName());
                            List<? extends TypeMirror> bounds = tp.getBounds();
                            if (!bounds.isEmpty()) {
                                if (bounds.size() > 1 || !"java.lang.Object".equals(bounds.get(0).toString())) { // NOI18N
                                    sb.append(" extends "); // NOI18N
                                    for (Iterator<? extends TypeMirror> bIt = bounds.iterator(); bIt.hasNext();) {
                                        sb.append(Utilities.getTypeName(ci, bIt.next(), false));
                                        if (bIt.hasNext()) {
                                            sb.append(" & "); // NOI18N
                                        }
                                    }
                                }
                            }
                            if (it.hasNext()) {
                                sb.append(", "); // NOI18N
                            }
                        }
                        sb.append(">"); // NOI18N
                    }
                    return sb.toString();
                case FIELD:
                case ENUM_CONSTANT:
                    return original.getSimpleName().toString();
                case CONSTRUCTOR:
                case METHOD:
                    ExecutableElement ee = (ExecutableElement) original;
                    sb = new StringBuilder();
                    if (ee.getKind() == ElementKind.CONSTRUCTOR) {
                        sb.append(ee.getEnclosingElement().getSimpleName());
                    } else {
                        sb.append(ee.getSimpleName());
                    }
                    sb.append("("); // NOI18N
                    for (Iterator<? extends VariableElement> it = ee.getParameters().iterator(); it.hasNext();) {
                        VariableElement param = it.next();
                        if (!it.hasNext() && ee.isVarArgs() && param.asType().getKind() == TypeKind.ARRAY) {
                            sb.append(Utilities.getTypeName(ci, ((ArrayType) param.asType()).getComponentType(), false, false));
                            sb.append("...");
                        } else {
                            sb.append(Utilities.getTypeName(ci, param.asType(), false, false));
                        }
                        sb.append(" "); // NOI18N
                        sb.append(param.getSimpleName());
                        if (it.hasNext()) {
                            sb.append(", "); // NOI18N
                        }
                    }
                    sb.append(")"); // NOI18N
                    return sb.toString();
            }
            return null;
        }

    }

    @Override
    public CompletableFuture<List<? extends StructureElement>> getStructure(Document doc) {
        JavaSource js = JavaSource.forDocument(doc);

        if (js != null) {
            List<JavaStructureElement> result = new ArrayList<>();
            try {
                js.runUserActionTask(cc -> {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    Trees trees = cc.getTrees();
                    CompilationUnitTree cu = cc.getCompilationUnit();
                    if (cu.getPackage() != null) {
                        TreePath tp = trees.getPath(cu, cu.getPackage());
                        Element el = trees.getElement(tp);
                        if (el != null && el.getKind() == ElementKind.PACKAGE) {
                            JavaStructureElement jse = element2StructureElement(cc, el);
                            if (jse != null) {
                                result.add(jse);
                            }
                        }
                    }
                    for (Element tel : cc.getTopLevelElements()) {
                        JavaStructureElement jse = element2StructureElement(cc, tel);
                        if (jse != null) {
                            result.add(jse);
                        }
                    }
                }, true);
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
            return CompletableFuture.completedFuture(result);
        }
        return CompletableFuture.completedFuture(null);
    }

    private static JavaStructureElement element2StructureElement(CompilationInfo info, Element el) {
        TreePath path = info.getTrees().getPath(el);
        if (path == null) {
            return null;
        }
        TreeUtilities tu = info.getTreeUtilities();
        if (tu.isSynthetic(path)) {
            return null;
        }
        return new JavaStructureElement(info, el);
    }
}

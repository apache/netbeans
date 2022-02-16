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
 *
 * @author Petr Pisl
 */
@MimeRegistration(mimeType = "text/x-java", service = StructureProvider.class)
public class JavaStructureProvider implements StructureProvider {

    private static final Logger LOGGER = Logger.getLogger(JavaStructureProvider.class.getName());

    @Override
    public List<StructureElement> getStructure(Document doc) {
        JavaSource js = JavaSource.forDocument(doc);

        if (js != null) {
            List<StructureElement> result = new ArrayList<>();
            try {
                js.runUserActionTask(cc -> {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    Trees trees = cc.getTrees();
                    CompilationUnitTree cu = cc.getCompilationUnit();
                    if (cu.getPackage() != null) {
                        TreePath tp = trees.getPath(cu, cu.getPackage());
                        Element el = trees.getElement(tp);
                        if (el != null && el.getKind() == ElementKind.PACKAGE) {
                            StructureElement jse = element2StructureElement(cc, el);
                            if (jse != null) {
                                result.add(jse);
                            }
                        }
                    }
                    for (Element tel : cc.getTopLevelElements()) {
                        StructureElement jse = element2StructureElement(cc, tel);
                        if (jse != null) {
                            result.add(jse);
                        }
                    }
                }, true);
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
            return result;
        }
        return Collections.EMPTY_LIST;
    }

    private static StructureElement element2StructureElement(CompilationInfo info, Element el) {
        TreePath path = info.getTrees().getPath(el);
        if (path == null) {
            return null;
        }
        TreeUtilities tu = info.getTreeUtilities();
        if (tu.isSynthetic(path)) {
            return null;
        }

        Builder builder = StructureProvider.newBuilder(createName(info, el), convertKind(el.getKind()));
        builder.detail(createDetail(info, el));
        setOffsets(info, el, builder);
        if (info.getElements().isDeprecated(el)) {
            builder.addTag(StructureElement.Tag.Deprecated);
        }
        for (Element child : el.getEnclosedElements()) {
            StructureElement jse = element2StructureElement(info, child);
            if (jse != null) {
                builder.children(jse);
            }
        }
        if (path.getLeaf().getKind() == Tree.Kind.METHOD || path.getLeaf().getKind() == Tree.Kind.VARIABLE) {
            getAnonymousInnerClasses(info, path, builder);
        }
        return builder.build();
    }

    private static String createName(CompilationInfo ci, Element original) {
        switch (original.getKind()) {
            case PACKAGE:
                PackageElement pe = (PackageElement) original;
                return pe.getSimpleName().toString();
            case CLASS:
            case INTERFACE:
            case ENUM:
            case ANNOTATION_TYPE:
            case RECORD:
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
            case RECORD_COMPONENT:
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

    private static StructureElement.Kind convertKind(ElementKind kind) {
        switch (kind) {
            case PACKAGE:
                return StructureElement.Kind.Package;
            case ENUM:
                return StructureElement.Kind.Enum;
            case CLASS:
            case RECORD:
                return StructureElement.Kind.Class;
            case ANNOTATION_TYPE:
                return StructureElement.Kind.Interface;
            case INTERFACE:
                return StructureElement.Kind.Interface;
            case ENUM_CONSTANT:
            case RECORD_COMPONENT:
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
    
    private static String createDetail(CompilationInfo ci, Element original) {
        ElementKind kind = original.getKind();
        String detail = null;
        if (kind == ElementKind.FIELD || kind == ElementKind.METHOD) {
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
    
    private static void setOffsets(CompilationInfo ci, Element original, Builder builder) {
        TreePath path = ci.getTrees().getPath(original);
        Tree tree = path.getLeaf();
        long start = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), tree);
        int expandedStart = (int) start;
        long end = ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), tree);
        if (end == -1) {
            end = start;
        }
        int expandedEnd = (int) end;
        builder.expandedStartOffset(expandedStart).expandedEndOffset(expandedEnd);
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
            builder.selectionStartOffset(expandedStart).selectionEndOffset(expandedEnd);
        } else {
            builder.selectionStartOffset(span[0]).selectionEndOffset(span[1]);
        }
    }
    
    private static void getAnonymousInnerClasses(CompilationInfo info, TreePath path, Builder builder) {
        new TreePathScanner<Void, Void>() {
            @Override
            public Void visitNewClass(NewClassTree node, Void p) {
                if (node.getClassBody() != null) {
                    Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getClassBody()));
                    if (e != null) {
                        Element te = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getIdentifier()));
                        if (te != null) {
                            StructureElement jse = element2StructureElement(info, te);
                            if (jse != null) {
                                builder.children(jse);
                            }
                        }
                    }
                }
                return null;
            }
        }.scan(path, null);
    }
}

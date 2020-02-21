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

package org.netbeans.modules.cnd.mixeddev.java;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaMethodInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaTypeInfo;
import org.netbeans.modules.cnd.mixeddev.java.QualifiedNamePart.Kind;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaClassInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaFieldInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaParameterInfo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 *
 */
public final class JavaContextSupport {
    
    private static final int TIMEOUT = 1000;
    
    public static <T> T resolveContext(FileObject fObj, ResolveJavaContextTask<T> task, boolean immediately) {
        if (fObj != null && fObj.isValid() && fObj.isData()) {
            return resolveContext(JavaSource.forFileObject(fObj), task, immediately);
        }
        return null;
    }       
    
    public static <T> T resolveContext(Document doc, ResolveJavaContextTask<T> task, boolean immediately) {
        if (doc != null) {
            return resolveContext(JavaSource.forDocument(doc), task, immediately);
        }
        return null;
    }   
    
    private static <T> T resolveContext(JavaSource js, ResolveJavaContextTask<T> task, boolean immediately) {
        if (js != null) {
            try {
                if (immediately) {
                    js.runUserActionTask(task, true);
                    return task.getResult();
                } else {
                    Future<Void> f = js.runWhenScanFinished(task, true);
                    f.get(TIMEOUT, TimeUnit.SECONDS);
                    if (f.isDone()){
                        return task.getResult();
                    }
                }
            } catch (IOException ioEx) {
                Exceptions.printStackTrace(ioEx);
            } catch (InterruptedException | ExecutionException | TimeoutException ex)  {
                // just ignore it
            }
        }
        return null;
    }
    
    public static int[] getIdentifierSpan(final Document doc, final int offset, final Token<JavaTokenId>[] token) {
        if (getFileObject(doc) == null) {
            //do nothing if FO is not attached to the document - the goto would not work anyway:
            return null;
        }
        final int[][] ret = new int[][] {null}; 
        doc.render(new Runnable() {
            @Override
            public void run() {
                TokenHierarchy th = TokenHierarchy.get(doc);
                TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(th, offset);

                if (ts == null)
                    return;

                ts.move(offset);
                if (!ts.moveNext())
                    return;

                Token<JavaTokenId> t = ts.token();

                if (JavaTokenId.JAVADOC_COMMENT == t.id()) {
                    return;
                } else if (!USABLE_TOKEN_IDS.contains(t.id())) {
                    ts.move(offset - 1);
                    if (!ts.moveNext())
                        return;
                    t = ts.token();
                    if (!USABLE_TOKEN_IDS.contains(t.id()))
                        return;
                }

                if (token != null)
                    token[0] = t;

                ret[0] = new int [] {ts.offset(), ts.offset() + t.length()};
            }
        });
        return ret[0];
    }    
    
    public static boolean isMethod(TreePath path) {
        return path != null &&
            path.getLeaf().getKind() == Tree.Kind.METHOD;
    }
    
    public static boolean isClass(TreePath path) {
        return path != null && 
               path.getLeaf() != null &&
               path.getLeaf().getKind() == Tree.Kind.CLASS;
    }
    
    public static boolean isInterface(TreePath path) {
        return path != null && 
               path.getLeaf() != null &&
               path.getLeaf().getKind() == Tree.Kind.INTERFACE;
    }    
    
    public static boolean isClassOrInterface(TreePath path) {
        return isClass(path) || isInterface(path);
    }
    
    public static boolean isField(TreePath path) {
        return path != null &&
            path.getLeaf() != null &&
            path.getLeaf().getKind() == Tree.Kind.VARIABLE &&
            isClassOrInterface(path.getParentPath());
    }
    
    public static JavaClassInfo createClassInfo(CompilationController controller, TreePath clsTreePath) {
        assert isClassOrInterface(clsTreePath);
        List<QualifiedNamePart> qualifiedName = getQualifiedName(clsTreePath);
        String simpleName = qualifiedName.size() > 0 ? qualifiedName.get(qualifiedName.size() - 1).getText().toString() : "<not_initialized>"; // NOI18N
        return new JavaClassInfo(simpleName, qualifiedName);
    }
    
    public static JavaMethodInfo createMethodInfo(CompilationController controller, TreePath mtdTreePath) {
        assert mtdTreePath.getLeaf().getKind() == Tree.Kind.METHOD;

        List<JavaParameterInfo> parameters = new ArrayList();
        MethodTree mtdTree = (MethodTree) mtdTreePath.getLeaf();
        for (VariableTree param : mtdTree.getParameters()) {
            parameters.add(createParameterInfo(controller, param));
        }

        List<QualifiedNamePart> qualifiedName = getQualifiedName(mtdTreePath);
        String simpleName = qualifiedName.size() > 0 ? qualifiedName.get(qualifiedName.size() - 1).getText().toString() : "<not_initialized>"; // NOI18N

        return new JavaMethodInfo(
            simpleName, 
            qualifiedName, 
            parameters, 
            createTypeInfo(controller, mtdTree.getReturnType()), 
            isOverloaded(mtdTreePath, simpleName),
            isFinal(mtdTreePath),
            isStatic(mtdTreePath),
            isNative(mtdTreePath)
        );
    }
    
    public static JavaFieldInfo createFieldInfo(CompilationController controller, TreePath fieldTreePath) {
        assert fieldTreePath.getLeaf().getKind() == Tree.Kind.VARIABLE;
        VariableTree varTree = (VariableTree) fieldTreePath.getLeaf();
        List<QualifiedNamePart> qualifiedName = getQualifiedName(fieldTreePath);
        String simpleName = qualifiedName.size() > 0 ? qualifiedName.get(qualifiedName.size() - 1).getText().toString() : "<not_initialized>"; // NOI18N
        return new JavaFieldInfo(
            simpleName, 
            qualifiedName, 
            createTypeInfo(controller, varTree.getType()),
            isFinal(fieldTreePath),
            isStatic(fieldTreePath)
        );
    }
    
    public static JavaParameterInfo createParameterInfo(CompilationController controller, VariableTree paramTree) {
        return new JavaParameterInfo(
            paramTree.getName(), 
            createTypeInfo(controller, paramTree.getType()), 
            isFinal(paramTree.getModifiers())
        );
    }

    public static JavaTypeInfo createTypeInfo(CompilationController controller, Tree type) {
        if (type != null) { 
            // TODO: handle TypeParameterElement nodes
            TreePath typePath = controller.getTrees().getPath(controller.getCompilationUnit(), type);
            switch (type.getKind()) {
                case CLASS: {
                    Element elem = controller.getTrees().getElement(typePath);
                    if (elem instanceof TypeElement) {
                        TypeElement typeElem = (TypeElement) elem;
                        return new JavaTypeInfo(typeElem.getSimpleName(), getQualifiedName(typeElem), 0);
                    }
                    break;
                }

                case IDENTIFIER: {
                    Element elem = controller.getTrees().getElement(typePath);
                    if (elem instanceof TypeElement) {
                        TypeElement typeElem = (TypeElement) elem;
                        return new JavaTypeInfo(typeElem.getSimpleName(), getQualifiedName(typeElem), 0);
                    }
                    break;
                }

                case MEMBER_SELECT: {
                    Element elem = controller.getTrees().getElement(typePath);
                    if (elem instanceof TypeElement) {
                        TypeElement typeElem = (TypeElement) elem;
                        return new JavaTypeInfo(typeElem.getSimpleName(), getQualifiedName(typeElem), 0);
                    }
                    break;
                }

                case ARRAY_TYPE: {
                    ArrayTypeTree arrayType = (ArrayTypeTree) type;
                    JavaTypeInfo inner = createTypeInfo(controller, arrayType.getType());
                    if (inner != null) {
                        return new JavaTypeInfo(inner.getName(), inner.getQualifiedName(), inner.getArrayDepth() + 1);
                    }
                    break;
                }
                
                case PRIMITIVE_TYPE: {
                    CharSequence primitiveName = convertKind(((PrimitiveTypeTree) type).getPrimitiveTypeKind());
                    return new JavaTypeInfo(primitiveName, Arrays.asList(new QualifiedNamePart(primitiveName, Kind.PRIMITIVE)), 0);
                }

                default:
                    return new JavaTypeInfo("<NOT_SUPPORTED_KIND_" + type.getKind() + ">", Collections.<QualifiedNamePart>emptyList(), 0); // NOI18N
            }        
        }
        return null;
    }
    
    public static String renderQualifiedName(List<QualifiedNamePart> qualName) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (QualifiedNamePart part : qualName) {
            if (!first) {
                switch (part.getKind()) {
                    case PACKAGE:
                        sb.append("/"); // NOI18N
                        break;
                    case CLASS:
                    case INTERFACE:
                        sb.append("/"); // NOI18N
                        break;
                    case VARIABLE:
                    case METHOD:
                        sb.append("/"); // NOI18N
                        break;
                    case NESTED_CLASS:
                    case NESTED_INTERFACE:
                        sb.append("$"); // NOI18N
                        break;
                }
            }
            sb.append(part.getText());
            first = false;
        }
        return sb.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //  Package section
    ////////////////////////////////////////////////////////////////////////////        
    
    /*package*/ static List<QualifiedNamePart> getQualifiedName(CompilationController controller, Tree tree) {
        TreePath treePath = controller.getTrees().getPath(controller.getCompilationUnit(), tree);
        return getQualifiedName(treePath);
    }    
        
    /*package*/ static List<QualifiedNamePart> getQualifiedName(TreePath treePath) {
        List<QualifiedNamePart> qualifiedName = new ArrayList();
        TreePath currentPath = treePath;
        do {
            switch (currentPath.getLeaf().getKind()) {
                case METHOD:
                    qualifiedName.add(0, new QualifiedNamePart(((MethodTree) currentPath.getLeaf()).getName(), Kind.METHOD));
                    break;
                    
                case VARIABLE:
                    qualifiedName.add(0, new QualifiedNamePart(((VariableTree) currentPath.getLeaf()).getName(), Kind.VARIABLE));
                    break;
                    
                case INTERFACE:
                    if (currentPath.getParentPath() != null && currentPath.getParentPath().getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT) {
                        qualifiedName.add(0, new QualifiedNamePart(((ClassTree) currentPath.getLeaf()).getSimpleName(), Kind.NESTED_INTERFACE));
                    } else {
                        qualifiedName.add(0, new QualifiedNamePart(((ClassTree) currentPath.getLeaf()).getSimpleName(), Kind.INTERFACE));
                    }
                    break;

                case CLASS: {
                    if (currentPath.getParentPath() != null && currentPath.getParentPath().getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT) {
                        qualifiedName.add(0, new QualifiedNamePart(((ClassTree) currentPath.getLeaf()).getSimpleName(), Kind.NESTED_CLASS));
                    } else {
                        qualifiedName.add(0, new QualifiedNamePart(((ClassTree) currentPath.getLeaf()).getSimpleName(), Kind.CLASS));
                    }
                    break;
                }
                    
                case COMPILATION_UNIT: {
                    List<CharSequence> dotExpression = renderExpression(((CompilationUnitTree) currentPath.getLeaf()).getPackageName());
                    qualifiedName.add(0, new QualifiedNamePart(MixedDevUtils.stringize(dotExpression, "/"), Kind.PACKAGE)); // NOI18N
                    break;
                }
            }
        } while ((currentPath = currentPath.getParentPath()) != null);
        return qualifiedName;
    }
    
    /*package*/ static List<QualifiedNamePart> getQualifiedName(TypeElement typeElement) {
        List<QualifiedNamePart> qualifiedName = new ArrayList();
        Element current = typeElement;
        while (current != null) {
            switch (current.getKind()) {
                case CLASS:
                    if (((TypeElement) current).getNestingKind().isNested()) {
                        qualifiedName.add(0, new QualifiedNamePart(current.getSimpleName(), Kind.NESTED_CLASS));
                    } else {
                        qualifiedName.add(0, new QualifiedNamePart(current.getSimpleName(), Kind.CLASS));
                    }
                    break;
                case INTERFACE:
                    if (((TypeElement) current).getNestingKind().isNested()) {
                        qualifiedName.add(0, new QualifiedNamePart(current.getSimpleName(), Kind.NESTED_INTERFACE));
                    } else {
                        qualifiedName.add(0, new QualifiedNamePart(current.getSimpleName(), Kind.INTERFACE));
                    }
                    break;
                case PACKAGE:
                    PackageElement pkgElem = (PackageElement) current;
                    String packageName = pkgElem.getQualifiedName().toString();
                    qualifiedName.add(0, new QualifiedNamePart(packageName.replaceAll("\\.", "/"), Kind.PACKAGE)); // NOI18N
                    break;
            }
            current = current.getEnclosingElement();
        }
        return qualifiedName;
    }

    ////////////////////////////////////////////////////////////////////////////
    //  Private section
    ////////////////////////////////////////////////////////////////////////////
    
    private static final Set<JavaTokenId> USABLE_TOKEN_IDS = EnumSet.of(JavaTokenId.IDENTIFIER);
    
    private static FileObject getFileObject(Document doc) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);        
        return od != null ? od.getPrimaryFile() : null;
    }     
    
    private static CharSequence convertKind(TypeKind kind) {
        switch (kind) {
            case BYTE:
                return "byte"; // NOI18N

            case BOOLEAN:
                return "boolean"; // NOI18N

            case CHAR:
                return "char"; // NOI18N

            case FLOAT:       
                return "float"; // NOI18N

            case DOUBLE:
                return "double"; // NOI18N
                
            case SHORT:
                return "short"; // NOI18N

            case INT:
                return "int"; // NOI18N

            case LONG:
                return "long"; // NOI18N
                
            case VOID:
                return "void"; // NOI18N
        }
        throw new UnsupportedOperationException("Unexpected type kind: " + kind); // NOI18N
    }
            
    private static List<CharSequence> renderExpression(ExpressionTree expr) {
        if (expr != null) {
            List<CharSequence> exprParts = new ArrayList<CharSequence>();

            do {
                switch (expr.getKind()) {
                    case MEMBER_SELECT:
                        exprParts.add(0, ((MemberSelectTree) expr).getIdentifier());
                        expr = ((MemberSelectTree) expr).getExpression();
                        break;

                    case IDENTIFIER:
                        exprParts.add(0, ((IdentifierTree) expr).getName());
                        expr = null;
                        break;

                    default:
                        expr = null;
                }
            } while (expr != null);

            return exprParts;
        }
        return Collections.emptyList();
    }    
    
    private static boolean isOverloaded(TreePath mtdTreePath, String mtdName) {
        if (Tree.Kind.METHOD.equals(mtdTreePath.getLeaf().getKind())) {
            boolean searchNative = ((MethodTree) mtdTreePath.getLeaf()).getModifiers().getFlags().contains(Modifier.NATIVE);
            if (Tree.Kind.CLASS.equals(mtdTreePath.getParentPath().getLeaf().getKind())) {
                int counter = 0;
                ClassTree cls = (ClassTree) mtdTreePath.getParentPath().getLeaf();
                for (Tree member : cls.getMembers()) {
                    if (Tree.Kind.METHOD.equals(member.getKind())) {
                        MethodTree method = (MethodTree) member;
                        if (mtdName.equals(method.getName().toString())) {
                            if (searchNative == method.getModifiers().getFlags().contains(Modifier.NATIVE)) {
                                ++counter;
                            }
                        }
                    }
                }

                if (counter > 1) {
                    return true;
                }
            }           
        }
        return false;
    }
    
    private static boolean isFinal(TreePath tp) {
        ModifiersTree mt = null;
        if (Tree.Kind.METHOD.equals(tp.getLeaf().getKind())) {
            MethodTree method = (MethodTree) tp.getLeaf();
            mt = method.getModifiers();
        } else if (Tree.Kind.VARIABLE.equals(tp.getLeaf().getKind())) {
            VariableTree var = (VariableTree) tp.getLeaf();
            mt = var.getModifiers();
        }
        return isFinal(mt);
    }
    
    private static boolean isFinal(ModifiersTree mt) {
        return mt != null ? mt.getFlags().contains(Modifier.FINAL) : false;
    }
    
    private static boolean isStatic(TreePath tp) {
        ModifiersTree mt = null;
        if (Tree.Kind.METHOD.equals(tp.getLeaf().getKind())) {
            MethodTree method = (MethodTree) tp.getLeaf();
            mt = method.getModifiers();
        } else if (Tree.Kind.VARIABLE.equals(tp.getLeaf().getKind())) {
            VariableTree var = (VariableTree) tp.getLeaf();
            mt = var.getModifiers();
        }
        return isStatic(mt);
    }
    
    private static boolean isStatic(ModifiersTree mt) {
        return mt != null ? mt.getFlags().contains(Modifier.STATIC) : false;
    }
    
    private static boolean isNative(TreePath tp) {
        if (Tree.Kind.METHOD.equals(tp.getLeaf().getKind())) {
            MethodTree method = (MethodTree) tp.getLeaf();
            return method.getModifiers().getFlags().contains(Modifier.NATIVE);
        }
        return false;
    }
    
    private JavaContextSupport() {
        throw new AssertionError("Not instantiable!"); // NOI18N
    }
}

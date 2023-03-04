/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.source.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
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
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeUtilities;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.api.lsp.StructureElement;
import org.netbeans.spi.lsp.StructureProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sdedic
 */
public class LspElementUtils {
    
    public static StructureElement element2StructureElement(CompilationInfo info, Element el, ElementAcceptor childAcceptor, 
            boolean allowResources, boolean bypassOpen, FileObject parentFile) {
        TreePath path = info.getTrees().getPath(el);
        if (!allowResources) {
            if (path == null) {
                return null;
            }
            TreeUtilities tu = info.getTreeUtilities();
            if (tu.isSynthetic(path)) {
                return null;
            }
        }

        StructureProvider.Builder builder = StructureProvider.newBuilder(createName(info, el), ElementHeaders.javaKind2Structure(el));
        builder.detail(createDetail(info, el));
        FileObject f = null;
        FileObject owner = null;
        if (!bypassOpen) {
            Object[] oi = setOffsets(info, el, builder);
            if (oi != null) {
                owner = f = (FileObject)oi[0]; 
            }
        } else {
            f = null;
            owner = parentFile;
        }
        if (owner == null && !bypassOpen && allowResources) {
            owner = findOwnerResource(info, el);
        }
        if (f == null && owner != null) {
            builder.file(owner);
        }
        if (info.getElements().isDeprecated(el)) {
            builder.addTag(StructureElement.Tag.Deprecated);
        }

        if (childAcceptor != null) {
            for (Element child : el.getEnclosedElements()) {
                TreePath p = info.getTrees().getPath(child);
                if (!allowResources) {
                    if (p == null) {
                        continue;
                    }
                }
                TypeMirror m = child.asType();
                if (childAcceptor.accept(child, m)) {
                    StructureElement jse = element2StructureElement(info, child, childAcceptor, allowResources, f == null, owner);
                    if (jse != null) {
                        builder.children(jse);
                    }
                }
            }
            if (path != null) {
                if (path.getLeaf().getKind() == Tree.Kind.METHOD || path.getLeaf().getKind() == Tree.Kind.VARIABLE) {
                    getAnonymousInnerClasses(info, path, builder, childAcceptor);
                }
            }
            // ensure children is always filled, if the caller requested traversal, force creation of the list.
            builder.children();
        }
        return builder.build();
    }
    
    public static StructureElement element2StructureElement(CompilationInfo info, Element el, ElementAcceptor childAcceptor) {
        return element2StructureElement(info, el, childAcceptor, false, false, null);
    }
    
    static FileObject findOwnerResource(CompilationInfo info, Element el) {
        ElementKind ek = el.getKind();
        if (ek == ElementKind.MODULE) {
            // not supported at the moment
            return null;
        }
        Element parent = el;
        if (!(ek.isClass() || ek.isInterface())) {
            parent = el.getEnclosingElement();
            if (!(parent.getKind().isClass() || parent.getKind().isInterface())) {
                return null;
            }
        }
        ElementHandle h = ElementHandle.create(parent);
        String s = h.getBinaryName();
        int lastSlash = s.lastIndexOf('.');
        int dollar = s.substring(lastSlash + 1).indexOf('$');
        
        String resourceName = s.substring(0, dollar >= 0 ? lastSlash + 1 + dollar : s.length()).replace(".", "/"); // NOI18N
        ClasspathInfo cpInfo = info.getClasspathInfo();
        final ClassPath[] cps = 
            new ClassPath[] {
                cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE),
                cpInfo.getClassPath(ClasspathInfo.PathKind.OUTPUT),
                cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT),                    
                cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE)
            };
        for (ClassPath cp : cps) {
            FileObject f = cp.findResource(resourceName);
            if (f != null) {
                return f;
            }
        }
        return null;
    }
    
    public  static StructureElement describeElement(CompilationInfo info, Element el, ElementAcceptor childAcceptor, boolean allowBinary) {
        return element2StructureElement(info, el, childAcceptor, allowBinary, false, null);
    }
    
    public static CompletableFuture<StructureElement> createStructureElement(CompilationInfo info, Element el, boolean resolveSources) {
        TreePath path = info.getTrees().getPath(el);
        AtomicBoolean cancel = new AtomicBoolean();
        CompletableFuture<StructureProvider.Builder> f1 = createStructureElement0(path, info, el, cancel, resolveSources);
        CompletableFuture<StructureElement> ret = f1.thenApply(b -> b == null ? null : b.build());
        ret.exceptionally(t -> {
            if (t instanceof CompletionException) {
                t = t.getCause();
            }
            if (t instanceof CancellationException) {
                // set the cancel flag on
                cancel.set(true);
                // attempt to cancel the 'main' future to interrupt the potential opening process. Will have no effect,
                // if the main future was cancelled for some reason.
                f1.cancel(true);
            }
            return null;
        });
        return ret;
    }
    
    private static CompletableFuture<StructureProvider.Builder> createStructureElement0(TreePath path, CompilationInfo info, Element el, AtomicBoolean cancel, boolean acquire) {
        StructureProvider.Builder builder = StructureProvider.newBuilder(createName(info, el), ElementHeaders.javaKind2Structure(el));
        builder.detail(createDetail(info, el));
        if (info.getElements().isDeprecated(el)) {
            builder.addTag(StructureElement.Tag.Deprecated);
        }
        return setFutureOffsets(info, el, builder, cancel, acquire);
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
                                    sb.append(getTypeName(ci, bIt.next(), false));
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
                        sb.append(getTypeName(ci, ((ArrayType) param.asType()).getComponentType(), false, false));
                        sb.append("...");
                    } else {
                        sb.append(getTypeName(ci, param.asType(), false, false));
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

    private static String createDetail(CompilationInfo ci, Element original) {
        ElementKind kind = original.getKind();
        String detail = null;
        if (kind == ElementKind.FIELD || kind == ElementKind.METHOD) {
            StringBuilder sb = new StringBuilder();
            if (kind == ElementKind.FIELD) {
                sb.append(": ");
                sb.append(getTypeName(ci, original.asType(), false));
                detail = sb.toString();
            } else {
                // METHOD   
                TypeMirror rt = ((ExecutableElement) original).getReturnType();
                if (rt.getKind() == TypeKind.VOID) {
                    sb.append(": void");
                } else {
                    sb.append(": ");
                    sb.append(getTypeName(ci, rt, false));
                }
            }
            detail = sb.toString();
        }
        return detail;
    }
    
    private static StructureProvider.Builder processOffsetInfo(Object[] info, StructureProvider.Builder builder) {
        if (info == null) {
            return builder;
        }
        int selStart = (int)info[3];
        if (selStart < 0) {
            selStart = (int)info[1];
        }
        int selEnd = (int)info[4];
        if (selEnd < 0) {
            selEnd = (int)info[2];
        }
        TreePathHandle pathHandle = (TreePathHandle)info[6];
        FileObject f = (FileObject)info[0];
        boolean[] synthetic = new boolean[] { false };
        if (f != null) {
            builder.file(f);
            if (pathHandle != null) {
                try {
                    JavaSource js = JavaSource.forFileObject(f);
                    if (js == null) {
                        return null;
                    }
                    js.runUserActionTask((cc) -> {
                        TreePath path = pathHandle.resolve(cc);
                        synthetic[0] = cc.getTreeUtilities().isSynthetic(path);
                    }, true);
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
        if (synthetic[0]) {
            return null;
        }
        builder.expandedStartOffset((int)info[1]).expandedEndOffset((int)info[2]);
        builder.selectionStartOffset(selStart).selectionEndOffset(selEnd);
        return builder;
    }
    
    private static CompletableFuture<StructureProvider.Builder> setFutureOffsets(CompilationInfo ci, Element original, 
            StructureProvider.Builder builder, AtomicBoolean cancel, boolean acquire) {
        ElementHandle<Element> h = ElementHandle.create(original);
        String name;
        if (original.getKind().isClass() || original.getKind().isInterface()) {
            name = h.getBinaryName().replace(".", "/") + ".class";
        } else {
            TypeElement e = ci.getElementUtilities().enclosingTypeElement(original);
            if (e != null) {
                name = e.getQualifiedName().toString();
            } else {
                name = h.getBinaryName();
            }
        }
        
        return ElementOpenAccessor.getInstance().getOpenInfoFuture(ci.getClasspathInfo(), h, name, cancel, acquire).thenApply(
            info -> processOffsetInfo(info, builder));
    }
    
    private static Object[] setOffsets(CompilationInfo ci, Element original, StructureProvider.Builder builder) {
        ElementHandle<Element> h = ElementHandle.create(original);
        Object[] openInfo = ElementOpenAccessor.getInstance().getOpenInfo(ci.getClasspathInfo(), h, new AtomicBoolean());
        processOffsetInfo(openInfo, builder);
        return openInfo;
    }
    
    private static void getAnonymousInnerClasses(CompilationInfo info, TreePath path, StructureProvider.Builder builder, ElementAcceptor childAcceptor) {
        new TreePathScanner<Void, Void>() {
            @Override
            public Void visitNewClass(NewClassTree node, Void p) {
                if (node.getClassBody() != null) {
                    Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getClassBody()));
                    if (e != null) {
                        TreePath path = new TreePath(getCurrentPath(), node.getIdentifier());
                        TypeMirror m = info.getTrees().getTypeMirror(path);
                        Element te = info.getTrees().getElement(path);
                        if (te != null & childAcceptor.accept(te, m)) {
                            StructureElement jse = element2StructureElement(info, te, childAcceptor);
                            if (jse != null) {
                                builder.children(jse);
                            }
                        }
                    }
                }
                return null;
            }

            @Override
            public Void visitClass(ClassTree node, Void p) {
                Element e = info.getTrees().getElement(getCurrentPath());
                TypeMirror m = info.getTrees().getTypeMirror(getCurrentPath());
                if (e != null & childAcceptor.accept(e, m)) {
                    StructureElement jse = element2StructureElement(info, e, childAcceptor);
                    if (jse != null) {
                        builder.children(jse);
                    }
                }
                return super.visitClass(node, p);
            }
            
        }.scan(path, null);
    }

    public static CharSequence getTypeName(CompilationInfo info, TypeMirror type, boolean fqn) {
        return getTypeName(info, type, fqn, false);
    }

    public static CharSequence getTypeName(CompilationInfo info, TypeMirror type, boolean fqn, boolean varArg) {
        Set<TypeUtilities.TypeNameOptions> options = EnumSet.noneOf(TypeUtilities.TypeNameOptions.class);
        if (fqn) {
            options.add(TypeUtilities.TypeNameOptions.PRINT_FQN);
        }
        if (varArg) {
            options.add(TypeUtilities.TypeNameOptions.PRINT_AS_VARARG);
        }
        return info.getTypeUtilities().getTypeName(type, options.toArray(new TypeUtilities.TypeNameOptions[0]));
    }
}

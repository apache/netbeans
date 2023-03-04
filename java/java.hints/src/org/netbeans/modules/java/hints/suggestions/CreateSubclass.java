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
package org.netbeans.modules.java.hints.suggestions;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.JTextComponent;
import javax.tools.Diagnostic;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.editor.codegen.ConstructorGenerator;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.hints.suggestions.NameAndPackagePanel.ErrorListener;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Kind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dusan Balek
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.suggestions.CreateSubclass", description = "#DESC_org.netbeans.modules.java.hints.suggestions.CreateSubclass", category = "suggestions", hintKind = Kind.ACTION, severity = Severity.HINT)
public class CreateSubclass {

    @TriggerTreeKind({Tree.Kind.CLASS, Tree.Kind.INTERFACE})
    public static ErrorDescription check(HintContext context) {
        TreePath tp = context.getPath();
        ClassTree cls = (ClassTree) tp.getLeaf();
        CompilationInfo info = context.getInfo();
        SourcePositions sourcePositions = info.getTrees().getSourcePositions();
        long startPos = sourcePositions.getStartPosition(tp.getCompilationUnit(), cls);
        if (startPos > Integer.MAX_VALUE) {
            return null;
        }
        int[] bodySpan = info.getTreeUtilities().findBodySpan(cls);
        if (bodySpan == null || bodySpan[0] <= startPos) {
            return null;
        }
        int caret = context.getCaretLocation();
        if (startPos < 0 || caret < 0 || caret < startPos || caret >= bodySpan[0]) {
            return null;
        }

        // #222487
        // If there is a compile-time error on the class, then don't offer to
        // create a subclass.
        List<Diagnostic> errors = info.getDiagnostics();
        if (!errors.isEmpty()) {
            for (Diagnostic d : errors) {
                if (d.getKind() != Diagnostic.Kind.ERROR) {
                    continue;
                }
                // Check that the error's start position is within the class header
                // Note: d.getEndPosition() is not used because, for example,
                // a "compiler.err.does.not.override.abstract" error ends at
                // the end of the class tree.
                if (startPos <= d.getStartPosition() && d.getStartPosition() <= bodySpan[0]) {
                    return null;
                }
            }
        }

        TypeElement typeElement = (TypeElement) info.getTrees().getElement(tp);
        
        if (typeElement == null || typeElement.getModifiers().contains(Modifier.FINAL)) return null;

        Element outer = typeElement.getEnclosingElement();
        // do not offer the hint for non-static inner classes. Permit for classes nested into itnerface - no enclosing instance
        if (outer != null && outer.getKind() != ElementKind.PACKAGE && outer.getKind() != ElementKind.INTERFACE) {
            if (outer.getKind() != ElementKind.CLASS && outer.getKind() != ElementKind.ENUM) {
                return null;
            }
            if (!typeElement.getModifiers().contains(Modifier.STATIC)) {
                return null;
            }
        }

        
        ClassPath cp = info.getClasspathInfo().getClassPath(PathKind.SOURCE);
        FileObject root = cp.findOwnerRoot(info.getFileObject());
        if (root == null) { //File not part of any project
            return null;
        }

        PackageElement packageElement = (PackageElement) info.getElementUtilities().outermostTypeElement(typeElement).getEnclosingElement();
        CreateSubclassFix fix = new CreateSubclassFix(info, root, packageElement.getQualifiedName().toString(), typeElement.getSimpleName().toString() + "Impl", typeElement); //NOI18N
        return ErrorDescriptionFactory.forTree(context, context.getPath(), NbBundle.getMessage(CreateSubclass.class, typeElement.getKind() == ElementKind.CLASS
                ? typeElement.getModifiers().contains(Modifier.ABSTRACT) ? "ERR_ImplementAbstractClass" : "ERR_CreateSubclass" : "ERR_ImplementInterface"), fix); //NOI18N
    }

    //for tests:
    static String[] overrideNameAndPackage;
    
    private static final class CreateSubclassFix implements Fix, PropertyChangeListener {

        private FileObject targetSourceRoot;
        private String packageName;
        private String simpleName;
        private ElementHandle<TypeElement> superType;
        private boolean isAbstract;
        private boolean hasNonDefaultConstructor = false;
        private FileObject target = null;

        public CreateSubclassFix(CompilationInfo info, FileObject targetSourceRoot, String packageName, String simpleName, TypeElement typeElement) {
            this.targetSourceRoot = targetSourceRoot;
            this.packageName = packageName;
            this.simpleName = simpleName;
            this.isAbstract = typeElement.getModifiers().contains(Modifier.ABSTRACT);
            this.superType = ElementHandle.create(typeElement);
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(CreateSubclass.class, superType.getKind() == ElementKind.CLASS ? isAbstract ? "FIX_ImplementAbstractClass" : "FIX_CreateSubclass" : "FIX_ImplementInterface"); //NOI18N
        }

        @Override
        public ChangeInfo implement() throws Exception {
            return IndexingManager.getDefault().runProtected(new Callable<ChangeInfo>() {
                @Override public ChangeInfo call() throws Exception {
                if (overrideNameAndPackage == null) {
                    final NameAndPackagePanel panel = new NameAndPackagePanel(targetSourceRoot, superType, simpleName, packageName);
                    final DialogDescriptor desc = new DialogDescriptor(panel, getText());
                    final NotificationLineSupport nls = desc.createNotificationLineSupport();
                    panel.setErrorListener(new ErrorListener() {
                        @Override public void setErrorMessage(String errorMessage) {
                            nls.setErrorMessage(errorMessage);
                            desc.setValid(errorMessage == null);
                        }
                    });
                    panel.checkValid();
                    if (DialogDisplayer.getDefault().notify(desc) != DialogDescriptor.OK_OPTION) {
                        return null;
                    }
                    simpleName = panel.getClassName();
                    packageName = panel.getPackageName();
                } else {
                    simpleName = overrideNameAndPackage[0];
                    packageName = overrideNameAndPackage[1];
                }

                EditorRegistry.addPropertyChangeListener(CreateSubclassFix.this);

                final String path = packageName.replace('.', '/') + '/' + simpleName + ".java"; //NOI18N
                target = targetSourceRoot.getFileObject(path);
                final JavaSource js = target != null ? JavaSource.forFileObject(target) : JavaSource.create(ClasspathInfo.create(targetSourceRoot));
                final ModificationResult result = js.runModificationTask(new Task<WorkingCopy>() {

                    @Override
                    public void run(WorkingCopy parameter) throws Exception {
                        parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TypeElement superTypeElement = superType.resolve(parameter);
                        if (superTypeElement != null) {
                            TreeMaker make = parameter.getTreeMaker();
                            List<TypeParameterTree> typeParameters = new ArrayList<>();
                            TypeElement jlObjectElement = parameter.getElements().getTypeElement("java.lang.Object");
                            TypeMirror  jlObjectType    = jlObjectElement != null ? jlObjectElement.asType() : null;
                            for (TypeParameterElement origTP : superTypeElement.getTypeParameters()) {
                                List<ExpressionTree> bounds = new ArrayList<>();
                                for (TypeMirror b : origTP.getBounds()) {
                                    if (jlObjectType != null && parameter.getTypes().isSameType(b, jlObjectType)) continue;
                                    bounds.add((ExpressionTree) make.Type(b));
                                }
                                typeParameters.add(make.TypeParameter(origTP.getSimpleName(), bounds));
                            }
                            CompilationUnitTree cut = parameter.getFileObject() != null
                                    ? parameter.getCompilationUnit()
                                    : GeneratorUtilities.get(parameter).createFromTemplate(targetSourceRoot, path, ElementKind.CLASS);
                            ClassTree source = (ClassTree) cut.getTypeDecls().get(0);
                            if (superTypeElement.getKind() == ElementKind.CLASS) {
                                Element el = parameter.getTrees().getElement(TreePath.getPath(cut, source));
                                if (el instanceof TypeElement) {
                                    TypeMirror sup = ((TypeElement)el).getSuperclass();
                                    if (!parameter.getTypes().isSubtype(superTypeElement.asType(), sup)) {
                                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(CreateSubclass.class, "ERR_IncompatibleSupertype", el.getSimpleName())); //NOI18N
                                        Toolkit.getDefaultToolkit().beep();
                                        return;
                                    }
                                }
                                parameter.rewrite(source, make.Class(source.getModifiers(), simpleName, typeParameters, make.Type(superTypeElement.asType()), source.getImplementsClause(), source.getMembers()));
                                for (ExecutableElement ctor : ElementFilter.constructorsIn(superTypeElement.getEnclosedElements())) {
                                    if (!ctor.getParameters().isEmpty()) {
                                        hasNonDefaultConstructor = true;
                                        break;
                                    }
                                }
                            } else {
                                List<? extends Tree> impls = source.getImplementsClause();
                                List<Tree> newImpls = new ArrayList<Tree>(impls.size() + 1);
                                newImpls.addAll(impls);
                                newImpls.add(make.Type(superTypeElement.asType()));
                                parameter.rewrite(source, make.Class(source.getModifiers(), source.getSimpleName(), typeParameters, source.getExtendsClause(), newImpls, source.getMembers()));
                            }
                            if (parameter.getFileObject() == null) {
                                parameter.rewrite(null, cut);
                            }
                        }
                    }
                });
                result.commit();

                if (!hasNonDefaultConstructor && !isAbstract) {
                    EditorRegistry.removePropertyChangeListener(CreateSubclassFix.this);
                }
                
                if (target == null) {
                    Iterator<File> it = result.getNewFiles().iterator();
                    target = it.hasNext() ? FileUtil.toFileObject(it.next()) : null;
                }
                return target != null ? new ChangeInfo(target, null, null) : null;
                }
            });
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final JTextComponent component = EditorRegistry.focusedComponent();
            FileObject fo = component != null ? NbEditorUtilities.getFileObject(component.getDocument()) : null;
            if (target == null || target != fo) {
                return;
            }
            EditorRegistry.removePropertyChangeListener(this);
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    try {
                        JavaSource js = JavaSource.forDocument(component.getDocument());
                        js.runModificationTask(new Task<WorkingCopy>() {

                            @Override
                            public void run(WorkingCopy parameter) throws Exception {
                                parameter.toPhase(JavaSource.Phase.RESOLVED);
                                CompilationUnitTree cut = parameter.getCompilationUnit();
                                if (!cut.getTypeDecls().isEmpty()) {
                                    TreePath path = TreePath.getPath(cut, cut.getTypeDecls().get(0));
                                    if (isAbstract) {
                                        GeneratorUtils.generateAllAbstractMethodImplementations(parameter, path);
                                    }
                                    if (hasNonDefaultConstructor) {
                                        ConstructorGenerator.Factory factory = new ConstructorGenerator.Factory();
                                        Iterator<? extends CodeGenerator> generators = factory.create(Lookups.fixed(component, parameter, path)).iterator();
                                        if (generators.hasNext()) {
                                            generators.next().invoke();
                                        }
                                    }
                                }
                            }
                        }).commit();
                    } catch (IOException ioe) {
                    }
                }
            });
        }
    }
}

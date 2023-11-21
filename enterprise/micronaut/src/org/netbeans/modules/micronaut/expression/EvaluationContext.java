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
package org.netbeans.modules.micronaut.expression;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.classfile.CPEntry;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.Code;
import org.netbeans.modules.classfile.ConstantPool;
import org.netbeans.modules.classfile.Method;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public final class EvaluationContext {

    private static final String REGISTRATIONS_METADATA = "META-INF/services/io.micronaut.inject.visitor.TypeElementVisitor";
    private static final String CONTEXT_REGISTRAR_CLASS = "io.micronaut.expressions.context.ExpressionEvaluationContextRegistrar";
    private static final String CONTEXT_REGISTRAR_METHOD_NAME = "getContextClassName";
    private static final String CONTEXT_REGISTRAR_METHOD_SIGNATURE = "()Ljava/lang/String;";
    private static final String ANNOTATION_CONTEXT_CLASS = "io.micronaut.context.annotation.AnnotationExpressionContext";

    private final CompilationInfo info;
    private final ClassPath compileCP;
    private final ClassPath processorCP;
    private final Scope scope;
    private final Element annotationMemeber;
    private List<TypeElement> contextClasses = null;

    public static EvaluationContext get(CompilationInfo info, TreePath path) {
        Project project = FileOwnerQuery.getOwner(info.getFileObject());
        ClassPath processorCP = getProcessorClasspath(project);
        return processorCP.findResource(CONTEXT_REGISTRAR_CLASS.replace('.', '/') + ".class") != null
                ? new EvaluationContext(info, getCompileClasspath(project), processorCP, path)
                : null;
    }

    private EvaluationContext(CompilationInfo info, ClassPath compileCP, ClassPath processorCP, TreePath path) {
        this.info = info;
        this.compileCP = compileCP;
        this.processorCP = processorCP;
        this.scope = info.getTrees().getScope(path);
        this.annotationMemeber = path.getLeaf().getKind() == Tree.Kind.ASSIGNMENT ? info.getTrees().getElement(new TreePath(path, ((AssignmentTree) path.getLeaf()).getVariable())) : null;
    }

    public Trees getTrees() {
        return info.getTrees();
    }

    public Types getTypes() {
        return info.getTypes();
    }

    public Elements getElements() {
        return info.getElements();
    }

    public Scope getScope() {
        return scope;
    }

    public List<ExecutableElement> getContextMethods() {
        if (contextClasses == null) {
            initializeContextClasses();
        }
        List<ExecutableElement> methods = new ArrayList<>();
        for (TypeElement contextClass : contextClasses) {
            methods.addAll(ElementFilter.methodsIn(contextClass.getEnclosedElements()));
        }
        return methods;
    }

    private void initializeContextClasses() {
        contextClasses = new ArrayList<>();
        ElementHandle<TypeElement> handle = ElementHandle.createTypeElementHandle(ElementKind.INTERFACE, CONTEXT_REGISTRAR_CLASS);
        Set<ElementHandle<TypeElement>> elements = info.getClasspathInfo().getClassIndex().getElements(handle, EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.allOf(ClassIndex.SearchScope.class));
        if (compileCP != null && processorCP != null) {
            for (FileObject fo : processorCP.findAllResources(REGISTRATIONS_METADATA)) {
                try {
                    for (String fqn : fo.asLines()) {
                        TypeElement te = info.getElements().getTypeElement(fqn);
                        if (te != null && elements.contains(ElementHandle.create(te))) {
                            FileObject clsFO = compileCP.findResource(fqn.replace('.', '/') + ".class");
                            if (clsFO != null) {
                                ClassFile classFile = new ClassFile(clsFO.getInputStream());
                                Method method = classFile.getMethod(CONTEXT_REGISTRAR_METHOD_NAME, CONTEXT_REGISTRAR_METHOD_SIGNATURE);
                                if (method != null) {
                                    Code code = method.getCode();
                                    byte[] byteCodes = code.getByteCodes();
                                    if (byteCodes.length == 3 && byteCodes[0] == (byte) 0x12 && byteCodes[2] == (byte) 0xb0) {
                                        ConstantPool constantPool = classFile.getConstantPool();
                                        CPEntry entry = constantPool.get((int) byteCodes[1]);
                                        Object value = entry.getValue();
                                        if (value instanceof String) {
                                            TypeElement cls = info.getElements().getTypeElement((String) value);
                                            if (cls != null) {
                                                contextClasses.add(cls);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        if (annotationMemeber != null) {
            addAnnotationContextClasses(annotationMemeber);
            addAnnotationContextClasses(annotationMemeber.getEnclosingElement());
        }
    }

    private void addAnnotationContextClasses(Element element) {
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            Element metaAnn = am.getAnnotationType().asElement();
            if (metaAnn.getKind() == ElementKind.ANNOTATION_TYPE && ANNOTATION_CONTEXT_CLASS.contentEquals(((TypeElement) metaAnn).getQualifiedName())) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                    if ("className".contentEquals(entry.getKey().getSimpleName())) {
                        Object value = entry.getValue().getValue();
                        if (value instanceof String) {
                            TypeElement te = info.getElements().getTypeElement((CharSequence) value);
                            if (te != null) {
                                contextClasses.add(te);
                            }
                        }
                    } else if ("value".contentEquals(entry.getKey().getSimpleName())) {
                        Object value = entry.getValue().getValue();
                        if (value instanceof DeclaredType) {
                            contextClasses.add((TypeElement) ((DeclaredType) value).asElement());
                        }
                    }
                }
            }
        }
    }

    private static ClassPath getProcessorClasspath(Project project) {
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (srcGroups.length > 0) {
            return ClassPath.getClassPath(srcGroups[0].getRootFolder(), JavaClassPathConstants.PROCESSOR_PATH);
        }
        return null;
    }

    private static ClassPath getCompileClasspath(Project project) {
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (srcGroups.length > 0) {
            return ClassPath.getClassPath(srcGroups[0].getRootFolder(), ClassPath.COMPILE);
        }
        return null;
    }
}

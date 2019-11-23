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
package org.netbeans.modules.openide.nodes;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.tools.Diagnostic.Kind;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.nodes.BeanInfoSearchPath;
import org.openide.nodes.PropertyEditorRegistration;
import org.openide.nodes.PropertyEditorSearchPath;
import org.openide.util.lookup.ServiceProvider;

/**
 * Processing annotation <code>@PropertyEditorSearchPath</code> 
 * and <code>@PropertyEditorRegistration</code>
 * 
 * @author Jan Horvath <jhorvath@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
public class NodesAnnotationProcessor extends LayerGeneratingProcessor {

    public NodesAnnotationProcessor() {
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<String>();
        set.add(PropertyEditorSearchPath.class.getName());
        set.add(PropertyEditorRegistration.class.getName());
        set.add(BeanInfoSearchPath.class.getName());
        return set;
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) 
            throws LayerGenerationException {
        Messager messager = processingEnv.getMessager();
        
        // handle @PropertyEditorSearchPath
        for (Element e : roundEnv.getElementsAnnotatedWith(PropertyEditorSearchPath.class)) {
            String pkg = findPackage(e);
            String pkgFilename = pkg.replace(".", "-"); //NOI18N
            LayerBuilder builder = layer(e);
            LayerBuilder.File file = builder.file(NodesRegistrationSupport.PE_LOOKUP_PATH 
                    + "/Package-" + pkgFilename + ".instance"); //NOI18N
            file.methodvalue("instanceCreate", NodesRegistrationSupport.class.getName(), 
                    "createPackageRegistration"); //NOI18N
            file.stringvalue(NodesRegistrationSupport.PACKAGE, pkg);
            file.stringvalue("instanceOf", NodesRegistrationSupport.PEPackageRegistration.class.getName());
            file.write();
        }

        // handle @PropertyEditorRegistration
        for (Element e : roundEnv.getElementsAnnotatedWith(PropertyEditorRegistration.class)) {
            if (e.getKind() == ElementKind.CLASS) {
                String className = ((TypeElement) e).getQualifiedName().toString();
                Collection<AnnotationValue> targetTypes = null;
                List<? extends AnnotationMirror> annotationMirrors = e.getAnnotationMirrors();
                for (AnnotationMirror am : annotationMirrors) {
                    for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                        if ("targetType".equals(entry.getKey().getSimpleName().toString())) { //NOI18N
                            targetTypes = (Collection<AnnotationValue>) entry.getValue().getValue();
                        }
                    }
                }
                if (targetTypes == null) {
                    messager.printMessage(Kind.ERROR, "No targetType is specified", e); //NOI18N
                    continue;
                }
                TypeElement typeElement = processingEnv.getElementUtils().getTypeElement("java.beans.PropertyEditor"); //NOI18N
                if (!processingEnv.getTypeUtils().isSubtype(e.asType(), typeElement.asType())) {
                    messager.printMessage(Kind.ERROR, className + " is not subtype of PropertyEditor", e); //NOI18N
                    continue;
                }

                LayerBuilder builder = layer(e);
                String clsFileName = className.replace(".", "-"); //NOI18N
                LayerBuilder.File file = builder.instanceFile(NodesRegistrationSupport.PE_LOOKUP_PATH, "Class-" + clsFileName); //NOI18N
                file.methodvalue("instanceCreate", NodesRegistrationSupport.class.getName(), "createClassRegistration"); //NOI18N
                file.stringvalue(NodesRegistrationSupport.EDITOR_CLASS, className); //NOI18N
                file.stringvalue("instanceOf", NodesRegistrationSupport.PEClassRegistration.class.getName());
                int i = 1;
                for (AnnotationValue type : targetTypes) {
                    String clsName = type.accept(new SimpleAnnotationValueVisitor6<String, Object>() {
                        
                        @Override
                        public String visitType(TypeMirror t, Object p) {
                            if (t.getKind() == TypeKind.DECLARED) {
                                return processingEnv.getElementUtils().getBinaryName((TypeElement) processingEnv.getTypeUtils().asElement(t)).toString();
                            }
                            return t.toString();
                        }
                    }, null);
                    file.stringvalue("targetType." + i, clsName); //NOI18N
                    i++;
                }
                file.write();
            }
        }
        
        // handle @BeanInfoSearchPath
        for (Element e : roundEnv.getElementsAnnotatedWith(BeanInfoSearchPath.class)) {
            String pkg = findPackage(e);
            String pkgFilename = pkg.replace(".", "-"); //NOI18N
            LayerBuilder builder = layer(e);
            LayerBuilder.File file = builder.file(NodesRegistrationSupport.BEANINFO_LOOKUP_PATH 
                    + "/BeanInfo-" + pkgFilename + ".instance"); //NOI18N
            file.methodvalue("instanceCreate", NodesRegistrationSupport.class.getName(), 
                    "createBeanInfoRegistration"); //NOI18N
            file.stringvalue(NodesRegistrationSupport.PACKAGE, pkg);
            file.stringvalue("instanceOf", NodesRegistrationSupport.BeanInfoRegistration.class.getName());
            file.write();
        }
        return true;
    }

    private String findPackage(Element e) {
        switch (e.getKind()) {
            case PACKAGE:
                return ((PackageElement) e).getQualifiedName().toString();
            default:
                return findPackage(e.getEnclosingElement());
        }
    }
}

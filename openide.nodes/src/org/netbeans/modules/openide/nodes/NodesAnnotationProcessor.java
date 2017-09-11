/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
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
@SupportedSourceVersion(SourceVersion.RELEASE_7)
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

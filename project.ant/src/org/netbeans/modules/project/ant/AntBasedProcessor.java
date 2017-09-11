/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.ant;

import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 * processor for {@link AntBasedProjectRegistration} annotation.
 * @author Jaroslav Tulach
 */
@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AntBasedProcessor extends LayerGeneratingProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(AntBasedProjectRegistration.class.getCanonicalName());
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        TypeElement aphType = processingEnv.getElementUtils().getTypeElement(AntProjectHelper.class.getName());
        assert aphType != null : "Cannot find AntProjectHelper in " + processingEnv;
        TypeMirror antHelper = aphType.asType();
        TypeMirror project = processingEnv.getElementUtils().getTypeElement(Project.class.getName()).asType();
        for (Element e : roundEnv.getElementsAnnotatedWith(AntBasedProjectRegistration.class)) {
            AntBasedProjectRegistration reg = e.getAnnotation(AntBasedProjectRegistration.class);
            String name;
            String classname;
            String methodname;
            switch (e.getKind()) {
                case CLASS:
                    classname = processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString();
                    name = classname.replace('.', '-');
                    methodname = null;
                    if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                        throw new LayerGenerationException("Class needs to be public", e, processingEnv, reg); // NOI18N
                    }
                    if (!processingEnv.getTypeUtils().isAssignable(e.asType(), project)) {
                        throw new LayerGenerationException("Class needs to extend Project", e, processingEnv, reg); // NOI18N
                    }
                    boolean found = false;
                    for (Element cns : processingEnv.getElementUtils().getAllMembers((TypeElement)e)) {
                        if (cns.getKind() != ElementKind.CONSTRUCTOR) {
                            continue;
                        }
                        ExecutableElement exec = (ExecutableElement)cns;
                        if (!exec.getModifiers().contains(Modifier.PUBLIC)) {
                            continue;
                        }
                        if (exec.getParameters().size() != 1) {
                            continue;
                        }
                        if (exec.getParameters().get(0).asType().equals(antHelper)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        throw new LayerGenerationException("There needs to be public constructor taking AntProjectHelper parameter", e, processingEnv, reg); // NOI18N
                    }

                    break;
                case METHOD:
                    classname = processingEnv.getElementUtils().getBinaryName((TypeElement)e.getEnclosingElement()).toString();
                    methodname = ((ExecutableElement) e).getSimpleName().toString();
                    name = (classname + "." + methodname).replace('.', '-');
                    
                    if (!e.getEnclosingElement().getModifiers().contains(Modifier.PUBLIC)) {
                        throw new LayerGenerationException("Class needs to be public", e, processingEnv, reg); // NOI18N
                    }

                    ExecutableElement exec = (ExecutableElement)e;
                    if (
                        !exec.getModifiers().contains(Modifier.PUBLIC) ||
                        !exec.getModifiers().contains(Modifier.STATIC) ||
                        exec.getParameters().size() != 1 ||
                        !exec.getParameters().get(0).asType().equals(antHelper)
                    ) {
                        throw new LayerGenerationException("The method needs to be public, static and take AntProjectHelper argument", e, processingEnv, reg); // NOI18N
                    }
                    if (!processingEnv.getTypeUtils().isAssignable(exec.getReturnType(), project)) {
                        throw new LayerGenerationException("Method needs to return Project", e, processingEnv, reg); // NOI18N
                    }

                    break;
                default:
                    throw new IllegalArgumentException(e.toString());
            }

            File f = layer(e).
                file("Services/AntBasedProjectTypes/" + reg.type().replace('.', '-') + ".instance").
                stringvalue("type", reg.type()).
                stringvalue("iconResource", reg.iconResource()).
                stringvalue("sharedName", reg.sharedName()).
                stringvalue("sharedNamespace", reg.sharedNamespace()).
                stringvalue("privateName", reg.privateName()).
                stringvalue("privateNamespace", reg.privateNamespace()).
                stringvalue("className", classname).
                stringvalue("instanceOf", AntBasedProjectType.class.getName()).
                methodvalue("instanceCreate", AntBasedProjectFactorySingleton.class.getName(), "create");

            if (methodname != null) {
                f = f.stringvalue("methodName", methodname);
            }
            f.write();
        }
        return true;
    }

}

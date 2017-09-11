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
package org.netbeans.modules.openide.loaders;

import java.util.*;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Eric Barboni <skygo@netbeans.org>
 */
@ServiceProvider(service = Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({"org.openide.loaders.DataObject.Registration", "org.openide.loaders.DataObject.Registrations"})
public class DataObjectFactoryProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }

        for (Element e : roundEnv.getElementsAnnotatedWith(DataObject.Registration.class)) {
            DataObject.Registration dfr = e.getAnnotation(DataObject.Registration.class);
            if (dfr == null) {
                continue;
            }
            process(e, dfr);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(DataObject.Registrations.class)) {
            DataObject.Registrations dfrr = e.getAnnotation(DataObject.Registrations.class);
            if (dfrr == null) {
                continue;
            }
            for (DataObject.Registration t : dfrr.value()) {
                process(e, t);
            }
        }
        return true;
    }

    //
    private void process(Element e, DataObject.Registration dfr) throws LayerGenerationException {
        TypeMirror dataObjectType = type(DataObject.class);
        TypeMirror fileObjectType = type(FileObject.class);
        TypeMirror multiFileLoaderType = type(MultiFileLoader.class);
        TypeMirror dataObjectFactoryType = type(DataObject.Factory.class);
        LayerBuilder builder = layer(e);
        //need class name to generate id and factory dataObjectClass parameter
        String className = processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString();
        String factoryId = className.replace(".", "-");

        boolean useFactory = true;

        // test if enclosing element is of DataObject type;

        if (isAssignable(e.asType(), dataObjectType)) {
            //attempt to use default factory 
            List<Element> ee = new LinkedList<Element>();
            // should be a public constructor with FileObject and MultiFileLoader as param
            for (ExecutableElement element : ElementFilter.constructorsIn(e.getEnclosedElements())) {

                if ((element.getKind() == ElementKind.CONSTRUCTOR) && (element.getModifiers().contains(Modifier.PUBLIC))) {  // found a public constructor ;                        
                    if ((element.getParameters().size() == 2) // parameters of constructor ok
                            && (isAssignable(element.getParameters().get(0).asType(), fileObjectType))
                            && (isAssignable(element.getParameters().get(1).asType(), multiFileLoaderType))) {
                        ee.add(element);
                    }
                }
            }
            // nothing is found 
            if (ee.isEmpty()) {
                throw new LayerGenerationException("DataObject subclass with @DataObject.Registration needs a public constructor with FileObject and MultiFileLoader parameters", e, processingEnv, dfr); // NOI18N
            } else {
                useFactory = true;
            }

        } else if (isAssignable(e.asType(), dataObjectFactoryType)) {
            List<Element> ee = new LinkedList<Element>();
            for (ExecutableElement element : ElementFilter.constructorsIn(e.getEnclosedElements())) {
                if ((element.getKind() == ElementKind.CONSTRUCTOR) && (element.getModifiers().contains(Modifier.PUBLIC))) {  // found a public constructor ;                        
                    if ((element.getParameters().isEmpty())) {// parameters of constructor ok
                        ee.add(element);
                    }
                }
            }
            if (ee.isEmpty()) {
                throw new LayerGenerationException("DataObject.Factory subclass with @DataObject.Registration needs a public default constructor", e, processingEnv, dfr); // NOI18N
            } else {
                useFactory = false;
                factoryId = className.replace(".class", "").replace(".", "-");
            }
        } else {
            throw new LayerGenerationException("Usage @DataObject.Registration only on DataObject.Factory subclass or DataObject subclass", e, processingEnv, dfr); // NOI18N
        }

        // check if mimeType annotation is set
        if (dfr.mimeType() == null) {
            throw new LayerGenerationException("@DataObject.Factory.Registration mimeType() cannot be null", e, processingEnv, dfr, "mimeTypes");
        }

        String aMimeType = dfr.mimeType();
        LayerBuilder.File f = builder.file("Loaders/" + aMimeType + "/Factories/" + factoryId + ".instance");

        // iconBase is optional but if set then shoud be in classpath
        if (dfr.iconBase().length() > 0) {
            builder.validateResource(dfr.iconBase(), e.getEnclosingElement(), dfr, "iconBase", true);
            f.stringvalue("iconBase", dfr.iconBase());
        }

        // position LayerBuilder 
        f.position(dfr.position());

        if (!dfr.displayName().isEmpty()) {
            f.bundlevalue("displayName", dfr.displayName(), dfr, "displayName");
        }

        if (useFactory) {
            f.methodvalue("instanceCreate", "org.openide.loaders.DataLoaderPool", "factory");
            f.stringvalue("dataObjectClass", className);
            // if factory mimetype is needed otherwise not
            f.stringvalue("mimeType", aMimeType);
        }
        f.write();

    }
// reuse from Action Processor
    private TypeMirror type(Class<?> type) {
        final TypeElement e = processingEnv.getElementUtils().getTypeElement(type.getCanonicalName());
        return e == null ? null : e.asType();
    }

    // reuse from Action Processor
    private boolean isAssignable(TypeMirror first, TypeMirror snd) {
        if (snd == null) {
            return false;
        } else {
            return processingEnv.getTypeUtils().isAssignable(first, snd);
        }
    }
}

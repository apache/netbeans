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
package org.netbeans.modules.openide.loaders;

import java.util.*;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
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

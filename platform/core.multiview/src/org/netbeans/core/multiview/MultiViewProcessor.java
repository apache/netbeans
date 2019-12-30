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

package org.netbeans.core.multiview;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Register {@link MultiViewElement}s for given mime types.
 * 
 * @author Jaroslav Tulach
 */
@ServiceProvider(service=Processor.class)
public class MultiViewProcessor extends LayerGeneratingProcessor {
    public @Override Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(
            MultiViewElement.Registration.class.getCanonicalName()
        ));
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        TypeMirror pane = null;
        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(CloneableEditorSupport.Pane.class.getCanonicalName());
        if(typeElement != null) {
            pane = typeElement.asType();
        }
        
        for (Element e : roundEnv.getElementsAnnotatedWith(MultiViewElement.Registration.class)) {
            MultiViewElement.Registration mvr = e.getAnnotation(MultiViewElement.Registration.class);
            if (mvr.mimeType().length == 0) {
                throw new LayerGenerationException("You must specify mimeType", e, processingEnv, mvr, "mimeType");
            }
            TypeMirror[] exprType = new TypeMirror[1];
            String[] binAndMethodNames = findDefinition(e, exprType, mvr);
            String fileBaseName = binAndMethodNames[0].replace('.', '-');
            if (binAndMethodNames[1] != null) {
                fileBaseName += "-" + binAndMethodNames[1];
            }
            for (String type : mvr.mimeType()) {
                final LayerBuilder builder = layer(e);
                LayerBuilder.File f = builder.file("Editors/" + (type.equals("") ? "" : type + '/') + "MultiView/" + fileBaseName + ".instance");
                f.methodvalue("instanceCreate", MultiViewFactory.class.getName(), "createMultiViewDescription");
                f.stringvalue("instanceClass", ContextAwareDescription.class.getName());
                f.stringvalue("class", binAndMethodNames[0]);
                f.bundlevalue("displayName", mvr.displayName(), mvr, "displayName");
                if (!mvr.iconBase().isEmpty()) {
                    builder.validateResource(mvr.iconBase(), e, mvr, "iconBase", true);
                    f.stringvalue("iconBase", mvr.iconBase());
                }
                f.stringvalue("preferredID", mvr.preferredID());
                f.intvalue("persistenceType", mvr.persistenceType());
                f.position(mvr.position());
                if (binAndMethodNames[1] != null) {
                    f.stringvalue("method", binAndMethodNames[1]);
                }
                if (pane != null && processingEnv.getTypeUtils().isAssignable(exprType[0], pane)) {
                    f.boolvalue("sourceview", true);
                }
                f.write();
            }
        }
        return true;
    }

    private String[] findDefinition(Element e, TypeMirror[] type, MultiViewElement.Registration mvr) throws LayerGenerationException {
        final TypeElement lkpElem = processingEnv.getElementUtils().getTypeElement(Lookup.class.getCanonicalName());
        final TypeMirror lkp = lkpElem == null ? null : lkpElem.asType();
        final TypeMirror mve = processingEnv.getElementUtils().getTypeElement(MultiViewElement.class.getName()).asType();
        if (e.getKind() == ElementKind.CLASS) {
            TypeElement clazz = (TypeElement) e;
            if (!processingEnv.getTypeUtils().isAssignable(clazz.asType(), mve)) {
                throw new LayerGenerationException("Not assignable to " + mve, e, processingEnv, mvr);
            }
            int constructorCount = 0;
            CONSTRUCTOR: for (ExecutableElement constructor : ElementFilter.constructorsIn(clazz.getEnclosedElements())) {
                if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
                    continue;
                }
                List<? extends VariableElement> params = constructor.getParameters();
                if (params.size() > 1) {
                    continue;
                }
                for (VariableElement param : params) {
                    if (!param.asType().equals(lkp)) {
                        continue CONSTRUCTOR;
                    }
                }
            }
            if (!clazz.getModifiers().contains(Modifier.PUBLIC)) {
                throw new LayerGenerationException("Class must be public", e, processingEnv, mvr);
            }
            type[0] = e.asType();
            return new String[] {processingEnv.getElementUtils().getBinaryName(clazz).toString(), null};
        } else {
            ExecutableElement meth = (ExecutableElement) e;
            if (!processingEnv.getTypeUtils().isAssignable(meth.getReturnType(), mve)) {
                throw new LayerGenerationException("Not assignable to " + mve, e, processingEnv, mvr);
            }
            if (!meth.getModifiers().contains(Modifier.PUBLIC)) {
                throw new LayerGenerationException("Method must be public", e, processingEnv, mvr);
            }
            if (!meth.getModifiers().contains(Modifier.STATIC)) {
                throw new LayerGenerationException("Method must be static", e, processingEnv, mvr);
            }
            List<? extends VariableElement> params = meth.getParameters();
            if (params.size() > 1) {
                throw new LayerGenerationException("Method must take at most one parameter", e, processingEnv, mvr);
            }
            for (VariableElement param : params) {
                if (!param.asType().equals(lkp)) {
                    throw new LayerGenerationException("Method parameter may only be Lookup", e, processingEnv, mvr);
                }
            }
            if (!meth.getEnclosingElement().getModifiers().contains(Modifier.PUBLIC)) {
                throw new LayerGenerationException("Class must be public", e, processingEnv, mvr);
            }
            type[0] = meth.getReturnType();
            return new String[] {
                processingEnv.getElementUtils().getBinaryName((TypeElement) meth.getEnclosingElement()).toString(),
                meth.getSimpleName().toString()};
        }
    }

}

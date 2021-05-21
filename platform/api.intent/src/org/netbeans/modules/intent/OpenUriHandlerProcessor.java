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
package org.netbeans.modules.intent;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.intent.Intent;
import org.netbeans.spi.intent.IntentHandlerRegistration;
import org.netbeans.spi.intent.Result;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jhavlin
 */
@ServiceProvider(service = Processor.class)
@SupportedAnnotationTypes("org.netbeans.spi.intent.IntentHandlerRegistration")
public class OpenUriHandlerProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) throws LayerGenerationException {

        for (Element e : roundEnv.getElementsAnnotatedWith(
                IntentHandlerRegistration.class)) {
            IntentHandlerRegistration r = e.getAnnotation(
                    IntentHandlerRegistration.class);
            registerHandler(e, r);
        }
        return true;
    }

    private static final String SUFFIX = ".instance";                   //NOI18N

    private void registerHandler(Element e, IntentHandlerRegistration r)
            throws LayerGenerationException {

        TypeElement intentTypeElement = getTypeElement(Intent.class);
        TypeElement objectTypeElement = getTypeElement(Object.class);
        TypeElement resultTypeElement = getTypeElement(Result.class);

        if (!ElementKind.METHOD.equals(e.getKind())) {
            throw error(e, "The annotation can be applied only to"      //NOI18N
                    + " a method.");//NOI18N
        }
        if (!e.getModifiers().contains(Modifier.STATIC)) {
            throw error(e, "The annotated method must be static.");     //NOI18N
        }

        ExecutableElement ee;
        if (e instanceof ExecutableElement) {
            ee = (ExecutableElement) e;
        } else {
            throw error(e, "Annotated element must be an "              //NOI18N
                    + "ExecutableElement");                             //NOI18N

        }

        String type;
        if (ee.getParameters().size() == 1
                && hasParameter(ee, 0, intentTypeElement)
                && hasResultType(ee, objectTypeElement)) {
            type = "RETURN";
        } else if (ee.getParameters().size() == 2
                && hasParameter(ee, 0, intentTypeElement)
                && hasParameter(ee, 1, resultTypeElement)
                && hasVoidResultType(ee)) {
            type = "SETBACK";
        } else {
            throw error(e, "The handler method must take a "            //NOI18N
                    + "single argument of type "                        //NOI18N
                    + "org.netbeans.api.intent.Intent and return Object"//NOI18N
                    + "; or take two arguments of types"                //NOI18N
                    + "org.netbeans.api.intent.Intent"                  //NOI18N
                    + "and org.netbeans.spi.intent.Result"              //NOI18N
                    + " and return void.");                             //NOI18N
        }

        boolean takeAll = false;
        boolean empty = true;
        StringBuilder sb = new StringBuilder();
        for (String action: r.actions()) {
            if ("*".equals(action)) {
                takeAll = true;
                break;
            } else {
                if (!empty) {
                    sb.append(',');
                }
                sb.append(action);
                empty = false;
            }
        }
        String actions = takeAll ? "*" : sb.toString();

        final LayerBuilder b = layer(e);
        File f = b.file("Services/Intent/Handlers/" //NOI18N
                + getName(e).replace('.', '-') + SUFFIX);
        f.position(r.position());
        f.stringvalue("instanceClass",                                  //NOI18N
                IntentHandler.class.getCanonicalName());
        f.methodvalue("instanceCreate", IntentHandler.class.getCanonicalName(),
                "create");                                              //NOI18N
        f.bundlevalue("displayName", r.displayName());                  //NOI18N
        f.stringvalue("uriPattern", r.uriPattern());                    //NOI18N
        f.stringvalue("icon", r.icon());                                //NOI18N
        f.stringvalue("type", type);                                    //NOI18N
        f.stringvalue("actions", actions);                              //NOI18N
        f.write();
    }

    private String getName(Element e) {
        if (e.getKind().isClass() || e.getKind().isInterface()) {
            return processingEnv.getElementUtils().getBinaryName(
                    (TypeElement) e).toString();
        } else if (e.getKind() == ElementKind.PACKAGE) {
            return e.getSimpleName().toString();
        } else {
            return getName(e.getEnclosingElement()) + '.' + e.getSimpleName();
        }
    }

    private boolean hasParameter(ExecutableElement ee, int pos,
            Element typeElement) {
        return processingEnv.getTypeUtils().asElement(
                ee.getParameters().get(pos).asType()).equals(
                        typeElement);
    }

    private boolean hasVoidResultType(ExecutableElement ee) {
        TypeMirror returnType = ee.getReturnType();
        Element returnTypeElement = processingEnv.getTypeUtils().asElement(
                returnType);
        return returnTypeElement == null;
    }

    private boolean hasResultType(ExecutableElement ee, Element typeElement) {
        TypeMirror returnType = ee.getReturnType();
        Element returnTypeElement = processingEnv.getTypeUtils().asElement(
                returnType);
        return returnTypeElement.equals(typeElement);
    }

    private TypeElement getTypeElement(Class<?> cls) {
        TypeElement typeElement = processingEnv.getElementUtils()
                .getTypeElement(cls.getCanonicalName());
        return typeElement;
    }

    private IllegalArgumentException error(Element e, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getEnclosingElement().toString());
        sb.append("."); //NOI18N
        sb.append(e.getSimpleName());
        sb.append(":"); //NOI18N
        sb.append(System.lineSeparator());
        sb.append(msg);
        return new IllegalArgumentException(sb.toString());
    }
}

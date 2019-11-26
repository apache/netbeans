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

package org.netbeans.junit.internal;

import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.lookup.ServiceProvider;

/**
 * Just verifies usage.
 */
@ServiceProvider(service=Processor.class)
public class RandomlyFailsProcessor extends AbstractProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(RandomlyFails.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            for (Element e : roundEnv.getElementsAnnotatedWith(RandomlyFails.class)) {
                Element typeEl = e.getKind() == ElementKind.METHOD ? e.getEnclosingElement() : e;
                TypeMirror nbTestCaseType = processingEnv.getElementUtils().
                        getTypeElement(NbTestCase.class.getName()).asType();
                if (!processingEnv.getTypeUtils().isAssignable(typeEl.asType(), nbTestCaseType)) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, "@RandomlyFails must be used on NbTestCase subclasses", e);
                }
            }
            return true;
        }
        return false;
    }

}

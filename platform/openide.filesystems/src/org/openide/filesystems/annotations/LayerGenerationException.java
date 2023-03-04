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

package org.openide.filesystems.annotations;

import java.lang.annotation.Annotation;
import java.util.Map;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Exception thrown when a layer entry cannot be generated due to erroneous sources.
 * @see LayerGeneratingProcessor
 * @since org.openide.filesystems 7.15
 */
public class LayerGenerationException extends Exception {

    final Element erroneousElement;
    final AnnotationMirror erroneousAnnotation;
    final AnnotationValue erroneousAnnotationValue;

    /**
     * An exception with no associated element.
     * @param message a detail message which could be reported to the user
     * @see Messager#printMessage(javax.tools.Diagnostic.Kind, CharSequence)
     */
    public LayerGenerationException(String message) {
        this(message, (Element) null, (AnnotationMirror) null, (AnnotationValue) null);
    }

    /**
     * An exception with an associated element.
     * @param message a detail message which could be reported to the user
     * @param erroneousElement the associated element
     * @see Messager#printMessage(javax.tools.Diagnostic.Kind, CharSequence, Element)
     */
    public LayerGenerationException(String message, Element erroneousElement) {
        this(message, erroneousElement, (AnnotationMirror) null, (AnnotationValue) null);
    }

    /**
     * An exception with an associated annotation.
     * @param message a detail message which could be reported to the user
     * @param erroneousElement the associated element
     * @param erroneousAnnotation the annotation on the element
     * @see Messager#printMessage(javax.tools.Diagnostic.Kind, CharSequence, Element, AnnotationMirror)
     */
    public LayerGenerationException(String message, Element erroneousElement, AnnotationMirror erroneousAnnotation) {
        this(message, erroneousElement, erroneousAnnotation, (AnnotationValue) null);
    }

    /**
     * An exception with an associated annotation value.
     * @param message a detail message which could be reported to the user
     * @param erroneousElement the associated element
     * @param erroneousAnnotation the annotation on the element
     * @param erroneousAnnotationValue the value of that annotation
     * @see Messager#printMessage(javax.tools.Diagnostic.Kind, CharSequence, Element, AnnotationMirror, AnnotationValue)
     */
    public LayerGenerationException(String message, Element erroneousElement, AnnotationMirror erroneousAnnotation, AnnotationValue erroneousAnnotationValue) {
        super(message);
        this.erroneousElement = erroneousElement;
        this.erroneousAnnotation = erroneousAnnotation;
        this.erroneousAnnotationValue = erroneousAnnotationValue;
    }

    /**
     * An exception with an associated annotation.
     * Convenience constructor which locates an annotation on the erroneous element for you.
     * @param message a detail message which could be reported to the user
     * @param erroneousElement the associated element
     * @param processingEnv the processing environment passed to the processor
     * @param erroneousAnnotation the reflected annotation on the element (may be null as a convenience)
     * @see Messager#printMessage(javax.tools.Diagnostic.Kind, CharSequence, Element, AnnotationMirror)
     * @since 7.50
     */
    public LayerGenerationException(String message, Element erroneousElement, ProcessingEnvironment processingEnv,
            Annotation erroneousAnnotation) {
        this(message, erroneousElement, processingEnv, erroneousAnnotation, (String) null);
    }

    /**
     * An exception with an associated annotation value.
     * Convenience constructor which locates an annotation and its value on the erroneous element for you.
     * @param message a detail message which could be reported to the user
     * @param erroneousElement the associated element
     * @param processingEnv the processing environment passed to the processor
     * @param erroneousAnnotation the reflected annotation on the element (may be null as a convenience)
     * @param erroneousAnnotationMethod the name of a method in that annotation (may be null)
     * @see Messager#printMessage(javax.tools.Diagnostic.Kind, CharSequence, Element, AnnotationMirror, AnnotationValue)
     * @since 7.50
     */
    public LayerGenerationException(String message, Element erroneousElement, ProcessingEnvironment processingEnv,
            Annotation erroneousAnnotation, String erroneousAnnotationMethod) {
        super(message);
        this.erroneousElement = erroneousElement;
        if (erroneousAnnotation != null) {
            Class<? extends Annotation> clazz = null;
            Class<?> implClass = erroneousAnnotation.getClass();
            for (Class<?> xface : implClass.getInterfaces()) {
                if (xface.isAnnotation()) {
                    if (clazz == null) {
                        clazz = xface.asSubclass(Annotation.class);
                    } else {
                        throw new IllegalArgumentException(">1 annotation implemented by " + implClass.getName());
                    }
                }
            }
            if (clazz == null) {
                throw new IllegalArgumentException("no annotation implemented by " + implClass.getName());
            }
            if (erroneousAnnotationMethod != null) {
                try {
                    clazz.getMethod(erroneousAnnotationMethod);
                } catch (NoSuchMethodException x) {
                    throw new IllegalArgumentException("No such method " + erroneousAnnotationMethod + " in " + erroneousAnnotation);
                } catch (SecurityException x) {/* ignore? */}
            }
            this.erroneousAnnotation = findAnnotationMirror(erroneousElement, processingEnv, clazz);
            this.erroneousAnnotationValue = this.erroneousAnnotation != null && erroneousAnnotationMethod != null ?
                findAnnotationValue(this.erroneousAnnotation, erroneousAnnotationMethod) : null;
        } else {
            this.erroneousAnnotation = null;
            this.erroneousAnnotationValue = null;
        }
    }

    private static AnnotationMirror findAnnotationMirror(Element element, ProcessingEnvironment processingEnv, Class<? extends Annotation> annotation) {
        for (AnnotationMirror ann : element.getAnnotationMirrors()) {
            if (processingEnv.getElementUtils().getBinaryName((TypeElement) ann.getAnnotationType().asElement()).
                    contentEquals(annotation.getName())) {
                return ann;
            }
        }
        return null;
    }

    private AnnotationValue findAnnotationValue(AnnotationMirror annotation, String name) {
        for (Map.Entry<? extends ExecutableElement,? extends AnnotationValue> entry : annotation.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

}

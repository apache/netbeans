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

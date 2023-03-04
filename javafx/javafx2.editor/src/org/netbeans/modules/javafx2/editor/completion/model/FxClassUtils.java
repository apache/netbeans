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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author sdedic
 */
public final class FxClassUtils {
    /**
     * Name of the value-of factory method, as per FXML guide
     */
    public static final String NAME_VALUE_OF = "valueOf"; // NOI18N
    
    private static final String DEFAULT_PROPERTY_TYPE_NAME = "javafx.beans.DefaultProperty"; // NO18N
    private static final String DEFAULT_PROPERTY_VALUE_NAME = "value"; // NO18N
    private static final String FXML_ANNOTATION_TYPE = "javafx.fxml.FXML"; // NOI18N
    
    /**
     * Attempts to find the {@code @DefaultProperty} annotation on the type, and returns
     * the default property name. Returns null, if @DefaultProperty is not defined
     * 
     * @param te type to inspect
     * @return default property name, or {@code null} if default property is not defined for the type.
     */
    public static String getDefaultProperty(TypeElement te) {
        for (AnnotationMirror an : te.getAnnotationMirrors()) {
            if (!((TypeElement)an.getAnnotationType().asElement()).getQualifiedName().contentEquals(DEFAULT_PROPERTY_TYPE_NAME)) {
                continue;
            }
            Map<? extends ExecutableElement, ? extends AnnotationValue> m =  an.getElementValues();
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> en : m.entrySet()) {
                if (en.getKey().getSimpleName().contentEquals(DEFAULT_PROPERTY_VALUE_NAME)) {
                    Object v = en.getValue().getValue();
                    return v == null ? null : v.toString();
                }
            }
        }
        return null;
    }
    
    public static boolean isFxmlAccessible(Element el) {
        return el.getModifiers().contains(Modifier.PUBLIC) || isFxmlAnnotated(el);
    }
    
    public static boolean isFxmlAnnotated(Element el) {
        for (AnnotationMirror an : el.getAnnotationMirrors()) {
            if (((TypeElement)an.getAnnotationType().asElement()).getQualifiedName().contentEquals(FXML_ANNOTATION_TYPE)) {
                return true;
            }
        }
        return false;
    }

    public static ExecutableElement findValueOf(TypeElement te, CompilationInfo ci) {
        TypeElement stringType = ci.getElements().getTypeElement("java.lang.String"); // NOI18N      
         List<ExecutableElement> methods = ElementFilter.methodsIn(te.getEnclosedElements());
        for (ExecutableElement e : methods) {
            if (!e.getModifiers().contains(Modifier.STATIC)) {
                //LOG.log(Level.FINE, "rejecting method: {0}; is not static", e);
                continue;
            }
            if (!(e.getModifiers().contains(Modifier.PUBLIC) || isFxmlAnnotated(e))) {
                //LOG.log(Level.FINE, "rejecting method: {0}; is not static", e);
                continue;
            }
            if (!e.getSimpleName().toString().equals(NAME_VALUE_OF)) {
                //LOG.log(Level.FINE, "rejecting method: {0}; different name", e);
                continue;
            }
            if (!ci.getTypes().isSameType(e.getReturnType(), te.asType())) {
                //LOG.log(Level.FINE, "rejecting method: {0}; does not return the type itself", e);
                continue;
            }

            List<? extends VariableElement> params = e.getParameters();
            if (params.size() != 1) {
                //LOG.log(Level.FINE, "rejecting method: {0}; has incorrect number of params ({1})",
                  //      new Object[]{e, params.size()});
                continue;
            }
            VariableElement v = params.get(0);
            TypeMirror vType = v.asType();
            if (!ci.getTypes().isSameType(vType, stringType.asType())) {
                //LOG.log(Level.FINE, "rejecting method: {0}; does not take String parameter ({1})",
                  //      new Object[]{e, vType});
                continue;
            }
            //LOG.fine("Found value-of class");

            return e;
        }

        return null;
    }
    
    private static final String FX_COLOR_CLASS = "javafx.scene.paint.Color"; // NOI18N
    private static final String FX_PAINT_CLASS = "javafx.scene.paint.Paint"; // NOI18N
    private static final String JL_STRING_CLASS = "java.lang.String"; // NOI18N
    
    public static boolean isSimpleType(TypeMirror t, CompilationInfo ci) {
        if (isPrimitive(t)) {
            return true;
        }
        if (t.getKind() == TypeKind.DECLARED) {
            TypeElement tel = (TypeElement)ci.getTypes().asElement(t);
            if (findValueOf(tel, ci) != null) {
                return true;
            }

            // special hack, see BeanAdapter - Color and Paint class can be written as primitives:
            String qn = tel.getQualifiedName().toString();
            if (FX_COLOR_CLASS.equals(qn) || FX_PAINT_CLASS.equals(qn) || JL_STRING_CLASS.equals(qn)) {
                return true;
            }
        }
        
        // arrays
        if (t.getKind() == TypeKind.ARRAY) {
            TypeMirror component = ((ArrayType)t).getComponentType();
            if (component.getKind() == TypeKind.ARRAY) {
                // no support for 2-dim arrays
                return false;
            }
            return isSimpleType(component, ci);
        }
        return false;
    }
    
    public static boolean isPrimitive(TypeMirror t) {
        switch (t.getKind()) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                return true;
        }
        return false;
    }
    
}

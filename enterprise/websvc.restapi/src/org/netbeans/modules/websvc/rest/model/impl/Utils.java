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

package org.netbeans.modules.websvc.rest.model.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.AnnotationValue;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Peter Liu
 */
public class Utils {

    private static final String VALUE = "value";        //NOI18N

    public static String getUriTemplate(Element element) {
        if (hasAnnotationType(element, RestConstants.PATH_JAKARTA)) {
            return getAnnotationValue(element, RestConstants.PATH_JAKARTA, VALUE);
        } else {
            return getAnnotationValue(element, RestConstants.PATH, VALUE);
        }
    }

    public static String getConsumeMime(Element element) {
        if (hasAnnotationType(element, RestConstants.CONSUME_MIME_JAKARTA)) {
            return getAnnotationValue(element, RestConstants.CONSUME_MIME_JAKARTA, VALUE);
        } else {
            return getAnnotationValue(element, RestConstants.CONSUME_MIME, VALUE);
        }
    }

    public static String getProduceMime(Element element) {
        if (hasAnnotationType(element, RestConstants.PRODUCE_MIME_JAKARTA)) {
            return getAnnotationValue(element, RestConstants.PRODUCE_MIME_JAKARTA, VALUE);
        } else {
            return getAnnotationValue(element, RestConstants.PRODUCE_MIME, VALUE);
        }
    }
    
    static void fillQueryParams( Map<String, String> queryParams,
            Element element )
    {
        if ( !( element instanceof ExecutableElement)){
            return;
        }
        ExecutableElement method = (ExecutableElement)element;
        List<? extends VariableElement> parameters = method.getParameters();
        for (VariableElement variableElement : parameters) {
            String paramName = null;
            if ( hasAnnotationType(variableElement, RestConstants.QUERY_PARAM_JAKARTA)){
                paramName = getAnnotationValue(variableElement, 
                        RestConstants.QUERY_PARAM_JAKARTA, VALUE);
            } else if ( hasAnnotationType(variableElement, RestConstants.QUERY_PARAM)){
                paramName = getAnnotationValue(variableElement,
                        RestConstants.QUERY_PARAM, VALUE);
            }
            String defaultValue = null;
            if ( hasAnnotationType(variableElement, RestConstants.DEFAULT_VALUE_JAKARTA)){
                defaultValue = getAnnotationValue(variableElement, 
                        RestConstants.DEFAULT_VALUE_JAKARTA, VALUE);
            } else if ( hasAnnotationType(variableElement, RestConstants.DEFAULT_VALUE)){
                defaultValue = getAnnotationValue(variableElement,
                        RestConstants.DEFAULT_VALUE, VALUE);
            }
            if ( paramName != null ){
                queryParams.put( paramName , defaultValue );
            }
        }
    }

    public static String getApplicationPath(Element element) {
        if(hasAnnotationType(element, RestConstants.APPLICATION_PATH_JAKARTA)) {
            return getAnnotationValue(element, RestConstants.APPLICATION_PATH_JAKARTA, VALUE);
        } else {
            return getAnnotationValue(element, RestConstants.APPLICATION_PATH , VALUE);
        }
    }

    public static String getHttpMethod(Element element) {
        if (hasAnnotationType(element, RestConstants.GET_JAKARTA)
                || hasAnnotationType(element, RestConstants.GET)) {
            return RestConstants.GET_ANNOTATION;
        } else if (hasAnnotationType(element, RestConstants.POST_JAKARTA)
                || hasAnnotationType(element, RestConstants.POST)) {
            return RestConstants.POST_ANNOTATION;
        } else if (hasAnnotationType(element, RestConstants.PUT_JAKARTA)
                || hasAnnotationType(element, RestConstants.PUT)) {
            return RestConstants.PUT_ANNOTATION;
        } else if (hasAnnotationType(element, RestConstants.DELETE_JAKARTA)
                || hasAnnotationType(element, RestConstants.DELETE)) {
            return RestConstants.DELETE_ANNOTATION;
        }
        return null;
    }

    public static boolean hasUriTemplate(Element element) {
        return hasAnnotationType(element, RestConstants.PATH_JAKARTA)
                || hasAnnotationType(element, RestConstants.PATH);
    }
    
    public static boolean hasHttpMethod(Element element) {
        return element.getKind() == ElementKind.METHOD
                && (
                hasAnnotationType(element, RestConstants.GET_JAKARTA)
                || hasAnnotationType(element, RestConstants.GET)
                || hasAnnotationType(element, RestConstants.POST_JAKARTA)
                || hasAnnotationType(element, RestConstants.POST)
                || hasAnnotationType(element, RestConstants.PUT_JAKARTA)
                || hasAnnotationType(element, RestConstants.PUT)
                || hasAnnotationType(element, RestConstants.DELETE_JAKARTA)
                || hasAnnotationType(element, RestConstants.DELETE)
                );
    }
    
    public static boolean hasConsumeMime(Element element) {
        return hasAnnotationType(element, RestConstants.CONSUME_MIME_JAKARTA)
                || hasAnnotationType(element, RestConstants.CONSUME_MIME);
    }
    
    public static boolean hasProduceMime(Element element) {
        return hasAnnotationType(element, RestConstants.PRODUCE_MIME_JAKARTA)
                || hasAnnotationType(element, RestConstants.PRODUCE_MIME);
    }

    private static String getAnnotationValue(Element element, String annotationType, String paramName) {
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            if (annotation.getAnnotationType().toString().equals(annotationType)) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> it :
                        annotation.getElementValues().entrySet()) {
                    //System.out.println("key = " + key.getSimpleName());
                    if (it.getKey().getSimpleName().toString().equals(paramName)) {
                        return stripQuotes(it.getValue().toString());
                    }
                }
            }
        }

        return "";
    }

    private static boolean hasAnnotationType(Element element, String annotationType) {
        if ( element == null ){
            return false;
        }
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            if (annotation.getAnnotationType().toString().equals(annotationType)) {
                return true;
            }
        }

        return false;
    }

    private static String stripQuotes(String value) {
        int index = value.indexOf('"');
        if ( index !=-1 ){
            int lastIndex = value.lastIndexOf('"');
            if ( lastIndex != -1 && index != lastIndex ) {
                return value.substring(index + 1, lastIndex);
            }
        }
        return value;
    }
    
    static void checkForJsr311Bootstrap(TypeElement element, Project project, 
            AnnotationModelHelper helper) 
    {
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        
        if ( restSupport == null || restSupport.isRestSupportOn() ){
            return;
        }
        if ( isRest(element, helper) ){
            // Fix for BZ#201039 - REST configuration dialog appears after expanding a web project with WebLogic target
            FileObject sourceFile = SourceUtils.getFile(ElementHandle.
                    create(element), helper.getClasspathInfo());
            if ( sourceFile == null ){
                // the element is not in source , it's binary
                return;
            }
            try {
                restSupport.ensureRestDevelopmentReady(RestSupport.RestConfig.IDE);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    static boolean isRest(TypeElement type, AnnotationModelHelper helper) {
        boolean isRest = false;
        if (type.getKind() != ElementKind.INTERFACE) { // don't consider interfaces
            if (!type.getModifiers().contains(Modifier.ABSTRACT)) {
                if (helper.hasAnnotation(type.getAnnotationMirrors(), RestConstants.PATH_JAKARTA)
                        || helper.hasAnnotation(type.getAnnotationMirrors(), RestConstants.PATH)) {
                    isRest = true;
                } else {
                    for (Element element : type.getEnclosedElements()) {
                        if (Utils.hasHttpMethod(element)) {
                            isRest = true;
                            break;
                        }
                    }
                }
            }
        }
        return isRest;
    }
    
    static boolean isProvider(TypeElement type, AnnotationModelHelper helper) {
        if (type.getKind() != ElementKind.INTERFACE) { // don't consider interfaces
            if (helper.hasAnnotation(type.getAnnotationMirrors(), RestConstants.PROVIDER_ANNOTATION_JAKARTA)
                    || helper.hasAnnotation(type.getAnnotationMirrors(), RestConstants.PROVIDER_ANNOTATION)) {
                return true;
            }
        }
        return false;
    }

    static boolean isRestApplication(TypeElement type, AnnotationModelHelper helper) {
        boolean isRest = false;
        if (type != null && type.getKind() != ElementKind.INTERFACE) { // don't consider interfaces
            if (helper.hasAnnotation(type.getAnnotationMirrors(), RestConstants.APPLICATION_PATH_JAKARTA)
                    || helper.hasAnnotation(type.getAnnotationMirrors(), RestConstants.APPLICATION_PATH)) {
                isRest = true;
            }
        }
        return isRest;
    }

}

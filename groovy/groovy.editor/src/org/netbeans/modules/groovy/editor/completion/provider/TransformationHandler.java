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
package org.netbeans.modules.groovy.editor.completion.provider;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem.FieldItem;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.api.elements.common.MethodElement.MethodParameter;
import org.netbeans.modules.groovy.editor.java.Utilities;

/**
 * Code completion handler focused on Groovy AST transformations. For example using
 * {@code @Singleton} on a certain Groovy class creates field called "instance" and
 * also accessor with name "getInstance".
 * 
 * @author Martin Janicek
 */
public final class TransformationHandler {
 
    private static final String SINGLETON_ANNOTATION = "Singleton"; // NOI18N
    private static final String DELEGATE_ANNOTATION = "Delegate"; // NOI18N
    
    private static final String SINGLETON_FIELD_NAME = "instance"; // NOI18N
    private static final String SINGLETON_METHOD_NAME = "getInstance"; // NOI18N
    
    
    public static Map<FieldSignature, CompletionItem> getFields(
            final GroovyIndex index,
            final ClassNode typeNode,
            final String prefix,
            final int anchorOffset) {
        
        final Map<FieldSignature, CompletionItem> result = new HashMap<FieldSignature, CompletionItem>();
        
        for (AnnotationNode annotation : typeNode.getAnnotations()) {
            final String annotationName = annotation.getClassNode().getNameWithoutPackage();
            
            if (SINGLETON_ANNOTATION.equals(annotationName)) {
                final FieldSignature signature = new FieldSignature(SINGLETON_FIELD_NAME);
                final CompletionItem proposal = new FieldItem(
                        typeNode.getNameWithoutPackage(),
                        SINGLETON_FIELD_NAME,
                        Modifier.STATIC,
                        anchorOffset);
                
                if (signature.getName().startsWith(prefix)) { // NOI18N
                    result.put(signature, proposal);
                }
            }
        }
        
        for (FieldNode field : typeNode.getFields()) {
            for (AnnotationNode fieldAnnotation : field.getAnnotations()) {
                final String fieldAnnotationName = fieldAnnotation.getClassNode().getNameWithoutPackage();

                // If any field on the current typeNode has @Delegate annotation then iterate
                // through all fields of that field and put them into the code completion result
                if (DELEGATE_ANNOTATION.equals(fieldAnnotationName)) {
                    for (FieldNode annotatedField : field.getType().getFields()) {
                        final FieldSignature signature = new FieldSignature(annotatedField.getName());
                        final CompletionItem fieldProposal = new FieldItem(
                                annotatedField.getType().getNameWithoutPackage(),
                                annotatedField.getName(),
                                annotatedField.getModifiers(),
                                anchorOffset);

                        if (signature.getName().startsWith(prefix)) {
                            result.put(signature, fieldProposal);
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    public static Map<MethodSignature, CompletionItem> getMethods(
            final GroovyIndex index,
            final ClassNode typeNode,
            final String prefix,
            final int anchorOffset) {
        
        final Map<MethodSignature, CompletionItem> result = new HashMap<MethodSignature, CompletionItem>();
        final boolean prefixed = "".equals(prefix) ? false : true; // NOI18N
        
        for (AnnotationNode annotation : typeNode.getAnnotations()) {
            final String annotationName = annotation.getClassNode().getNameWithoutPackage();
            
            if (SINGLETON_ANNOTATION.equals(annotationName)) {
                final MethodSignature signature = new MethodSignature(SINGLETON_METHOD_NAME, new String[0]);
                final CompletionItem proposal = CompletionAccessor.instance().createJavaMethod(
                        typeNode.getNameWithoutPackage(),
                        SINGLETON_METHOD_NAME,
                        Collections.emptyList(),
                        typeNode.getNameWithoutPackage(),
                        Utilities.reflectionModifiersToModel(Modifier.STATIC),
                        anchorOffset,
                        true,
                        false);
                
                if (signature.getName().startsWith(prefix)) {
                    result.put(signature, proposal);
                }
            }
        }
        
        for (FieldNode field : typeNode.getFields()) {
            for (AnnotationNode fieldAnnotation : field.getAnnotations()) {
                final String fieldAnnotationName = fieldAnnotation.getClassNode().getNameWithoutPackage();

                if (DELEGATE_ANNOTATION.equals(fieldAnnotationName)) {
                    for (MethodNode method : field.getType().getMethods()) {
                        final MethodSignature signature = getSignature(method);
                        final CompletionItem proposal = createMethodProposal(method, prefixed, anchorOffset);
                        
                        if (signature.getName().startsWith(prefix)) { // NOI18N
                            result.put(signature, proposal);
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    private static MethodSignature getSignature(MethodNode method) {
        String[] parameters = new String[method.getParameters().length];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = Utilities.translateClassLoaderTypeName(method.getParameters()[i].getName());
        }

        return new MethodSignature(method.getName(), parameters);
    }
    
    private static CompletionItem createMethodProposal(
            final MethodNode method, 
            final boolean prefixed,
            final int anchorOffset) {
        
        final String methodName = method.getName();
        final List<MethodParameter> methodParams = getMethodParams(method);
        final String returnType = method.getReturnType().getName();
        
        return CompletionAccessor.instance().createDynamicMethod(anchorOffset, methodName, methodParams, returnType, prefixed);
    }
    
    private static List<MethodParameter> getMethodParams(MethodNode method) {
        List<MethodParameter> result = new ArrayList<>(method.getParameters().length);
        Parameter[] mps = method.getParameters();
        for (Parameter p : mps) {
            result.add(new MethodParameter(p.getType().getName(), p.getType().getNameWithoutPackage(), p.getName()));
        }
        return result;
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.editor.completion.provider;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem.FieldItem;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
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
                final CompletionItem proposal = CompletionItem.forJavaMethod(
                        typeNode.getNameWithoutPackage(),
                        SINGLETON_METHOD_NAME,
                        Collections.<String>emptyList(),
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
        final String[] methodParams = getMethodParams(method);
        final String returnType = method.getReturnType().getName();

        return CompletionItem.forDynamicMethod(anchorOffset, methodName, methodParams, returnType, prefixed);
    }
    
    private static String[] getMethodParams(MethodNode method) {
        String[] parameters = new String[method.getParameters().length];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = method.getParameters()[i].getName();
        }
        return parameters;
    }
}

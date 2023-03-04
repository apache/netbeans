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
package org.netbeans.modules.web.beans.navigation;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import org.netbeans.api.java.source.CompilationInfo;


/**
 * Based on Utils class from java.navigation
 * @author ads
 *
 */
final class Utils {
    
    static void firstRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            tree.setSelectionRow(0);
            scrollTreeToSelectedRow(tree);
        }
    }

    static void scrollTreeToSelectedRow(final JTree tree) {
        final int selectedRow = tree.getLeadSelectionRow();
        if (selectedRow >=0) {
            SwingUtilities.invokeLater(
                    new Runnable() {
                public void run() {
                    tree.scrollRectToVisible(tree.getRowBounds(selectedRow));
                }
            }
            );
        }
    }
    
    static void previousRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            int selectedRow = tree.getSelectionModel().getMinSelectionRow();
            if (selectedRow == -1) {
                selectedRow = (rowCount -1);
            } else {
                selectedRow--;
                if (selectedRow < 0) {
                    selectedRow = (rowCount -1);
                }
            }
            tree.setSelectionRow(selectedRow);
            scrollTreeToSelectedRow(tree);
        }
    }
    
    static void nextRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            int selectedRow = tree.getSelectionModel().getMinSelectionRow();
            if (selectedRow == -1) {
                selectedRow = 0;
                tree.setSelectionRow(selectedRow);
            } else {
                selectedRow++;
            }
            tree.setSelectionRow(selectedRow % rowCount);
            scrollTreeToSelectedRow(tree);
        }
    }
    
    static void lastRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            tree.setSelectionRow(rowCount - 1);
            scrollTreeToSelectedRow(tree);
        }
    }
    
    static boolean patternMatch(JavaElement javaToolsJavaElement, String pattern, 
            String patternLowerCase) 
    {

        if (pattern == null) {
            return true;
        }

        String patternRegexpString = pattern;

        if (pattern.trim().length() == 0) {
            patternRegexpString = pattern + ".*";
        } else {
            patternRegexpString = pattern.
                    replaceAll(Pattern.quote("*"), Matcher.quoteReplacement(".*")).
                    replaceAll(Pattern.quote("?"), Matcher.quoteReplacement(".")) +
                    (pattern.endsWith("$") ? "" : ".*");
        }

        String name = javaToolsJavaElement.getName();

        try {
            Pattern compiledPattern = Pattern.compile(patternRegexpString,
                    WebBeansNavigationOptions.isCaseSensitive() ? 0
                                                           : Pattern.CASE_INSENSITIVE);
            Matcher m = compiledPattern.matcher(name);

            return m.matches();
        } catch (PatternSyntaxException pse) {
            if (WebBeansNavigationOptions.isCaseSensitive()) {
                return name.startsWith(pattern);
            }

            return name.toLowerCase().startsWith(patternLowerCase);
        }
    }
    
    static String format(Element element, DeclaredType parent ,
            CompilationInfo compilationInfo) 
    {
        return format(element, parent, compilationInfo, false, false);
    }
    
    static String format(Element element, DeclaredType parent , 
            CompilationInfo compilationInfo, boolean forSignature, boolean FQNs) 
    {
        StringBuilder stringBuilder = new StringBuilder();
        format(element, parent , compilationInfo , stringBuilder, forSignature, FQNs);

        return stringBuilder.toString();
    }
    
    static void format(Element element, DeclaredType parent ,  
            CompilationInfo compilationInfo , StringBuilder stringBuilder, 
            boolean forSignature, boolean FQNs) 
    {
        if (element == null) {
            return;
        }

        Set<Modifier> modifiers = element.getModifiers();

        switch (element.getKind()) {
        case PACKAGE:
            break;
        case CLASS:
        case INTERFACE:
        case ENUM:
        case ANNOTATION_TYPE:
            if (forSignature) {
                stringBuilder.append(toString(modifiers));
                if (modifiers.size() > 0) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(" ");
                    }
                }
            }
            
            if (forSignature) {
                switch (element.getKind()) {
                    case CLASS:
                        stringBuilder.append("class "); // NOI18N
                        break;
                    case INTERFACE:
                        stringBuilder.append("interface "); // NOI18N
                        break;
                    case ENUM:
                        stringBuilder.append("enum "); // NOI18N
                        break;
                    case ANNOTATION_TYPE:
                        stringBuilder.append("@interface "); // NOI18N
                        break;
                }
            }
            
            TypeElement typeElement = (TypeElement) element;
            stringBuilder.append(FQNs
                ? typeElement.getQualifiedName().toString()
                : typeElement.getSimpleName().toString());

            formatTypeParameters(typeElement.getTypeParameters(), 
                    compilationInfo, stringBuilder, FQNs);

            break;

        case CONSTRUCTOR:
            break;

        case METHOD:
            ExecutableElement methodElement = (ExecutableElement) element;
            ExecutableType methodType = (ExecutableType)compilationInfo.getTypes().
                asMemberOf(parent, methodElement);
            TypeMirror returnTypeMirror = methodType.getReturnType();
            /*List<?extends TypeParameterElement> typeParameters = 
                methodElement.getTypeParameters();*/
            List<? extends TypeVariable> typeVars = methodType.getTypeVariables();

            if (forSignature) {
                stringBuilder.append(toString(modifiers));
                
                if (modifiers.size() > 0) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(" ");
                    }
                }

                /*if ((typeParameters != null) && (typeParameters.size() > 0)) {
                    formatTypeParameters(typeParameters, compilationInfo, 
                            stringBuilder, FQNs);
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(" ");
                    }
                }*/
                if ((typeVars != null) && (typeVars.size() > 0)) {
                    formatTypeMirrors(typeVars, stringBuilder, FQNs);
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(" ");
                    }
                }

                formatTypeMirror(returnTypeMirror, stringBuilder, FQNs);
            }

            if (stringBuilder.length() > 0) {
                    stringBuilder.append(" ");
            }

            stringBuilder.append(methodElement.getSimpleName().toString());
            stringBuilder.append("(");
            List<? extends TypeMirror> parameterTypes = methodType.getParameterTypes();
            /*formatVariableElements(methodElement.getParameters(),
                methodElement.isVarArgs(), compilationInfo, stringBuilder, FQNs);*/
            formatTypeMirrors(parameterTypes, stringBuilder, FQNs);
            if (methodElement.isVarArgs()) {
                stringBuilder.append("...");
            }
            stringBuilder.append(")");

            List<? extends TypeMirror> thrownTypesMirrorsByMethod = methodElement.getThrownTypes();
            if (!thrownTypesMirrorsByMethod.isEmpty()) {
                stringBuilder.append(" throws "); // NOI18N
                formatTypeMirrors(thrownTypesMirrorsByMethod, stringBuilder, FQNs);
            }

            if (forSignature) {
                AnnotationValue annotationValue = methodElement.getDefaultValue();
                if (annotationValue != null) {
                    Object annotationValueValue = annotationValue.getValue();
                    if (annotationValueValue != null) {
                        stringBuilder.append(" default "); // NOI18N
                        if (annotationValueValue instanceof String) {
                            stringBuilder.append("\"");
                        } else if (annotationValueValue instanceof Character) {
                            stringBuilder.append("\'");
                        } 
                        stringBuilder.append(String.valueOf(annotationValueValue));
                        if (annotationValueValue instanceof String) {
                            stringBuilder.append("\"");
                        } else if (annotationValueValue instanceof Character) {
                            stringBuilder.append("\'");
                        }                    
                    }
                }
            } else {
                stringBuilder.append(":");

                formatTypeMirror(returnTypeMirror, stringBuilder, FQNs);

                /*if ((typeParameters != null) && (typeParameters.size() > 0)) {
                    stringBuilder.append(":");
                    formatTypeParameters(typeParameters, compilationInfo, 
                            stringBuilder, FQNs);
                }*/
                if ((typeVars != null) && (typeVars.size() > 0)) {
                    stringBuilder.append(":");
                    formatTypeMirrors(typeVars, stringBuilder, FQNs);
                }

            }

            break;

        case TYPE_PARAMETER:
            break;

        case FIELD:
            VariableElement fieldElement = (VariableElement) element;
            TypeMirror fieldType = compilationInfo.getTypes().
                asMemberOf(parent, fieldElement);
            if (forSignature) {
                stringBuilder.append(toString(modifiers));

                if (stringBuilder.length() > 0) {
                    stringBuilder.append(" ");
                }

                formatTypeMirror(fieldType, stringBuilder, FQNs);
            }

            if (stringBuilder.length() > 0) {
                stringBuilder.append(" ");
            }

            stringBuilder.append(fieldElement.getSimpleName().toString());

            if (forSignature) {
                Object fieldValue = fieldElement.getConstantValue();
                if (fieldValue != null) {
                    stringBuilder.append(" = ");
                    if (fieldValue instanceof String) {
                        stringBuilder.append("\"");
                    } else if (fieldValue instanceof Character) {
                        stringBuilder.append("\'");
                    } 
                    stringBuilder.append(String.valueOf(fieldValue));
                    if (fieldValue instanceof String) {
                        stringBuilder.append("\"");
                    } else if (fieldValue instanceof Character) {
                        stringBuilder.append("\'");
                    }                    
                }
            } else {
                stringBuilder.append(":");

                formatTypeMirror(fieldType, stringBuilder, FQNs);
            }
            
            break;

        case ENUM_CONSTANT:

            break;

        case PARAMETER:
        case LOCAL_VARIABLE:
            break;
        }
    }
    
    static String toString(Set<Modifier> modifiers) {
        return java.lang.reflect.Modifier.toString(getIntModifiers(modifiers));
    }
    
    static int getIntModifiers(Set<Modifier> modifiers) {
        int intModifiers = 0;

        if (modifiers.contains(Modifier.ABSTRACT)) {
            intModifiers |= java.lang.reflect.Modifier.ABSTRACT;
        }

        if (modifiers.contains(Modifier.FINAL)) {
            intModifiers |= java.lang.reflect.Modifier.FINAL;
        }

        if (modifiers.contains(Modifier.NATIVE)) {
            intModifiers |= java.lang.reflect.Modifier.NATIVE;
        }

        if (modifiers.contains(Modifier.PRIVATE)) {
            intModifiers |= java.lang.reflect.Modifier.PRIVATE;
        }

        if (modifiers.contains(Modifier.PROTECTED)) {
            intModifiers |= java.lang.reflect.Modifier.PROTECTED;
        }

        if (modifiers.contains(Modifier.PUBLIC)) {
            intModifiers |= java.lang.reflect.Modifier.PUBLIC;
        }

        if (modifiers.contains(Modifier.STATIC)) {
            intModifiers |= java.lang.reflect.Modifier.STATIC;
        }

        if (modifiers.contains(Modifier.STRICTFP)) {
            intModifiers |= java.lang.reflect.Modifier.STRICT;
        }

        if (modifiers.contains(Modifier.SYNCHRONIZED)) {
            intModifiers |= java.lang.reflect.Modifier.SYNCHRONIZED;
        }

        if (modifiers.contains(Modifier.TRANSIENT)) {
            intModifiers |= java.lang.reflect.Modifier.TRANSIENT;
        }

        if (modifiers.contains(Modifier.VOLATILE)) {
            intModifiers |= java.lang.reflect.Modifier.VOLATILE;
        }

        return intModifiers;
    }
    
    static void formatTypeParameters(
            List<? extends TypeParameterElement> typeParameters, 
            CompilationInfo compilationInfo,StringBuilder stringBuilder, 
            boolean FQNs )
    {
        if ((typeParameters == null) || (typeParameters.size() == 0)) {
            return;
        }

        boolean first = true;
        if (typeParameters.size() > 0) {
            stringBuilder.append("<");
            first = true;

            for (TypeParameterElement typeParameterElement : typeParameters) {
                if (first) {
                    first = false;
                }
                else {
                    stringBuilder.append(", ");
                }

                format(typeParameterElement, null, compilationInfo,
                        stringBuilder, false, FQNs);
            }

            stringBuilder.append(">");
        }
    }
    
    static void formatTypeMirrors( List<? extends TypeMirror> typeMirros,
            StringBuilder stringBuilder, boolean FQNs )
    {
        if ((typeMirros == null) || (typeMirros.size() == 0)) {
            return;
        }

        boolean first = true;

        for (TypeMirror typeMirror : typeMirros) {
            if (first) {
                first = false;
            }
            else {
                stringBuilder.append(", ");
            }

            formatTypeMirror(typeMirror, stringBuilder, FQNs);
        }
    }
    
    static void formatTypeMirror( TypeMirror typeMirror,
            StringBuilder stringBuilder, boolean FQNs )
    {
        if (typeMirror == null) {
            return;
        }

        switch (typeMirror.getKind()) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case NONE:
            case NULL:
            case SHORT:
            case VOID:
                stringBuilder.append(typeMirror);

                break;

            case TYPEVAR:
                TypeVariable typeVariable = (TypeVariable) typeMirror;
                stringBuilder.append(typeVariable.asElement().getSimpleName()
                        .toString());
                break;

            case WILDCARD:
                WildcardType wildcardType = (WildcardType) typeMirror;
                stringBuilder.append("?");
                if (wildcardType.getExtendsBound() != null) {
                    stringBuilder.append(" extends "); // NOI18N
                    formatTypeMirror(wildcardType.getExtendsBound(),
                            stringBuilder, FQNs);
                }
                if (wildcardType.getSuperBound() != null) {
                    stringBuilder.append(" super "); // NOI18N
                    formatTypeMirror(wildcardType.getSuperBound(),
                            stringBuilder, FQNs);
                }

                break;

            case DECLARED:
                DeclaredType declaredType = (DeclaredType) typeMirror;
                Element element = declaredType.asElement();
                if (element instanceof TypeElement) {
                    stringBuilder.append(FQNs ? ((TypeElement) element)
                            .getQualifiedName().toString() : element
                            .getSimpleName().toString());
                }
                else {
                    stringBuilder.append(element.getSimpleName().toString());
                }
                List<? extends TypeMirror> typeArgs = declaredType
                        .getTypeArguments();
                if (!typeArgs.isEmpty()) {
                    stringBuilder.append("<");
                    formatTypeMirrors(typeArgs, stringBuilder, FQNs);
                    stringBuilder.append(">");
                }

                break;

            case ARRAY:

                int dims = 0;

                while (typeMirror.getKind() == TypeKind.ARRAY) {
                    dims++;
                    typeMirror = ((ArrayType) typeMirror).getComponentType();
                }

                formatTypeMirror(typeMirror, stringBuilder, FQNs);

                for (int i = 0; i < dims; i++) {
                    stringBuilder.append("[]");
                }

                break;
        }
    }
    
    /*static void formatVariableElements(
            List<? extends VariableElement> variableElements, boolean varArgs,
            StringBuilder stringBuilder, boolean FQNs )
    {
        if ((variableElements == null) || (variableElements.size() == 0)) {
            return;
        }

        boolean first = true;

        for (VariableElement variableElement : variableElements) {
            if (first) {
                first = false;
            }
            else {
                stringBuilder.append(", ");
            }

            format(variableElement, stringBuilder, false, FQNs);
        }

        if (varArgs) {
            stringBuilder.append("...");
        }
    }*/
}

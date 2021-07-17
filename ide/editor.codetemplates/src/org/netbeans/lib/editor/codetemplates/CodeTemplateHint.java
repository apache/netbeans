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

package org.netbeans.lib.editor.codetemplates;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Code template hint gives additional specification of what the code template parameter's value should be.
 *
 * <p>
 * The hints are specified in the following form:
 * <pre>
 * ${param hint=value [hint2=value2] ... }
 * ${param hint="string-literal" ... }
 * ${param hint ... }
 * </pre>
 * <br>
 * The hints without explicit <code>=value</code> are assigned with string value "true".
 * <br>
 * The applicability of each hint may be limited to specific languages or otherwise the hint applies to all
 * possible languages.
 *
 * @author Arthur Sadykov
 * 
 * @since 1.54
 */
public enum CodeTemplateHint {
    /**
     * Allows the template to be used to surround code.
     */
    ALLOW_SURROUND("allowSurround", "${param allowSurround}"), //NOI18N

    /**
     * Requires the parameter value to be of an array type (including arrays of primitive data types).
     */
    ARRAY("array", "${param array}"), //NOI18N

    /**
     * Defines that the parameter value would be a type cast if necessary.
     */
    CAST("cast", "${param cast editable=false}"), //NOI18N

    /**
     * Requires the code completion to be invoked at the position of the parameter.
     */
    COMPLETION_INVOKE("completionInvoke", "${param completionInvoke}"), //NOI18N

    /**
     * Requires the parameter value to be the fully qualified name of the enclosing class.
     */
    CURRENT_CLASS_FULLY_QUALIFIED_NAME("currClassFQName", "${param currClassFQName editable=false}"), //NOI18N

    /**
     * Requires the parameter value to be the simple name of the enclosing class.
     */
    CURRENT_CLASS_NAME("currClassName", "${param currClassName editable=false}"), //NOI18N

    /**
     * Requires the parameter value to be the name of the enclosing method.
     */
    CURRENT_METHOD_NAME("currMethodName", "${param currMethodName editable=false}"), //NOI18N

    /**
     * Requires the parameter value to be the name of the enclosing package.
     */
    CURRENT_PACKAGE_NAME("currPackageName", "${param currPackageName editable=false}"), //NOI18N

    /**
     * Defines the parameter's default value.
     */
    DEFAULT("default", "${param default=\"\"}"), //NOI18N

    /**
     * Defines whether the parameter value can be edited after the template is expanded.
     */
    EDITABLE("editable", "${param editable=false}"), //NOI18N

    /**
     * Requires the parameter value to be an instance or variable of the given type.
     */
    INSTANCE_OF("instanceof", "${param instanceof=\"\"}"), //NOI18N

    /**
     * Requires the parameter value to be of an array type or an instance of "java.lang.Iterable".
     */
    ITERABLE("iterable", "${param iterable editable=false}"), //NOI18N

    /**
     * Requires the parameter value to be the type of the iterable element.
     */
    ITERABLE_ELEMENT_TYPE("iterableElementType", "${param iterableElementType editable=false}"), //NOI18N

    /**
     * Requires the parameter value to be the type of the expression on the assignment's left side.
     */
    LEFT_SIDE_TYPE("leftSideType", "${param leftSideType editable=false}"), //NOI18N

    /**
     * Defines that the parameter value should be its name.
     */
    NAMED("named", "${param named editable=false}"), //NOI18N

    /**
     * Defines that the parameter value should be a 'fresh' unused variable name in the given context.
     */
    NEW_VAR_NAME("newVarName", "${param newVarName}"), //NOI18N

    /**
     * Defines the sequence in which placeholders are completed.
     *
     * <p>
     * Use 0 for the first element, 1 for the second... Placeholders without ordering information will be placed after
     * the last placeholder with ordering information.
     */
    ORDERING("ordering", "${param ordering=}"), //NOI18N

    /**
     * Requires the parameter value to be the type of the expression on the assignment's right side.
     */
    RIGHT_SIDE_TYPE("rightSideType", "${param rightSideType editable=false}"), //NOI18N

    /**
     * Requires the parameter value to be the name of the specified static member.
     *
     * <p>
     * The infrastructure will try to use the short name and add a static import statement if possible.
     */
    STATIC_IMPORT("staticImport", "${param staticImport=\"\" editable=false}"), //NOI18N

    /**
     * Requires the parameter value to be the given type.
     *
     * <p>
     * The infrastructure will try to use short name and import fully qualified name if possible.
     */
    TYPE("type", "${param type=\"\" editable=false}"), //NOI18N

    /**
     * Defines that the parameter value should be the value of this hint.
     */
    TYPE_VAR("typeVar", "${param typeVar=\"\" editable=false}"), //NOI18N

    UNCAUGHT_EXCEPTION_CATCH_STATEMENTS("uncaughtExceptionCatchStatements", "${param uncaughtExceptionCatchStatements}"), //NOI18N
    UNCAUGHT_EXCEPTION_TYPE("uncaughtExceptionType", "${param uncaughtExceptionType}"), //NOI18N

    /**
     * The parameter value is the name of the closest variable assigned after the code template.
     */
    VARIABLE_FROM_NEXT_ASSIGNMENT_NAME("variableFromNextAssignmentName", //NOI18N
            "${param variableFromNextAssignmentName editable=false}"), //NOI18N

    /**
     * The parameter value is the type of the closest variable assigned after the code template.
     */
    VARIABLE_FROM_NEXT_ASSIGNMENT_TYPE("variableFromNextAssignmentType", //NOI18N
            "${param variableFromNextAssignmentType editable=false}"), //NOI18N

    /**
     * The parameter value is the closest previously assigned variable.
     */
    VARIABLE_FROM_PREVIOUS_ASSIGNMENT("variableFromPreviousAssignment", //NOI18N
            "${param variableFromPreviousAssignment editable=false}"); //NOI18N
    /**
     * The name of the Java programming language.
     */
    public static final String JAVA_LANGUAGE = "Java"; //NOI18N
    /**
     * The name of the PHP programming language.
     */
    public static final String PHP_LANGUAGE = "PHP"; //NOI18N
    public static final String ALL_LANGUAGES = "All Languages"; //NOI18N
    private static final int DEFAULT_NUMBER_OF_LANGUAGES = 2;
    private final String name;
    private final String parameterText;

    private CodeTemplateHint(String name, String parameterText) {
        this.name = name;
        this.parameterText = parameterText;
    }

    /**
     * Returns a set of languages for which this hint is applicable.
     *
     * <p>
     * The language is a name of the programming language denoted by a respective mime type (e.g. Java, PHP).
     * <br>
     * If the hint's applicability is not limited to any specific languages this method returns a set consisting of
     * only one string '{@value #ALL_LANGUAGES}'.
     *
     * @return unmodifiable set of languages for which the given hint is applicable or if the hint applies to all
     *         possible languages a set consisting of a single string '{@value #ALL_LANGUAGES}'.
     *
     * @since 1.54
     */
    public Set<String> getLanguages() {
        Set<String> languages = new HashSet<>(DEFAULT_NUMBER_OF_LANGUAGES);
        switch (this) {
            case ALLOW_SURROUND: {
                languages.add(PHP_LANGUAGE);
                break;
            }
            case ARRAY: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case CAST: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case COMPLETION_INVOKE: {
                languages.add(ALL_LANGUAGES);
                break;
            }
            case CURRENT_CLASS_FULLY_QUALIFIED_NAME: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case CURRENT_CLASS_NAME: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case CURRENT_METHOD_NAME: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case CURRENT_PACKAGE_NAME: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case DEFAULT: {
                languages.add(ALL_LANGUAGES);
                break;
            }
            case EDITABLE: {
                languages.add(ALL_LANGUAGES);
                break;
            }
            case INSTANCE_OF: {
                languages.addAll(Arrays.asList(JAVA_LANGUAGE, PHP_LANGUAGE));
                break;
            }
            case ITERABLE: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case ITERABLE_ELEMENT_TYPE: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case LEFT_SIDE_TYPE: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case NAMED: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case NEW_VAR_NAME: {
                languages.addAll(Arrays.asList(JAVA_LANGUAGE, PHP_LANGUAGE));
                break;
            }
            case ORDERING: {
                languages.add(ALL_LANGUAGES);
                break;
            }
            case RIGHT_SIDE_TYPE: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case STATIC_IMPORT: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case TYPE: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case TYPE_VAR: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case UNCAUGHT_EXCEPTION_CATCH_STATEMENTS: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case UNCAUGHT_EXCEPTION_TYPE: {
                languages.add(JAVA_LANGUAGE);
                break;
            }
            case VARIABLE_FROM_NEXT_ASSIGNMENT_NAME: {
                languages.add(PHP_LANGUAGE);
                break;
            }
            case VARIABLE_FROM_NEXT_ASSIGNMENT_TYPE: {
                languages.add(PHP_LANGUAGE);
                break;
            }
            case VARIABLE_FROM_PREVIOUS_ASSIGNMENT: {
                languages.add(PHP_LANGUAGE);
                break;
            }
        }
        return Collections.unmodifiableSet(languages);
    }

    /**
     * Returns a name of this hint.
     * 
     * @return name of this hint.
     * 
     * @since 1.54
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a string representation of the code template parameter declaration with this hint.
     * 
     * @return string representation of the code template parameter declaration with this hint.
     * 
     * @since 1.54
     */
    public String getParameterText() {
        return parameterText;
    }

    @Override
    public String toString() {
        return name;
    }
}

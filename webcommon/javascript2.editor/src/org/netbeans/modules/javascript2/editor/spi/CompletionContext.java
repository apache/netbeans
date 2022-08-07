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
package org.netbeans.modules.javascript2.editor.spi;

/**
 * Type of completion for {@link CompletionProviderEx}.
 * @author Petr Hejl
 */
public enum CompletionContext {
    NONE, // There shouldn't be any code completion
    EXPRESSION, // usually, we will offer everything what we know in the context
    OBJECT_PROPERTY, // object property that are visible outside the object
    OBJECT_MEMBERS, // usually after this.
    /**
     * This context is before ':' in an object literal definition, when a property
     * is defined. Typically
     * var object_listeral = {
     *  property_name : value
     * }
     *
     * This context can be used by frameworks to suggest the names of properties
     * to define for example various configuration objects.
     */
    OBJECT_PROPERTY_NAME,
    DOCUMENTATION, // inside documentation blocks
    GLOBAL,
    IN_STRING,      // inside a string
    STRING_ELEMENTS_BY_ID, // should offers css elements by id from project
    STRING_ELEMENTS_BY_CLASS_NAME, // should offers css elements by class name from project
    CALL_ARGUMENT, // the position when the cc is called at position of an argument of a function call
    NUMBER, // cc should offer methods of Number objects
    STRING, // cc should offer methods of String objects
    REGEXP,  // cc should offer methods of RegEx objects
    IMPORT_EXPORT_MODULE, // the position where js modules names should be offered
    IMPORT_EXPORT_SPECIAL_TOKENS //the position where as,from keywords should be displayed in CC list
}

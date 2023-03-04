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

package org.netbeans.modules.groovy.editor.api.completion;

/**
 *
 * @author schmidtm
 */
public enum CaretLocation {
    
    ABOVE_PACKAGE("ABOVE_PACKAGE"),         // above the "package" statement (if any).
    ABOVE_FIRST_CLASS("ABOVE_FIRST_CLASS"), // Outside any classs and above the first class or interface stmt.
    OUTSIDE_CLASSES("OUTSIDE_CLASSES"),     // Outside any class but behind some class or interface stmt.
    INSIDE_PACKAGE("INSIDE_PACKAGE"),       // inside package statement
    INSIDE_CLASS("INSIDE_CLASS"),           // inside a class definition but not in a method.
    INSIDE_METHOD("INSIDE_METHOD"),         // in a method definition.
    INSIDE_CLOSURE("INSIDE_CLOSURE"),       // inside a closure definition.
    INSIDE_CONSTRUCTOR_CALL(""),            // inside constructor call
    INSIDE_PARAMETERS("INSIDE_PARAMETERS"), // inside a parameter-list definition (signature) of a method.
    INSIDE_COMMENT("INSIDE_COMMENT"),       // inside a line or block comment
    INSIDE_STRING("INSIDE_STRING"),         // inside string literal
    INSIDE_IMPORT("INSIDE_IMPORT"),         // inside import statement
    UNDEFINED("UNDEFINED");
    
    private String id;

    CaretLocation(String id) {
        this.id = id;
    }
    
}

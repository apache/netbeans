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
package org.netbeans.modules.javascript2.types.api;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public interface Type {

    static String BOOLEAN = "Boolean";   //NOI18N
    static String NUMBER = "Number";     //NOI18N
    static String STRING = "String";     //NOI18N
    static String ARRAY = "Array";       //NOI18N

    static String REGEXP = "RegExp";     //NOI18N
    static String FUNCTION = "Function"; //NOI18N

    /**
     * When the type is unknown / we are not able to resolve it
     */
    static String UNRESOLVED = "unresolved"; //NOI18N
    static String UNDEFINED = "undefined";   //NOI18N
    static String NULL = "null";            //NOI18N
    static String OBJECT = "Object";        //NOI18N
    static String NAN = "NaN";              // NOI18N
    static String INFINITY = "Infinity";    // NOI18N
    
    String getType();

    int getOffset();
    
}

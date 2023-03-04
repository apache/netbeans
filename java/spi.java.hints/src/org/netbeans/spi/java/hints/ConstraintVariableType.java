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

package org.netbeans.spi.java.hints;

import java.lang.annotation.Target;

/**Specifies a type of a variable. During the matching process, only those
 * sections of the source code that have the given type are considered.
 *
 * @author Jan Lahoda
 */
@Target({})
public @interface ConstraintVariableType {

    /**Variable name, must start with the dollar sign (<code>$</code>).
     * Variable<code>$this</code> is automatically bound to the current class.
     */
    public String variable();

    /**The required type of the section of source code. The value must be a type
     * per JLS 4.1, i.e. a primitive type (JLS 4.2), or a reference type (JLS 4.3).
     * All elements of the type must be resolvable when written to any Java file,
     * they may not contain e.g. references to type variables, simple names, etc.
     *
     * The type may include any actual type arguments, including wildcard.
     *
     * While matching, the type of the tree that corresponds to the variable in
     * the actual occurrence candidate is accepted if it is assignable into the
     * variable defined by the attribute.
     */
    public String type();
    
}

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
package org.netbeans.api.debugger.jpda;

import java.io.InvalidObjectException;

/**
 * Represents a variable that can be modified.
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @see LocalVariable
 * @see Field
 * @see JPDAWatch
 *
 * @author Martin Entlicher
 * @since 2.44
 */
public interface MutableVariable extends Variable {
    
    /**
     * Sets a value represented as text, to this variable.
     *
     * @param value The string value to be set to this variable
     * @throws InvalidExpressionException if the expression is not correct
     */
    void setValue (String value) throws InvalidExpressionException;
    
    /**
     * Set the value of this variable to match the given mirror object.
     * 
     * @param obj The mirror object
     * @throws InvalidObjectException when it was not possible to set value of
     *                                this variable from the provided object.
     * @see Variable#createMirrorObject()
     */
    void setFromMirrorObject(Object obj) throws InvalidObjectException;
    
}

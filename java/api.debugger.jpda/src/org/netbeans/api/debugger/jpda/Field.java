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


/**
 * Represents one field. This interface is extended by {@link ObjectVariable}
 * interface, if the represented field contains not primitive value (object
 * value).
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @see ObjectVariable
 * @author   Jan Jancura
 */
public interface Field extends MutableVariable {

    /**
     * Declared name of field.
     *
     * @return name of this field.
     */
    public abstract String getName ();

    /**
     * Returns name of enclosing class.
     *
     * @return name of enclosing class
     */
    public abstract String getClassName ();
    
    /**
     * Get the class type in which this field was declared.
     * @return the class type
     */
    public abstract JPDAClassType getDeclaringClass();

    /**
     * Declared type of this field.
     *
     * @return declared type of this field
     */
    public abstract String getDeclaredType ();

    /**
     * Returns <code>true</code> for static fields.
     *
     * @return <code>true</code> for static fields
     */
    public abstract boolean isStatic ();

    /**
     * Sets value of this field represented as text.
     *
     * @return sets value of this field represented as text
     * @throws InvalidExpressionException if the expression is not correct
     */
    @Override
    public abstract void setValue (String value) 
    throws InvalidExpressionException;
}

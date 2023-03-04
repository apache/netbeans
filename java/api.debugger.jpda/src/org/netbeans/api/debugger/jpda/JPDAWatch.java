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
 * Represents watch in JPDA debugger.
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * It's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @author   Jan Jancura
 */

public interface JPDAWatch extends MutableVariable {

    /**
     * Watched expression.
     *
     * @return watched expression
     */
    public abstract String getExpression ();

    /**
     * Sets watched expression.
     *
     * @param expression a expression to be watched
     */
    public abstract void setExpression (String expression);
    
    /**
     * Remove the watch from the list of all watches in the system.
     */
    public abstract void remove ();

    /**
     * Declared type of this local.
     *
     * @return declared type of this local
     */
    public abstract String getType ();

    /**
     * Text representation of current value of this local.
     *
     * @return text representation of current value of this local
     */
    public abstract String getValue ();

    /**
     * Returns description of problem is this watch can not be evaluated
     * in current context.
     *
     * @return description of problem
     */
    public abstract String getExceptionDescription ();
    
    /**
     * Sets value of this local represented as text.
     *
     * @param value a new value of this variable represented as text
     */
    @Override
    public abstract void setValue (String value) throws InvalidExpressionException;

    /**
     * Calls {@link java.lang.Object#toString} in debugged JVM and returns
     * its value.
     *
     * @return toString () value of this instance
     */
    public abstract String getToStringValue () throws InvalidExpressionException;
}


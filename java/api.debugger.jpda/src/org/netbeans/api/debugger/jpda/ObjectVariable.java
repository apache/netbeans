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

import java.util.List;


/**
 * Represents instance of some object in debugged JVM. This interface can
 * be optionally implemented by a implementation of {@link LocalVariable} or
 * {@link Field} interfaces.
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @see LocalVariable
 * @see Field
 * @see This
 * @see Super
 * @see JPDAThread#getContendedMonitor
 * @see JPDAThread#getOwnedMonitors
 *
 * @author   Jan Jancura
 */
public interface ObjectVariable extends Variable {

    /**
     * Calls {@link java.lang.Object#toString} in debugged JVM and returns
     * its value.
     *
     * @return toString () value of this instance
     * @throws InvalidExpressionException in case of execution problems
     */
    public abstract String getToStringValue () throws InvalidExpressionException;
    
    /**
     * Calls given method in debugged JVM on this instance and returns
     * its value.
     *
     * @param methodName a name of method to be called
     * @param signature a signature of method to be called
     * @param arguments arguments to be used
     *
     * @return value of given method call on this instance
     * @throws NoSuchMethodException when the method does not exist
     * @throws InvalidExpressionException in case of execution problems
     */
    public abstract Variable invokeMethod (
        String methodName,
        String signature,
        Variable[] arguments
    ) throws NoSuchMethodException, InvalidExpressionException;

    /**
     * Number of fields defined in this object.
     *
     * @return number of fields defined in this object
     */
    public abstract int getFieldsCount ();

    /**
     * Returns field defined in this object.
     *
     * @param name a name of field to be returned
     *
     * @return field defined in this object
     */
    public abstract Field getField (String name);

    /**
     * Returns non static fields defined in this object.
     *
     * @param from the index of first field to be returned
     * @param to the index of last field, exclusive
     *
     * @return fields defined in this object that are greater then or equal to
     * <code>from</code> index and less then <code>to</code> index.
     */
    public abstract Field[] getFields (int from, int to);

    /**
     * Return all static fields.
     *
     * @return all static fields
     */
    public abstract Field[] getAllStaticFields (int from, int to);

    /**
     * Return all inherited fields.
     *
     * @return all inherited fields
     */
    public abstract Field[] getInheritedFields (int from, int to);
    
    /**
     * Returns variables that directly reference this variable.
     * Use {@link JPDADebugger#canGetInstanceInfo} to determine if this operation is supported.
     * @param maxReferrers The maximum number of referring variables to return. Must be non-negative. If zero, all referring variables are returned.
     * @return A list of referring variables.
     */
    List<ObjectVariable> getReferringObjects(long maxReferrers) throws UnsupportedOperationException;
    
    /**
     * Returns representation of super class of this object.
     *
     * @return representation of super class of this object
     */
    public abstract Super getSuper ();
    
    /**
     * Get the run-time class type of this object variable.
     * @return The variable class type.
     */
    JPDAClassType getClassType();
    
    /**
     * Returns a unique identifier for this object variable.
     * It is guaranteed to be unique among all object variables from the same debuggee that have not yet been disposed.
     * @return a long unique ID
     */
    long getUniqueID();
}

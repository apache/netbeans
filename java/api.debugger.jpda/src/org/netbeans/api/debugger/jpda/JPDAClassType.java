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

import com.sun.jdi.AbsentInformationException;

import java.util.List;

/**
 * Represents type of an object (class, interface, array) in the debugged process.
 * 
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @since 2.7
 *
 * @author Martin Entlicher
 */
public interface JPDAClassType extends VariableType {
    
    /**
     * Get the source name of this type.
     * @return the source file name of this type.
     */
    String getSourceName() throws AbsentInformationException;
    
    /**
     * Returns the class object variable, that corresponds to this type in the target VM.
     * @return the class object variable.
     * @throws UnsupportedOperationException when not supported by target VM.
     */
    ClassVariable classObject();
    
    /**
     * Gets the classloader object which loaded the class corresponding to this type.
     * @return an object variable representing the classloader, or <code>null</code>
     *         if the class was loaded through the bootstrap class loader.
     */
    ObjectVariable getClassLoader();
    
    /**
     * Gets the superclass of this class.
     * @return the superclass of this class in the debuggee, or <code>null</code>
     *         if no such class exists.
     */
    Super getSuperClass();
    
    /**
     * Get the currently loaded subclasses, when this type represents a class,
     * or loaded subinterfaces and implementors of this interface, if this type
     * represents an interface.
     * @return a list of direct classes and interfaces, that extend this type.
     *         Returns an empty array when there are no such types.
     * @since 3.2
     */
    List<JPDAClassType> getSubClasses();

    /**
     * Get all interfaces that are directly or indirectly implemented by this class,
     * or extended by this interface.
     * @return a list of all implemented or extended interfaces.
     *         Returns an empty array when there are no such interfaces.
     * @since 3.2
     */
    List<JPDAClassType> getAllInterfaces();

    /**
     * Get the interfaces that are directly implemented by this class,
     * or directly extended by this interface.
     * @return a list of all implemented or extended interfaces.
     *         Returns an empty array when there are no such interfaces.
     * @since 3.2
     */
    List<JPDAClassType> getDirectInterfaces();

    /**
     * Check if this type in an instance of a given class name.
     * @param className the class name
     * @return <code>true</code> when this type is an instance of the given class
     * name, <code>false</code> otherwise.
     * @since 3.2
     */
    boolean isInstanceOf(String className);

    /**
     * Provide a list of static fields declared in this type.
     * @return the list of {@link org.netbeans.api.debugger.jpda.Field} objects
     *         representing static fields.
     */
    List<Field> staticFields();
    
    /**
     * Calls given static method in debugged JVM on this class and returns
     * its value.
     *
     * @param methodName a name of method to be called
     * @param signature a signature of method to be called
     * @param arguments arguments to be used
     *
     * @return value of given method call on this instance
     * @throws NoSuchMethodException when the method does not exist
     * @throws InvalidExpressionException in case of execution problems
     * @since 2.47
     */
    public abstract Variable invokeMethod (
        String methodName,
        String signature,
        Variable[] arguments
    ) throws NoSuchMethodException, InvalidExpressionException;

    /**
     * Retrieves the number of instances this class.
     * Use {@link JPDADebugger#canGetInstanceInfo} to determine if this operation is supported.
     * @return the number of instances.
     */
    long getInstanceCount() throws UnsupportedOperationException;
    
    /**
     * Returns instances of this class type. Only instances that are reachable
     * for the purposes of garbage collection are returned.
     * Use {@link JPDADebugger#canGetInstanceInfo} to determine if this operation is supported.
     * @param maxInstances the maximum number of instances to return. Must be non-negative. If zero, all instances are returned.
     * @return a List of object variables.
     */
    List<ObjectVariable> getInstances(long maxInstances) throws UnsupportedOperationException;
    
}

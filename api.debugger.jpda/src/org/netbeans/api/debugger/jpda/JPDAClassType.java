/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

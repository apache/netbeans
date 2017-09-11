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

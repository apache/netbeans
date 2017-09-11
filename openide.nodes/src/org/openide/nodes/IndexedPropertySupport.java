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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.nodes;

import org.openide.util.Utilities;

import java.beans.Beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Support for indexed properties.
*
* @author Jan Jancura
 * @param T type of the whole property
 * @param E type of one element
*/
public class IndexedPropertySupport<T,E> extends Node.IndexedProperty<T,E> {
    /** Instance of the bean. */
    protected Object instance;

    /** setter method */
    private Method setter;

    /** getter method */
    private Method getter;

    /** indexed setter method */
    private Method indexedSetter;

    /** indexed getter method */
    private Method indexedGetter;

    /** Constructor.
    * @param instance the bean for which these properties exist
    * @param valueType type of the entire property
    * @param elementType type of one element of the property
    * @param getter get method for the entire property
    * @param setter set method for the entire property
    * @param indexedGetter get method for one element
    * @param indexedSetter set method for one element
    */
    public IndexedPropertySupport(
        Object instance, Class<T> valueType, Class<E> elementType, Method getter, Method setter, Method indexedGetter,
        Method indexedSetter
    ) {
        super(valueType, elementType);
        this.instance = instance;
        this.setter = setter;
        this.getter = getter;
        this.indexedSetter = indexedSetter;
        this.indexedGetter = indexedGetter;
    }

    /* Setter for display name.
    * @param s the string
    */
    public final void setDisplayName(String s) {
        super.setDisplayName(s);
    }

    /* Setter for name.
    * @param s the string
    */
    public final void setName(String s) {
        super.setName(s);
    }

    /* Setter for short description.
    * @param s the string
    */
    public final void setShortDescription(String s) {
        super.setShortDescription(s);
    }

    /* Can read the value of the property.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    public boolean canRead() {
        return getter != null;
    }

    /* Getter for the value.
    * @return the value of the property
    * @exception IllegalAccessException cannot access the called method
    * @exception IllegalArgumentException wrong argument
    * @exception InvocationTargetException an exception during invocation
    */
    public T getValue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (!canRead()) {
            throw new IllegalAccessException();
        }

        Object validInstance = Beans.getInstanceOf(instance, getter.getDeclaringClass());

        return PropertySupport.cast(getValueType(), getter.invoke(validInstance));
    }

    /* Can write the value of the property.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    public boolean canWrite() {
        return setter != null;
    }

    /* Setter for the value.
    * @param val the value of the property
    * @exception IllegalAccessException cannot access the called method
    * @exception IllegalArgumentException wrong argument
    * @exception InvocationTargetException an exception during invocation
    */
    public void setValue(T val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (!canWrite()) {
            throw new IllegalAccessException();
        }

        Object validInstance = Beans.getInstanceOf(instance, setter.getDeclaringClass());

        Object value = val;
        if (
            (val != null) && (setter.getParameterTypes()[0].getComponentType().isPrimitive()) &&
                (!val.getClass().getComponentType().isPrimitive())
        ) {
            value = Utilities.toPrimitiveArray((Object[]) val);
        }

        setter.invoke(validInstance, value);
    }

    /* Can read the indexed value of the property.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    public boolean canIndexedRead() {
        return indexedGetter != null;
    }

    /* Getter for the indexed value.
    * @return the value of the property
    * @exception IllegalAccessException cannot access the called method
    * @exception IllegalArgumentException wrong argument
    * @exception InvocationTargetException an exception during invocation
    */
    public E getIndexedValue(int index)
    throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (!canIndexedRead()) {
            throw new IllegalAccessException();
        }

        Object validInstance = Beans.getInstanceOf(instance, indexedGetter.getDeclaringClass());

        return PropertySupport.cast(getElementType(), indexedGetter.invoke(validInstance, index));
    }

    /* Can write the indexed value of the property.
    * @return <CODE>true</CODE> if the read of the value is supported
    */
    public boolean canIndexedWrite() {
        return indexedSetter != null;
    }

    /* Setter for the indexed value.
    * @param val the value of the property
    * @exception IllegalAccessException cannot access the called method
    * @exception IllegalArgumentException wrong argument
    * @exception InvocationTargetException an exception during invocation
    */
    public void setIndexedValue(int index, E val)
    throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (!canIndexedWrite()) {
            throw new IllegalAccessException();
        }

        Object validInstance = Beans.getInstanceOf(instance, indexedSetter.getDeclaringClass());
        indexedSetter.invoke(validInstance, new Object[] { new Integer(index), val });
    }
}

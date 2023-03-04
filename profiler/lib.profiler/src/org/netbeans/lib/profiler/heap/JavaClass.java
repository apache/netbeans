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

package org.netbeans.lib.profiler.heap;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * This class represents java.lang.Class instances on the heap.
 * @author Tomas Hurka
 */
public interface JavaClass extends Type {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Returns a value object that reflects the specified static field of the class
     * represented by this {@link JavaClass} object.
     * The name parameter is a String that specifies the simple name of the desired static field.
     * <br>
     * Speed: normal
     * @param name the name of the field
     * @return the value for the specified static field in this class.
     * If a static field with the specified name is not found <CODE>null</CODE> is returned.
     * If the field.getType() is {@link Type} object {@link Instance} is returned as field value,
     * for primitive types its corresponding object wrapper (Boolean, Integer, Float, etc.) is returned.
     */
    public Object getValueOfStaticField(String name);

    /**
     * returns the size of all instances in bytes. For non array classes
     * this is computed as getInstancesCount()*getInstanceSize().
     * Instance size of all instances is summed for arrays.
     * <br>
     * Speed: normal
     * @return the size of all instances in bytes
     */
    long getAllInstancesSize();

    /**
     * returns true if this JavaClass represents array (for example: java.lang.String[]).
     * <br>
     * Speed: fast
     * @return <CODE>true</CODE> if this class represents array, <CODE>false</CODE> otherwise
     */
    boolean isArray();

    /**
     * returns {@link Instance} representing class loader of this class.
     * <br>
     * Speed: fast
     * @return class loader
     */
    Instance getClassLoader();

    /**
     * returns List of instance fields of this {@link JavaClass}.
     * <br>
     * Speed: normal
     * @return list of {@link Field} instance fields
     */
    List /*<Field>*/ getFields();

    /**
     * returns the size of the {@link Instance} in bytes if this JavaClass does
     * not represent array. For arrays -1 is returned
     * <br>
     * Speed: fast
     * @return returns the size of the {@link Instance} in bytes, for arrays -1 is returned.
     */
    int getInstanceSize();

    /**
     * computes the list of all {@link Instance} of this class.
     * The instances are ordered according to {@link Instance#getInstanceNumber()}
     * <br>
     * Speed: slow
     * @return list {@link Instance} of instances
     */
    List /*<Instance>*/ getInstances();

   /**
     * returns an iterator over the {@link Instance}es of this {@link JavaClass}. 
     * The instances are ordered according to {@link Instance#getInstanceNumber()}.
     * <br>
     * Speed: fast
     *
     * @return an <tt>Iterator</tt> over the {@link Instance}es of this {@link JavaClass}.
     */
    public Iterator /*<Instance>*/ getInstancesIterator();

    /**
     * gets number of instances of this class.
     * <br>
     * Speed: first invocation is slow, all subsequent invocations are fast
     * @return number of instances
     */
    int getInstancesCount();

    /**
     * gets sum of retained sizes of all class instances.
     * <br>
     * Speed: first invocation is slow, all subsequent invocations are fast
     * @return sum of retained sizes of all class instances
     */
    long getRetainedSizeByClass();

    /**
     * gets unique (in whole heap) ID of this {@link JavaClass}.
     * <br>
     * Speed: fast
     * @return ID of this {@link JavaClass}
     */
    long getJavaClassId();

    /**
     * return human readable name of the class.
     * Innerclasses are separated by $. Anonymous classes uses $1 $2 etc.
     * Arrays uses [] after the class or primitive type name
     * <br>
     * Speed: fast
     * @return name of the class
     */
    String getName();

    /**
     * Returns a {@link Field} object that reflects the specified field of the class
     * represented by this {@link JavaClass} object.
     * The name parameter is a String that specifies the simple name of the desired field.
     * <br>
     * Speed: normal
     * @param name the name of the field
     * @return the {@link Field} object for the specified field in this class.
     * If a field with the specified name is not found <CODE>null</CODE> is returned.
     */

    /*    public Field getField(String name); */
    /**
     * computes the list of instance field values. The order is fields of this class followed by
     * super class, etc.
     * <br>
     * Speed: normal
     * @return list of {@link FieldValue} instance field values.
     */
    List /*<FieldValue>*/ getStaticFieldValues();

    /**
     * returns all subclasses of this class. This method works recursively
     * so it returns also subclasses of subclasses etc.
     * <br>
     * Speed: slow
     * @return returns the {@link Collection} of {@link JavaClass}.
     */
    Collection /*<JavaClass>*/ getSubClasses();

    /**
     * returns {@link JavaClass} representing super class of this class.
     * <br>
     * Speed: fast
     * @return super class (super class of java.lang.Object is null)
     */
    JavaClass getSuperClass();
}

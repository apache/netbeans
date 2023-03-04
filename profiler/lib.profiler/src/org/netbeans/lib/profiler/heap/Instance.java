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

import java.util.List;


/**
 * This object represents one instance of java class.
 * @author Tomas Hurka
 */
public interface Instance {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * computes the list of instance field values. The order is: fields of this class followed by
     * super class, etc.
     * <br>
     * Speed: normal
     * @return list of {@link FieldValue} instance field values.
     */
    List /*<FieldValue>*/ getFieldValues();

    /**
     * returns <CODE>true</CODE> if this is instance of GC root.
     * <br>
     * Speed: normal for first invocation, fast for subsequent
     * @return <CODE>true</CODE> if this is instance of GC root, <CODE>false</CODE> otherwise.
     */
    boolean isGCRoot();

    /**
     * gets unique (in whole heap) ID of this {@link Instance}.
     * <br>
     * Speed: fast
     * @return ID of this {@link Instance}
     */
    long getInstanceId();

    /**
     * gets unique number of this {@link Instance} among all instances of the same Java Class.
     * Instances are numbered sequentially starting from 1.
     * <br>
     * Speed: fast
     * @return unique number of this {@link Instance}
     */
    int getInstanceNumber();

    /**
     * returns corresponding {@link JavaClass} for this instance.
     * <br>
     * Speed: fast
     * @return {@link JavaClass} of this instance.
     */
    JavaClass getJavaClass();

    /**
     * returns next {@link Instance} on the path to the nearest GC root.
     * <br>
     * Speed: first invocation is slow, all subsequent invocations are fast
     * @return next {@link Instance} on the path to the nearest GC root, itself if the instance is GC root,
     * <CODE>null</CODE> if path to the nearest GC root does not exist
     */
    Instance getNearestGCRootPointer();

    long getReachableSize();

    /**
     * returns the list of references to this instance. The references can be of two kinds.
     * The first one is from {@link ObjectFieldValue} and the second one if from {@link ArrayItemValue}
     * <br>
     * Speed: first invocation is slow, all subsequent invocations are fast
     * @return list of {@link Value} representing all references to this instance
     */
    List /*<Value>*/ getReferences();

    long getRetainedSize();

    /**
     * returns the size of the {@link Instance} in bytes. If the instance is not
     * {@link PrimitiveArrayInstance} or {@link ObjectArrayInstance} this size is
     * equal to <CODE>getJavaClass().getInstanceSize()</CODE>.
     * <br>
     * Speed: fast
     * @return size of this {@link Instance}
     */
    long getSize();

    /**
     * returns the list of static field values.
     * This is delegated to {@link JavaClass#getStaticFieldValues()}
     * <br>
     * Speed: normal
     * @return list of {@link FieldValue} static field values.
     */
    List /*<FieldValue>*/ getStaticFieldValues();

    /**
     * Returns a value object that reflects the specified field of the instance
     * represented by this {@link Instance} object. Fields are searched from the java.lang.Object.
     * The first field with the matching name is used.
     * The name parameter is a String that specifies the simple name of the desired field.
     * <br>
     * Speed: normal
     * @param name the name of the field
     * @return the value for the specified static field in this class.
     * If a field with the specified name is not found <CODE>null</CODE> is returned.
     * If the field.getType() is {@link Type} object {@link Instance} is returned as a field value,
     * for primitive types its corresponding object wrapper (Boolean, Integer, Float, etc.) is returned.
     */
    Object getValueOfField(String name);
}

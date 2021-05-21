/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.util.enum;

import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * The class that presents specifiED (in constructor) array
 * as an Enumeration.
 * @deprecated JDK 1.5 treats enum as a keyword so this class was
 *             replaced by {@link org.openide.util.Enumerations#array}.
 * @author Ian Formanek
 */
public class ArrayEnumeration implements Enumeration {
    /** The array */
    private Object[] array;

    /** Current index in the array */
    private int index = 0;

    /** Constructs a new ArrayEnumeration for specified array */
    public ArrayEnumeration(Object[] array) {
        this.array = array;
    }

    /** Tests if this enumeration contains more elements.
    * @return  <code>true</code> if this enumeration contains more elements;
    *          <code>false</code> otherwise.
    */
    public boolean hasMoreElements() {
        return (index < array.length);
    }

    /** Returns the next element of this enumeration.
    * @return     the next element of this enumeration.
    * @exception  NoSuchElementException  if no more elements exist.
    */
    public Object nextElement() {
        try {
            return array[index++];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NoSuchElementException();
        }
    }
}

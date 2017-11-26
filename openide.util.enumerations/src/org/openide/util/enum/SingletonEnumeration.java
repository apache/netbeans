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
 * The class that encapsulates one object into one element enumeration.
 * @deprecated JDK 1.5 treats enum as a keyword so this class was
 *             replaced by {@link org.openide.util.Enumerations#singleton}.
 * @author Jaroslav Tulach
 */
public class SingletonEnumeration implements Enumeration {
    /** object to return */
    private Object object;

    /** @param object object to be put into the enumeration
    */
    public SingletonEnumeration(Object object) {
        this.object = object;
    }

    /** Tests if this enumeration contains next element.
    * @return  <code>true</code> if this enumeration contains it
    *          <code>false</code> otherwise.
    */
    public boolean hasMoreElements() {
        return object != null;
    }

    /** Returns the next element of this enumeration.
    * @return     the next element of this enumeration.
    * @exception  NoSuchElementException  if no more elements exist.
    */
    public synchronized Object nextElement() {
        if (object == null) {
            throw new NoSuchElementException();
        } else {
            Object o = object;
            object = null;

            return o;
        }
    }
}

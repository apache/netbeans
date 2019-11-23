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


/**
 * Abstract class that takes an enumeration and alter their elements
 * to new objects.
 * To get this class fully work one must override <CODE>alter</CODE> method.
 * Objects in the input and resulting enumeration must not be <CODE>null</CODE>.
 * @deprecated JDK 1.5 treats enum as a keyword so this class was
 *             replaced by {@link org.openide.util.Enumerations#convert}.
 * @author Jaroslav Tulach
 */
public abstract class AlterEnumeration extends Object implements Enumeration {
    /** enumeration to filter */
    private Enumeration en;

    /**
    * @param en enumeration to filter
    */
    public AlterEnumeration(Enumeration en) {
        this.en = en;
    }

    /** Alters objects. Overwrite this to alter the object in the
    * enumeration by another.
    * @param o the object to decide on
    * @return new object to be placed into the output enumeration
    */
    protected abstract Object alter(Object o);

    /** @return true if there is more elements in the enumeration
    */
    public boolean hasMoreElements() {
        return en.hasMoreElements();
    }

    /** @return next object in the enumeration
    * @exception NoSuchElementException can be thrown if there is no next object
    *   in the enumeration
    */
    public Object nextElement() {
        return alter(en.nextElement());
    }
}

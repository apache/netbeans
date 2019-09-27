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
 * Abstract class that takes an enumeration and filters its elements.
 * To get this class fully work one must override <CODE>accept</CODE> method.
 * Objects in the enumeration must not be <CODE>null</CODE>.
 * @deprecated JDK 1.5 treats enum as a keyword so this class was
 *             replaced by {@link org.openide.util.Enumerations#filter}.
 * @author Jaroslav Tulach
 */
public class FilterEnumeration extends Object implements Enumeration {
    /** marker object stating there is no nexte element prepared */
    private static final Object EMPTY = new Object();

    /** enumeration to filter */
    private Enumeration en;

    /** element to be returned next time or {@link #EMPTY} if there is
    * no such element prepared */
    private Object next = EMPTY;

    /**
    * @param en enumeration to filter
    */
    public FilterEnumeration(Enumeration en) {
        this.en = en;
    }

    /** Filters objects. Overwrite this to decide which objects should be
    * included in enumeration and which not.
    * <P>
    * Default implementation accepts all non-null objects
    *
    * @param o the object to decide on
    * @return true if it should be in enumeration and false if it should not
    */
    protected boolean accept(Object o) {
        return o != null;
    }

    /** @return true if there is more elements in the enumeration
    */
    public boolean hasMoreElements() {
        if (next != EMPTY) {
            // there is a object already prepared
            return true;
        }

        while (en.hasMoreElements()) {
            // read next
            next = en.nextElement();

            if (accept(next)) {
                // if the object is accepted
                return true;
            }

            ;
        }

        next = EMPTY;

        return false;
    }

    /** @return next object in the enumeration
    * @exception NoSuchElementException can be thrown if there is no next object
    *   in the enumeration
    */
    public Object nextElement() {
        if ((next == EMPTY) && !hasMoreElements()) {
            throw new NoSuchElementException();
        }

        Object res = next;
        next = EMPTY;

        return res;
    }
}

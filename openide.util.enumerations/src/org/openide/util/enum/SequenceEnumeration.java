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
 * Composes several enumerations into one.
 * @deprecated JDK 1.5 treats enum as a keyword so this class was
 *             replaced by {@link org.openide.util.Enumerations#concat}.
 * @author Jaroslav Tulach, Petr Nejedly
 */
public class SequenceEnumeration extends Object implements Enumeration {
    /** enumeration of Enumerations */
    private Enumeration en;

    /** current enumeration */
    private Enumeration current;

    /** is {@link #current} up-to-date and has more elements?
    * The combination <CODE>current == null</CODE> and
    * <CODE>checked == true means there are no more elements
    * in this enumeration.
    */
    private boolean checked = false;

    /** Constructs new enumeration from already existing. The elements
    * of <CODE>en</CODE> should be also enumerations. The resulting
    * enumeration contains elements of such enumerations.
    *
    * @param en enumeration of Enumerations that should be sequenced
    */
    public SequenceEnumeration(Enumeration en) {
        this.en = en;
    }

    /** Composes two enumerations into one.
    * @param first first enumeration
    * @param second second enumeration
    */
    public SequenceEnumeration(Enumeration first, Enumeration second) {
        this(new ArrayEnumeration(new Enumeration[] { first, second }));
    }

    /** Ensures that current enumeration is set. If there aren't more
    * elements in the Enumerations, sets the field <CODE>current</CODE> to null.
    */
    private void ensureCurrent() {
        while ((current == null) || !current.hasMoreElements()) {
            if (en.hasMoreElements()) {
                current = (Enumeration) en.nextElement();
            } else {
                // no next valid enumeration
                current = null;

                return;
            }
        }
    }

    /** @return true if we have more elements */
    public boolean hasMoreElements() {
        if (!checked) {
            ensureCurrent();
            checked = true;
        }

        return current != null;
    }

    /** @return next element
    * @exception NoSuchElementException if there is no next element
    */
    public synchronized Object nextElement() {
        if (!checked) {
            ensureCurrent();
        }

        if (current != null) {
            checked = false;

            return current.nextElement();
        } else {
            checked = true;
            throw new java.util.NoSuchElementException();
        }
    }
}

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
import java.util.HashSet;


/**
 * Enumeration that scans through another one and removes duplicates.
 * Two objects are duplicate if <CODE>one.equals (another)</CODE>.
 * @deprecated JDK 1.5 treats enum as a keyword so this class was
 *             replaced by {@link org.openide.util.Enumerations#removeDuplicates}.
 * @author Jaroslav Tulach
 */
public class RemoveDuplicatesEnumeration extends FilterEnumeration {
    /** hashtable with all returned objects */
    private HashSet all = new HashSet(37);

    /**
    * @param en enumeration to filter
    */
    public RemoveDuplicatesEnumeration(Enumeration en) {
        super(en);
    }

    /** Filters objects. Overwrite this to decide which objects should be
    * included in enumeration and which not.
    * @param o the object to decide on
    * @return true if it should be in enumeration and false if it should not
    */
    protected boolean accept(Object o) {
        return all.add(o);
    }
}

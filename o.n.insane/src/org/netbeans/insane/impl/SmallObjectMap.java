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

package org.netbeans.insane.impl;

import java.lang.reflect.*;
import java.util.*;

import org.netbeans.insane.scanner.*;


/** This is a special kind of IdentityHashSet.
 * It hashes the objects according to their system hash code
 * and in case of colision keeps wrappers to provide unique IDs
 */
class SmallObjectMap implements ObjectMap {
    // the primary table keeping all the known objects
    // hashed using their system hashcodes, with circular-linear shift
    // in case of bucket collision.
    private Object[] table = new Object[128*1024];
    private int size;

    // this map keeps reference to all objects with system hash collision
    private Map<Object,Integer> wrappers = new IdentityHashMap<Object,Integer>();

    int idCounter;

    int maxDisplace;

    SmallObjectMap() {}

    public boolean isKnown(Object o) {
        int bucket = System.identityHashCode(o) % table.length;

        while (table[bucket] != null) {
            if (table[bucket] == o) return true;

            bucket = (bucket + 1) % table.length;
        }

        return false;
    }


    public String getID(Object o) {
        // find whether it is known and wrapped
        Integer wid = wrappers.get(o);
        if (wid != null) return getWrappedId(o, wid.intValue());

        // ... or at least known
        if (isKnown(o)) return getNormalId(o);

        // unknown object
        if (putObject(o)) { //wrapped
            return getWrappedId(o, wrappers.get(o).intValue());
        } else {
             return getNormalId(o);
        }
    }

    private static String getWrappedId(Object o, int i) {
        return Integer.toHexString(System.identityHashCode(o)) + '.' + Integer.toHexString(i);
    }

    private static String getNormalId(Object o) {
        return Integer.toHexString(System.identityHashCode(o));
    }

    // knows it is not there.
    // returns true iff wraps
    private boolean putObject(Object o) {
        if (5*size/4 > table.length) rehash(3*table.length/2);

        size++;
        int sysID = System.identityHashCode(o);
        int bucket = sysID % table.length;
        boolean wrap = false;

        int temp = 0;
        // find an empty slot, look for friends with the same ID
        while (table[bucket] != null) {
            if (System.identityHashCode(table[bucket]) == sysID) wrap = true;
            temp++;
            bucket = (bucket + 1) % table.length;
        }
        if (temp > maxDisplace) maxDisplace = temp;

        // fill the slot
        table[bucket] = o;

        // add the wrapping info
        if (wrap) wrappers.put(o, new Integer(idCounter++));
        return wrap;
    }

    private void rehash(int newSize) {
        Object[] newTable = new Object[newSize];
        for (int i=0; i<table.length; i++) {
            Object act = table[i];
            if (act != null) {
                int bucket = System.identityHashCode(act) % newTable.length;
                int temp=0;
                while (newTable[bucket] != null) { // find an empty slot
                    temp++;
                    bucket = (bucket + 1) % newTable.length;
                }
                if (temp > maxDisplace) maxDisplace = temp;

                newTable[bucket] = act;
            }
        }

        table = newTable;
    }
}

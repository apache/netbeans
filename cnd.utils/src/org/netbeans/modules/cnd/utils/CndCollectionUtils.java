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

package org.netbeans.modules.cnd.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public final class CndCollectionUtils {


    private CndCollectionUtils() {
    }
    
    public static int hashCode(Collection<?> col) {
        int hash = 0;
        if (col != null) {
            hash = 5;
            for (Object o : col) {
                hash = 67 * hash + Objects.hashCode(o);
            }
        }
        return hash;
    }

    public static int hashCode(Map<?, ?> thisCol) {
        int hash = 5;
        for (Object p : thisCol.keySet()) {
            hash ^= Objects.hashCode(p);
        }
        return hash;
    }
    
    public static boolean equals(Map<?, ?> thisCol, Map<?, ?> otherCol) {
        if (thisCol == otherCol) {
            return true;
        }
        if ((thisCol == null) != (otherCol == null)) {
            return false;
        }
        if (thisCol != null && otherCol != null) {
            if (thisCol.size() != otherCol.size()) {
                return false;
            }
            for (Map.Entry<?, ?> entry : thisCol.entrySet()) {
                Object otherVal = otherCol.get(entry.getKey());
                if (!Objects.equals(entry.getValue(), otherVal)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean equals(List<?> thisCol, List<?> otherCol) {
        if (thisCol == otherCol) {
            return true;
        }
        if ((thisCol == null) != (otherCol == null)) {
            return false;
        }
        if (thisCol != null && otherCol != null) {
            if (thisCol.size() != otherCol.size()) {
                return false;
            }
            for (int i = 0; i < thisCol.size(); i++) {
                if (!Objects.equals(thisCol.get(i), otherCol.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean equals(Iterable<?> thisCol, Iterable<?> otherCol) {
        if (thisCol == otherCol) {
            return true;
        }
        if ((thisCol == null) != (otherCol == null)) {
            return false;
        }
        if (thisCol != null && otherCol != null) {
            Iterator<?> itThis = thisCol.iterator();
            Iterator<?> itOther = otherCol.iterator();
            while (itThis.hasNext()) {
                if (!itOther.hasNext()) {
                    return false;
                }
                if (!Objects.equals(itThis.next(), itOther.next())) {
                    return false;
                }
            }
            if (itOther.hasNext()) {
                return false;
            }
        }
        return true;
    }
}

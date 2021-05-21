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

package org.netbeans.insane.live;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.netbeans.insane.impl.Root;
import org.netbeans.insane.impl.Utils;

/**
 * The representation of the path from GC root to given object.
 * Forms a linked list, where each node represents one Object, next Path node
 * in the reference chain and the outgoing reference from the node to the next.
 *
 * Root node might have no associated object, while the last node has neither
 * outgoing reference nor next node.
 *
 * @author nenik
 */
public final class Path {
    static {
        Utils.PATH_FACTORY = new Utils.PathFactory() {
            public Path createPath(Object item, Path next) {
                return new Path(item, next);
            }
        };
    }
    
    private Object item;
    private Path nextElement;

    /** Internal use only! Will be removed/made private.
     */
    private Path(Object item, Path next) {
        this.item = item;
        this.nextElement = next;
    }

    /**
     * Get the Object instance this Path node represents.
     * @return Object for this path node or null in case of root node.
     */
    public Object getObject() {
        return item;
    }
    
    /**
     * Get the next node in the Path chain.
     * @return Next Path node or null in case of the last node.
     */
    public Path nextNode() {
        return nextElement;
    }
    
    /**
     * Get the textual representation of the reference between this node
     * and the next one.
     * @return Name of the field, array index or root description.
     */
    public String describeReference() {
        return getField();
    }
    

    /**
     * Provides an object description in the form of "ClassName@systemHash".
     * @return e.g. "java.lang.String@82acb368"
     */
    public static String describeObject(Object obj) {
        if (obj == null) {
            // http://deadlock.netbeans.org/job/NB-Core-Build/1393/testReport/org.netbeans.modules.projectapi/AuxiliaryConfigBasedPreferencesProviderTest/testReclaimable/
            // java.lang.NullPointerException
            //         at org.netbeans.insane.live.Path.describeObject(Path.java:109)
            //         at org.netbeans.insane.live.Path.toString(Path.java:119)
            //         at org.netbeans.insane.live.Path.toString(Path.java:121)
            //         at java.lang.String.valueOf(String.java:2615)
            //         at java.lang.StringBuilder.append(StringBuilder.java:116)
            //         at org.netbeans.junit.NbTestCase.findRefsFromRoot(NbTestCase.java:1354)
            //         at org.netbeans.junit.NbTestCase.assertGC(NbTestCase.java:1226)
            //         at org.netbeans.junit.NbTestCase.assertGC(NbTestCase.java:1171)
            //         at org.netbeans.modules.projectapi.AuxiliaryConfigBasedPreferencesProviderTest.testReclaimable(AuxiliaryConfigBasedPreferencesProviderTest.java:256)
            return "null";
        } else if (obj instanceof Class) {
            return obj.toString() + "@" + Integer.toHexString(System.identityHashCode(obj));
        }
        return obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
    }


    /**
     * Provides a formatted textual representation of the whole reference chain
     * up to the last referenced object, including newlines between path nodes.
     */
    public String toString() {
        if (nextElement == null) {
            return describeObject(item);
        } else if (item instanceof Root) {
            return ((Root)item).describe() + "->\n" + nextElement.toString();
        } else {
            return describeObject(item) + "-" + getField() + "->\n" + nextElement.toString();
        }
    }

    private String getField() {
        Object target = nextElement.item;
        Class cls = item.getClass();

        if (cls.isArray()) {
            assert !cls.getComponentType().isPrimitive();

            // find array offset
            Object[] arr = (Object[])item;
            for (int i=0; i<arr.length; i++) {
                if (arr[i] == target) return "[" + i + "]";
            }
            return "<changed>";
        }

        // Check all fields
        while (cls != null) { // go over the class hierarchy
            Field[] flds = cls.getDeclaredFields();
            for (int i=0; i<flds.length; i++) {
                try {
                    Field act = flds[i];

                    if (act.getType().isPrimitive() || (act.getModifiers() & Modifier.STATIC) != 0) continue;
                    act.setAccessible(true);
                    if (target == act.get(item)) return act.getName();
                } catch (Exception e) {
                    return "<error>";
                };
            }
            cls = cls.getSuperclass();
        }

        return "<changed>";
    }

    public int hashCode() {
        return System.identityHashCode(item);
    }

    public boolean equals(Object o) {
        return (o instanceof Path) && (((Path)o).item == item);
    }
}

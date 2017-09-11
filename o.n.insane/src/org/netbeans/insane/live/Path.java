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

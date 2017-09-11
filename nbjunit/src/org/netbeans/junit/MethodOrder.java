/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.junit;

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/** Support for sorting or shuffling NbTestCase test methods.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class MethodOrder {
    private static final List<Method[]> allDeclaredMethods = new LinkedList<Method[]>();
    private static final String DEFAULT_METHOD_ORDER = "natural"; // NOI18N
    private static Long shuffleSeed;
    
    
    static void initialize() {
        String orderS = findOrder();
        if (!"natural".equals(orderS)) { // NOI18N
            try {
                Field classesF = ClassLoader.class.getDeclaredField("classes"); // NOI18N
                classesF.setAccessible(true);
                @SuppressWarnings("unchecked")
                Collection<Class<?>> classes = new ArrayList<Class<?>>((Collection<Class<?>>) classesF.get(NbTestCase.class.getClassLoader()));
                for (Class<?> c : classes) {
                    if (NbTestCase.class.isAssignableFrom(c) && c != NbTestCase.class) {
                        orderMethods(c, orderS);
                    }
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }
    static void orderMethods(Class<?> c, String orderS) throws Exception { // #7023180
        if (orderS == null) {
            orderS = findOrder();
        }
        if ("natural".equals(orderS)) { // NOI18N
            return;
        }
        Method[] ms = null;
        try {
            Field declaredMethodsF = Class.class.getDeclaredField("declaredMethods");
            declaredMethodsF.setAccessible(true);
            while (ms == null) {
                c.getDeclaredMethods();
                ms = (Method[]) ((Reference) declaredMethodsF.get(c)).get();
            }
        } catch (NoSuchFieldException ex) {
            // try JDK8
            Field rdF = Class.class.getDeclaredField("reflectionData");
            rdF.setAccessible(true);
            c.getDeclaredMethods();
            Reference<Object> ref = (Reference<Object>) rdF.get(c);
            Object refData = ref.get();
            Field dmF = refData.getClass().getDeclaredField("declaredMethods");
            dmF.setAccessible(true);
            ms = (Method[]) dmF.get(refData);
        }
        allDeclaredMethods.add(ms); // prevent GC
        if (orderS.equals("a-z")) {
            abcSort(ms, true);
        } else if (orderS.equals("z-a")) {
            abcSort(ms, false);
        } else if (orderS.equals("shuffle")) {
            if (shuffleSeed == null) {
                shuffleSeed = System.currentTimeMillis();
            }
            shuffle(ms, shuffleSeed);
        } else {
            try {
                long seed = Long.parseLong(orderS);
                shuffle(ms, seed);
            } catch (NumberFormatException ex) {
                throw new Exception("Specify -DNbTestCase.order=a-z or =z-a or =shuffle or =<number>");
            }
        }
    }

    private static void abcSort(Method[] ms, boolean asscending) {
        final int multi = asscending ? 1 : -1;
        Arrays.sort(ms, new Comparator<Method>() {
            @Override public int compare(Method m1, Method m2) {
                return multi * m1.toString().compareTo(m2.toString());
            }
        });
    }

    private static void shuffle(Method[] arr, long seed) {
        abcSort(arr, true);
        Random r = new Random(seed);
        for (int i = 0; i < arr.length; i++) {
            int from = i + r.nextInt(arr.length - i);
            if (i != from) {
                Method m = arr[i];
                arr[i] = arr[from];
                arr[from] = m;
            }
        }
    }

    private static String findOrder() {
        String orderS = System.getProperty("NbTestCase.order");
        if (orderS == null) {
            orderS = DEFAULT_METHOD_ORDER;
        }
        return orderS;
    }
    
    static boolean isShuffled() {
        return shuffleSeed != null;
    }

    static long getSeed() {
        assert shuffleSeed != null;
        return shuffleSeed;
    }
}

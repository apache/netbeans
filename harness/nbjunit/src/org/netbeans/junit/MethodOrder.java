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
                // TODO: ClassLoader fields can not be accessed via reflection on JDK 12+
                // see jdk.internal.reflect.Reflection#fieldFilterMap
                // this won't work
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
                System.err.println("WARNING: test method ordering disabled");
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

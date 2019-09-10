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
package org.netbeans.lib.profiler.server.system;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.management.openmbean.CompositeData;

/**
 *
 * @author Tomas Hurka
 */
public class ThreadDump {

    private static final String[][] methods = new String[][]{
        {"sun.management.ThreadInfoCompositeData", "toCompositeData"}, // NOI18N Sun JVM
        {"com.ibm.lang.management.ManagementUtils", "toThreadInfoCompositeData"} // NOI18N IBM J9
    };

    private static Method toCompositeDataMethod;
    private static ThreadMXBean threadBean;
    private static boolean runningOnJdk15;

    public static void initialize(boolean jdk15) {
        runningOnJdk15 = jdk15;
        threadBean = ManagementFactory.getThreadMXBean();
        for (String[] method : methods) {
            String className = method[0];
            String methodName = method[1];
            try {
                Class<?> clazz = Class.forName(className);
                toCompositeDataMethod = clazz.getMethod(methodName, ThreadInfo.class);
                if (toCompositeDataMethod != null) {
                    break;
                }
            } catch (ClassNotFoundException ex) {
            } catch (NoSuchMethodException ex) {
            } catch (SecurityException ex) {
            }
        }
    }

    public static Object[] takeThreadDump() {
        ThreadInfo[] threads = (runningOnJdk15) ? takeThreadDump15() : takeThreadDump16();
        List compositeData = new ArrayList(threads.length);

        for (int i = 0; i < threads.length; i++) {
            ThreadInfo ti = threads[i];
            if (ti != null) {
                compositeData.add(toCompositeData(ti));
            }
        }
        return compositeData.toArray(new CompositeData[0]);
    }

    public static boolean isJDK15() {
        return runningOnJdk15;
    }
    
    private static CompositeData toCompositeData(ThreadInfo tinfo) {
        try {
            return (CompositeData) toCompositeDataMethod.invoke(null, tinfo);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static ThreadInfo[] takeThreadDump15() {
        long[] tids = threadBean.getAllThreadIds();
        return threadBean.getThreadInfo(tids, Integer.MAX_VALUE);
    }

    private static ThreadInfo[] takeThreadDump16() {
        return threadBean.dumpAllThreads(true, true);
    }

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import javax.management.JMRuntimeException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;


/**
 * Provides methods for obtaining various high-resolution system times.
 * A version for JDK 1.5
 *
 * @author  Misha Dmitriev
 */
public class Timers {
    
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static MBeanServerConnection conn;
    private static final String PROCESS_CPU_TIME_ATTR = "ProcessCpuTime"; // NOI18N
    private static final String PROCESSING_CAPACITY_ATTR = "ProcessingCapacity"; // NOI18N
    private static ObjectName osName;
    private static long processCPUTimeMultiplier;
    private static int processorsCount;
    private static boolean processCPUTimeAttribute;
    private static boolean initialized;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * "counts" instead of nanoseconds in this method are for compatibility with the previous
     * versions of JFluid, that call a native method for system timer, which, in turn, returns
     * the result in sub-microsecond "counts" on Windows.
     */
    public static long getCurrentTimeInCounts() {
        return System.nanoTime();
    }

    public static long getNoOfCountsInSecond() {
        return 1000000000;
    }

    public static native long getThreadCPUTimeInNanos();

    /**
     * Returns the approximate accumulated process CPU elapsed time
     * in nanoseconds. Note that the time is normalized to one processor.
     * This method returns <tt>-1</tt> if the collection
     * elapsed time is undefined for this collector.
     *
     * @return the approximate accumulated process CPU elapsed time
     * in nanoseconds.
     */
    public static long getProcessCpuTime() {
        initializeProcessCPUTime();
        if (processCPUTimeAttribute) {
             try {
                 Long cputime = (Long)conn.getAttribute(osName,PROCESS_CPU_TIME_ATTR);

                 return (cputime.longValue()*processCPUTimeMultiplier)/processorsCount;
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
        }
        return -1;
    }

    /**
     * This is relevant only on Solaris. By default, the resolution of the thread local CPU timer is 10 ms. If we enable
     * micro state accounting, it enables significantly (but possibly at a price of some overhead). So I turn it on only
     * when thread CPU timestamps are really collected.
     */
    public static native void enableMicrostateAccounting(boolean v);

    /** Should be called at earliest possible time */
    public static void initialize() {
        ManagementFactory.getThreadMXBean();
        getThreadCPUTimeInNanos();
        initializeProcessCPUTime();
    }

    private static void initializeProcessCPUTime() {
        if (initialized) {
            return;
        }

        initialized = true;
        try {
            MBeanAttributeInfo[] attrs;
            
            conn = ManagementFactory.getPlatformMBeanServer();
            osName = new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
            attrs = conn.getMBeanInfo(osName).getAttributes();
            processCPUTimeMultiplier = 1;
            processorsCount = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
            for (int i = 0; i < attrs.length; i++) {
                String name = attrs[i].getName();
                
                if (PROCESS_CPU_TIME_ATTR.equals(name)) {
                    processCPUTimeAttribute = true;
                }
                if (PROCESSING_CAPACITY_ATTR.equals(name)) {
                    Number mul = (Number) conn.getAttribute(osName,PROCESSING_CAPACITY_ATTR);
                    processCPUTimeMultiplier = mul.longValue();
                }
            }
        } catch (JMRuntimeException ex) {
            // Glassfish: if ManagementFactory.getPlatformMBeanServer() is called too early it will throw JMRuntimeException
            // in such case initialization will be rerun later as part of getProcessCpuTime()
            System.err.println(ex.getLocalizedMessage());
            initialized = false;       
            return;   
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * WORKS ONLY ON UNIX, calls nanosleep(). On Solaris, this is more precise than the built-in Thread.sleep() call
     * implementation that, at least in JDK 1.4.2, goes to select(3C). On Linux, it should be more precise, but it
     * turns out that nanosleep() in this OS, at least in version 7.3 that I tested, has a resolution of at least 20ms.
     * This seems to be a known issue; hopefully they fix it in future.
     */
    public static native void osSleep(int ns);
}

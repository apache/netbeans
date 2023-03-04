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
import javax.management.InstanceNotFoundException;
import javax.management.JMRuntimeException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;


/**
 *
 * @author Tomas Hurka
 */
public class HeapDump {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static MBeanServer mserver;
    private static ObjectName hotspotDiag;
    private static boolean initialized;
    private static boolean runningOnJdk15;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    private HeapDump() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static void initialize(boolean jdk15) {
        runningOnJdk15 = jdk15;
        if (runningOnJdk15) {
            initialize15();
        } else {
            initialize16();
        }
    }

    public static String takeHeapDump(String outputFile) {
        if (runningOnJdk15) {
            return takeHeapDump15(outputFile);
        }

        return takeHeapDump16(outputFile);
    }

    private static native void initialize15();

    private static void initialize16() {
        if (initialized) {
            return;
        }

        initialized = true;

        try {
            mserver = ManagementFactory.getPlatformMBeanServer();
        } catch (JMRuntimeException ex) {
            // Glassfish: if ManagementFactory.getPlatformMBeanServer() is called too early it will throw JMRuntimeException
            // in such case initialization will be rerun later as part of takeHeapDump()
            System.err.println(ex.getLocalizedMessage());
            initialized = false;

            return;
        }

        try {
            hotspotDiag = new ObjectName("com.sun.management:type=HotSpotDiagnostic");   // NOI18N
            mserver.getObjectInstance(hotspotDiag);
        } catch (MalformedObjectNameException ex) {
            ex.printStackTrace();
        } catch (InstanceNotFoundException ex) {
            System.err.println("Heap Dump is not available"); // NOI18N
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private static String takeHeapDump15(String outputFile) {
        int error = -1;

        try {
            error = takeHeapDump15Native(outputFile);
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        }

        if (error == -1) {
            return "Take heap dump is not available."; // NOI18N
        }

        return null;
    }

    private static native int takeHeapDump15Native(String outputFile);

    private static String takeHeapDump16(String outputFile) {
        String error = null;
        initialize16();

        if ((mserver == null) || (hotspotDiag == null)) {
            return "Take heap dump is not available."; // NOI18N
        }

        try {
            mserver.invoke(hotspotDiag, "dumpHeap", new Object[] {outputFile, true}, new String[] {String.class.getName(), boolean.class.getName()} );  // NOI18N
        } catch (IllegalArgumentException ex) {
            error = ex.getLocalizedMessage();
        } catch (InstanceNotFoundException ex) {
            error = ex.getLocalizedMessage();
        } catch (MBeanException ex) {
            error = ex.getLocalizedMessage();
        } catch (ReflectionException ex) {
            error = ex.getLocalizedMessage();
        }

        return error;
    }
}

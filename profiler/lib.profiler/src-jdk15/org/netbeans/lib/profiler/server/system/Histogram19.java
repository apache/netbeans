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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 *
 * @author Tomas Hurka
 */
class Histogram19 {
    private static final String DIAGNOSTIC_COMMAND_MXBEAN_NAME =
            "com.sun.management:type=DiagnosticCommand";    // NOI18N
    private static final String ALL_OBJECTS_OPTION = "-all";    // NOI18N
    private static final String HISTOGRAM_COMMAND = "gcClassHistogram";       // NOI18N
    private static MBeanServer mserver;
    private static ObjectName hotspotDiag;

    static boolean initialize() {
        boolean initok = false;
        try {
            mserver = ManagementFactory.getPlatformMBeanServer();
            hotspotDiag = new ObjectName(DIAGNOSTIC_COMMAND_MXBEAN_NAME);
            mserver.getObjectInstance(hotspotDiag);
            initok = true;
        } catch (MalformedObjectNameException ex) {
            ex.printStackTrace();
        } catch (InstanceNotFoundException ex) {
            System.err.println("Heap Histogram is not available"); // NOI18N
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        return initok;
    }

    static InputStream getRawHistogram() {
        try {
            Object histo = mserver.invoke(hotspotDiag,
                    HISTOGRAM_COMMAND,
                    new Object[] {new String[] {ALL_OBJECTS_OPTION}},
                    new String[] {String[].class.getName()}
            );
            if (histo instanceof String) {
                return new ByteArrayInputStream(((String)histo).getBytes(StandardCharsets.UTF_8));
            }
        } catch (InstanceNotFoundException ex) {
            ex.printStackTrace();
        } catch (MBeanException ex) {
            ex.printStackTrace();
        } catch (ReflectionException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
}

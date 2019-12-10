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

package org.netbeans.modules.extexecution.process.jdk9;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.netbeans.spi.extexecution.base.ProcessesImplementation;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author phejl
 */
@ServiceProvider(service=ProcessesImplementation.class, position = 1000)
public class ProcessesImpl implements ProcessesImplementation {

    private static final Logger LOGGER = Logger.getLogger(ProcessesImpl.class.getName());

    private static final boolean ENABLED;

    private static final Method PROCESS_TO_HANDLE;

    private static final Method PROCESS_HANDLE_DESCENDANTS;

    private static final Method PROCESS_HANDLE_DESTROY;

    static {
        Method toHandle = null;
        Method descendants = null;
        Method destroy = null;
        try {
            toHandle = Process.class.getDeclaredMethod("toHandle", new Class[]{}); // NOI18N
            if (toHandle != null) {
                Class processHandle = Class.forName("java.lang.ProcessHandle"); // NOI18N
                descendants = processHandle.getDeclaredMethod("descendants", new Class[]{}); // NOI18N
                destroy = processHandle.getDeclaredMethod("destroy", new Class[]{}); // NOI18N
            }
        } catch (NoClassDefFoundError | Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }

        ENABLED = toHandle != null && descendants != null && destroy != null;
        PROCESS_TO_HANDLE = toHandle;
        PROCESS_HANDLE_DESCENDANTS = descendants;
        PROCESS_HANDLE_DESTROY = destroy;
    }

    @Override
    public void killTree(Process process, Map<String, String> environment) {
        if (!ENABLED) {
            throw new UnsupportedOperationException("The JDK 9 way of killing process tree is not supported"); // NOI18N
        }

        try {
            Object handle = PROCESS_TO_HANDLE.invoke(process, (Object[]) null);
            try (Stream<?> s = (Stream<?>) PROCESS_HANDLE_DESCENDANTS.invoke(handle, (Object[]) null)) {
                destroy(handle);
                s.forEach(ch -> destroy(ch));
            }
        } catch (IllegalAccessException | IllegalArgumentException |InvocationTargetException ex) {
            throw new UnsupportedOperationException("The JDK 9 way of killing process tree has failed", ex); // NOI18N
        }
    }

    private static void destroy(Object handle) {
        try {
            PROCESS_HANDLE_DESTROY.invoke(handle, (Object[]) null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

}

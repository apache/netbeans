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
package org.netbeans.modules.profiler.heapwalk.details.jdk;

import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Hurka
 */
@ServiceProvider(service=DetailsProvider.class)
public class LangDetailsProvider extends DetailsProvider.Basic {
    private static final String ENUM_MASK = "java.lang.Enum+";                    // NOI18N
    private static final String STACKTRACE_MASK = "java.lang.StackTraceElement";    // NOI18N
    
    public LangDetailsProvider() {
        super(ENUM_MASK, STACKTRACE_MASK);
    }
    
    public String getDetailsString(String className, Instance instance, Heap heap) {
        if (ENUM_MASK.equals(className)) {                                      // Enum+
            String name = DetailsUtils.getInstanceFieldString(instance, "name", heap); // NOI18N
            int ordinal = DetailsUtils.getIntFieldValue(instance, "ordinal", -1); // NOI18N
            if (name != null) {
                if (ordinal != -1) {
                    return name+" ("+ordinal+")";       // NOI18N
                }
                return name;
            }
        } else if (STACKTRACE_MASK.equals(className)) {                         // StackTraceElement
            String declaringClass = DetailsUtils.getInstanceFieldString(instance, "declaringClass", heap); // NOI18N
            if (declaringClass != null) {
                String methodName = DetailsUtils.getInstanceFieldString(instance, "methodName", heap); // NOI18N
                String fileName = DetailsUtils.getInstanceFieldString(instance, "fileName", heap); // NOI18N
                int lineNumber = DetailsUtils.getIntFieldValue(instance, "lineNumber", -1); // NOi18N                
                if (methodName == null) methodName = "Unknown method";   // NOI18N
                StackTraceElement ste = new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
                return ste.toString();
            }
        }
        
        return null;
    }
    
}

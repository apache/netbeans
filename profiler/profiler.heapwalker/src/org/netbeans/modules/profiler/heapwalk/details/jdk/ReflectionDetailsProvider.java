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
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider(service=DetailsProvider.class)
public class ReflectionDetailsProvider extends DetailsProvider.Basic {
    
    private static final String CLASS_MASK = "java.lang.Class";                     // NOI18N
    private static final String CONSTRUCTOR_MASK = "java.lang.reflect.Constructor"; // NOI18N
    private static final String METHOD_MASK = "java.lang.reflect.Method";           // NOI18N
    private static final String FIELD_MASK = "java.lang.reflect.Field";             // NOI18N
    private static final String PARAMETER_MASK = "java.lang.reflect.Parameter";     // NOI18N
    
    public ReflectionDetailsProvider() {
        super(CLASS_MASK,CONSTRUCTOR_MASK, METHOD_MASK, FIELD_MASK, PARAMETER_MASK);
    }
    
    public String getDetailsString(String className, Instance instance, Heap heap) {
        if (CLASS_MASK.equals(className)) {                                     // Class
            String name = DetailsUtils.getInstanceFieldString(instance, "name", heap); // NOI18N
            if (name == null && CLASS_MASK.equals(instance.getJavaClass().getName())) {
                JavaClass jclass = heap.getJavaClassByID(instance.getInstanceId());
                if (jclass != null) name = BrowserUtils.getSimpleType(jclass.getName());
//                if (jclass != null) name = jclass.getName();
            }
            return name;
        } else if (CONSTRUCTOR_MASK.equals(className)) {                        // Constructor
            Object value = instance.getValueOfField("clazz");                   // NOI18N
            if (value instanceof Instance) return getDetailsString("java.lang.Class", (Instance)value, heap); // NOI18N
        } else if (METHOD_MASK.equals(className)) {                             // Method
            return DetailsUtils.getInstanceFieldString(instance, "name", heap); // NOI18N
        } else if (FIELD_MASK.equals(className)) {                              // Field
            return DetailsUtils.getInstanceFieldString(instance, "name", heap); // NOI18N
        } else if (PARAMETER_MASK.equals(className)) {                          // Parameter
            return DetailsUtils.getInstanceFieldString(instance, "name", heap); // NOI18N
        }
        return null;
    }
    
}

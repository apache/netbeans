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

import java.lang.ref.Reference;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider(service=DetailsProvider.class)
public final class ReferenceDetailsProvider extends DetailsProvider.Basic {
    
    public ReferenceDetailsProvider() {
        super(Reference.class.getName() + "+");                                 // NOI18N
    }
    
    public String getDetailsString(String className, Instance instance, Heap heap) {
        Object value = instance.getValueOfField("referent");                    // NOI18N
        if (value instanceof Instance) {
            Instance i = (Instance)value;
            // TODO: can create cycle?
            String s = DetailsUtils.getInstanceString(i, heap);
            s = s == null ? "#" + i.getInstanceNumber() : ": " + s;             // NOI18N
            return BrowserUtils.getSimpleType(i.getJavaClass().getName()) + s;
        }
        return null;
    }
    
}

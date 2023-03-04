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

package org.netbeans.modules.debugger.jpda.heapwalk;

import org.netbeans.lib.profiler.heap.Field;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.Type;

/**
 *
 * @author Martin Entlicher
 */
public class FieldImpl implements Field {
    
    private org.netbeans.api.debugger.jpda.Field field;
    private HeapImpl heap;
    
    /** Creates a new instance of FieldImpl */
    public FieldImpl(HeapImpl heap, org.netbeans.api.debugger.jpda.Field field) {
        this.field = field;
        this.heap = heap;
    }

    @Override
    public JavaClass getDeclaringClass() {
        return new JavaClassImpl(heap, field.getDeclaringClass());
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public boolean isStatic() {
        return field.isStatic();
    }

    @Override
    public Type getType() {
        return new TypeImpl(field.getDeclaredType());
    }
    
}

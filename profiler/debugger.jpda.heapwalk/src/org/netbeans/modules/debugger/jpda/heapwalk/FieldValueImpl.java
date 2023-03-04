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
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Instance;

/**
 *
 * @author Martin Entlicher
 */
public class FieldValueImpl implements FieldValue {
    
    private org.netbeans.api.debugger.jpda.Field field;
    private Instance defInstance;
    private HeapImpl heap;
    private String value;
    
    /** Creates a new instance of FieldValueImpl */
    public FieldValueImpl(HeapImpl heap, Instance defInstance, org.netbeans.api.debugger.jpda.Field field) {
        this.field = field;
        this.defInstance = defInstance;
        this.heap = heap;
        // Preload the value, so that it's not retrieved in AWT.
        this.value = field.getValue();
    }

    @Override
    public Field getField() {
        return new FieldImpl(heap, field);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Instance getDefiningInstance() {
        return defInstance;
    }
    
}

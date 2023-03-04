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

import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.ObjectArrayInstance;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;

/**
 *
 * @author Martin Entlicher
 */
public class ObjectArrayInstanceImpl extends InstanceImpl implements ObjectArrayInstance {
    
    private ObjectVariable array;
    
    /** Creates a new instance of ObjectArrayInstanceImpl */
    public ObjectArrayInstanceImpl(HeapImpl heap, ObjectVariable array) {
        super(heap, array);
        this.array = array;
    }

    @Override
    public int getLength() {
        return array.getFieldsCount();
    }

    @Override
    public List<Instance> getValues() {
        Variable[] values = array.getFields(0, getLength());
        List<Instance> instances = new ArrayList<Instance>(values.length);
        for (Variable value: values) {
            Instance instance = InstanceImpl.createInstance(heap, (ObjectVariable) value);
            instances.add(instance);
        }
        return instances;
    }

    @Override
    public List getItems() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

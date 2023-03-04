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

import org.netbeans.lib.profiler.heap.ArrayItemValue;
import org.netbeans.lib.profiler.heap.Instance;

/**
 *
 * @author Martin Entlicher
 */
public class ArrayItemValueImpl implements ArrayItemValue {
    
    private Instance defInstance;
    private Instance instance;
    private int index;
    
    /** Creates a new instance of ArrayItemValueImpl */
    public ArrayItemValueImpl(Instance defInstance, Instance instance, int index) {
        this.defInstance = defInstance;
        this.instance = instance;
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    @Override
    public Instance getDefiningInstance() {
        return defInstance;
    }
    
}

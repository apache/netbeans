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

package org.netbeans.modules.profiler.heapwalk.memorylint;

import java.util.ArrayDeque;
import org.netbeans.lib.profiler.heap.Field;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.ObjectFieldValue;
import java.util.List;
import java.util.Queue;
import org.netbeans.lib.profiler.heap.ObjectArrayInstance;
import org.netbeans.lib.profiler.heap.Type;


/**
 * An utility class capable of walking the object graph and counting all
 * found objects.
 *
 * @param T the entry type, which can add additional properties
 * @author nenik
 */
public final class Walker {
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    private static final Type OBJECT = new Type() {
        public String getName() { return "object"; }
    };
    
    private static class ArrayEntryValue implements ObjectFieldValue, Field {
        int idx;
        private Instance src;
        private Instance target;

        public ArrayEntryValue(int idx, Instance src, Instance target) {
            this.idx = idx;
            this.src = src;
            this.target = target;
        }
        
        public Instance getInstance() {
            return target;
        }

        public Field getField() {
            return this;
        }

        public String getValue() {
            return "Instance #" + target.getInstanceId();
        }

        public Instance getDefiningInstance() {
            return src;
        }

        public JavaClass getDeclaringClass() {
            return src.getJavaClass(); // XXX
        }

        public String getName() {
            return "[" + idx + "]";
        }

        public boolean isStatic() {
            return false;
        }

        public Type getType() {
            return OBJECT;
        }
        
    }
    
    public static interface Filter {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public boolean accept(ObjectFieldValue val);
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Distribution log = new Distribution();

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public Walker() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Distribution getResults() {
        return log;
    }

    public void walk(Instance in) {
        walk(in, null);
    }

    public void walk(Instance in, Filter f) {
        Queue<Instance> q = new ArrayDeque<Instance>();
        q.add(in);

        log.add(in);

        while (!q.isEmpty()) {
            Instance act = q.poll();
            
            if (act instanceof ObjectArrayInstance) {
                List<Instance> out = ((ObjectArrayInstance)act).getValues();
                int i = 0;
                for (Instance target : out) {
                    if (target != null) {
                        if ((f == null || f.accept(new ArrayEntryValue(i, act, target))) && !log.isCounted(target)) {
                            log.add(target);
                            q.add(target);
                        }
                    }
                    i++;
                }
            }

            @SuppressWarnings("unchecked")
            List<FieldValue> out = (List<FieldValue>) act.getFieldValues();

            for (FieldValue fv : out) {
                if (fv instanceof ObjectFieldValue) {
                    ObjectFieldValue ofv = (ObjectFieldValue) fv;

                    if ((f != null) && !f.accept(ofv)) {
                        continue;
                    }

                    Instance target = ofv.getInstance();

                    if ((target != null) && !log.isCounted(target)) {
                        log.add(target);
                        q.add(target);
                    }
                }
            }
        }
    }
}

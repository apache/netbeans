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

package org.netbeans.modules.profiler.heapwalk.memorylint.rules;

import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;
import org.netbeans.modules.profiler.heapwalk.memorylint.*;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;


//@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.heapwalk.memorylint.Rule.class)
public class OverallocatedString extends IteratingRule {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private FieldAccess fldCount;
    private FieldAccess fldOffset;
    private FieldAccess fldValue;
    private JavaClass clsString;
    private Map<Instance, Integer> covered = new HashMap<Instance, Integer>();
    private int total;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public OverallocatedString() {
        super(NbBundle.getMessage(OverallocatedString.class, "LBL_OverStr_Name"),
                NbBundle.getMessage(OverallocatedString.class, "LBL_OverStr_Desc"),
                "java.lang.String"); // NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    @Override
    public String getHTMLDescription() {
        return NbBundle.getMessage(OverallocatedString.class, "LBL_OverStr_LongDesc");
    }

    protected void perform(Instance in) {
        int off = fldOffset.getIntValue(in);
        int cnt = fldCount.getIntValue(in);
        PrimitiveArrayInstance arrValue = (PrimitiveArrayInstance) fldValue.getRefValue(in);

        if (arrValue == null) {
            return; // empty
        }

        if ((off > 0) || (arrValue.getLength() > cnt)) {
            if (covered.containsKey(arrValue)) {
                // simplification - don't track shared char arrays
                total -= covered.remove(arrValue);
            } else {
                int waste = (2 * off) + (2 * (arrValue.getLength() - (cnt + off)));
                covered.put(arrValue, waste);
                total += waste;
            }
        }
    }

    protected void prepareRule(MemoryLint context) {
        Heap heap = context.getHeap();
        clsString = heap.getJavaClassByName("java.lang.String"); // NOI18N
        fldOffset = new FieldAccess(clsString, "offset"); // NOI18N
        fldCount = new FieldAccess(clsString, "count"); // NOI18N
        fldValue = new FieldAccess(clsString, "value"); // NOI18N
    }

    @Override
    protected void summary() {
        getContext().appendResults(NbBundle.getMessage(OverallocatedString.class, "FMT_OverStr_Result", total));

        Histogram<Histogram.Entry> h = new Histogram<Histogram.Entry>();

        for (Map.Entry<Instance, Integer> e : covered.entrySet()) {
            String incomming = getContext().getRootIncommingString(e.getKey());
            incomming = Utils.printClass(getContext(), incomming);
            h.add(incomming, new Histogram.Entry(e.getValue()));
        }

        getContext().appendResults(h.toString(5000));
    }
}

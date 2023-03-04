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
import org.netbeans.lib.profiler.heap.ObjectArrayInstance;
import org.netbeans.modules.profiler.heapwalk.memorylint.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.NbBundle;


//@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.heapwalk.memorylint.Rule.class)
public class HashMapHistogram extends IteratingRule {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class HashmapEntry extends Histogram.Entry<HashmapEntry> {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        int hmeCount;
        int strCount;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        HashmapEntry(long size, int hmeCount, int strCount) {
            super(size);
            this.hmeCount = hmeCount;
            this.strCount = strCount;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        public String toString() {
            return "#:" + getCount() + "/" + getSize() + "B, " + hmeCount + " HMEs, " + strCount + " Strings<br>"; // NOI18N
        }

        @Override
        protected void add(HashmapEntry source) {
            hmeCount += source.hmeCount;
            strCount += source.strCount;
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private FieldAccess fldHMEKey;
    private FieldAccess fldHMENext;
    private FieldAccess fldHMEValue;
    private FieldAccess fldHMTable;
    private FieldAccess fldSValue;
    private Histogram<HashmapEntry> byIncomming;
    private JavaClass clsHM;
    private JavaClass clsHME;
    private JavaClass clsString;
    private Set<Instance> known = new HashSet<Instance>();

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public HashMapHistogram() {
        super(NbBundle.getMessage(HashMapHistogram.class, "LBL_HMH_Name"),
                NbBundle.getMessage(HashMapHistogram.class, "LBL_HMH_Desc"),
                "java.util.HashMap"); // NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    @Override
    public String getHTMLDescription() {
        return NbBundle.getMessage(HashMapHistogram.class, "LBL_HMH_LongDesc");
    }

    protected void perform(Instance hm) {
        HashmapEntry he = sizeOfHashmap(hm);
        String incomming = getContext().getRootIncommingString(hm);
        incomming = Utils.printClass(getContext(), incomming);
        byIncomming.add(incomming, he);
    }

    protected @Override void prepareRule(MemoryLint context) {
        Heap heap = context.getHeap();
        clsString = heap.getJavaClassByName("java.lang.String"); // NOI18N
        clsHM = heap.getJavaClassByName("java.util.HashMap"); // NOI18N
        clsHME = heap.getJavaClassByName("java.util.HashMap$Entry"); // NOI18N
        fldSValue = new FieldAccess(clsString, "value"); // NOI18N
        fldHMTable = new FieldAccess(clsHM, "table"); // NOI18N
        fldHMEKey = new FieldAccess(clsHME, "key"); // NOI18N
        fldHMEValue = new FieldAccess(clsHME, "value"); // NOI18N
        fldHMENext = new FieldAccess(clsHME, "next"); // NOI18N
        byIncomming = new Histogram<HashmapEntry>();
    }

    protected @Override void summary() {
        getContext().appendResults(byIncomming.toString(50000));
    }

    private boolean add(Instance inst) {
        if (known.contains(inst)) {
            return false;
        }

        known.add(inst);

        return true;
    }

    private long sizeIfNewString(Instance obj) {
        if (obj == null) {
            return 0;
        }

        if ("java.lang.String".equals(obj.getJavaClass().getName())) { // NOI18N
            if (add(obj)) {
                long sz = obj.getSize();
                Instance arr = fldSValue.getRefValue(obj);

                if ((arr != null) && add(arr)) {
                    sz += arr.getSize();
                }

                return sz;
            }
        }

        return 0;
    }

    private HashmapEntry sizeOfHashmap(Instance hm) {
        ObjectArrayInstance table = (ObjectArrayInstance) fldHMTable.getRefValue(hm);
        long sum = hm.getSize() + table.getSize();
        int hmeCount = 0;
        int strCount = 0;

        List<Instance> tval = table.getValues();

        for (Instance entry : tval) {
            while (entry != null) {
                hmeCount++;
                sum += entry.getSize(); // size of entry

                long sz = sizeIfNewString(fldHMEKey.getRefValue(entry));

                if (sz != 0) {
                    strCount++;
                }

                sum += sz;
                sz = sizeIfNewString(fldHMEValue.getRefValue(entry));

                if (sz != 0) {
                    strCount++;
                }

                sum += sz;
                entry = fldHMENext.getRefValue(entry);
            }
        }

        HashmapEntry hme = new HashmapEntry(sum, hmeCount, strCount);

        return hme;
    }
}

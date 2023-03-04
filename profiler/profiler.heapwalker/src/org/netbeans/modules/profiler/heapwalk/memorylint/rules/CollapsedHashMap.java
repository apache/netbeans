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

@NbBundle.Messages({
    "LBL_CHM_Name=Collapsed (Weak)HashMaps",
    "LBL_CHM_Desc=HashMaps populated by entries with poorly distributed hashcode",
    "LBL_CHM_LongDesc=<html><body>This rule checks for (<code>Weak</code>)<code>" +
    "HashMap</code>s that have bad distribution of entries among allocated buckets," +
    "like in the illustration: <br><img src='res/wrongmap.png'><br> This can be " +
    "caused by bad implementation of <code>hashcode()</code> or " +
    "<code>equals()</code> methods of the objects used as map keys</body></html>",
    "FMT_CHM_Record={0}: {1} {2,number} entries are allocated to " +
    "{3,choice,1#'<b>one bucket</b>'|2#{3,number,integer} buckets}"
})
//@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.heapwalk.memorylint.Rule.class)
public class CollapsedHashMap extends IteratingRule {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class HMRecord {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Instance hm;
        private int size;
        private int slots;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        HMRecord(Instance hm, int size, int slots) {
            this.hm = hm;
            this.size = size;
            this.slots = slots;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        public String toString() {
            boolean reallyBad = slots == 1;
            return Bundle.FMT_CHM_Record(
                        Utils.printClass(getContext(), getContext().getRootIncommingString(hm)),
                        Utils.printInstance(hm),
                        size,
                        slots
                    );
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private FieldAccess fldHMSize;
    private FieldAccess fldHMTable;
    private FieldAccess fldWHMSize;
    private FieldAccess fldWHMTable;
    private JavaClass clsHM;
    private JavaClass clsWHM;
    private Set<HMRecord> poorHM = new HashSet<HMRecord>();

    /** Threshold for count of chained entries to raise the warning */
    private float ratio = 1.5f;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public CollapsedHashMap() {
        super(Bundle.LBL_CHM_Name(),
                Bundle.LBL_CHM_Desc(),
                "java.util.HashMap|java.util.WeakHashMap"); // NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    @Override
    public String getHTMLDescription() {
        return Bundle.LBL_CHM_LongDesc();
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    @Override
    protected void perform(Instance hm) {
        if (clsHM.equals(hm.getJavaClass())) {
            scanHashmap(hm, fldHMSize, fldHMTable);
        } else {
            scanHashmap(hm, fldWHMSize, fldWHMTable);
        }
    }

    @Override
    protected void prepareRule(MemoryLint context) {
        Heap heap = context.getHeap();
        clsHM = heap.getJavaClassByName("java.util.HashMap"); // NOI18N
        fldHMTable = new FieldAccess(clsHM, "table"); // NOI18N
        fldHMSize = new FieldAccess(clsHM, "size"); // NOI18N
        clsWHM = heap.getJavaClassByName("java.util.WeakHashMap"); // NOI18N
        fldWHMTable = new FieldAccess(clsWHM, "table"); // NOI18N
        fldWHMSize = new FieldAccess(clsWHM, "size"); // NOI18N
    }

    @Override
    protected void summary() {
        for (HMRecord hm : poorHM) {
            getContext().appendResults(hm.toString() + "<br>"); // NOI18N
        }
    }

    private void scanHashmap(Instance hm, FieldAccess sizeAccess, FieldAccess tableAccess) {
        int size = sizeAccess.getIntValue(hm);

        if (size < 5) {
            return; // not really significant
        }

        ObjectArrayInstance table = (ObjectArrayInstance) tableAccess.getRefValue(hm);

        if (table != null) {
            int slots = 0;
            @SuppressWarnings("unchecked")
            List<Instance> tval = table.getValues();

            for (Instance entry : tval) {
                if (entry != null) {
                    slots++;
                }
            }

            if (slots > 0 && (size / slots) > ratio) {
                poorHM.add(new HMRecord(hm, size, slots));
            }
        }

        return;
    }
}

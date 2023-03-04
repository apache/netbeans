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
public class WrongWeakHashMap extends IteratingRule {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class WHMRecord {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Instance hm;
        private Instance key;
        private Instance value;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        WHMRecord(Instance hm, Instance key, Instance value) {
            this.hm = hm;
            this.key = key;
            this.value = value;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        public String toString() {
            return NbBundle.getMessage(WrongWeakHashMap.class, "FMT_WWHM_Entry",
                    new Object[] {
                        Utils.printClass(getContext(), getContext().getRootIncommingString(hm)),
                        Utils.printInstance(hm),
                        Utils.printInstance(key),
                        Utils.printInstance(value)
                    }
            );
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private FieldAccess fldHMEKey;
    private FieldAccess fldHMENext;
    private FieldAccess fldHMEValue;
    private FieldAccess fldHMTable;
    private JavaClass clsHM;
    private JavaClass clsHME;
    private Set<WHMRecord> poorWHM = new HashSet<WHMRecord>();

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public WrongWeakHashMap() {
        super(NbBundle.getMessage(WrongWeakHashMap.class, "LBL_WWHM_Name"),
                NbBundle.getMessage(WrongWeakHashMap.class, "LBL_WWHM_Desc"),
                "java.util.WeakHashMap");
        
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    @Override
    public String getHTMLDescription() {
        return NbBundle.getMessage(WrongWeakHashMap.class, "LBL_WWHM_LongDesc");
    }

    protected void perform(Instance hm) {
        scanWeakHashmap(hm);
    }

    @Override
    protected void prepareRule(MemoryLint context) {
        // TODO WeakHashMap might not be present in the dump
        Heap heap = context.getHeap();
        clsHM = heap.getJavaClassByName("java.util.WeakHashMap"); // NOI18N
        clsHME = heap.getJavaClassByName("java.util.WeakHashMap$Entry"); // NOI18N
        fldHMTable = new FieldAccess(clsHM, "table"); // NOI18N

        JavaClass ref = heap.getJavaClassByName("java.lang.ref.Reference"); // NOI18N
        fldHMEKey = new FieldAccess(ref, "referent"); // NOI18N
        fldHMEValue = new FieldAccess(clsHME, "value"); // NOI18N
        fldHMENext = new FieldAccess(clsHME, "next"); // NOI18N
    }

    @Override
    protected void summary() {
        for (WHMRecord whm : poorWHM) {
            getContext().appendResults(whm.toString());
        }
    }

    private void scanWeakHashmap(Instance hm) {
        ObjectArrayInstance table = (ObjectArrayInstance) fldHMTable.getRefValue(hm);

        if (table == null) { // ? 

            return;
        }

        @SuppressWarnings("unchecked")
        List<Instance> tval = table.getValues();

        for (Instance entry : tval) {
            while (entry != null) {
                Instance key = fldHMEKey.getRefValue(entry);

                if (key != null) { // XXX can also scan for weak HM pending cleanup

                    Instance value = fldHMEValue.getRefValue(entry);

                    if (Utils.isReachableFrom(value, key)) {
                        poorWHM.add(new WHMRecord(hm, key, value));

                        return;
                    }
                }

                entry = fldHMENext.getRefValue(entry);
            }
        }

        return;
    }
}

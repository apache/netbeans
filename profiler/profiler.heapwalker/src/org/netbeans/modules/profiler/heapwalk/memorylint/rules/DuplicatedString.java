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
import org.netbeans.modules.profiler.heapwalk.memorylint.*;
import java.util.HashMap;
import org.openide.util.NbBundle;


//@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.heapwalk.memorylint.Rule.class)
public class DuplicatedString extends IteratingRule {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private FieldAccess fldValue;
    private HashMap<String, Integer> map = new HashMap<String, Integer>();
    private Histogram<Histogram.Entry> dupSources = new Histogram<Histogram.Entry>();
    private StringHelper helper;
    private int total;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public DuplicatedString() {
        super(NbBundle.getMessage(DuplicatedString.class, "LBL_DupStr_Name"),
                NbBundle.getMessage(DuplicatedString.class, "LBL_DupStr_Desc"),
                "java.lang.String"); // NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    @Override
    public String getHTMLDescription() {
        return NbBundle.getMessage(DuplicatedString.class, "LBL_DupStr_LongDesc");
    }

    protected void perform(Instance in) {
        String str = helper.decodeString(in);
        Integer val = map.get(str);

        if (val != null) { // already known, histogram the rest.

            long strSize = in.getSize();
            Instance arr = fldValue.getRefValue(in);

            if (arr != null) {
                strSize += ((str.length() * 2) + 14); // XXX aproximation
            }

            String incomming = getContext().getRootIncommingString(in);
            incomming = Utils.printClass(getContext(), incomming);
            dupSources.add(incomming, new Histogram.Entry(strSize));
            total += strSize;
        }

        val = (val == null) ? 1 : (val + 1);
        map.put(str, val);
    }

    protected @Override void prepareRule(MemoryLint context) {
        Heap heap = context.getHeap();
        helper = context.getStringHelper();

        JavaClass clsString = heap.getJavaClassByName("java.lang.String"); // NOI18N
        fldValue = new FieldAccess(clsString, "value"); // NOI18N
    }

    protected @Override void summary() {
        getContext().appendResults(NbBundle.getMessage(DuplicatedString.class, "FMT_DupStr_Result", total));
        getContext().appendResults(dupSources.toString(50000));
    }
}

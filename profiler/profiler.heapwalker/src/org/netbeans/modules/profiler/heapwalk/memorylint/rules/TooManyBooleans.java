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
import org.openide.util.NbBundle;


//@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.heapwalk.memorylint.Rule.class)
public class TooManyBooleans extends IteratingRule {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    Histogram<Histogram.Entry> booleans = new Histogram<Histogram.Entry>();
    private Heap heap;
    private Instance FALSE;
    private Instance TRUE;
    private StringHelper helper;
    private int count;
    private int total;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public TooManyBooleans() {
        super(NbBundle.getMessage(TooManyBooleans.class, "LBL_TMB_Name"),
                NbBundle.getMessage(TooManyBooleans.class, "LBL_TMB_Desc"),
                "java.lang.Boolean"); // NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    @Override
    public String getHTMLDescription() {
        return NbBundle.getMessage(TooManyBooleans.class, "LBL_TMB_LongDesc");
    }

    protected void perform(Instance in) {
        if (in.equals(TRUE) || in.equals(FALSE)) {
            return;
        }

        count++;
        booleans.add(Utils.printClass(getContext(), getContext().getRootIncommingString(in)), new Histogram.Entry(in.getSize()));
    }

    protected @Override void prepareRule(MemoryLint context) {
        heap = context.getHeap();
        helper = context.getStringHelper();

        JavaClass booleanClass = heap.getJavaClassByName("java.lang.Boolean"); // NOI18N
        TRUE = (Instance) booleanClass.getValueOfStaticField("TRUE"); // NOI18N
        FALSE = (Instance) booleanClass.getValueOfStaticField("FALSE"); // NOI18N
    }

    protected @Override void summary() {
        if (count > 0) {
            getContext().appendResults(
                    NbBundle.getMessage(TooManyBooleans.class, "FMT_TMB_Result", count+2, (count * TRUE.getSize())));
            getContext().appendResults(booleans.toString(0));
        } else {
            getContext().appendResults(NbBundle.getMessage(TooManyBooleans.class, "FMT_TMB_ResultOK"));
        }
    }
}

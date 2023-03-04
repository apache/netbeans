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
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

//import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import org.openide.util.NbBundle;


public class RetainedSetByClass extends Rule {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private MemoryLint context;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public RetainedSetByClass() {
        super(NbBundle.getMessage(RetainedSetByClass.class, "LBL_RSBC_Name"),
                NbBundle.getMessage(RetainedSetByClass.class, "LBL_RSBC_Desc"));
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    @Override
    public String getHTMLDescription() {
        return NbBundle.getMessage(RetainedSetByClass.class, "LBL_RSBC_LongDesc");
    }

    public void perform() {
        Heap heap = context.getHeap();
        @SuppressWarnings("unchecked")
        List<JavaClass> classes = heap.getAllClasses();

        // TODO access to progress
        //        BoundedRangeModel progress = context.getProgress();
        //        progress.setMaximum(classes.size());
        Histogram<Histogram.Entry> hist = new Histogram<Histogram.Entry>();

        for (JavaClass cls : classes) {
            Logger.getLogger(RetainedSetByClass.class.getName()).log(Level.FINE, "Executing rule on class {0}.", cls); // NOI18N
            performClass(cls, hist);

            if (context.isInterruped()) {
                return;
            }

            // TODO access to progress
            //            progress.setValue(progress.getValue()+1);
        }

        summary(hist);
    }

    @Override
    public void prepare(MemoryLint context) {
        this.context = context;
    }

    @Override
    protected JComponent createCustomizer() {
        return null;
    }

    @SuppressWarnings("unchecked")
    private void performClass(JavaClass clz, Histogram<Histogram.Entry> hist) {
        Set<Instance> retained = Utils.getRetainedSet(clz.getInstances(), context.getHeap());
        String name = clz.getName();
        name = Utils.printClass(context, name);

        for (Instance i : retained) {
            hist.add(name, new Histogram.Entry<Histogram.Entry>(i.getSize()));
        }
    }

    private void summary(Histogram h) {
//        context.appendResults("<hr>Histogram of retained size:<br>");
        context.appendResults(h.toString(0));
    }
}

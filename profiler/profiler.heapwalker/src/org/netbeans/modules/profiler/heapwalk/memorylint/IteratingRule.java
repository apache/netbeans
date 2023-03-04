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

import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;


/**
 * An iterating rule is a rule that iterates through all instances of single
 * class and does some analysis over each instance. When such a Rule is based
 * on this helper class, the infrastructure can independently monitor
 * the progress and also paralelize the task among available CPUs.
 *
 * Rules can override {@link #prepareRule(MemoryLint)} and {@link #summary()}
 * for preparation and finalization work, and must implement
 * {@link #perform(Instance)} for actual, per-instance analysis.
 *
 * @author nenik
 */
public abstract class IteratingRule extends Rule {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private MemoryLint context;
    private Pattern classNamePattern;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public IteratingRule(String name, String desc, String classNamePattern) {
        super(name, desc);
        setClassNamePattern(classNamePattern);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public final void perform() {
        Heap heap = context.getHeap();
        @SuppressWarnings("unchecked")
        List<JavaClass> classes = heap.getAllClasses();
        List<JavaClass> matching = new ArrayList<JavaClass>();
        int count = 0;

        for (JavaClass cls : classes) {
            if (classNamePattern.matcher(cls.getName()).matches()) {
                matching.add(cls);
                count += cls.getInstancesCount();
            }

            if (context.isInterruped()) {
                return;
            }
        }

        BoundedRangeModel progress = context.getProgress();
        progress.setMaximum((count != 0) ? count : 1);

        for (JavaClass actCls : matching) {
            @SuppressWarnings("unchecked")
            List<Instance> instances = actCls.getInstances();

            for (Instance inst : instances) {
                Logger.getLogger(IteratingRule.class.getName()).log(Level.FINE, "Executing rule on {0} instance", inst); // NOI18N
                perform(inst);
                progress.setValue(progress.getValue() + 1);

                if (context.isInterruped()) {
                    return;
                }
            }
        }

        if (count == 0) {
            progress.setValue(1);
        }

        summary();
    }

    public final void prepare(MemoryLint context) {
        this.context = context;
        prepareRule(context);
    }

    /** Configures the rule to be applied on all instances of classes
     * matching to given pattern.
     */
    protected final void setClassNamePattern(String classNamePattern) {
        this.classNamePattern = Pattern.compile(classNamePattern);
    }

    protected abstract void perform(Instance inst);

    protected final MemoryLint getContext() {
        return context;
    }

    /** Default implementation returns <code>null</code>
     * (no customizer for the rule).
     */
    protected JComponent createCustomizer() {
        return null;
    }

    protected void prepareRule(MemoryLint context) {
    }

    protected void summary() {
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.profiler.heap;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.SwingUtilities;


/**
 * @author Tomas Hurka
 */
public final class HeapProgress {
    
    public static final int PROGRESS_MAX = 1000;
    private static ThreadLocal progressThreadLocal = new ThreadLocal();
    
    private HeapProgress() {
        
    }
    
    public static BoundedRangeModel getProgress() {
        BoundedRangeModel model = (BoundedRangeModel) progressThreadLocal.get();
        
        if (model == null) {
            model = new DefaultBoundedRangeModel(0,0,0,PROGRESS_MAX);
            progressThreadLocal.set(model);
        }
        return model;
    }
    
    static void progress(long counter, long startOffset, long value, long endOffset) {
        // keep this method short so that it can be inlined 
        if (counter % 100000 == 0) {
            progress(value, endOffset, startOffset);
        }
    }

    static void progress(long value, long endValue) {
        progress(value,0,value,endValue);
    }
    
    private static void progress(final long value, final long endOffset, final long startOffset) {
        BoundedRangeModel model = (BoundedRangeModel) progressThreadLocal.get();
        if (model != null) {
            long val = PROGRESS_MAX*(value - startOffset)/(endOffset - startOffset);
            setValue(model, (int)val);
        }
    }
    
    static void progressFinish() {
        BoundedRangeModel model = (BoundedRangeModel) progressThreadLocal.get();
        if (model != null) {
            setValue(model, PROGRESS_MAX);
            progressThreadLocal.remove();
        }
    }
    
    private static void setValue(final BoundedRangeModel model, final int val) {
        if (SwingUtilities.isEventDispatchThread()) {
            model.setValue(val);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() { model.setValue(val); }
            });
        }
    }
}

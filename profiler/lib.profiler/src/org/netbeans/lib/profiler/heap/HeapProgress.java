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
    private static Progress.Listener listener;

    private HeapProgress() {
    }

    public static BoundedRangeModel getProgress() {
        registerListener();

        ModelInfo info = (ModelInfo) progressThreadLocal.get();
        if (info == null) {
            info = new ModelInfo();
            progressThreadLocal.set(info);
        }
        return info.model;
    }

    private static synchronized void registerListener() {
        if (listener == null) {
            listener = new Progress.Listener() {
                @Override
                public void started(Progress.Handle h) {
                    progressStart();
                }

                @Override
                public void progress(Progress.Handle h) {
                    HeapProgress.progress(h.getValue(), h.getEndOffset(), h.getStartOffset());
                }

                @Override
                public void finished(Progress.Handle h) {
                    progressFinish();
                }
            };
            Progress.register(listener);
        }
    }

    private static void progress(final long value, final long endOffset, final long startOffset) {
        ModelInfo info = (ModelInfo) progressThreadLocal.get();
        if (info != null) {
            if (info.level>info.divider) {
                info.divider = info.level;
            }
            long val = PROGRESS_MAX*(value - startOffset)/(endOffset - startOffset);
            int modelVal = (int) (info.offset + val/info.divider);
            setValue(info.model, modelVal);
        }
    }

    private static int levelAdd(ModelInfo info, int diff) {
        info.level+=diff;
        return info.level;
    }

    private static void progressStart() {
        ModelInfo info = (ModelInfo) progressThreadLocal.get();
        if (info != null) {
            levelAdd(info, 1);
        }
    }

    private static void progressFinish() {
        ModelInfo info = (ModelInfo) progressThreadLocal.get();
        if (info != null) {
            int level = levelAdd(info, -1);

            assert level >= 0;
            if (level == 0) {
                progressThreadLocal.remove();
            }
            info.offset = info.model.getValue();
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

    private static class ModelInfo {
        private BoundedRangeModel model;
        private int level;
        private int divider;
        private int offset;

        private ModelInfo() {
            model = new DefaultBoundedRangeModel(0,0,0,PROGRESS_MAX);
        }
    }
}

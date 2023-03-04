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
package org.netbeans.spi.debugger.ui;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.modules.debugger.ui.views.ViewModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.CompoundModel;

/**
 * Support class for a custom view based on registered view models.
 * 
 * @author Martin Entlicher
 * @since 2.34
 */
public final class ViewLifecycle {
    
    private ViewModelListener vml;
    private final CompoundModelUpdateListener cmul;

    ViewLifecycle(ViewModelListener vml, CompoundModelUpdateListener cmul) {
        this.vml = vml;
        this.cmul = cmul;
    }
    
    /**
     * Get the current compound model, that can be used to construct the custom
     * view.
     * @return The current compound model
     */
    public Models.CompoundModel getModel() {
        return cmul.getCurrentModel();
    }
    
    /**
     * Add a listener, which is called with the updated compound model.
     * @param mul The model update listener
     */
    public void addModelUpdateListener(ModelUpdateListener mul) {
        cmul.addModelUpdateListener(mul);
    }
    
    /**
     * Remove a model update listener
     * @param mul The model update listener
     */
    public void removeModelUpdateListener(ModelUpdateListener mul) {
        cmul.removeModelUpdateListener(mul);
    }

    /**
     * Destroy the underlying data, call this method when the view is closed.
     * Model updates will no longer be received after this method is called.
     */
    public void destroy() {
        vml.destroy();
    }


    /**
     * Model update listener, notified with updated compound model.
     */
    public static interface ModelUpdateListener {
        
        /**
         * Called when compound model is updated.
         * 
         * @param compoundModel The new compound model
         * @param de The associated debugger engine, whose models were used to create the
         * compound model. Can be <code>null</code>, when no active debugger engine
         * was found.
         */
        public void modelUpdated(Models.CompoundModel compoundModel, DebuggerEngine de);
        
    }
    
    static class CompoundModelUpdateListener implements ModelUpdateListener {
        
        private final List<ModelUpdateListener> muls = new LinkedList<ModelUpdateListener>();
        private CompoundModel currentCompoundModel;

        public void addModelUpdateListener(ModelUpdateListener mul) {
            synchronized(muls) {
                muls.add(mul);
            }
        }

        public void removeModelUpdateListener(ModelUpdateListener mul) {
            synchronized(muls) {
                muls.remove(mul);
            }
        }
        
        public CompoundModel getCurrentModel() {
            return currentCompoundModel;
        }

        @Override
        public void modelUpdated(CompoundModel compoundModel, DebuggerEngine de) {
            List<ModelUpdateListener> muls2;
            synchronized(muls) {
                currentCompoundModel = compoundModel;
                muls2 = new LinkedList<ModelUpdateListener>(muls);
            }
            for (ModelUpdateListener mul : muls2) {
                mul.modelUpdated(compoundModel, de);
            }
        }
        
    }
    
}

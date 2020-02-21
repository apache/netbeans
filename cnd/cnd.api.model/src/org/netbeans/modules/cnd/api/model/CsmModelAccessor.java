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
package org.netbeans.modules.cnd.api.model;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 * utility class to access Csm model
 */
public final class CsmModelAccessor {

    // singleton instance of model
    private static CsmModel model;
    private static CsmModel dummy;
    private static CsmModelStateListener stateListener = new CsmModelStateListener() {

        @Override
        public void modelStateChanged(CsmModelState newState, CsmModelState oldState) {
            if (newState == CsmModelState.OFF) {
                CsmListeners.getDefault().removeModelStateListener(stateListener);
                model = null;
            }
        }
    };

    public static CsmModelState getModelState() {
        CsmModel aModel = model;
        return (aModel == null) ? CsmModelState.OFF : aModel.getState();
    }

    public static boolean isModelAlive() {
        final CsmModelState modelState = CsmModelAccessor.getModelState();
        return modelState == CsmModelState.ON || modelState == CsmModelState.SUSPENDED;
    }

    private static class ModelStub implements CsmModel {

        @Override
        public Collection<CsmProject> projects() {
            return Collections.<CsmProject>emptyList();
        }

        @Override
        public CsmProject getProject(Object id) {
            return null;
        }

        @Override
        public CsmFile findFile(FSPath absPath, boolean createIfPossible, boolean snapShot) {
            return null;
        }

        @Override
        public CsmFile[] findFiles(FSPath absPath, boolean createIfPossible, boolean snapShot) {
            return new CsmFile[0];
        }

        @Override
        public CsmModelState getState() {
            return CsmModelState.OFF;
        }

        @Override
        public Cancellable enqueue(Runnable task, CharSequence name) {
            return cancellableStub;
        }

        @Override
        public void scheduleReparse(Collection<CsmProject> projects) {
        }

        @Override
        @org.netbeans.api.annotations.common.SuppressWarnings("NP")
        public Boolean isProjectEnabled(Object id) {
            return null;
        }

        @Override
        public void disableProject(Object p) {

        }

        @Override
        public void enableProject(Object p) {

        }
    }

    private static final Cancellable cancellableStub = new Cancellable() {

        @Override
        public boolean cancel() {
            return true;
        }
    };

    /** Creates a new instance of CsmModelAccessor */
    private CsmModelAccessor() {
    }
    private static final boolean TRACE_GET_MODEL = Boolean.getBoolean("trace.get.model");

    /**
     * Gets CsmModel using Lookup
     */
    public static CsmModel getModel() {
        if (TRACE_GET_MODEL) {
            Thread.dumpStack();
        }
        if (model == null) {
            synchronized (CsmModel.class) {
                if (model == null) {
                    model = Lookup.getDefault().lookup(CsmModel.class);
                    if (model == null) {
                        return getStub();
                    } else {
                        CsmListeners.getDefault().addModelStateListener(stateListener);
                    }
                }
            }
        }
        return model;
    }

    private static CsmModel getStub() {
        if (dummy == null) {
            dummy = new ModelStub();
        }
        return dummy;
    }
}

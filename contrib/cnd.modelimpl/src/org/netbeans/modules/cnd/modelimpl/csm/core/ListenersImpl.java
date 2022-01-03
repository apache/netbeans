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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmModelStateListener;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.util.WeakList;
import org.openide.util.Lookup;

/**
 * CsmListeners implementation
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.CsmListeners.class)
public class ListenersImpl extends CsmListeners {

    private final WeakList<CsmModelListener> modelListeners = new WeakList<>();
    private final WeakList<CsmModelStateListener> modelStateListeners = new WeakList<>();
    
    @Override
    public void addModelListener(CsmModelListener listener) {
	modelListeners.add(listener);
    }

    @Override
    public void removeModelListener(CsmModelListener listener) {
	modelListeners.remove(listener);
    }

    @Override
    public void addProgressListener(CsmProgressListener listener) {
	ProgressSupport.instance().addProgressListener(listener);
    }

    @Override
    public void removeProgressListener(CsmProgressListener listener) {
	ProgressSupport.instance().removeProgressListener(listener);
    }
    
    @Override
    public void addModelStateListener(CsmModelStateListener listener) {
	modelStateListeners.add(listener);
    }

    @Override
    public void removeModelStateListener(CsmModelStateListener listener) {
	modelStateListeners.remove(listener);
    }
    
    //package-local
    static ListenersImpl getImpl() {
	return (ListenersImpl) CsmListeners.getDefault();
    }
    
    private Iterable<? extends CsmModelListener> getModelListeners() {
	Collection<? extends CsmModelListener> services = Lookup.getDefault().lookupAll(CsmModelListener.class);
	return (services.isEmpty()) ? modelListeners : modelListeners.join(services);
    }
    
    private Iterable<? extends CsmModelStateListener> getModelStateListeners() {
	Collection<? extends CsmModelStateListener> services = Lookup.getDefault().lookupAll(CsmModelStateListener.class);
	return (services.isEmpty()) ? modelStateListeners : modelStateListeners.join(services);
    }
    
    //package-local
    void fireProjectOpened(final ProjectBase csmProject) {
        csmProject.onAddedToModel();
        for ( CsmModelListener listener : getModelListeners() ) {
            try {
                listener.projectOpened(csmProject);
            } catch (AssertionError ex){
                DiagnosticExceptoins.register(ex);
            } catch (Exception ex) {
                DiagnosticExceptoins.register(ex);
            }
        }
    }
    
    //package-local
    void fireProjectClosed(CsmProject csmProject) {
        for ( CsmModelListener listener : getModelListeners() ) {
            try {
                listener.projectClosed(csmProject);
            } catch (AssertionError ex){
                DiagnosticExceptoins.register(ex);
            } catch (Exception ex) {
                DiagnosticExceptoins.register(ex);
            }
        }
    }
    
    //package-local
    void fireModelChanged(CsmChangeEvent e) {
        for ( CsmModelListener listener : getModelListeners() ) {
            try {
                listener.modelChanged(e);
            } catch (AssertionError ex){
                DiagnosticExceptoins.register(ex);
            } catch (Exception ex) {
                DiagnosticExceptoins.register(ex);
            }
        }
    }
    
    //package-local
    void fireModelStateChanged(CsmModelState newState, CsmModelState oldState) {
        for ( CsmModelStateListener listener : getModelStateListeners() ) {
            try {
                listener.modelStateChanged(newState, oldState);
            } catch (AssertionError ex){
                DiagnosticExceptoins.register(ex);
            } catch (Exception ex) {
                DiagnosticExceptoins.register(ex);
            }
        }
    }
    
}

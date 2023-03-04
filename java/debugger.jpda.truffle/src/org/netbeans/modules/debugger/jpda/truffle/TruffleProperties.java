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

package org.netbeans.modules.debugger.jpda.truffle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.netbeans.api.debugger.Properties;
import org.openide.util.BaseUtilities;

public final class TruffleProperties {
    
    private static final Properties truffleProperties = Properties.getDefault().getProperties("debugger.options.Truffle");  // NOI18N
    private static final String PROP_SHOW_INTERNAL = "showInternal";            // NOI18N
    private static TruffleProperties INSTANCE = new TruffleProperties();
    
    private TrufflePropertiesListener trufflePropertiesListener;
    
    private TruffleProperties() {}
    
    /*
    public static TruffleProperties getInstance() {
        return INSTANCE;
    }
    */
    public boolean isShowInternal() {
        return truffleProperties.getBoolean(PROP_SHOW_INTERNAL, false);
    }
    
    public void setShowInternal(boolean showInternal) {
        truffleProperties.setBoolean(PROP_SHOW_INTERNAL, showInternal);
    }
    
    public synchronized Disposable onShowInternalChange(Consumer<Boolean> onChange) {
        if (trufflePropertiesListener == null) {
            trufflePropertiesListener = new TrufflePropertiesListener();
            truffleProperties.addPropertyChangeListener(trufflePropertiesListener);
        }
        return trufflePropertiesListener.addOnShowInternalChange(onChange);
    }
    
    public final class Disposable {
        
        private final LinkedList<?> list;
        private final Consumer f;
        private final DisposableReference ref;
        
        Disposable(LinkedList<?> list, Consumer f) {
            this.list = list;
            this.f = f;
            ref = new DisposableReference(this, BaseUtilities.activeReferenceQueue());
        }
        
        public void dispose() {
            ref.dispose();
            ref.clear();
        }
    }
    
    private static class DisposableReference extends WeakReference<Disposable> implements Runnable {
        
        private final LinkedList<?> list;
        private final Consumer f;
        
        DisposableReference(Disposable disposable,  ReferenceQueue<? super Disposable> queue) {
            super(disposable, queue);
            this.list = disposable.list;
            this.f = disposable.f;
        }
        
        public void dispose() {
            synchronized (list) {
                list.remove(f);
            }
        }

        @Override
        public void run() {
            dispose();
        }
    }
    
    private class TrufflePropertiesListener implements PropertyChangeListener {
        
        private LinkedList<Consumer<Boolean>> onChangeListeners = new LinkedList<>();

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            switch (propertyName) {
                case PROP_SHOW_INTERNAL:
                    Boolean isInternal = (Boolean) evt.getNewValue();
                    List<Consumer<Boolean>> listeners;
                    synchronized (onChangeListeners) {
                        listeners = new ArrayList<>(onChangeListeners);
                    }
                    for (Consumer<Boolean> f : listeners) {
                        f.accept(isInternal);
                    }
                    break;
            }
        }

        private Disposable addOnShowInternalChange(Consumer<Boolean> onChange) {
            synchronized (onChangeListeners) {
                onChangeListeners.add(onChange);
            }
            return new Disposable(onChangeListeners, onChange);
        }
        
    }
    
}

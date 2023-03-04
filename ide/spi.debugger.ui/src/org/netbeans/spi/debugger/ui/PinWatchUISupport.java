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

import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.Watch.Pin;
import org.netbeans.modules.debugger.ui.annotations.WatchAnnotationProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Access to the default UI implementation of pin watches in editor.
 * Watches that are to be pinned by this support class, need to use the
 * {@link EditorPin} as the {@link Watch#getPin()} and an implementation
 * of {@link ValueProvider} needs to be register via {@link DebuggerServiceRegistration},
 * which acts as a supplier of the watch value.
 * <p>
 * Use {@link #pin(org.netbeans.api.debugger.Watch, java.lang.String)} to pin
 * the watch into editor where the corresponding {@link EditorPin} points at
 * and provide <code>valueProviderId</code> of the corresponding registered
 * {@link ValueProvider} which handles the value updates.
 * 
 * @author Martin Entlicher
 * @since 2.53
 */
public final class PinWatchUISupport {
    
    private static final PinWatchUISupport INSTANCE = new PinWatchUISupport();
    private final Object valueProvidersLock = new Object();
    private Map<String, DelegatingValueProvider> valueProviders;
    
    private PinWatchUISupport() {
        WatchAnnotationProvider.PIN_SUPPORT_ACCESS = new WatchAnnotationProvider.PinSupportedAccessor() {
            @Override
            public ValueProvider getValueProvider(EditorPin pin) {
                String id = pin.getVpId();
                if (id == null) {
                    return null;
                }
                synchronized (valueProvidersLock) {
                    DelegatingValueProvider vp = getValueProviders().get(id);
                    if (vp == null) {
                        vp = new DelegatingValueProvider(id);
                        valueProviders.put(id, vp);
                    }
                    return vp;
                }
            }
        };
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_CURRENT_ENGINE,
                new DebuggerManagerAdapter() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        refreshValueProviders();
                    }
                }
        );
    }
    
    /**
     * Get the default instance of this class.
     * @return An instance of {@link PinWatchUISupport} class.
     */
    public static PinWatchUISupport getDefault() {
        return INSTANCE;
    }
    
    /**
     * Pin the watch into an editor.
     * The pinned watch need to return {@link EditorPin} from {@link Watch#getPin()}
     * and a {@link ValueProvider} needs to be registered for the provided
     * <code>valueProviderId</code>.
     * @param watch A watch to pin at the location determined by the {@link EditorPin}.
     * @param valueProviderId An id of a registered {@link ValueProvider}.
     * @throws IllegalArgumentException is thrown when {@link Watch#getPin()}
     *                                  does not return an {@link EditorPin}, or
     *                                  when we're not able to find the editor
     *                                  where {@link EditorPin} points at.
     */
    public void pin(Watch watch, String valueProviderId) throws IllegalArgumentException {
        Pin wpin = watch.getPin();
        if (!(wpin instanceof EditorPin)) {
            throw new IllegalArgumentException("Unsupported pin: "+wpin);
        }
        synchronized (valueProvidersLock) {
            if (!getValueProviders().containsKey(valueProviderId)) {
                valueProviders.put(valueProviderId, new DelegatingValueProvider(valueProviderId));
            }
        }
        EditorPin pin = (EditorPin) wpin;
        pin.setVpId(valueProviderId);
        try {
            WatchAnnotationProvider.PIN_SUPPORT_ACCESS.pin(watch);
        } catch (DataObjectNotFoundException ex) {
            throw new IllegalArgumentException("Unable to find the pin's editor.", ex);
        }
    }
    
    private Map<String, DelegatingValueProvider> getValueProviders() {
        synchronized (valueProvidersLock) {
            if (valueProviders == null) {
                valueProviders = new HashMap<>();
                refreshValueProviders();
            }
            return valueProviders;
        }
    }
    
    private void refreshValueProviders() {
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        DebuggerEngine e = dm.getCurrentEngine ();
        List<? extends ValueProvider> providers;
        if (e == null) {
            providers = dm.lookup (null, ValueProvider.class);
        } else {
            providers = DebuggerManager.join(e, dm).lookup (null, ValueProvider.class);
        }
        if (!providers.isEmpty()) {
            synchronized (valueProvidersLock) {
                if (valueProviders == null) {
                    valueProviders = new HashMap<>();
                }
                Set<String> existingProviderIds = new HashSet<>();
                for (ValueProvider provider : providers) {
                    String id = provider.getId();
                    existingProviderIds.add(id);
                    DelegatingValueProvider dvp = valueProviders.get(id);
                    if (dvp == null) {
                        dvp = new DelegatingValueProvider(id);
                        valueProviders.put(id, dvp);
                    }
                    dvp.setDelegate(provider);
                }
                Set<String> staleProviderIds = new HashSet<>(valueProviders.keySet());
                staleProviderIds.removeAll(existingProviderIds);
                for (String staleId : staleProviderIds) {
                    valueProviders.get(staleId).setDelegate(null);
                }
            }
        }
    }
    
    private static final class DelegatingValueProvider implements ValueProvider {
        
        private final String id;
        private volatile ValueProvider delegate;
        private final Map<Watch, ValueChangeListener> listeners = new HashMap<>();
        
        DelegatingValueProvider(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getValue(Watch watch) {
            ValueProvider vp = delegate;
            if (vp != null) {
                return vp.getValue(watch);
            } else {
                return null;
            }
        }

        @Override
        public String getEvaluatingText() {
            ValueProvider vp = delegate;
            if (vp != null) {
                return vp.getEvaluatingText();
            } else {
                return ValueProvider.super.getEvaluatingText();
            }
        }

        @Override
        public String getEditableValue(Watch watch) {
            ValueProvider vp = delegate;
            if (vp != null) {
                return vp.getEditableValue(watch);
            } else {
                return ValueProvider.super.getEditableValue(watch);
            }
        }

        @Override
        public boolean setValue(Watch watch, String value) {
            ValueProvider vp = delegate;
            if (vp != null) {
                return vp.setValue(watch, value);
            } else {
                return ValueProvider.super.setValue(watch, value);
            }
        }

        @Override
        public Action[] getHeadActions(Watch watch) {
            ValueProvider vp = delegate;
            if (vp != null) {
                return vp.getHeadActions(watch);
            } else {
                return ValueProvider.super.getHeadActions(watch);
            }
        }

        @Override
        public Action[] getTailActions(Watch watch) {
            ValueProvider vp = delegate;
            if (vp != null) {
                return vp.getTailActions(watch);
            } else {
                return ValueProvider.super.getTailActions(watch);
            }
        }

        @Override
        public synchronized void setChangeListener(Watch watch, ValueChangeListener chl) {
            ValueProvider vp = delegate;
            if (vp != null) {
                vp.setChangeListener(watch, chl);
            }
            listeners.put(watch, chl);
        }

        @Override
        public synchronized void unsetChangeListener(Watch watch) {
            ValueProvider vp = delegate;
            if (vp != null) {
                vp.unsetChangeListener(watch);
            }
            listeners.remove(watch);
        }
        
        synchronized void setDelegate(ValueProvider delegate) {
            this.delegate = delegate;
            if (delegate == null) {
                for (Map.Entry<Watch, ValueChangeListener> wvl : listeners.entrySet()) {
                    wvl.getValue().valueChanged(wvl.getKey());
                }
            } else {
                for (Map.Entry<Watch, ValueChangeListener> wvl : listeners.entrySet()) {
                    delegate.setChangeListener(wvl.getKey(), wvl.getValue());
                }
            }
        }
        
    }
    
    /**
     * Provider of pinned watch value.
     * Register an implementation of this class via {@link DebuggerServiceRegistration}
     * for the corresponding debugger session ID path.
     */
    public static interface ValueProvider {
        
        /**
         * Get a unique ID of this value provider.
         * Use this ID when pinning a watch via {@link #pin(org.netbeans.api.debugger.Watch, java.lang.String)}.
         * @return An ID of this value provider.
         */
        String getId();
        
        /**
         * Get current value of pinned watch. This method must not block,
         * it's called synchronously in the EQ thread. This method should return
         * most recent value of the watch, or the same instance which
         * {@link #getEvaluatingText()} returns when the watch value is being computed,
         * or <code>null</code> when the watch can not be resolved.
         * @param watch the watch whose value is to be returned.
         * @return The current value of the watch, or {@link #getEvaluatingText()},
         *         or <code>null</code>.
         */
        String getValue(Watch watch);
        
        /**
         * Get a localized text to be displayed when the watch is being evaluated.
         * The pin watch UI highlights changed values. It uses this string to
         * distinguish real watch values. Return the same instance from
         * {@link #getValue(org.netbeans.api.debugger.Watch)} when the watch is
         * being computed.
         * @return A localized text displayed while the watch is evaluating.
         */
        @NbBundle.Messages("WATCH_EVALUATING=Evaluating...")
        default String getEvaluatingText() {
            return Bundle.WATCH_EVALUATING();
        }

        /**
         * Determine whether the current watch value is editable and if yes,
         * return the value to edit.
         * The value returned by {@link #getValue(org.netbeans.api.debugger.Watch)}
         * might not be the proper value to edit. It may contain type description
         * or more descriptive information about the value. This method should
         * provide a raw textual representation of the value to edit,
         * or <code>null</code> when the value is not editable.<br>
         * The default implementation return <code>null</code>.
         * @param watch The watch to return the editable value for
         * @return The string representation of the editable value,
         * or <code>null</code> when the value is not editable.
         */
        default String getEditableValue(Watch watch) {
            return null;
        }

        /**
         * Set a watch value as a response to finished editing.
         * This method is called only when a prior call to
         * {@link #getEditableValue(org.netbeans.api.debugger.Watch)} returns
         * a non-null value.<br>
         * The default implementation throws {@link UnsupportedOperationException}.
         * @param watch The watch to set the value for.
         * @param value The new watch value.
         * @return <code>true</code> when the value was set successfully and
         *         the UI should get updated from {@link #getValue(org.netbeans.api.debugger.Watch)},
         *         <code>false</code> when the set fails and the last watch value
         *         should stay. The implementation is responsible for any error
         *         reporting when the set fails.
         */
        default boolean setValue(Watch watch, String value) {
            throw new UnsupportedOperationException("Watch not editable.");
        }

        /**
         * Get actions to be displayed at the head of pin watch component.
         * Typically, you put an expansion action there.
         * The default implementation returns <code>null</code>.
         * @param watch The watch to get the actions for
         * @return An array of actions or <code>null</code> elements (for action separators),
         *         or <code>null</code> when no actions should be displayed.
         */
        default Action[] getHeadActions(Watch watch) {
            return null;
        }

        /**
         * Get actions to be displayed at the tail of pin watch component.
         * Typically, you put a details action there, which displays a detailed
         * view of the watch value. These actions, if any, are followed by comment
         * and close actions, which are always present.
         * The default implementation returns <code>null</code>.
         * @param watch The watch to get the actions for
         * @return An array of actions or <code>null</code> elements (for action separators),
         *         or <code>null</code> when no actions should be displayed.
         */
        default Action[] getTailActions(Watch watch) {
            return null;
        }

        /**
         * Allows to set a value change listener for a specific watch.
         * @param watch The watch to listen for changes
         * @param chl The value change listener.
         */
        void setChangeListener(Watch watch, ValueChangeListener chl);
        
        /**
         * Unset a value change listener for a specific watch.
         * Use this method to unregister the listeners and free up associated resources.
         * @param watch The watch to unset the listener from.
         */
        void unsetChangeListener(Watch watch);
        
        /**
         * Listener for watch value changes.
         */
        public static interface ValueChangeListener {
            
            /**
             * Notify that a watch value has changed.
             * {@link ValueProvider#getValue(org.netbeans.api.debugger.Watch)}
             * then returns the new value.
             * @param watch The watch whose value has changed.
             */
            void valueChanged(Watch watch);
        }
    }
}

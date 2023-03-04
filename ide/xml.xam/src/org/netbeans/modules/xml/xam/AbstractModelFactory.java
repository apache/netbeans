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

package org.netbeans.modules.xml.xam;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.xam.spi.ModelAccessProvider;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Chris Webster
 * @author Nam Nguyen
 */
public abstract class AbstractModelFactory<M extends Model> {
    private static final Logger LOG = Logger.getLogger(AbstractModelFactory.class.getName()); 
    
    public AbstractModelFactory() {
        factories.add(new WeakReference<AbstractModelFactory>(this));
        propSupport = new PropertyChangeSupport(this);
    }
    
    public static final int DELAY_SYNCER = 2000;  // milisecs.
    public static final int DELAY_DIRTY = 1000;  // milisecs.
    
    public static final String MODEL_LOADED_PROPERTY = "modelLoaded";

    private WeakHashMap<Object, WeakReference<M>> cachedModels = 
	new WeakHashMap<Object,WeakReference<M>>();
    private PropertyChangeSupport propSupport;
    
    protected abstract M createModel(ModelSource source);
    

    /**
     * Create new model from given model source; returns null if there are errors
     * during creation. 
     * 
     * Note that, the returned model might not be valid, i.e., source is well-formed. 
     * Also, that the returned model is not cached and does not have background 
     * auto-sync support.
     */
    public M createFreshModel(ModelSource modelSource) {
        M model = createModel(modelSource);
        try {
            if (model != null) {
                model.sync();
                propSupport.firePropertyChange(MODEL_LOADED_PROPERTY, null, model);
            }
        } catch (IOException ioe) {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Sync has errors", ioe);
        }
        return model;
    }
    
    /**
     * This method extracts the key from the model source. A subclass can 
     * change the ModelSource lookup requirements and thus this method may
     * be overridden to allow a different key to be used. 
     */
    protected Object getKey(ModelSource source) {
        ModelAccessProvider p = getEffectiveAccessProvider(source);
        if (p != null) {
            return p.getModelSourceKey(source);
        }
	return (File) source.getLookup().lookup(File.class);
    }
    
    private ModelAccessProvider getEffectiveAccessProvider(ModelSource ms) {
	ModelAccessProvider p = (ModelAccessProvider)
	    ms.getLookup().lookup(ModelAccessProvider.class);
	return p == null ? getAccessProvider() : p;
    }
    
    public static ModelAccessProvider getAccessProvider() {
        return (ModelAccessProvider) Lookup.getDefault().lookup(ModelAccessProvider.class);
    }
    
    protected synchronized M getModel(ModelSource source) {
        if (source == null) {
            return null;
        }
	Object key = getKey(source);
        if (key == null) {
            LOG.log(Level.WARNING, "Could not get key for the model source {0}. effectiveAP: {1}, lookup contents: {2}",
                    new Object[] { source, getEffectiveAccessProvider(source), 
                        source.getLookup().lookupAll(Object.class)
                    });
            LOG.log(Level.WARNING, "ModelSource created from:", source.creation);
        }
	assert key != null;
        WeakReference<M> modelRef = cachedModels.get(key);
        M model = (modelRef == null ? null : modelRef.get());
        if (model == null) {
            model = createModel(source);
            if (model != null) {
                try {
                    model.sync();
                } catch (IOException ioe) {
                    Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Sync has errors", ioe);
                }
                cachedModels.put(key, new WeakReference<M>(model));
                propSupport.firePropertyChange(MODEL_LOADED_PROPERTY, null, model);
            }
        }
        return model;
    }

    private static List<WeakReference<AbstractModelFactory>> factories = 
            new ArrayList<WeakReference<AbstractModelFactory>>();
    
    private static RequestProcessor.Task SYNCER = null; 
    static {
        if (getAccessProvider() != null) {
            SYNCER = RequestProcessor.getDefault().post(
                new Runnable() {
                    public void run() {
                        try {
                            for (AbstractModel model : getAllModels()) {
                                if (model.isAutoSyncActive() && 
                                    model.getAccess().dirtyIntervalMillis() > DELAY_DIRTY) {
                                    model.runAutoSync();
                                }
                            }
                        } catch (Exception e) {
                            Logger.getLogger(getClass().getName()).log(Level.FINE, "auto-sync", e);
                        }
                        SYNCER.schedule(DELAY_SYNCER);
                    }
                }, DELAY_SYNCER);
        }
    }
    
    private static synchronized List<AbstractModel> getAllModels() {
        List<AbstractModel> models = new ArrayList<AbstractModel>();
        List<WeakReference<AbstractModelFactory>> mfactories = 
            new ArrayList<WeakReference<AbstractModelFactory>>(factories);
        
        for (WeakReference<AbstractModelFactory> r : mfactories) {
            AbstractModelFactory factory = r.get();
            if (factory != null) {
                for (Object m : factory.getModels()) {
                    if (m instanceof AbstractModel) {
                        AbstractModel am = (AbstractModel) m;
                        models.add(am);
                    }
                }
            }
        }
        return models;
    }
    
    /**
     * Returns list of models currently cached by the factory.
     */
    public List<M> getModels() {
        List<WeakReference<M>> refs;
        synchronized(this) {
            refs = new ArrayList<WeakReference<M>>(cachedModels.values());
        }

        List<M> ret = new ArrayList<M>();
        for (WeakReference<M> ref : refs) {
            if (ref != null) {
                M model = ref.get();
                if (model != null) {
                    ret.add(model);
                }
            }
        }
        return ret;
    }
    
    /**
     * Adds property change listener on the factory to be notified on
     * when new models are loaded by the factory.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propSupport.addPropertyChangeListener(l);
    }

    /**
     * Removes property change listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propSupport.removePropertyChangeListener(l);
    }
}


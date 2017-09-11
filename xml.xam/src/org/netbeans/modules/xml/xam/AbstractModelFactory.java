/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    
    private synchronized static List<AbstractModel> getAllModels() {
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


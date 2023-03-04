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

package org.netbeans.modules.xml.schema.model.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.util.WeakListeners;

/**
 * Class is intended to do optimized resolving of a schema model by an 
 * Import/Include/Redefine. It provide caching and processing of changes in models.
 *
 * @author Nikita Krjukov
 */
public class RefCacheSupport {

    public static final long UNRESOLVED_EXPIRATION_DELAY = 5000;

    // Owning schema model.
    private SchemaModel mSModel;

    // The caching map.
    private WeakHashMap<SchemaModelReference, Object> refModelCache =
            new WeakHashMap<SchemaModelReference, Object>();

    // Listens self schema model.
    private PropertyChangeListener mPropertySelfListener = null;

    // Listens external schema models.
    private PropertyChangeListener mPropertyExtListener = null;

    public RefCacheSupport(SchemaModel sModel) {
        mSModel = sModel;
    }

    /**
     * It is mainly intended to be used by JUnit tests.
     * @return
     */
    public int getCachedModelsSize() {
        return refModelCache.size();
    }

    /**
     * Checks that all cached schema model references relate to the cache's owner model.
     * It is mainly intended to be used by JUnit tests.
     *
     * @return count of unappropriate items in the cache
     */
    public int checkKeys() {
        int wrongModelCounter = 0;
        for (SchemaModelReference smr : refModelCache.keySet()) {
            if (smr.getModel() != mSModel) {
                wrongModelCounter++;
            }
        }
        return wrongModelCounter;
    }

    /**
     * It is mainly intended to be used by JUnit tests.
     * @return
     */
    public SchemaModelImpl getCachedModel(SchemaModelReference ref) {
        Object cachedValue = refModelCache.get(ref);
        if (cachedValue instanceof SmAttachment) {
            return SmAttachment.class.cast(cachedValue).mSchemaModel;
        } else {
            return null;
        }
    }

    /**
     * It is mainly intended to be used by JUnit tests.
     * @return
     */
    public boolean contains(SchemaModelImpl model) {
        return refModelCache.containsValue(model);
    }

    /**
     * Does optimized resolve. It means that the required schema model
     * can be taken from cache instead of being resolved again.
     * 
     * @param ref is an Import/Include/Redefine.
     * @return found schema model. 
     */
    public SchemaModelImpl optimizedResolve(SchemaModelReference ref) {
        try {
            Object cachedValue = refModelCache.get(ref);
            if (cachedValue != null) {
                if (cachedValue instanceof SmAttachment) {
                    SmAttachment sma = SmAttachment.class.cast(cachedValue);
                    SchemaModelImpl cachedModel = sma.mSchemaModel;
                    State cachedModelState = cachedModel.getState();
                    if (cachedModelState == State.VALID) {
                        return cachedModel;
                    } else {
                        // If the cached model is invalid then it has to be
                        // unsubscribed and removed from cached first.
                        // Otherwize it can remain in cache forever.
                        excludeModel(cachedModel);
                    }
                } else if (cachedValue instanceof UnresolvedSchemaRef) {
                    if (System.currentTimeMillis() <
                            UnresolvedSchemaRef.class.cast(
                            cachedValue).expiratoinTime) {
                        //
                        // The unresolved schema reference hasn't expared yet.
                        return null;
                    }
                }
            }
            //
            SchemaModelImpl resolved = (SchemaModelImpl) ref.resolveReferencedModel();
            //
            if (resolved != null) {
                attach(ref, resolved);
            } else {
                refModelCache.put(ref, new UnresolvedSchemaRef());
            }
            return resolved;
        } catch (CatalogModelException ex) {
            refModelCache.put(ref, new UnresolvedSchemaRef());
            return null;
        }
    }

    /**
     * Unsubscribes from all external schema models and clear the cache.
     * After execution the cash should be in the same state like it has 
     * just after creation.
     * The method can be helpful for finalization. 
     */
    public void discardCache() {
        for (Object cachedValue : refModelCache.values()) {
            if (cachedValue instanceof SmAttachment) {
                SmAttachment sma = SmAttachment.class.cast(cachedValue);
                sma.mSchemaModel.removePropertyChangeListener(sma.mPCL);
            }
        }
        refModelCache.clear();
        mSModel.removePropertyChangeListener(mPropertySelfListener);
    }

    /**
     * Initializes a listener, which listens self model.
     * It looks for removing of an Import/Include/Redefine.
     * If an Import/Include/Redefine is removed, it is excluded from
     * the cache. 
     */
    private void lazySelfListenerInit() {
        //
        if (mPropertySelfListener == null) {
            mPropertySelfListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    String propName = evt.getPropertyName();
                    if (Schema.SCHEMA_REFERENCES_PROPERTY.equals(propName)) {
                        Object newValue = evt.getNewValue();
                        Object oldValue = evt.getOldValue();
                        if (newValue == null && oldValue instanceof SchemaModelReference) {
                            //
                            // An Import/Include/Redefine is deleted.
                            SchemaModelReference sModelRef =
                                    SchemaModelReference.class.cast(oldValue);
                            excludeModelRef(sModelRef);
                            // System.out.println("removed from cache");
                        }
                    }
                    //
                    if (SchemaModelReference.SCHEMA_LOCATION_PROPERTY.equals(propName)) {
                        Object source = evt.getSource();
                        if (source instanceof SchemaModelReference) {
                            excludeModelRef((SchemaModelReference)source);
                            // System.out.println("schema location changed");
                        }
                    }
                    //
                    if (Import.NAMESPACE_PROPERTY.equals(propName)) {
                        Object source = evt.getSource();
                        if (source instanceof Import) {
                            excludeModelRef((Import)source);
                            // System.out.println("Import's namespace changed");
                        }
                    }
                    //
                    if (Model.STATE_PROPERTY.equals(propName)) {
                        Object oldValue = evt.getOldValue();
                        Object newValue = evt.getNewValue();
                        Object source = evt.getSource();
                        if (newValue != State.VALID && source == mSModel) {
                            discardCache();
                            // System.out.println("schema is not valid");
                        }
                        if (oldValue != State.VALID && newValue == State.VALID
                                && source == mSModel) {
                            // Discard again for other case. 
                            discardCache();
                            // System.out.println("schema has become valid");
                        }
                    }
                }
            };
            mSModel.addPropertyChangeListener(mPropertySelfListener);
        }
    }

    /**
     * Creates attachment between a schema reference and the corresponding model.
     * All required activities are made synchronized:
     *  - new weak listener is created and subscribtion is adde to listen
     * changes of the referenced model.
     *  - the model and the listener is added to cache
     */
    private synchronized void attach(SchemaModelReference ref, 
            SchemaModelImpl referencedModel) {
        //
        lazySelfListenerInit();
        //
        if (mPropertyExtListener == null) {
            initExtListener();
        }
        PropertyChangeListener weakListener = WeakListeners.propertyChange(
                mPropertySelfListener, referencedModel);
        //
        referencedModel.addPropertyChangeListener(weakListener);
        //
        refModelCache.put(ref, new SmAttachment(referencedModel, weakListener));
    }

    /**
     * Excludes a schema model from the cache.
     * @param referencedModel
     */
    private synchronized void excludeModel(SchemaModel referencedModel) {
        //
        // Find cache entry to remove
        List<SchemaModelReference> toRemove = new ArrayList<SchemaModelReference>();
        for (Entry<SchemaModelReference, Object> entry : refModelCache.entrySet()) {
            Object cachedValue = entry.getValue();
            if (cachedValue instanceof SmAttachment) {
                SmAttachment sma = SmAttachment.class.cast(cachedValue);
                if (referencedModel.equals(sma.mSchemaModel)) {
                    toRemove.add(entry.getKey());
                }
            }
        }
        //
        // Remove
        for (SchemaModelReference smr : toRemove) {
            excludeModelRef(smr);
        }
    }

    /**
     * Excludes an Import/Include/Redefine from the cache.
     * @param sModelRef
     */
    private synchronized void excludeModelRef(SchemaModelReference sModelRef) {
        Object oldValue = refModelCache.remove(sModelRef);
        //
        if (oldValue instanceof SmAttachment) {
            SmAttachment sma = SmAttachment.class.cast(oldValue);
            sma.mSchemaModel.removePropertyChangeListener(sma.mPCL);
        }
    }

    /**
     * Creates a listener which processes change events from external models.
     * Only relevant changes are taken into account.
     */
    private void initExtListener() {
        mPropertyExtListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if (Model.STATE_PROPERTY.equals(propName)) {
                    Object oldValue = evt.getOldValue();
                    Object newValue = evt.getNewValue();
                    Object source = evt.getSource();
                    if (newValue != State.VALID && source instanceof SchemaModel) {
                        excludeModel(SchemaModel.class.cast(source));
                        // System.out.println("schema is not valid");
                    }
                    if (oldValue != State.VALID && newValue == State.VALID
                            && source == mSModel) {
                        // Remove schema again for other case.
                        excludeModel(SchemaModel.class.cast(source));
                        // System.out.println("schema has become valid");
                    }
                }
                if (Schema.TARGET_NAMESPACE_PROPERTY.equals(propName)) {
                    Object source = evt.getSource();
                    if (source instanceof Schema) {
                        Schema schema = Schema.class.cast(source);
                        excludeModel(schema.getModel());
                        // System.out.println("target namespace changed");
                    }
                }
            }
        };
    }

    private static class SmAttachment {
        public SchemaModelImpl mSchemaModel;
        public PropertyChangeListener mPCL;

        public SmAttachment(SchemaModelImpl sModel, PropertyChangeListener pcl) {
            assert sModel != null && pcl != null;
            //
            mSchemaModel = sModel;
            mPCL = pcl;
        }
    }

    private static class UnresolvedSchemaRef {
        public long expiratoinTime;

        public UnresolvedSchemaRef() {
            expiratoinTime = System.currentTimeMillis() + UNRESOLVED_EXPIRATION_DELAY;
        }
    }

}

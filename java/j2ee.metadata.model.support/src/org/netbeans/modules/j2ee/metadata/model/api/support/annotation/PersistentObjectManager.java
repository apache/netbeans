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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.j2ee.metadata.model.support.PersistentObjectList;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Andrei Badea
 */
public class PersistentObjectManager<T extends PersistentObject> implements JavaContextListener {

    // XXX perhaps also initialize temporary when classpath not registered in GPR
    // (since then there would be no events)

    private static final Logger LOGGER = Logger.getLogger(PersistentObjectManager.class.getName());
    private static final boolean NO_EVENTS = Boolean.getBoolean("netbeans.metadata.model.noevents"); // NOI18N
    // XXX for M9 only
    // XXX causes PersistentObjectManagerTest to fail; currently excluded in test/cfg-unit.xml
    // private static final boolean NO_EVENTS = true;

    private final AnnotationModelHelper helper;
    private final ObjectProvider<T> provider;
    private final PersistentObjectList<T> objectList = new PersistentObjectList<T>();
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final RequestProcessor rp = new RequestProcessor("PersistentObjectManager", 1); // NOI18N

    // not private because used in unit tests
    boolean initialized = false;
    boolean temporary = false;

    static <V extends PersistentObject> PersistentObjectManager<V> create(AnnotationModelHelper helper, ObjectProvider<V> provider) {
        PersistentObjectManager<V> pom = new PersistentObjectManager<V>(helper, provider);
        if (NO_EVENTS) {
            LOGGER.log(Level.FINE, "ignoring events"); // NOI18N
        }
        helper.addJavaContextListener(pom);
        return pom;
    }

    private PersistentObjectManager(AnnotationModelHelper helper, ObjectProvider<T> provider) {
        this.helper = helper;
        this.provider = provider;
    }

    public Collection<T> getObjects() {
        ensureInitialized();
        List<T> result = objectList.get();
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "getObjects returning {0} objects: {1}", new Object[] { result.size(), result}); // NOI18N
        } else {
            LOGGER.log(Level.FINE, "getObjects returning {0} objects", result.size()); // NOI18N
        }
        return result;
    }

    private void ensureInitialized() {
        if (!initialized) {
            boolean scanInProgress = SourceUtils.isScanInProgress();
            temporary = NO_EVENTS | scanInProgress;
            if (temporary) {
                LOGGER.log(Level.FINE, "initalizing temporarily (scanInProgress: {0})", scanInProgress); // NOI18N
            } else {
                LOGGER.log(Level.FINE, "intializing"); // NOI18N
            }
            List<T> objects;
            try {
                objects = provider.createInitialObjects();
            } catch (InterruptedException e) {
                LOGGER.log(Level.FINE, "initializing temporarily (createInitialObjects() throwed InterruptedException)"); // NOI18N
                temporary = true;
                objects = Collections.emptyList();
            }
            LOGGER.log(Level.FINE, "created initial objects {0}", objects); // NOI18N
            objectList.add(objects);
            initialized = true;
        }
    }

    private void deinitialize() {
        initialized = false;
        objectList.clear();
    }

    void typesAdded(Iterable<? extends ElementHandle<TypeElement>> typeHandles) {
        // XXX assert not in AMH java context
        LOGGER.log(Level.FINE, "typesAdded: called with {0}", typeHandles); // NOI18N
        if (!initialized) {
            LOGGER.log(Level.FINE, "typesAdded: not initialized, firing change event"); // NOI18N
            fireChange();
            return;
        }
        for (ElementHandle<TypeElement> typeHandle : typeHandles) {
            TypeElement type = typeHandle.resolve(helper.getCompilationController());
            if (type == null) {
                LOGGER.log(Level.WARNING, "typesAdded: type {0} has dissapeared", typeHandle); // NOI18N
                continue;
            }
            List<T> newObjects = provider.createObjects(type);
            LOGGER.log(Level.FINE, "typesAdded: new objects {0}", newObjects); // NOI18N
            if (objectList.put(typeHandle, newObjects)) {
                fireChange();
            }
        }
    }

    void typesRemoved(Iterable<? extends ElementHandle<TypeElement>> typeHandles) {
        // XXX assert not in AMH java context
        LOGGER.log(Level.FINE, "typesRemoved: called with {0}", typeHandles); // NOI18N
        if (!initialized) {
            LOGGER.log(Level.FINE, "typesRemoved: not initialized, firing change event"); // NOI18N
            fireChange();
            return;
        }
        for (ElementHandle<TypeElement> typeHandle : typeHandles) {
            List<T> list = objectList.remove(typeHandle);
            if (list != null) {
                LOGGER.log(Level.FINE, "typesRemoved: removing objects {0}", list); // NOI18N
                fireChange();
            }
        }
    }

    void typesChanged(Iterable<? extends ElementHandle<TypeElement>> typeHandles) {
        // XXX assert not in AMH java context
        LOGGER.log(Level.FINE, "typesChanged: called with {0}", typeHandles); // NOI18N
        if (!initialized) {
            LOGGER.log(Level.FINE, "typesChanged: not initialized, firing change event"); // NOI18N
            fireChange();
            return;
        }
        for (ElementHandle<TypeElement> typeHandle : typeHandles) {
            List<T> list = objectList.get(typeHandle);
            if (list != null){
                // we have objects based on this type
                TypeElement type = typeHandle.resolve(helper.getCompilationController());
                if (type == null) {
                    LOGGER.log(Level.WARNING, "typesChanged: type {0} has dissapeared", typeHandle); // NOI18N
                    continue;
                }
                List<T> oldNewObjects = new ArrayList<T>(list);
                boolean modified = provider.modifyObjects(type, oldNewObjects);
                if (modified) {
                    LOGGER.log(Level.FINE, "typesChanged: modified objects to {0}", oldNewObjects); // NOI18N
                    if (objectList.put(typeHandle, oldNewObjects)) {
                        fireChange();
                    }
                } else {
                    LOGGER.log(Level.FINE, "typesChanged: not modifying any objects"); // NOI18N
                }
            } else {
                // we don't have any object based on this type
                TypeElement type = typeHandle.resolve(helper.getCompilationController());
                if (type == null) {
                    LOGGER.log(Level.WARNING, "typesChanged: type {0} has dissapeared", typeHandle); // NOI18N
                    continue;
                }
                List<T> newObjects = provider.createObjects(type);
                LOGGER.log(Level.FINE, "typesChanged: new objects {0}", newObjects); // NOI18N
                if (objectList.put(typeHandle, newObjects)) {
                    fireChange();
                }
            }
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private void fireChange() {
        LOGGER.log(Level.FINE, "firing change event"); // NOI18N
        if (!changeSupport.hasListeners()) {
            return;
        }
        rp.post(new Runnable() {
            public void run() {
                changeSupport.fireChange();
            }
        });
    }

    void rootsChanged() {
        // XXX assert not in AMH java context
        LOGGER.log(Level.FINE, "rootsChanged called"); // NOI18N
        deinitialize();
        fireChange();
    }

    public void javaContextLeft() {
        if (initialized && temporary) {
            LOGGER.log(Level.FINE, "discarding temporary manager"); // NOI18N
            deinitialize();
        }
    }
}

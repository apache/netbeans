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
package org.netbeans.modules.javaee.resources.impl.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.javaee.resources.api.JmsDestination;
import org.netbeans.modules.javaee.resources.api.JndiResource;
import org.netbeans.modules.javaee.resources.spi.model.JndiResourcesModelProvider;

/**
 * Base implementation of the JndiResourcesModelProvider.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
class JndiResourcesModelProviderImpl implements JndiResourcesModelProvider {

    private PersistentObjectManager<JmsDestinationImpl> jmsDestinationManager;
    private PersistentObjectManager<JmsDestinationsImpl> jmsDestinationsManager;

    private final AnnotationModelHelper modelHelper;
    private final AtomicBoolean isDirty = new AtomicBoolean(true);
    private Map<JndiResource.Type, List<? extends JndiResource>> cachedResources =
            new EnumMap<JndiResource.Type, List<? extends JndiResource>>(JndiResource.Type.class);

    public JndiResourcesModelProviderImpl(JndiResourcesModelImpl jndiResourcesModelImplementation) {
        this.modelHelper = jndiResourcesModelImplementation.getHelper();
        addIndexListener();
    }

    @Override
    public List<JndiResource> getResources() {
        List<JndiResource> allResources = new ArrayList<JndiResource>();
        allResources.addAll(getJmsDestinations());
        return allResources;
    }

    @Override
    public List<JmsDestination> getJmsDestinations() {
        boolean dirty = isDirty.getAndSet(false);
        if (!dirty) {
            List<JndiResource> result = getCachedNamedElements(JndiResource.Type.JMS_DESTINATION);
            List<JmsDestination> destinations = new ArrayList<JmsDestination>();
            for (JndiResource jndiResource : result) {
                destinations.add((JmsDestination) jndiResource);
            }
            if (!isDirty.get()) {
                return destinations;
            }
        }

        List<JmsDestination> result = new ArrayList<JmsDestination>();
        result.addAll(getJmsDestinationManager().getObjects());
        for (JmsDestinationsImpl jmsDestinationsImpl : getJmsDestinationsManager().getObjects()) {
            result.addAll(jmsDestinationsImpl.getJmsDestinations());
        }
        setCachedResult(JndiResource.Type.JMS_DESTINATION, result);
        return result;
    }

    private synchronized PersistentObjectManager<JmsDestinationImpl> getJmsDestinationManager() {
        if (jmsDestinationManager == null) {
            jmsDestinationManager = modelHelper.createPersistentObjectManager(new JndiResourcesObjectProviders.JmsDestinationProvider(modelHelper));
        }
        return jmsDestinationManager;
    }

    private synchronized PersistentObjectManager<JmsDestinationsImpl> getJmsDestinationsManager() {
        if (jmsDestinationsManager == null) {
            jmsDestinationsManager = modelHelper.createPersistentObjectManager(new JndiResourcesObjectProviders.JmsDestinationsProvider(modelHelper));
        }
        return jmsDestinationsManager;
    }

    private void addIndexListener() {
        modelHelper.getClasspathInfo().getClassIndex().addClassIndexListener(new ClassIndexListener() {
            @Override
            public void typesAdded(final TypesEvent event) {
                setDirty();
            }
            @Override
            public void typesRemoved(final TypesEvent event) {
                setDirty();
            }
            @Override
            public void typesChanged(final TypesEvent event) {
                setDirty();
            }
            @Override
            public void rootsAdded(RootsEvent event) {
                setDirty();
            }
            @Override
            public void rootsRemoved(RootsEvent event) {
                setDirty();
            }
            private void setDirty() {
                isDirty.set(true);
            }
        });
    }

    private void setCachedResult(JndiResource.Type resultType, List<? extends JndiResource> resources) {
        cachedResources.put(resultType, resources);
    }

    private List<JndiResource> getCachedNamedElements(JndiResource.Type resultType) {
        List<JndiResource> result = new ArrayList<JndiResource>(cachedResources.get(resultType));
        return result;
    }
}

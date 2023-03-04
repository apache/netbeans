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
package org.netbeans.modules.spring.beans.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.spring.api.beans.SpringAnnotations;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.spi.beans.SpringModelProvider;

/**
 * @author Martin Fousek <marfous@netbeans.org>
 */
class SpringModelProviderImpl implements SpringModelProvider {

    private AnnotationModelHelper helper;
    private PersistentObjectManager<SpringBeanImpl> springBeanManager;
    
    private AtomicBoolean isDirty = new AtomicBoolean(true);
    private volatile boolean isIndexListenerAdded;
    private List<SpringBean> cachedSpringBeans;

    public SpringModelProviderImpl(SpringModelImplementation springModelImplementation) {
        this.helper = springModelImplementation.getHelper();
        this.springBeanManager = helper.createPersistentObjectManager(new ObjectProviders.SpringBeanProvider(helper));
    }

    /**
     * Returns annotated Spring bean classes. It means every class annotated with one of
     * {@link SpringAnnotations#SPRING_COMPONENTS} string type.
     * @return 
     */
    @Override
    public List<SpringBean> getBeans() {
        boolean dirty = isDirty.getAndSet(false);

        if (!isIndexListenerAdded) {
            addIndexListener();
        }
        if (!dirty) {
            List<SpringBean> result = getCachedNamedElements();
            if (!isDirty.get()) {
                return result;
            }
        }

        List<SpringBean> result = new LinkedList<SpringBean>();
        Collection<SpringBeanImpl> springBeans = getSpringBeanManager().getObjects();
        for (SpringBeanImpl springBeanImpl : springBeans) {
            result.add(springBeanImpl);
        }

        setCachedResult(result);
        return result;
    }

    private PersistentObjectManager<SpringBeanImpl> getSpringBeanManager() {
        return springBeanManager;
    }

    private void addIndexListener() {
        isIndexListenerAdded = true;
        helper.getClasspathInfo().getClassIndex().addClassIndexListener(
                new ClassIndexListener() {

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

    private void setCachedResult(List<SpringBean> list) {
        cachedSpringBeans = new ArrayList<SpringBean>(list);
    }

    private List<SpringBean> getCachedNamedElements() {
        List<SpringBean> result = new ArrayList<SpringBean>(cachedSpringBeans);
        return result;
    }
}

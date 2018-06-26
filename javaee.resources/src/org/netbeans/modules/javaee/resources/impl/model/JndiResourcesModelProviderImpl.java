/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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

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
package org.netbeans.modules.javaee.resources.api.model;

import java.util.Collection;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.javaee.resources.spi.model.JndiResourcesModelProvider;
import org.netbeans.modules.javaee.resources.spi.model.JndiResourcesModelProviderFactory;
import org.openide.util.Lookup;

/**
 * Abstract JndiResourceModel implementation, holder of meta data, annotation helper etc.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public abstract class JndiResourcesAbstractModel {

    private final JndiResourcesModel model;
    private final AnnotationModelHelper helper;
    private final JndiResourcesModelProvider provider;

    protected JndiResourcesAbstractModel(JndiResourcesModelUnit modelUnit) {
        this.model = new JndiResourcesModel(this);
        this.helper = AnnotationModelHelper.create(modelUnit.getClassPathInfo());
        this.provider = createModelProvider();
    }

    private JndiResourcesModelProvider createModelProvider() {
        JndiResourcesModelProvider result = null;
        Collection<? extends JndiResourcesModelProviderFactory> factories =
                Lookup.getDefault().lookupAll(JndiResourcesModelProviderFactory.class);
        for (JndiResourcesModelProviderFactory factory : factories) {
            result = factory.createProvider(this);
            if (result != null) {
                return result;
            }
        }
        return result;
    }

    /**
     * Returns specific implementation of {@code JndiResourcesModel}.
     * @return specific {@code JndiResourcesModel} class
     */
    protected JndiResourcesModel getModel() {
        return model;
    }

    /**
     * Returns {@code AnnotationModelHelper} of this Model.
     * @return {@code AnnotationModelHelper} of this Model
     */
    public AnnotationModelHelper getHelper() {
        return helper;
    }

    /**
     * Returns {@code JndiResourcesModelProvider} for current Model.
     * @return {@code JndiResourcesModelProvider}
     */
    protected JndiResourcesModelProvider getProvider() {
        return provider;
    }

}

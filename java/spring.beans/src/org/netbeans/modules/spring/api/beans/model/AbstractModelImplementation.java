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
package org.netbeans.modules.spring.api.beans.model;

import org.netbeans.modules.spring.spi.beans.SpringModelProviderFactory;
import org.netbeans.modules.spring.spi.beans.SpringModelProvider;
import java.util.Collection;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;

import org.openide.util.Lookup;

/**
 * Abstract model for holding fields with Spring annotation support meta data.
 * 
 * @author Martin Fousek <marfous@netbeans.org>
 */
public abstract class AbstractModelImplementation {

    private ModelUnit modelUnit;
    private SpringModel model;
    private AnnotationModelHelper helper;
    private SpringModelProvider provider;

    protected AbstractModelImplementation(ModelUnit unit) {
        modelUnit = unit;
        model = new SpringModel(this);
        helper = AnnotationModelHelper.create(unit.getClassPathInfo());
        Collection<? extends SpringModelProviderFactory> factories =
                Lookup.getDefault().lookupAll(SpringModelProviderFactory.class);
        for (SpringModelProviderFactory factory : factories) {
            provider = factory.createSpringModelProvider(this);
            if (provider != null) {
                break;
            }
        }

    }

    /**
     * Returns {@code ModelUnit} of the Model.
     * @return {@code ModelUnit} with classpath informations
     */
    public ModelUnit getModelUnit() {
        return modelUnit;
    }

    /**
     * Returns specific implementation of {@code SpringModel} for this Model.
     * @return specific {@code SpringModel} class
     */    
    protected SpringModel getModel() {
        return model;
    }

    /**
     * Returns {@code AnnotationModelHelper} of the Model.
     * @return {@code AnnotationModelHelper} of the Model
     */    
    public AnnotationModelHelper getHelper() {
        return helper;
    }

    /**
     * Returns {@code SpringModelProvider} for current Model.
     * @return {@code SpringModelProvider}
     */        
    protected SpringModelProvider getProvider() {
        return provider;
    }
}

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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.netbeans.modules.spring.beans.model.impl.SpringModelImplementation;

/**
 * Factory for getting or creating {@code MetadataModel<SpringModel>}.
 * 
 * @author marfous
 */
public final class SpringModelFactory {

    private static final Logger LOGGER = Logger.getLogger(SpringModelFactory.class.getName());    
    
    //protected due to JUnit tests
    protected static HashMap<ModelUnit, WeakReference<MetadataModel<SpringModel>>> 
            MODELS = new HashMap<ModelUnit, WeakReference<MetadataModel<SpringModel>>>();

    private SpringModelFactory() {
    }

    /**
     * Returns {@link MetadataModel} for given {@link ModelUnit}. If the 
     * {@code MetadataModel} isn't created and cached, new one originates.
     * 
     * @param unit {@link ModelUnit} of involved project (btw, {@code ModelUnit} can be
     * easily created by {@code SpringModelSupport}.
     * @return existing metamodel for given {@code ModelUnit} or created new one; never null
     */    
    public static synchronized MetadataModel<SpringModel> getMetaModel(ModelUnit unit) {
        WeakReference<MetadataModel<SpringModel>> reference = MODELS.get(unit);
        MetadataModel<SpringModel> metadataModel = null;
        if (reference != null) {
            metadataModel = reference.get();
        }
        if (metadataModel == null) {
            LOGGER.log(Level.FINE, "Metadata model not found in cache for model unit: {0}, reference: {1}", new Object[]{unit, reference});
            metadataModel = createMetaModel(unit);
            if (reference == null) {
                LOGGER.log(Level.FINE, "No reference found, creating new one.");
                reference = new WeakReference<MetadataModel<SpringModel>>(metadataModel);
            }
            MODELS.put(unit, reference);
        }
        return metadataModel;
    }

    /**
     * Creates new {@link MetadataModel} for given {@link ModelUnit}.
     * @param unit {@link ModelUnit} of involved project
     * @return newly created {@link MetadataModel<SrpingModel>}
     */
    public static MetadataModel<SpringModel> createMetaModel(ModelUnit unit) {
        LOGGER.log(Level.FINE, "Creating metadata model for model unit: {0}", unit);
        return MetadataModelFactory.createMetadataModel(SpringModelImplementation.createMetaModel(unit));
    }
}

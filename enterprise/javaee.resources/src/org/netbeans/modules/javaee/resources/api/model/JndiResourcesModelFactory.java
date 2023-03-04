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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.netbeans.modules.javaee.resources.impl.model.JndiResourcesModelImpl;

/**
 * Factory for obtaining of the JndiResourceModel. Entry point by getting JNDI resources.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JndiResourcesModelFactory {

    private static final Logger LOGGER = Logger.getLogger(JndiResourcesModelFactory.class.getName());

    private JndiResourcesModelFactory() {
    }

    /**
     * Creates new JndiResourcesModel for given {@link JndiResourcesModelUnit}.
     * @param unit {@link JndiResourcesModelUnit} of involved project
     * @return newly created JndiResourcesModel
     */
    public static MetadataModel<JndiResourcesModel> createMetaModel(JndiResourcesModelUnit unit) {
        LOGGER.log(Level.FINE, "Creating metadata model for model unit: {0}", unit);
        return MetadataModelFactory.createMetadataModel(JndiResourcesModelImpl.createMetaModel(unit));
    }

}

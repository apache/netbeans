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

package org.netbeans.modules.j2ee.dd.spi.client;

import org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata;
import org.netbeans.modules.j2ee.dd.impl.client.annotation.AppClientMetadataModelImpl;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;

/**
 * Factory for application client metadata.
 * @author Tomas Mysik
 */
public class AppClientMetadataModelFactory {
    
    private AppClientMetadataModelFactory() {
    }
    
    /**
     * Create metadata model of application client.
     * @param metadataUnit XXX ???
     * @return metadata model of application client.
     */
    public static MetadataModel<AppClientMetadata> createMetadataModel(MetadataUnit metadataUnit) {
        return MetadataModelFactory.createMetadataModel(new AppClientMetadataModelImpl(metadataUnit));
    }
}

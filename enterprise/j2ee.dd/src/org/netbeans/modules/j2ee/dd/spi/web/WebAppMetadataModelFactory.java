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

package org.netbeans.modules.j2ee.dd.spi.web;

import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.impl.web.metadata.WebAppMetadataModelImpl;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;

/**
 *
 * @author Andrei Badea
 */
public class WebAppMetadataModelFactory {

    private WebAppMetadataModelFactory() {}

    // merge option is ignored (metadata model controls merging by itself)
    public static MetadataModel<WebAppMetadata> createMetadataModel(MetadataUnit metadataUnit, boolean merge) {
        return MetadataModelFactory.createMetadataModel(WebAppMetadataModelImpl.create(metadataUnit));
    }
}

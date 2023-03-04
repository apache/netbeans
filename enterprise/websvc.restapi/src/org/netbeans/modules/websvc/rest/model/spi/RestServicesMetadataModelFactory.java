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

package org.netbeans.modules.websvc.rest.model.spi;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.netbeans.modules.websvc.rest.model.api.RestApplicationModel;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.model.impl.RestApplicationMetadataModelImpl;
import org.netbeans.modules.websvc.rest.model.impl.RestServicesMetadataModelImpl;

/**
 *
 * @author Milan Kuchtiak
 */
public class RestServicesMetadataModelFactory {
    
    private RestServicesMetadataModelFactory() {
    }
    
    public static RestServicesModel createMetadataModel(MetadataUnit metadataUnit, Project project) {
        RestServicesModel impl = new RestServicesMetadataModelImpl(metadataUnit, project);
        MetadataModelFactory.createMetadataModel(impl);
        return impl;
    }
    public static RestApplicationModel createApplicationMetadataModel(MetadataUnit metadataUnit, Project project) {
        RestApplicationModel impl = new RestApplicationMetadataModelImpl(metadataUnit, project);
        return impl;
    }
    
}

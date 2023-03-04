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
package org.netbeans.modules.j2ee.earproject.model;

import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.ApplicationMetadata;

/**
 * Default implementation of {@link ApplicationMetadata}.
 * @author Tomas Mysik
 */
public class ApplicationMetadataImpl implements ApplicationMetadata {
    
    private final Application application;
    
    /**
     * Constructor with all properties.
     * @param application model of enterprise application.
     */
    public ApplicationMetadataImpl(Application application) {
        this.application = application;
    }

    public Application getRoot() {
        return application;
    }
}

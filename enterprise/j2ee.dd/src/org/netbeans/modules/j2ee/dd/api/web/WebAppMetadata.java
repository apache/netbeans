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

package org.netbeans.modules.j2ee.dd.api.web;

import java.util.List;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.web.model.FilterInfo;
import org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo;
import org.openide.filesystems.FileObject;

/**
 * Interface for access metadata for web application.
 * @author Petr Slechta
 */
public interface WebAppMetadata {

    /**
     * @return object model of main web.xml deployment descriptor. Returns null
     * if web.xml is not present.
     */
    WebApp getRoot();

    /**
     * @return list of object models for web-fragment.xml deployment descriptors.
     * Never returns null.
     */
    List<WebFragment> getFragments();

    List<FileObject> getFragmentFiles();

    /**
     * @return list of objects that hold information about servlets (information
     * aggregated from web.xml file, web-fragment.xml files, and annotations).
     * Never returns null.
     */
    List<ServletInfo> getServlets();

    /**
     * @return list of objects that hold information about filters (information
     * aggregated from web.xml file, web-fragment.xml files, and annotations).
     * Never returns null.
     */
    List<FilterInfo> getFilters();

    /**
     * @return list of all defined security roles
     */
    List<String> getSecurityRoles();

    List<ResourceRef> getResourceRefs();

    List<ResourceEnvRef> getResourceEnvRefs();

    List<EnvEntry> getEnvEntries();

    List<MessageDestinationRef> getMessageDestinationRefs();

    List<ServiceRef> getServiceRefs();

    List<EjbLocalRef> getEjbLocalRefs();

    List<EjbRef> getEjbRefs();

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;

/**
 * handle data inputs and outputs in the license headers customizer panel.
 * Copied from org.netbeans.spi.project.support.ant.ui.CustomizerUtilities
 * See bug #257920
 */
public interface LicensePanelContentHandler {

    /**
     * raw, unevaluated value points to the file in project space containing the header,
     * if present takes precedence over <code>getGlobalLicenseName()</code>
     *
     * @return value or null if the value is unknown/doesn't exist
     */
    String getProjectLicenseLocation();

    /**
     * value of pointing to the global license header in the IDE.
     * @return
     */
    String getGlobalLicenseName();

    /**
     * take the value from <code>getProjectLicenseLocation</code> and evaluate it to FileObject
     *
     * @param path
     * @return FileObject instance if found, otherwise null
     */
    FileObject resolveProjectLocation(@NonNull String path);

    /**
     * new value for project location, null value allowed and is meant to remove the value (to effectively use the global license)
     * @param newLocation
     */
    void setProjectLicenseLocation(@NullAllowed String newLocation);

    /**
     * set new value of global license template
     * @param newName
     */
    void setGlobalLicenseName(@NullAllowed String newName);

    /**
     * if no <code>getProjectLicenseLocation</code> is returned, this method will return the default project location.
     * @return
     */
    String getDefaultProjectLicenseLocation();

    /**
     * set the user edited content of the license header file in project space.
     * @param text
     */
    void setProjectLicenseContent(@NullAllowed String text);
    
}

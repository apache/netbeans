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
package org.netbeans.modules.javaee.project.spi;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;

/**
 * An SPI for JavaEE project's setting access independently to the project type.
 * Projects can provide implementation of this interface in its {@link Project#getLookup lookup} to allow clients
 * to obtain and change JavaEE projects settings.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 * @author Martin Janicek <mjanicek@netbeans.org>
 *
 * @since 1.0
 */
public interface JavaEEProjectSettingsImplementation {

    /**
     * Sets {@code Profile} of the JavaEE project.
     * @param profile profile to be set
     *
     * @since 1.0
     */
    void setProfile(Profile profile);

    /**
     * Gets {@code Profile} of the JavaEE project.
     *
     * @since 1.0
     */
    Profile getProfile();
 
    /**
     * Sets browser ID of the JavaEE project.
     * @param browserID browser ID to be set
     *
     * @since 1.4
     */
    void setBrowserID(String browserID);

    /**
     * Gets browser ID of the JavaEE project.
     *
     * @since 1.4
     */
    String getBrowserID();
 
    /**
     * Sets server instance ID of the JavaEE project.
     * @param serverInstanceID server instance ID to be set
     *
     * @since 1.5
     */
    void setServerInstanceID(String serverInstanceID);

    /**
     * Gets server instance ID of the JavaEE project.
     *
     * @since 1.5
     */
    String getServerInstanceID();
}

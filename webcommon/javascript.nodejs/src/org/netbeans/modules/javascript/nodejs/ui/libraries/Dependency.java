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

package org.netbeans.modules.javascript.nodejs.ui.libraries;

/**
 * Dependency.
 *
 * @author Jan Stola
 */
public class Dependency {
    /** Dependency types. */
    public static enum Type { REGULAR, DEVELOPMENT, OPTIONAL };
    /** Name of the package. */
    private final String name;
    /** Required version of the package. */
    private String requiredVersion;
    /** Installed version of the package. */
    private String installedVersion;

    /**
     * Creates a new {@code Dependency}.
     * 
     * @param name name of a package.
     */
    Dependency(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the package.
     * 
     * @return name of the package.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the required version of the package.
     * 
     * @return required version of the package.
     */
    public String getRequiredVersion() {
        return requiredVersion;
    }

    /**
     * Sets the required version of the package.
     * 
     * @param requiredVersion required version of the package.
     */
    public void setRequiredVersion(String requiredVersion) {
        this.requiredVersion = requiredVersion;
    }

    /**
     * Returns the installed version of the package.
     * 
     * @return installed version of the package.
     */
    public String getInstalledVersion() {
        return installedVersion;
    }

    /**
     * Sets the installed version of the package.
     * 
     * @param installedVersion installed version of the package.
     */
    public void setInstalledVersion(String installedVersion) {
        this.installedVersion = installedVersion;
    }

}

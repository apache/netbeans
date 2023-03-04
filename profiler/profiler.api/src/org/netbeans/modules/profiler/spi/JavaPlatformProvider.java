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
package org.netbeans.modules.profiler.spi;

import java.util.Map;

/**
 *
 * @author Tomas Hurka
 */
public abstract class JavaPlatformProvider {

    /**
     * @return  a descriptive, human-readable name of the platform
     */
    public abstract String getDisplayName();

    /** Gets the java platform system properties.
     * @return the java platform system properties
     */
    public abstract Map<String,String> getSystemProperties();
    
    /** Gets the java platform properties.
     * @return the java platform properties
     */
    public abstract Map<String,String> getProperties();


    /** Gets a path to java executable for specified platform. The platform passed cannot be null.
     * Errors when obtaining the java executable will be reported to the user and null will be returned.
     *
     * @param platform A JavaPlatform for which we need the java executable path
     * @return A path to java executable or null if not found
     */
    public abstract String getPlatformJavaFile();

    /**
     * @return  a unique name of the platform
     */
    public abstract String getPlatformId();
    
}

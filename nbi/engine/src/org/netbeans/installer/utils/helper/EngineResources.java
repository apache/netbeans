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

package org.netbeans.installer.utils.helper;

/**
 *
 * @author Kirill Sorokin
 */
public final class EngineResources {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private EngineResources() {
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String LOCAL_ENGINE_PATH_PROPERTY = 
            "nbi.product.local.engine.path";
    
    /*public static final String LOCAL_ENGINE_UNINSTALL_COMMAND_PROPERTY =
            "nbi.product.local.engine.uninstall.command";
    
    public static final String LOCAL_ENGINE_MODIFY_COMMAND_PROPERTY =
            "nbi.product.local.engine.modify.command";
    */
    public static final String DATA_DIRECTORY = 
            "data";
    
    public static final String ENGINE_CONTENTS_LIST = 
            DATA_DIRECTORY + "/engine.list";
    
    public static final String ENGINE_PROPERTIES_BUNDLE = 
            DATA_DIRECTORY + ".engine";
    
    @Deprecated
    public static final String ENGINE_PROPERTIES = 
            DATA_DIRECTORY + "/engine.properties";
    
    public static final String ENGINE_PROPERTIES_PATTERN = 
            "^" + DATA_DIRECTORY  + "/engine(_[a-zA-Z]+)*.properties$";//NOI18N
}

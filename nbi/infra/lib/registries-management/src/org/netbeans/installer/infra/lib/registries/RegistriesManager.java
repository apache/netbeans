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

package org.netbeans.installer.infra.lib.registries;

import java.io.File;
import java.util.Properties;
import org.netbeans.installer.utils.helper.Platform;

public interface RegistriesManager {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String ENGINE_JAR = "engine.jar";
    
    public static final String REGISTRY_XML = "registry.xml";
    public static final String BUNDLES_LIST = "bundles.list";
    
    public static final String BUNDLES = "bundles";
    
    public static final String COMPONENTS = "components";
    public static final String PRODUCTS = COMPONENTS + "/products";
    public static final String GROUPS = COMPONENTS + "/groups";
    
    public static final String TEMP = "temp";
    
    // engine operations ////////////////////////////////////////////////////////////
    File getEngine(
            final File root) throws ManagerException;
    
    void updateEngine(
            final File root,
            final File archive) throws ManagerException;
    
    // components operations ////////////////////////////////////////////////////////
    void addPackage(
            final File root,
            final File archive,
            final String parentUid,
            final String parentVersion,
            final String parentPlatforms) throws ManagerException;
    
    void removeProduct(
            final File root,
            final String uid,
            final String version,
            final String platforms) throws ManagerException;
    
    void removeGroup(
            final File root,
            final String uid) throws ManagerException;
    
    // bundles //////////////////////////////////////////////////////////////////////
    File createBundle(
            final File root,
            final Platform platform,
            final String[] components) throws ManagerException;
    
    // bundles //////////////////////////////////////////////////////////////////////
    File createBundle(
            final File root,
            final Platform platform,
            final String[] components,
            final Properties props,
            final Properties bundleProps) throws ManagerException;
    
    void deleteBundles(
            final File root) throws ManagerException;
    
    void generateBundles(
            final File root) throws ManagerException;
    
    // miscellanea //////////////////////////////////////////////////////////////////
    void initializeRegistry(
            final File root) throws ManagerException;
    
    File exportRegistry(
            final File root,
            final File destination,
            final String codebase) throws ManagerException;
            
    String generateComponentsJs(
            final File root) throws ManagerException;
    
    String generateComponentsJs(
            final File root, final File bundlesList) throws ManagerException;

    String generateComponentsJs(
            final File root, final File bundlesList, final String locale) throws ManagerException;
}

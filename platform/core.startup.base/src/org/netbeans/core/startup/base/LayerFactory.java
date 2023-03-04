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
package org.netbeans.core.startup.base;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.openide.filesystems.FileSystem;

/**
 * SPI interface to separate layered filesystem infrastructure from the I/O and environment.
 * The purpose is to separate loading / caching of the layered filesystem contents and possible
 * configuration from the environment from the layering code itself.
 * 
 * @author sdedic
 * @since 1.60
 */
public interface LayerFactory {
    /**
     * Creates an empty FS used as the base for layered filesystem.
     * @return an instance of FileSystem
     * @throws IOException 
     */
    public FileSystem   createEmptyFileSystem() throws IOException;
    
    /**
     * Loads filesystem contents from the cache.
     * @return initialized FileSystem instance
     * @throws IOException 
     */
    public FileSystem   loadCache() throws IOException;
    
    /**
     * Called to store populate the FileSystem and store its contents.
     * The method will load the FileSystem from the supplied URLs.
     * @param cache the filesystem to initialize and store
     * @param urls list of URLs used to populate the filesystem
     * @return the instance to be used from now; possibly the same as 'cache'.
     * @throws IOException 
     */
    public FileSystem   store(FileSystem cache, List<URL> urls) throws IOException;
    
    /**
     * Retrives additional layers which should be included into FS configuration
     * @param urls additional URLs
     * @return 
     */
    public List<URL>    additionalLayers(List<URL> urls);

    /**
     * SPI to create a system-dependent {@link LayerFactory}
     */
    public interface Provider {
        /**
         * Creates the LayerFactory instance. Two modes are supported:
         * <ul>
         * <li>system - settings taken from NetBeans installation, or from a shared location only. 
         * The created FileSystem will be typically shared between multiple contexts in a multi-tenant environment.
         * <li>user - settings are taken from the user storage, but based on shared ones.
         * </ul>
         * 
         * @param system true, if the instance is shared
         * @return LayerFactory to create and populate the FileSystem
         */
        public LayerFactory     create(boolean system);
    }
}

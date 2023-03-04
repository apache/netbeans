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

package org.netbeans.modules.java.preprocessorbridge.spi;

import java.io.File;
import java.util.Set;

/**
 * This class provides a SPI used by {@link RepositoryUpdater} to generate java proxies
 * for languages which form a single compilation unit with java files, for example groovy.
 * @author Tomas Zezula
 */
public interface VirtualSourceProvider {
    
    public interface Result {
        /**
         * Registers a binding among source and virtual source
         * @param source from which the virtual source was generated
         * @param packageName of the virtual source
         * @param relativeName of the virtual source without extension
         * @param content of the virtual source
         */
        public void add (File source, String packageName, String relativeName, CharSequence content);
    }
    
    /**
     * Returns a set of extensions supported by this {@link VirtualSourceProvider}
     * @return a set of supported extensions
     */
    public Set<String> getSupportedExtensions ();
    
    /**
     * If true the java indexer will index the virtual source
     * @return boolean
     */
    public boolean index ();

    /**
     * Return a list of tuples {fully qualified name, file content} as a result of
     * transformation of given files.
     * @param files to be transformed
     * @param sourceRoot containing the sources
     * @param  result of transformation
     */
    public void translate (Iterable<File> files, File sourceRoot, Result result);
}

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

package org.netbeans.spi.project.libraries;

import java.net.URI;
import java.util.List;

/**
 * Library enhancement allowing setting/getting library content as URI list.
 * Useful for example for storing relative library entries.
 * 
 * @since org.netbeans.modules.project.libraries/1 1.18
 */
public interface LibraryImplementation2 extends LibraryImplementation {

    /**
     * Returns List of resources contained in the given volume.
     * The returned list is unmodifiable. To change the content of
     * the given volume use setContent method.
     * @param volumeType the type of volume for which the content should be returned.
     * @return list of resource URIs (never null)
     * @throws IllegalArgumentException if the library does not support given type of volume
     */
    List<URI> getURIContent(String volumeType) throws IllegalArgumentException;

    /**
     * Sets content of given volume
     * @param volumeType the type of volume for which the content should be set
     * @param path the list of resource URIs
     * @throws IllegalArgumentException if the library does not support given volumeType
     */
    void setURIContent(String volumeType, List<URI> path) throws IllegalArgumentException;

}

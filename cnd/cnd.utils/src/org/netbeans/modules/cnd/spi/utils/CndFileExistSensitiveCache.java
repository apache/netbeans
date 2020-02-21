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

package org.netbeans.modules.cnd.spi.utils;

import org.openide.filesystems.FileSystem;

/**
 * service which is sensitive to file existence cache
 */
public interface CndFileExistSensitiveCache {
    /**
     * notification that all information about file existence is invalid
     */
    public void invalidateAll();
    
    /**
     * notification that information about file existence of input file is no more invalid
     * @param fileSystem  file system
     * @param file invalidated file path
     */
    public void invalidateFile(FileSystem fileSystem, String file);
}

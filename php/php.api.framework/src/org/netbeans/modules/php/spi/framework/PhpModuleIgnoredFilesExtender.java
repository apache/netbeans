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

package org.netbeans.modules.php.spi.framework;

import java.io.File;
import java.util.Set;

/**
 * Provides support for extending a PHP module's visibility, that is,
 * it allows to recommend to hide any file or folder (suitable mainly
 * for any kind of caches, private config files etc.).
 * <p>
 * Please note that such files/folders are not only recommended to be invisible
 * in different user views but are likely not e.g. scanned and indexed too.
 *
 * @author Tomas Mysik
 */
public abstract class PhpModuleIgnoredFilesExtender {

    /**
     * Get collection of ignored files. These files do not need to exist but cannot be <code>null</code>.
     * This method is frequently called so it should be very fast (or consider using any kind of cache).
     * <p>
     * <b>Warning:</b> These files must represent <b>absolute</b> path in order to prevent
     * unexpected and unwanted results.
     * @return collection of ignored files
     */
    public abstract Set<File> getIgnoredFiles();
}

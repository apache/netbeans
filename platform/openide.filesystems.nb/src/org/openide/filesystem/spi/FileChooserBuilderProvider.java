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
package org.openide.filesystem.spi;

import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileSystem;

/**
 * Provides FileSystemBuilder instance.
 * @author Danila Sergeyev
 * @since 9.11
 */
public abstract class FileChooserBuilderProvider {
    
    /**
     * Provides FileChooserBuilder for the given file system.
     * @param fileSystem A virtual file system
     * @return FileChooserBuilder related to the given file system
     */
    public abstract FileChooserBuilder createFileChooserBuilder(FileSystem fileSystem);
    
    /**
     * Provides FileChooserBuilder for the given file system. The passed key is used as a key
     * into NbPreferences to look up the directory the file chooser should
     * initially be rooted on.
     * @param fileSystem A virtual file system
     * @param dirKey A non-null ad-hoc string.  If a FileChooser was previously
     * used with the same string as is passed, then the initial directory
     * @return FileChooserBuilder related to the given file system
     */
    public abstract FileChooserBuilder createFileChooserBuilder(FileSystem fileSystem, String dirKey);
    
}

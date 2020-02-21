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
package org.netbeans.modules.cnd.discovery.api;

import org.openide.filesystems.FileSystem;

/**
 * Type safe properties
 * 
 * 
 * @param <T> property type
 */
public final class ProviderPropertyType<T> {

    public enum PropertyKind {
        BinaryFile,
        ArtifactFileSystem,
        BinaryFiles,
        SourceFile,
        MakeLogFile,
        Folder,
        Folders,
        String,
        Boolean,
        Object
    }    
    
    public static final ProviderPropertyType<String> ExecLogPropertyType = new ProviderPropertyType<>("exec-log-file", PropertyKind.MakeLogFile); //NOI18N
    public static final ProviderPropertyType<String> MakeLogPropertyType = new ProviderPropertyType<>("make-log-file", PropertyKind.MakeLogFile); //NOI18N
    public static final ProviderPropertyType<FileSystem> LogFileSystemPropertyType = new ProviderPropertyType<>("log-filesystem", PropertyKind.ArtifactFileSystem); //NOI18N
    public static final ProviderPropertyType<String> RestrictSourceRootPropertyType = new ProviderPropertyType<>("restrict_source_root", PropertyKind.String); //NOI18N
    public static final ProviderPropertyType<String> RestrictCompileRootPropertyType = new ProviderPropertyType<>("restrict_compile_root", PropertyKind.String); //NOI18N

    public static final ProviderPropertyType<String> ExecutablePropertyType = new ProviderPropertyType<>("executable", PropertyKind.BinaryFile); //NOI18N
    public static final ProviderPropertyType<FileSystem> BinaryFileSystemPropertyType = new ProviderPropertyType<>("binary-filesystem", PropertyKind.ArtifactFileSystem); //NOI18N
    public static final ProviderPropertyType<String[]> LibrariesPropertyType = new ProviderPropertyType<>("libraries", PropertyKind.BinaryFiles); //NOI18N
    public static final ProviderPropertyType<Boolean> FindMainPropertyType = new ProviderPropertyType<>("find_main", PropertyKind.Boolean); //NOI18N
    
    public static final ProviderPropertyType<String> ExecutableFolderPropertyType = new ProviderPropertyType<>("folder", PropertyKind.Folder); //NOI18N
    
    public static final ProviderPropertyType<String> ModelFolderPropertyType = new ProviderPropertyType<>("model-folder", PropertyKind.Folder); //NOI18N
    public static final ProviderPropertyType<Boolean> PreferLocalFilesPropertyType = new ProviderPropertyType<>("prefer-local", PropertyKind.Boolean); //NOI18N
    
    private final String key;
    private final PropertyKind kind;

    ProviderPropertyType(String key, PropertyKind kind) {
        this.key = key;
        this.kind = kind;
    }

    public String key() {
        return key;
    }

    public PropertyKind kind() {
        return kind;
    }

    public T getProperty(DiscoveryProvider provider) {
        return (T) provider.getProperty(key);
    }

    public void setProperty(DiscoveryProvider provider, T value) {
        provider.getProperty(key).setValue(value);
    }

    @Override
    public String toString() {
        return key+"["+kind+"]"; //NOI18N
    }
}

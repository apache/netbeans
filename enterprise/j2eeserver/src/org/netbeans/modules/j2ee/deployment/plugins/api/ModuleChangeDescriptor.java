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

package org.netbeans.modules.j2ee.deployment.plugins.api;

import java.io.File;

/**
 * This interface allows a plugin to find out what about a module
 * or application has changed since the last deployment.
 * @author  George Finklang
 */
public interface ModuleChangeDescriptor {
    /**
     * Return true if any of the standard or server specific deployment descriptors have changed.
     */
    public boolean descriptorChanged();

    /**
     * Return true if any of the standard or server specific deployment descriptors have changed.
     */
    public boolean serverDescriptorChanged();
    
    /**
     * Return true if any file changes require the module class loader refresh.
     */
    public boolean classesChanged();
    
    /**
     * Return true if the manifest.mf of the module has changed.
     */
    public boolean manifestChanged();

    /**
     * Returns distribution relative paths of changed files.
     */
    public File[] getChangedFiles();

    /**
     * Returns distribution relative paths of deleted files.
     */
    public File[] getRemovedFiles();
}

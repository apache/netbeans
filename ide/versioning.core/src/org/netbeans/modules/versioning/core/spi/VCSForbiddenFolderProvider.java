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
package org.netbeans.modules.versioning.core.spi;

import org.netbeans.modules.versioning.core.api.VCSFileProxy;


public interface VCSForbiddenFolderProvider {
    /**
     * Some folders are special and versioning should not look for metadata in
     * them. Folders like /net with automount enabled may take a long time to
     * answer I/O on their children, so
     * <code>VCSFileProxy.exists("/net/.git")</code> will freeze until it timeouts.
     * This method is called from org.netbeans.modules.versioning.core.util.Utils.isForbiddenFolder()
     * This does not mean however that whole subtree should be excluded from version control, 
     * only that you should not look for the metadata directly in this folder.
     * Returns <code>true</code> if the given folder is among such folders.
     * @author vkvashin
     * @param folder a folder to query
     * @return <code>true</code> if the given folder should be skipped when
     * searching for metadata.
     * @since 1.32.0
     */
    boolean isForbiddenFolder (VCSFileProxy folder);
}

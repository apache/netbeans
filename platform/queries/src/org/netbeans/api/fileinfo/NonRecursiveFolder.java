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

package org.netbeans.api.fileinfo;

import org.openide.filesystems.FileObject;

/**
 * Marker interface for representation of a folder without subfolders.
 * When an implementation of this interface is contained in the
 * lookup of a node, actions on that node should not process the subfolders
 * of this folder.
 * It is permitted for a Node to have both the <a href="@org-openide-loaders@/org/openide/loaders/DataFolder.html">DataFolder</a>
 * and NonRecursiveFolder in its lookup. In this case the {@link NonRecursiveFolder#getFolder} has to
 * return the same {@link FileObject} as the <a href="@org-openide-loaders@/org/openide/loaders/DataObject.html#getPrimaryFile()">DataFolder#getPrimaryFile</a> method.
 * Any action which checks for the NonRecursiveFolder at all must prefer it to
 * the DataFolder since it is considered to be more specific information.
 *
 * @author  Martin Entlicher
 * @since 1.4
 */
public interface NonRecursiveFolder {
    
    /**
     * Get the folder file object, which represents the non-recursive folder.
     * Only direct children should be processed, no sub-folders.
     * @return The file object that represents non-recursive folder.
     */
    FileObject getFolder();
    
}

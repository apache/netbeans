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

package org.netbeans.modules.cnd.api.project;

import java.util.List;

public interface NativeProjectItemsListener {

     /**
      * Called when multiple files are added to the project.
      * @param fileItems the list of file items that was added.
      */
     public void filesAdded(List<NativeFileItem> fileItems);
     
     /**
      * Called when multiple files are removed from the project.
      * @param fileItems the list of file items that was added.
      */
     public void filesRemoved(List<NativeFileItem> fileItems);
     
     /**
      * Called when include paths or macro definitions have changed (and
      * files needs to be re-parsed) for multiple files.
      * @param fileItems the list of file items that has changed.
      */
     public void filesPropertiesChanged(List<NativeFileItem> fileItems);
     
     /**
      * Called when include paths or macro definitions have changed (and
      * files needs to be re-parsed) for all files in project.
      * @param nativeProject project whose properties have changed
      */
     public void filesPropertiesChanged(NativeProject nativeProject);

     /**
      * Called when item name is changed.
      * @param oldPath the old file path.
      * @param newFileIetm the new file item.
      */
    void fileRenamed(String oldPath, NativeFileItem newFileIetm);
    
    /**
     * Called when the project is deleted
     * @param nativeProject project that is closed
     */
    void projectDeleted(NativeProject nativeProject);
    
    /**
     * notifies about intensive file operations to be started.
     * @param nativeProject 
     */
    void fileOperationsStarted(NativeProject nativeProject);

    /**
     * notifies about intensive file operations finished.
     * @param nativeProject
     */
    void fileOperationsFinished(NativeProject nativeProject);
}

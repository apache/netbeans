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
package org.netbeans.modules.cnd.makeproject.api.configurations;

/**
 * Allows view provider to update view
 * 
 */
public interface MakeLogicalViewModel {
    /**
     * Item was added or updated.
     * 
     * @param folder
     * @param item 
     */
    void checkForChangedViewItemNodes(final Folder folder, final Item item);

    /**
     * Some Item names was changed.
     */
    void checkForChangedName();

    /**
     * Broken items may become valid.
     */
    void refreshBrokenItems();

    /**
     * Project configuration was significant changed.
     * 
     * @param delta 
     */
    public void checkForChangedViewItemNodes(ConfigurationDescriptorProvider.SnapShot delta);
}

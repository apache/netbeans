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

package org.netbeans.modules.bugtracking.spi;

import java.awt.Image;

/**
 * Represents information related to one particular issue priority. 
 * The Priority attributes are used in various Task Dashboard features 
 * - e.g. Icon is shown next to an Issue, etc.
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public final class IssuePriorityInfo {
    private final String id;
    private final String displayName;
    private final Image icon;

    /**
     * Creates a IssuePriorityInfo. 
     * Note that when no icon is provided the Tasks Dashboard will 
     * use default icons given by the order of Priority infos returned
     * via {@link IssuePriorityProvider#getPriorityInfos()}
     * 
     * @param id - priority id as given by the particular implementation
     * @param displayName - priority name as given by the particular implementation
     * @see IssuePriorityProvider#getPriorityInfos() 
     * @since 1.85
     */
    public IssuePriorityInfo(String id, String displayName) {
        this(id, displayName, null);
    }
    
    /**
     * Creates a IssuePriorityInfo. 
     * 
     * @param id - priority id as given by the particular implementation
     * @param displayName - priority name as given by the particular implementation
     * @param icon - priority icon as given by the particular implementation
     * @see IssuePriorityProvider#getPriorityInfos() 
     * @since 1.85
     */
    public IssuePriorityInfo(String id, String displayName, Image icon) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
    }

    /**
     * Returns the display name for this Priority.
     * 
     * @return display name associated with this Priority
     * @since 1.85
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the icon to be shown next to an Issue in the Tasks Dashboard. 
     * 
     * @return icon associated with this Priority
     * @see IssuePriorityProvider#getPriorityInfos()
     * @since 1.85
     */
    public Image getIcon() {
        return icon;
    }

    /**
     * Returns a unique id for this Priority.
     * 
     * @return a unique id
     * @since 1.85
     */
    public String getID() {
        return id;
    }
}

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

package org.netbeans.modules.tasklist.trampoline;

import java.awt.Image;
import java.util.List;

/**
 * A class that describes Task group, e.g. Error, Warning, TODO etc. Task groups are
 * visible to the user in Task List's window.
 * 
 * @author S. Aubrecht
 */
public final class TaskGroup implements Comparable<TaskGroup> {
    
    private String name;
    private String displayName;
    private String description;
    private Image icon;
    private int index;
    
    /** 
     * Creates a new instance of TaskGroup
     *  
     * @param name Group's id
     * @param displayName Group's display name
     * @param description Group's description (for tooltips)
     * @param icon Group's icon
     */
    public TaskGroup( String name, String displayName, String description, Image icon ) {
        assert null != name;
        assert null != displayName;
        assert null != icon;
        
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }
    
    /**
     * @return List of all available TaskGroups.
     */
    public static List<? extends TaskGroup> getGroups() {
        return TaskGroupFactory.getDefault().getGroups();
    }
    
    /**
     * @return Identification of the group.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return Group's display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * @return Group's description (for tooltips etc)
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @return Group's icon.
     */
    public Image getIcon() {
        return icon;
    }

    public int compareTo( TaskGroup otherGroup ) {
        return index - otherGroup.index;
    }
    
    void setIndex( int index ) {
        this.index = index;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        final TaskGroup test = (TaskGroup) o;

        if (this.name != test.name && this.name != null &&
            !this.name.equals(test.name))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}

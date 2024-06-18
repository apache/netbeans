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
package org.netbeans.modules.cloud.oracle.items;

import java.util.Objects;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

/**
 * Represents Oracle Cloud Resource identified by Oracle Cloud Identifier (OCID)
 * 
 * @author Jan Horvath
 */
public abstract class OCIItem {
    final OCID id;
    final String name;
    final String compartmentId;
    String description;
    transient ChangeSupport changeSupport;

    /**
    * Construct a new {@code OCIItem}.
    * 
    * @param id OCID of the item
    * @param compartmentId OCID of the compartmentId
    * @param name Name of the item
    */
    public OCIItem(OCID id, String compartmentId, String name) {
        this.id = id;
        this.name = name;
        this.compartmentId = compartmentId;
        changeSupport = new ChangeSupport(this);
    }

    public OCIItem() {
        this(null, null, null);
    }
    
    /**
     * OCID of the item.
     * 
     * @return OCID of the item
     */
    public OCID getKey() {
        return id;
    }

    /**
     * Name of the item
     * 
     * @return Name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns short description of the item.
     * 
     * @return Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * OCID of the compartmentId.
     * 
     * @return OCID of the compartmentId
     */
    public String getCompartmentId() {
        return compartmentId;
    }
    
    /**
     * Short description of the item.
     * 
     * @return Name
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Triggers node refresh.
     */
    public void refresh() {
        changeSupport.fireChange();
    }
    
    /**
     * Adds a <code>ChangeListener</code> to the listener list.
     * 
     * @param listener the <code>ChangeListener</code> to be added.
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    /**
     * Removes a <code>ChangeListener</code> from the listener list.
     * 
     * @param listener the <code>ChangeListener</code> to be removed.
     */
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public int maxInProject() {
        return 0;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.id);
        hash = 59 * hash + Objects.hashCode(this.name);
        hash = 59 * hash + Objects.hashCode(this.description);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OCIItem other = (OCIItem) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        return Objects.equals(this.id, other.id);
    }
    
    
}

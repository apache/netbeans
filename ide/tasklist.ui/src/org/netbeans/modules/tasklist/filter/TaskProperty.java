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

package org.netbeans.modules.tasklist.filter;

import org.netbeans.spi.tasklist.Task;
import org.openide.util.NbBundle;



/**
 * Lightweight property for indirect access to suggestion
 * properties. Replaces both reflection and property-getter-dispatchers
 * in filters and view columns. Represents an API to add properties to
 * task views and filters. We don't like bean properties and reflection
 * for effectivity reasons.
 *
 * A property serves to extract the value it represents from
 * a given Suggestion.
 *
 * Properties for different views/filters/... are difined in factories
 * named in plural like SuggestionProperties, TaskProperties, etc.
 */
abstract class TaskProperty {
    protected TaskProperty(String id, Class valueClass) {
        this.id = id;
    }
    
    public String getID() { return id;}
    
    /**
     * Returns human readable name of this property. The name is
     * retrieved from the bundle stored in the same directory as
     * the real class of this property with the key:
     * "LBL_" + getID() + "Property".
     * @return localized String
     */
    public String getName() {
        if (name == null) {
            name = NbBundle.getMessage(this.getClass(), "LBL_" + id + "Property"); //NOI18N //NOI18N
        }
        return name;
    }
    
    
    /**
     * Extract the value represented by this property from the given
     * suggestion.
     * @param obj the Suggestion to extract from
     * @return Object value extracted
     */
    public abstract Object getValue(Task t);
    
    public String toString() { return id;}
    
    /**
     * Returns class of values of this property.
     * @return Class
     */
    public Class getValueClass() { return valueClass;}
    
    
    ///////
    private String id;
    private transient String name;
    private Class valueClass;
}


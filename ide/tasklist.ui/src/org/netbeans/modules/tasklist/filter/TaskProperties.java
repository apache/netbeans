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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.modules.tasklist.impl.Accessor;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;



/**
 * An abstract factory for creating SuggestionProperties from their id.
 */
class TaskProperties {
    public static final String PROPID_GROUP = "group"; //NOI18N
    public static final String PROPID_DESCRIPTION = "description"; //NOI18N
    public static final String PROPID_FILE = "file"; //NOI18N
    public static final String PROPID_LOCATION = "location"; //NOI18N
    
    /**
     * A factory method for properties on Suggestion.
     * @param propID one of the PROP_* constant defined in this class
     * @return a property for accessing the property
     */
    public static TaskProperty getProperty(String propID) {
        if( propID.equals(PROPID_GROUP) ) {
            return PROP_GROUP;
        } else if( propID.equals(PROPID_DESCRIPTION) ) {
            return PROP_DESCRIPTION;
        } else if( propID.equals(PROPID_FILE) ) {
            return PROP_FILE;
        } else if( propID.equals(PROPID_LOCATION) ) {
            return PROP_LOCATION;
        } else {
            throw new IllegalArgumentException("Unresolved property id " + propID); //NOI18N
        }
    }
    
    
    public static TaskProperty PROP_GROUP = new TaskProperty(PROPID_GROUP, TaskGroup.class) {
        public Object getValue(Task t) {
            return Accessor.getGroup(t);
        }
    };
    
    public static TaskProperty PROP_DESCRIPTION  = new TaskProperty(PROPID_DESCRIPTION, String.class) {
        public Object getValue(Task t) {
            return Accessor.getDescription(t);
        }
    };
    
    public static TaskProperty PROP_FILE = new TaskProperty(PROPID_FILE, String.class) {
        public Object getValue(Task t) {
            return Accessor.getFileNameExt(t);
        }
    };
    
    public static TaskProperty PROP_LOCATION = new TaskProperty(PROPID_LOCATION, String.class) {
        public Object getValue(Task t) {
            return Accessor.getLocation(t);
        }
    };
}


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
package org.netbeans.modules.db.dataview.meta;

/**
 * @author Ahimanikya Satapathy
 */
public abstract class DBObject<Parent> {

    protected transient String displayName;
    protected transient Parent parentObject;

    public String getDisplayName() {
        return this.displayName;
    }

    public Parent getParentObject() {
        return this.parentObject;
    }

    public void setDisplayName(String newName) {
        displayName = (newName != null) ? newName.trim() : ""; // NOI18N
    }

    public void setParentObject(Parent newParent) {
        this.parentObject = newParent;
    }

    static boolean isNullString(String str) {
        return (str == null || str.trim().length() == 0);
    }
}


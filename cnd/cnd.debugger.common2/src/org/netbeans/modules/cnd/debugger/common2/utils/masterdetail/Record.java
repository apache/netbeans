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

package org.netbeans.modules.cnd.debugger.common2.utils.masterdetail;

/**
 * Record
 */
public interface Record {

    /**
     * Get the key for this record
     */
    public String getKey();

    /**
     * Set the key for this record.
     */
    public void setKey(String key);

    /**
     * Return true if this Record's key matches the given 'key'.
     */
    public boolean matches(String key);

    /**
     * Return true if Record is the archetypal Record.
     *
     * The archetypal Record is the one from which the first duplicate is made.
     * It is always present and is constrained to always show at the top.
     * It cannot be deleted.
     * It's characterising property, usually it's key, cannot be set or edited.
     */
    public boolean isArchetype();

    /**
     * Returns a String representation of this record to be used
     * for displaying the record.
     */
    public String displayName();

    public String getHostName();

    /**
     * Return a clone (copy) of this record
     */
    public Record cloneRecord();
}

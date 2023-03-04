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

package org.netbeans.modules.css.refactoring.api;

/**
 *
 * @author mfukala@netbeans.org
 */
public final class EntryHandle {

    private Entry entry;
    private boolean isRelatedEntry;

    public static EntryHandle createEntryHandle(Entry entry, boolean isRelatedEntry) {
        return new EntryHandle(entry).setIsRelatedEntry(isRelatedEntry);
    }

    private EntryHandle(Entry entry) {
        this.entry = entry;
    }

    public Entry entry() {
        return entry;
    }

    public boolean isRelatedEntry() {
        return isRelatedEntry;
    }

    private EntryHandle setIsRelatedEntry(boolean isRelatedEntry) {
        this.isRelatedEntry = isRelatedEntry;
        return this;
    }

    @Override
    public String toString() {
        return "EntryHandle[isRelated=" + isRelatedEntry() +
                ", entry=" + entry() +
                "]"; //NOI18N
    }


}

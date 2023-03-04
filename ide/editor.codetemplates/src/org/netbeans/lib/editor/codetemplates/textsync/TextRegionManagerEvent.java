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

package org.netbeans.lib.editor.codetemplates.textsync;

import java.util.Collections;
import java.util.EventObject;
import java.util.List;

/**
 *
 * @author mmetelka
 */
public final class TextRegionManagerEvent extends EventObject {

    private final boolean focusChange;

    private final List<TextSyncGroup<?>> removedGroups;

    private final TextSync previousTextSync;


    TextRegionManagerEvent(TextRegionManager source, boolean focusChange,
            List<TextSyncGroup<?>> removedGroups, TextSync previousTextSync
    ) {
        super(source);
        this.focusChange = focusChange;
        this.removedGroups = (removedGroups != null) ? removedGroups : Collections.<TextSyncGroup<?>>emptyList();
        this.previousTextSync = previousTextSync;
    }

    public TextRegionManager textRegionManager() {
        return (TextRegionManager)getSource();
    }

    public boolean isFocusChange() {
        return focusChange;
    }

    public <T> List<TextSyncGroup<T>> removedGroups() {
        @SuppressWarnings("unchecked")
        List<TextSyncGroup<T>> ret = (List<TextSyncGroup<T>>)(List)removedGroups;
        return ret;
    }

    public TextSync previousTextSync() {
        return previousTextSync;
    }

    /**
     * Returns text sync for which the document modifications are being replicated across the respective regions.
     * 
     * @return active text sync, can be {@code null}.
     * 
     * @since 1.53
     */
    public TextSync activeTextSync() {
        return textRegionManager().activeTextSync();
    }

}

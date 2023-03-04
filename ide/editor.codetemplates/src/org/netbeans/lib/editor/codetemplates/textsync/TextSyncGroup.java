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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Group of text syncs that can be traversed by TAB (Shift-TAB) keys. ENTER may
 * be used to activate a special caret text sync.
 * <br/>
 * {@link TextRegionManager} can maintain multiple groups but only one of them
 * may be active at a given time.
 *
 * @author Miloslav Metelka
 */
public final class TextSyncGroup<I> {
    
    private TextRegionManager manager;
    
    private List<TextSync> textSyncs;
    
    private int activeTextSyncIndex = -1;

    /**
     * Client-specific information.
     */
    private I clientInfo;

    public TextSyncGroup(TextSync... textSyncs) {
        initTextSyncs(textSyncs.length);
        for (TextSync textSync : textSyncs)
            addTextSync(textSync);
    }
    
    public TextSyncGroup() {
        initTextSyncs(4);
    }

    private void initTextSyncs(int size) {
        this.textSyncs = new ArrayList<TextSync>(size);
    }
    
    /**
     * Get list of all text syncs managed by this group.
     *
     * @return non-null unmodifiable list of text syncs.
     */
    public List<TextSync> textSyncs() {
        return Collections.unmodifiableList(textSyncs);
    }
    
    public void addTextSync(TextSync textSync) {
        if (textSync == null)
            throw new IllegalArgumentException("textSync cannot be null");
        if (textSync.group() != null)
            throw new IllegalArgumentException("textSync " + textSync + // NOI18N
                    " already assigned to group " + textSync.group()); // NOI18N
        textSyncs.add(textSync);
        textSync.setGroup(this);
        
    }

    public void removeTextSync(TextSync textSync) {
        if (textSyncs.remove(textSync)) {
            textSync.setGroup(null);
        }
    }

    List<TextSync> textSyncsModifiable() {
        return textSyncs;
    }

    public TextRegionManager textRegionManager() {
        return manager;
    }
    
    void setTextRegionManager(TextRegionManager manager) {
        this.manager = manager;
    }

    public I clientInfo() {
        return clientInfo;
    }

    public void setClientInfo(I clientInfo) {
        this.clientInfo = clientInfo;
    }

    public TextSync activeTextSync() {
        return (activeTextSyncIndex >= 0 && activeTextSyncIndex < textSyncs.size())
                ? textSyncs.get(activeTextSyncIndex)
                : null;
    }

    public int activeTextSyncIndex() {
        return activeTextSyncIndex;
    }

    void setActiveTextSyncIndex(int activeTextSyncIndex) {
        this.activeTextSyncIndex = activeTextSyncIndex;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(textSyncs.size() * 50 + 2);
        sb.append(super.toString());
        if (clientInfo != null) {
            sb.append(" ").append(clientInfo);
        }
        sb.append('\n');
        for (TextSync ts : textSyncs) {
            sb.append("  ").append(ts).append('\n');
        }
        return sb.toString();
    }

}

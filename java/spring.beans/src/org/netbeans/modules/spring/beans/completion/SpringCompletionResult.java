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

package org.netbeans.modules.spring.beans.completion;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Rohan Ranade
 */
public final class SpringCompletionResult {
    private final List<SpringXMLConfigCompletionItem> items;
    private final int anchorOffset;
    private final boolean additionalItems;
    private final String additionalItemsText;
    
    public static final SpringCompletionResult NONE = new SpringCompletionResult(Collections.<SpringXMLConfigCompletionItem>emptyList(), -1, false, null);
    
    public static SpringCompletionResult create(List<SpringXMLConfigCompletionItem> items, int anchorOffset, boolean hasAdditionalItems, String additionalItemsText) {
        if(items.isEmpty()) {
            return SpringCompletionResult.NONE;
        }
        
        return new SpringCompletionResult(items, anchorOffset, hasAdditionalItems, additionalItemsText);
    }
    
    public static SpringCompletionResult create(List<SpringXMLConfigCompletionItem> items, int anchorOffset) {
        return create(items, anchorOffset, false, null);
    }
    
    private SpringCompletionResult(List<SpringXMLConfigCompletionItem> items, int anchorOffset, boolean hasAdditionalItems, String additionalItemsText) {
        this.items = items;
        this.anchorOffset = anchorOffset;
        this.additionalItems = hasAdditionalItems;
        this.additionalItemsText = additionalItemsText;
    }

    public List<SpringXMLConfigCompletionItem> getItems() {
        return items;
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }

    public String getAdditionalItemsText() {
        return additionalItemsText;
    }

    public boolean hasAdditionalItems() {
        return additionalItems;
    }
}

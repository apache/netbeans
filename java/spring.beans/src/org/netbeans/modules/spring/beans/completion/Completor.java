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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public abstract class Completor {

    private volatile List<SpringXMLConfigCompletionItem> cache = new ArrayList<SpringXMLConfigCompletionItem>();
    private volatile int anchorOffset = -1;
    private final int invocationOffset;
    private volatile boolean hasAdditionalItems = false;
    private volatile boolean cancelled = false;

    protected Completor(int invocationOffset) {
        this.invocationOffset = invocationOffset;
    }

    public final SpringCompletionResult complete(CompletionContext context) {
        try {
            anchorOffset = initAnchorOffset(context);
            compute(context);
            return SpringCompletionResult.create(cache, anchorOffset, hasAdditionalItems, getAdditionalItemsText());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return SpringCompletionResult.NONE;
    }

    protected abstract int initAnchorOffset(CompletionContext context);

    protected abstract void compute(CompletionContext context) throws IOException;

    public boolean canFilter(CompletionContext context) {
        return false;
    }

    public final SpringCompletionResult filter(CompletionContext context) {
        return SpringCompletionResult.create(doFilter(context), anchorOffset, hasAdditionalItems, getAdditionalItemsText());
    }

    protected List<SpringXMLConfigCompletionItem> doFilter(CompletionContext context) {
        throw new UnsupportedOperationException("No default implementation"); // NOI18N
    }

    protected final void addCacheItem(SpringXMLConfigCompletionItem item) {
        cache.add(item);
    }

    protected List<SpringXMLConfigCompletionItem> getCacheItems() {
        return cache;
    }

    protected final int getAnchorOffset() {
        return anchorOffset;
    }

    protected final boolean isCancelled() {
        return cancelled;
    }

    protected final void cancel() {
        cancelled = true;
    }

    protected final int getInvocationOffset() {
        return invocationOffset;
    }

    protected String getAdditionalItemsText() {
        return "";
    }

    protected void setAdditionalItems(boolean additionalItems) {
        hasAdditionalItems = additionalItems;
    }
}

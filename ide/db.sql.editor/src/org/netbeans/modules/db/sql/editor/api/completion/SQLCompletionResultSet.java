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

package org.netbeans.modules.db.sql.editor.api.completion;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author Andrei Badea
 */
public class SQLCompletionResultSet {

    private final List<CompletionItem> items = new CopyOnWriteArrayList<CompletionItem>();
    private volatile int anchorOffset;

    public static SQLCompletionResultSet create() {
        return new SQLCompletionResultSet();
    }

    private SQLCompletionResultSet() {
    }

    public List<CompletionItem> getItems() {
        return items;
    }

    public void addItem(CompletionItem item) {
        items.add(item);
    }

    public void addAllItems(Collection<? extends CompletionItem> toAdd) {
        items.addAll(toAdd);
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }

    public void setAnchorOffset(int anchorOffset) {
        this.anchorOffset = anchorOffset;
    }
}

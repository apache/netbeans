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
package org.netbeans.modules.editor.bookmarks;

import java.util.Collections;
import java.util.List;
import org.netbeans.lib.editor.util.GapList;

/**
 * History of visited bookmarks.
 *
 * @author Miloslav Metelka
 */
public class BookmarkHistory {
    
    private static BookmarkHistory INSTANCE = new BookmarkHistory();
    
    public static BookmarkHistory get() {
        return INSTANCE;
    }
    
    private final List<BookmarkInfo> history = new GapList<BookmarkInfo>();

    private BookmarkHistory() {
    }
    
    public synchronized List<BookmarkInfo> historyBookmarks() {
        return Collections.unmodifiableList(history);
    }
    
    public synchronized void add(BookmarkInfo info) {
        history.remove(info); // Remove if inside the list already
        history.add(info);
    }
    
    public synchronized void remove(BookmarkInfo info) {
        history.remove(info);
    }
    
    public synchronized void remove(ProjectBookmarks projectBookmarks) {
        for (int i = history.size() - 1; i >= 0; i--) {
            if (history.get(i).getFileBookmarks().getProjectBookmarks() == projectBookmarks) {
                history.remove(i);
            }
        }
    }

    
}

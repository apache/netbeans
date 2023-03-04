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
package org.netbeans.modules.editor.lib2.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Offset range describing change in particular view factory.
 * <br>
 * Each factory may fire a list of changes of different types at once.
 * 
 * @author Miloslav Metelka
 */
public final class EditorViewFactoryChange {
    
    private final int startOffset;
    
    private final int endOffset;
    
    private final Type type;

    public static EditorViewFactoryChange create(int startOffset, int endOffset, Type type) {
        return new EditorViewFactoryChange(startOffset, endOffset, type);
    }
    
    public static List<EditorViewFactoryChange> createList(int startOffset, int endOffset, Type type) {
        return Collections.singletonList(create(startOffset, endOffset, type));
    }

    public static List<EditorViewFactoryChange> createList(EditorViewFactoryChange... changes) {
        return Arrays.asList(changes);
    }

    private EditorViewFactoryChange(int startOffset, int endOffset, Type type) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.type = type;
    }
    
    int getStartOffset() {
        return startOffset;
    }
    
    int getEndOffset() {
        return endOffset;
    }
    
    Type getType() {
        return type;
    }
    
    public enum Type {
    
        /**
         * Characters may have their coloring changed but paragraph views can be retained.
         * Mark local views as dirty but they can be recomputed later when it's necessary
         * to display them.
         */
        CHARACTER_CHANGE,
        /**
         * Rebuild paragraph views in the given area as soon as possible since they might change
         * their vertical spans (e.g. due to fold collapse/expand).
         */
        PARAGRAPH_CHANGE,
        /**
         * Rebuild all views in the given area from scratch (typically whole document)
         * due to some global change e.g. change in settings (line height changed etc.).
         */
        REBUILD
    
    }

    @Override
    public String toString() {
        return getType() + ":<" + getStartOffset() + "," + getEndOffset() + ">"; // NOI18N
    }

}

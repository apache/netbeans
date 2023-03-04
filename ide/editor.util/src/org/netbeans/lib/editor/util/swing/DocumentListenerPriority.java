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

package org.netbeans.lib.editor.util.swing;

/**
* Priorities of firing of document listeners being added to a document.
*
* @author Miloslav Metelka
* @since 1.4
*/

public final class DocumentListenerPriority {

    /**
     * Level that gets notified first (before all other levels).
     * <br/>
     * It may be used in cooperation with other levels to pre-mark that there was
     * a document modification performed that may change some of the data
     * that will be updated later on another level. For example the view hierarchy
     * may mark that it is temporarily invalid until its update will be triggered
     * on {@link #VIEW} level.
     */
    public static final DocumentListenerPriority FIRST
            = new DocumentListenerPriority(6, "first"); // NOI18N

    /**
     * Lexer gets notified early to allow other levels to use the udpated
     * token list.
     */
    public static final DocumentListenerPriority LEXER
            = new DocumentListenerPriority(5, "lexer"); // NOI18N

    /**
     * Fold update gets notified prior default level.
     */
    public static final DocumentListenerPriority FOLD_UPDATE
            = new DocumentListenerPriority(4, "fold-update"); // NOI18N

    /**
     * Default level is used for all listeners added
     * by regular {@link javax.swing.text.Document#addDocumentListener(
     * javax.swing.event.DocumentListener)} method.
     */
    public static final DocumentListenerPriority DEFAULT
            = new DocumentListenerPriority(3, "default"); // NOI18N

    /**
     * Views are updated after default level and prior caret gets updated.
     * @since 1.11
     */
    public static final DocumentListenerPriority VIEW
            = new DocumentListenerPriority(2, "view"); // NOI18N

    /**
     * Caret udpate gets notified as last.
     */
    public static final DocumentListenerPriority CARET_UPDATE
            = new DocumentListenerPriority(1, "caret-update"); // NOI18N

    /**
     * Udpate that follows caret update.
     * @since 1.6
     */
    public static final DocumentListenerPriority AFTER_CARET_UPDATE
            = new DocumentListenerPriority(0, "after-caret-update"); // NOI18N

    static final DocumentListenerPriority[] PRIORITIES = new DocumentListenerPriority[] {
        AFTER_CARET_UPDATE, CARET_UPDATE, VIEW, DEFAULT, FOLD_UPDATE, LEXER, FIRST
    };
    
    private int priority;
    
    private String description;

    /**
     * Construct new DocumentListenerPriority.
     *
     * @param priority higher priority means sooner firing.
     * @param description textual description of the priority.
     */
    private DocumentListenerPriority(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }

    public String toString() {
        return getDescription();
    }

    /**
     * Get the integer priority for the purpose of adding/removing
     * of the listener with this priority.
     */
    int getPriority() {
        return priority;
    }

}

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

package org.netbeans.api.lexer;

/**
 * Token hierarchy event type determines the reason
 * why token hierarchy modification described by {@link TokenHierarchyEvent}
 * happened.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public enum TokenHierarchyEventType {

    /**
     * Modification (insert/remove) of the characters
     * in the underlying character sequence was performed.
     */
    MODIFICATION,

    /**
     * Explicit relexing of a part of the token hierarchy
     * without any text modification.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source.
     * <br/>
     * This is not actively used yet (no API support yet).
     */
    RELEX,

    /**
     * Complete rebuild of the token hierarchy.
     * <br/>
     * This may be necessary because of any changes
     * in input attributes that influence the lexing.
     * <br/>
     * Only the removed tokens will be notified.
     * There will be no added tokens because they will be created lazily when asked by clients.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source.
     */
    REBUILD,

    /**
     * Token hierarchy became inactive (while being active before) or vice versa.
     * <br/>
     * Current activity state can be determined by {@link TokenHierarchy#isActive()}.
     * <br/>
     * A maintainer of the given mutable input source may decide to activate/deactivate
     * token hierarchy by using {@link org.netbeans.spi.lexer.TokenHierarchyControl#setActive(boolean)}.
     * For example if a Swing docuemnt is not showing and it has not been edited for a long time
     * its token hierarchy may be deactivated to save memory. Once the hierarchy
     * gets deactivated the clients should drop all the functionality depending
     * on the tokens (for example not provide a token-dependent syntax highlighting).
     * <br/>
     * Only the removed tokens will be notified in case the hierarchy becomes inactive.
     * <br/>
     * There will be no added tokens notified in case the the hierarchy becomes active because
     * the tokens will be created lazily when asked by clients.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source. Only the initial (automatic) activation
     * of the mutable token hierarchy will happen under the read lock of the client
     * asking for <code>TokenHierarchy.tokenSequence()</code> or a similar method
     * that leads to automatic activation.
     */
    ACTIVITY,
        
    /**
     * Custom language embedding was created by
     * {@link TokenSequence#createEmbedding(Language,int,int)}.
     * <br/>
     * The {@link TokenHierarchyEvent#tokenChange()} contains the token
     * where the embedding was created and the embedded change
     * {@link TokenChange#embeddedChange(int)} that describes the added
     * embedded language.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source.
     */
    EMBEDDING_CREATED,
    
    /**
     * Custom language embedding was removed by
     * {@link TokenSequence#removeEmbedding(Language)}.
     * <br/>
     * The {@link TokenHierarchyEvent#tokenChange()} contains the token
     * where the embedding was created and the embedded change
     * {@link TokenChange#embeddedChange(int)} that describes the added
     * embedded language.
     * <br/>
     * This change is notified under modification lock (write lock)
     * of the corresponding input source.
     */
    EMBEDDING_REMOVED,
    
    /**
     * Notification that result of
     * {@link TokenHierarchy#languagePaths()} has changed.
     * <br/>
     * This change may be notified under both read and write lock
     * of the corresponding input source.
     */
    LANGUAGE_PATHS;

}

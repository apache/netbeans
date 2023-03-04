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

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;

/**
 * Mutable attributed character sequence allowing to listen for changes in its text.
 *
 * <p>
 * The input can temporarily be made inactive which leads to dropping
 * of all the present tokens for the input until the input becomes
 * active again.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class MutableTextInput<I> {

    private TokenHierarchyControl<I> thc;

    /**
     * Get the language suitable for lexing of this input.
     *
     * @return language language by which the text of this input should be lexed.
     *  <br/>
     *  This method is only checked upon creation of token hierarchy.
     *  <br/>
     *  If this method returns null the token hierarchy cannot be created
     *  and will be returned as null upon asking until this method will return
     *  non-null value.
     */
    protected abstract Language<?> language();

    /**
     * Get the character sequence provided and maintained by this input.
     */
    protected abstract CharSequence text();
    
    /**
     * Get lexer-specific information about this input
     * or null if there is no specific information.
     * <br>
     * The attributes are typically retrieved by the lexer.
     */
    protected abstract InputAttributes inputAttributes();
    
    /**
     * Get object that logically provides the text
     * for this mutable text input.
     * <br/>
     * For example it may be a swing text document instance
     * {@link javax.swing.text.Document} in case the result of {@link #text()}
     * is the content of the document.
     *
     * @return non-null mutable input source.
     */
    protected abstract I inputSource();

    /**
     * Called by infrastructure to check if the underlying input source is currently
     * read-locked by the current thread.
     * <br/>
     * This method is only called when turning on the logger for token hierarchy updates:
     * <code>-J-Dorg.netbeans.lib.lexer.TokenHierarchyUpdate.level=FINE</code>
     * <br/>
     * It is expected that if write-lock means read-lock too i.e. if {@link #isWriteLocked()}
     * returns true that this method will also return true automatically.
     * <br/>
     * The following operations require read-locking:
     * <ul>
     *   <li>Creation and using of token sequence {@link org.netbeans.api.lexer.TokenHierarchy#tokenSequence()}.</li>
     *   <li>Creation of token sequence list {@link org.netbeans.api.lexer.TokenHierarchy#tokenSequenceList(LanguagePath,int,int)}.</li>
     * </ul>
     * 
     * @return true if the underlying input source is read-locked by the current thread
     * (or if unsure e.g. if there is a read-lock present but it's not possible
     *  to determine whether it was this or other thread that performed the locking).
     *  <br/>
     *  Returning false means that the input source is surely unlocked which will be
     *  reported as a serious error by the infrastructure.
     */
    protected abstract boolean isReadLocked();
    
    /**
     * Called by infrastructure to check if the underlying input source is currently
     * write-locked by the current thread.
     * <br/>
     * This method is only called when turning on the logger for token hierarchy updates:
     * <code>-J-Dorg.netbeans.lib.lexer.TokenHierarchyUpdate.level=FINE</code>
     * <br/>
     * The following operations require write-locking:
     * <ul>
     *   <li>Text modification {@link org.netbeans.spi.lexer.TokenHierarchyControl#textModified(int,int,CharSequence,int)}</li>
     *   <li>Creation of custom embedding by {@link org.netbeans.api.lexer.TokenSequence#createEmbedding(Language,int,int)}.</li>
     *   <li>Removal of custom embedding by {@link org.netbeans.api.lexer.TokenSequence#removeEmbedding(Language)}.</li>
     * </ul>
     * 
     * @return true if the underlying input source is write-locked by the current thread
     * (or if unsure).
     *  Returning false means that the input source is surely unlocked which will be
     *  reported as a serious error by the infrastructure.
     */
    protected abstract boolean isWriteLocked();

    /**
     * Get token hierarchy control for this mutable text input.
     * <br>
     * Each mutable text input can hold it in a specific way
     * e.g. swing document can use
     * <code>getProperty(TokenHierarchyControl.class)</code>.
     */
    public final TokenHierarchyControl<I> tokenHierarchyControl() {
        synchronized (this) {
            if (thc == null) {
                thc = new TokenHierarchyControl<I>(this);
            }
            return thc;
        }
    }

}

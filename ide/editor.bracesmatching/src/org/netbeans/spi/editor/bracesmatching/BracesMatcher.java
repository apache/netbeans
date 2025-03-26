/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.spi.editor.bracesmatching;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

/**
 * The common interface for all matchers. Implementations of this interface
 * are supposed to be created from the {@link BracesMatcherFactory} and
 * will be used by the editor infrastructure to find areas of matching characters
 * in a document.
 * 
 * <p>Generally, <code>BracesMatcher</code>s operate within a {@link MatcherContext}
 * that describes a position in a document where the search should start.
 * The matcher first looks in the document at that position to determine if it contains
 * a character or a sequence of characters that normally come in pairs. This can
 * be a single brace or an XML tag or anything that the matcher wishes to recognize.
 * This area is called the original area and its boundaries - beginning and ending
 * offsets - should be returned from {@link #findOrigin} method. The infrastructure
 * will then ask the matcher to find the other matching areas by calling its 
 * {@link #findMatches} method.
 * 
 * <p>Besides of describing the position in a document where the search should start
 * the <code>MatcherContext</code> also tells the matcher in which direction
 * it should be looking for the original area. The direction can either be
 * backward to look towards the beginning of the document or it can be forward 
 * to look towards the end of the document. The search direction can be determined 
 * by calling {@link MatcherContext#isSearchingBackward}.
 * 
 * <p>The search for the original area should only be attempted in the near proximity
 * of the caret. The maximum distance from the caret where the matcher is supposed
 * to look can be obtained from {@link MatcherContext#getSearchLookahead}.
 * 
 * <p>While there can only be one original area, the area in the document where
 * the search start, the matcher may report multiple areas matching the
 * original one. Typically, though, there will only be one matching area too.
 * 
 * <p>In the situation when the matcher reported an original area, but was unable
 * to find any matching areas, the original area will be marked as mismatched,
 * which may be indicated visually to a user.
 * 
 * <p><b>Cancellable tasks</b>
 * 
 * <p>It is important to understand that matching is generally initiated in response
 * to a moving caret in a text component on screen. Therefore there can be many
 * requests for matching started, but only the last one provides results interesting
 * for a user. In order not to flood the system with background tasks that are computing
 * results nobody is interested in, it is essential to implement both <code>findOrigin</code>
 * and <code>findMatches</code> methods in a way that makes their work possible
 * to interrupt and cancel.
 * 
 * <p>The infrastructre uses <code>RequestProcessor</code> for spawning a background
 * thread that runs the asynchronous matching tasks. When the infrastructure wants
 * to cancel a task it simply interrupts its thread. The task <b>must</b>
 * check up on its thread status periodically and quit immediately when the
 * thread is interruped. The example below shows how this can be implemented:
 * 
 * <pre>
 * public int [] findMatches() {
 *     while(stillSearching) {
 *         
 *         // look a bit furter in the document ....
 * 
 *         if (MatcherContext.isTaskCanceled()) {
 *             return null;
 *         }
 *     }
 * }
 * </pre>
 * 
 * @author Vita Stejskal
 */
public interface BracesMatcher {

    /**
     * Checks if the <code>MatcherContext</code> is positioned in an area suitable
     * for matching. This check should be very simple and fast. The matcher
     * should only try to find the original area within the search lookahead
     * from the caret offset that it obtains from <code>MatcherContext</code>.
     * 
     * <p>Normally this method is supposed to return offset boundaries of the
     * original area or <code>null</code> if the original area can't be found.
     * The infrastructure will highlight the whole original area according to
     * the result of the <code>findMatches</code> call. If for some reason the
     * matcher does not want to highlight the whole original area it can return
     * additional offset pairs for areas that should be highlighted instead.
     * 
     * <p>The infrastructure does not lock the document prior calling this method.
     * 
     * @return The starting and ending offset of the original area or <code>null</code>
     *   if the matcher can't detect the origin area within the lookahead distance.
     *   If <code>null</code> is returned the infrastructure will never call
     *   the <code>findMatches</code> method on this instance.
     * 
     * @throws java.lang.InterruptedException If the thread was engaged in a
     *   call that resulted in <code>InterruptedException</code>, the exception
     *   should be rethrown.
     * @throws javax.swing.text.BadLocationException If a document operation fails.
     */
    public int [] findOrigin() throws InterruptedException, BadLocationException;
    
    /**
     * Finds all areas matching the original one. This method is called by the
     * infrastructure to find all areas that are matching the original area
     * of the document in the <code>MatcherContext</code>.
     * 
     * <p>The infrastructure does not lock the document prior calling this method.
     * 
     * <p>It is essential for all implementations to respond when thread running
     * this method is interrupted and abort the task and return immediately. This can
     * be done simply by checking the thread's status like in the code below.
     * 
     * <pre>
     * if (MatcherContext.isTaskCanceled()) {
     *     return;
     * }
     * </pre>
     * 
     * @return Starting and ending offsets of all areas matching the original area or
     *   <code>null</code> if no matching areas can be found. If the returned array
     *   is not null, it should have even number of elements.
     * 
     * @throws java.lang.InterruptedException If the thread was engaged in a
     *   call that resulted in <code>InterruptedException</code>, the exception
     *   should be rethrown.
     * @throws javax.swing.text.BadLocationException If a document operation fails.
     */
    public int [] findMatches() throws InterruptedException, BadLocationException;
    
    /**
     * Mixin interface, which provides context ranges for brace matches.
     * The interface is expected to be implemented on the {@link BracesMatcher} instances
     * produced by {@link BracesMatcherFactory}. If implemented, the infrastructure may
     * call the method to obtain additional context for display along with brace highlight. 
     * See the {@link BraceContext} for more information.
     */
    public interface ContextLocator {
        /**
         * Obtains context for the given text position. At this moment, only start offset
         * of the origin will be passed in, but the implementation should be prepared to
         * handle (or ignore) each of the starting offsets reported by {@link #findOrigin()} or 
         * {@link #findMatches()}.
         * <p>
         * Note: the document is <b>not read locked</b> by the caller. If the passed position does
         * not exist in the document, or seems obsolete, the SPI should return {@code null}.
         * 
         * @param originOrMatchPosition position of 'origin' or 'match' brace
         * @return context information or {@code null} if the context cannot be provided.
         */
        public BraceContext findContext(int originOrMatchPosition);
    }
}

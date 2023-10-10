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
package org.netbeans.modules.csl.api;

import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;


/**
 * Interface that a plugin can implement to assist with bracket completion during
 * editing.
 *
 * @author Tor Norbye
 */
public interface KeystrokeHandler {
    /**
     * (Based on BracketCompletion class in NetBeans' java editor support)
     *
     * A hook method called after a character was inserted into the
     * document. The function checks for special characters for
     * completion ()[]'"{} and other conditions and optionally performs
     * changes to the doc and or caret (complets braces, moves caret,
     * etc.)
     *
     * Return true if the character was already inserted (and the IDE
     * should not further insert anything)
     *
     * XXX Fix javadoc.
     * @deprecated  {@link DeletedTextInterceptor} {@link TypedTextInterceptor} {@link TypedBreakInterceptor} instead.
     */
    @Deprecated
    boolean beforeCharInserted(@NonNull Document doc, int caretOffset, @NonNull JTextComponent target, char ch)
        throws BadLocationException;

    /** @todo Rip out the boolean return value? What does it mean? 
     * @deprecated  {@link DeletedTextInterceptor} {@link TypedTextInterceptor} {@link TypedBreakInterceptor} instead.
     */
    @Deprecated
    boolean afterCharInserted(@NonNull Document doc, int caretOffset, @NonNull JTextComponent target, char ch)
        throws BadLocationException;

    /**
     * (Based on KeystrokeHandler class in NetBeans' java editor support)
     *
     * Hook called after a character *ch* was backspace-deleted from
     * *doc*. The function possibly removes bracket or quote pair if
     * appropriate.
     * @todo Document why both caretOffset and caret is passed in!
     * Return the new offset, or -1
     * @deprecated  {@link DeletedTextInterceptor} {@link TypedTextInterceptor} {@link TypedBreakInterceptor} instead.
     */

    /** @todo Split into before and after? */
    public boolean charBackspaced(@NonNull Document doc, int caretOffset, @NonNull JTextComponent target, char ch)
        throws BadLocationException;

    /**
     * A line break is being called. Return -1 to do nothing.
     * If you want to modify the document first, you can do that, and then
     * return the new offset to assign the caret to AFTER the newline has been
     * inserted.
     *
     * @todo rip out return value
     * @todo Document why both caretOffset and caret is passed in!
     * @deprecated  {@link DeletedTextInterceptor} {@link TypedTextInterceptor} {@link TypedBreakInterceptor} instead.
     */
    @Deprecated
    int beforeBreak(@NonNull Document doc, int caretOffset, @NonNull JTextComponent target)
        throws BadLocationException;

    /**
     * Compute a range matching the caret position. If no eligible range
     * is found, return {@link OffsetRange#NONE}.
     * @deprecated  {@link DeletedTextInterceptor} {@link TypedTextInterceptor} {@link TypedBreakInterceptor} instead.
     */
    @NonNull
    @Deprecated
    OffsetRange findMatching(@NonNull Document doc, int caretOffset);
    
    /**
     * Compute set of selection ranges for the given parse tree (around the given offset),
     * in leaf-to-root order.
     */
    @NonNull
    List<OffsetRange> findLogicalRanges(@NonNull ParserResult info, int caretOffset);
    
    /**
     * Compute the previous word position, if any. Can be used to implement
     * camel case motion etc.
     * 
     * @param doc The document to move in
     * @param caretOffset The caret position corresponding to the current word
     * @param reverse If true, move forwards, otherwise move backwards (e.g. "previous" word)
     * @return The next word boundary offset in the given direction, or -1 if this
     *   implementation doesn't want to compute word boundaries (the default will be used)
     * @deprecated use interceptor {@link CamelCaseInterceptor} instead
     */
    @CheckForNull
    @Deprecated
    int getNextWordOffset(Document doc, int caretOffset, boolean reverse);
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    boolean beforeCharInserted(@NonNull Document doc, int caretOffset, @NonNull JTextComponent target, char ch)
        throws BadLocationException;

    /** @todo Rip out the boolean return value? What does it mean? 
     * @deprecated  {@link DeletedTextInterceptor} {@link TypedTextInterceptor} {@link TypedBreakInterceptor} instead.
     */
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
    int beforeBreak(@NonNull Document doc, int caretOffset, @NonNull JTextComponent target)
        throws BadLocationException;

    /**
     * Compute a range matching the caret position. If no eligible range
     * is found, return {@link OffsetRange#NONE}.
     * @deprecated  {@link DeletedTextInterceptor} {@link TypedTextInterceptor} {@link TypedBreakInterceptor} instead.
     */
    @NonNull
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
    int getNextWordOffset(Document doc, int caretOffset, boolean reverse);
}

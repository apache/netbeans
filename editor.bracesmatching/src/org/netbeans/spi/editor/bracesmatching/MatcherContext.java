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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.spi.editor.bracesmatching;

import javax.swing.text.Document;
import org.netbeans.modules.editor.bracesmatching.MasterMatcher;
import org.netbeans.modules.editor.bracesmatching.SpiAccessor;

/**
 * An immutable context for a {@link BracesMatcher}. This context is created by the
 * editor infrastructure when it needs to create a matcher by using a registered
 * {@link BracesMatcherFactory}. The context provides a matcher with 
 * information essential to perform the search.
 * 
 * @author Vita Stejskal
 */
public final class MatcherContext {

    static {
        SpiAccessor.register(new SpiAccessorImpl());
    }
    
    private final Document document;
    private final int offset;
    private final boolean backward;
    private final int lookahead;
    
    private MatcherContext(Document document, int offset, boolean backward, int lookahead) {
        this.document = document;
        this.offset = offset;
        this.backward = backward;
        this.lookahead = lookahead;
    }

    /**
     * Gets a document that should be searched for matching areas.
     * 
     * @return The document to search in.
     */
    public Document getDocument() {
        return document;
    }
    
    /**
     * Gets an offset in a document where searching should start. It's usually
     * a position of a caret.
     * 
     * @return The caret's position.
     */
    public int getSearchOffset() {
        return offset;
    }

    /**
     * Gets the direction to search in for an original area. The search always
     * begins at the caret offset.
     * 
     * @return <code>true</code> to search backward towards the beginning of 
     *   a document or <code>false</code> to search forward towards the end of 
     *   a document.
     */
    public boolean isSearchingBackward() {
        return backward;
    }
    
    /**
     * Gets an offset in a document towards which the search should go. This
     * is basically <code>searchOffset ± searchLookahead</code> depending on
     * the search direction.
     * 
     * @return The offset limiting the search, <code>searchOffset ± searchLookahead</code>.
     */
    public int getLimitOffset() {
        return backward ? offset - lookahead : offset + lookahead;
    }
    
    /**
     * Gets the number of characters to search through when looking for an original
     * area. When searching for an original area matchers should not look
     * further from the caret offset then the number of characters returned from
     * this method.
     * 
     * @return A small positive number to limit the search for an original
     *   area.
     */
    public int getSearchLookahead() {
        return lookahead;
    }

    /**
     * Determines if a braces matching task was canceled.
     * 
     * <p>IMPORTANT: This method may only
     * be called from the thread running a braces matching task, ie. the one
     * that calls your <code>BracesMatcher</code>'s <code>findOrigin</code> and
     * <code>findMatches</code> methods. Calling this method from a different thread
     * will assert and fail.
     * 
     * @return <code>true</code> if the task was canceled, <code>false</code> otherwise
     * @since 1.3
     */
    public static boolean isTaskCanceled() {
        return MasterMatcher.isTaskCanceled();
    }
    
    private static final class SpiAccessorImpl extends SpiAccessor {
        
        public MatcherContext createCaretContext(Document document, int offset, boolean backward, int lookahead) {
            return new MatcherContext(document, offset, backward, lookahead);
        }
        
    } // End of SpiAccessorImpl class
}

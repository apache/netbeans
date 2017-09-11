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

package org.netbeans.spi.editor.highlighting;

import java.util.NoSuchElementException;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;

/**
 * An iterator through highlights in a <code>HighlightsContainer</code>.
 *
 * <p><b>Implementation:</b> Any <code>HighlightsSequence</code> obtained from any of the classes in
 * the Highlighting API will behave as so called <i>fast-fail</i> iterator. It
 * means that it will throw <code>ConcurrentModificationException</code> from
 * its methods if the underlying data (highlights) have changed since when the instance
 * of the <code>HighlightsSequence</code> was obtained.
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface HighlightsSequence {

    /**
     * An empty <code>HighlightsSequence</code>.
     */
    public static final HighlightsSequence EMPTY = new HighlightsSequence() {
        public boolean moveNext() {
            return false;
        }

        public int getStartOffset() {
            throw new NoSuchElementException();
        }

        public int getEndOffset() {
            throw new NoSuchElementException();
        }

        public AttributeSet getAttributes() {
            throw new NoSuchElementException();
        }
    }; // End of EMPTY HighlightsSequence
    
    /**
     * Moves the internal pointer to the next highlight in this sequence (if there is any).
     * If this method returns <code>true</code> highlight's boundaries and attributes
     * can be retrieved by calling the getter methods.
     *
     * @return <code>true</code> If there is a highlight available and it is safe
     *         to call the getters.
     * @throws ConcurrentModificationException If the highlights this sequence is
     *         iterating through have been changed since the creation of the sequence.
     */
    boolean moveNext();
    
    /**
     * Gets the start offset of a current highlight.
     *
     * @return The offset in a document where the current highlight starts.
     * @throws ConcurrentModificationException If the highlights this sequence is
     * iterating through have been changed since the creation of the sequence.
     */
    int getStartOffset();
    
    /**
     * Gets the end offset of a current highlight.
     *
     * @return The offset in a document where the current highlight ends.
     * @throws ConcurrentModificationException If the highlights this sequence is
     * iterating through have been changed since the creation of the sequence.
     */
    int getEndOffset();
    
    /**
     * Gets the set of attributes that define how to render a current highlight.
     * 
     * <p>Since the <code>AttributeSet</code> can contain any attributes implementors
     * must be aware of whether the attributes returned from this method affect
     * metrics or not and set the <code>isFixedSize</code> parameter appropriately
     * when createing <code>HighlightsLayer</code>s.
     *
     * @return The set of text rendering attributes. Must not return <code>null</code>.
     * @throws ConcurrentModificationException If the highlights this sequence is
     * iterating through have been changed since the creation of the sequence.
     * 
     * @see org.netbeans.spi.editor.highlighting.HighlightsLayer
     */
    AttributeSet getAttributes();
}

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

/**
 * The container of highlighted areas and their attributes.
 *
 * @author vita
 */
public interface HighlightsContainer {

    /**
     * The attribute key for highlights that need to span across a whole line.
     *
     * <p>Typically highlights only affect rendering of a small part of text
     * (perhaps just several characters). Some layers, however, need to highlight
     * a whole line in an editor window regardless of how much text the line
     * contains. The highlighting of a line with a caret is an example of such a layer.
     *
     * <p>If you want a highlight that spans accross the whole editor pane you
     * can add this attribute key to the highlight's <code>AttributeSet</code>
     * and set its value to <code>Boolean.TRUE</code>. The highlighted area must
     * contain the new-line character at the end of the line.
     */
    static final String ATTR_EXTENDS_EOL =
            "org.netbeans.spi.editor.highlighting.HighlightsContainer.ATTR_EXTENDS_EOL"; //NOI18N
    
    /**
     * The attribute key for highlights that need to show up on empty lines.
     * 
     * <p>If you use this key for a highlight which contains the new-line character
     * at the end of an empty line and set the value of this attribute to
     * <code>Boolean.TRUE</code> then the highlight will be drawn as
     * a half-character-wide stripe at the beginning of the line.
     */
    static final String ATTR_EXTENDS_EMPTY_LINE =
            "org.netbeans.spi.editor.highlighting.HighlightsContainer.ATTR_EXTENDS_EMPTY_LINE"; //NOI18N
    
    /**
     * Provides the list of highlighted areas that should be used for rendering
     * a document.
     *
     * <p>The returned highlighted areas (highlights) must obey the following rules:
     * <ul>
     * <li>The starting and ending offsets of each highlight should be
     * within the range specified by the <code>startOffset</code> and <code>endOffset</code>
     * parameters. Any highlights outside of this range will be clipped by the
     * rendering infrastructure.
     * <li>The highlights must not overlap. The infrastructure may ignore or trim
     * any overlapping highlights.
     * <li>The list of highlights must be sorted by their
     * starting offsets ascendingly (i.e. the smallest offset first).
     * </ul>
     *
     * <p>The editor infrastructure will log any problems it may encounter with
     * provided implementations of this interface. Although the infrastructure
     * will try to do its best to render all highlights supplied by the implementors,
     * if the above rules are violated the results can't be garanteed.
     * 
     * @param startOffset The starting offset of the area which the caller
     *  attempts to repaint (or create views for). The staring offset is always >=0.
     * @param endOffset The ending offset of the rendered area. The <code>Integer.MAX_VALUE</code>
     *  can be passed in if the end offset is unknown to the caller.
     *  The highlights container is then expected to return all highlights
     *  up to the end of the document.
     *
     * @return non-null iterator of highlights sorted by offsets.
     */
    HighlightsSequence getHighlights(int startOffset, int endOffset);
    
    /**
     * Adds a listener to this highlights container.
     *
     * @param listener    The listener to add.
     */
    void addHighlightsChangeListener(HighlightsChangeListener listener);
    
    /**
     * Removes a listener from this highlights container.
     *
     * @param listener    The listener to remove.
     */
    void removeHighlightsChangeListener(HighlightsChangeListener listener);
    
}

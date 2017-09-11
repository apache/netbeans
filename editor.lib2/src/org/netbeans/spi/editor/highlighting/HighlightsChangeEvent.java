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

import java.util.EventObject;

/**
 * An event object notifying about a change in highlighting of certain area
 * of a document. The area where the highlighting has changed is specified by
 * its starting and ending offsets. Whoever receives this event should consider
 * re-requesting the new list of highlighted areas from the 
 * <code>HighlightsContainer</code> that fired the event.
 *
 * @author Vita Stejskal
 */
public final class HighlightsChangeEvent extends EventObject {
    
    private int startOffset;
    private int endOffset;
    
    /** 
     * Creates a new instance of <code>HighlightsChangeEvent</code>. The
     * <code>startOffset</code> and <code>endOffset</code> parameters specify
     * the area of a document where highlighting has changed. It's possible to
     * use <code>Integer.MAX_VALUE</code> for the <code>endOffset</code> parameter
     * meaning that the end of the change is unknown or the change spans up to
     * the end of a document.
     *
     * @param source         The highlight layer that fired this event.
     * @param startOffset    The beginning of the area that has changed.
     * @param endOffset      The end of the changed area.
     */
    public HighlightsChangeEvent(HighlightsContainer source, int startOffset, int endOffset) {
        super(source);
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    /**
     * Gets the beginning of an area in the document where highlighting has
     * changed.
     *
     * @return The starting offset of the chaged area. Should always be greater than
     *         or equal to zero.
     */
    public int getStartOffset() {
        return startOffset;
    }
    
    /**
     * Gets the end of an area in the document where highlighting has
     * changed.
     *
     * @return The ending offset of the chaged area. May return <code>Integer.MAX_VALUE</code>
     *         if the ending position is unknown.
     */
    public int getEndOffset() {
        return endOffset;
    }
}

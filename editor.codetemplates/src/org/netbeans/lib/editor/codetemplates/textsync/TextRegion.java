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

package org.netbeans.lib.editor.codetemplates.textsync;

import java.util.Arrays;
import java.util.List;
import javax.swing.text.Position;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.lib.editor.util.swing.PositionRegion;

/**
 * Text region inside a document held by a pair of swing text positions.
 * <br/>
 * A region can have another nested regions.
 *
 * @author Miloslav Metelka
 */
public final class TextRegion<I> {
    
    private static final Position FIXED_ZERO_POSITION = PositionRegion.createFixedPosition(0);
    
    public static Position createFixedPosition(int offset) {
        return PositionRegion.createFixedPosition(offset);
    }

    private Position startPos; // 12 bytes (8-super + 4)

    private Position endPos; // 16 bytes

    private TextSync textSync; // 20 bytes
    
    private TextRegion<?> parent; // 24 bytes
    
    /**
     * Child regions - managed by TextRegionManager.
     */
    private List<TextRegion<?>> regions; // 28 bytes
    
    /**
     * Client-specific information.
     */
    private I clientInfo; // 32 bytes
    
    public TextRegion() {
        this(FIXED_ZERO_POSITION, FIXED_ZERO_POSITION);
    }
    
    public TextRegion(int startOffset, int endOffset) {
        this(PositionRegion.createFixedPosition(startOffset), PositionRegion.createFixedPosition(endOffset));
    }
    
    public TextRegion(Position startPos, Position endPos) {
        if (startPos == null)
            throw new IllegalArgumentException("startPos cannot be null"); // NOI18N
        if (endPos == null)
            throw new IllegalArgumentException("endPos cannot be null"); // NOI18N
        this.startPos = startPos;
        this.endPos = endPos;
    }
    
    public int startOffset() {
        return startPos.getOffset();
    }

    public int endOffset() {
        return endPos.getOffset();
    }
    
    public void updateBounds(Position startPos, Position endPos) {
        if (textRegionManager() != null)
            throw new IllegalStateException("Change of bounds of region " + // NOI18N
                    "connected to textRegionManager prohibited."); // NOI18N
        if (startPos != null)
            setStartPos(startPos);
        if (endPos != null)
            setEndPos(endPos);
    }
    
    public I clientInfo() {
        return clientInfo;
    }
    
    public void setClientInfo(I clientInfo) {
        this.clientInfo = clientInfo;
    }

    public TextSync textSync() {
        return textSync;
    }

    void setTextSync(TextSync textSync) {
        this.textSync = textSync;
    }

    TextRegion<?> parent() {
        return parent;
    }

    void setParent(TextRegion<?> parent) {
        this.parent = parent;
    }

    List<TextRegion<?>> regions() {
        return regions;
    }

    List<TextRegion<?>> validRegions() {
        if (regions == null) {
            regions = new GapList<TextRegion<?>>(2);
        }
        return regions;
    }
    
    void initRegions(TextRegion<?>[] consumedRegions) {
        assert (regions == null || regions.size() == 0);
        regions = new GapList<TextRegion<?>>(Arrays.asList(consumedRegions));
    }
    
    void clearRegions() {
        regions = null;
    }
    
    void setStartPos(Position startPos) {
        this.startPos = startPos;
    }

    void setEndPos(Position endPos) {
        this.endPos = endPos;
    }
    
    TextRegionManager textRegionManager() {
        return (textSync != null) ? textSync.textRegionManager() : null;
    }

    @Override
    public String toString() {
        return "<" + startOffset() + "," + endOffset() + ") IHC=" +
                System.identityHashCode(this) + ", parent=" +
                ((parent != null) ? System.identityHashCode(parent) : "null") +
                ((clientInfo != null) ? (" clientInfo:" + clientInfo) : "");
    }
    
}

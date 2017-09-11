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

package org.netbeans.modules.csl.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.openide.ErrorManager;

/**Copied from editor/codetemplates and adjusted for the needs of the instant rename.
 * 
 * Maintain the same text in the selected regions of the text document.
 *
 * @author Miloslav Metelka
 */
public final class SyncDocumentRegion {
    
    private Document doc;
    
    private List<? extends MutablePositionRegion> regions;
    
    private List<? extends MutablePositionRegion> sortedRegions;
    
    private boolean regionsSortPerformed;
    
    /**
     * Construct synchronized document regions.
     *
     * @param doc document on which to operate.
     * @param regions regions that should be kept synchronized.
     *  The first region is the master. All the regions need to have
     *  the initial position to have the backward bias.
     */
    public SyncDocumentRegion(Document doc, List<? extends MutablePositionRegion> regions) {
        this.doc = doc;
        this.regions = regions;
        // Check bounds correctness and whether they are sorted
        regionsSortPerformed = PositionRegion.isRegionsSorted(regions);
        if (regionsSortPerformed) {
            sortedRegions = regions;
        } else {
            sortedRegions = new ArrayList<MutablePositionRegion>(regions);
            Collections.sort(sortedRegions, PositionRegion.getComparator());
        }
    }
    
    public int getRegionCount() {
        return regions.size();
    }
    
    public MutablePositionRegion getRegion(int regionIndex) {
        return regions.get(regionIndex);
    }

    public int getFirstRegionStartOffset() {
        return getRegion(0).getStartOffset();
    }
    
    public int getFirstRegionEndOffset() {
        return getRegion(0).getEndOffset();
    }
    
    public int getFirstRegionLength() {
        return getFirstRegionEndOffset() - getFirstRegionStartOffset();
    }
    
    /**
     * Get region in a sorted list of the regions.
     *
     * @param regionIndex of the region.
     * @return region in a sorted list of the regions.
     */
    public MutablePositionRegion getSortedRegion(int regionIndex) {
         return sortedRegions.get(regionIndex);
    }

    /**
     * Propagate text of the first region into all other regions.
     *
     * @param moveStartDownLength how much to move starting position
     *  down. It may be 0 to signal that the startng position should
     *  stay as is.
     */
    public void sync(int moveStartDownLength) {
        if (moveStartDownLength != 0) {
            // Move first region's start offset down
            MutablePositionRegion firstRegion = getRegion(0);
            try {
                Position newStartPos = doc.createPosition(
                        firstRegion.getStartOffset() - moveStartDownLength);
                firstRegion.setStartPosition(newStartPos);
                
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
            
        }
        
        String firstRegionText = getFirstRegionText();
        if (firstRegionText != null) {
            int regionCount = getRegionCount();
            for (int i = 1; i < regionCount; i++) {
                MutablePositionRegion region = getRegion(i);
                int offset = region.getStartOffset();
                int length = region.getEndOffset() - offset;
                try {
                    if (!CharSequenceUtilities.textEquals(firstRegionText, DocumentUtilities.getText(doc, offset, length))) {
                        if (firstRegionText.length() > 0) {
                            doc.insertString(offset, firstRegionText, null);
                        }
                        doc.remove(offset + firstRegionText.length(), length);
                    }
                } catch (BadLocationException e) {
                    ErrorManager.getDefault().notify(e);
                }

            }
        }
    }

    private String getFirstRegionText() {
        return getRegionText(0);
    }
    
    private String getRegionText(int regionIndex) {
        try {
            MutablePositionRegion region = getRegion(regionIndex);
            int offset = region.getStartOffset();
            int length = region.getEndOffset() - offset;
            return doc.getText(offset, length);
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
}

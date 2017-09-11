/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.view;

import java.awt.font.TextLayout;

/**
 * Info about last successful breaking of the view.
 * <br>
 * Since TextLayout creation is expensive this should speed up breaking of the views
 * (used in line wrap) considerably.
 *
 * @author Miloslav Metelka
 */
public class TextLayoutBreakInfo {

    /**
     * Even line that breaks just into two lines may query 3 times
     * (create-start-part; find-out-that-it's-wide(text-layout measurement
     * versus TextLayout.getCaretInfo()); create-another-start-part; create-end-part).
     */
    private static final int MIN_ITEMS = 4;

    /**
     * Limit number of cache items (iteration is linear
     * - consider a map for very large text layouts or maybe split very large text layouts
     * initially into e.g. 4KB).
     */
    private static final int MAX_ITEMS = 32;
    
    /**
     * Possibly make a computed constructor parameter in future.
     */
    private static final int TYPICAL_LINE_LENGTH = 80;
    
    private final Item[] items;
    
    private int itemsCount;
    
    TextLayoutBreakInfo(int textLayoutLength) {
        int itemsLength = Math.max(MIN_ITEMS, Math.min(MAX_ITEMS,
                // Some split attempts may double due to measurements; "<<" due to start and end part for each line
                (textLayoutLength / TYPICAL_LINE_LENGTH + 2) << 1));
        items = new Item[itemsLength];
    }
    
    TextLayout findPartTextLayout(int shift, int length) {
        for (int i = 0; i < itemsCount; i++) {
            Item item = items[i];
            if (item.shift == shift && item.length == length) {
                // Move to front
                if (i != 0) {
                    System.arraycopy(items, 0, items, 1, i);
                    items[0] = item;
                }
                return item.partTextLayout;
            }
        }
        return null;
    }
    
    void add(int shift, int length, TextLayout partTextLayout) {
        if (itemsCount < items.length) {
            itemsCount++;
        }
        if (itemsCount > 1) {
            System.arraycopy(items, 0, items, 1, itemsCount - 1);
        }
        items[0] = new Item(shift, length, partTextLayout);
    }
    
    private static final class Item {

        /**
        * Shift in original text layout.
        */
        final int shift; // 8(super) + 4 = 12 bytes

        /**
        * Shift in original text layout.
        */
        final int length; // 12 + 4 = 16 bytes

        final TextLayout partTextLayout; // 16 + 4 = 20 bytes

        public Item(int shift, int length, TextLayout partTextLayout) {
            this.shift = shift;
            this.length = length;
            this.partTextLayout = partTextLayout;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
        
        
        
    }
    
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

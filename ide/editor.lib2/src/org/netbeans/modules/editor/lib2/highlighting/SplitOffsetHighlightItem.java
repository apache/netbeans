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
package org.netbeans.modules.editor.lib2.highlighting;

import javax.swing.text.AttributeSet;

/**
 * Highlight item supporting complex positions.
 *
 * @author Miloslav Metelka
 */
public final class SplitOffsetHighlightItem extends HighlightItem {
    
    /**
     * Ending split offset of the highlight.
     * Start split offset is derived from end split offset of the previous item.
     */
    private final int endSplitOffset; // 12 + 4 = 16 bytes

    private final CharSequence viewCustomText; // 20 + 4 = 24 bytes

    public SplitOffsetHighlightItem(int endOffset, int endSplitOffset, AttributeSet attrs) {
        super(endOffset, attrs);
        this.endSplitOffset = endSplitOffset;
        this.viewCustomText = null; // Not actively used yet
    }
  
    @Override
    public int getEndSplitOffset() {
        return endSplitOffset;
    }

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.editor.fold;

import org.openide.util.NbBundle;

import static org.netbeans.api.editor.fold.Bundle.*;

/**
 * Template that describes how the fold should appear and interact with the
 * user. The FoldTemplate defines how many characters at the start (end) should
 * act as a fold guard: change to that area will remove the fold because it
 * becomes damaged. It also defines what placeholder should appear in place of the 
 * folded code.
 * <p>
 * A template may be attached to a {@link FoldType} instance. Folds of that kind will
 * automatically use the attached Template, if not overriden explicitly.
 * <p>
 * The string {@code "..."} ({@link #CONTENT_PLACEHOLDER}) is treated as a placeholder. If a 
 * ContentReader is available for the {@link FoldType}, the placeholder will be replaced by 
 * the product of the Reader. Otherwise the placeholder will remain in the displayText 
 * and will be presented.
 * 
 * @since 1.35
 * @author sdedic
 */
public final class FoldTemplate {
    /**
     * The default template for folded text: no markers before+after, ellipsis
     * shown.
     */
    @NbBundle.Messages("FT_DefaultTemplate=...")
    public static final FoldTemplate DEFAULT = new FoldTemplate(0, 0, FT_DefaultTemplate()); // NOI18N
    
    /**
     * A standard template, which represents a block of something.
     */
    @NbBundle.Messages("FT_DefaultBlockTemplate={...}")
    public static final FoldTemplate DEFAULT_BLOCK = new FoldTemplate(0, 0, FT_DefaultBlockTemplate()); // NOI18N
    
    /**
     * This string is interpreted as a placeholder for the infrastructure to inject 
     */
    public static final String CONTENT_PLACEHOLDER = FT_DefaultTemplate();
    
    /**
     * # of characters at the start of the fold, which serve as a marker that the fold
     * has been damaged/destroyed
     */
    private int     guardedStart;
    
    /**
     * The guarded portion at the end of the fold
     */
    private int     guardedEnd;
    
    /**
     * Description that appears in the folded area.
     */
    private String  displayText;
    
    /**
     * Creates a FoldTemplate with a fixed description.
     * 
     * @param guardedStart length of the start marker, or -1 if no start marker is present
     * @param guardedEnd length of the end marker, or -1 if no end marker is present
     * @param displayText text which should be displayed in place of the folded content
     */
    public FoldTemplate(int guardedStart, int guardedEnd, String displayText) {
        this.guardedStart = guardedStart;
        this.guardedEnd = guardedEnd;
        this.displayText = displayText;
    }

    public String getDescription() {
        return displayText;
    }

    public int getGuardedEnd() {
        return guardedEnd;
    }

    public int getGuardedStart() {
        return guardedStart;
    }
}

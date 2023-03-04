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
package org.netbeans.modules.csl.api;

import java.util.Set;

import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;


/**
 * An item to be added to the code completion dialog
 *
 * @author Tor Norbye
 */
public interface CompletionProposal {
    /**
     * The offset at which the completion item substitution should begin
     * @return The anchor offset
     */
    int getAnchorOffset();

    @CheckForNull
    ElementHandle getElement();

    @NonNull
    String getName();

    @NonNull
    String getInsertPrefix();

    @CheckForNull
    String getSortText();

    @NonNull
    String getLhsHtml(@NonNull HtmlFormatter formatter);

    @CheckForNull
    String getRhsHtml(@NonNull HtmlFormatter formatter);

    @NonNull
    ElementKind getKind();

    @CheckForNull
    ImageIcon getIcon();

    @NonNull
    Set<Modifier> getModifiers();
    
    /**
     * Return true iff this is a "smart" completion item - one that should be emphasized
     * (currently the IDE flushes these to the top and separates them with a line)
     * @return True iff this item is a smart completion item and should be shown first
     */
    boolean isSmart();

    /**
     * Most modules should return 0 here. If you return non-0, this is a sorting priority
     * for use by GSF when ordering the completion proposals. You can use this to override
     * the default behavior (which generally moves smart items to the top, and within each
     * smart/nonsmart section, orders for example variables higher than methods.
     * 
     * @return 0 to use normal sorting, or some other number to override the sorting priority
     *  of this item. Smaller items (including negative numbers) go nearer the top of the list.
     */
    int getSortPrioOverride();

    /**
     * Provide a custom live code template that will be inserted when
     * this item is chosen for insertion. 
     *
     * @return A live code template to be inserted into the document
     *   at the anchor offset. Return null to get the default behavior
     *   where the insert prefix, the insert params and param list delimiters
     *   are used instead.
     */
    @CheckForNull
    String getCustomInsertTemplate();
}

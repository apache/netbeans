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

package org.netbeans.api.editor;

/**
 * Names of common editor actions.
 * Clients can use the names constants e.g. for <ul>
 * <li>
 * Retrieving (and possibly running) action's impl for a particular kit by
 * {@link EditorUtilities#getAction(javax.swing.text.EditorKit, java.lang.String)}.
 * </li>
 * <li>
 * When overriding action's impl for a target mime-type (rather than using a string literal).
 * </li>
 * </ul>
 * Ideally all editor actions' names should be declared here.
 *
 * @author Miloslav Metelka
 * @since 1.13
 */
public final class EditorActionNames {

    private EditorActionNames() {
        // No instances
    }

    /** Toggle the visibility of the editor toolbar */
    public static final String toggleToolbar = "toggle-toolbar"; // NOI18N

    /** Toggle visibility of line numbers */
    public static final String toggleLineNumbers = "toggle-line-numbers"; // NOI18N

    /** 
     * Toggle visibility of non-printable characters.
     * @since 1.20
     */
    public static final String toggleNonPrintableCharacters = "toggle-non-printable-characters"; // NOI18N

    /** Goto declaration depending on the context under the caret */
    public static final String gotoDeclaration = "goto-declaration"; // NOI18N

    /**
     * Zoom text in by increasing default font size.
     * <br>
     * textComponent.getClientProperty("text-zoom") contains positive (or negative)
     * integer of how many points the font size should be increased (decreased).
     * @since 1.45
     */
    public static final String zoomTextIn = "zoom-text-in"; // NOI18N

    /**
     * Zoom text out by decreasing default font size.
     * @since 1.45
     */
    public static final String zoomTextOut = "zoom-text-out"; // NOI18N
    
    /**
     * Toggle between regular text selection and rectangular block selection
     * when caret selects in a column mode.
     */
    public static final String toggleRectangularSelection = "toggle-rectangular-selection"; // NOI18N
    
    /**
     * Transpose letter at caret offset with the next one (useful when making typo).
     * @since 1.48
     */
    public static final String transposeLetters = "transpose-letters"; // NOI18N

    /**
     * Move entire code elements (statements and class members) up.
     * @since 1.56
     */
    public static final String moveCodeElementUp = "move-code-element-up"; // NOI18N

    /**
     * Move entire code elements (statements and class members) down.
     * @since 1.56
     */
    public static final String moveCodeElementDown = "move-code-element-down"; // NOI18N

    /**
     * Remove the enclosing parts of a nested statement.
     * @since 1.57
     */
    public static final String removeSurroundingCode = "remove-surrounding-code"; // NOI18N

    /**
     * Organize import statements to correspond to the specified code style rules
     * @since 1.64
     */
    public static final String organizeImports = "organize-imports"; // NOI18N

    /**
     * Organize class members order to correspond to the specified code style rules
     * @since 1.64
     */
    public static final String organizeMembers = "organize-members"; // NOI18N


    /**
     * Toggle caret between regular insert mode and overwrite mode.
     * @since 2.6
     */
    public static final String toggleTypingMode = "toggle-typing-mode"; // NOI18N
    
    /**
     * Remove the last added caret.
     * @since 2.6
     */
    public static final String removeLastCaret = "remove-last-caret"; // NOI18N
    
    /**
     * Navigates to the previous occurence of the symbol under the caret. The action
     * should be implemented by specific language EditorKit
     * @since 2.3
     */
    public static final String gotoPrevOccurrence = "prev-marked-occurrence"; // NOI18N
    
    /**
     * Navigates to the next occurence of the symbol under the caret. The action
     * should be implemented by specific language EditorKit
     * @since 2.3
     */
    public static final String gotoNextOccurrence = "next-marked-occurrence"; // NOI18N
    
    /**
     * Add a new caret at the line above (at the same column) for all existing carets.
     * @since 2.6
     */
    public static final String addCaretUp = "add-caret-up"; // NOI18N
    
    /**
     * Add a new caret at the line below (at the same column) for all existing carets.
     * @since 2.6
     */
    public static final String addCaretDown = "add-caret-down"; // NOI18N

}

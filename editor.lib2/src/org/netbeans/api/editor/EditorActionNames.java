/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
     * @see #zoomInTextAction
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

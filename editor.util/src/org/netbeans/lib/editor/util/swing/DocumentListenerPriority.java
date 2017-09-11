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

package org.netbeans.lib.editor.util.swing;

/**
* Priorities of firing of document listeners being added to a document.
*
* @author Miloslav Metelka
* @since 1.4
*/

public final class DocumentListenerPriority {

    /**
     * Level that gets notified first (before all other levels).
     * <br/>
     * It may be used in cooperation with other levels to pre-mark that there was
     * a document modification performed that may change some of the data
     * that will be updated later on another level. For example the view hierarchy
     * may mark that it is temporarily invalid until its update will be triggered
     * on {@link #VIEW} level.
     */
    public static final DocumentListenerPriority FIRST
            = new DocumentListenerPriority(6, "first"); // NOI18N

    /**
     * Lexer gets notified early to allow other levels to use the udpated
     * token list.
     */
    public static final DocumentListenerPriority LEXER
            = new DocumentListenerPriority(5, "lexer"); // NOI18N

    /**
     * Fold update gets notified prior default level.
     */
    public static final DocumentListenerPriority FOLD_UPDATE
            = new DocumentListenerPriority(4, "fold-update"); // NOI18N

    /**
     * Default level is used for all listeners added
     * by regular {@link javax.swing.text.Document#addDocumentListener(
     * javax.swing.event.DocumentListener)} method.
     */
    public static final DocumentListenerPriority DEFAULT
            = new DocumentListenerPriority(3, "default"); // NOI18N

    /**
     * Views are updated after default level and prior caret gets updated.
     * @since 1.11
     */
    public static final DocumentListenerPriority VIEW
            = new DocumentListenerPriority(2, "view"); // NOI18N

    /**
     * Caret udpate gets notified as last.
     */
    public static final DocumentListenerPriority CARET_UPDATE
            = new DocumentListenerPriority(1, "caret-update"); // NOI18N

    /**
     * Udpate that follows caret update.
     * @since 1.6
     */
    public static final DocumentListenerPriority AFTER_CARET_UPDATE
            = new DocumentListenerPriority(0, "after-caret-update"); // NOI18N

    static final DocumentListenerPriority[] PRIORITIES = new DocumentListenerPriority[] {
        AFTER_CARET_UPDATE, CARET_UPDATE, VIEW, DEFAULT, FOLD_UPDATE, LEXER, FIRST
    };
    
    private int priority;
    
    private String description;

    /**
     * Construct new DocumentListenerPriority.
     *
     * @param priority higher priority means sooner firing.
     * @param description textual description of the priority.
     */
    private DocumentListenerPriority(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }

    public String toString() {
        return getDescription();
    }

    /**
     * Get the integer priority for the purpose of adding/removing
     * of the listener with this priority.
     */
    int getPriority() {
        return priority;
    }

}

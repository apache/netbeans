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

package org.netbeans.modules.editor.lib2;

/**
 * This class contains settings names copied over from SettingsNames and ExtSettingsNames.
 * It exists merely to allow editor infrastructure to use these constants for
 * backwards compatibility reasons without having to depend on editor.deprecated.pre61settings module.
 * 
 * The SettingsNames, ExtSettingsNames classes do not use string literals, but they refer
 * directly to this class.
 * 
 * Rules:
 * This class should not contain names of settings that belong to the API category 'public'.
 * Such names ought to be listed in SimpleValueNames class. The names for settings
 * from other categories (devel, friend, private, deprecated) may be listed here,
 * but generally 'devel', 'friend' settings should eventually end up in SimpleValueNames too.
 * Fields with names of deprecated settings should be deprecated too.
 * 
 */
public final class DocumentPreferencesKeys {

    private DocumentPreferencesKeys() {
        //no-op
    }
    
    // -----------------------------------------------------------------------
    // --- from SettingsNames
    // -----------------------------------------------------------------------

    public static final String EXPAND_TABS = "expand-tabs"; // NOI18N
    public static final String INDENT_SHIFT_WIDTH = "indent-shift-width"; // NOI18N
    public static final String SPACES_PER_TAB = "spaces-per-tab"; // NOI18N
    public static final String TAB_SIZE = "tab-size"; // NOI18N
    
    
    /** Acceptor that recognizes the identifier characters.
    * If set it's used instead of the default Syntax.isIdentifierPart() call.
    * Values: org.netbeans.editor.Acceptor instances
    */
    public static final String IDENTIFIER_ACCEPTOR = "identifier-acceptor"; // NOI18N

    /** Acceptor that recognizes the whitespace characters.
    * If set it's used instead of the default Syntax.isWhitespace() call.
    * Values: org.netbeans.editor.Acceptor instances
    */
    public static final String WHITESPACE_ACCEPTOR = "whitespace-acceptor"; // NOI18N

    /** Finder for finding the next word. If it's not set,
    * the <tt>FinderFactory.NextWordFwdFinder</tt> is used.
    * Values: org.netbeans.editor.Finder
    */
    public static final String NEXT_WORD_FINDER = "next-word-finder"; // NOI18N

    /** Finder for finding the previous word. If it's not set,
    * the <tt>FinderFactory.WordStartBwdFinder</tt> is used.
    * Values: org.netbeans.editor.Finder
    */
    public static final String PREVIOUS_WORD_FINDER = "previous-word-finder"; // NOI18N

    /** Whether the word move should stop on the '\n' character. This setting
    * affects both the 
    * Values: java.lang.Boolean
    */
    public static final String WORD_MOVE_NEWLINE_STOP = "word-move-newline-stop"; // NOI18N

    /** Buffer size for reading into the document from input stream or reader.
    * Values: java.lang.Integer
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String READ_BUFFER_SIZE = "read-buffer-size"; // NOI18N

    /** Buffer size for writing from the document to output stream or writer.
    * Values: java.lang.Integer instances
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String WRITE_BUFFER_SIZE = "write-buffer-size"; // NOI18N

    /** How many lines should be processed at once in the various text
    * processing. This is used for example when processing the text
    * by syntax scanner.
    */
    public static final String LINE_BATCH_SIZE = "line-batch-size"; // NOI18N
}

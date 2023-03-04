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

package org.netbeans.modules.editor.indent.api;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.editor.indent.IndentImpl;

/**
 * Utility methods related to indentation and reformatting.
 *
 * @author Miloslav Metelka
 */
public final class IndentUtils {
    
    private static final int MAX_CACHED_INDENT = 80;
    private static final Logger LOG = Logger.getLogger(IndentUtils.class.getName());
    
    private static final String[] cachedSpacesStrings = new String[MAX_CACHED_INDENT + 1];
    static {
        cachedSpacesStrings[0] = ""; //NOI18N
    }
    
    private static final int MAX_CACHED_TAB_SIZE = 8; // Should mostly be <= 8
    
    /**
     * Cached indentation string containing tabs.
     * <br/>
     * The cache does not contain indents smaller than the particular tabSize
     * since they are only spaces contained in cachedSpacesStrings.
     */
    private static final String[][] cachedTabIndents = new String[MAX_CACHED_TAB_SIZE + 1][];
    
    private IndentUtils() {
        // no instances
    }
    
    /**
     * Get number of spaces that form a single indentation level.
     * 
     * @return &gt;=0 size of indentation level in spaces.
     */
    public static int indentLevelSize(Document doc) {
        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("INDENT_SHIFT_WIDTH='" + prefs.get(SimpleValueNames.INDENT_SHIFT_WIDTH, null) //NOI18N
                    + "', EXPAND_TABS='" + prefs.get(SimpleValueNames.EXPAND_TABS, null) //NOI18N
                    + "', SPACES_PER_TAB='" + prefs.get(SimpleValueNames.SPACES_PER_TAB, null)//NOI18N
                    + "', TAB_SIZE='" + prefs.get(SimpleValueNames.TAB_SIZE, null) //NOI18N
                    + "' for " + doc //NOI18N
            );
        }
        
        int indentLevel = prefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, -1);
        
        if (indentLevel < 0) {
            boolean expandTabs = prefs.getBoolean(SimpleValueNames.EXPAND_TABS, true);
            if (expandTabs) {
                indentLevel = prefs.getInt(SimpleValueNames.SPACES_PER_TAB, 4);
            } else {
                indentLevel = prefs.getInt(SimpleValueNames.TAB_SIZE, 8);
            }
        }

        assert indentLevel >= 0 : "Invalid indentLevelSize " + indentLevel + " for " + doc; //NOI18N
        return indentLevel;
    }

    /**
     * Get number of spaces that visually substitute '\t' character.
     * 
     * @return &gt;=0 size corresponding to '\t' character in spaces.
     */
    public static int tabSize(Document doc) {
        int tabSize = CodeStylePreferences.get(doc).getPreferences().getInt(SimpleValueNames.TAB_SIZE, 8);
        assert tabSize > 0 : "Invalid tabSize " + tabSize + " for " + doc; //NOI18N
        return tabSize;
    }

    /**
     * Get whether the indentation strings should contain hard tabs '\t'
     * or whether they should only contain spaces.
     * 
     * @return true if the tabs should be expanded or false if not.
     */
    public static boolean isExpandTabs(Document doc) {
        return CodeStylePreferences.get(doc).getPreferences().getBoolean(SimpleValueNames.EXPAND_TABS, true);
    }
    
    /**
     * Get start offset of a line in a document.
     * 
     * @param doc non-null document.
     * @param offset &gt;= 0 offset anywhere on the line.
     * @throws BadLocationException for invalid offset
     */
    public static int lineStartOffset(Document doc, int offset) throws BadLocationException {
        IndentImpl.checkOffsetInDocument(doc, offset);
        Element lineRootElement = IndentImpl.lineRootElement(doc);
        return lineRootElement.getElement(lineRootElement.getElementIndex(offset)).getStartOffset();
    }

    /**
     * Get indentation of a line in a document as a number of spaces.
     * 
     * @param doc non-null document.
     * @param lineStartOffset &gt;= 0 start offset of a line in the document.
     * @throws BadLocationException for invalid offset
     */
    public static int lineIndent(Document doc, int lineStartOffset) throws BadLocationException {
        IndentImpl.checkOffsetInDocument(doc, lineStartOffset);
        CharSequence docText = DocumentUtilities.getText(doc);
        int indent = 0;
        int tabSize = -1;
        while (lineStartOffset < docText.length()) {
            char ch;
            switch (ch = docText.charAt(lineStartOffset)) {
                case '\n': //NOI18N
                    return indent;

                case '\t': //NOI18N
                    if (tabSize == -1)
                        tabSize = tabSize(doc);
                    // Round to next tab stop
                    indent = (indent + tabSize) / tabSize * tabSize;
                    break;

                default:
                    if (Character.isWhitespace(ch)) {
                        indent++;
                    } else {
                        return indent;
                    }
            }
            lineStartOffset++;
        }
        return indent;
    }
    
    /**
     * Create (or get from cache) indentation string for the given indent.
     * <br/>
     * The indentation settings (tab-size etc. are determined based on the given
     * document).
     * 
     * @param doc document from which the indentation settings will be retrieved.
     * @param indent &gt;=0 indentation in number of spaces.
     * @return indentation string containing tabs and spaces according to the document's
     *  settings (tab-size etc.).
     */
    public static String createIndentString(Document doc, int indent) {
        if (indent < 0) {
            throw new IllegalArgumentException("indent=" + indent + " < 0"); // NOI18N
        }
        return cachedOrCreatedIndentString(indent, isExpandTabs(doc), tabSize(doc));
    }
    
    /**
     * Create (or get from cache) indentation string for the given indent while knowing
     * whether tabs are exapnded and tabSize value.
     * 
     * @param indent &gt;=0 indentation in number of spaces.
     * @param expandTabs true if no tab characters '\t' should be used for indentation string
     * (only spaces will be used).
     * @param tabSize number of spaces equal to each '\t' character used in indentation string.
     *  <br/>This only applies if expandTabs == false.
     *  <br/>For example if indent == 20 and tabSize == 8 then indent string
     *  would contain two tabs followed by four spaces.
     * @return indentation string containing tabs and spaces according to the given parameters
     * @since 1.22
     */
    public static String createIndentString(int indent, boolean expandTabs, int tabSize) {
        return cachedOrCreatedIndentString(indent, expandTabs, tabSize);
    }

    static String cachedOrCreatedIndentString(int indent, boolean expandTabs, int tabSize) {
        String indentString;
        if (expandTabs || (indent < tabSize)) {
            if (indent <= MAX_CACHED_INDENT) {
                synchronized (cachedSpacesStrings) {
                    indentString = cachedSpacesStrings[indent];
                    if (indentString == null) {
                        // Create string with MAX_CACHED_SPACES spaces first if not cached yet
                        indentString = cachedSpacesStrings[MAX_CACHED_INDENT];
                        if (indentString == null) {
                            indentString = createSpacesString(MAX_CACHED_INDENT);
                            cachedSpacesStrings[MAX_CACHED_INDENT] = indentString;
                        }
                        indentString = indentString.substring(0, indent);
                        cachedSpacesStrings[indent] = indentString;
                    }
                }
            } else {
                indentString = createSpacesString(indent);
            }

        } else { // Do not expand tabs
            if (indent <= MAX_CACHED_INDENT && tabSize <= MAX_CACHED_TAB_SIZE) {
                synchronized (cachedTabIndents) {
                    String[] tabIndents = cachedTabIndents[tabSize];
                    if (tabIndents == null) {
                        // Do not cache spaces-only strings
                        tabIndents = new String[MAX_CACHED_INDENT - tabSize + 1];
                        cachedTabIndents[tabSize] = tabIndents;
                    }
                    indentString = tabIndents[indent - tabSize];
                    if (indentString == null) {
                        indentString = createTabIndentString(indent, tabSize);
                        tabIndents[indent - tabSize] = indentString;
                    }
                }
            } else {
                indentString = createTabIndentString(indent, tabSize);
            }
        }
        return indentString;
    }
    
    private static String createSpacesString(int spaceCount) {
        StringBuilder sb = new StringBuilder(spaceCount);
        ArrayUtilities.appendSpaces(sb, spaceCount);
        return sb.toString();
    }
    
    private static String createTabIndentString(int indent, int tabSize) {
        StringBuilder sb = new StringBuilder();
        while (indent >= tabSize) {
            sb.append('\t'); //NOI18N
            indent -= tabSize;
        }
        ArrayUtilities.appendSpaces(sb, indent);
        return sb.toString();
    }
    
}

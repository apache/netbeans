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

package org.netbeans.editor;

import java.util.Map;
import java.io.IOException;
import java.io.Writer;
import java.io.CharArrayWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.WeakHashMap;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.deprecated.pre65formatting.LegacyFormattersProvider;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib.KitsTracker;
import org.netbeans.modules.editor.lib.SettingsConversions;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 * Various services related to indentation and text formatting
 * are located here. Each kit can have different formatter
 * so the first action should be getting the right formatter
 * for the given kit by calling Formatter.getFormatter(kitClass).
 *
 * @author Miloslav Metelka
 * @version 1.00
 *
 * @deprecated Please use Editor Indentation API instead, for details see
 *   <a href="@org-netbeans-modules-editor-indent@/overview-summary.html">Editor Indentation</a>.
 */
@Deprecated
public class Formatter {

    private static final Map<Class, Formatter> kitClass2Formatter = new WeakHashMap<Class, Formatter>();
    private static final Map<MimePath, Formatter> mimePath2Formatter = new WeakHashMap<MimePath, Formatter>();

    /** 
     * Gets <code>Formatter</code> implementation for given editor kit's
     * implementation class.
     * 
     * @param kitClass The editor kit's implementation class to get the
     *   <code>Formatter</code> for.
     * 
     * @return <code>Formatter</code> implementation created by the editor kit.
     * @deprecated Use of editor kit's implementation classes is deprecated
     *   in favor of mime types.
     */
    @Deprecated
    public static synchronized Formatter getFormatter(Class kitClass) {
        String mimeType = KitsTracker.getInstance().findMimeType(kitClass);
        if (mimeType != null) {
            return getFormatter(mimeType);
        } else {
            Formatter formatter = kitClass2Formatter.get(kitClass);
            if (formatter == null) {
                BaseKit kit = BaseKit.getKit(kitClass);
                formatter = callCreateFormatterMethod(kit);
                kitClass2Formatter.put(kitClass, formatter);
            }
            return formatter;
        }
    }
    
    /**
     * Gets <code>Formatter</code> implementation for given mime type.
     * 
     * @param mimeType The mime type to get the <code>Formatter</code> for.
     * 
     * @return <code>Formatter</code> implementation created by the mime
     *   type's editor kit.
     * @deprecated Use Editor Indentation API.
     * @since 1.18
     */
    @Deprecated
    public static synchronized Formatter getFormatter(String mimeType) {
        MimePath mimePath = MimePath.parse(mimeType);
        Formatter formatter = mimePath2Formatter.get(mimePath);
        
        if (formatter == null) {
            EditorKit editorKit = MimeLookup.getLookup(mimePath).lookup(EditorKit.class);
            if (editorKit instanceof BaseKit) {
                formatter = callCreateFormatterMethod((BaseKit) editorKit);
            } else {
                formatter = callCreateFormatterMethod(BaseKit.getKit(BaseKit.class));
            }
            mimePath2Formatter.put(mimePath, formatter);
        }
        
        return formatter;
    }
    
    private static synchronized void setFormatter(String mimeType, Formatter formatter) {
        mimePath2Formatter.put(MimePath.parse(mimeType), formatter);
    }
    
    /** 
     * Sets the formatter for the given kit-class.
     * 
     * @param kitClass class of the kit for which the formatter
     *  is being assigned.
     * @param formatter new formatter for the given kit
     * 
     * @deprecated Use Editor Indentation API.
     */
    @Deprecated
    public static synchronized void setFormatter(Class kitClass, Formatter formatter) {
        String mimeType = KitsTracker.getInstance().findMimeType(kitClass);
        if (mimeType != null) {
            setFormatter(mimeType, formatter);
        } else {
            kitClass2Formatter.put(kitClass, formatter);
        }
    }


    /** Maximum tab size for which the indent strings will be cached. */
    private static final int ISC_MAX_TAB_SIZE = 16;
    
    /** Cache the indentation strings up to this size */
    private static final int ISC_MAX_INDENT_SIZE = 32;
    
    /** Cache holding the indentation strings for various tab-sizes. */
    private static final String[][] indentStringCache
        = new String[ISC_MAX_TAB_SIZE][];


    private final Class kitClass;

    /** Whether values were already inited from the cache */
    private boolean inited;

    private int tabSize;

    private boolean customTabSize;

    private Integer shiftWidth;

    private boolean customShiftWidth;

    private boolean expandTabs;

    private boolean customExpandTabs;

    private int spacesPerTab;

    private boolean customSpacesPerTab;

    private final Preferences prefs;
    private final PreferenceChangeListener prefsListener = new PreferenceChangeListener() {
        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt == null ? null : evt.getKey();
            if (!inited || key == null || SimpleValueNames.TAB_SIZE.equals(key)) {
                if (!customTabSize) {
                    tabSize = prefs.getInt(SimpleValueNames.TAB_SIZE, EditorPreferencesDefaults.defaultTabSize);
                }
            }

            // Shift-width often depends on the rest of parameters
            if (!customShiftWidth) {
                int shw = prefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, -1);
                if (shw >= 0) {
                    shiftWidth = shw;
                }
            }

            if (!inited || key == null || SimpleValueNames.EXPAND_TABS.equals(key)) {
                if (!customExpandTabs) {
                    expandTabs = prefs.getBoolean(SimpleValueNames.EXPAND_TABS, EditorPreferencesDefaults.defaultExpandTabs);
                }
            }
            
            if (!inited || key == null || SimpleValueNames.SPACES_PER_TAB.equals(key)) {
                if (!customSpacesPerTab) {
                    spacesPerTab = prefs.getInt(SimpleValueNames.SPACES_PER_TAB, EditorPreferencesDefaults.defaultSpacesPerTab);
                }
            }

            inited = true;
            
            SettingsConversions.callSettingsChange(Formatter.this);
        }
    };
    
    /** Construct new formatter.
    * @param kitClass the class of the kit for which this formatter is being
    *  constructed.
    */
    public Formatter(Class kitClass) {
        this.kitClass = kitClass;
        
        String mimeType = BaseKit.getKit(kitClass).getContentType();
        prefs = MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(Preferences.class);
        prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs));
    }

    /** Get the kit-class for which this formatter is constructed. */
    public Class getKitClass() {
        return kitClass;
    }

    // ------------------------------------------------------------------------
    // Formatting Settings setters and getters
    // ------------------------------------------------------------------------

    /** Get the number of spaces the TAB character ('\t') visually represents
     * for non-BaseDocument documents. It shouldn't be used for BaseDocument
     * based documents. The reason for that is that the returned value 
     * reflects the value of the setting for the kit class over which
     * this formatter was constructed. However it's possible that the kit class of
     * the document being formatted is different than the kit of the formatter.
     * For example java document could be formatted by html formatter.
     * Therefore <code>BaseDocument.getTabSize()</code> must be used
     * for BaseDocuments to reflect the document's own tabsize.
     * @see BaseDocument.getTabSize()
     */
    public int getTabSize() {
        Document doc = LegacyFormattersProvider.getFormattingContextDocument();
        if (doc != null) {
            Object ret = callIndentUtils("tabSize", doc); //NOI18N
            if (ret instanceof Integer) {
                return (Integer) ret;
            }
        }

        if (!customTabSize && !inited) {
            prefsListener.preferenceChange(null);
        }

        return tabSize;
    }

    /** Set the number of spaces the TAB character ('\t') visually represents
     * for non-BaseDocument documents. It doesn't affect BaseDocument
     * based documents.
     *
     * @see getTabSize()
     * @see BaseDocument.setTabSize()
     */
    public void setTabSize(int tabSize) {
        customTabSize = true;
        this.tabSize = tabSize;
    }


    /** Get the width of one indentation level for non-BaseDocument documents.
     * The algorithm first checks whether there's a value for the INDENT_SHIFT_WIDTH
     * setting. If so it uses it, otherwise it uses <code>getSpacesPerTab()</code>
     * 
     * @see setShiftWidth()
     * @see getSpacesPerTab()
     */
    public int getShiftWidth() {
        Document doc = LegacyFormattersProvider.getFormattingContextDocument();
        if (doc != null) {
            Object ret = callIndentUtils("indentLevelSize", doc); //NOI18N
            if (ret instanceof Integer) {
                return (Integer) ret;
            }
        }

        if (!customShiftWidth && !inited) {
            prefsListener.preferenceChange(null);
        }

        return (shiftWidth != null) ? shiftWidth.intValue() : getSpacesPerTab();
    }

    /** Set the width of one indentation level for non-BaseDocument documents.
     * It doesn't affect BaseDocument based documents.
     *
     * @see getShiftWidth()
     */
    public void setShiftWidth(int shiftWidth) {
        customShiftWidth = true;
        if (this.shiftWidth == null || this.shiftWidth.intValue() != shiftWidth) {
            this.shiftWidth = new Integer(shiftWidth);
        }
    }

    /** Should the typed tabs be expanded to the spaces? */
    public boolean expandTabs() {
        Document doc = LegacyFormattersProvider.getFormattingContextDocument();
        if (doc != null) {
            Object ret = callIndentUtils("isExpandTabs", doc); //NOI18N
            if (ret instanceof Boolean) {
                return (Boolean) ret;
            }
        }

        if (!customExpandTabs && !inited) {
            prefsListener.preferenceChange(null);
        }

        return expandTabs;
    }

    public void setExpandTabs(boolean expandTabs) {
        customExpandTabs = true;
        this.expandTabs = expandTabs;
    }

    /** Get the number of spaces that should be inserted into the document
    * instead of one typed tab.
    */
    public int getSpacesPerTab() {
        Document doc = LegacyFormattersProvider.getFormattingContextDocument();
        if (doc != null) {
            Object ret = callIndentUtils("indentLevelSize", doc); //NOI18N
            if (ret instanceof Integer) {
                return (Integer) ret;
            }
        }

        if (!customSpacesPerTab && !inited) {
            prefsListener.preferenceChange(null);
        }

        return spacesPerTab;
    }

    public void setSpacesPerTab(int spacesPerTab) {
        customSpacesPerTab = true;
        this.spacesPerTab = spacesPerTab;
    }

    // ------------------------------------------------------------------------
    // Pure API methods
    // ------------------------------------------------------------------------

    public String getIndentString(BaseDocument doc, int indent) {
        LegacyFormattersProvider.pushFormattingContextDocument(doc);
        try {
            return getIndentString(indent, expandTabs(), doc.getTabSize());
        } finally {
            LegacyFormattersProvider.popFormattingContextDocument(doc);
        }
    }
        
    /** Get the string that is appropriate for the requested indentation.
    * The returned string respects the <tt>expandTabs()</tt> and
    * the <tt>getTabSize()</tt> and will contain either spaces only
    * or fully or partially tabs as necessary.
    */
    public String getIndentString(int indent) {
        return getIndentString(indent, expandTabs(), getTabSize());
    }

    /** Modify the line to move the text starting at dotPos one tab
     * column to the right.  Whitespace preceeding dotPos may be
     * replaced by a TAB character if tabs expanding is on.
     * @param doc document to operate on
     * @param dotPos insertion point
     */
    public void insertTabString (final BaseDocument doc, final int dotPos)
    throws BadLocationException {
        LegacyFormattersProvider.pushFormattingContextDocument(doc);
        try {
            final BadLocationException[] badLocationExceptions = new BadLocationException [1];
            doc.runAtomic (new Runnable () {
                public void run () {
                    try {
                        // Determine first white char before dotPos
                        int rsPos = Utilities.getRowStart(doc, dotPos);
                        int startPos = Utilities.getFirstNonWhiteBwd(doc, dotPos, rsPos);
                        startPos = (startPos >= 0) ? (startPos + 1) : rsPos;

                        int startCol = Utilities.getVisualColumn(doc, startPos);
                        int endCol = Utilities.getNextTabColumn(doc, dotPos);
                        String tabStr = Analyzer.getWhitespaceString(startCol, endCol, expandTabs(), doc.getTabSize());

                        // Search for the first non-common char
                        char[] removeChars = doc.getChars(startPos, dotPos - startPos);
                        int ind = 0;
                        while (ind < removeChars.length && removeChars[ind] == tabStr.charAt(ind)) {
                            ind++;
                        }

                        startPos += ind;
                        doc.remove(startPos, dotPos - startPos);
                        doc.insertString(startPos, tabStr.substring(ind), null);
                    } catch (BadLocationException ex) {
                        badLocationExceptions [0] = ex;
                    }
                }
            });
            if (badLocationExceptions[0] != null)
                throw badLocationExceptions [0];
        } finally {
            LegacyFormattersProvider.popFormattingContextDocument(doc);
        }
    }

    /** Change the indent of the given row. Document is atomically locked
    * during this operation.
    */
    public void changeRowIndent (final BaseDocument doc, final int pos, final int newIndent)
    throws BadLocationException {
        LegacyFormattersProvider.pushFormattingContextDocument(doc);
        try {
            final BadLocationException[] badLocationExceptions = new BadLocationException [1];
            doc.runAtomic (new Runnable () {
                public void run () {
                    try {
                        int indent = newIndent < 0 ? 0 : newIndent;
                        int firstNW = Utilities.getRowFirstNonWhite(doc, pos);
                        if (firstNW == -1) { // valid first non-blank
                            firstNW = Utilities.getRowEnd(doc, pos);
                        }
                        int replacePos = Utilities.getRowStart(doc, pos);
                        int removeLen = firstNW - replacePos;
                        CharSequence removeText = DocumentUtilities.getText(doc, replacePos, removeLen);
                        String newIndentText = getIndentString(doc, indent);
                        if (CharSequenceUtilities.startsWith(newIndentText, removeText)) {
                            // Skip removeLen chars at start
                            newIndentText = newIndentText.substring(removeLen);
                            replacePos += removeLen;
                            removeLen = 0;
                        } else if (CharSequenceUtilities.endsWith(newIndentText, removeText)) {
                            // Skip removeLen chars at the end
                            newIndentText = newIndentText.substring(0, newIndentText.length() - removeLen);
                            removeLen = 0;
                        }

                        if (removeLen != 0) {
                            doc.remove(replacePos, removeLen);
                        }

                        doc.insertString(replacePos, newIndentText, null);
                    } catch (BadLocationException ex) {
                        badLocationExceptions [0] = ex;
                    }
                }
            });
            if (badLocationExceptions[0] != null)
                throw badLocationExceptions [0];
        } finally {
            LegacyFormattersProvider.popFormattingContextDocument(doc);
        }
    }

    /** Increase/decrease indentation of the block of the code. Document
    * is atomically locked during the operation.
    * @param doc document to operate on
    * @param startPos starting line position
    * @param endPos ending line position
    * @param shiftCnt positive/negative count of shiftwidths by which indentation
    *   should be shifted right/left
    */
    public void changeBlockIndent (final BaseDocument doc, final int startPos, final int endPos,
                                  final int shiftCnt) throws BadLocationException {
        LegacyFormattersProvider.pushFormattingContextDocument(doc);
        try {
            GuardedDocument gdoc = (doc instanceof GuardedDocument)
                                   ? (GuardedDocument)doc : null;
            if (gdoc != null){
                for (int i = startPos; i<endPos; i++){
                    if (gdoc.isPosGuarded(i)){
                        java.awt.Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                }
            }

            final BadLocationException[] badLocationExceptions = new BadLocationException [1];
            doc.runAtomic (new Runnable () {
                public void run () {
                    try {
                        int indentDelta = shiftCnt * doc.getShiftWidth();
                        int end = (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) ?
                            endPos - 1 : endPos;

                        int pos = Utilities.getRowStart(doc, startPos );
                        for (int lineCnt = Utilities.getRowCount(doc, startPos, end);
                                lineCnt > 0; lineCnt--
                            ) {
                            int indent = Utilities.getRowIndent(doc, pos);
                            if (Utilities.isRowWhite(doc, pos)) {
                                indent = -indentDelta; // zero indentation for white line
                            }
                            changeRowIndent(doc, pos, Math.max(indent + indentDelta, 0));
                            pos = Utilities.getRowStart(doc, pos, +1);
                        }
                    } catch (BadLocationException ex) {
                        badLocationExceptions [0] = ex;
                    }
                }
            });
            if (badLocationExceptions[0] != null)
                throw badLocationExceptions [0];
        } finally {
            LegacyFormattersProvider.popFormattingContextDocument(doc);
        }
    }

    /** Shift line either left or right */
    public void shiftLine(BaseDocument doc, int dotPos, boolean right)
    throws BadLocationException {
        LegacyFormattersProvider.pushFormattingContextDocument(doc);
        try {
            int ind = doc.getShiftWidth();
            if (!right) {
                ind = -ind;
            }

            if (Utilities.isRowWhite(doc, dotPos)) {
                ind += Utilities.getVisualColumn(doc, dotPos);
            } else {
                ind += Utilities.getRowIndent(doc, dotPos);
            }
            ind = Math.max(ind, 0);
            changeRowIndent(doc, dotPos, ind);
        } finally {
            LegacyFormattersProvider.popFormattingContextDocument(doc);
        }
    }

    /** Reformat a block of code.
    * @param doc document to work with
    * @param startOffset offset at which the formatting starts
    * @param endOffset offset at which the formatting ends
    * @return length of the reformatted code
    */
    public int reformat(BaseDocument doc, int startOffset, int endOffset)
    throws BadLocationException {
        LegacyFormattersProvider.pushFormattingContextDocument(doc);
        try {
            try {
                CharArrayWriter cw = new CharArrayWriter();
                Writer w = createWriter(doc, startOffset, cw);
                String originalString = doc.getText(startOffset, endOffset - startOffset);
                w.write(originalString);
                w.close();
                String out = new String(cw.toCharArray());
                if(!out.equals(originalString)){
                    doc.remove(startOffset, endOffset - startOffset);
                    doc.insertString(startOffset, out, null);
                    return out.length();
                }else{
                    //nothing changed
                    return 0;
                }
            } catch (IOException e) {
                Utilities.annotateLoggable(e);
                return 0;
            }
        } finally {
            LegacyFormattersProvider.popFormattingContextDocument(doc);
        }
    }

    // ------------------------------------------------------------------------
    // Pure SPI methods
    // ------------------------------------------------------------------------

    /** Indents the current line. Should not affect any other
    * lines.
    * @param doc the document to work on
    * @param offset the offset of a character on the line
    * @return new offset of the original character
    */
    public int indentLine(Document doc, int offset) {
        return offset;
    }

    /** Inserts new line at given position and indents the new line with
    * spaces.
    *
    * @param doc the document to work on
    * @param offset the offset of a character on the line
    * @return new offset to place cursor to
    */
    public int indentNewLine(Document doc, int offset) {
        try {
            doc.insertString(offset, "\n", null); // NOI18N
            offset++;

        } catch (GuardedException e) {
            java.awt.Toolkit.getDefaultToolkit().beep();

        } catch (BadLocationException e) {
            Utilities.annotateLoggable(e);
        }

        return offset;
    }

    /** Creates a writer that formats text that is inserted into it.
    * The writer should not modify the document but use the 
    * provided writer to write to. Usually the underlaying writer
    * will modify the document itself and optionally it can remember
    * the current position in document. That is why the newly created
    * writer should do no buffering.
    * <P>
    * The provided document and pos are only informational,
    * should not be modified but only used to find correct indentation
    * strategy.
    *
    * @param doc document 
    * @param offset position to begin inserts at
    * @param writer writer to write to
    * @return new writer that will format written text and pass it
    *   into the writer
    */
    public Writer createWriter(Document doc, int offset, Writer writer) {
        return writer;
    }

    // ------------------------------------------------------------------------
    // Methods that are called by API clients and overriden by SPI implementors
    // ------------------------------------------------------------------------

    /**
     * Formatter clients should call this method
     * before acquiring of the document's write lock
     * and using of the {@link #indentLine(Document,int)}
     * and {@link #indentNewLine(Document,int)} methods.
     * <br/>
     * Subclasses may override this method
     * and perform necessary pre-locking (e.g. of java infrastructure).
     * <br/>
     * The following pattern should be used:
     * <pre>
     * formatter.indentLock();
     * try {
     *     doc.atomicLock();
     *     try {
     *         formatter.indentLine(...);
     *     } finally {
     *         doc.atomicUnlock();
     *     }
     * } finally {
     *     formatter.indentUnlock();
     * }
     * </pre>
     */
    public void indentLock() {
        // No extra locking by default
    }
    
    /**
     * Formatter clients should call this method 
     * after releasing of the document's write lock
     * as a counterpart of {@link #indentLock()}.
     * <br/>
     * Subclasses may override this method
     * and perform necessary post-unlocking (e.g. of java infrastructure).
     * <br/>
     * The following pattern should be used:
     * <pre>
     * formatter.indentLock();
     * try {
     *     doc.atomicLock();
     *     try {
     *         formatter.indentLine(...);
     *     } finally {
     *         doc.atomicUnlock();
     *     }
     * } finally {
     *     formatter.indentUnlock();
     * }
     * </pre>
     */
    public void indentUnlock() {
        // No extra locking by default
    }

    /**
     * Formatter clients should call this method 
     * before acquiring of the document's write lock
     * before using of {@link #reformat(BaseDocument,int,int)} method.
     * <br/>
     * Subclasses may override this method
     * and perform necessary pre-locking (e.g. of java infrastructure).
     * <br/>
     * The following pattern should be used:
     * <pre>
     * formatter.reformatLock();
     * try {
     *     doc.atomicLock();
     *     try {
     *         formatter.reformat(...);
     *     } finally {
     *         doc.atomicUnlock();
     *     }
     * } finally {
     *     formatter.reformatUnlock();
     * }
     * </pre>
     */
    public void reformatLock() {
        // No extra locking by default
    }
    
    /**
     * Formatter clients should call this method 
     * after releasing of the document's write lock
     * as a counterpart of {@link #reformatLock()}.
     * <br/>
     * Subclasses may override this method
     * and perform necessary post-unlocking (e.g. of java infrastructure).
     * <br/>
     * The following pattern should be used:
     * <pre>
     * formatter.reformatLock();
     * try {
     *     doc.atomicLock();
     *     try {
     *         formatter.reformat(...);
     *     } finally {
     *         doc.atomicUnlock();
     *     }
     * } finally {
     *     formatter.reformatUnlock();
     * }
     * </pre>
     */
    public void reformatUnlock() {
        // No extra locking by default
    }

    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------

    private static boolean noIndentUtils = false;
    private static WeakReference<Class> indentUtilsClassRef = null;
    private static Object callIndentUtils(String methodName, Document doc) {
        if (noIndentUtils) {
            return null;
        }
        
        Class indentUtilsClass = indentUtilsClassRef == null ? null : indentUtilsClassRef.get();
        if (indentUtilsClass == null) {
            try {
                ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                indentUtilsClass = loader.loadClass("org.netbeans.modules.editor.indent.api.IndentUtils"); //NOI18N
                indentUtilsClassRef = new WeakReference<Class>(indentUtilsClass);
            } catch (Exception e) {
                noIndentUtils = true;
                return null;
            }
        }

        try {
            Method m = indentUtilsClass.getDeclaredMethod(methodName, Document.class);
            return m.invoke(null, doc);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getIndentString(int indent, boolean expandTabs, int tabSize) {
        if (indent <= 0) {
            return "";
        }

        if (expandTabs) { // store in 0th slot
            tabSize = 0;
        }

        synchronized (indentStringCache) {
            boolean large = (tabSize >= indentStringCache.length)
                || (indent > ISC_MAX_INDENT_SIZE); // indexed by (indent - 1)
            String indentString = null;
            String[] tabCache = null;
            if (!large) {
                tabCache = indentStringCache[tabSize]; // cache for this tab
                if (tabCache == null) {
                    tabCache = new String[ISC_MAX_INDENT_SIZE];
                    indentStringCache[tabSize] = tabCache;
                }
                indentString = tabCache[indent - 1];
            }

            if (indentString == null) {
                indentString = Analyzer.getIndentString(indent, expandTabs, tabSize);

                if (!large) {
                    tabCache[indent - 1] = indentString;
                }
            }

            return indentString;
        }
    }

    private static Formatter callCreateFormatterMethod(BaseKit kit) {
        try {
            Method m = kit.getClass().getMethod("createFormatter"); //NOI18N
            return (Formatter) m.invoke(kit);
        } catch (Exception e) {
            return new Formatter(kit.getClass());
        }
    }
}

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

package org.netbeans.editor.ext;

import java.io.CharArrayWriter;
import java.io.Writer;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.GuardedException;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Syntax;
import org.netbeans.modules.editor.lib2.EditorPreferencesKeys;
import org.netbeans.modules.editor.lib.SettingsConversions;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 * Unlike the formatter class, the ExtFormatter concentrates
 * on providing a support for the real formatting process.
 * Each formatter (there's only one per each kit) can contain
 * one or more formatting layers. The <tt>FormatLayer</tt>
 * operates over the chain of the tokens provided
 * by the <tt>FormatWriter</tt>. The formatting consist
 * of changing the chain of the tokens until it gets
 * the desired look.
 * Each formatting requires a separate instance
 * of <tt>FormatWriter</tt> but the same set of format-layers
 * is used for all the format-writers. Although the base
 * implementation is synchronized so that only one
 * format-writer at time is processed by each format-writer,
 * in general it's not necessary.
 * The basic implementation processes all the format-layers
 * sequentialy in the order they were added to the formatter
 * but this can be redefined.
 * The <tt>getSettingValue</tt> enables to get the up-to-date
 * value for the particular setting.
 *
 * @author Miloslav Metelka
 * @version 1.00
 *
 * @deprecated Please use Editor Indentation API instead, for details see
 *   <a href="@org-netbeans-modules-editor-indent@/overview-summary.html">Editor Indentation</a>.
 */
@Deprecated
public class ExtFormatter extends Formatter implements FormatLayer {

    /** List holding the format layers */
    private List formatLayerList = new ArrayList();

    /** Use this instead of testing by containsKey() */
    private static final Object NULL_VALUE = new Object();

    /** Map that contains the requested [setting-name, setting-value] pairs */
    private final HashMap settingsMap = new HashMap();

    private Acceptor indentHotCharsAcceptor;
    private boolean reindentWithTextBefore;

    private final String mimeType;
    private final Preferences prefs;
    private final PreferenceChangeListener prefsListener = new PreferenceChangeListener() {
        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt == null ? null : evt.getKey();
            if (key == null || EditorPreferencesKeys.INDENT_HOT_CHARS_ACCEPTOR.equals(key)) {
                indentHotCharsAcceptor = (Acceptor) SettingsConversions.callFactory(
                    prefs, MimePath.parse(mimeType), EditorPreferencesKeys.INDENT_HOT_CHARS_ACCEPTOR, AcceptorFactory.FALSE);
            }

            if (key == null || EditorPreferencesKeys.REINDENT_WITH_TEXT_BEFORE.equals(key)) {
                reindentWithTextBefore = prefs.getBoolean(EditorPreferencesKeys.REINDENT_WITH_TEXT_BEFORE, false);
            }
        }
    };
    
    public ExtFormatter(Class kitClass) {
        super(kitClass);

        initFormatLayers();

        this.mimeType = BaseKit.getKit(kitClass).getContentType();
        prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs));
    }

    /** Add the desired format-layers to the formatter */
    protected void initFormatLayers() {
    }

    /** Return the name of this formatter. By default
     * it's the name of the kit-class for which it's created
     * without the package name.
     */
    public String getName() {
        return getKitClass().getName().substring(
                getKitClass().getName().lastIndexOf('.') + 1); //NOI18N
    }

    /** Get the value of the given setting.
    * @param settingName name of the setting to get.
    */
    public Object getSettingValue(String settingName) {
        synchronized (settingsMap) {
            Object value = settingsMap.get(settingName);
            if (value != null) {
                return (value != NULL_VALUE) ? value : null;
            }
        }
        
        try {
            ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
            Class settingsClass = loader.loadClass("org.netbeans.editor.Settings"); //NOI18N
            Method m = settingsClass.getMethod("getValue", Class.class, String.class); //NOI18N
            return m.invoke(null, getKitClass(), settingName);
        } catch (Exception e) {
            return null;
        }
    }

    /** This method allows to set a custom value to a setting thus
     * overriding the value retrieved from the <tt>Settings</tt>.
     * Once done the value is no longer synchronized with the changes
     * in <tt>Settings</tt> for the particular setting.
     * There's a map holding the names of all the custom
     * settings.
     */
    public void setSettingValue(String settingName, Object settingValue) {
        synchronized (settingsMap) {
            settingsMap.put(settingName, settingValue == null ? NULL_VALUE : settingValue);
        }
    }

    /** Add the new format layer to the layer hierarchy.
    */
    public synchronized void addFormatLayer(FormatLayer layer) {
        formatLayerList.add(layer);
    }

    /** Replace the format-layer with the layerName
    * with the the given layer. If there's no such layer with the same
    * name, the layer is not replaced and false is returned.
    */
    public synchronized boolean replaceFormatLayer(String layerName, FormatLayer layer) {
        int cnt = formatLayerList.size();
        for (int i = 0; i < cnt; i++) {
            if (layerName.equals(((FormatLayer)formatLayerList.get(i)).getName())) {
                formatLayerList.set(i, layer);
                return true;
            }
        }
        return false;
    }

    /** Remove the first layer which has the same name as the given one.
    */
    public synchronized void removeFormatLayer(String layerName) {
        Iterator it = formatLayerIterator();
        while (it.hasNext()) {
            if (layerName.equals(((FormatLayer)it.next()).getName())) {
                it.remove();
                return;
            }
        }
    }

    /** Get the iterator over the format layers.
    */
    public Iterator formatLayerIterator() {
        return formatLayerList.iterator();
    }

    /** Whether do no formatting at all. If this method returns true,
     * the FormatWriter will simply write its input into the underlying
     * writer.
     */
    public boolean isSimple() {
        return false;
    }

    /** Called by format-writer to do the format */
    public synchronized void format(FormatWriter fw) {
        boolean done = false;
        int safetyCounter = 0;
        do {
            // Mark the chain as unmodified at the begining
            fw.setChainModified(false);
            fw.setRestartFormat(false);

            Iterator it = formatLayerIterator();
            while (it.hasNext()) {
                ((FormatLayer)it.next()).format(fw);
                if (fw.isRestartFormat()) {
                    break;
                }
            }

            if (!it.hasNext() && !fw.isRestartFormat()) {
                done = true;
            }

            if (safetyCounter > 1000) { // prevent infinite loop
                new Exception("Indentation infinite loop detected").printStackTrace(); // NOI18N
                break;
            }
        } while (!done);
    }

    /** Reformat a block of code.
    * @param doc document to work with
    * @param startOffset position at which the formatting starts
    * @param endOffset position at which the formatting ends
    * @param indentOnly whether just the indentation should be changed
    *  or regular formatting should be performed.
    * @return formatting writer. The text was already reformatted
    *  but the writer can contain useful information.
    */
    public Writer reformat(BaseDocument doc, int startOffset, int endOffset, boolean indentOnly) throws BadLocationException, IOException {
        pushFormattingContextDocument(doc);
        try {
            CharArrayWriter cw = new CharArrayWriter();
            Writer w = createWriter(doc, startOffset, cw);
            FormatWriter fw = (w instanceof FormatWriter) ? (FormatWriter)w : null;

            boolean fix5620 = true; // whether apply fix for #5620 or not

            if (fw != null) {
                fw.setIndentOnly(indentOnly);
                if (fix5620) {
                    fw.setReformatting(true); // #5620
                }
            }

            w.write(doc.getChars(startOffset, endOffset - startOffset));
            w.close();

            if (!fix5620 || fw == null) { // #5620 - for (fw != null) the doc was already modified
                String out = new String(cw.toCharArray());
                doc.remove(startOffset, endOffset - startOffset);
                doc.insertString(startOffset, out, null);
            }

            return w;
        } finally {
            popFormattingContextDocument(doc);
        }
    }

    /** Fix of #5620 - same method exists in Formatter (predecessor */
    public @Override int reformat(BaseDocument doc, int startOffset, int endOffset)
    throws BadLocationException {
        try {
            javax.swing.text.Position pos = doc.createPosition(endOffset);
            reformat(doc, startOffset, endOffset, false);
            return pos.getOffset() - startOffset;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /** Get the block to be reformatted after keystroke was pressed.
     * @param target component to which the text was typed. Caaret position
     *  can be checked etc.
     * @param typedText text (usually just one character) that the user has typed.
     * @return block of the code to be reformatted or null if nothing should
     *  reformatted. It can return block containing just one character. The caller
     *  usually expands even one character to the whole line because less than
     *  the whole line usually doesn't provide enough possibilities for formatting.
     * @see ExtKit.ExtDefaultKeyTypedAction.checkIndentHotChars()
     */
    public int[] getReformatBlock(JTextComponent target, String typedText) {
        if (indentHotCharsAcceptor == null) { // init if necessary
            prefsListener.preferenceChange(null);
        }

        if (indentHotCharsAcceptor.accept(typedText.charAt(0))) {
            /* This is bugfix 10771. See the issue for problem description.
             * The behaviour before fix was that whenever the lbrace is
             * entered, the line is indented. This make no sense if a text
             * exist on the line before the lbrace. In this case we
             * simply will not indent the line. This is handled by the hasTextBefore 
             * check
             */
            if(!reindentWithTextBefore) {
                if(hasTextBefore(target, typedText)) {
                    return null;
                }
            }
            int dotPos = target.getCaret().getDot();
            return new int[] { Math.max(dotPos - 1, 0), dotPos };
            
        } else {
            return null;
        }
    }

    protected boolean hasTextBefore(JTextComponent target, String typedText) {
        BaseDocument doc = Utilities.getDocument(target);
        int dotPos = target.getCaret().getDot();
        try {
            int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
            return dotPos != fnw+typedText.length();
        } catch (BadLocationException e) {
            return false;
        }
    }

    /** Create the indentation writer.
    */
    public @Override Writer createWriter(Document doc, int offset, Writer writer) {
        return new FormatWriter(this, doc, offset, writer, false);
    }

    /** Indents the current line. Should not affect any other
    * lines.
    * @param doc the document to work on
    * @param offset the offset of a character on the line
    * @return new offset of the original character
    */
    public @Override int indentLine(Document doc, int offset) {
        if (doc instanceof BaseDocument) {
            try {
                BaseDocument bdoc = (BaseDocument)doc;
                int lineStart = Utilities.getRowStart(bdoc, offset);
                int nextLineStart = Utilities.getRowStart(bdoc, offset, 1);
                if (nextLineStart < 0) { // end of doc
                    nextLineStart = bdoc.getLength();
                }
                reformat(bdoc, lineStart, nextLineStart, false);
                return Utilities.getRowEnd(bdoc, lineStart);
            } catch (GuardedException e) {
                java.awt.Toolkit.getDefaultToolkit().beep();

            } catch (BadLocationException e) {
                Utilities.annotateLoggable(e);
            } catch (IOException e) {
                Utilities.annotateLoggable(e);
            }

            return offset;

        }

        return super.indentLine(doc, offset);
    }
    
    /** Returns offset of EOL for the white line */
    protected int getEOLOffset(BaseDocument bdoc, int offset) throws BadLocationException{
        return Utilities.getRowEnd(bdoc, offset);
    }

    /** Inserts new line at given position and indents the new line with
    * spaces.
    *
    * @param doc the document to work on
    * @param offset the offset of a character on the line
    * @return new offset to place cursor to
    */
    public @Override int indentNewLine (final Document doc, final int offset) {
        final int[] result = new int [] {offset};
        if (doc instanceof BaseDocument) {
            final BaseDocument bdoc = (BaseDocument)doc;

            bdoc.runAtomicAsUser (new Runnable () {
                public void run () {
                    boolean newLineInserted = false;
                    try {
                        bdoc.insertString(result [0], "\n", null); // NOI18N
                        result [0]++;
                        newLineInserted = true;

                        int eolOffset = Utilities.getRowEnd(bdoc, result [0]);

                        // Try to change the indent of the new line
                        // It may fail when inserting '\n' before the guarded block
                        Writer w = reformat(bdoc, result [0], eolOffset, true);

                        // Find the caret position
                        eolOffset = Utilities.getRowFirstNonWhite(bdoc, result [0]);
                        if (eolOffset < 0) { // white line
                            eolOffset = getEOLOffset(bdoc, result [0]);
                        }

                        result [0] = eolOffset;

                        // Resulting offset (caret position) can be shifted
                        if (w instanceof FormatWriter) {
                            result [0] += ((FormatWriter)w).getIndentShift();
                        }

                    } catch (GuardedException e) {
                        // Possibly couldn't insert additional indentation
                        // at the begining of the guarded block
                        // but the initial '\n' could be fine
                        if (!newLineInserted) {
                            java.awt.Toolkit.getDefaultToolkit().beep();
                        }

                    } catch (BadLocationException e) {
                        Utilities.annotateLoggable(e);
                    } catch (IOException e) {
                        Utilities.annotateLoggable(e);
                    }
                }
            });
        } else { // not BaseDocument
            try {
                doc.insertString (result [0], "\n", null); // NOI18N
                result [0]++;
            } catch (BadLocationException ex) {
            }
        }

        return result [0];
    }

    /** Whether the formatter accepts the given syntax
     * that will be used for parsing the text passed to
     * the FormatWriter.
     * @param syntax syntax to be tested.
     * @return true whether this formatter is able to process
     *  the tokens created by the syntax or false otherwise.
     */
    protected boolean acceptSyntax(Syntax syntax) {
        return true;
    }

    /** Simple formatter */
    public static class Simple extends ExtFormatter {

        public Simple(Class kitClass) {
            super(kitClass);
        }

        public @Override boolean isSimple() {
            return true;
        }
        
        /** Returns offset of EOL for the white line */
        protected @Override int getEOLOffset(BaseDocument bdoc, int offset) throws BadLocationException{
            return offset;
        }
        
    }

    /* package */ static void pushFormattingContextDocument(Document doc) {
        try {
            Method m = Formatter.class.getDeclaredMethod("pushFormattingContextDocument", Document.class); //NOI18N
            m.setAccessible(true);
            m.invoke(null, doc);
        } catch (Exception e) {
            // ignore
        }
    }

    /* package */ static void popFormattingContextDocument(Document doc) {
        try {
            Method m = Formatter.class.getDeclaredMethod("popFormattingContextDocument", Document.class); //NOI18N
            m.setAccessible(true);
            m.invoke(null, doc);
        } catch (Exception e) {
            // ignore
        }
    }

}

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

package org.netbeans.modules.editor.search;

import java.awt.Insets;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.editor.NavigationHistory;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.modules.editor.lib2.ComponentUtils;
import org.netbeans.modules.editor.lib2.DocUtils;
import org.netbeans.modules.editor.lib2.highlighting.BlockHighlighting;
import org.netbeans.modules.editor.lib2.highlighting.Factory;
import org.netbeans.modules.editor.search.DocumentFinder.FindReplaceResult;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
* Find management
*
* @author Miloslav Metelka
* @version 1.00
*/

public final class EditorFindSupport {

    private static final Logger LOG = Logger.getLogger(EditorFindSupport.class.getName());
    
    /* Find properties.
    * They are read by FindSupport when its instance is being initialized.
    * FIND_WHAT: java.lang.String - search expression
    * FIND_REPLACE_BY: java.lang.String - replace string
    * FIND_HIGHLIGHT_SEARCH: java.lang.Boolean - highlight matching strings in text
    * FIND_INC_SEARCH: java.lang.Boolean - show matching strings immediately
    * FIND_BACKWARD_SEARCH: java.lang.Boolean - search in backward direction
    * FIND_WRAP_SEARCH: java.lang.Boolean - if end of doc reached, start from begin
    * FIND_MATCH_CASE: java.lang.Boolean - match case of letters
    * FIND_SMART_CASE: java.lang.Boolean - case insensitive search if FIND_MATCH_CASE
    *   is false and all letters of FIND_WHAT are small, case sensitive otherwise
    * FIND_WHOLE_WORDS: java.lang.Boolean - match only whole words
    * FIND_REG_EXP: java.lang.Boolean - use regular expressions in search expr
    * FIND_HISTORY: java.util.List - History of search expressions
    * FIND_HISTORY_SIZE: java.lang.Integer - Maximum size of the history
    * FIND_BLOCK_SEARCH: java.lang.Boolean - search in block
    * FIND_BLOCK_SEARCH_START: javax.swing.text.Position - start position of the block in block search
    * FIND_BLOCK_SEARCH_END: javax.swing.text.Position - end position of the block in block search
    * 
    */
    public static final String FIND_WHAT = "find-what"; // NOI18N
    public static final String FIND_REPLACE_WITH = "find-replace-with"; // NOI18N
    public static final String FIND_HIGHLIGHT_SEARCH = "find-highlight-search"; // NOI18N
    public static final String FIND_INC_SEARCH = "find-inc-search"; // NOI18N
    public static final String FIND_INC_SEARCH_DELAY = "find-inc-search-delay"; // NOI18N
    public static final String FIND_BACKWARD_SEARCH = "find-backward-search"; // NOI18N
    public static final String FIND_WRAP_SEARCH = "find-wrap-search"; // NOI18N
    public static final String FIND_MATCH_CASE = "find-match-case"; // NOI18N
    public static final String FIND_SMART_CASE = "find-smart-case"; // NOI18N
    public static final String FIND_PRESERVE_CASE = "find-preserve-case"; // NOI18N
    public static final String FIND_WHOLE_WORDS = "find-whole-words"; // NOI18N
    public static final String FIND_REG_EXP = "find-reg-exp"; // NOI18N
    public static final String FIND_HISTORY = "find-history"; // NOI18N
    public static final String FIND_HISTORY_SIZE = "find-history-size"; // NOI18N
    public static final String FIND_BLOCK_SEARCH = "find-block-search"; //NOI18N
    public static final String FIND_BLOCK_SEARCH_START = "find-block-search-start"; //NOI18N
    public static final String FIND_BLOCK_SEARCH_END = "find-block-search-end"; //NOI18N
    public static final String ADD_MULTICARET = "add-multi-caret"; //NOI18N

    private static final String FOUND_LOCALE = "find-found"; // NOI18N
    private static final String NOT_FOUND_LOCALE = "find-not-found"; // NOI18N
    private static final String WRAP_START_LOCALE = "find-wrap-start"; // NOI18N
    private static final String WRAP_END_LOCALE = "find-wrap-end"; // NOI18N
    private static final String WRAP_BLOCK_START_LOCALE = "find-block-wrap-start"; // NOI18N
    private static final String WRAP_BLOCK_END_LOCALE = "find-block-wrap-end"; // NOI18N
    private static final String ITEMS_REPLACED_LOCALE = "find-items-replaced"; // NOI18N
    /** It's public only to keep backwards compatibility of the FindSupport class. */
    public static final String REVERT_MAP = "revert-map"; // NOI18N

    /** It's public only to keep backwards compatibility of the FindSupport class. */
    public static final String FIND_HISTORY_PROP = "find-history-prop"; //NOI18N
    public static final String REPLACE_HISTORY_PROP = "replace-history-prop"; //NOI18N
    /** It's public only to keep backwards compatibility of the FindSupport class. */
    public static final String FIND_HISTORY_CHANGED_PROP = "find-history-changed-prop"; //NOI18N
    public static final String REPLACE_HISTORY_CHANGED_PROP = "replace-history-changed-prop"; //NOI18N
    
    /**
     * Default message 'importance' for messages from find and replace actions.
     * <br/>
     * Corresponds to StatusDisplayer.IMPORTANCE_FIND_OR_REPLACE.
     */
    private static final int IMPORTANCE_FIND_OR_REPLACE = 800;

    /** Shared instance of FindSupport class */
    private static EditorFindSupport findSupport;

    /** Find properties */
    private Map<String, Object> findProps;
    private WeakReference<JTextComponent> focusedTextComponent;
    private final RequestProcessor executor = new RequestProcessor(EditorFindSupport.class.getName(), 1);

    private final WeakHashMap<JTextComponent, Map<String, WeakReference<BlockHighlighting>>> comp2layer =
        new WeakHashMap<>();
    
    /** Support for firing change events */
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    private SPW lastSelected;
    private List<SPW> historyList = new ArrayList<>();
    private List<RP> replaceList = new ArrayList<>();

    private String cachekey = "";
    private int[] cacheContent = new int[0];
    private static final int TIME_LIMIT = 2;
    
    private EditorFindSupport() {
    }

    /** Get shared instance of find support */
    public static synchronized EditorFindSupport getInstance() {
        if (findSupport == null) {
            findSupport = new EditorFindSupport();
        }
        return findSupport;
    }

    public Map<String, Object> createDefaultFindProperties() {
        HashMap<String, Object> props = new HashMap<>();
        
        props.put(FIND_WHAT, null);
        props.put(FIND_REPLACE_WITH, null);
        props.put(FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        props.put(FIND_INC_SEARCH, Boolean.TRUE);
        props.put(FIND_BACKWARD_SEARCH, Boolean.FALSE);
        props.put(FIND_WRAP_SEARCH, Boolean.TRUE);
        props.put(FIND_MATCH_CASE, Boolean.FALSE);
        props.put(FIND_SMART_CASE, Boolean.FALSE);
        props.put(FIND_WHOLE_WORDS, Boolean.FALSE);
        props.put(FIND_REG_EXP, Boolean.FALSE);
        props.put(FIND_HISTORY, Integer.valueOf(30));
        props.put(FIND_PRESERVE_CASE, Boolean.FALSE);
        props.put(ADD_MULTICARET, Boolean.FALSE);

        return props;
    }

    private int getBlockEndOffset(){
        Position pos = (Position) getFindProperties().get(FIND_BLOCK_SEARCH_END);
        return (pos != null) ? pos.getOffset() : -1;
    }
    
    public Map<String, Object> getFindProperties() {
        if (findProps == null) {
            findProps = createDefaultFindProperties();
        }
        return findProps;
    }

    /** Get find property with specified name */
    public Object getFindProperty(String name) {
        return getFindProperties().get(name);
    }

    private Map<String, Object> getValidFindProperties(Map<String, Object> props) {
        return (props != null) ? props : getFindProperties();
    }

    /**
     * <p><b>IMPORTANT:</b> This method is public only for keeping backwards
     * compatibility of the {@link org.netbeans.editor.FindSupport} class.
     */
    public synchronized int[] getBlocks(final int[] blocks, final Document doc,
            int startOffset, int endOffset) throws BadLocationException {
        final Map<String, Object> props = getValidFindProperties(null);

        String newCacheKey = calculateCacheKey(doc, startOffset, endOffset, props);
        if (cachekey.equals(newCacheKey)) {
            return Arrays.copyOf(cacheContent, cacheContent.length);
        }

        boolean blockSearch = Boolean.TRUE.equals(props.get(FIND_BLOCK_SEARCH));
        Position blockSearchStartPos = (Position) props.get(FIND_BLOCK_SEARCH_START);
        Position blockSearchEndPos = (Position) props.get(FIND_BLOCK_SEARCH_END);

        if (blockSearch && blockSearchStartPos != null && blockSearchEndPos != null){
            if (endOffset >= blockSearchStartPos.getOffset() &&
                    startOffset <= blockSearchEndPos.getOffset())
            {
                startOffset = Math.max(blockSearchStartPos.getOffset(), startOffset);
                endOffset = Math.min(blockSearchEndPos.getOffset(), endOffset);
            } else {
                return blocks;
            }
        }

        final int so = startOffset;
        final int eo = endOffset;
        currentResult = null;
        try {
            executor.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        currentResult = DocumentFinder.findBlocks(doc, so, eo, props, blocks);
                        cacheContent = currentResult.getFoundPositions();
                    } catch (BadLocationException ble) {
                        cacheContent = Arrays.copyOf(blocks, blocks.length);
                        LOG.log(Level.INFO, ble.getMessage(), ble);
                    }
                    
                }
            }).get(TIME_LIMIT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            cacheContent = Arrays.copyOf(blocks, blocks.length);
            org.netbeans.editor.Utilities.setStatusBoldText(getFocusedTextComponent(), NbBundle.getMessage(EditorFindSupport.class, "slow-search"));
            LOG.log(Level.INFO, ex.getMessage(), ex);
        }
        if (currentResult != null && currentResult.hasErrorMsg()) {
            org.netbeans.editor.Utilities.setStatusBoldText(getFocusedTextComponent(), currentResult.getErrorMsg());
        }
        cachekey = newCacheKey;
        return Arrays.copyOf(cacheContent, cacheContent.length);
    }

    /** Set find property with specified name and fire change.
    */
    public void putFindProperty(String name, Object newValue) {
        Object oldValue = getFindProperty(name);
        if ((oldValue == null && newValue == null)
                || (oldValue != null && oldValue.equals(newValue))
           ) {
            return;
        }
        if (newValue != null) {
            getFindProperties().put(name, newValue);
        } else {
            getFindProperties().remove(name);
        }
        firePropertyChange(name, oldValue, newValue);
    }

    /**
     * Add/replace properties from some other map
     * to current find properties. If the added properties
     * are different than the original ones,
     * the property change is fired.
     */
    public void putFindProperties(Map<String, Object> propsToAdd) {
        if (getFindProperties() != propsToAdd) {
            getFindProperties().putAll(propsToAdd);
        }
        //highlight will not be updated on empty properties
        if (propsToAdd.get(FIND_WHAT) != null) {
            firePropertyChange(null, null, null);
        }
    }
    
    public void setFocusedTextComponent(JTextComponent component) { 
        focusedTextComponent = new WeakReference<>(component);
        firePropertyChange(null, null, null);
    }
    
    public JTextComponent getFocusedTextComponent() {
        JTextComponent jc = focusedTextComponent != null ? focusedTextComponent.get() : null;
        return (jc != null) ? jc : EditorRegistry.lastFocusedComponent();
    }
    
    public void setBlockSearchHighlight(int startSelection, int endSelection){
        JTextComponent comp = getFocusedTextComponent();
        BlockHighlighting layer = comp == null ? null : findLayer(comp, Factory.BLOCK_SEARCH_LAYER);

        if (layer != null) {
            
            if (startSelection >= 0 && endSelection >= 0 && startSelection < endSelection ) {
                layer.highlightBlock(startSelection, endSelection, FontColorNames.BLOCK_SEARCH_COLORING, true, true);
            } else {
                layer.highlightBlock(-1, -1, FontColorNames.BLOCK_SEARCH_COLORING, true, true);
            }            
        }
    }
    
    public boolean incSearch(Map<String, Object> props, int caretPos) {
        props = getValidFindProperties(props);
        
        Boolean b = (Boolean)props.get(FIND_INC_SEARCH);
        if (b != null && b.booleanValue()) { // inc search enabled
            JTextComponent comp = getFocusedTextComponent();
            
            if (comp != null) {
                b = (Boolean)props.get(FIND_BACKWARD_SEARCH);
                boolean back = (b != null && b.booleanValue());
                b = (Boolean)props.get(FIND_BLOCK_SEARCH);
                boolean blockSearch = (b != null && b.booleanValue());
                Position blockStartPos = (Position) props.get(FIND_BLOCK_SEARCH_START);
                int blockSearchStartOffset = (blockStartPos != null) ? blockStartPos.getOffset() : -1;
                
                Position endPos = (Position) props.get(FIND_BLOCK_SEARCH_END);
                int blockSearchEndOffset = (endPos != null) ? endPos.getOffset() : -1;
                int pos;
                int len = 0;
                try {
                    int start = (blockSearch && blockSearchStartOffset > -1) ? blockSearchStartOffset : 0;
                    int end = (blockSearch && blockSearchEndOffset > 0) ? blockSearchEndOffset : -1;
                    if (start > 0 && end == -1) {
                        return false;
                    }
                    int findRet[] = findInBlock(comp, caretPos, 
                        start, 
                        end, 
                        props, false);
                            
                    if (findRet == null) {
                        incSearchReset();
                        return false;
                    }
                    pos = findRet[0];
                    len = findRet.length > 1 ? findRet[1] - pos : 0;
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, e.getMessage(), e);
                    return false;
                }
                
                if (pos >= 0) {
                    // Find the layer
                    BlockHighlighting layer = findLayer(comp, Factory.INC_SEARCH_LAYER);

                    if (len > 0) {
                        if (comp.getSelectionEnd() > comp.getSelectionStart()){
                            comp.select(caretPos, caretPos);
                        }
                        
                        if (layer != null) {
                            layer.highlightBlock(
                                pos,
                                pos + len,
                                blockSearch ? FontColorNames.INC_SEARCH_COLORING : FontColorNames.SELECTION_COLORING,
                                false,
                                false
                            );
                        }
                        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
                        if (prefs.get(SimpleValueNames.EDITOR_SEARCH_TYPE, "default").equals("closing")) { // NOI18N
                            ensureVisible(comp, pos, pos);
                        } else {
                            selectText(comp, pos, pos + len, back);
                        }
                        return true;
                    }
                }
               
            }
        } else { // inc search not enabled
            incSearchReset();
        }
        return false;
    }

    public void incSearchReset() {
        // Find the layer
        JTextComponent comp = getFocusedTextComponent();
        BlockHighlighting layer = comp == null ? null : findLayer(comp, Factory.INC_SEARCH_LAYER);
        
        if (layer != null) {
            layer.highlightBlock(-1, -1, null, false, false);
        }
    }
    
    private boolean isBackSearch(Map<String, Object> props, boolean oppositeDir) {
        Boolean b = (Boolean)props.get(FIND_BACKWARD_SEARCH);
        boolean back = (b != null && b.booleanValue());
        if (oppositeDir) {
            back = !back;
        }
        return back;
    }
    
    private void addCaretSelectText(JTextComponent c, int start, int end, boolean back) {
        Caret eCaret = c.getCaret();
        ensureVisible(c, start, end);
        if (eCaret instanceof EditorCaret) {
            EditorCaret caret = (EditorCaret) eCaret;
            try {
                caret.addCaret(c.getDocument().createPosition(end), Position.Bias.Forward,
                    c.getDocument().createPosition(start), Position.Bias.Forward);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void selectText(JTextComponent c, int start, int end, boolean back){
        Caret caret = c.getCaret();
        ensureVisible(c, start, end);
        if (back) {
            caret.setDot(end);
            caret.moveDot(start);
        } else { // forward direction
            caret.setDot(start);
            caret.moveDot(end);
        }
    }
    
    private void ensureVisible(JTextComponent c, int startOffset, int endOffset) {
        // TODO: read insets from settings
        ensureVisible(c, startOffset, endOffset, new Insets(10, 10, 10, 10));
    }
    
    /**
     * Ensure that the given region will be visible in the view
     * with the appropriate find insets.
     */
    private void ensureVisible(JTextComponent c, int startOffset, int endOffset, Insets extraInsets) {
        try {
            Rectangle startBounds = c.modelToView(startOffset);
            Rectangle endBounds = c.modelToView(endOffset);
            if (startBounds != null && endBounds != null) {
                startBounds.add(endBounds);
                if (extraInsets != null) {
                    Rectangle visibleBounds = c.getVisibleRect();
                    int extraTop = (extraInsets.top < 0)
                        ? -extraInsets.top * visibleBounds.height / 100 // percentage
                        : extraInsets.top * endBounds.height; // line count
                    startBounds.y -= extraTop;
                    startBounds.height += extraTop;
                    startBounds.height += (extraInsets.bottom < 0)
                        ? -extraInsets.bottom * visibleBounds.height / 100 // percentage
                        : extraInsets.bottom * endBounds.height; // line count
                    int extraLeft = (extraInsets.left < 0)
                        ? -extraInsets.left * visibleBounds.width / 100 // percentage
                        : extraInsets.left * endBounds.width; // char count
                    startBounds.x -= extraLeft;
                    startBounds.width += extraLeft;
                    startBounds.width += (extraInsets.right < 0)
                        ? -extraInsets.right * visibleBounds.width / 100 // percentage
                        : extraInsets.right * endBounds.width; // char count
                }
                c.scrollRectToVisible(startBounds);
            }
        } catch (BadLocationException e) {
            // do not scroll
        }
    }
    
    private int[] findMatches = null;
    private synchronized boolean findMatches(final String text, final Map<String, Object> props) {
        if(text == null) {
            return false;
        }
        try {
            final PlainDocument plainDocument = new PlainDocument();
            plainDocument.insertString(0, text, null);
            findMatches = null;
            try {
                executor.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            findMatches = DocumentFinder.find(plainDocument, 0, text.length(), props, false);
                        } catch (BadLocationException ble) {
                            LOG.log(Level.INFO, ble.getMessage(), ble);
                        }
                    }
                }).get(TIME_LIMIT, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                org.netbeans.editor.Utilities.setStatusBoldText(getFocusedTextComponent(), NbBundle.getMessage(
                        EditorFindSupport.class, "slow-search"));
                LOG.log(Level.INFO, ex.getMessage(), ex);
            }
            return findMatches != null && findMatches[0] != -1;
        } catch (BadLocationException ex) {
            return false;
        }
    }
    
    FindReplaceResult findReplaceImpl(String replaceExp, 
            Map<String, Object> props, boolean oppositeDir, JTextComponent c) {
        incSearchReset();
        props = getValidFindProperties(props);
        boolean back = isBackSearch(props, oppositeDir);
        if (!(props.get(FIND_WHAT) instanceof String)) {
            return null;
        }
        String findWhat = (String) props.get(FIND_WHAT);
        if (c != null) {
            ComponentUtils.clearStatusText(c);
            Caret caret = c.getCaret();
            int dotPos = caret.getDot();
            if (findMatches(c.getSelectedText(), props)) {
                Object dp = props.get(FIND_BACKWARD_SEARCH);
                boolean direction = (dp != null) ? ((Boolean)dp).booleanValue() : false;

                if (dotPos == (oppositeDir ^ direction ? c.getSelectionEnd() : c.getSelectionStart())) {
                    dotPos += (oppositeDir ^ direction ? -1 : 1);
                }
                
                if (replaceExp != null) {
                    if (oppositeDir ^ direction) {
                        dotPos = c.getSelectionEnd();
                    } else {
                        dotPos = c.getSelectionStart();
                    }
                }
            }
            
            Boolean b = (Boolean)props.get(FIND_BLOCK_SEARCH);
            boolean blockSearch = (b != null && b.booleanValue());
            Position blockStartPos = (Position) props.get(FIND_BLOCK_SEARCH_START);
            int blockSearchStart = (blockStartPos != null) ? blockStartPos.getOffset() : -1;
            int blockSearchEnd = getBlockEndOffset();

            boolean backSearch = Boolean.TRUE.equals(props.get(FIND_BACKWARD_SEARCH));
            if (backSearch) {
                blockSearchEnd = dotPos;
                dotPos = 0;
            }
            try {
                FindReplaceResult result = findReplaceInBlock(replaceExp, c, dotPos, 
                        (blockSearch && blockSearchStart > -1) ? blockSearchStart : 0, 
                        (blockSearch && blockSearchEnd > 0) ? blockSearchEnd : backSearch ? blockSearchEnd : -1, 
                        props, oppositeDir);
                
                if (result != null && result.hasErrorMsg()) {
                    ComponentUtils.setStatusText(c, result.getErrorMsg());
                    c.getCaret().setDot(c.getCaret().getDot());
                    return null;
                }
                int[] blk = null; 
                if (result != null){
                    blk = result.getFoundPositions();
                }
                if (blk != null) {
                    if (Boolean.TRUE.equals(props.get(EditorFindSupport.ADD_MULTICARET))) {
                        addCaretSelectText(c, blk[0], blk[1], back);
                    } else {
                        selectText(c, blk[0], blk[1], back);
                    }
                    String msg = NbBundle.getMessage(EditorFindSupport.class, FOUND_LOCALE, findWhat, DocUtils.debugPosition(c.getDocument(), Integer.valueOf(blk[0])));
//                    String msg = exp + NbBundle.getMessage(EditorFindSupport.class, FOUND_LOCALE)
//                                 + ' ' + DocUtils.debugPosition(c.getDocument(), blk[0]);
                    if (blk[2] == 1) { // wrap was done
                        msg += "; "; // NOI18N
                        if (blockSearch && blockSearchEnd>0 && blockSearchStart >-1){
                            msg += back ? NbBundle.getMessage(EditorFindSupport.class, WRAP_BLOCK_END_LOCALE)
                                   : NbBundle.getMessage(EditorFindSupport.class, WRAP_BLOCK_START_LOCALE);
                        }else{
                            msg += back ? NbBundle.getMessage(EditorFindSupport.class, WRAP_END_LOCALE)
                                   : NbBundle.getMessage(EditorFindSupport.class, WRAP_START_LOCALE);
                        }
                        ComponentUtils.setStatusText(c, msg, IMPORTANCE_FIND_OR_REPLACE);
                        c.getToolkit().beep();
                    } else {
                        ComponentUtils.setStatusText(c, msg, IMPORTANCE_FIND_OR_REPLACE);
                    }
                    return result;
                } else { // not found
                    ComponentUtils.setStatusText(c, NbBundle.getMessage(
                                                    EditorFindSupport.class, NOT_FOUND_LOCALE, findWhat), IMPORTANCE_FIND_OR_REPLACE);
                    // issue 14189 - selection was not removed
                    c.getCaret().setDot(c.getCaret().getDot());
                    }
            } catch (BadLocationException e) {
                LOG.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return null;
    }
    
    /** Find the text from the caret position.
    * @param localProps search properties
    * @param oppositeDir whether search in opposite direction
    */
    public boolean find(Map<String, Object> props, boolean oppositeDir) {
        FindReplaceResult result = findReplaceImpl(null, props, oppositeDir, getFocusedTextComponent());
        return (result != null);
    }

    private FindReplaceResult currentResult = null;
    private synchronized FindReplaceResult findReplaceInBlock(final String replaceExp, JTextComponent c, int startPos, int blockStartPos,
                             int blockEndPos, Map<String, Object> props, final boolean oppositeDir) throws BadLocationException {
        if (c != null) {
            final Map<String, Object> validProps = getValidFindProperties(props);
            final Document doc = c.getDocument();
            int pos = -1;
            boolean wrapDone = false;
            String replaced = null;

            boolean back = isBackSearch(validProps, oppositeDir);
            Boolean b = (Boolean)validProps.get(FIND_WRAP_SEARCH);
            boolean wrap = (b != null && b.booleanValue());
            int docLen = doc.getLength();
            if (blockEndPos == -1) {
                blockEndPos = docLen;
            }
            if (startPos == -1) {
                startPos = docLen;
            }

            int retFind[];
            while (true) {
                //pos = doc.find(sf, startPos, back ? blockStartPos : blockEndPos);
                final int off1 = startPos;
                final int off2 = oppositeDir ? blockStartPos : blockEndPos;
                currentResult = null;
                try {
                    executor.submit(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                currentResult = DocumentFinder.findReplaceResult(replaceExp, doc, off1, off2,
                                        validProps, oppositeDir);
                            } catch (BadLocationException ble) {
                                LOG.log(Level.WARNING, ble.getMessage(), ble);
                            }

                        }
                    }).get(TIME_LIMIT, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    org.netbeans.editor.Utilities.setStatusBoldText(getFocusedTextComponent(), NbBundle.getMessage(
                            EditorFindSupport.class, "slow-search"));
                    LOG.log(Level.INFO, ex.getMessage(), ex);
                }

                if (currentResult == null) {     
                    return null;
                }
                
                if (currentResult.hasErrorMsg()) {
                    return currentResult;
                }
                retFind = currentResult.getFoundPositions();
                replaced = currentResult.getReplacedString();
                if (retFind == null){
                    break;
                }
                pos = retFind[0];
                
                if (pos != -1) {
                    break;
                }

                if (wrap) {
                    if (back) {
                        //Bug #20552 the wrap search check whole document
                        //instead of just the remaining not-searched part to be
                        //able to find expressions with the cursor in it

                        //blockStartPos = startPos;
                        startPos = blockEndPos;
                        blockEndPos = docLen;
                    } else {
                        //blockEndPos = startPos;
                        startPos = blockStartPos;
                    }
                    wrapDone = true;
                    wrap = false; // only one loop
                } else { // no wrap set
                    break;
                }

            }
            if (pos != -1) {
                int[] ret = new int[3];
                ret[0] = pos;
                ret[1] = retFind[1];
                ret[2] = wrapDone ? 1 : 0;
                
                return new FindReplaceResult(ret, replaced);
            }
        }
        return null;
    }
    
    /** Find the searched expression
    * @param startPos position from which to search. It must be inside the block.
    * @param blockStartPos starting position of the block. It must
    *   be valid position greater or equal than zero. It must be lower than
    *   or equal to blockEndPos (except blockEndPos=-1).
    * @param blockEndPos ending position of the block. It can be -1 for the end
    *   of document. It must be greater or equal than blockStartPos (except blockEndPos=-1).
    * @param localProps search properties
    * @param oppositeDir whether search in opposite direction
    * @param displayWrap whether display messages about the wrapping
    * @return either null when nothing was found or integer array with three members
    *    ret[0] - starting position of the found string
    *    ret[1] - ending position of the found string
    *    ret[2] - 1 or 0 when wrap was or wasn't performed in order to find the string 
    */
    public int[] findInBlock(JTextComponent c, int startPos, int blockStartPos,
                             int blockEndPos, Map<String, Object> props, boolean oppositeDir) throws BadLocationException {
        FindReplaceResult result = findReplaceInBlock(null, c, startPos, blockStartPos,
                             blockEndPos, props, oppositeDir);
        return result == null ? null : result.getFoundPositions();
    }

    public boolean replace(Map<String, Object> props, boolean oppositeDir)
    throws BadLocationException {
        incSearchReset();
        return replaceImpl(props, oppositeDir, getFocusedTextComponent());
    }

    boolean replaceImpl(Map<String, Object> props, boolean oppositeDir, JTextComponent c) throws BadLocationException {
        props = getValidFindProperties(props);
        boolean back = Boolean.TRUE.equals(props.get(FIND_BACKWARD_SEARCH));
        if (oppositeDir) {
            back = !back;
        }
        boolean blockSearch = Boolean.TRUE.equals(props.get(FIND_BLOCK_SEARCH));
        Position blockSearchStartPos = (Position) props.get(FIND_BLOCK_SEARCH_START);
        int blockSearchStartOffset = (blockSearchStartPos != null) ? blockSearchStartPos.getOffset() : -1;

        if (c != null) {
            String s = (String)props.get(FIND_REPLACE_WITH);
            Caret caret = c.getCaret();
            if (caret.isSelectionVisible() && caret.getDot() != caret.getMark()){
                Object dp = props.get(FIND_BACKWARD_SEARCH);
                boolean direction = (dp != null) ? ((Boolean)dp).booleanValue() : false;
                int dotPos = (oppositeDir ^ direction ? c.getSelectionEnd() : c.getSelectionStart());
                c.setCaretPosition(dotPos);
            }
            
            FindReplaceResult result = findReplaceImpl(s, props, oppositeDir, c);
            if (result!=null){
                s  = result.getReplacedString();
            } else {
                return false;
            }

            Document doc = c.getDocument();
            int startOffset = c.getSelectionStart();
            int len = c.getSelectionEnd() - startOffset;
            DocUtils.atomicLock(doc);
            try {
                if (len > 0) {
                    doc.remove(startOffset, len);
                }
                if (s != null && s.length() > 0) {
                    try {
                        NavigationHistory.getEdits().markWaypoint(c, startOffset, false, true);
                    } catch (BadLocationException e) {
                        LOG.log(Level.WARNING, "Can't add position to the history of edits.", e); //NOI18N
                    }
                    doc.insertString(startOffset, s, null);
                    if (startOffset == blockSearchStartOffset) { // Replaced at begining of block
                        blockSearchStartPos = doc.createPosition(startOffset);
                        props.put(EditorFindSupport.FIND_BLOCK_SEARCH_START, blockSearchStartPos);
                    }
                }
            } finally {
                DocUtils.atomicUnlock(doc);
                if (blockSearch){
                    setBlockSearchHighlight(blockSearchStartOffset, getBlockEndOffset());
                }
            }
            
            // adjust caret pos after replace operation
            int adjustedCaretPos = (back || s == null) ? startOffset : startOffset + s.length();
            caret.setDot(adjustedCaretPos);
            
        }
        
        return true;
    }

    public void replaceAll(Map<String, Object> props) {
        incSearchReset();
        replaceAllImpl(props, getFocusedTextComponent());
    }

    /**
     * This method is called from unit test. It is implementation of the above method.
     * @param props
     * @param c
     */
    void replaceAllImpl(Map<String, Object> props, JTextComponent c) {
        props = getValidFindProperties(props);
        Map<String,Object> localProps = new HashMap<>(props);
        String replaceWithOriginal = (String)localProps.get(FIND_REPLACE_WITH);

        Object findWhat = localProps.get(FIND_WHAT);
        if (findWhat == null) { // nothing to search for
            return;
        }
        if (findWhat.equals(replaceWithOriginal)) {
            return;
        }

        Document doc = c.getDocument();
        int maxCnt = doc.getLength();
        int replacedCnt = 0;
        int totalCnt = 0;

        boolean blockSearch = Boolean.TRUE.equals(localProps.get(FIND_BLOCK_SEARCH));
        boolean wrapSearch = Boolean.TRUE.equals(localProps.get(FIND_WRAP_SEARCH));
        boolean backSearch = Boolean.TRUE.equals(localProps.get(FIND_BACKWARD_SEARCH));

        if (wrapSearch){
            localProps.put(FIND_WRAP_SEARCH, Boolean.FALSE);
            localProps.put(FIND_BACKWARD_SEARCH, Boolean.FALSE);
            backSearch = false;
            firePropertyChange(null, null, null);
        }

        Position blockSearchStartPos = (Position) localProps.get(FIND_BLOCK_SEARCH_START);
        int blockSearchStartOffset = (blockSearchStartPos != null) ? blockSearchStartPos.getOffset() : -1;
        int blockSearchEndOffset = getBlockEndOffset();

        if (c != null) {
            DocUtils.atomicLock(doc);
            try {
                int startPosWholeSearch = 0;
                int endPosWholeSearch = -1;
                int caretPos = c.getCaret().getDot();

                if (!wrapSearch){
                    if (backSearch){
                        startPosWholeSearch = 0;
                        endPosWholeSearch = caretPos;
                    }else{
                        startPosWholeSearch = caretPos;
                        endPosWholeSearch = -1;
                    }
                }

                int actualPos = wrapSearch ? 0 : c.getCaret().getDot();

                int pos = (blockSearch && blockSearchStartOffset > -1) ?  blockSearchStartOffset : (backSearch? 0 : actualPos); // actual position
                while (true) {
                    FindReplaceResult result = findReplaceInBlock(replaceWithOriginal, c, pos,
                            (blockSearch && blockSearchStartOffset > -1) ? blockSearchStartOffset : startPosWholeSearch,
                            (blockSearch && blockSearchEndOffset > 0) ? blockSearchEndOffset : endPosWholeSearch,
                            localProps, false);
                    if (result == null){
                        break;
                    }
                    int[] blk = result.getFoundPositions();
                    String replaceWith = result.getReplacedString();
                    if (blk == null) {
                        break;
                    }
                    totalCnt++;
                    int len = blk[1] - blk[0];
                    boolean skip = false; // cannot remove (because of guarded block)?
                    try {
                        doc.remove(blk[0], len);
                    } catch (BadLocationException e) {
                        // replace in guarded block
                        if (ComponentUtils.isGuardedException(e)) {
                            skip = true;
                        } else {
                            throw e;
                        }
                    }
                    if (skip) {
                        pos = backSearch ? blk[0] : blk[0] + len;

                    } else { // can and will insert the new string
                        if (replaceWith != null && replaceWith.length() > 0) {
                            int offset = blk[0];
                            try {
                                NavigationHistory.getEdits().markWaypoint(c, offset, false, true);
                            } catch (BadLocationException e) {
                                LOG.log(Level.WARNING, "Can't add position to the history of edits.", e); //NOI18N
                            }
                            doc.insertString(offset, replaceWith, null);
                            if (offset == blockSearchStartOffset) { // Replaced at begining of block
                                blockSearchStartPos = doc.createPosition(offset);
                                // Update position in original properties
                                props.put(EditorFindSupport.FIND_BLOCK_SEARCH_START, blockSearchStartPos);
                            }
                            blockSearchEndOffset = getBlockEndOffset();
                        }
                        pos = backSearch ? blk[0] : blk[0] + ((replaceWith != null) ? replaceWith.length() : 0);
                        if (!wrapSearch && backSearch) {
                            endPosWholeSearch = endPosWholeSearch < blk[0] ? endPosWholeSearch : blk[0];
                            blockSearchEndOffset = blockSearchEndOffset < blk[0] ? blockSearchEndOffset : blk[0];
                            pos = (blockSearch && blockSearchStartOffset > -1) ?  blockSearchStartOffset : 0;
                        }
                        replacedCnt++;
                    }
                    // The following is lame attempt to break the loop: if
                    // someone knows a better way please remove this but check
                    // that all tests in EditorFindSupportTest pass!
                    if (replacedCnt > maxCnt) {
                        break;
                    }
                }

                // Display message about replacement
                if (totalCnt == 0){
                    String exp = "'" + findWhat + "' "; //NOI18N
                    ComponentUtils.setStatusText(c, exp + NbBundle.getMessage(
                                EditorFindSupport.class, NOT_FOUND_LOCALE), IMPORTANCE_FIND_OR_REPLACE);
                }else{
                    MessageFormat fmt = new MessageFormat(
                                            NbBundle.getMessage(EditorFindSupport.class, ITEMS_REPLACED_LOCALE));
                    String msg = fmt.format(new Object[] { Integer.valueOf(replacedCnt), Integer.valueOf(totalCnt) });
                    ComponentUtils.setStatusText(c, msg, IMPORTANCE_FIND_OR_REPLACE);
                }

            } catch (BadLocationException e) {
                LOG.log(Level.WARNING, e.getMessage(), e);
            } finally {
                DocUtils.atomicUnlock(doc);
                if (blockSearch){
                    setBlockSearchHighlight(blockSearchStartOffset, getBlockEndOffset());
                }
            }
        }
    }

    public void hookLayer(BlockHighlighting layer, JTextComponent component) {
        synchronized (comp2layer) {
            Map<String, WeakReference<BlockHighlighting>> type2layer = comp2layer.get(component);

            if (type2layer == null) {
                type2layer = new HashMap<>();
                comp2layer.put(component, type2layer);
            }

            type2layer.put(layer.getLayerTypeId(), new WeakReference<>(layer));
        }
    }
    
    public void unhookLayer(BlockHighlighting layer, JTextComponent component) {
        synchronized (comp2layer) {
            Map<String, WeakReference<BlockHighlighting>> type2layer = comp2layer.get(component);

            if (type2layer != null) {
                type2layer.remove(layer.getLayerTypeId());
                if (type2layer.isEmpty()) {
                    comp2layer.remove(component);
                }
            }
        }
    }
    
    public BlockHighlighting findLayer(JTextComponent component, String layerId) {
        synchronized (comp2layer) {
            Map<String, WeakReference<BlockHighlighting>> type2layer = comp2layer.get(component);
            BlockHighlighting layer = null;

            if (type2layer != null) {
                WeakReference<BlockHighlighting> ref = type2layer.get(layerId);
                if (ref != null) {
                    layer = ref.get();
                }
            }

            return layer;
        }
    }

    /** Add weak listener to listen to change of any property. The caller must
    * hold the listener object in some instance variable to prevent it
    * from being garbage collected.
    */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }

    public synchronized void addPropertyChangeListener(String findPropertyName,
            PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(findPropertyName, l);
    }

    /** Remove listener for changes in properties */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    /**
     * <p><b>IMPORTANT:</b> This method is public only for keeping backwards
     * compatibility of the {@link org.netbeans.editor.FindSupport} class.
     */
    public void firePropertyChange(String settingName, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(settingName, oldValue, newValue);
    }

    public void setHistory(List<SPW> spwList){
        this.historyList = new ArrayList<>(spwList);
        if (!spwList.isEmpty()) {
            setLastSelected(spwList.get(0));
//        firePropertyChange(FIND_HISTORY_CHANGED_PROP,null,null);
        }
    }
    
    public void setReplaceHistory(List<RP> rpList){
        this.replaceList = new ArrayList<>(rpList);
    }
    
    public List<SPW> getHistory(){
        if (historyList.isEmpty()) {
            firePropertyChange(FIND_HISTORY_CHANGED_PROP,null,null);
        }
        return historyList;
    }
    
    public List<RP> getReplaceHistory(){
        if (replaceList.isEmpty()) {
            firePropertyChange(REPLACE_HISTORY_CHANGED_PROP,null,null);
        }
        return replaceList;
    }
    
    public void setLastSelected(SPW spw){
        this.lastSelected = spw;
        Map<String, Object> props = getFindProperties();
        if (spw == null) {
            return;
        }
        props.put(FIND_WHAT, spw.getSearchExpression());
        props.put(FIND_MATCH_CASE, Boolean.valueOf(spw.isMatchCase()));
        props.put(FIND_REG_EXP, Boolean.valueOf(spw.isRegExp()));
        props.put(FIND_WHOLE_WORDS, Boolean.valueOf(spw.isWholeWords()));
    }
    
    public SPW getLastSelected(){
        return lastSelected;
    }
    
    public void addToHistory(SPW spw){
        if (spw == null) {
            return;
        }
        firePropertyChange(FIND_HISTORY_PROP, null, spw);
    }
    
    public void addToReplaceHistory(RP rp) {
        if (rp == null) {
            return;
        }
        firePropertyChange(REPLACE_HISTORY_PROP, null, rp);
    }

    private String calculateCacheKey(Document doc, int startOffset, int endOffset, Map<String, Object> props) {
        StringBuilder newCacheKey = new StringBuilder();
        newCacheKey.append("#").append(doc.getLength());
        newCacheKey.append("#").append(startOffset);
        newCacheKey.append("#").append(endOffset);
        newCacheKey.append("#").append(props.get(FIND_WHAT));
        newCacheKey.append("#").append(props.get(FIND_HIGHLIGHT_SEARCH));
        newCacheKey.append("#").append(props.get(FIND_INC_SEARCH));
        newCacheKey.append("#").append(props.get(FIND_BACKWARD_SEARCH));
        newCacheKey.append("#").append(props.get(FIND_WRAP_SEARCH));
        newCacheKey.append("#").append(props.get(FIND_MATCH_CASE));
        newCacheKey.append("#").append(props.get(FIND_SMART_CASE));
        newCacheKey.append("#").append(props.get(FIND_WHOLE_WORDS));
        newCacheKey.append("#").append(props.get(FIND_REG_EXP));
        newCacheKey.append("#").append(props.get(FIND_BLOCK_SEARCH));
        newCacheKey.append("#").append(props.get(FIND_BLOCK_SEARCH_START));
        newCacheKey.append("#").append(props.get(FIND_BLOCK_SEARCH_END));
        return newCacheKey.toString();
    }
    
    public static final class SPW{
        private final String searchExpression;
        private final boolean wholeWords;
        private final boolean matchCase;
        private final boolean regExp;
        
        public SPW(String searchExpression, boolean wholeWords,
            boolean matchCase, boolean regExp){
            this.searchExpression = searchExpression;
            this.wholeWords = wholeWords;
            this.matchCase = matchCase;
            this.regExp = regExp;
        }
        
        /** @return searchExpression */
        public String getSearchExpression(){
            return searchExpression;
        }

        /** @return true if the wholeWords parameter was used during search performing */
        public boolean isWholeWords(){
            return wholeWords;
        }

        /** @return true if the matchCase parameter was used during search performing */
        public boolean isMatchCase(){
            return matchCase;
        }
        
        /** @return true if the regExp parameter was used during search performing */
        public boolean isRegExp(){
            return regExp;
        }

        public @Override boolean equals(Object obj){
            if (!(obj instanceof SPW)){
                return false;
            }
            SPW sp = (SPW)obj;
            return (this.searchExpression.equals(sp.getSearchExpression()) &&
                    this.wholeWords == sp.isWholeWords() &&
                    this.matchCase == sp.isMatchCase() &&
                    this.regExp == sp.isRegExp());
        }

        public @Override int hashCode() {
            int result = 17;
            result = 37*result + (this.wholeWords ? 1:0);
            result = 37*result + (this.matchCase ? 1:0);
            result = 37*result + (this.regExp ? 1:0);
            result = 37*result + this.searchExpression.hashCode();
            return result;
        }
        
        public @Override String toString(){
            StringBuilder sb = new StringBuilder("[SearchPatternWrapper:]\nsearchExpression:"+searchExpression);//NOI18N
            sb.append('\n');
            sb.append("wholeWords:");//NOI18N
            sb.append(wholeWords);
            sb.append('\n');
            sb.append("matchCase:");//NOI18N
            sb.append(matchCase);
            sb.append('\n');
            sb.append("regExp:");//NOI18N
            sb.append(regExp);
            return  sb.toString();
        }
    } // End of SPW class

    public static final class RP {

        private final String replaceExpression;
        private final boolean preserveCase;

        public RP(String replaceExpression, boolean preserveCase) {
            this.replaceExpression = replaceExpression;
            this.preserveCase = preserveCase;
        }

        public String getReplaceExpression() {
            return replaceExpression;
        }

        public boolean isPreserveCase() {
            return preserveCase;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof RP)) {
                return false;
            }
            RP sp = (RP) obj;
            return (this.replaceExpression.equals(sp.getReplaceExpression())
                    && this.preserveCase == sp.isPreserveCase());
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 37 * result + (this.preserveCase ? 1 : 0);
            result = 37 * result + this.replaceExpression.hashCode();
            return result;
        }
    }
}

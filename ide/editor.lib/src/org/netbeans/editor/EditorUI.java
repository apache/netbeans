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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JViewport;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.plaf.TextUI;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import org.netbeans.api.editor.EditorUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.api.editor.StickyWindowSupport;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.editor.lib.ColoringMap;
import org.netbeans.modules.editor.lib.EditorExtPackageAccessor;
import org.netbeans.modules.editor.lib.drawing.DrawLayerList;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib.KitsTracker;
import org.netbeans.modules.editor.lib.SettingsConversions;
import org.netbeans.modules.editor.lib.drawing.EditorUiAccessor;
import org.netbeans.modules.editor.lib2.EditorApiPackageAccessor;
import org.openide.util.WeakListeners;

/**
* Editor UI for the component. All the additional UI features
* like advanced scrolling, info about fonts, abbreviations,
* keyword matching are based on this class.
*
* @author Miloslav Metelka
* @version 1.00
*/
public class EditorUI implements ChangeListener, PropertyChangeListener, MouseListener {

    private static final Logger LOG = Logger.getLogger(EditorUI.class.getName());
    
    public static final String OVERWRITE_MODE_PROPERTY = "overwriteMode"; // NOI18N

    public static final String COMPONENT_PROPERTY = "component"; // NOI18N

    /** Default scrolling type is used for the standard
    * setDot() call. If the area is on the screen, it
    * jumps to it, otherwise it centers the requested area
    * vertically in the middle of the window and it uses
    * smallest covering on the right side.
    */
    public static final int SCROLL_DEFAULT = 0;

    /** Scrolling type used for regular caret moves.
    * The scrollJump is used when the caret requests area outside the screen.
    */
    public static final int SCROLL_MOVE = 1;

    /** Scrolling type where the smallest covering
    * for the requested rectangle is used. It's useful
    * for going to the end of the line for example.
    */
    public static final int SCROLL_SMALLEST = 2;

    /** Scrolling type for find operations, that can
    * request additional configurable area in each
    * direction, so the context around is visible too.
    */
    public static final int SCROLL_FIND = 3;


    /* package */ static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);
    
    private static final Insets DEFAULT_INSETS = new Insets(0, 2, 0, 0);    

    /** Default margin on the left and right side of the line number */
    public static final Insets defaultLineNumberMargin = new Insets(0, 3, 0, 3);

    /** Component this extended UI is related to. */
    private JTextComponent component;

    private JComponent extComponent;
    
    private JToolBar toolBarComponent;

    /** Property change support for firing property changes */
    PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /** Document for the case ext ui is constructed without the component */
    private BaseDocument printDoc;

    /** Map holding the [name, coloring] pairs */
    private ColoringMap coloringMap;

    /** Character (or better line) height. Particular view can use a different
    * character height however most views will probably use this one.
    */
    private int lineHeight = -1;

    private float lineHeightCorrection = 1.0f;

    /** Ascent of the line which is maximum ascent of all the fonts used. */
    private int lineAscent = -1;

    /** Width of the space in the default coloring's font */
    int defaultSpaceWidth = 1;

    /** Should the search words be colored? */
    boolean highlightSearch;
    
    /** Enable displaying line numbers. Both this flag and <code>lineNumberVisibleSetting</code>
    * must be true to have the line numbers visible in the window. This flag is false
    * by default. It's turned on automatically if the getExtComponent is called.
    */
    boolean lineNumberEnabled;

    /** This flag corresponds to the LINE_NUMBER_VISIBLE setting. */
    boolean lineNumberVisibleSetting;

    /** Whether to show line numbers or not. This flag is obtained using bitwise AND
    * operation on lineNumberEnabled flag and lineNumberVisibleSetting flag.
    */
    boolean lineNumberVisible;

    /** Line number total width with indentation. It includes left and right
    * line-number margins and lineNumberDigitWidth * lineNumberMaxDigitCount.
    */
    int lineNumberWidth;

    /** Width of one digit used for line numbering. It's based
    * on the information from the line coloring.
    */
    int lineNumberDigitWidth;

    /** Current maximum count of digits in line number */
    int lineNumberMaxDigitCount;

    /** Margin between the line-number bar and the text. */
    int textLeftMarginWidth;

    /** This is the full margin around the text. The left margin
    * is an addition of component's margin and lineNumberWidth 
    * and textLeftMarginWidth.
    */
    Insets textMargin = DEFAULT_INSETS;

    /** How much columns/lines to add when the scroll is performed
    * so that the component is not scrolled so often.
    * Negative number means portion of the extent width/height
    */
    Insets scrollJumpInsets;

    /** How much columns/lines to add when the scroll is performed
    * so that the component is not scrolled so often.
    * Negative number means portion of the extent width/height
    */
    Insets scrollFindInsets;

    /** EditorUI properties */
    private final HashMap<Object, Object> props = new HashMap<Object, Object>(11);

    boolean textLimitLineVisible;

    int textLimitWidth;

    private Abbrev abbrev;

    private WordMatch wordMatch;

    private Object componentLock;

    /** Status bar */
    StatusBar statusBar;

    private FocusAdapter focusL;

    Map<?, ?> renderingHints;

    /** Glyph gutter used for drawing of annotation glyph icons. */
    private GlyphGutter glyphGutter = null;

    /** The line numbers can be shown in glyph gutter and therefore it is necessary 
     * to disable drawing of lines here. During the printing on the the other hand, line 
     * numbers must be visible. */
    private boolean disableLineNumbers = true;

    /** Left right corner of the JScrollPane */
    private JPanel glyphCorner;
    
    private boolean popupMenuEnabled;

    public static final String LINE_HEIGHT_CHANGED_PROP = "line-height-changed-prop"; //NOI18N
    public static final String TAB_SIZE_CHANGED_PROP = "tab-size-changed-prop"; //NOI18N

    /** init paste action #39678 */
    private static boolean isPasteActionInited = false;

    private Preferences prefs = null;
    private final Listener listener = new Listener();
    private PreferenceChangeListener weakPrefsListener = null;

    private final DrawLayerList drawLayerList = new DrawLayerList();
    private StickyWindowSupport stickyWindowSupport;

    /** Construct extended UI for the use with a text component */
    public EditorUI() {
        focusL = new FocusAdapter() {
                     public @Override void focusGained(FocusEvent evt) {
                         /* Fix of #25475 - copyAction's enabled flag
                          * must be updated on focus change
                          */
                         stateChanged(null);
                         if (component!=null){
                            BaseTextUI ui = (BaseTextUI)component.getUI();
                            if (ui!=null) ui.refresh();
                         }
                     }

                     @Override
                     public void focusLost(FocusEvent e) {
                         // see #222935, update actions before menu activates
                         if (e.isTemporary()) {
                             doStateChange(true);
                         }
                     }
                 };

        getToolTipSupport();
    }

    /** Construct extended UI for printing the given document */
    public EditorUI(BaseDocument printDoc) {
        this(printDoc, true, true);
    }
        
    /**
     * Construct extended UI for printing the given document
     * and specify which set of colors should be used.
     *
     * @param printDoc document that should be printed.
     * @param usePrintColoringMap Ignored.
     *  of the regular ones.
     * @param lineNumberEnabled if set to false the line numbers will not be printed.
     *  If set to true the visibility of line numbers depends on lineNumberVisibleSetting.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public EditorUI(BaseDocument printDoc, boolean usePrintColoringMap, boolean lineNumberEnabled) {
        this.printDoc = printDoc;
        listener.preferenceChange(null);
        setLineNumberEnabled(lineNumberEnabled);
        updateLineNumberWidth(0);
    }
    
    /**
     * Tries to gather all colorings defined for the kit's mime type. If the
     * mime type can't be determined from the editor kit's class passed in, this
     * method will try to load colorings defined for the empty mime path (ie. all
     * languages).
     * 
     * @param kitClass The kit class for which the colorings should be loaded.
     * 
     * @return The map with all colorings defined for the mime type of the editor
     *   kit class passed in. The returned map may be inaccurate, please use 
     *   the new Editor Settings API and its <code>FontColorSettings</code> class.
     * 
     * @deprecated Use Editor Settings API instead.
     */
    @Deprecated
    protected static Map<String, Coloring> getSharedColoringMap(Class kitClass) {
        String mimeType = KitsTracker.getInstance().findMimeType(kitClass);
        return ColoringMap.get(mimeType).getMap();
    }

    /**
     * A workaround method to initialize lineHeight variable
     * so that the DrawEngineLineView can use it.
     */
    void initLineHeight(JTextComponent c) {
        // Initialize lineHeight variable from the given component
        updateLineHeight(c);
    }

    /** Called when the <code>BaseTextUI</code> is being installed
    * into the component.
    */
    protected void installUI(JTextComponent c) {
        
        // initialize preferences
        String mimeType = Utilities.getMimeType(c);
        MimePath mimePath = MimePath.parse(mimeType);
        prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
        
        // Initialize the coloring map
        this.coloringMap = ColoringMap.get(mimeType);
        this.coloringMap.addPropertyChangeListener(WeakListeners.propertyChange(this, coloringMap));

        // initialize rendering hints
        FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        renderingHints = (Map<?, ?>) fcs.getFontColors(FontColorNames.DEFAULT_COLORING).getAttribute(EditorStyleConstants.RenderingHints);

        synchronized (getComponentLock()) {
            this.component = c;
            
            putProperty(COMPONENT_PROPERTY, c);

            // listen on component
            component.addPropertyChangeListener(this);
            component.addFocusListener(focusL);
            component.addMouseListener(this);

            // listen on caret
            Caret caret = component.getCaret();
            if (caret != null) {
                caret.addChangeListener(this);
            }

            BaseDocument doc = getDocument();
            if (doc != null) {
                modelChanged(null, doc);
            }
        }

        // Make sure all the things depending on non-null component will be updated
        weakPrefsListener = WeakListeners.create(PreferenceChangeListener.class, listener, prefs);
        prefs.addPreferenceChangeListener(weakPrefsListener);
        listener.preferenceChange(null);
        
        if (!GraphicsEnvironment.isHeadless()) {
            // enable drag and drop feature
            component.setDragEnabled(true);
        }
    }

    /** Called when the <code>BaseTextUI</code> is being uninstalled
    * from the component.
    */
    protected void uninstallUI(JTextComponent c) {

        // stop listening
        if (prefs != null && weakPrefsListener != null) {
            prefs.removePreferenceChangeListener(weakPrefsListener);
        }
        if (coloringMap != null) {
            coloringMap.removePropertyChangeListener(this);
        }
        
        synchronized (getComponentLock()) {
            // fix for issue 12996
            if (component != null) {
                
                // stop listening on caret
                Caret caret = component.getCaret();
                if (caret != null) {
                    caret.removeChangeListener(this);
                }

                // stop listening on component
                component.removePropertyChangeListener(this);
                component.removeFocusListener(focusL);
                component.removeMouseListener(this);

                // Detach and clear JTextComponent.inputMethodRequestsHandler since it's strongly held by document
                try {
                    Field inputMethodRequestsHandlerField = JTextComponent.class.getDeclaredField("inputMethodRequestsHandler"); //NOI18N
                    inputMethodRequestsHandlerField.setAccessible(true);
                    Object value = inputMethodRequestsHandlerField.get(component);
                    if (value instanceof DocumentListener) {
                        component.getDocument().removeDocumentListener((DocumentListener) value);
                        inputMethodRequestsHandlerField.set(component, null);
                    }
                } catch (Exception e) {
                    // Ignore exception -> it means a possible mem leak until all clones of a document get closed
                }
            }

            BaseDocument doc = getDocument();
            if (doc != null) {
                modelChanged(doc, null);
            }

            extComponent = null;
            component = null;
            putProperty(COMPONENT_PROPERTY, null);
            
            // Clear the font-metrics cache
            FontMetricsCache.clear();
        }

        // destroy all the stuff we created
        coloringMap = null;
        prefs = null;
        weakPrefsListener = null;
        renderingHints = null;
    }

    /** Get the lock assuring the component will not be changed
    * by <code>installUI()</code> or <code>uninstallUI()</code>.
    * It's useful for the classes that want to listen for the
    * component change in <code>EditorUI</code>.
    */
    public Object getComponentLock() {
        if (componentLock == null) {
            componentLock = new ComponentLock();
        }
        return componentLock;
    }
    static class ComponentLock {};

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, l);
    }

    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void settingsChangeImpl(String settingName) {
    }
    
    public @Override void stateChanged(ChangeEvent evt) {
        doStateChange(false);
    }
    
    private void doStateChange(final boolean b) {
        SwingUtilities.invokeLater(
            new Runnable() {
                
                /** @return true if the document supports guarded sections
                 * and when either the caret is in guarded block
                 * or when selection spans any guarded block(s).
                 */
                private boolean [] isCaretGuarded() {
                    JTextComponent c = component;
                    BaseDocument bdoc = getDocument();
                    if (bdoc instanceof GuardedDocument){
                        GuardedDocument gdoc = (GuardedDocument)bdoc;

                        boolean selectionSpansGuardedSection = false;
                        for (int i=c.getSelectionStart(); i<c.getSelectionEnd(); i++){
                            if (gdoc.isPosGuarded(i)){
                                selectionSpansGuardedSection = true;
                                break;
                            }
                        }

                        if (selectionSpansGuardedSection) {
                            return new boolean [] { true, true };
                        } else {
                            int offset = c.getCaretPosition();
                            boolean guarded = gdoc.isPosGuarded(offset);
                            boolean startGuarded = guarded;
                            // only enable paste if on start of the 1st guarded line, not at each line start.
                            if (offset > 0 && !gdoc.isPosGuarded(offset -1) &&
                                org.netbeans.lib.editor.util.swing.DocumentUtilities.getText(bdoc).charAt(offset - 1) == '\n') { // NOI18N
                                startGuarded = false;
                            }
                            return new boolean [] {
                                guarded,
                                startGuarded
                            };
                        }
                    }
                    return new boolean [] { false, false };
                }
                
                public @Override void run() {
                    JTextComponent c = component;
                    if (c != null && (b || c.hasFocus())) { // do nothing if the component does not have focus, see #110715
                        BaseKit kit = Utilities.getKit(c);
                        if (kit != null) {
                            boolean isEditable = c.isEditable();
                            boolean selectionVisible = Utilities.isSelectionShowing(c);
                            boolean [] caretGuarded = isCaretGuarded();

                            Action a = kit.getActionByName(BaseKit.copyAction);
                            if (a != null) {
                                // In order to allow the action to operate even if there is no selection
                                // (to copy a single line) the action is always enabled.
                                a.setEnabled(true);
                            }

                            a = kit.getActionByName(BaseKit.cutAction);
                            if (a != null) {
                                a.setEnabled(!caretGuarded[0] && isEditable);
                            }

                            a = kit.getActionByName(BaseKit.removeSelectionAction);
                            if (a != null) {
                                a.setEnabled(selectionVisible && !caretGuarded[0] && isEditable);
                            }

                            a = kit.getActionByName(BaseKit.pasteAction);
                            if (a != null) {
                                if (!isPasteActionInited) {
                                    // #39678
                                    a.setEnabled(!a.isEnabled());
                                    isPasteActionInited = true;
                                }
                                a.setEnabled(!caretGuarded[1] && isEditable);
                            }

                            a = kit.getActionByName(BaseKit.pasteFormatedAction);
                            if (a != null) {
                                a.setEnabled(!caretGuarded[1] && isEditable);
                            }
                        }
                    }
                }
            }
        );
    }

    protected void modelChanged(BaseDocument oldDoc, BaseDocument newDoc) {
        if (newDoc != null) {
            coloringMap = ColoringMap.get(Utilities.getMimeType(newDoc));
            listener.preferenceChange(null);

            checkUndoManager(newDoc);
        }
    }
    
    private void checkUndoManager(Document doc) {
        // Check if there's any undo manager listening already and if so return immediately
        if (doc instanceof AbstractDocument && ((AbstractDocument)doc).getUndoableEditListeners().length >= 1) {
            return;
        }
        // If there is the wrapper component the default UndoManager
        // of the document gets cleared so that only the TopComponent's one gets used.
        UndoManager undoManager = (UndoManager) doc.getProperty(BaseDocument.UNDO_MANAGER_PROP);
        if (undoManager == null) {
            undoManager = (UndoManager) doc.getProperty(UndoManager.class);
        }
        if (hasExtComponent()) {
            if (undoManager != null) {
                doc.removeUndoableEditListener(undoManager);
                doc.putProperty(BaseDocument.UNDO_MANAGER_PROP, null);
                doc.putProperty(UndoManager.class, null); // For tests compatibility
            }
        } else { // Implicit use e.g. an editor pane in a dialog
            if (undoManager == null) {
                // By default have an undo manager in UNDO_MANAGER_PROP
                undoManager = new UndoManager();
                doc.addUndoableEditListener(undoManager);
                doc.putProperty(BaseDocument.UNDO_MANAGER_PROP, undoManager);
                doc.putProperty(UndoManager.class, undoManager); // For tests compatibility
            }
        }
    }

    public @Override void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if ("document".equals(propName)) { // NOI18N
            BaseDocument oldDoc = (evt.getOldValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getOldValue() : null;
            BaseDocument newDoc = (evt.getNewValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getNewValue() : null;
            modelChanged(oldDoc, newDoc);

        } else if ("margin".equals(propName)) { // NOI18N
            updateTextMargin();

        } else if ("caret".equals(propName)) { // NOI18N
            if (evt.getOldValue() instanceof Caret) {
                ((Caret)evt.getOldValue()).removeChangeListener(this);
            }
            if (evt.getNewValue() instanceof Caret) {
                ((Caret)evt.getNewValue()).addChangeListener(this);
            }

        } else if ("enabled".equals(propName)) { // NOI18N
            if (!component.isEnabled()) {
                component.getCaret().setVisible(false);
            }
        } else if (EditorUtilities.CARET_OVERWRITE_MODE_PROPERTY.equals(propName)) {
            // Mirror the property into EditorUI
            putProperty(OVERWRITE_MODE_PROPERTY, component.getClientProperty(EditorUtilities.CARET_OVERWRITE_MODE_PROPERTY));
        }
        
        if (propName == null || ColoringMap.PROP_COLORING_MAP.equals(propName)) {
            listener.preferenceChange(null);
        }
    }

    /**
     * @deprecated Use Editor Settings or Editor Settings Storage API instead.
     *   This method is never called.
     */
    @Deprecated
    protected Map createColoringMap() {
        return Collections.emptyMap();
    }

    public int getLineHeight() {
        if (lineHeight == -1 && component != null) {
            updateLineHeight(component);
        }
        return lineHeight > 0 ? lineHeight : 1;
    }

    public int getLineAscent() {
        if (lineAscent == -1 && component != null) {
            updateLineHeight(component);
        }
        return lineAscent > 0 ? lineAscent : 1;
    }

    /**
     * Tries to gather all colorings defined for the component's mime type. If
     * this instance is not installed to any component the method will try to
     * load colorings for the empty mime path (ie. all languages).
     * 
     * @return The map with all colorings defined the mime type of this instance.
     *   The returned map may be inaccurate, please use the new Editor Settings
     *   API and its <code>FontColorSettings</code> class.
     * 
     * @deprecated Use Editor Settings API instead.
     */
    @Deprecated
    public Map<String, Coloring> getColoringMap() {
        // Return mutable map
        return new HashMap<String, Coloring>(getCMInternal());
    }

    private Map<String, Coloring> getCMInternal() {
        ColoringMap cm = coloringMap;
        if (cm != null) {
            return cm.getMap();
        } else {
            return ColoringMap.get(null).getMap();
        }
    }
    
    /**
     * Gets the default coloring. The default coloring is the one called
     * <code>FontColorNames.DEFAULT_COLORING</code> and defined for the empty
     * mime path (ie. all languages).
     * 
     * @deprecated Use Editor Settings API instead.
     */
    @Deprecated
    public Coloring getDefaultColoring() {
        final MimePath mimePath;
        if (component != null)
            mimePath = MimePath.get(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(component));
        else
            mimePath = MimePath.EMPTY;
        FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        Coloring c = Coloring.fromAttributeSet(fcs.getFontColors(FontColorNames.DEFAULT_COLORING));
        assert c != null : "No default coloring!"; //NOI18N
        return c;
    }

    /**
     * Gets a coloring from the coloring map. This metod uses the coloring map
     * returned by <code>getColoringMap</code> to find a coloring by its name.
     * 
     * @param coloringName The name of the coloring to find.
     * 
     * @return The coloring or <code>null</code> if there is no coloring with the
     *   requested name.
     * @deprecated Use Editor Settings API instead.
     */
    @Deprecated
    public Coloring getColoring(String coloringName) {
        return getCMInternal().get(coloringName);
    }

    private void updateLineHeight(final JTextComponent component) {
        if (component == null) {
            return;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Computing lineHeight for '" + Utilities.getMimeType(component) + "'"); //NOI18N
        }
        
        Map<String, Coloring> cm = getCMInternal();
        int maxHeight = -1;
        int maxAscent = -1;
        for(String coloringName : cm.keySet()) {
            if (FontColorNames.STATUS_BAR_COLORING.equals(coloringName) ||
                FontColorNames.STATUS_BAR_BOLD_COLORING.equals(coloringName)
            ) {
                //#57112
                continue;
            }

            Coloring c = cm.get(coloringName);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Probing coloring '" + coloringName + "' : " + c);
            }

            if (c != null) {
                Font font = c.getFont();
                if (font != null && (c.getFontMode() & Coloring.FONT_MODE_APPLY_SIZE) != 0) {
                    FontMetrics fm = FontMetricsCache.getFontMetrics(font, component);
                    if (fm != null) {
                        if (LOG.isLoggable(Level.FINE)) {
                            if (maxHeight < fm.getHeight()) {
                                LOG.fine("Updating maxHeight from " //NOI18N
                                    + maxHeight + " to " + fm.getHeight() // NOI18N
                                    + ", coloringName=" + coloringName // NOI18N
                                    + ", font=" + font // NOI18N
                                );
                            }

                            if (maxAscent < fm.getAscent()) {
                                LOG.fine("Updating maxAscent from " //NOI18N
                                    + maxAscent + " to " + fm.getAscent() // NOI18N
                                    + ", coloringName=" + coloringName // NOI18N
                                    + ", font=" + font // NOI18N
                                );
                            }
                        }

                        maxHeight = Math.max(maxHeight, fm.getHeight());
                        maxAscent = Math.max(maxAscent, fm.getAscent());
                    }
                }
            }
        }

        final View rootView = Utilities.getDocumentView(component);
        final int[] wrapMaxHeight = new int[] { -1 };
        if (rootView != null) {
            Utilities.runViewHierarchyTransaction(component, true, new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 1 /*rootView.getViewCount()*/; i++) { // scan just first line for now
                        View view = rootView.getView(i);
                        if (view == null) { // seen in tests
                            break;
                        }

                        int offset = view.getStartOffset();
                        Rectangle r = null;

                        try {
                            r = component.getUI().modelToView(component, offset);
                        } catch (BadLocationException ble) {
                            LOG.log(Level.INFO, null, ble);
                        }

                        if (r == null) {
                            break;
                        }

                        if (LOG.isLoggable(Level.FINE)) {
                            if (wrapMaxHeight[0] < r.getHeight()) {
                                try {
                                    LOG.fine("Updating maxHeight from " //NOI18N
                                            + wrapMaxHeight + " to " + r.getHeight() // NOI18N
                                            + ", line=" + i // NOI18N
                                            + ", text=" + component.getDocument().getText(offset, view.getEndOffset() - offset) //NOI18N
                                            );
                                } catch (BadLocationException ble) {
                                    LOG.log(Level.FINE, null, ble);
                                }
                            }
                        }

                        wrapMaxHeight[0] = Math.max(wrapMaxHeight[0], (int) r.getHeight());
                    }
                }
            });
        }
        if (wrapMaxHeight[0] > 0) {
            maxHeight = wrapMaxHeight[0];
        }
        
        if (maxAscent > 0) {
            lineAscent = (int)(maxAscent * lineHeightCorrection);
        }
        
        if (maxHeight > 0) {
            int oldLineHeight = lineHeight;
            lineHeight = (int)(maxHeight * lineHeightCorrection);
            if (oldLineHeight != lineHeight && oldLineHeight != -1) {
                firePropertyChange(LINE_HEIGHT_CHANGED_PROP, Integer.valueOf(oldLineHeight), Integer.valueOf(lineHeight));
            }
        }
    }
    
    /**
     * Update various properties of the component in AWT thread.
     */
    void updateComponentProperties() {

        // Refresh rendering hints
        String mimeType = org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(component);
        FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
        renderingHints = (Map<?, ?>) fcs.getFontColors(FontColorNames.DEFAULT_COLORING).getAttribute(EditorStyleConstants.RenderingHints);
        
        // Set the margin
        String value = prefs.get(SimpleValueNames.MARGIN, null);
        Insets margin = value != null ? SettingsConversions.parseInsets(value) : null;
        component.setMargin(margin != null ? margin : NULL_INSETS);

        lineNumberDigitWidth = computeLineNumberDigitWidth();

        // Update line height
        updateLineHeight(getComponent());

        // Update space width of the default coloring's font
        FontMetricsCache.Info fmcInfo = FontMetricsCache.getInfo(getDefaultColoring().getFont());
        defaultSpaceWidth = fmcInfo.getSpaceWidth(component);

        updateLineNumberWidth(0);

        // update glyph gutter colors and fonts
        if (isGlyphGutterVisible()) {
            glyphGutter.update();
            updateScrollPaneCornerColor();
        }
    }
    
    protected void update(Graphics g) {
        // Possibly apply the rendering hints
        if (renderingHints != null) {
            ((Graphics2D)g).addRenderingHints(renderingHints);
        }
    }

    public final JTextComponent getComponent() {
        return component;
    }

    /** Get the document to work on. Either component's document or printed document
    * is returned. It can return null in case the component's document is not instance
    * of BaseDocument.
    */
    public final BaseDocument getDocument() {
        return (component != null) ? Utilities.getDocument(component) : printDoc;
    }

    private Class getKitClass() {
        return (component != null) ? Utilities.getKitClass(component)
               : ((printDoc != null) ? printDoc.getKitClass() : null);
    }

    public Object getProperty(Object key) {
        return props.get(key);
    }

    public void putProperty(Object key, Object value) {
        Object oldValue;
        if (value != null) {
            oldValue = props.put(key, value);
        } else {
            oldValue = props.remove(key);
        }
        firePropertyChange(key.toString(), oldValue, value);
    }

    /** Get extended editor component.
     * The extended component should normally be used
     * for editing files instead of just the JEditorPane
     * because it offers status bar and possibly
     * other useful components.
     * <br>
     * The component no longer includes toolbar - it's returned
     * by a separate method {@link #getToolBarComponent()}.
     * <br>
     * The getExtComponent() should not be used when
     * the JEditorPane is included in dialog.
     * @see #hasExtComponent()
     */
    public JComponent getExtComponent() {
        if (extComponent == null) {
            if (component != null) {
                extComponent = createExtComponent();
                checkUndoManager(getDocument());
            }
        }
        return extComponent;
    }

    /**
     * Get the toolbar component appropriate for this editor.
     */
    public JToolBar getToolBarComponent() {
        if (toolBarComponent == null) {
            if (component != null) {
                toolBarComponent = createToolBarComponent();
            }
        }
        return toolBarComponent;
    }
    
    /**
     * Construct the toolbar component appropriate for this editor.
     * 
     * @return non-null toolbar component or null if there is no appropriate
     *  toolbar component for this editor.
     */
    protected JToolBar createToolBarComponent() {
       return null; 
    }

    protected void initGlyphCorner(JScrollPane scroller){
        glyphCorner = new JPanel();
        updateScrollPaneCornerColor();
        scroller.setCorner(JScrollPane.LOWER_LEFT_CORNER, glyphCorner);
    }
    
    protected void setGlyphGutter(GlyphGutter gutter){
        glyphGutter = gutter;
    }
    
    public final int getSideBarWidth(){
        JScrollPane scroll = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, getParentViewport());
        if (scroll!=null && scroll.getRowHeader()!=null){
            Rectangle bounds = scroll.getRowHeader().getBounds();
            if (bounds!=null){
                return bounds.width;
            }
        }
        return 40;
    }
    
    protected JComponent createExtComponent() {
        setLineNumberEnabled(true); // enable line numbering

        // extComponent will be a panel
        JComponent ec = new JPanel(new BorderLayout());
        ec.putClientProperty(JTextComponent.class, component);

        // Add the scroll-pane with the component to the center
        JScrollPane scroller = new JScrollPane(component);
        scroller.getViewport().setMinimumSize(new Dimension(4,4));
        
        // remove default scroll-pane border, winsys will handle borders itself           
        scroller.setBorder(null);
        
        setGlyphGutter(new GlyphGutter(this));
        scroller.setRowHeaderView(glyphGutter);

        initGlyphCorner(scroller);
        
        ec.add(scroller);

        // Install the status-bar panel to the bottom
        ec.add(getStatusBar().getPanel(), BorderLayout.SOUTH);
        
        return ec;
    }
    
    /** Whether this ui uses extComponent or not.
     * @see #getExtComponent()
     */
    public boolean hasExtComponent() {
        return (extComponent != null);
    }

    /** @deprecated Use Editor Code Templates API instead. */
    @Deprecated
    public Abbrev getAbbrev() {
        if (abbrev == null) {
            abbrev = new Abbrev(this, true, true);
        }
        return abbrev;
    }

    public WordMatch getWordMatch() {
        if (wordMatch == null) {
            wordMatch = new WordMatch(this);
        }
        return wordMatch;
    }

    public StatusBar getStatusBar() {
        if (statusBar == null) {
            statusBar = new StatusBar(this);
        }
        return statusBar;
    }
    
    public void repaint(int startY) {
        repaint(startY, component.getHeight());
    }

    public void repaint(int startY, int height) {
        if (height <= 0) {
            return;
        }
        int width = Math.max(component.getWidth(), 0);
        startY = Math.max(startY, 0);
        component.repaint(0, startY, width, height);
    }

    public void repaintOffset(int pos) throws BadLocationException {
        repaintBlock(pos, pos);
    }

    /** Repaint the block between the given positions. */
    public void repaintBlock(int startPos, int endPos)
    throws BadLocationException {
        BaseTextUI ui = (BaseTextUI)component.getUI();
        if (startPos > endPos) { // swap
            int tmpPos = startPos;
            startPos = endPos;
            endPos = tmpPos;
        }
        
        int yFrom;
        int yTo;

        try {
            yFrom = ui.getYFromPos(startPos);
        } catch (BadLocationException e) {
            Utilities.annotateLoggable(e);
            yFrom = 0;
        }
        
        try {
            yTo = ui.getYFromPos(endPos) + getLineHeight();
        } catch (BadLocationException e) {
            Utilities.annotateLoggable(e);
            yTo = (int) ui.getRootView(component).getPreferredSpan(View.Y_AXIS);
        }
        
        repaint(yFrom, yTo - yFrom);
    }

    /** Is the parent of some editor component a viewport */
    private JViewport getParentViewport() {
        Component pc = component.getParent();
        if (pc instanceof JLayeredPane) {
            pc = pc.getParent();
        }
        return (pc instanceof JViewport) ? (JViewport)pc : null;
    }

    /** Finds the frame - parent of editor component */
    public static Frame getParentFrame(Component c) {
        do {
            c = c.getParent();
            if (c instanceof Frame) {
                return (Frame)c;
            }
        } while (c != null);
        return null;
    }

    /** Possibly update virtual width. If the width
    * is really updated, the method returns true.
    * @deprecated virtual size is no longer used and effects of this method are ignored
    */
    @Deprecated
    public boolean updateVirtualWidth(int width) {
        return false;
    }

    /** Possibly update virtual height. If the height
    * is really updated, the method returns true. There is
    * a slight difference against virtual width in that
    * if the height is shrinked too much the virtual height
    * is shrinked too.
    * 0 can be used to update to the real height.
    * @deprecated virtual size is no longer used and effects of this method are ignored
    */
    @Deprecated
    public boolean updateVirtualHeight(int height) {
        return false;
    }

    public boolean isLineNumberEnabled() {
        return lineNumberEnabled;
    }

    public void setLineNumberEnabled(boolean lineNumberEnabled) {
        this.lineNumberEnabled = lineNumberEnabled;
        lineNumberVisible = lineNumberEnabled && lineNumberVisibleSetting;
        if (disableLineNumbers)
            lineNumberVisible = false;
    }

    void setLineNumberVisibleSetting(boolean lineNumberVisibleSetting) {
        this.lineNumberVisibleSetting = lineNumberVisibleSetting;
    }
    
    /** Update the width that will be occupied by the line number.
    * @param maxDigitCount maximum digit count that can the line number have.
    *  if it's lower or equal to zero it will be computed automatically.
    */
    public void updateLineNumberWidth(int maxDigitCount) {
        int oldWidth = lineNumberWidth;

        if (lineNumberVisible) {
            try {
                if (maxDigitCount <= 0) {
                    BaseDocument doc = getDocument();
                    int lineCnt = LineDocumentUtils.getLineIndex(doc, doc.getLength()) + 1;
                    maxDigitCount = Integer.toString(lineCnt).length();
                }

                if (maxDigitCount > lineNumberMaxDigitCount) {
                    lineNumberMaxDigitCount = maxDigitCount;
                }

            } catch (BadLocationException e) {
                lineNumberMaxDigitCount = 1;
            }
            lineNumberWidth = lineNumberMaxDigitCount * lineNumberDigitWidth;
            Insets lineMargin = getLineNumberMargin();
            if (lineMargin != null) {
                lineNumberWidth += lineMargin.left + lineMargin.right;
            }

        } else {
            lineNumberWidth = 0;
        }

        updateTextMargin();
        if (oldWidth != lineNumberWidth) { // changed
            if (component != null) {
                component.repaint();
            }
        }
    }

    public void updateTextMargin() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public @Override void run() {
                        updateTextMargin();
                    }
                }
            );
        }

        Insets orig = textMargin;
        Insets cm = (component != null) ? component.getMargin() : null;
        int leftWidth = lineNumberWidth + textLeftMarginWidth;
        if (cm != null) {
            textMargin = new Insets(cm.top, cm.left + leftWidth,
                                    cm.bottom, cm.right);
        } else {
            textMargin = new Insets(0, leftWidth, 0, 0);
        }
        if (orig.top != textMargin.top || orig.bottom != textMargin.bottom) {
            ((BaseTextUI)component.getUI()).invalidateStartY();
        }
    }

    public Rectangle getExtentBounds() {
        return getExtentBounds(null);
    }

    /** Get position of the component extent. The (x, y) are set to (0, 0) if there's
    * no viewport or (-x, -y) if there's one.
    */
    public Rectangle getExtentBounds(Rectangle r) {
        if (r == null) {
            r = new Rectangle();
        }
        if (component != null) {
            JViewport port = getParentViewport();
            if (port != null) {
                Point p = port.getViewPosition();
                r.width = port.getWidth();
                r.height = port.getHeight();
                r.x = p.x;
                r.y = p.y;
            } else { // no viewport
                r.setBounds(component.getVisibleRect());
            }
        }
        return r;
    }

    /** Get the begining of the area covered by text */
    public Insets getTextMargin() {
        return textMargin;
    }

    /**
     * Scroll the editor window so that the given rectangle is visible.
     *
     * @param r rectangle to which the editor window should be scrolled.
     * @param scrollPolicy the way how scrolling should be done.
     *  One of <code>EditorUI.SCROLL_*</code> constants.
     *
     * @deprecated use <code>JComponent.scrollRectToVisible()</code> instead of this method.
     */
    @Deprecated
    public void scrollRectToVisible(final Rectangle r, final int scrollPolicy) {
        Utilities.runInEventDispatchThread(
            new Runnable() {
                boolean docLocked;
                public @Override void run() {
                    if (!docLocked) {
                        docLocked = true;
                        Document doc = getDocument();
                        if (doc != null) {
                            doc.render(this);
                        }
                    } else { // Document already read-locked
                        scrollRectToVisibleFragile(r, scrollPolicy);
                    }
                }
            }
        );
    }

    /** Must be called with EventDispatchThread */
    boolean scrollRectToVisibleFragile(Rectangle r, int scrollPolicy) {
        Insets margin = getTextMargin();
        Rectangle bounds = getExtentBounds();
        r = new Rectangle(r); // make copy of orig rect
        r.x -= margin.left;
        r.y -= margin.top;
        bounds.width -= margin.left + margin.right;
        bounds.height -= margin.top + margin.bottom;
        return scrollRectToVisibleImpl(r, scrollPolicy, bounds);
    }

    /** Scroll the view so that requested rectangle is best visible.
     * There are different scroll policies available.
     * @return whether the extent has to be scrolled in any direction.
     */
    private boolean scrollRectToVisibleImpl(Rectangle r, int scrollPolicy,
                                            Rectangle bounds) {
        if (bounds.width <= 0 || bounds.height <= 0) {
            return false;
        }

        // handle find scrolling specifically
        if (scrollPolicy == SCROLL_FIND) {
            // converted inset
            int cnvFI = (scrollFindInsets.left < 0)
                ? (- bounds.width * scrollFindInsets.left / 100)
                : scrollFindInsets.left * defaultSpaceWidth;

            int nx = Math.max(r.x - cnvFI, 0);
            
            cnvFI = (scrollFindInsets.right < 0)
                ? (- bounds.width * scrollFindInsets.right / 100)
                : scrollFindInsets.right * defaultSpaceWidth;

            r.width += (r.x - nx) + cnvFI;
            r.x = nx;

            cnvFI = (scrollFindInsets.top < 0)
                ? (- bounds.height * scrollFindInsets.top / 100)
                : scrollFindInsets.top * getLineHeight();

            int ny = Math.max(r.y - cnvFI, 0);

            cnvFI = (scrollFindInsets.bottom < 0)
                ? (- bounds.height * scrollFindInsets.bottom / 100)
                : scrollFindInsets.bottom * getLineHeight();

            r.height += (r.y - ny) + cnvFI;
            r.y = ny;

            return scrollRectToVisibleImpl(r, SCROLL_SMALLEST, bounds); // recall
        }
        
        int viewWidth = (int)((TextUI)component.getUI()).getRootView(component).getPreferredSpan(View.X_AXIS);
        int viewHeight = (int)((TextUI)component.getUI()).getRootView(component).getPreferredSpan(View.Y_AXIS);
        
        // r must be within virtualSize's width
        if (r.x + r.width > viewWidth) {
            r.x = viewWidth - r.width;
            if (r.x < 0) {
                r.x = 0;
                r.width = viewWidth;
            }
            return scrollRectToVisibleImpl(r, scrollPolicy, bounds); // recall
        }
        // r must be within virtualSize's height
        if (r.y +r.height  > viewHeight) {
            r.y = viewHeight - r.height;
            if (r.y < 0) {
                r.y = 0;
                r.height = viewHeight;
            }
            return scrollRectToVisibleImpl(r, scrollPolicy, bounds);
        }

        // if r extends bounds dimension it must be corrected now
        if (r.width > bounds.width || r.height > bounds.height) {
            try {
                Rectangle caretRect = component.getUI().modelToView(
                                            component, component.getCaret().getDot(), Position.Bias.Forward);
                if (caretRect.x >= r.x
                        && caretRect.x + caretRect.width <= r.x + r.width
                        && caretRect.y >= r.y
                        && caretRect.y + caretRect.height <= r.y + r.height
                   ) { // caret inside requested rect
                    // move scroll rect for best caret visibility
                    int overX = r.width - bounds.width;
                    int overY = r.height - bounds.height;
                    if (overX > 0) {
                        r.x -= overX * (caretRect.x - r.x) / r.width;
                    }
                    if (overY > 0) {
                        r.y -= overY * (caretRect.y - r.y) / r.height;
                    }
                }
                r.height = bounds.height;
                r.width = bounds.width; // could be different algorithm
                return scrollRectToVisibleImpl(r, scrollPolicy, bounds);            
            } catch (BadLocationException ble){
                LOG.log(Level.WARNING, null, ble);
            }
        }

        int newX = bounds.x;
        int newY = bounds.y;
        boolean move = false;
        // now the scroll rect is within bounds of the component
        // and can have size of the extent at maximum
        if (r.x < bounds.x) {
            move = true;
            switch (scrollPolicy) {
            case SCROLL_MOVE:
                newX = (scrollJumpInsets.left < 0)
                       ? (bounds.width * (-scrollJumpInsets.left) / 100)
                       : scrollJumpInsets.left * defaultSpaceWidth;
                newX = Math.min(newX, bounds.x + bounds.width - (r.x + r.width));
                newX = Math.max(r.x - newX, 0); // new bounds.x
                break;
            case SCROLL_DEFAULT:
            case SCROLL_SMALLEST:
            default:
                newX = r.x;
                break;
            }
            updateVirtualWidth(newX + bounds.width);
        } else if (r.x + r.width > bounds.x + bounds.width) {
            move = true;
            switch (scrollPolicy) {
            case SCROLL_SMALLEST:
                newX = r.x + r.width - bounds.width;
                break;
            default:
                newX = (scrollJumpInsets.right < 0)
                       ? (bounds.width * (-scrollJumpInsets.right) / 100 )
                       : scrollJumpInsets.right * defaultSpaceWidth;
                newX = Math.min(newX, bounds.width - r.width);
                newX = (r.x + r.width) + newX - bounds.width;
                break;
            }

            updateVirtualWidth(newX + bounds.width);
        }

        if (r.y < bounds.y) {
            move = true;
            switch (scrollPolicy) {
            case SCROLL_MOVE:
                newY = r.y;
                newY -= (scrollJumpInsets.top < 0)
                        ? (bounds.height * (-scrollJumpInsets.top) / 100 )
                        : scrollJumpInsets.top * getLineHeight();
                break;
            case SCROLL_SMALLEST:
                newY = r.y;
                break;
            case SCROLL_DEFAULT:
            default:
                newY = r.y - (bounds.height - r.height) / 2; // center
                break;
            }
            newY = Math.max(newY, 0);
        } else if (r.y + r.height > bounds.y + bounds.height) {
            move = true;
            switch (scrollPolicy) {
            case SCROLL_MOVE:
                newY = (r.y + r.height) - bounds.height;
                newY += (scrollJumpInsets.bottom < 0)
                        ? (bounds.height * (-scrollJumpInsets.bottom) / 100 )
                        : scrollJumpInsets.bottom * getLineHeight();
                break;
            case SCROLL_SMALLEST:
                newY = (r.y + r.height) - bounds.height;
                break;
            case SCROLL_DEFAULT:
            default:
                newY = r.y - (bounds.height - r.height) / 2; // center
                break;
            }
            newY = Math.max(newY, 0);
        }

        if (move) {
            setExtentPosition(newX, newY);
        }
        return move;
    }

    void setExtentPosition(int x, int y) {
        JViewport port = getParentViewport();
        if (port != null) {
            Point p = new Point(Math.max(x, 0), Math.max(y, 0));
            port.setViewPosition(p);
        }
    }

    public void adjustWindow(int caretPercentFromWindowTop) {
        final Rectangle bounds = getExtentBounds();
        if (component != null) {
            try {
                Rectangle caretRect = component.modelToView(component.getCaretPosition());
                bounds.y = caretRect.y - (caretPercentFromWindowTop * bounds.height) / 100
                        + (caretPercentFromWindowTop * getLineHeight()) / 100;
                Utilities.runInEventDispatchThread(new Runnable() {
                    public @Override void run() {
                        scrollRectToVisible(bounds, SCROLL_SMALLEST);
                    }
                });
            } catch (BadLocationException e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
    }

    /** Set the dot according to the currently visible screen window.
    * #param percentFromWindowTop percentage giving the distance of the caret
    *  from the top of the currently visible window.
    */
    public void adjustCaret(int percentFromWindowTop) {
        JTextComponent c = component;
        if (c != null) {
            Rectangle bounds = getExtentBounds();
            bounds.y += (percentFromWindowTop * bounds.height) / 100
                        - (percentFromWindowTop * getLineHeight()) / 100;
            try {
                int offset = ((BaseTextUI)c.getUI()).getPosFromY(bounds.y);
                if (offset >= 0) {
                    caretSetDot(offset, null, SCROLL_SMALLEST);
                }
            } catch (BadLocationException e) {
            }
        }
    }

    /** Set the position of the caret and scroll the extent if necessary.
     * @param offset position where the caret should be placed
     * @param scrollRect rectangle that should become visible. It can be null
     *   when no scrolling should be done.
     * @param scrollPolicy policy to be used when scrolling.
     * @deprecated
     */
    @Deprecated
    public void caretSetDot(int offset, Rectangle scrollRect, int scrollPolicy) {
        if (component != null) {
            Caret caret = component.getCaret();
            if (caret instanceof BaseCaret) {
                ((BaseCaret)caret).setDot(offset, scrollRect, scrollPolicy);
            } else {
                caret.setDot(offset);
            }
        }
    }

    /** Set the position of the caret and scroll the extent if necessary.
     * @param offset position where the caret should be placed
     * @param scrollRect rectangle that should become visible. It can be null
     *   when no scrolling should be done.
     * @param scrollPolicy policy to be used when scrolling.
     * @deprecated
     */
    @Deprecated
    public void caretMoveDot(int offset, Rectangle scrollRect, int scrollPolicy) {
        if (component != null) {
            Caret caret = component.getCaret();
            if (caret instanceof BaseCaret) {
                ((BaseCaret)caret).moveDot(offset, scrollRect, scrollPolicy);
            } else {
                caret.moveDot(offset);
            }
        }
    }

    /** This method is called by textui to do the paint.
    * It is forwarded either to paint through the image
    * and then copy the image area to the screen or to
    * paint directly to this graphics. The real work occurs
    * in draw-engine.
    */
    protected void paint(Graphics g) {
        if (component != null) { // component must be installed
            update(g);
        }
    }

    /** Returns the line number margin */
    public Insets getLineNumberMargin() {
        return defaultLineNumberMargin;
    }

    private int computeLineNumberDigitWidth(){
        // Handle line number fonts and widths
        Coloring dc = getDefaultColoring();        
        Coloring lnc = getCMInternal().get(FontColorNames.LINE_NUMBER_COLORING);
        if (lnc != null) {
            Font lnFont = lnc.getFont();
            if (lnFont == null) {
                lnFont = dc.getFont();
            }
            if (component == null) return lineNumberDigitWidth;
            FontMetrics lnFM = FontMetricsCache.getFontMetrics(lnFont, component);
            if (lnFM == null) return lineNumberDigitWidth;
            int maxWidth = 1;
            for (int i = 0; i <= 9; i++) {
                maxWidth = Math.max(maxWidth, lnFM.charWidth((char)('0' + i)));
            }
            return maxWidth;
        }
        return lineNumberDigitWidth;
    }
    
    /** Returns width of the one digit */
    public int getLineNumberDigitWidth() {
        return lineNumberDigitWidth;
    }

    /** Is glyph gutter created and visible for the document or not */
    public boolean isGlyphGutterVisible() {
        return glyphGutter != null;
    }
    
    public final GlyphGutter getGlyphGutter() {
        return glyphGutter;
    }
    
    protected void updateScrollPaneCornerColor() {
        Coloring lineColoring = getCMInternal().get(FontColorNames.LINE_NUMBER_COLORING);
        Coloring defaultColoring = getDefaultColoring();
        
        Color backgroundColor;
        if (lineColoring != null && lineColoring.getBackColor() != null) {
            backgroundColor = lineColoring.getBackColor();
        } else {
            backgroundColor = defaultColoring.getBackColor();
        }
        
        if (glyphCorner != null){
            glyphCorner.setBackground(backgroundColor);
        }
    }

    protected int textLimitWidth() {
        int ret = textLimitWidth;
        Object textLimitLine = component == null ? null : component.getClientProperty("TextLimitLine"); //NOI18N
        if (textLimitLine instanceof Integer) {
            ret = ((Integer)textLimitLine).intValue();
        }
        return ret;
    }

    private class Listener implements PreferenceChangeListener {

        public @Override void preferenceChange(PreferenceChangeEvent evt) {
            // ignore events that come after uninstalling the EditorUI from a component
            if (prefs == null) {
                disableLineNumbers = false;
                return;
            }
            
            String settingName = evt == null ? null : evt.getKey();
            settingsChangeImpl(settingName);

            if (SimpleValueNames.TAB_SIZE.equals(settingName)) { // Only on explicit tab-size change to minimize perf impact
                firePropertyChange(TAB_SIZE_CHANGED_PROP, null, null);
            }
            
            if (settingName == null || SimpleValueNames.LINE_NUMBER_VISIBLE.equals(settingName)) {
                lineNumberVisibleSetting = prefs.getBoolean(SimpleValueNames.LINE_NUMBER_VISIBLE, EditorPreferencesDefaults.defaultLineNumberVisible);
                lineNumberVisible = lineNumberEnabled && lineNumberVisibleSetting;

                // if this is printing, the drawing of original line numbers must be enabled
                if (component == null)
                    disableLineNumbers = false;

                if (disableLineNumbers)
                    lineNumberVisible = false;
            }

            if (settingName == null || SimpleValueNames.POPUP_MENU_ENABLED.equals(settingName)) {
                popupMenuEnabled = prefs.getBoolean(SimpleValueNames.POPUP_MENU_ENABLED, EditorPreferencesDefaults.defaultPopupMenuEnabled);
            }
            
            BaseDocument doc = getDocument();
            if (doc != null) {

                if (settingName == null || SimpleValueNames.TEXT_LEFT_MARGIN_WIDTH.equals(settingName)) {
                    textLeftMarginWidth = 0; // prefs.getInt(SimpleValueNames.TEXT_LEFT_MARGIN_WIDTH, EditorPreferencesDefaults.defaultTextLeftMarginWidth);
                }

                if (settingName == null || SimpleValueNames.LINE_HEIGHT_CORRECTION.equals(settingName)) {
                    float newLineHeightCorrection = prefs.getFloat(SimpleValueNames.LINE_HEIGHT_CORRECTION, EditorPreferencesDefaults.defaultLineHeightCorrection);
                    if (newLineHeightCorrection != lineHeightCorrection){
                        lineHeightCorrection = newLineHeightCorrection;
                        updateLineHeight(getComponent());
                    }
                }

                if (settingName == null || SimpleValueNames.TEXT_LIMIT_LINE_VISIBLE.equals(settingName)) {
                    textLimitLineVisible = prefs.getBoolean(SimpleValueNames.TEXT_LIMIT_LINE_VISIBLE, EditorPreferencesDefaults.defaultTextLimitLineVisible);
                }

                if (settingName == null || SimpleValueNames.TEXT_LIMIT_WIDTH.equals(settingName)) {
                    textLimitWidth = prefs.getInt(SimpleValueNames.TEXT_LIMIT_WIDTH, EditorPreferencesDefaults.defaultTextLimitWidth);
                }

                // component only properties
                if (component != null) {
                    if (settingName == null || SimpleValueNames.SCROLL_JUMP_INSETS.equals(settingName)) {
                        String value = prefs.get(SimpleValueNames.SCROLL_JUMP_INSETS, null);
                        Insets insets = value != null ? SettingsConversions.parseInsets(value) : null;
                        scrollJumpInsets = insets != null ? insets : EditorPreferencesDefaults.defaultScrollJumpInsets;
                    }

                    if (settingName == null || SimpleValueNames.SCROLL_FIND_INSETS.equals(settingName)) {
                        String value = prefs.get(SimpleValueNames.SCROLL_FIND_INSETS, null);
                        Insets insets = value != null ? SettingsConversions.parseInsets(value) : null;
                        scrollFindInsets = insets != null ? insets : EditorPreferencesDefaults.defaultScrollFindInsets;
                    }

                    Utilities.runInEventDispatchThread(new Runnable() {
                        public @Override void run() {
                            JTextComponent c = component;
                            if (c != null) {
                                updateComponentProperties();
                                ((BaseTextUI)c.getUI()).preferenceChanged(true, true);
                            }
                        }
                    });
                }
            }
        }
    } // End of Listener class
    
    /* package */ Color getTextLimitLineColor() {
        Coloring c = getCMInternal().get(FontColorNames.TEXT_LIMIT_LINE_COLORING);
        if (c != null && c.getForeColor() != null) {
            return c.getForeColor();
        } else {
            return new Color(255, 235, 235);
        }
    }
    
    private void showPopupMenuForPopupTrigger(final MouseEvent evt) {
        if (component != null && evt.isPopupTrigger() && popupMenuEnabled) {
            // Postponing menu creation in order to give other listeners chance
            // to do their job. See IZ #140127 for details.
            SwingUtilities.invokeLater(new Runnable() {
                public @Override void run() {
                    // #205150 - must position the caret before building popup menu
                    if (component != null) {
                            Caret c = component.getCaret();
                            if ((c instanceof BaseCaret) && !Utilities.isSelectionShowing(c)) {
                                int offset = ((BaseCaret)c).mouse2Offset(evt);
                                component.getCaret().setDot(offset);
                            }
                    }
                    showPopupMenu(evt.getX(), evt.getY());
                }
            });
        }
    }
    
    // -----------------------------------------------------------------------
    // MouseListener implementation
    // -----------------------------------------------------------------------
    
    public @Override void mouseClicked(MouseEvent evt) {
    }

    public @Override void mousePressed(MouseEvent evt) {
        getWordMatch().clear();
        showPopupMenuForPopupTrigger(evt);
    }
    
    public @Override void mouseReleased(MouseEvent evt) {
        showPopupMenuForPopupTrigger(evt); // On Win the popup trigger is on mouse release
    }

    public @Override void mouseEntered(MouseEvent evt) {
    }

    public @Override void mouseExited(MouseEvent evt) {
    }
    
    // -----------------------------------------------------------------------
    // Popup menus and tooltip support (moved here from ExtEditorUI)
    // -----------------------------------------------------------------------
    
    private ToolTipSupport toolTipSupport;

    private JPopupMenu popupMenu;

    private PopupManager popupManager;

    public ToolTipSupport getToolTipSupport() {
        if (toolTipSupport == null) {
            toolTipSupport = EditorExtPackageAccessor.get().createToolTipSupport(this);
        }
        return toolTipSupport;
    }
    
    /**
     * @see StickyWindowSupport 
     * @since 4.6
     */
    public StickyWindowSupport getStickyWindowSupport() {
        if(stickyWindowSupport == null) {
            stickyWindowSupport = EditorApiPackageAccessor.get().createStickyWindowSupport(this.getComponent());
        }
        return stickyWindowSupport;
    }

    public PopupManager getPopupManager() {
        if (popupManager == null) {

            synchronized (getComponentLock()) {
                JTextComponent c = getComponent();
                if (c != null) {
                    popupManager = new PopupManager(c);
                }
            }
        }

        return popupManager;
    }
    
    public void showPopupMenu(int x, int y) {
        // First call the build-popup-menu action to possibly rebuild the popup menu
        JTextComponent c = getComponent();
        if (c != null) {
            BaseKit kit = Utilities.getKit(c);
            if (kit != null) {
                Action a = kit.getActionByName(ExtKit.buildPopupMenuAction);
                if (a != null) {
                    a.actionPerformed(new ActionEvent(c, 0, "")); // NOI18N
                }
            }

            JPopupMenu pm = getPopupMenu();
            if (pm != null) {
                if (c.isShowing()) { // fix of #18808
                    if (!c.isFocusOwner()) {
                        c.requestFocus();
                    }
                    pm.show(c, x, y);
                }
            }
        }
    }

    public void hidePopupMenu() {
        JPopupMenu pm = getPopupMenu();
        if (pm != null) {
            pm.setVisible(false);
        }
    }

    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    public void setPopupMenu(JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
    }

    static {
        EditorUiAccessor.register(new Accessor());
    }

    private static final class Accessor extends EditorUiAccessor {

        @Override
        public boolean isLineNumberVisible(EditorUI eui) {
            return eui.lineNumberVisible;
        }

        @Override
        public Coloring getColoring(EditorUI eui, String coloringName) {
            return eui.getColoring(coloringName);
        }

        @Override
        public int getLineNumberMaxDigitCount(EditorUI eui) {
            return eui.lineNumberMaxDigitCount;
        }

        @Override
        public int getLineNumberWidth(EditorUI eui) {
            return eui.lineNumberWidth;
        }

        @Override
        public int getLineNumberDigitWidth(EditorUI eui) {
            return eui.lineNumberDigitWidth;
        }

        @Override
        public Insets getLineNumberMargin(EditorUI eui) {
            return eui.getLineNumberMargin();
        }

        @Override
        public int getLineHeight(EditorUI eui) {
            return eui.getLineHeight();
        }

        @Override
        public Coloring getDefaultColoring(EditorUI eui) {
            return eui.getDefaultColoring();
        }

        @Override
        public int getDefaultSpaceWidth(EditorUI eui) {
            return eui.defaultSpaceWidth;
        }

        @Override
        public Map<?, ?> getRenderingHints(EditorUI eui) {
            return eui.renderingHints;
        }

        @Override
        public Rectangle getExtentBounds(EditorUI eui) {
            return eui.getExtentBounds();
        }

        @Override
        public Insets getTextMargin(EditorUI eui) {
            return eui.getTextMargin();
        }

        @Override
        public int getTextLeftMarginWidth(EditorUI eui) {
            return eui.textLeftMarginWidth;
        }

        @Override
        public boolean getTextLimitLineVisible(EditorUI eui) {
            return eui.textLimitLineVisible;
        }

        @Override
        public Color getTextLimitLineColor(EditorUI eui) {
            return eui.getTextLimitLineColor();
        }

        @Override
        public int getTextLimitWidth(EditorUI eui) {
            return eui.textLimitWidth;
        }

        @Override
        public int getLineAscent(EditorUI eui) {
            return eui.getLineAscent();
        }

        @Override
        public void paint(EditorUI eui, Graphics g) {
            eui.paint(g);
        }

        @Override
        public DrawLayerList getDrawLayerList(EditorUI eui) {
            return eui.drawLayerList;
        }

    } // End of Accessor class
    
}

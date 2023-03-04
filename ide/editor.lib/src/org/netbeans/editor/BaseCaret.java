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

package org.netbeans.editor;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.IntUnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.EventListenerList;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;

import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib.SettingsConversions;
import org.netbeans.modules.editor.lib2.RectangularSelectionTransferHandler;
import org.netbeans.modules.editor.lib2.RectangularSelectionUtils;
import org.netbeans.modules.editor.lib2.view.*;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
* Caret implementation
*
* @author Miloslav Metelka
* @version 1.00
*/

@SuppressWarnings("ClassWithMultipleLoggers")
public class BaseCaret implements Caret,
MouseListener, MouseMotionListener, PropertyChangeListener,
DocumentListener, ActionListener, 
AtomicLockListener, FoldHierarchyListener {

    /** Caret type representing block covering current character */
    public static final String BLOCK_CARET = EditorPreferencesDefaults.BLOCK_CARET; // NOI18N

    /** One dot thin line compatible with Swing default caret */
    public static final String THIN_LINE_CARET = EditorPreferencesDefaults.THIN_LINE_CARET; // NOI18N

    /** @since 1.23 */
    public static final String THICK_LINE_CARET = EditorPreferencesDefaults.THICK_LINE_CARET; // NOI18N

    /** Default caret type */
    public static final String LINE_CARET = "line-caret"; // NOI18N

    /** Boolean property defining whether selection is being rectangular in a particular text component. */
    private static final String RECTANGULAR_SELECTION_PROPERTY = "rectangular-selection"; // NOI18N

    /** List of positions (with even size) defining regions of rectangular selection. Maintained by BaseCaret. */
    private static final String RECTANGULAR_SELECTION_REGIONS_PROPERTY = "rectangular-selection-regions"; // NOI18N

    // -J-Dorg.netbeans.editor.BaseCaret.level=FINEST
    private static final Logger LOG = Logger.getLogger(BaseCaret.class.getName());
    
    // -J-Dorg.netbeans.editor.BaseCaret.EDT.level=FINE - check that setDot() and other operations in EDT only
    private static final Logger LOG_EDT = Logger.getLogger(BaseCaret.class.getName() + ".EDT");

    static {
        // Compatibility debugging flags mapping to logger levels
        if (Boolean.getBoolean("netbeans.debug.editor.caret.focus") && LOG.getLevel().intValue() < Level.FINE.intValue())
            LOG.setLevel(Level.FINE);
        if (Boolean.getBoolean("netbeans.debug.editor.caret.focus.extra") && LOG.getLevel().intValue() < Level.FINER.intValue())
            LOG.setLevel(Level.FINER);
    }

    /**
     * Implementation of various listeners.
     */
    private final ListenerImpl listenerImpl;
    
    /**
     * Present bounds of the caret. This rectangle needs to be repainted
     * prior the caret gets repainted elsewhere.
     */
    private volatile Rectangle caretBounds;

    /** Component this caret is bound to */
    protected JTextComponent component;

    /** Position of the caret on the screen. This helps to compute
    * caret position on the next after jump.
    */
    Point magicCaretPosition;

    /** Position of caret. */
    Position caretPos;

    /** Position of selection mark. */
    Position markPos;

    /** Is the caret visible */
    boolean caretVisible;

    /** Whether blinking caret is currently visible.
     * <code>caretVisible</code> must be also true in order to paint the caret.
     */
    boolean blinkVisible;

    /** Is the selection currently visible? */
    boolean selectionVisible;

    /** Listeners */
    protected EventListenerList listenerList = new EventListenerList();

    /** Timer used for blinking the caret */
    protected Timer flasher;

    /** Type of the caret */
    String type;

    /** Width of thick caret */
    int width;
    
    /** Is the caret italic for italic fonts */
    boolean italic;

    private int xPoints[] = new int[4];
    private int yPoints[] = new int[4];
    private Action selectWordAction;
    private Action selectLineAction;

    /** Change event. Only one instance needed because it has only source property */
    protected ChangeEvent changeEvent;

    /** Dot array of one character under caret */
    protected char dotChar[] = {' '};

    private boolean overwriteMode;

    /** Remembering document on which caret listens avoids
    * duplicate listener addition to SwingPropertyChangeSupport
    * due to the bug 4200280
    */
    private BaseDocument listenDoc;

    /** Font of the text underlying the caret. It can be used
    * in caret painting.
    */
    protected Font afterCaretFont;

    /** Font of the text right before the caret */
    protected Font beforeCaretFont;

    /** Foreground color of the text underlying the caret. It can be used
    * in caret painting.
    */
    protected Color textForeColor;

    /** Background color of the text underlying the caret. It can be used
    * in caret painting.
    */
    protected Color textBackColor;

    /** Whether the text is being modified under atomic lock.
     * If so just one caret change is fired at the end of all modifications.
     */
    private transient boolean inAtomicLock = false;
    private transient boolean inAtomicUnlock = false;
    
    /** Helps to check whether there was modification performed
     * and so the caret change needs to be fired.
     */
    private transient boolean modified;
    
    /** Whether there was an undo done in the modification and the offset of the modification */
    private transient int undoOffset = -1;
    
    static final long serialVersionUID =-9113841520331402768L;

    /**
     * Set to true once the folds have changed. The caret should retain
     * its relative visual position on the screen.
     */
    private boolean updateAfterFoldHierarchyChange;
    
    /**
     * Whether at least one typing change occurred during possibly several atomic operations.
     */
    private boolean typingModificationOccurred;

    private Preferences prefs = null;
    private final PreferenceChangeListener prefsListener = new PreferenceChangeListener() {
        public @Override void preferenceChange(PreferenceChangeEvent evt) {
            String setingName = evt == null ? null : evt.getKey();
            if (setingName == null || SimpleValueNames.CARET_BLINK_RATE.equals(setingName)) {
                SettingsConversions.callSettingsChange(BaseCaret.this);
                int rate = prefs.getInt(SimpleValueNames.CARET_BLINK_RATE, -1);
                if (rate == -1) {
                    JTextComponent c = component;
                    Integer rateI = c == null ? null : (Integer) c.getClientProperty(BaseTextUI.PROP_DEFAULT_CARET_BLINK_RATE);
                    rate = rateI != null ? rateI : EditorPreferencesDefaults.defaultCaretBlinkRate;
                }
                setBlinkRate(rate);
                refresh();
            }
        }
    };
    private PreferenceChangeListener weakPrefsListener = null;
    
    private boolean caretUpdatePending;
    
    private MouseState mouseState = MouseState.DEFAULT;
    
    /**
     * Minimum selection start for word and line selections.
     * This helps to ensure that when extending word (or line) selections
     * the selection will always include at least the initially selected word (or line).
     */
    private int minSelectionStartOffset;
    
    private int minSelectionEndOffset;
    
    private boolean rectangularSelection;
    
    /**
     * Rectangle that corresponds to model2View of current point of selection.
     */
    private Rectangle rsDotRect;

    /**
     * Rectangle that corresponds to model2View of beginning of selection.
     */
    private Rectangle rsMarkRect;
    
    /**
     * Rectangle marking rectangular selection.
     */
    private Rectangle rsPaintRect;
    
    /**
     * List of start-pos and end-pos pairs that denote rectangular selection
     * on the selected lines.
     */
    private List<Position> rsRegions;

    /**
     * Used for showing the default cursor instead of the text cursor when the
     * mouse is over a block of selected text.
     * This field is used to prevent repeated calls to component.setCursor()
     * with the same cursor.
     */
    private boolean showingTextCursor = true;
    
    public BaseCaret() {
        listenerImpl = new ListenerImpl();
    }

    void updateType() {
        JTextComponent c = component;
        if (c != null && prefs != null && !Boolean.TRUE.equals(c.getClientProperty("AsTextField"))) {
            
            String newType;
            int newWidth = 0;
            boolean newItalic;
            Color caretColor = Color.black;
            
            if (overwriteMode) {
                newType = prefs.get(SimpleValueNames.CARET_TYPE_OVERWRITE_MODE, EditorPreferencesDefaults.defaultCaretTypeOverwriteMode);
                newItalic = prefs.getBoolean(SimpleValueNames.CARET_ITALIC_OVERWRITE_MODE, EditorPreferencesDefaults.defaultCaretItalicOverwriteMode);
            } else { // insert mode
                newType = prefs.get(SimpleValueNames.CARET_TYPE_INSERT_MODE, EditorPreferencesDefaults.defaultCaretTypeInsertMode);
                newItalic = prefs.getBoolean(SimpleValueNames.CARET_ITALIC_INSERT_MODE, EditorPreferencesDefaults.defaultCaretItalicInsertMode);
                newWidth = prefs.getInt(SimpleValueNames.THICK_CARET_WIDTH, EditorPreferencesDefaults.defaultThickCaretWidth);
            }

            FontColorSettings fcs = MimeLookup.getLookup(DocumentUtilities.getMimeType(c)).lookup(FontColorSettings.class);
            if (fcs != null) {
                if (overwriteMode) {
                    AttributeSet attribs = fcs.getFontColors(FontColorNames.CARET_COLOR_OVERWRITE_MODE); //NOI18N
                    if (attribs != null) {
                        caretColor = (Color) attribs.getAttribute(StyleConstants.Foreground);
                    }
                } else {
                    AttributeSet attribs = fcs.getFontColors(FontColorNames.CARET_COLOR_INSERT_MODE); //NOI18N
                    if (attribs != null) {
                        caretColor = (Color) attribs.getAttribute(StyleConstants.Foreground);
                    }
                }
            }
            
            this.type = newType;
            this.italic = newItalic;
            this.width = newWidth;
            c.setCaretColor(caretColor);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Updating caret color:" + caretColor + '\n'); // NOI18N
            }

            resetBlink();
            dispatchUpdate(false);
        }
    }

    /**
     * Assign new caret bounds into <code>caretBounds</code> variable.
     *
     * @return true if the new caret bounds were successfully computed
     *  and assigned or false otherwise.
     */
    private boolean updateCaretBounds() {
        final JTextComponent c = component;
        final boolean[] ret = { false };
        if (c != null) {
            final Document doc = c.getDocument();
            doc.render(new Runnable() {
                @Override
                public void run() {
                    int offset = getDot();
                    if (offset > doc.getLength()) {
                        offset = doc.getLength();
                    }
                    if (doc != null) {
                        CharSequence docText = DocumentUtilities.getText(doc);
                        dotChar[0] = docText.charAt(offset);
                    }
                    Rectangle newCaretBounds;
                    try {
                        DocumentView docView = DocumentView.get(c);
                        if (docView != null) {
                            // docView.syncViewsRebuild(); // Make sure pending views changes are resolved
                        }
                        newCaretBounds = c.getUI().modelToView(
                                c, offset, Position.Bias.Forward);
                        // [TODO] Temporary fix - impl should remember real bounds computed by paintCustomCaret()
                        if (newCaretBounds != null) {
                            int minwidth = 2;
                            // [NETBEANS-4940] Caret drawing problems over a TAB
                            Object o = component.getClientProperty("CARET_MIN_WIDTH");
                            if(o instanceof IntUnaryOperator) {
                                minwidth = ((IntUnaryOperator)o).applyAsInt(offset);
                            }
                            newCaretBounds.width = Math.max(newCaretBounds.width, minwidth);
                        }
                    } catch (BadLocationException e) {
                        newCaretBounds = null;
                        Utilities.annotateLoggable(e);
                    }
                    if (newCaretBounds != null) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "updateCaretBounds: old={0}, new={1}, offset={2}",
                                    new Object[]{caretBounds, newCaretBounds, offset}); //NOI18N
                        }
                        caretBounds = newCaretBounds;
                        ret[0] = true;
                    }
                }
            });
        }
        LOG.log(Level.FINE, "updateCaretBounds: no change, old={0}", caretBounds); //NOI18N
        return ret[0];
    }

    /** Called when UI is being installed into JTextComponent */
    public @Override void install(JTextComponent c) {
        assert (SwingUtilities.isEventDispatchThread()); // must be done in AWT
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Installing to " + s2s(c)); //NOI18N
        }
        
        component = c;
        blinkVisible = true;
        
        // Assign dot and mark positions
        BaseDocument doc = Utilities.getDocument(c);
        if (doc != null) {
            modelChanged(null, doc);
        }

        // Attempt to assign initial bounds - usually here the component
        // is not yet added to the component hierarchy.
        updateCaretBounds();
        
        if (caretBounds == null) {
            // For null bounds wait for the component to get resized
            // and attempt to recompute bounds then
            component.addComponentListener(listenerImpl);
        }

        component.addPropertyChangeListener(this);
        component.addFocusListener(listenerImpl);
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
        ViewHierarchy.get(component).addViewHierarchyListener(listenerImpl);

        EditorUI editorUI = Utilities.getEditorUI(component);
        editorUI.addPropertyChangeListener( this );
        
        if (component.hasFocus()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Component has focus, calling BaseCaret.focusGained(); doc=" // NOI18N
                    + component.getDocument().getProperty(Document.TitleProperty) + '\n');
            }
            listenerImpl.focusGained(null); // emulate focus gained
        }

        dispatchUpdate(false);
    }

    /** Called when UI is being removed from JTextComponent */
    @Override
    @SuppressWarnings("NestedSynchronizedStatement")
    public void deinstall(JTextComponent c) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Deinstalling from " + s2s(c)); //NOI18N
        }
        
        component = null; // invalidate
        caretBounds = null;

        // No idea why the sync is done the way how it is, but the locks must
        // always be acquired in the same order otherwise the code will deadlock
        // sooner or later. See #100734
        synchronized (this) {
            synchronized (listenerImpl) {
                if (flasher != null) {
                    setBlinkRate(0);
                }
            }
        }
        
        c.removeComponentListener(listenerImpl);
        c.removePropertyChangeListener(this);
        c.removeFocusListener(listenerImpl);
        c.removeMouseListener(this);
        c.removeMouseMotionListener(this);
        ViewHierarchy.get(c).removeViewHierarchyListener(listenerImpl);

        
        EditorUI editorUI = Utilities.getEditorUI(c);
        editorUI.removePropertyChangeListener(this);

        modelChanged(listenDoc, null);
    }

    protected void modelChanged(BaseDocument oldDoc, BaseDocument newDoc) {
        if (oldDoc != null) {
            // ideally the oldDoc param shouldn't exist and only listenDoc should be used
            assert (oldDoc == listenDoc);

            DocumentUtilities.removeDocumentListener(
                    oldDoc, this, DocumentListenerPriority.CARET_UPDATE);
            oldDoc.removeAtomicLockListener(this);

            caretPos = null;
            markPos = null;

            listenDoc = null;
            if (prefs != null && weakPrefsListener != null) {
                prefs.removePreferenceChangeListener(weakPrefsListener);
            }
        }


        if (newDoc != null) {

            DocumentUtilities.addDocumentListener(
                    newDoc, this, DocumentListenerPriority.CARET_UPDATE);
            listenDoc = newDoc;
            newDoc.addAtomicLockListener(this);

            // Leave caretPos and markPos null => offset==0
            prefs = MimeLookup.getLookup(DocumentUtilities.getMimeType(newDoc)).lookup(Preferences.class);
            if (prefs != null) {
                weakPrefsListener = WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs);
                prefs.addPreferenceChangeListener(weakPrefsListener);
            }
            
            Utilities.runInEventDispatchThread(
                new Runnable() {
                    public @Override void run() {
                        updateType();
                    }
                }
            );
        }
    }

    /** Renders the caret */
    public @Override void paint(Graphics g) {
        JTextComponent c = component;
        if (c == null) return;

        // #70915 Check whether the caret was moved but the component was not
        // validated yet and therefore the caret bounds are still null
        // and if so compute the bounds and scroll the view if necessary.
        if (getDot() != 0 && caretBounds == null) {
            update(true);
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("BaseCaret.paint(): caretBounds=" + caretBounds + dumpVisibility() + '\n');
        }
        if (caretBounds != null && isVisible() && blinkVisible) {
            paintCustomCaret(g);
        }
        if (rectangularSelection && rsPaintRect != null && g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {4, 2}, 0);
            Stroke origStroke = g2d.getStroke();
            Color origColor = g2d.getColor();
            try {
                // Render translucent rectangle
                Color selColor = c.getSelectionColor();
                g2d.setColor(selColor);
                Composite origComposite = g2d.getComposite();
                try {
                    g2d.setComposite(AlphaComposite.SrcOver.derive(0.2f));
                    g2d.fill(rsPaintRect);
                } finally {
                    g2d.setComposite(origComposite);
                }
                // Paint stroked line around rectangular selection rectangle
                g.setColor(c.getCaretColor());
                g2d.setStroke(stroke);
                Rectangle onePointSmallerRect = new Rectangle(rsPaintRect);
                onePointSmallerRect.width--;
                onePointSmallerRect.height--;
                g2d.draw(onePointSmallerRect);

            } finally {
                g2d.setStroke(origStroke);
                g2d.setColor(origColor);
            }
        }
    }

    protected void paintCustomCaret(Graphics g) {
        JTextComponent c = component;
        if (c != null) {
            EditorUI editorUI = Utilities.getEditorUI(c);
            g.setColor(c.getCaretColor());
            if (THIN_LINE_CARET.equals(type)) { // thin line caret
                int upperX = caretBounds.x;
                if (beforeCaretFont != null && beforeCaretFont.isItalic() && italic) {
                    upperX += Math.tan(beforeCaretFont.getItalicAngle()) * caretBounds.height;
                }
                g.drawLine((int)upperX, caretBounds.y, caretBounds.x,
                        (caretBounds.y + caretBounds.height - 1));
            } else if (THICK_LINE_CARET.equals(type)) { // thick caret
                int blkWidth = this.width;
                if (blkWidth <= 0) blkWidth = 5; // sanity check
                if (afterCaretFont != null) g.setFont(afterCaretFont);
                Color textBackgroundColor = c.getBackground();
                if (textBackgroundColor != null) {
                    g.setXORMode( textBackgroundColor);
                }
                g.fillRect(caretBounds.x, caretBounds.y, blkWidth, caretBounds.height - 1);
            } else if (BLOCK_CARET.equals(type)) { // block caret
                if (afterCaretFont != null) g.setFont(afterCaretFont);
                if (afterCaretFont != null && afterCaretFont.isItalic() && italic) { // paint italic caret
                    int upperX = (int)(caretBounds.x
                            + Math.tan(afterCaretFont.getItalicAngle()) * caretBounds.height);
                    xPoints[0] = upperX;
                    yPoints[0] = caretBounds.y;
                    xPoints[1] = upperX + caretBounds.width;
                    yPoints[1] = caretBounds.y;
                    xPoints[2] = caretBounds.x + caretBounds.width;
                    yPoints[2] = caretBounds.y + caretBounds.height - 1;
                    xPoints[3] = caretBounds.x;
                    yPoints[3] = caretBounds.y + caretBounds.height - 1;
                    g.fillPolygon(xPoints, yPoints, 4);

                } else { // paint non-italic caret
                    g.fillRect(caretBounds.x, caretBounds.y, caretBounds.width, caretBounds.height);
                }
                
                if (!Character.isWhitespace(dotChar[0])) {
                    Color textBackgroundColor = c.getBackground();
                    if (textBackgroundColor != null)
                        g.setColor(textBackgroundColor);
                    // int ascent = FontMetricsCache.getFontMetrics(afterCaretFont, c).getAscent();
                    g.drawChars(dotChar, 0, 1, caretBounds.x,
                            caretBounds.y + editorUI.getLineAscent());
                }

            } else { // two dot line caret
                int blkWidth = 2;
                if (beforeCaretFont != null && beforeCaretFont.isItalic() && italic) {
                    int upperX = (int)(caretBounds.x 
                            + Math.tan(beforeCaretFont.getItalicAngle()) * caretBounds.height);
                    xPoints[0] = upperX;
                    yPoints[0] = caretBounds.y;
                    xPoints[1] = upperX + blkWidth;
                    yPoints[1] = caretBounds.y;
                    xPoints[2] = caretBounds.x + blkWidth;
                    yPoints[2] = caretBounds.y + caretBounds.height - 1;
                    xPoints[3] = caretBounds.x;
                    yPoints[3] = caretBounds.y + caretBounds.height - 1;
                    g.fillPolygon(xPoints, yPoints, 4);

                } else { // paint non-italic caret
                    g.fillRect(caretBounds.x, caretBounds.y, blkWidth, caretBounds.height - 1);
                }
            }
        }
    }

    /** Update the caret's visual position */
    void dispatchUpdate(final boolean scrollViewToCaret) {
        /* After using SwingUtilities.invokeLater() due to fix of #18860
         * there is another fix of #35034 which ensures that the caret's
         * document listener will be added AFTER the views hierarchy's
         * document listener so the code can run synchronously again
         * which should eliminate the problem with caret lag.
         * However the document can be modified from non-AWT thread
         * which is the case in #57316 and in that case the code
         * must run asynchronously in AWT thread.
         */
        Utilities.runInEventDispatchThread(
            new Runnable() {
                public @Override void run() {
                    JTextComponent c = component;
                    if (c != null) {
                        BaseDocument doc = Utilities.getDocument(c);
                        if (doc != null) {
                            doc.readLock();
                            try {
                                update(scrollViewToCaret);
                            } finally {
                                doc.readUnlock();
                            }
                        }
                    }
                }
            }
        );
    }

    /**
     * Update the caret's visual position.
     * <br/>
     * The document is read-locked while calling this method.
     *
     * @param scrollViewToCaret whether the view of the text component should be
     *  scrolled to the position of the caret.
     */
    protected void update(boolean scrollViewToCaret) {
        caretUpdatePending = false;
        JTextComponent c = component;
        if (c != null) {
            if (!c.isValid()) {
                c.validate();
            }
            BaseTextUI ui = (BaseTextUI)c.getUI();
            BaseDocument doc = Utilities.getDocument(c);
            if (doc != null) {
                Rectangle oldCaretBounds = caretBounds; // no need to deep copy
                if (oldCaretBounds != null) {
                    if (italic) { // caret is italic - add char height to the width of the rect
                        oldCaretBounds.width += oldCaretBounds.height;
                    }
                    c.repaint(oldCaretBounds);
                }

                // note - the order is important ! caret bounds must be updated even if the fold flag is true.
                if (updateCaretBounds() || updateAfterFoldHierarchyChange) {
                    Rectangle scrollBounds = new Rectangle(caretBounds);
                    
                    // Optimization to avoid extra repaint:
                    // If the caret bounds were not yet assigned then attempt
                    // to scroll the window so that there is an extra vertical space 
                    // for the possible horizontal scrollbar that may appear
                    // if the line-view creation process finds line-view that
                    // is too wide and so the horizontal scrollbar will appear
                    // consuming an extra vertical space at the bottom.
                    if (oldCaretBounds == null) {
                        Component viewport = c.getParent();
                        if (viewport instanceof JViewport) {
                            Component scrollPane = viewport.getParent();
                            if (scrollPane instanceof JScrollPane) {
                                JScrollBar hScrollBar = ((JScrollPane)scrollPane).getHorizontalScrollBar();
                                if (hScrollBar != null) {
                                    int hScrollBarHeight = hScrollBar.getPreferredSize().height;
                                    Dimension extentSize = ((JViewport)viewport).getExtentSize();
                                    // If the extent size is high enough then extend
                                    // the scroll region by extra vertical space
                                    if (extentSize.height >= caretBounds.height + hScrollBarHeight) {
                                        scrollBounds.height += hScrollBarHeight;
                                    }
                                }
                            }
                        }
                    }
                    
                    Rectangle visibleBounds = c.getVisibleRect();
                    
                    // If folds have changed attempt to scroll the view so that 
                    // relative caret's visual position gets retained
                    // (the absolute position will change because of collapsed/expanded folds).
                    boolean doScroll = scrollViewToCaret;
                    boolean explicit = false;
                    if (oldCaretBounds != null && (!scrollViewToCaret || updateAfterFoldHierarchyChange)) {
                        int oldRelY = oldCaretBounds.y - visibleBounds.y;
                        // Only fix if the caret is within visible bounds and the new x or y coord differs from the old one
                        if (LOG.isLoggable(Level.FINER)) {
                            LOG.log(Level.FINER, "oldCaretBounds: {0}, visibleBounds: {1}, caretBounds: {2}",
                                    new Object[] { oldCaretBounds, visibleBounds, caretBounds });
                        }
                        if (oldRelY >= 0 && oldRelY < visibleBounds.height &&
                                (oldCaretBounds.y != caretBounds.y || oldCaretBounds.x != caretBounds.x))
                        {
                            doScroll = true; // Perform explicit scrolling
                            explicit = true;
                            int oldRelX = oldCaretBounds.x - visibleBounds.x;
                            // Do not retain the horizontal caret bounds by scrolling
                            // since many modifications do not explicitly say that they are typing modifications
                            // and this would cause problems like #176268
//                            scrollBounds.x = Math.max(caretBounds.x - oldRelX, 0);
                            scrollBounds.y = Math.max(caretBounds.y - oldRelY, 0);
//                            scrollBounds.width = visibleBounds.width;
                            scrollBounds.height = visibleBounds.height;
                        }
                    }

                    // Historically the caret is expected to appear
                    // in the middle of the window if setDot() gets called
                    // e.g. by double-clicking in Navigator.
                    // If the caret bounds are more than a caret height below the present
                    // visible view bounds (or above the view bounds)
                    // then scroll the window so that the caret is in the middle
                    // of the visible window to see the context around the caret.
                    // This should work fine with PgUp/Down because these
                    // scroll the view explicitly.
                    if (scrollViewToCaret && 
                        !explicit && // #219580: if the preceding if-block computed new scrollBounds, it cannot be offset yet more
                        /* # 70915 !updateAfterFoldHierarchyChange && */
                        (caretBounds.y > visibleBounds.y + visibleBounds.height + caretBounds.height
                            || caretBounds.y + caretBounds.height < visibleBounds.y - caretBounds.height)
                    ) {
                        // Scroll into the middle
                        scrollBounds.y -= (visibleBounds.height - caretBounds.height) / 2;
                        scrollBounds.height = visibleBounds.height;
                    }
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("Resetting fold flag, current: " + updateAfterFoldHierarchyChange);
                    }
                    updateAfterFoldHierarchyChange = false;
                    
                    // Ensure that the viewport will be scrolled either to make the caret visible
                    // or to retain cart's relative visual position against the begining of the viewport's visible rectangle.
                    if (doScroll) {
                        if (LOG.isLoggable(Level.FINER)) {
                            LOG.finer("Scrolling to: " + scrollBounds);
                        }
                        c.scrollRectToVisible(scrollBounds);
                        if (!c.getVisibleRect().intersects(scrollBounds)) {
                            // HACK: see #219580: for some reason, the scrollRectToVisible may fail.
                            c.scrollRectToVisible(scrollBounds);
                        }
                    }
                    resetBlink();
                    c.repaint(caretBounds);
                }
            }
        }
    }

    private void updateSystemSelection() {
        if (getDot() != getMark() && component != null) {
            Clipboard clip = getSystemSelection();
            
            if (clip != null) {
                clip.setContents(new java.awt.datatransfer.StringSelection(component.getSelectedText()), null);
            }
        }
    }

    private Clipboard getSystemSelection() {
        return component.getToolkit().getSystemSelection();
    }
    
    private void updateRectangularSelectionPositionBlocks() {
        JTextComponent c = component;
        if (rectangularSelection) {
            if (listenDoc != null) {
                listenDoc.readLock();
                try {
                    if (rsRegions == null) {
                        rsRegions = new ArrayList<Position>();
                        c.putClientProperty(RECTANGULAR_SELECTION_REGIONS_PROPERTY, rsRegions);
                    }
                    synchronized (rsRegions) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Rectangular-selection position regions:\n");
                        }
                        rsRegions.clear();
                        if (rsPaintRect != null) {
                            LockedViewHierarchy lvh = ViewHierarchy.get(c).lock();
                            try {
                                float rowHeight = lvh.getDefaultRowHeight();
                                double y = rsPaintRect.y;
                                double maxY = y + rsPaintRect.height;
                                double minX = rsPaintRect.getMinX();
                                double maxX = rsPaintRect.getMaxX();
                                do {
                                    int startOffset = lvh.viewToModel(minX, y, null);
                                    int endOffset = lvh.viewToModel(maxX, y, null);
                                    // They could be swapped due to RTL text
                                    if (startOffset > endOffset) {
                                        int tmp = startOffset; startOffset = endOffset; endOffset = tmp;
                                    }
                                    Position startPos = listenDoc.createPosition(startOffset);
                                    Position endPos = listenDoc.createPosition(endOffset);
                                    rsRegions.add(startPos);
                                    rsRegions.add(endPos);
                                    if (LOG.isLoggable(Level.FINE)) {
                                        LOG.fine("  <" + startOffset + "," + endOffset + ">\n");
                                    }
                                    y += rowHeight;
                                } while (y < maxY);
                                c.putClientProperty(RECTANGULAR_SELECTION_REGIONS_PROPERTY, rsRegions);
                            } finally {
                                lvh.unlock();
                            }
                        }
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    listenDoc.readUnlock();
                }
            }
        }
    }

    /**
     * Redefine to Object.equals() to prevent defaulting to Rectangle.equals()
     * which would cause incorrect firing
     */
    @Override@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {
        return (this == o);
    }

    public @Override int hashCode() {
        return System.identityHashCode(this);
    }
    
    /** Adds listener to track when caret position was changed */
    public @Override void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    /** Removes listeners to caret position changes */
    public @Override void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    /** Notifies listeners that caret position has changed */
    protected void fireStateChanged() {
        Runnable runnable = new Runnable() {
            public @Override void run() {
                // #252441: uninstallUI might detached the caret from component while this
                // event was queued.
                if (component == null || component.getCaret() != BaseCaret.this) {
                    return;
                }
                Object listeners[] = listenerList.getListenerList();
                for (int i = listeners.length - 2; i >= 0 ; i -= 2) {
                    if (listeners[i] == ChangeListener.class) {
                        if (changeEvent == null) {
                            changeEvent = new ChangeEvent(BaseCaret.this);
                        }
                        ((ChangeListener)listeners[i + 1]).stateChanged(changeEvent);
                    }
                }
            }
        };
        
        // Fix of #24336 - always do in AWT thread
        // Fix of #114649 - when under document's lock repost asynchronously
        if (inAtomicUnlock) {
            SwingUtilities.invokeLater(runnable);
        } else {
            Utilities.runInEventDispatchThread(runnable);
        }
        updateSystemSelection();
    }

    /**
     * Whether the caret currently visible.
     * <br>
     * Although the caret is visible it may be in a state when it's
     * not physically showing on screen in case when it's blinking.
     */
    public final @Override boolean isVisible() {
        return caretVisible;
    }

    protected void setVisibleImpl(boolean v) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("BaseCaret.setVisible(" + v + ")\n");
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.INFO, "", new Exception());
            }
        }
        boolean visible = isVisible();
        synchronized (this) {
            synchronized (listenerImpl) {
                if (flasher != null) {
                    if (visible) {
                        flasher.stop();
                    }
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer((v ? "Starting" : "Stopping") + // NOI18N
                                " the caret blinking timer: " + dumpVisibility() + '\n'); // NOI18N
                    }
                    if (v) {
                        flasher.start();
                    } else {
                        flasher.stop();
                    }
                }
            }

            caretVisible = v;
        }
        JTextComponent c = component;
        if (c != null && caretBounds != null) {
            Rectangle repaintRect = caretBounds;
            if (italic) {
                repaintRect = new Rectangle(repaintRect); // copy
                repaintRect.width += repaintRect.height; // ensure enough horizontally
            }
            c.repaint(repaintRect);
        }
    }

    private String dumpVisibility() {
        return "visible=" + isVisible() + ", blinkVisible=" + blinkVisible;
    }

    @SuppressWarnings("NestedSynchronizedStatement")
    void resetBlink() {
        synchronized (this) {
            boolean visible = isVisible();
            synchronized (listenerImpl) {
                if (flasher != null) {
                    flasher.stop();
                    blinkVisible = true;
                    if (visible) {
                        if (LOG.isLoggable(Level.FINER)){
                            LOG.finer("Reset blinking (caret already visible)" + // NOI18N
                                    " - starting the caret blinking timer: " + dumpVisibility() + '\n'); // NOI18N
                        }
                        flasher.start();
                    } else {
                        if (LOG.isLoggable(Level.FINER)){
                            LOG.finer("Reset blinking (caret not visible)" + // NOI18N
                                    " - caret blinking timer not started: " + dumpVisibility() + '\n'); // NOI18N
                        }
                    }
                }
            }
        }
    }

    /** Sets the caret visibility */
    public @Override void setVisible(final boolean v) {
        Utilities.runInEventDispatchThread(
            new Runnable() {
                public @Override void run() {
                    setVisibleImpl(v);
                }
            }
        );
    }

    /** Is the selection visible? */
    public final @Override boolean isSelectionVisible() {
        return selectionVisible;
    }

    /** Sets the selection visibility */
    public @Override void setSelectionVisible(boolean v) {
        if (selectionVisible == v) {
            return;
        }
        JTextComponent c = component;
        Document doc;
        if (c != null && (doc = c.getDocument()) != null) {
            selectionVisible = v;

            // repaint the block
            final BaseTextUI ui = (BaseTextUI)c.getUI();
            doc.render(new Runnable() {
                @Override
                public void run() {
                    try {
                        ui.getEditorUI().repaintBlock(getDot(), getMark());
                    } catch (BadLocationException e) {
                        Utilities.annotateLoggable(e);
                    }
                }
            });
        }
    }

    /** Saves the current caret position.  This is used when
    * caret up or down actions occur, moving between lines
    * that have uneven end positions.
    *
    * @param p  the Point to use for the saved position
    */
    public @Override void setMagicCaretPosition(Point p) {
        magicCaretPosition = p;
    }

    /** Get position used to mark begining of the selected block */
    public @Override final Point getMagicCaretPosition() {
        return magicCaretPosition;
    }

    /** Sets the caret blink rate.
    * @param rate blink rate in milliseconds, 0 means no blink
    */
    public @Override synchronized void setBlinkRate(int rate) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("setBlinkRate(" + rate + ")" + dumpVisibility() + '\n'); // NOI18N
        }
        synchronized (listenerImpl) {
            if (flasher == null && rate > 0) {
                flasher = new Timer(rate, new WeakTimerListener(this));
            }
            if (flasher != null) {
                if (rate > 0) {
                    if (flasher.getDelay() != rate) {
                        flasher.setDelay(rate);
                    }
                } else { // zero rate - don't blink
                    flasher.stop();
                    flasher.removeActionListener(this);
                    flasher = null;
                    blinkVisible = true;
                    if (LOG.isLoggable(Level.FINER)){
                        LOG.finer("Zero blink rate - no blinking. flasher=null; blinkVisible=true"); // NOI18N
                    }
                }
            }
        }
    }

    /** Returns blink rate of the caret or 0 if caret doesn't blink */
    @Override
    @SuppressWarnings("NestedSynchronizedStatement")
    public int getBlinkRate() {
        synchronized (this) {
            synchronized (listenerImpl) {
                return (flasher != null) ? flasher.getDelay() : 0;
            }
        }
    }

    /** Gets the current position of the caret */
    public @Override int getDot() {
        if (component != null) {
            return (caretPos != null) ? caretPos.getOffset() : 0;
        }
        return 0;
    }

    /** Gets the current position of the selection mark.
    * If there's a selection this position will be different
    * from the caret position.
    */
    public @Override int getMark() {
        if (component != null) {
            return (markPos != null) ? markPos.getOffset() : 0;
        }
        return 0;
    }

    /**
     * Assign the caret a new offset in the underlying document.
     * <br/>
     * This method implicitly sets the selection range to zero.
     */
    public @Override void setDot(int offset) {
        // The first call to this method in NB is done when the component
        // is already connected to the component hierarchy but its size
        // is still (0,0,0,0) (although its preferred size is already non-empty).
        // This causes the TextUI.modelToView() to return null
        // because BasicTextUI.getVisibleEditorRect() returns null.
        // Thus caretBounds will be null in such case although
        // the offset in setDot() is already non-zero.
        // In such case the component listener listens for resizing
        // of the editor component and reassigns the caretBounds
        // once the component gets resized.
        setDot(offset, caretBounds, EditorUI.SCROLL_DEFAULT);
    }

    public void setDot(int offset, boolean expandFold) {
        setDot(offset, caretBounds, EditorUI.SCROLL_DEFAULT, expandFold);
    }
    
    
    /** Sets the caret position to some position. This
     * causes removal of the active selection. If expandFold set to true
     * fold containing offset position will be expanded.
     *
     * <p>
     * <b>Note:</b> This method is deprecated and the present implementation
     * ignores values of scrollRect and scrollPolicy parameters.
     *
     * @param offset offset in the document to which the caret should be positioned.
     * @param scrollRect rectangle to which the editor window should be scrolled.
     * @param scrollPolicy the way how scrolling should be done.
     *  One of <code>EditorUI.SCROLL_*</code> constants.
     * @param expandFold whether possible fold at the caret position should be expanded.
     *
     * @deprecated use #setDot(int, boolean) preceded by <code>JComponent.scrollRectToVisible()</code>.
     */
    @Deprecated
    public void setDot(int offset, Rectangle scrollRect, int scrollPolicy, boolean expandFold) {
        if (LOG_EDT.isLoggable(Level.FINE)) { // Only permit operations in EDT
            if (!SwingUtilities.isEventDispatchThread()) {
                throw new IllegalStateException("BaseCaret.setDot() not in EDT: offset=" + offset); // NOI18N
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("setDot: offset=" + offset); //NOI18N
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.INFO, "setDot call stack", new Exception());
            }
        }
        
        JTextComponent c = component;
        if (c != null) {
            BaseDocument doc = (BaseDocument)c.getDocument();
            boolean dotChanged = false;
            doc.readLock();
            try {
                if (doc != null && offset >= 0 && offset <= doc.getLength()) {
                    dotChanged = true;
                    try {
                        caretPos = doc.createPosition(offset);
                        markPos = doc.createPosition(offset);

                        Callable<Boolean> cc = (Callable<Boolean>)c.getClientProperty("org.netbeans.api.fold.expander");
                        if (cc != null && expandFold) {
                            // the caretPos/markPos were already called.
                            // nothing except the document is locked at this moment.
                            try {
                                cc.call();
                            } catch (Exception ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        if (rectangularSelection) {
                            setRectangularSelectionToDotAndMark();
                        }
                        
                    } catch (BadLocationException e) {
                        throw new IllegalStateException(e.toString());
                        // setting the caret to wrong position leaves it at current position
                    }
                }
            } finally {
                doc.readUnlock();
            }
            
            if (dotChanged) {
                fireStateChanged();
                dispatchUpdate(true);
            }
        }
    }
    
    /** Sets the caret position to some position. This
     * causes removal of the active selection.
     *
     * <p>
     * <b>Note:</b> This method is deprecated and the present implementation
     * ignores values of scrollRect and scrollPolicy parameters.
     *
     * @param offset offset in the document to which the caret should be positioned.
     * @param scrollRect rectangle to which the editor window should be scrolled.
     * @param scrollPolicy the way how scrolling should be done.
     *  One of <code>EditorUI.SCROLL_*</code> constants.
     *
     * @deprecated use #setDot(int) preceded by <code>JComponent.scrollRectToVisible()</code>.
     */
    @Deprecated
    public void setDot(int offset, Rectangle scrollRect, int scrollPolicy) {
        setDot(offset, scrollRect, scrollPolicy, true);
    }

    public @Override void moveDot(int offset) {
        moveDot(offset, caretBounds, EditorUI.SCROLL_MOVE);
    }

    /** Makes selection by moving dot but leaving mark.
     * 
     * <p>
     * <b>Note:</b> This method is deprecated and the present implementation
     * ignores values of scrollRect and scrollPolicy parameters.
     *
     * @param offset offset in the document to which the caret should be positioned.
     * @param scrollRect rectangle to which the editor window should be scrolled.
     * @param scrollPolicy the way how scrolling should be done.
     *  One of <code>EditorUI.SCROLL_*</code> constants.
     *
     * @deprecated use #setDot(int) preceded by <code>JComponent.scrollRectToVisible()</code>.
     */
    @Deprecated
    public void moveDot(int offset, Rectangle scrollRect, int scrollPolicy) {
        if (LOG_EDT.isLoggable(Level.FINE)) { // Only permit operations in EDT
            if (!SwingUtilities.isEventDispatchThread()) {
                throw new IllegalStateException("BaseCaret.moveDot() not in EDT: offset=" + offset); // NOI18N
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("moveDot: offset=" + offset); //NOI18N
        }

        JTextComponent c = component;
        if (c != null) {
            BaseDocument doc = (BaseDocument)c.getDocument();
            if (doc != null && offset >= 0 && offset <= doc.getLength()) {
                doc.readLock();
                try {
                    int oldCaretPos = getDot();
                    if (offset == oldCaretPos) { // no change
                        return;
                    }
                    caretPos = doc.createPosition(offset);
                    if (selectionVisible) { // selection already visible
                        Utilities.getEditorUI(c).repaintBlock(oldCaretPos, offset);
                    }
                    if (rectangularSelection) {
                        Rectangle r = c.modelToView(offset);
                        if (rsDotRect != null) {
                            rsDotRect.y = r.y;
                            rsDotRect.height = r.height;
                        } else {
                            rsDotRect = r;
                        }
                        updateRectangularSelectionPaintRect();
                    }
                } catch (BadLocationException e) {
                    throw new IllegalStateException(e.toString());
                    // position is incorrect
                } finally {
                    doc.readUnlock();
                }
            }
            fireStateChanged();
            dispatchUpdate(true);
        }
    }

    // DocumentListener methods
    public @Override void insertUpdate(DocumentEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            int offset = evt.getOffset();
            int endOffset = offset + evt.getLength();
            if (evt.getOffset() == 0) {
                // Insert at offset 0 the marks would stay at offset == 0
                if (getMark() == 0) {
                    try {
                        markPos = listenDoc.createPosition(endOffset);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                if (getDot() == 0) {
                    try {
                        caretPos = listenDoc.createPosition(endOffset);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            boolean typingModification;
            if ((bevt.isInUndo() || bevt.isInRedo())
                    && component == Utilities.getLastActiveComponent()
                    && !Boolean.TRUE.equals(DocumentUtilities.getEventProperty(evt, "caretIgnore"))
               ) {
                // in undo mode and current component
                undoOffset = evt.getOffset() + evt.getLength();
                // Undo operations now cause the caret to move to the place where the undo occurs.
                // In future we should put additional info into the document event whether it was
                // a typing modification and if not the caret should not be relocated and scrolled.
                typingModification = true;
            } else {
                undoOffset = -1;
                typingModification = DocumentUtilities.isTypingModification(component.getDocument());
            }

            modified = true;

            modifiedUpdate(typingModification);
        }
    }

    public @Override void removeUpdate(DocumentEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            // make selection invisible if removal shrinked block to zero size
            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            boolean typingModification;
            if ((bevt.isInUndo() || bevt.isInRedo())
                && c == Utilities.getLastActiveComponent()
                && !Boolean.TRUE.equals(DocumentUtilities.getEventProperty(evt, "caretIgnore"))
            ) {
                // in undo mode and current component
                undoOffset = evt.getOffset();
                // Undo operations now cause the caret to move to the place where the undo occurs.
                // In future we should put additional info into the document event whether it was
                // a typing modification and if not the caret should not be relocated and scrolled.
                typingModification = true;
            } else { // Not undo or redo
                undoOffset = -1;
                typingModification = DocumentUtilities.isTypingModification(component.getDocument());

            }

            modified = true;
            
            modifiedUpdate(typingModification);
        }
    }
    
    private void modifiedUpdate(boolean typingModification) {
        if (!inAtomicLock) {
            JTextComponent c = component;
            if (modified && c != null) {
                if (undoOffset >= 0) { // last modification was undo => set the dot to undoOffset
                    setDot(undoOffset);
                } else { // last modification was not undo
                    BaseDocument doc = listenDoc;
                    if (doc != null) {
                        doc.readLock();
                        try {
                            updateRectangularSelectionPaintRect();
                        } finally {
                            doc.readUnlock();
                        }
                    }
                    fireStateChanged();
                    // Scroll to caret only for component with focus
                    dispatchUpdate(c.hasFocus() && typingModification);
                }
                modified = false;
            }
        } else {
            typingModificationOccurred |= typingModification;
        }
    }
    
    public @Override void atomicLock(AtomicLockEvent evt) {
        inAtomicLock = true;
    }
    
    public @Override void atomicUnlock(AtomicLockEvent evt) {
        inAtomicLock = false;
        inAtomicUnlock = true;
        try {
            modifiedUpdate(typingModificationOccurred);
        } finally {
            inAtomicUnlock = false;
            typingModificationOccurred = false;
        }
    }
    
    public @Override void changedUpdate(DocumentEvent evt) {
        // XXX: used as a backdoor from HighlightingDrawLayer
        if (evt == null) {
            dispatchUpdate(false);
        }
    }

    // MouseListener methods
    @Override
    public void mousePressed(MouseEvent evt) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("mousePressed: " + logMouseEvent(evt) + ", state=" + mouseState + '\n'); // NOI18N
        }

        JTextComponent c = component;
        if (c != null && isLeftMouseButtonExt(evt)) {
            // Expand fold if offset is in collapsed fold
            int offset = mouse2Offset(evt);
            switch (evt.getClickCount()) {
                case 1: // Single press
                    if (c.isEnabled() && !c.hasFocus()) {
                        c.requestFocus();
                    }
                    c.setDragEnabled(true);
                    if (evt.isShiftDown()) { // Select till offset
                        moveDot(offset);
                        adjustRectangularSelectionMouseX(evt.getX(), evt.getY()); // also fires state change
                        mouseState = MouseState.CHAR_SELECTION;
                    } else { // Regular press
                        // check whether selection drag is possible
                        if (isDragPossible(evt) && mapDragOperationFromModifiers(evt) != TransferHandler.NONE) {
                            mouseState = MouseState.DRAG_SELECTION_POSSIBLE;
                        } else { // Drag not possible
                            mouseState = MouseState.CHAR_SELECTION;
                            setDot(offset);
                        }
                    }
                    break;

                case 2: // double-click => word selection
                    mouseState = MouseState.WORD_SELECTION;
                    // Disable drag which would otherwise occur when mouse would be over text
                    c.setDragEnabled(false);
                    // Check possible fold expansion
                    try {
                        // hack, to get knowledge of possible expansion. Editor depends on Folding, so it's not really possible
                        // to have Folding depend on BaseCaret (= a cycle). If BaseCaret moves to editor.lib2, this contract
                        // can be formalized as an interface.
                        Callable<Boolean> cc = (Callable<Boolean>)c.getClientProperty("org.netbeans.api.fold.expander");
                        if (cc == null || !cc.equals(this)) {
                            if (selectWordAction == null) {
                                selectWordAction = ((BaseKit) c.getUI().getEditorKit(
                                        c)).getActionByName(BaseKit.selectWordAction);
                            }
                            if (selectWordAction != null) {
                                selectWordAction.actionPerformed(null);
                            }
                            // Select word action selects forward i.e. dot > mark
                            minSelectionStartOffset = getMark();
                            minSelectionEndOffset = getDot();
                        }
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    break;
                    
                case 3: // triple-click => line selection
                    mouseState = MouseState.LINE_SELECTION;
                    // Disable drag which would otherwise occur when mouse would be over text
                    c.setDragEnabled(false);
                    if (selectLineAction == null) {
                        selectLineAction = ((BaseKit) c.getUI().getEditorKit(
                                c)).getActionByName(BaseKit.selectLineAction);
                    }
                    if (selectLineAction != null) {
                        selectLineAction.actionPerformed(null);
                        // Select word action selects forward i.e. dot > mark
                        minSelectionStartOffset = getMark();
                        minSelectionEndOffset = getDot();
                    }
                    break;

                default: // multi-click
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("mouseReleased: " + logMouseEvent(evt) + ", state=" + mouseState + '\n'); // NOI18N
        }
        
        int offset = mouse2Offset(evt);
        switch (mouseState) {
            case DRAG_SELECTION_POSSIBLE:
                setDot(offset);
                adjustRectangularSelectionMouseX(evt.getX(), evt.getY()); // also fires state change
                break;

            case CHAR_SELECTION:
                moveDot(offset); // Will do setDot() if no selection
                adjustRectangularSelectionMouseX(evt.getX(), evt.getY()); // also fires state change
                break;
        }
        // Set DEFAULT state; after next mouse press the state may change
        // to another state according to particular click count
        mouseState = MouseState.DEFAULT;
        component.setDragEnabled(true);
    }
    
    /**
     * Translates mouse event to text offset
     */
    int mouse2Offset(MouseEvent evt) {
        JTextComponent c = component;
        int offset = 0;
        if (c != null) {
            int y = evt.getY();
            if (y < 0) {
                offset = 0;
            } else if (y > c.getSize().getHeight()) {
                offset = c.getDocument().getLength();
            } else {
                offset = c.viewToModel(new Point(evt.getX(), evt.getY()));
            }
        }
        return offset;
    }
    
    @Override
    public void mouseClicked(MouseEvent evt) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("mouseClicked: " + logMouseEvent(evt) + ", state=" + mouseState + '\n'); // NOI18N
        }
        
        JTextComponent c = component;
        if (c != null) {
            if (isMiddleMouseButtonExt(evt)) {
                if (evt.getClickCount() == 1) {
                    if (c == null) {
                        return;
                    }
                    Clipboard buffer = getSystemSelection();

                    if (buffer == null) {
                        return;
                    }

                    Transferable trans = buffer.getContents(null);
                    if (trans == null) {
                        return;
                    }

                    final BaseDocument doc = (BaseDocument) c.getDocument();
                    if (doc == null) {
                        return;
                    }

                    final int offset = ((BaseTextUI) c.getUI()).viewToModel(c,
                            evt.getX(), evt.getY());

                    try {
                        final String pastingString = (String) trans.getTransferData(DataFlavor.stringFlavor);
                        if (pastingString == null) {
                            return;
                        }
                        doc.runAtomicAsUser(new Runnable() {
                            public @Override
                            void run() {
                                try {
                                    doc.insertString(offset, pastingString, null);
                                    setDot(offset + pastingString.length());
                                } catch (BadLocationException exc) {
                                }
                            }
                        });
                    } catch (UnsupportedFlavorException ufe) {
                    } catch (IOException ioe) {
                    }
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent evt) {
    }

    @Override
    public void mouseExited(MouseEvent evt) {
    }

    // MouseMotionListener methods
    @Override
    public void mouseMoved(MouseEvent evt) {
        if (mouseState == MouseState.DEFAULT) {
            boolean textCursor = true;
            int position = component.viewToModel(evt.getPoint());
            if (RectangularSelectionUtils.isRectangularSelection(component)) {
                List<Position> positions = RectangularSelectionUtils.regionsCopy(component);
                for (int i = 0; textCursor && i < positions.size(); i += 2) {
                    int a = positions.get(i).getOffset();
                    int b = positions.get(i + 1).getOffset();
                    if (a == b) {
                        continue;
                    }

                    textCursor &= !(position >= a && position <= b || position >= b && position <= a);
                }
            } else {
                // stream selection
                if (getDot() == getMark()) {
                    // empty selection
                    textCursor = true;
                } else {
                    int dot = getDot();
                    int mark = getMark();
                    if (position >= dot && position <= mark || position >= mark && position <= dot) {
                        textCursor = false;
                    } else {
                        textCursor = true;
                    }
                }
            }

            if (textCursor != showingTextCursor) {
                int cursorType = textCursor ? Cursor.TEXT_CURSOR : Cursor.DEFAULT_CURSOR;
                component.setCursor(Cursor.getPredefinedCursor(cursorType));
                showingTextCursor = textCursor;
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent evt) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("mouseDragged: " + logMouseEvent(evt) + ", state=" + mouseState + '\n'); //NOI18N
        }
        
        if (isLeftMouseButtonExt(evt)) {
            JTextComponent c = component;
            int offset = mouse2Offset(evt);
            int dot = getDot();
            int mark = getMark();
            try {
                switch (mouseState) {
                    case DEFAULT:
                    case DRAG_SELECTION:
                        break;

                    case DRAG_SELECTION_POSSIBLE:
                        mouseState = MouseState.DRAG_SELECTION;
                        break;
                    
                    case CHAR_SELECTION:
                        moveDot(offset);
                        adjustRectangularSelectionMouseX(evt.getX(), evt.getY());
                        break; // Use the offset under mouse pointer

                    
                    case WORD_SELECTION:
                        // Increase selection if at least in the middle of a word.
                        // It depends whether selection direction is from lower offsets upward or back.
                        if (offset >= mark) { // Selection extends forward.
                            offset = Utilities.getWordEnd(c, offset);
                        } else { // Selection extends backward.
                            offset = Utilities.getWordStart(c, offset);
                        }
                        selectEnsureMinSelection(mark, dot, offset);
                        break;

                    case LINE_SELECTION:
                        if (offset >= mark) { // Selection extends forward
                            offset = Math.min(Utilities.getRowEnd(c, offset) + 1, c.getDocument().getLength());
                        } else { // Selection extends backward
                            offset = Utilities.getRowStart(c, offset);
                        }
                        selectEnsureMinSelection(mark, dot, offset);
                        break;

                    default:
                        throw new AssertionError("Invalid state " + mouseState); // NOI18N
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private void adjustRectangularSelectionMouseX(int x, int y) {
        if (!rectangularSelection) {
            return;
        }
        JTextComponent c = component;
        int offset = c.viewToModel(new Point(x, y));
        Rectangle r = null;
        if (offset >= 0) {
            try {
                r = c.modelToView(offset);
            } catch (BadLocationException ex) {
                r = null;
            }
        }
        if (r != null) {
            float xDiff = x - r.x;
            if (xDiff > 0) {
                float charWidth;
                LockedViewHierarchy lvh = ViewHierarchy.get(c).lock();
                try {
                    charWidth = lvh.getDefaultCharWidth();
                } finally {
                    lvh.unlock();
                }
                int n = (int) (xDiff / charWidth);
                r.x += n * charWidth;
                r.width = (int) charWidth;
            }
            rsDotRect.x = r.x;
            rsDotRect.width = r.width;
            updateRectangularSelectionPaintRect();
            fireStateChanged();
        }
    }
    
    void setRectangularSelectionToDotAndMark() {
        int dotOffset = getDot();
        int markOffset = getMark();
        try {
            rsDotRect = component.modelToView(dotOffset);
            rsMarkRect = component.modelToView(markOffset);
        } catch (BadLocationException ex) {
            rsDotRect = rsMarkRect = null;
        }
        updateRectangularSelectionPaintRect();
    }

    public void updateRectangularUpDownSelection() {
        JTextComponent c = component;
        int dotOffset = getDot();
        try {
            Rectangle r = c.modelToView(dotOffset);
            rsDotRect.y = r.y;
            rsDotRect.height = r.height;
        } catch (BadLocationException ex) {
            // Leave rsDotRect unchanged
        }
    }
    
    /**
     * Extend rectangular selection either by char in a specified selection
     * or by word (if ctrl is pressed).
     *
     * @param toRight true for right or false for left.
     * @param ctrl 
     */
    public void extendRectangularSelection(boolean toRight, boolean ctrl) {
        JTextComponent c = component;
        Document doc = c.getDocument();
        int dotOffset = getDot();
        Element lineRoot = doc.getDefaultRootElement();
        int lineIndex = lineRoot.getElementIndex(dotOffset);
        Element lineElement = lineRoot.getElement(lineIndex);
        float charWidth;
        LockedViewHierarchy lvh = ViewHierarchy.get(c).lock();
        try {
            charWidth = lvh.getDefaultCharWidth();
        } finally {
            lvh.unlock();
        }
        int newDotOffset = -1;
        try {
            int newlineOffset = lineElement.getEndOffset() - 1;
            Rectangle newlineRect = c.modelToView(newlineOffset);
            if (!ctrl) {
                if (toRight) {
                    if (rsDotRect.x < newlineRect.x) {
                        newDotOffset = dotOffset + 1;
                    } else {
                        rsDotRect.x += charWidth;
                    }
                } else { // toLeft
                    if (rsDotRect.x > newlineRect.x) {
                        rsDotRect.x -= charWidth;
                        if (rsDotRect.x < newlineRect.x) { // Fix on rsDotRect
                            newDotOffset = newlineOffset;
                        }
                    } else {
                        newDotOffset = Math.max(dotOffset - 1, lineElement.getStartOffset());
                    }
                }

            } else { // With Ctrl
                int numVirtualChars = 8; // Number of virtual characters per one Ctrl+Shift+Arrow press
                if (toRight) {
                    if (rsDotRect.x < newlineRect.x) {
                        newDotOffset = Math.min(Utilities.getNextWord(c, dotOffset), lineElement.getEndOffset() - 1);
                    } else { // Extend virtually
                        rsDotRect.x += numVirtualChars * charWidth;
                    }
                } else { // toLeft
                    if (rsDotRect.x > newlineRect.x) { // Virtually extended
                        rsDotRect.x -= numVirtualChars * charWidth;
                        if (rsDotRect.x < newlineRect.x) {
                            newDotOffset = newlineOffset;
                        }
                    } else {
                        newDotOffset = Math.max(Utilities.getPreviousWord(c, dotOffset), lineElement.getStartOffset());
                    }
                }
            }

            if (newDotOffset != -1) {
                rsDotRect = c.modelToView(newDotOffset);
                moveDot(newDotOffset); // updates rs and fires state change
            } else {
                updateRectangularSelectionPaintRect();
                fireStateChanged();
            }
        } catch (BadLocationException ex) {
            // Leave selection as is
        }
    }
    
    private void updateRectangularSelectionPaintRect() {
        // Repaint current rect
        JTextComponent c = component;
        Rectangle repaintRect = rsPaintRect;
        if (rsDotRect == null || rsMarkRect == null) {
            return;
        }
        Rectangle newRect = new Rectangle();
        if (rsDotRect.x < rsMarkRect.x) { // Swap selection to left
            newRect.x = rsDotRect.x; // -1 to make the visual selection non-empty
            newRect.width = rsMarkRect.x - newRect.x;
        } else { // Extend or shrink on right
            newRect.x = rsMarkRect.x;
            newRect.width = rsDotRect.x - newRect.x;
        }
        if (rsDotRect.y < rsMarkRect.y) {
            newRect.y = rsDotRect.y;
            newRect.height = (rsMarkRect.y + rsMarkRect.height) - newRect.y;
        } else {
            newRect.y = rsMarkRect.y;
            newRect.height = (rsDotRect.y + rsDotRect.height) - newRect.y;
        }
        if (newRect.width < 2) {
            newRect.width = 2;
        }
        rsPaintRect = newRect;

        // Repaint merged region with original rect
        if (repaintRect == null) {
            repaintRect = rsPaintRect;
        } else {
            repaintRect = repaintRect.union(rsPaintRect);
        }
        c.repaint(repaintRect);
        
        updateRectangularSelectionPositionBlocks();
    }

    private void selectEnsureMinSelection(int mark, int dot, int newDot) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("selectEnsureMinSelection: mark=" + mark + ", dot=" + dot + ", newDot=" + newDot); // NOI18N
        }
        if (dot >= mark) { // Existing forward selection
            if (newDot >= mark) {
                moveDot(Math.max(newDot, minSelectionEndOffset));
            } else { // newDot < mark => swap mark and dot
                setDot(minSelectionEndOffset);
                moveDot(Math.min(newDot, minSelectionStartOffset));
            }

        } else { // Existing backward selection 
            if (newDot <= mark) {
                moveDot(Math.min(newDot, minSelectionStartOffset));
            } else { // newDot > mark => swap mark and dot
                setDot(minSelectionStartOffset);
                moveDot(Math.max(newDot, minSelectionEndOffset));
            }
        }
    }
    
    private boolean isLeftMouseButtonExt(MouseEvent evt) {
        return (SwingUtilities.isLeftMouseButton(evt)
                && !(evt.isPopupTrigger())
                && (evt.getModifiers() & (InputEvent.META_MASK | InputEvent.ALT_MASK)) == 0);
    }
    
    private boolean isMiddleMouseButtonExt(MouseEvent evt) {
        return (evt.getButton() == MouseEvent.BUTTON2) &&
                (evt.getModifiersEx() & (InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK | /* cannot be tested bcs of bug in JDK InputEvent.ALT_DOWN_MASK | */ InputEvent.ALT_GRAPH_DOWN_MASK)) == 0;
    }

    protected int mapDragOperationFromModifiers(MouseEvent e) {
        int mods = e.getModifiersEx();
        
        if ((mods & InputEvent.BUTTON1_DOWN_MASK) == 0) {
            return TransferHandler.NONE;
        }
        
        return TransferHandler.COPY_OR_MOVE;
    }

    /**
     * Determines if the following are true:
     * <ul>
     * <li>the press event is located over a selection
     * <li>the dragEnabled property is true
     * <li>A TranferHandler is installed
     * </ul>
     * <p>
     * This is implemented to check for a TransferHandler.
     * Subclasses should perform the remaining conditions.
     */
    protected boolean isDragPossible(MouseEvent e) {
        JComponent comp = getEventComponent(e);
        boolean possible =  (comp == null) ? false : (comp.getTransferHandler() != null);
        if (possible) {
            JTextComponent c = (JTextComponent) getEventComponent(e);
            if (c.getDragEnabled()) {
                Caret caret = c.getCaret();
                int dot = caret.getDot();
                int mark = caret.getMark();
                if (dot != mark) {
                    Point p = new Point(e.getX(), e.getY());
                    int pos = c.viewToModel(p);

                    int p0 = Math.min(dot, mark);
                    int p1 = Math.max(dot, mark);
                    if ((pos >= p0) && (pos < p1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    protected JComponent getEventComponent(MouseEvent e) {
	Object src = e.getSource();
	if (src instanceof JComponent) {
	    JComponent c = (JComponent) src;
	    return c;
	}
	return null;
    }
    
    private static String logMouseEvent(MouseEvent evt) {
        return "x=" + evt.getX() + ", y=" + evt.getY() + ", clicks=" + evt.getClickCount() //NOI18N
            + ", component=" + s2s(evt.getComponent()) //NOI18N
            + ", source=" + s2s(evt.getSource()) + ", button=" + evt.getButton() + ", mods=" + evt.getModifiers() + ", modsEx=" + evt.getModifiersEx(); //NOI18N
    }

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }

    // PropertyChangeListener methods
    public @Override void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if ("document".equals(propName)) { // NOI18N
            BaseDocument newDoc = (evt.getNewValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getNewValue() : null;
            modelChanged(listenDoc, newDoc);

        } else if (EditorUI.OVERWRITE_MODE_PROPERTY.equals(propName)) {
            Boolean b = (Boolean)evt.getNewValue();
            overwriteMode = (b != null) ? b.booleanValue() : false;
            updateType();

        } else if ("ancestor".equals(propName) && evt.getSource() == component) { // NOI18N
            // The following code ensures that when the width of the line views
            // gets computed on background after the file gets opened
            // (so the horizontal scrollbar gets added after several seconds
            // for larger files) that the suddenly added horizontal scrollbar
            // will not hide the caret laying on the last line of the viewport.
            // A component listener gets installed into horizontal scrollbar
            // and if it's fired the caret's bounds will be checked whether
            // they intersect with the horizontal scrollbar
            // and if so the view will be scrolled.
            Container parent = component.getParent();
            if (parent instanceof JViewport) {
                parent = parent.getParent(); // parent of viewport
                if (parent instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane)parent;
                    JScrollBar hScrollBar = scrollPane.getHorizontalScrollBar();
                    if (hScrollBar != null) {
                        // Add weak listener so that editor pane could be removed
                        // from scrollpane without being held by scrollbar
                        hScrollBar.addComponentListener(
                                (ComponentListener)WeakListeners.create(
                                ComponentListener.class, listenerImpl, hScrollBar));
                    }
                }
            }
        } else if ("enabled".equals(propName)) {
            Boolean enabled = (Boolean) evt.getNewValue();
            if(component.isFocusOwner()) {
                if(enabled == Boolean.TRUE) {
                    if(component.isEditable()) {
                        setVisible(true);
                    }
                    setSelectionVisible(true);
                } else {
                    setVisible(false);
                    setSelectionVisible(false);
                }
            }
        } else if (RECTANGULAR_SELECTION_PROPERTY.equals(propName)) {
            boolean origRectangularSelection = rectangularSelection;
            rectangularSelection = Boolean.TRUE.equals(component.getClientProperty(RECTANGULAR_SELECTION_PROPERTY));
            if (rectangularSelection != origRectangularSelection) {
                if (rectangularSelection) {
                    setRectangularSelectionToDotAndMark();
                    RectangularSelectionTransferHandler.install(component);

                } else { // No rectangular selection
                    RectangularSelectionTransferHandler.uninstall(component);
                }
                fireStateChanged();
            }
        }
    }
    
    // ActionListener methods
    /** Fired when blink timer fires */
    public @Override void actionPerformed(ActionEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            blinkVisible = !blinkVisible;
            if (caretBounds != null) {
                Rectangle repaintRect = caretBounds;
                if (italic) {
                    repaintRect = new Rectangle(repaintRect); // clone
                    repaintRect.width += repaintRect.height;
                }
                c.repaint(repaintRect);
            }
        }
    }
    
    /**
     * This method is an implementation detail.
     * Please do not use it.
     * 
     * @param evt
     * @deprecated
     */
    @Deprecated
    public @Override void foldHierarchyChanged(FoldHierarchyEvent evt) {
        
    }

    void scheduleCaretUpdate() {
        if (!caretUpdatePending) {
            caretUpdatePending = true;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    update(false);
                }
            });
        }
    }
    
    private class ListenerImpl extends ComponentAdapter
    implements FocusListener, ViewHierarchyListener {

        ListenerImpl() {
        }

        // FocusListener methods
        public @Override void focusGained(FocusEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(
                        "BaseCaret.focusGained(); doc=" + // NOI18N
                        component.getDocument().getProperty(Document.TitleProperty) + '\n'
                );
            }
            
            JTextComponent c = component;
            if (c != null) {
                updateType();
                if (component.isEnabled()) {
                    if (component.isEditable()) {
                        setVisible(true);
                }
                    setSelectionVisible(true);
                }
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("Caret visibility: " + isVisible() + '\n'); // NOI18N
                }
            } else {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("Text component is null, caret will not be visible" + '\n'); // NOI18N
                }
            }
        }

        public @Override void focusLost(FocusEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("BaseCaret.focusLost(); doc=" + // NOI18N
                        component.getDocument().getProperty(Document.TitleProperty) +
                        "\nFOCUS GAINER: " + evt.getOppositeComponent() + '\n' // NOI18N
                );
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("FOCUS EVENT: " + evt + '\n'); // NOI18N
                }
            }
	    setVisible(false);
            setSelectionVisible(evt.isTemporary());
        }

        // ComponentListener methods
        /**
         * May be called for either component or horizontal scrollbar.
         */
        public @Override void componentShown(ComponentEvent e) {
            // Called when horizontal scrollbar gets visible
            // (but the same listener added to component as well so must check first)
            // Check whether present caret position will not get hidden
            // under horizontal scrollbar and if so scroll the view
            Component hScrollBar = e.getComponent();
            if (hScrollBar != component) { // really called for horizontal scrollbar
                Component scrollPane = hScrollBar.getParent();
                if (caretBounds != null && scrollPane instanceof JScrollPane) {
                    Rectangle viewRect = ((JScrollPane)scrollPane).getViewport().getViewRect();
                    Rectangle hScrollBarRect = new Rectangle(
                            viewRect.x,
                            viewRect.y + viewRect.height,
                            hScrollBar.getWidth(),
                            hScrollBar.getHeight()
                            );
                    if (hScrollBarRect.intersects(caretBounds)) {
                        // Update caret's position
                        dispatchUpdate(true); // should be visible so scroll the view
                    }
                }
            }
        }

        
        /**
         * May be called for either component or horizontal scrollbar.
         */
        public @Override void componentResized(ComponentEvent e) {
            Component c = e.getComponent();
            if (c == component) { // called for component
                // In case the caretBounds are still null
                // (component not connected to hierarchy yet or it has zero size
                // so the modelToView() returned null) re-attempt to compute the bounds.
                if (caretBounds == null) {
                    dispatchUpdate(true);
                    if (caretBounds != null) { // detach the listener - no longer necessary
                        c.removeComponentListener(this);
                    }
                }
            }
        }

        @Override
        public void viewHierarchyChanged(ViewHierarchyEvent evt) {
            scheduleCaretUpdate();
        }
        
    } // End of ListenerImpl class

    public final void refresh() {
        updateType();
        SwingUtilities.invokeLater(new Runnable() {
            public @Override void run() {
                updateCaretBounds(); // the line height etc. may have change
            }
        });
    }
    
    private static enum MouseState {

        DEFAULT, // Mouse released; not extending any selection
        CHAR_SELECTION, // Extending character selection after single mouse press 
        WORD_SELECTION, // Extending word selection after double-click when mouse button still pressed
        LINE_SELECTION, // Extending line selection after triple-click when mouse button still pressed
        DRAG_SELECTION_POSSIBLE,  // There was a selected text when mouse press arrived so drag is possible
        DRAG_SELECTION  // Drag is being done (text selection existed at the mouse press)
        
    }

    /**
     * Refreshes caret display on the screen.
     * Some height or view changes may result in the caret going off the screen. In some cases, this is not desirable,
     * as the user's work may be interrupted by e.g. an automatic refresh. This method repositions the view so the
     * caret remains visible.
     * <p/>
     * The method has two modes: it can reposition the view just if it originally displayed the caret and the caret became
     * invisible, and it can scroll the caret into view unconditionally.
     * @param retainInView true to scroll only if the caret was visible. False to refresh regardless of visibility.
     */
    public void refresh(boolean retainInView) {
        Rectangle b = caretBounds;
        updateAfterFoldHierarchyChange = b != null;
        boolean wasInView = b != null && component.getVisibleRect().intersects(b);
        
        update(!retainInView || wasInView);
    }

}

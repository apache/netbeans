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
package org.netbeans.api.editor.caret;

import org.netbeans.spi.editor.caret.CascadingNavigationFilter;
import org.netbeans.spi.editor.caret.CaretMoveHandler;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.EditorUtilities;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.AtomicLockEvent;
import org.netbeans.api.editor.document.AtomicLockListener;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.lib.editor.util.ListenerList;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.EditorCaretTransferHandler;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib2.RectangularSelectionCaretAccessor;
import org.netbeans.modules.editor.lib2.RectangularSelectionUtils;
import org.netbeans.modules.editor.lib2.actions.EditorActionUtilities;
import org.netbeans.modules.editor.lib2.caret.CaretFoldExpander;
import org.netbeans.modules.editor.lib2.document.DocumentPostModificationUtils;
import org.netbeans.modules.editor.lib2.document.UndoRedoDocumentEventResolver;
import org.netbeans.modules.editor.lib2.highlighting.CaretOverwriteModeHighlighting;
import org.netbeans.modules.editor.lib2.view.LockedViewHierarchy;
import org.netbeans.modules.editor.lib2.view.ViewHierarchy;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyEvent;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyListener;
import org.netbeans.modules.editor.lib2.view.ViewUtils;
import org.netbeans.spi.editor.caret.NavigationFilterBypass;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Extension to standard Swing caret used by all NetBeans editors.
 * <br>
 * It supports multi-caret editing mode where an arbitrary number of carets
 * is placed at arbitrary positions throughout a document.
 * In this mode each caret is described by its <code>CaretInfo</code> object.
 * <br>
 * The <code>Caret</code> interface is not aware of multiple carets and a call
 * to <code>setDot(int)</code> will only retain a single caret. For multiple
 * carets a call to <code>moveDot(int)</code> will move the last caret only
 * (but it retains other existing carets).
 * <br>
 * The caret works over text components having {@link AbstractDocument} based document.
 *
 * @author Miloslav Metelka
 * @author Ralph Ruijs
 * @since 2.6
 */
public final class EditorCaret implements Caret {
    
    // Temporary until rectangular selection gets ported to multi-caret support
    private static final String RECTANGULAR_SELECTION_PROPERTY = "rectangular-selection"; // NOI18N
    private static final String RECTANGULAR_SELECTION_REGIONS_PROPERTY = "rectangular-selection-regions"; // NOI18N
    private static final String NAVIGATION_FILTER_PROPERTY = EditorCaret.class.getName() + ".navigationFilters"; // NOI18N
    private static final String CHAIN_FILTER_PROPERTY = EditorCaret.class.getName() + ".chainFilter"; // NOI18N
    
    // -J-Dorg.netbeans.api.editor.caret.EditorCaret.level=FINEST
    static final Logger LOG = Logger.getLogger(EditorCaret.class.getName());
    
    // -J-Dorg.netbeans.api.editor.caret.EditorCaret.paint=true with logging enabled for this class
    static final boolean logPaint = Boolean.getBoolean("org.netbeans.api.editor.caret.EditorCaret.paint");
    
    // -J-Dorg.netbeans.api.editor.caret.EditorCaret.blinkRate=800 - Force overrride of a default caret blink rate
    static final Integer overrideCaretBlinkRate = Integer.getInteger("org.netbeans.api.editor.caret.EditorCaret.blinkRate");
    
    /**
     * D&D can be turned off in the editor by -J-Dorg.netbeans.editor.dnd.disabled=true
     */
    private static final boolean dndDisabled = Boolean.getBoolean("org.netbeans.editor.dnd.disabled");
    
    static final long serialVersionUID = 0L;
    
    static {
        RectangularSelectionCaretAccessor.register(new RectangularSelectionCaretAccessor() {
            @Override
            public void setRectangularSelectionToDotAndMark(EditorCaret editorCaret) {
                editorCaret.setRectangularSelectionToDotAndMark();
            }

            @Override
            public void updateRectangularUpDownSelection(EditorCaret editorCaret) {
                editorCaret.updateRectangularUpDownSelection();
            }

            @Override
            public void extendRectangularSelection(EditorCaret editorCaret, boolean toRight, boolean ctrl) {
                editorCaret.extendRectangularSelection(toRight, ctrl);
            }
        });
    }

    /**
     * Non-empty list of individual carets in the order they were created.
     * At least one item is always present.
     */
    private @NonNull GapList<CaretItem> caretItems;
    
    /**
     * Non-empty list of individual carets in the order they were created.
     * At least one item is always present.
     */
    private @NonNull GapList<CaretItem> sortedCaretItems;
    
    /**
     * Cached infos corresponding to caret items or null if any of the items were changed
     *  so another copy of the caret items- (translated into caret infos) will get created
     * upon query to {@link #getCarets() }.
     * <br>
     * Once the list gets created both the list content and information in each caret info are immutable.
     */
    private List<CaretInfo> caretInfos;
    
    /**
     * Cached infos corresponding to sorted caret items or null if any of the sorted items were changed
     * so another copy of the sorted caret items (translated into caret infos) will get created
     * upon query to {@link #getSortedCarets() }.
     * <br>
     * Once the list gets created both the list content and information in each caret info are immutable.
     */
    private List<CaretInfo> sortedCaretInfos;
    
    /** Component this caret is bound to */
    private JTextComponent component;
    
    /** List of individual carets */
    private boolean overwriteMode;

    private final ListenerList<EditorCaretListener> listenerList;

    private final ListenerList<ChangeListener> changeListenerList;

    /**
     * Implementor of various listeners.
     */
    private final ListenerImpl listenerImpl;
    
    /** Is the caret visible (after <code>setVisible(true)</code> call? */
    private boolean visible;

    /**
     * Whether blinking caret is currently visible on the screen.
     * <br>
     * This changes from true to false and back as caret(s) blink.
     * <br>
     * If false then original locations do not need to be repainted.
     */
    private boolean showing;

    /**
     * Determine if a possible selection would be displayed or not.
     */
    private boolean selectionVisible;

    /** Type of the caret */
    private CaretType type = CaretType.THICK_LINE_CARET;

    /** Width of caret */
    private int thickCaretWidth = EditorPreferencesDefaults.defaultThickCaretWidth;
    
    private MouseState mouseState = MouseState.DEFAULT;
    
    /**
     * Default delay for caret blinking if the system is responsive enough.
     * Zero if caret blinking is disabled.
     */
    private int blinkDefaultDelay;
    
    /**
     * Currently used delay according to system responsiveness.
     */
    private int blinkCurrentDelay;
    
    /**
     * Last time when blink timer action was invoked or zero if the blinking delay
     * should not be examined upon next timer action invocation.
     * If there are too many carets being painted and the system becomes busy
     * the blinkCurrentDelay may be increased to lower the blinking frequency
     * and allow the system to be responsive.
     */
    private long lastBlinkTime;
    
    /**
     * Carets blinking timer.
     */
    private Timer blinkTimer;
    
    private ActionListener weakTimerListener;

    private Action selectWordAction;
    private Action selectLineAction;

    private AbstractDocument activeDoc;
    
    private Thread lockThread;
    
    private int lockDepth;
    
    private CaretTransaction activeTransaction;
    
    /**
     * Caret item to which the view should scroll or null for no scrolling.
     */
    private boolean scrollToLastCaret;

    /**
     * Whether the text is being modified under atomic lock.
     * If so just one caret change is fired at the end of all modifications.
     */
    private transient boolean inAtomicSection = false;
    private transient boolean inAtomicUnlock = false;

    /**
     * Offset to which the caret should be relocated if there's no explicit request
     * for caret placement and there were some modifications performed.
     */
    private int atomicSectionImplicitSetDotOffset;
    
    /**
     * Whether there was an explicit request to place the caret during the atomic section or not.
     * If not explicit request was done then use atomicSectionSetDotOffset and place the caret there.
     */
    private boolean atomicSectionAnyCaretChange;
    
    /**
     * Lowest offset where modification was performed or where view change occurred
     * during atomic lock section.
     * <br>
     * Set to Integer.MAX_VALUE if no modification was done during atomic section yet.
     */
    private transient int atomicSectionStartChangeOffset;
    /**
     * Highest offset where modification was performed or where view change occurred
     * during atomic lock section.
     * <br>
     * It may be Integer.MAX_VALUE to mark that all caret infos till end of doc
     * must be recomputed.
     */
    private transient int atomicSectionEndChangeOffset;
    
    private Preferences prefs = null;

    private PreferenceChangeListener weakPrefsListener = null;
    
    private boolean caretUpdatePending;
    
    /**
     * If the component is invalid upon call to update() method
     * then request a non-scrolling update by setting the variable true.
     */
    private boolean updateLaterDuringPaint;
    
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

    public EditorCaret() {
        caretItems = new GapList<>();
        sortedCaretItems = new GapList<>();
        CaretItem singleCaret = new CaretItem(this, null, Position.Bias.Forward, null, Position.Bias.Forward);
        caretItems.add(singleCaret);
        sortedCaretItems.add(singleCaret);

        listenerList = new ListenerList<>();
        changeListenerList = new ListenerList<>();
        listenerImpl = new ListenerImpl();
    }

    /**
     * Get dot offset of the last created caret in the underlying document.
     * @return dot offset &gt;=0
     */
    @Override
    public int getDot() {
        return getLastCaret().getDot();
    }
    
    /**
     * Get a bias of the dot position which is either
     * {@link javax.swing.text.Position.Bias#Forward} or {@link javax.swing.text.Position.Bias#Backward} depending
     * on whether the caret biases towards the next character or previous one.
     * The bias is always forward for non bidirectional text document.
     *
     * @return either forward or backward bias.
     * @since 2.12
     */
    @NonNull
    public Position.Bias getDotBias() {
        return getLastCaret().getDotBias();
    }

    /**
     * Get mark offset of the last created caret in the underlying document.
     * If there is a selection, the mark will not be the same as the dot.
     * @return mark offset &gt;=0
     */
  
    @Override
    public int getMark() {
        return getLastCaret().getMark();
    }
    
    /**
     * Get a bias of the mark position which is either
     * {@link javax.swing.text.Position.Bias#Forward} or {@link javax.swing.text.Position.Bias#Backward} depending
     * on whether the caret biases towards the next character or previous one.
     * The bias is always forward for non bidirectional text document.
     *
     * @return either forward or backward bias.
     * @since 2.12
     */
    @NonNull
    public Position.Bias getMarkBias() {
        return getLastCaret().getMarkBias();
    }

    /**
     * Get information about all existing carets in the order they were created.
     * <br>
     * The list always has at least one item. The last caret (last item of the list)
     * is the most recent caret.
     * <br>
     * The list is a snapshot of the current state of the carets. The list content itself and its contained
     * caret infos are guaranteed not change after subsequent calls to caret API or document modifications.
     * <br>
     * The list is nonmodifiable.
     * <br>
     * This method should be called with document's read-lock acquired which will guarantee
     * stability of {@link CaretInfo#getDot() } and {@link CaretInfo#getMark() } and prevent
     * caret merging as a possible effect of document modifications.
     * 
     * @return copy of caret list with size &gt;= 1 containing information about all carets.
     */
    public @NonNull List<CaretInfo> getCarets() {
        synchronized (listenerList) {
            if (caretInfos == null) {
                int i = caretItems.size();
                CaretInfo[] infos = new CaretInfo[i--];
                for (; i >= 0; i--) {
                    infos[i] = caretItems.get(i).getValidInfo();
                }
                caretInfos = ArrayUtilities.unmodifiableList(infos);
            }
            return caretInfos;
        }
    }
    
    /**
     * Get information about all existing carets sorted by dot positions in ascending order.
     * <br>
     * If some of the carets are {@link org.netbeans.api.editor.document.ComplexPositions}
     * their order will reflect the increasing split offset.
     * <br>
     * The list is a snapshot of the current state of the carets. The list content itself and its contained
     * caret infos are guaranteed not change after subsequent calls to caret API or document modifications.
     * <br>
     * The list is nonmodifiable.
     * <br>
     * This method should be called with document's read-lock acquired which will guarantee
     * stability of {@link CaretInfo#getDot() } and {@link CaretInfo#getMark() } and prevent
     * caret merging as a possible effect of document modifications.
     * 
     * @return copy of caret list with size &gt;= 1 sorted by dot positions in ascending order.
     */
    public @NonNull List<CaretInfo> getSortedCarets() {
        synchronized (listenerList) {
            if (sortedCaretInfos == null) {
                int i = sortedCaretItems.size();
                CaretInfo[] sortedInfos = new CaretInfo[i--];
                for (; i >= 0; i--) {
                    sortedInfos[i] = sortedCaretItems.get(i).getValidInfo();
                }
                sortedCaretInfos = ArrayUtilities.unmodifiableList(sortedInfos);
            }
            return sortedCaretInfos;
        }
    }
    
    /**
     * Get info about the most recently created caret.
     * <br>
     * For normal mode this is the only caret returned by {@link #getCarets() }.
     * <br>
     * For multi-caret mode this is the last item in the list returned by {@link #getCarets() }.
     * 
     * @return last caret (the most recently added caret).
     */
    public @NonNull CaretInfo getLastCaret() {
        synchronized (listenerList) {
            return caretItems.get(caretItems.size() - 1).getValidInfo();
        }
    }
    
    /**
     * Get information about the caret at the specified offset.
     *
     * @param offset the offset of the caret
     * @return CaretInfo for the caret at offset, null if there is no caret or
     * the offset is invalid
     */
    public @CheckForNull CaretInfo getCaretAt(int offset) {
        return null; // TBD
    }
    
    /**
     * Assign a new offset to the caret in the underlying document.
     * <br>
     * This method implicitly sets the selection range to zero.
     * <br>
     * If multiple carets are present this method retains only last caret
     * which then moves to the given offset.
     * 
     * @param offset {@inheritDoc}
     * @see Caret#setDot(int) 
     */
    public @Override void setDot(final int offset) {
        setDot(offset, Position.Bias.Forward, MoveCaretsOrigin.DEFAULT);
    }

    /**
     * Assign a new offset to the caret and identify the operation which
     * originated the caret movement. 
     * <p>
     * In addition to {@link #setDot(int)},
     * the caller may identify the operation that originated the caret movement.
     * This information is received by {@link NavigationFilter}s or {@link ChangeListener}s
     * and may be used to react or modify the caret movements.
     * </p><p>
     * Use {@code null} or {@link MoveCaretsOrigin#DEFAULT} if the operation not known. Use
     * {@link MoveCaretsOrigin#DIRECT_NAVIGATION} action type to identify simple navigational
     * actions (pg up, pg down, left, right, ...).
     * </p>
     * @param offset new offset for the caret
     * @param bias new bias for the caret. Use either {@link javax.swing.text.Position.Bias#Forward}
     *  or {@link javax.swing.text.Position.Bias#Backward} depending on whether the caret should bias
     *  towards the next character or previous one. Use forward bias for non-bidirectional text document.
     * @param origin specifies the operation which caused the caret to move.
     * @see #setDot(int) 
     * @since 2.10
     */
    public void setDot(final int offset, Position.Bias bias, MoveCaretsOrigin origin) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("EditorCaret.setDot: offset=" + offset + ", bias=" + bias + ", origin=" + origin + "\n"); //NOI18N
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.INFO, "    setDot call stack\n", new Exception());
            }
        }
        runTransaction(CaretTransaction.RemoveType.RETAIN_LAST_CARET, 0, null, new CaretMoveHandler() {
            @Override
            public void moveCarets(CaretMoveContext context) {
                Document doc = context.getComponent().getDocument();
                if (doc != null) {
                    try {
                        Position pos = doc.createPosition(offset);
                        context.setDot(context.getOriginalLastCaret(), pos, Position.Bias.Forward);
                    } catch (BadLocationException ex) {
                        // Ignore the setDot() request
                    }
                }
            }
        }, origin);
    }

    /**
     * Moves the caret position (dot) to some other position, leaving behind the
     * mark. This is useful for making selections.
     * <br>
     * If multiple carets are present this method retains all carets
     * and moves the dot of the last caret to the given offset.
     * 
     * @param offset {@inheritDoc}
     * @see Caret#moveDot(int) 
     */
    public @Override void moveDot(final int offset) {
        moveDot(offset, Position.Bias.Forward, MoveCaretsOrigin.DEFAULT);
    }
    
    /**
     * Moves the caret position (dot) to some other position, leaving behind the
     * mark. 
     * <p>
     * In addition to {@link #setDot(int)},
     * the caller may identify the operation that originated the caret movement.
     * This information is received by {@link NavigationFilter}s or {@link EditorCaretListener}s
     * and may be used to react or modify the caret movements.
     * </p><p>
     * Use {@code null} or {@link MoveCaretsOrigin#DEFAULT} if the operation not known. Use
     * {@link MoveCaretsOrigin#DIRECT_NAVIGATION} action type to identify simple navigational
     * actions (pg up, pg down, left, right, ...).
     * </p>
     * 
     * @param offset new offset for the caret
     * @param bias new bias for the caret. Use either {@link javax.swing.text.Position.Bias#Forward}
     *  or {@link javax.swing.text.Position.Bias#Backward} depending on whether the caret should bias
     *  towards the next character or previous one. Use forward bias for non-bidirectional text document.
     * @param origin specifies the operation which caused the caret to move.
     * @see #moveDot(int) 
     * @since 2.10
     */
    public void moveDot(final int offset, Position.Bias bias, MoveCaretsOrigin origin) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("EditorCaret.moveDot: offset=" + offset + ", bias=" + bias + ", origin=" + origin + "\n"); //NOI18N
        }
        
        runTransaction(CaretTransaction.RemoveType.NO_REMOVE, 0, null, new CaretMoveHandler() {
            @Override
            public void moveCarets(CaretMoveContext context) {
                Document doc = context.getComponent().getDocument();
                if (doc != null) {
                    try {
                        Position pos = doc.createPosition(offset);
                        context.moveDot(context.getOriginalLastCaret(), pos, Position.Bias.Forward);
                    } catch (BadLocationException ex) {
                        // Ignore the setDot() request
                    }
                }
            }
        }, origin);
    }

    /**
     * Move multiple carets or create/modify selections.
     * <br>
     * For performance reasons this is made as a single transaction over the caret
     * with only one change event being fired.
     * <br>
     * Note that the move handler does not permit to add or remove carets - this has to be performed
     * by other methods present in this class (as another transaction over the editor caret).
     * <br>
     * <pre>
     * <code>
     *   // Go one line up with all carets
     *   editorCaret.moveCarets(new CaretMoveHandler() {
     *     &#64;Override public void moveCarets(CaretMoveContext context) {
     *       for (CaretInfo caretInfo : context.getOriginalSortedCarets()) {
     *         try {
     *           int dot = caretInfo.getDot();
     *           dot = Utilities.getPositionAbove(target, dot, p.x);
     *           Position dotPos = doc.createPosition(dot);
     *           context.setDot(caretInfo, dotPos);
     *         } catch (BadLocationException e) {
     *           // the position stays the same
     *         }
     *       }
     *     }
     *   });
     * </code>
     * </pre>
     *
     * @param moveHandler non-null move handler to perform the changes. The handler's methods
     *  will be given a context to operate on.
     * @return difference between current count of carets and the number of carets when the operation started.
     *  Returns Integer.MIN_VALUE if the operation was cancelled due to the caret not being installed in any text component
     *  or no document installed in the text component.
     */
    public int moveCarets(@NonNull CaretMoveHandler moveHandler) {
        Parameters.notNull("moveHandler", moveHandler);
        return runTransaction(CaretTransaction.RemoveType.NO_REMOVE, 0, null, moveHandler);
    }
    
    /**
     * Move multiple carets or create/modify selections, specifies the originating operation.
     * <p>
     * In addition to {@link #moveCarets(org.netbeans.spi.editor.caret.CaretMoveHandler)}, the caller may specify
     * what operation causes the caret movements in the `origin' parameter, see {@link MoveCaretsOrigin} class. 
     * This information is received by {@link NavigationFilter}s or {@link EditorCaretListener}s
     * and may be used to react or modify the caret movements.
     * </p><p>
     * Use {@code null} or {@link MoveCaretsOrigin#DEFAULT} if the operation not known. Use
     * {@link MoveCaretsOrigin#DIRECT_NAVIGATION} action type to identify simple navigational
     * actions (pg up, pg down, left, right, ...).
     * </p><p>
     * See the {@link #moveCarets(org.netbeans.spi.editor.caret.CaretMoveHandler) } for detailed description of
     * how carets are moved.
     * </p>
     * 
     * @param moveHandler handler which moves individual carets
     * @param origin description of the originating operation. Use {@code null} or {@link MoveCaretsOrigin#DEFAULT} for default/unspecified operation.
     * @return difference between number of carets, see {@link #moveCarets(org.netbeans.spi.editor.caret.CaretMoveHandler)}.
     * @see #moveCarets(org.netbeans.spi.editor.caret.CaretMoveHandler) 
     * @since 2.10
     */
    public int moveCarets(@NonNull CaretMoveHandler moveHandler, MoveCaretsOrigin origin) {
        Parameters.notNull("moveHandler", moveHandler);
        if (origin ==  null) {
            origin = MoveCaretsOrigin.DEFAULT;
        }
        return runTransaction(CaretTransaction.RemoveType.NO_REMOVE, 0, null, moveHandler, origin);
    }

    /**
     * Create a new caret at the given position with a possible selection.
     * <br>
     * The caret will become the last caret of the list returned by {@link #getCarets() }.
     * <br>
     * This method requires the caller to have either read lock or write lock acquired
     * over the underlying document.
     * <br>
     * <pre>
     * <code>
     *   editorCaret.addCaret(pos, pos); // Add a new caret at pos.getOffset()
     * 
     *   Position pos2 = doc.createPosition(pos.getOffset() + 2);
     *   // Add a new caret with selection starting at pos and extending to pos2 with caret located at pos2
     *   editorCaret.addCaret(pos2, pos);
     *   // Add a new caret with selection starting at pos and extending to pos2 with caret located at pos
     *   editorCaret.addCaret(pos, pos2);
     * </code>
     * </pre>
     * 
     * @param dotPos position of the newly created caret.
     * @param dotBias bias of the new caret. Use either {@link javax.swing.text.Position.Bias#Forward}
     *  or {@link javax.swing.text.Position.Bias#Backward} depending on whether the caret should bias
     *  towards the next character or previous one. Use forward bias for non-bidirectional text document.
     * @param markPos beginning of the selection (the other end is dotPos) or the same position like dotPos for no selection.
     *  The markPos may have higher offset than dotPos to select in a backward direction.
     * @param markBias bias of the begining of the selection. Use either {@link javax.swing.text.Position.Bias#Forward}
     *  or {@link javax.swing.text.Position.Bias#Backward} depending on whether the caret should bias
     *  towards the next character or previous one. Use forward bias for non-bidirectional text document.
     * @return difference between current count of carets and the number of carets when the operation started.
     *  Returns Integer.MIN_VALUE if the operation was canceled due to the caret not being installed in any text component
     *  or no document installed in the text component.
     *  <br>
     *  Note that adding a new caret to offset where another caret is already located may lead
     *  or no document installed in the text component.
     */
    public int addCaret(@NonNull Position dotPos, @NonNull Position.Bias dotBias,
            @NonNull Position markPos, @NonNull Position.Bias markBias)
    {
        return runTransaction(CaretTransaction.RemoveType.NO_REMOVE, 0,
                new CaretItem[] { new CaretItem(this, dotPos, dotBias, markPos, markBias) }, null);
    }
    
    /**
     * Add multiple carets at once.
     * <br>
     * It is similar to calling {@link #addCaret(javax.swing.text.Position, javax.swing.text.Position.Bias, javax.swing.text.Position, javax.swing.text.Position.Bias) }
     * multiple times but this method is more efficient (it only fires caret change once).
     * <br>
     * This method requires the caller to have either read lock or write lock acquired
     * over the underlying document.
     * <br>
     * <pre>
     * <code>
     *   List&lt;Position&gt; pairs = new ArrayList&lt;&gt;();
     *   pairs.add(dotPos);
     *   pairs.add(dotPos);
     *   pairs.add(dot2Pos);
     *   pairs.add(mark2Pos);
     *   // Add caret located at dotPos.getOffset() and another one with selection
     *   // starting at mark2Pos and extending to dot2Pos with caret located at dot2Pos
     *   editorCaret.addCaret(pairs);
     * </code>
     * </pre>
     * 
     * @param dotAndMarkPosPairs list of position pairs consisting of dot position
     *  and mark position (selection start position) which may be the same position like the dot
     *  if the particular caret has no selection. The list must have even size.
     * @param dotAndMarkBiases list of position biases (corresponding to the dot and mark positions
     *  in the previous parameter) or null may be passed if all dotAndMarkPosPairs positions should have a forward bias.
     * @return difference between current count of carets and the number of carets when the operation started.
     *  Returns Integer.MIN_VALUE if the operation was canceled due to the caret not being installed in any text component
     *  or no document installed in the text component.
     * @see #addCaret(javax.swing.text.Position, javax.swing.text.Position.Bias, javax.swing.text.Position, javax.swing.text.Position.Bias)
     */
    public int addCarets(@NonNull List<Position> dotAndMarkPosPairs, List<Position.Bias> dotAndMarkBiases) {
        return runTransaction(CaretTransaction.RemoveType.NO_REMOVE, 0,
                CaretTransaction.asCaretItems(this, dotAndMarkPosPairs, dotAndMarkBiases), null);
    }

    /**
     * Replace all current carets with the new ones.
     * <br>
     * This method requires the caller to have either read lock or write lock acquired
     * over the underlying document.
     * <br>
     * @param dotAndMarkPosPairs list of position pairs consisting of dot position
     *  and mark position (selection start position) which may be the same position like dot
     *  if the particular caret has no selection. The list must have even size.
     * @param dotAndMarkBiases list of position biases (corresponding to the dot and mark positions
     *  in the previous parameter) or null may be passed if all dotAndMarkPosPairs positions should have a forward bias.
     * @return difference between current count of carets and the number of carets when the operation started.
     *  Returns Integer.MIN_VALUE if the operation was cancelled due to the caret not being installed in any text component
     *  or no document installed in the text component.
     */
    public int replaceCarets(@NonNull List<Position> dotAndMarkPosPairs, List<Position.Bias> dotAndMarkBiases) {
        if (dotAndMarkPosPairs.isEmpty()) {
            throw new IllegalArgumentException("dotAndSelectionStartPosPairs list must not be empty");
        }
        CaretItem[] addedItems = CaretTransaction.asCaretItems(this, dotAndMarkPosPairs, dotAndMarkBiases);
        return runTransaction(CaretTransaction.RemoveType.REMOVE_ALL_CARETS, 0, addedItems, null);
    }

    /**
     * Remove last added caret (determined by {@link #getLastCaret() }).
     * <br>
     * If there is just one caret the method has no effect.
     * 
     * @return difference between current count of carets and the number of carets when the operation started.
     *  Returns Integer.MIN_VALUE if the operation was cancelled due to the caret not being installed in any text component
     *  or no document installed in the text component.
     */
    public int removeLastCaret() {
        return runTransaction(CaretTransaction.RemoveType.REMOVE_LAST_CARET, 0, null, null);
    }

    /**
     * Switch to single caret mode by removing all carets except the last caret.
     * @return difference between current count of carets and the number of carets when the operation started.
     *  Returns Integer.MIN_VALUE if the operation was cancelled due to the caret not being installed in any text component
     *  or no document installed in the text component.
     */
    public int retainLastCaretOnly() {
        return runTransaction(CaretTransaction.RemoveType.RETAIN_LAST_CARET, 0, null, null);
    }
    
    /**
     * Adds listener to track caret changes in detail.
     * 
     * @param listener non-null listener.
     */
    public void addEditorCaretListener(@NonNull EditorCaretListener listener) {
        listenerList.add(listener);
    }
    
    /**
     * Adds listener to track caret position changes (to fulfil {@link Caret} interface).
     */
    @Override
    public void addChangeListener(@NonNull ChangeListener l) {
        changeListenerList.add(l);
    }

    public void removeEditorCaretListener(@NonNull EditorCaretListener listener) {
        listenerList.remove(listener);
    }
    
    /**
     * Removes listener to track caret position changes (to fulfil {@link Caret} interface).
     */
    @Override
    public void removeChangeListener(@NonNull ChangeListener l) {
        changeListenerList.remove(l);
    }

    /**
     * Determines if the caret is currently visible (it may be blinking depending on settings).
     * <p>
     * Caret becomes visible after <code>setVisible(true)</code> gets called on it.
     *
     * @return <code>true</code> if visible else <code>false</code>
     */
    @Override
    public boolean isVisible() {
        synchronized (listenerList) {
            return visible;
        }
    }

    /**
     * Sets the caret visibility, and repaints the caret.
     *
     * @param visible the visibility specifier
     * @see Caret#setVisible
     */
    @Override
    public void setVisible(boolean visible) {
        boolean log = LOG.isLoggable(Level.FINE);
        if (log && LOG.isLoggable(Level.FINER)) {
            LOG.finer("EditorCaret.setVisible(" + visible + ")\n");
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.INFO, "", new Exception());
            }
        }
        synchronized (listenerList) {
            if (blinkTimer != null) {
                if (this.visible && !visible) {
                    blinkTimer.stop();
                    setShowing(false);
                }
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("    " + (visible ? "Starting" : "Stopping") + // NOI18N
                            " the caret blinking timer: " + dumpVisibility() + '\n'); // NOI18N
                }
                this.visible = visible;
                if (visible) {
                    setShowing(true);
                    blinkTimer.start();
                } else {
                    blinkTimer.stop();
                }
            }
        }
        JTextComponent c = component;
        if (c != null) {
            List<CaretInfo> sortedCarets = getSortedCarets();
            int sortedCaretsSize = sortedCarets.size();
            for (int i = 0; i < sortedCaretsSize; i++) {
                CaretInfo caret = sortedCarets.get(i);
                CaretItem caretItem = caret.getCaretItem();
                caretItem.repaintIfShowing(c, "setVisible", i);
            }
        }
    }

    @Override
    public boolean isSelectionVisible() {
        return selectionVisible;
    }

    @Override
    public void setSelectionVisible(boolean v) {
        if (selectionVisible == v) {
            return;
        }
        JTextComponent c = component;
        Document doc;
        if (c != null && (doc = c.getDocument()) != null) {
            selectionVisible = v;
            // [TODO] ensure to repaint 
        }
    }

    @Override
    public void install(JTextComponent c) {
        assert (SwingUtilities.isEventDispatchThread()); // must be done in AWT
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("EditorCaret.install: Installing to " + s2s(c) + "\n"); //NOI18N
        }
        
        component = c;
        setVisible(c.hasFocus());
        modelChanged(null, c.getDocument());

        Boolean b = (Boolean) c.getClientProperty(EditorUtilities.CARET_OVERWRITE_MODE_PROPERTY);
        overwriteMode = (b != null) ? b : false;
        updateOverwriteModeLayer(true);
        
        // Attempt to assign initial bounds - usually here the component
        // is not yet added to the component hierarchy.
        requestUpdateAllCaretsBounds();
        
        if(getLastCaretItem().getCaretBounds() == null) {
            // For null bounds wait for the component to get resized
            // and attempt to recompute bounds then
            component.addComponentListener(listenerImpl);
        }

        component.addPropertyChangeListener(listenerImpl);
        component.addFocusListener(listenerImpl);
        component.addMouseListener(listenerImpl);
        component.addMouseMotionListener(listenerImpl);
        component.addKeyListener(listenerImpl);
        ViewHierarchy.get(component).addViewHierarchyListener(listenerImpl);
        
        EditorCaretTransferHandler.install(component);

        if (component.hasFocus()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("    Component has focus, calling EditorCaret.focusGained(); doc=" // NOI18N
                    + component.getDocument().getProperty(Document.TitleProperty) + '\n');
            }
            listenerImpl.focusGained(null); // emulate focus gained
        }
        invalidateCaretBounds(0, Integer.MAX_VALUE);
        dispatchUpdate(false);
        resetBlink();
    }
    
    @Override
    public void deinstall(JTextComponent c) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("EditorCaret.deinstall: Deinstalling from " + s2s(c) + "\n"); //NOI18N
        }
        
        synchronized (listenerList) {
            if (blinkTimer != null) {
                setBlinkRate(0);
            }
        }
        
        c.removeComponentListener(listenerImpl);
        c.removePropertyChangeListener(listenerImpl);
        c.removeFocusListener(listenerImpl);
        c.removeMouseListener(listenerImpl);
        c.removeMouseMotionListener(listenerImpl);
        ViewHierarchy.get(c).removeViewHierarchyListener(listenerImpl);

        
        modelChanged(activeDoc, null);
    }
    
    /**
     * Saves relative position of the 'main' caret for the case that fold view
     * update is underway - this update should maintain caret visual position on screen.
     * For that, the nearest update must reposition scroller's viewrect so that the 
     * recomputed caretBounds is at the same
     */
    private synchronized void maybeSaveCaretOffset(Rectangle cbounds) {
        if (component.getClientProperty("editorcaret.updateRetainsVisibleOnce")  == null || // NOI18N
            lastCaretVisualOffset != -1) {
           return; 
        }
        Rectangle editorRect;
        final JViewport viewport = getViewport();
        if (viewport != null) {
            editorRect = viewport.getViewRect();
        } else {
            Dimension size = component.getSize();
            editorRect = new Rectangle(0, 0, size.width, size.height);
        }
        if (cbounds.y >= editorRect.y && cbounds.y < (editorRect.y + editorRect.height)) {
            lastCaretVisualOffset = (cbounds.y - editorRect.y);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.fine("EditorCaret: saving caret offset. Bounds = " + cbounds + ", editor = " + editorRect + ", offset = " + lastCaretVisualOffset);
            }
        } else {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.fine("EditorCaret: caret is off screen. Bounds = " + cbounds + ", editor = " + editorRect);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        final JTextComponent c = component;
        if (c == null || !isShowing()) {
            return;
        }
        if (updateLaterDuringPaint) {
            updateLaterDuringPaint = false;
            update(true);
        }
        Color origColor = g.getColor();
        try {
            g.setColor(c.getCaretColor());
            Rectangle clipBounds = g.getClipBounds();
            LockedViewHierarchy lvh = ViewHierarchy.get(c).lock();
            try {
                int clipStartOffset = lvh.yToParagraphStartOffset(clipBounds.y);
                // Find the first caret to be painted
                // Only paint carets that are part of the clip - use sorted carets
                List<CaretInfo> sortedCarets = getSortedCarets();
                CaretItem lastCaret = getLastCaretItem();
                int low = 0;
                int caretsSize = sortedCarets.size();
                if (caretsSize > 1) {
                    int high = caretsSize - 1;
                    while (low <= high) {
                        int mid = (low + high) >>> 1;
                        CaretInfo midCaretInfo = sortedCarets.get(mid);
                        int midDot = midCaretInfo.getDot();
                        if (midDot < clipStartOffset) {
                            low = mid + 1;
                        } else if (midDot > clipStartOffset) {
                            high = mid - 1;
                        } else { // midDot == clipStartOffset
                            // There should not be multiple carets at the same offset so use the found one
                            low = mid;
                            break;
                        }
                    }
                }
                // Use "low" index (which is higher than "high" at end of binary-search)
                for (int i = low; i < caretsSize; i++) {
                    CaretInfo caretInfo = sortedCarets.get(i);
                    CaretItem caretItem = caretInfo.getCaretItem();
                    // Recompute bounds if they are obsolete - optimization in update() only recomputes visible carets,
                    // so by the way it's not necessary to repaint the original or just-computed bounds
                    // since they were not visible up to this moment.
                    if (caretItem.getAndClearUpdateCaretBounds()) {
                        int dot = caretItem.getDot();
                        Rectangle newCaretBounds = lvh.modelToViewBounds(dot, Position.Bias.Forward);
                        Rectangle oldBounds = caretItem.setCaretBoundsWithRepaint(newCaretBounds, c, "EditorCaret.paint()", i);
                        if (caretItem == lastCaret && oldBounds != null) {
                            maybeSaveCaretOffset(oldBounds);
                        }
                    }
                    Rectangle caretBounds = caretItem.getCaretBounds();
                    if (caretBounds != null) {
                        // Break painting if caret is below clip
                        if (caretBounds.y > clipBounds.y + clipBounds.height) {
                            break;
                        }
                        boolean painted = false;
                        switch (type) {
                            case THICK_LINE_CARET:
                                g.fillRect(caretBounds.x, caretBounds.y, this.thickCaretWidth, caretBounds.height - 1);
                                painted = true;
                                break;

                            case THIN_LINE_CARET:
                                int upperX = caretBounds.x;
                                g.drawLine((int) upperX, caretBounds.y, caretBounds.x, (caretBounds.y + caretBounds.height - 1));
                                painted = true;
                                break;

                            case BLOCK_CARET:
                                // Use a CaretOverwriteModeHighlighting layer to paint the caret
                                // Do not mark caret as painted since the highlighting layers handle all this automatically
                                break;

                            default:
                                throw new IllegalStateException("Invalid caret type=" + type);
                        }
                        if (painted) {
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("EditorCaret.paint: caretItem[" + i + "] item=" + caretItem.toStringDetail() + "\n");
                            }
                            caretItem.markCaretPainted();
                        }
                    }
                }
            } finally {
                lvh.unlock();
            }

            // Possibly service rectangular selection
            if (rectangularSelection && rsPaintRect != null && g instanceof Graphics2D) {
                Graphics2D g2d = (Graphics2D) g;
                Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {4, 2}, 0);
                Stroke origStroke = g2d.getStroke();
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
                }
            }
        } finally {
            g.setColor(origColor);
        }
    }

    public @Override void setMagicCaretPosition(final Point p) {
        runTransaction(CaretTransaction.RemoveType.NO_REMOVE, 0, null, new CaretMoveHandler() {
            @Override
            public void moveCarets(CaretMoveContext context) {
                context.setMagicCaretPosition(context.getOriginalLastCaret(), p);
            }
        }, MoveCaretsOrigin.DISABLE_FILTERS);
    }

    public @Override final Point getMagicCaretPosition() {
        return getLastCaretItem().getMagicCaretPosition();
    }

    public @Override void setBlinkRate(int rate) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("EditorCaret.setBlinkRate(" + rate + ") " + dumpVisibility() + '\n'); // NOI18N
        }
        synchronized (listenerList) {
            this.blinkDefaultDelay = rate;
            this.blinkCurrentDelay = rate;
            if (blinkTimer == null && rate > 0) {
                blinkTimer = new Timer(rate, null);
                blinkTimer.addActionListener(weakTimerListener = WeakListeners.create(
                        ActionListener.class, listenerImpl, blinkTimer));
            }
            if (blinkTimer != null) {
                if (rate > 0) {
                    if (blinkTimer.getDelay() != rate) {
                        blinkTimer.setInitialDelay(rate);
                        blinkTimer.setDelay(rate);
                    }
                } else { // zero rate - don't blink
                    blinkTimer.stop();
                    if (weakTimerListener != null) {
                        blinkTimer.removeActionListener(weakTimerListener);
                    }
                    blinkTimer = null;
                    setShowing(true);
                    LOG.finer("    Zero blink rate - no blinking. blinkTimer=null; showing=true\n"); // NOI18N
                }
            }
        }
    }
    
    /**
     * Returns the navigation filter for a certain operation. 
     * {@link NavigationFilter} can be 
     * registered to receive only limited set of operations. This method returns the filter 
     * for the specified operation. Use {@link MoveCaretsOrigin#DEFAULT} to get text 
     * component's navigation filter (equivalent to {@link JTextComponent#getNavigationFilter() 
     * JTextComponent.getNavigationFilter()}. That filter receives all caret movements.
     * @param component the component whose filter should be returned
     * @param origin the operation description
     * @return the current navigation filter.
     * @since 2.10
     */
    public static @CheckForNull NavigationFilter getNavigationFilter(@NonNull JTextComponent component, @NonNull MoveCaretsOrigin origin) {
        Parameters.notNull("origin", origin);
        if (origin == MoveCaretsOrigin.DEFAULT) {
            return component.getNavigationFilter();
        } else if (origin == MoveCaretsOrigin.DISABLE_FILTERS) {
            return null;
        }
        NavigationFilter navi = doGetNavigationFilter(component, origin.getActionType());
        // Note: a special delegator is returned, since the component's navigation filter queue
        // can be manipulated after call to getNavigationFilter. So if we would have returned the global filter instance directly,
        // the calling client may unknowingly bypass certain (global) filters registered after call to this method.
        // In other words, there are two possible insertion points into the navigation filter chanin
        return navi != null ? navi : getChainNavigationFilter(component);
    }

    /**
     * Variant of {@link #getNavigationFilter}, which does not default to chaining navigation
     * filter
     * @param origin operation specifier
     * @return navigation filter or {@code null}
     */
    @CheckForNull NavigationFilter getNavigationFilterNoDefault(@NonNull MoveCaretsOrigin origin) {
        if (origin == MoveCaretsOrigin.DEFAULT) {
            return component.getNavigationFilter();
        } else if (origin == MoveCaretsOrigin.DISABLE_FILTERS) {
            return null;
        }
        NavigationFilter navi2 = doGetNavigationFilter(component, origin.getActionType());
        return navi2 != null ? navi2 : component.getNavigationFilter();
    }

    /**
     * Bottom navigation filter which delegates to the main NavigationFilter in the
     * Component (if it exists).
     */
    private static class ChainNavigationFilter extends NavigationFilter {
        private final JTextComponent component;

        public ChainNavigationFilter(JTextComponent component) {
            this.component = component;
        }
        
        @Override
        public int getNextVisualPositionFrom(JTextComponent text, int pos, Position.Bias bias, int direction, Position.Bias[] biasRet) throws BadLocationException {
            NavigationFilter chain = component.getNavigationFilter();
            return chain != null ? chain.getNextVisualPositionFrom(text, pos, bias, direction, biasRet) : super.getNextVisualPositionFrom(text, pos, bias, direction, biasRet);
        }

        @Override
        public void moveDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
            NavigationFilter chain = component.getNavigationFilter();
            if (chain != null) {
                chain.moveDot(fb, dot, bias);
            } else {
                super.moveDot(fb, dot, bias);
            }
        }

        @Override
        public void setDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
            NavigationFilter chain = component.getNavigationFilter();
            if (chain != null) {
                chain.setDot(fb, dot, bias);
            } else {
                super.setDot(fb, dot, bias);
            }
        }
    }
    
    /**
     * Sets navigation filter for a certain operation type, defined by {@link MoveCaretsOrigin}.
     * <p>
     * The registered filter will receive <b>only those caret movements</b>, which correspond to the
     * passed {@link MoveCaretsOrigin}. To receive all caret movements, register for {@link MoveCaretsOrigin#DEFAULT} 
     * or use {@link JTextComponent#setNavigationFilter}.
     * </p><p>
     * All the key part(s) of MoveCaretOrigin of a caret operation and `origin' parameter in this function must
     * match in order for the filter to be invoked.
     * </p><p>
     * The NavigationFilter implementation <b>may downcast</b> the passed {@link NavigationFilter.FilterBypass FilterBypass}
     * parameter to {@link NavigationFilterBypass} to get full infomration about the movement. 
     * </p>
     * @param component the component which will use the filter
     * @param origin the origin
     * @param naviFilter the installed filter
     * @see JTextComponent#setNavigationFilter
     * @see NavigationFilterBypass
     * @since 2.10
     */
    public static void setNavigationFilter(JTextComponent component, MoveCaretsOrigin origin, @NullAllowed NavigationFilter naviFilter) {
        if (origin == null) {
            origin = MoveCaretsOrigin.DEFAULT;
        }
        final NavigationFilter prev = getNavigationFilter(component, origin);
        if (naviFilter != null) {
            // Note:
            // if the caller passes in a non-cascading filter, we would loose the filter chain information.
            // the alien filter is wrapped by CascadingNavigationFilter delegator, so the previous filter
            // link is preserved.
            if (!(naviFilter instanceof CascadingNavigationFilter)) {
                final NavigationFilter del = naviFilter;
                naviFilter = new CascadingNavigationFilter() {
                    @Override
                    public void setDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
                        del.setDot(fb, dot, bias);
                    }

                    @Override
                    public void moveDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
                        del.moveDot(fb, dot, bias);
                    }

                    @Override
                    public int getNextVisualPositionFrom(JTextComponent text, int pos, Position.Bias bias, int direction, Position.Bias[] biasRet) throws BadLocationException {
                        return del.getNextVisualPositionFrom(text, pos, bias, direction, biasRet);
                    }
                };
            }
            ((CascadingNavigationFilter)naviFilter).setOwnerAndPrevious(component, origin, prev);
        }
        if (MoveCaretsOrigin.DEFAULT == origin) {
            component.setNavigationFilter(naviFilter);
        } else {
            doPutNavigationFilter(component, origin.getActionType(), prev);
        }
    }
    
    private static NavigationFilter getChainNavigationFilter(JTextComponent component) {
        NavigationFilter chain = (NavigationFilter)component.getClientProperty(CHAIN_FILTER_PROPERTY);
        if (chain == null) {
            component.putClientProperty(CHAIN_FILTER_PROPERTY, chain = new ChainNavigationFilter(component));
        }
        return chain;
    }
    
    /**
     * Records the navigation filter. Note that the filter is stored in the JTextComponent rather than
     * in this Caret. If the Component's UI changes or the caret is recreated for some reason, the 
     * navigation filters remain registered.
     * 
     * @param type type of nav filter
     * @param n the filter instance
     */
    private static void doPutNavigationFilter(JTextComponent component, String type, NavigationFilter n) {
        if (component == null) {
            throw new IllegalStateException("Not attached to a Component");
        }
        Map<String, NavigationFilter> m = (Map<String, NavigationFilter>)component.getClientProperty(NAVIGATION_FILTER_PROPERTY);
        if (m == null) {
            if (n == null) {
                return;
            }
            m = new HashMap<>();
            component.putClientProperty(NAVIGATION_FILTER_PROPERTY, m);
        } 
        if (n == null) {
            m.remove(type);
        } else {
            m.put(type, n);
        }
    }
    
    private static NavigationFilter doGetNavigationFilter(JTextComponent component, String n) {
        if (component == null) {
            throw new IllegalStateException("Not attached to a Component");
        }
        Map<String, NavigationFilter> m = (Map<String, NavigationFilter>)component.getClientProperty(NAVIGATION_FILTER_PROPERTY);
        return m == null ? null : m.get(n);
    }

    @Override
    public int getBlinkRate() {
        synchronized (listenerList) {
            return blinkDefaultDelay;
        }
    }

    /**
     * 
     */
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

    /**
     * 
     */
    void updateRectangularUpDownSelection() {
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
     * @param ctrl true for ctrl pressed.
     */
    void extendRectangularSelection(boolean toRight, boolean ctrl) {
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
                LineDocument lineDoc = LineDocumentUtils.asRequired(doc, LineDocument.class);
                int numVirtualChars = 8; // Number of virtual characters per one Ctrl+Shift+Arrow press
                if (toRight) {
                    if (rsDotRect.x < newlineRect.x) {
                        newDotOffset = Math.min(LineDocumentUtils.getNextWordStart(lineDoc, dotOffset), lineElement.getEndOffset() - 1);
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
                        newDotOffset = Math.max(LineDocumentUtils.getPreviousWordStart(lineDoc, dotOffset), lineElement.getStartOffset());
                    }
                }
            }

            if (newDotOffset != -1) {
                rsDotRect = c.modelToView(newDotOffset);
                moveDot(newDotOffset); // updates rs and fires state change
            } else {
                updateRectangularSelectionPaintRect();
                fireStateChanged(null);
            }
        } catch (BadLocationException ex) {
            // Leave selection as is
        }
    }
    
    
    // Private implementation
    
    /** This method should only be accessed by transaction's methods */
    GapList<CaretItem> getCaretItems() {
        return caretItems; // No sync as this should only be accessed by transaction's methods
    }
    
    /** This method should only be accessed by transaction's methods */
    GapList<CaretItem> getSortedCaretItems() {
        return sortedCaretItems; // No sync as this should only be accessed by transaction's methods
    }
    
    void ensureValidInfo(CaretItem caretItem) {
        synchronized (listenerList) {
            caretItem.ensureValidInfo();
        }
    }

    /** This method may be accessed arbitrarily */
    private CaretItem getLastCaretItem() {
        synchronized (listenerList) {
            return caretItems.get(caretItems.size() - 1);
        }
    }

    /**
     * Run a transaction to modify number of carets or their dots or selections.
     * @param removeType type of carets removal.
     * @param offset offset of document text removal otherwise the value is ignored.
     *  Internally this is also used for an end offset of the insertion at offset zero once it happens.
     * @param addCarets carets to be added if any.
     * @param moveHandler caret move handler or null if there's no caret moving. API client's move handlers
     *  are only invoked without any extra removals or additions so the original caret infos are used.
     *  Internal transactions may use caret additions or removals together with caret move handlers
     *  but they are also passed with original caret infos so the handlers must be aware of the removal
     *  and only pick the valid non-removed items.
     * @return 
     */
    private int runTransaction(CaretTransaction.RemoveType removeType, int offset, CaretItem[] addCarets, CaretMoveHandler moveHandler) {
        return runTransaction(removeType, offset, addCarets, moveHandler, MoveCaretsOrigin.DEFAULT);
    }
    
    private int runTransaction(CaretTransaction.RemoveType removeType, int offset, CaretItem[] addCarets, CaretMoveHandler moveHandler, MoveCaretsOrigin org) {
        lock();
        try {
            if (activeTransaction == null) {
                JTextComponent c = component;
                Document d = activeDoc;
                if (c != null && d != null) {
                    activeTransaction = new CaretTransaction(this, c, d, org);
                    if (LOG.isLoggable(Level.FINE)) {
                        StringBuilder msgBuilder = new StringBuilder(200);
                        msgBuilder.append("EditorCaret.runTransaction: removeType=").append(removeType).
                                append(", offset=").append(offset);
                        if (addCarets != null) {
                            msgBuilder.append(", addCarets:");
                            for (int i = 0; i < addCarets.length; i++) {
                                msgBuilder.append("\n    [").append(i).append("]: ").append(addCarets[i]);
                            }
                        }
                        msgBuilder.append("\n    moveHandler=").append(moveHandler).append('\n');
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.log(Level.FINEST, msgBuilder.toString(), new Exception());
                        } else {
                            LOG.log(Level.FINE, msgBuilder.toString());
                        }
                    }
                    try {
                        activeTransaction.replaceCarets(removeType, offset, addCarets);
                        if (moveHandler != null) {
                            activeTransaction.runCaretMoveHandler(moveHandler);
                        }
                        activeTransaction.removeOverlappingRegions();
                        int diffCount = 0;
                        boolean inAtomicSectionL;
                        List<Position> expandFoldPositions;
                        synchronized (listenerList) {
                            inAtomicSectionL = inAtomicSection;
                            GapList<CaretItem> replaceItems = activeTransaction.getReplaceItems();
                            if (replaceItems != null) {
                                caretItems = replaceItems;
                                diffCount = replaceItems.size() - caretItems.size();
                                sortedCaretItems = activeTransaction.getSortedCaretItems();
                                assert (sortedCaretItems != null) : "Null sortedCaretItems! removeType=" + removeType; // NOI18N
                            }
                            boolean chg = false;
                            if (activeTransaction.isDotOrStructuralChange()) {
                                caretInfos = null;
                                sortedCaretInfos = null;
                                if (inAtomicSectionL) {
                                    atomicSectionAnyCaretChange = true;
                                }
                                chg = true;
                            } else if (activeTransaction.isMagicPosChange()) {
                                caretInfos = null;
                                sortedCaretInfos = null;
                                chg = true;
                            }
                            if (chg) {
                                for (CaretItem caretItem : caretItems) {
                                    if (caretItem.getAndClearInfoObsolete()) {
                                        caretItem.clearInfo();
                                    }
                                }
                            }
                            scrollToLastCaret |= activeTransaction.isScrollToLastCaret();
                            expandFoldPositions = activeTransaction.expandFoldPositions();
                        }
                        // Repaint bounds of removed items
                        GapList<CaretItem> removedItems = activeTransaction.allRemovedItems();
                        if (removedItems != null) {
                            for (CaretItem removedItem : removedItems) {
                                removedItem.repaintIfShowing(c, "runTransaction-repaintRemovedItems", -1);
                            }
                        }
                        // Possibly update RS
                        if (rectangularSelection) {
                            if (activeTransaction.isAnyMarkChanged()) { // Was setDot() performed i.e. mark was reset?
                                setRectangularSelectionToDotAndMark();
                            } else {
                                try {
                                    Rectangle r = c.modelToView(getLastCaretItem().getDot());
                                    if (rsDotRect != null) {
                                        rsDotRect.y = r.y;
                                        rsDotRect.height = r.height;
                                    } else {
                                        rsDotRect = r;
                                    }
                                    updateRectangularSelectionPaintRect();
                                } catch (BadLocationException ex) {
                                    // Do not update RS dot rect
                                }
                            }
                        }
                        if (expandFoldPositions != null) {
                            CaretFoldExpander caretFoldExpander = CaretFoldExpander.get();
                            if (caretFoldExpander != null) {
                                caretFoldExpander.checkExpandFolds(c, expandFoldPositions);
                            }
                        }
                        boolean updateDispatched = false;
                        if (activeTransaction.isDotOrStructuralChange()) {
                            if (!inAtomicSectionL) {
                                // For now clear the lists and use old way TODO update to selective updating and rendering
                                fireStateChanged(activeTransaction.getOrigin());
                                dispatchUpdate(false);
                                updateDispatched = true;
                                resetBlink();
                            }
                        }
                        if (activeTransaction.isScrollToLastCaret() && !updateDispatched) {
                            dispatchUpdate(false);
                        }
                        return diffCount;
                    } finally {
                        activeTransaction = null;
                    }
                }
                return Integer.MIN_VALUE;
                
            } else { // Nested transaction - document insert/remove within transaction
                switch (removeType) {
                    case DOCUMENT_REMOVE:
                        activeTransaction.documentRemove(offset);
                        break;
                    case DOCUMENT_INSERT_ZERO_OFFSET:
                        activeTransaction.documentInsertAtZeroOffset(offset);
                        break;
                    default:
                        throw new AssertionError("Unsupported removeType=" + removeType + " in nested transaction"); // NOI18N
                }
                return 0;
            }
        } finally {
            unlock();
        }
    }
    
    private void updateRectangularSelectionDotRect() { // Assumes update caret bounds of getLastCaretItem()
        if (rectangularSelection) {
            Rectangle caretBounds = getLastCaretItem().getCaretBounds();
            if (caretBounds != null) {
                if (rsDotRect != null) {
                    rsDotRect.y = caretBounds.y;
                    rsDotRect.height = caretBounds.height;
                } else {
                    rsDotRect = caretBounds;
                }
            }
            updateRectangularSelectionPaintRect();
        }
    }
    
    private void fireEditorCaretChange(EditorCaretEvent evt) {
        for (EditorCaretListener listener : listenerList.getListeners()) {
            listener.caretChanged(evt);
        }
    }
    
    /**
     * Notifies listeners that caret position has changed.
     */
    private void fireStateChanged(final MoveCaretsOrigin origin) {
        Runnable runnable = new Runnable() {
            public @Override void run() {
                JTextComponent c = component;
                if (c == null || c.getCaret() != EditorCaret.this) {
                    return;
                }
                fireEditorCaretChange(new EditorCaretEvent(EditorCaret.this, 0, Integer.MAX_VALUE, origin)); // [TODO] temp firing without detailed info
                ChangeEvent evt = new ChangeEvent(EditorCaret.this);
                List<ChangeListener> listeners = changeListenerList.getListeners();
                for (ChangeListener l : listeners) {
                    l.stateChanged(evt);
                }
            }
        };
        
        // Always fire in EDT
        if (inAtomicUnlock) { // Cannot fire within atomic lock
            SwingUtilities.invokeLater(runnable);
        } else {
            ViewUtils.runInEDT(runnable);
        }
        updateSystemSelection();
    }

    private void lock() {
        Thread curThread = Thread.currentThread();
        try {
            synchronized (listenerList) {
                while (lockThread != null) {
                    if (curThread == lockThread) {
                        lockDepth++;
                        return;
                    }
                    listenerList.wait();
                }
                lockThread = curThread;
                lockDepth = 1;

            }
        } catch (InterruptedException e) {
            throw new Error("Interrupted attempt to acquire lock"); // NOI18N
        }
    }
    
    private void unlock() {
        Thread curThread = Thread.currentThread();
        synchronized (listenerList) {
            if (lockThread == curThread) {
                lockDepth--;
                if (lockDepth == 0) {
                    lockThread = null;
                    listenerList.notifyAll();
                }
            } else {
                throw new IllegalStateException("Invalid thread called EditorCaret.unlock():  thread=" + // NOI18N
                        curThread + ", lockThread=" + lockThread); // NOI18N
            }
        }
    }

    private void updateType() {
        final JTextComponent c = component;
        if (c != null) {
            CaretType newType;
            boolean cIsTextField = Boolean.TRUE.equals(c.getClientProperty("AsTextField"));
            
            if (cIsTextField) {
                newType = CaretType.THIN_LINE_CARET;
            } else if (prefs != null) {
                String newTypeStr;
                if (overwriteMode) {
                    newTypeStr = prefs.get(SimpleValueNames.CARET_TYPE_OVERWRITE_MODE, EditorPreferencesDefaults.defaultCaretTypeOverwriteMode);
                } else { // insert mode
                    newTypeStr = prefs.get(SimpleValueNames.CARET_TYPE_INSERT_MODE, EditorPreferencesDefaults.defaultCaretTypeInsertMode);
                    this.thickCaretWidth = prefs.getInt(SimpleValueNames.THICK_CARET_WIDTH, EditorPreferencesDefaults.defaultThickCaretWidth);
                }
                newType = CaretType.decode(newTypeStr);
            } else {
                newType = CaretType.THICK_LINE_CARET;
            }
            this.type = newType;

            String mimeType = DocumentUtilities.getMimeType(c);
            FontColorSettings fcs = (mimeType != null) ? MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class) : null;
            Color caretColor = Color.BLACK;
            if (fcs != null) {
                AttributeSet attribs = fcs.getFontColors(overwriteMode
                        ? FontColorNames.CARET_COLOR_OVERWRITE_MODE
                        : FontColorNames.CARET_COLOR_INSERT_MODE); //NOI18N
                if (attribs != null) {
                    caretColor = (Color) attribs.getAttribute(StyleConstants.Foreground);
                }
            }
            c.setCaretColor(caretColor);

            Preferences prefs = this.prefs;
            if (prefs != null) {
                int rate = prefs.getInt(SimpleValueNames.CARET_BLINK_RATE, -1);
                if (rate == -1) {
                    rate = EditorPreferencesDefaults.defaultCaretBlinkRate;
                }
                if (overrideCaretBlinkRate != null) {
                    rate = overrideCaretBlinkRate;
                }
                setBlinkRate(rate);
            }
        }
    }

    /**
     * Schedule recomputation of visual bounds of all carets.
     */
    private void requestUpdateAllCaretsBounds() {
        JTextComponent c = component;
        AbstractDocument doc;
        if (c != null && (doc = activeDoc) != null) {
            doc.readLock();
            try {
                List<CaretInfo> sortedCarets = getSortedCarets();
                for (CaretInfo caret : sortedCarets) {
                    caret.getCaretItem().markUpdateCaretBounds();
                }
            } finally {
                doc.readUnlock();
            }
        }
    }
    
    private void modelChanged(Document oldDoc, Document newDoc) {
        if (oldDoc != null) {
            // ideally the oldDoc param shouldn't exist and only listenDoc should be used
            assert (oldDoc == activeDoc);

            DocumentUtilities.removeDocumentListener(
                    oldDoc, listenerImpl, DocumentListenerPriority.CARET_UPDATE);
            AtomicLockDocument oldAtomicDoc = LineDocumentUtils.as(oldDoc, AtomicLockDocument.class);
            if (oldAtomicDoc != null) {
                oldAtomicDoc.removeAtomicLockListener(listenerImpl);
            }

            activeDoc = null;
            if (prefs != null && weakPrefsListener != null) {
                prefs.removePreferenceChangeListener(weakPrefsListener);
            }
        }

        // EditorCaret only installs successfully into AbstractDocument based documents that carry a mime-type
        if (newDoc instanceof AbstractDocument) {
            String mimeType = DocumentUtilities.getMimeType(newDoc);
            activeDoc = (AbstractDocument) newDoc;
            /* Ensure that the caret's document listener will be fired AFTER the views hierarchy's
             * document listener so the views update prior the caret and the caret has correct modelToView information.
             * If the document is modified from non-EDT then the caret updating
             * will be done asynchronously from EDT.
             */
            DocumentUtilities.addDocumentListener(
                    newDoc, listenerImpl, DocumentListenerPriority.CARET_UPDATE);
            AtomicLockDocument newAtomicDoc = LineDocumentUtils.as(newDoc, AtomicLockDocument.class);
            if (newAtomicDoc != null) {
                newAtomicDoc.addAtomicLockListener(listenerImpl);
            }

            // #269262 when IME is used, deinstall and install method is invoked
            // so in such case, the existing position shoud be used
            // because newDoc.StartPosition() may be an incorrect position
            CaretInfo lastCaret = getLastCaret();
            Position dotPos = lastCaret.getDotPosition();
            if (dotPos == null) {
                dotPos = newDoc.getStartPosition();
            }

            // Set caret to zero position upon document change (DefaultCaret impl does this too)
            runTransaction(CaretTransaction.RemoveType.REMOVE_ALL_CARETS, 0,
                    new CaretItem[] { new CaretItem(this, dotPos, Position.Bias.Forward,
                            null, Position.Bias.Forward ) }, null);
            
            // Leave caretPos and markPos null => offset==0
            prefs = (mimeType != null) ? MimeLookup.getLookup(mimeType).lookup(Preferences.class) : null;
            if (prefs != null) {
                weakPrefsListener = WeakListeners.create(PreferenceChangeListener.class, listenerImpl, prefs);
                prefs.addPreferenceChangeListener(weakPrefsListener);
            }
            
            updateType();
        }
    }
    
    /** Update visual position of caret(s) */
    private void dispatchUpdate(boolean forceInvokeLater) {
        boolean alreadyPending;
        synchronized (listenerList) {
            alreadyPending = caretUpdatePending;
            caretUpdatePending = true;
        }
        if (!alreadyPending) {
            Runnable updateRunnable = new Runnable() {
                public @Override void run() {
                    AbstractDocument doc = activeDoc;
                    if (doc != null) {
                        doc.readLock();
                        try {
                            update(false);
                        } finally {
                            doc.readUnlock();
                        }
                    } else {
                        // #269262 avoid that update() is not invoked
                        synchronized (listenerList) {
                            caretUpdatePending = false;
                        }
                    }
                }
            };

            if (!forceInvokeLater && SwingUtilities.isEventDispatchThread()) {
                updateRunnable.run();
            } else {
                SwingUtilities.invokeLater(updateRunnable);
            }
        }
    }
    
    /**
     * Pixel distance of the caret, from the top of the editor view. Saved on the before caret position changes
     * when editorcaret.updateRetainsVisibleOnce (set by folding on the text component) is present. During update,
     * the distance is used to maintain caret position on screen, assuming view hierarchy changed (folds have collapsed).
     */
    private int lastCaretVisualOffset = -1;

    private boolean isWrapping() {
        // See o.n.modules.editor.lib2.view.DocumentViewOp.updateLineWrapType().
        Object lwt = null;
        if (component != null) {
            lwt = component.getClientProperty(SimpleValueNames.TEXT_LINE_WRAP);
            if (lwt == null) {
                lwt = component.getDocument().getProperty(SimpleValueNames.TEXT_LINE_WRAP);
            }
        }
        return (lwt instanceof String) && !"none".equals(lwt);
    }

    private JViewport getViewport() {
        Component parent = component.getParent();
        if (parent instanceof JLayeredPane) {
            parent = parent.getParent();
        }
        return (parent instanceof JViewport) ? (JViewport) parent : null;
    }

    /**
     * Update the caret's visual position.
     * <br>
     * The document is read-locked while calling this method.
     *
     * @param calledFromPaint whether update was called from {@link #paint(java.awt.Graphics) } method
     *  which means that it does not check component validity because the component might still
     *  not be valid but its bounds are already set properly.
     */
    private void update(boolean calledFromPaint) {
        synchronized (listenerList) {
            caretUpdatePending = false;
        }
        final JTextComponent c = component;
        if (c != null) {
            boolean forceUpdate = c.getClientProperty("editorcaret.updateRetainsVisibleOnce") != null;
            boolean log = LOG.isLoggable(Level.FINE);
            Rectangle editorRect;
            final JViewport viewport = getViewport();
            if (viewport != null) {
                editorRect = viewport.getViewRect();
            } else {
                Dimension size = c.getSize();
                editorRect = new Rectangle(0, 0, size.width, size.height);
            }
            if (forceUpdate) {
                Rectangle cbounds = getLastCaretItem().getCaretBounds();
                if (cbounds != null) {
                    // save relative position of the main caret
                    maybeSaveCaretOffset(cbounds);
                    if (log) {
                        LOG.fine("EditorCaret.update: forced:true, savedBounds=" + cbounds + ", relativeOffset=" + lastCaretVisualOffset + "\n"); // NOI18N
                    }
                }
            }
            if (!calledFromPaint && !c.isValid() /* && maintainVisible == null */) {
                updateLaterDuringPaint = true;
                return;
            }

            Document doc = c.getDocument();
            if (doc != null) {
                if (log) {
                    LOG.fine("EditorCaret.update: editorRect=" + editorRect + "\n"); // NOI18N
                }
                // Only update caret bounds of the carets that are currently showing on the screen.
                // Carets that are not visible on the screen are not updated at this point - they will be updated
                // in paint() method once a request for their painting arrives.
                LockedViewHierarchy lvh = ViewHierarchy.get(c).lock();
                try {
                    CaretItem lastCaretItem;
                    List<CaretInfo> sortedCarets;
                    boolean scroll;
                    synchronized (listenerList) {
                        lastCaretItem = getLastCaretItem();
                        sortedCarets = getSortedCarets();
                        scroll = scrollToLastCaret;
                        scrollToLastCaret = false;
                    }
                    if (lastCaretVisualOffset == -1) {
                        // wasn't able to save the visual offset, the caret was already off screen. Do not scroll just because of fold updates.
                        forceUpdate = false;
                        c.putClientProperty("editorcaret.updateRetainsVisibleOnce", null); // NOI18N
                    }
                    if (scroll || forceUpdate) {
                        Rectangle caretBounds;
                        Rectangle oldCaretBounds;
                        if (lastCaretItem.getAndClearUpdateCaretBounds()) {
                            caretBounds = lvh.modelToViewBounds(lastCaretItem.getDot(), Position.Bias.Forward);
                            oldCaretBounds = lastCaretItem.setCaretBoundsWithRepaint(caretBounds, c, "update()-last-for-scroll", -2);
                        } else {
                            caretBounds = lastCaretItem.getCaretBounds();
                            oldCaretBounds = caretBounds;
                        }
                        if (log) {
                            LOG.fine("EditorCaret.update: caretBounds=" + caretBounds + "\n"); // NOI18N
                            LOG.fine("EditorCaret.update: oldCaretBounds=" + oldCaretBounds + "\n"); // NOI18N
                        }
                        if (caretBounds != null) {
                            Rectangle scrollBounds = new Rectangle(caretBounds); // Must possibly be cloned upon change
                            if (viewport != null && isWrapping()) {
                                /* When wrapping, only scroll to the right if the caret is
                                decisively outside the wrapped area (e.g. on a very long unbreakable
                                word). Otherwise, always scroll back to the left. When typing such
                                that the caret goes from the end of one wrap line to the next, the
                                new caret position might be one or more characters away from the
                                first character on the wrap line, so a regular
                                scroll-to-make-the-caret-visible would not do the job. */
                                if (scrollBounds.x <= viewport.getExtentSize().width) {
                                    scrollBounds.x = 0;
                                    scrollBounds.width = 1;
                                    /* Avoid generating a drag-select as a result of the viewport
                                    being automatically scrolled back to x=0 as a result of the user
                                    clicking once to move the caret. */
                                    if (viewport.getViewPosition().x > 0 && getDot() == getMark()) {
                                        mouseState = MouseState.DEFAULT;
                                    }
                                }
                            }
                            // Only scroll the view for the LAST caret to be visible
                            // For null old bounds (likely at begining of component displayment) ensure that a possible
                            // horizontal scrollbar would not hide the caret so enlarge the scroll bounds by hscrollbar height.
                            if (oldCaretBounds == null && viewport != null) {
                                Component scrollPane = viewport.getParent();
                                if (scrollPane instanceof JScrollPane) {
                                    JScrollBar hScrollBar = ((JScrollPane) scrollPane).getHorizontalScrollBar();
                                    if (hScrollBar != null) {
                                        int hScrollBarHeight = hScrollBar.getPreferredSize().height;
                                        Dimension extentSize = ((JViewport) viewport).getExtentSize();
                                        // If the extent size is high enough then extend
                                        // the scroll region by extra vertical space
                                        if (extentSize.height >= caretBounds.height + hScrollBarHeight) {
                                            scrollBounds = new Rectangle(scrollBounds); // Clone
                                            scrollBounds.height += hScrollBarHeight;
                                        }
                                    }
                                }
                            }
                            if (forceUpdate && oldCaretBounds != null) {
                                c.putClientProperty("editorcaret.updateRetainsVisibleOnce", null);
                                if (lastCaretVisualOffset >= 0) {
                                    // change scroll to so that caret will maintain its relative position saved before the caret was changed.
                                    scrollBounds = new Rectangle(caretBounds.x, caretBounds.y - lastCaretVisualOffset, scrollBounds.width, editorRect.height);
                                }
                                lastCaretVisualOffset = -1;
                            }
                            if (editorRect == null || !editorRect.contains(scrollBounds)) {
                                Rectangle visibleBounds = c.getVisibleRect();
                                if (!forceUpdate) {
                                    if (// #219580: if the preceding if-block computed new scrollBounds, it cannot be offset yet more
                                            /* # 70915 !updateAfterFoldHierarchyChange && */ (caretBounds.y > visibleBounds.y + visibleBounds.height + caretBounds.height
                                            || caretBounds.y + caretBounds.height < visibleBounds.y - caretBounds.height)) {
                                        // Scroll into the middle
                                        scrollBounds.y -= (visibleBounds.height - caretBounds.height) / 2;
                                        scrollBounds.height = visibleBounds.height;
                                    }
                                }
                                // When typing on a longest line the size of the component may still not incorporate just performed insert
                                // at this point so schedule the scrolling for later.
                                Dimension size = c.getSize();
                                if (scrollBounds.x + scrollBounds.width <= size.width &&
                                    scrollBounds.y + scrollBounds.height <= size.height)
                                {
                                    c.scrollRectToVisible(scrollBounds);
                                } else {
                                    final Rectangle finalScrollBounds = scrollBounds;
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            c.scrollRectToVisible(finalScrollBounds);
                                        }
                                    });
                                }
                                // Schedule another update that will read the updated editorRect
                                dispatchUpdate(true);
                                return;
                            }
                            if (log && LOG.isLoggable(Level.FINER)) {
                                LOG.finer("EditorCaret.update: Scrolling to: " + scrollBounds + "\n");
                            }
                        }
                    }

                    int editorRectStartOffset = lvh.yToParagraphStartOffset((editorRect != null) ? editorRect.y : 0);
                    int editorRectEndY = (editorRect != null)
                            ? (editorRect.y + editorRect.height)
                            : Integer.MAX_VALUE;
                    // Find the first caret to be painted
                    // Only paint carets that are part of the clip - use sorted carets
                    int low = 0;
                    int caretsSize = sortedCarets.size();
                    if (caretsSize > 1) {
                        int high = caretsSize - 1;
                        while (low <= high) {
                            int mid = (low + high) >>> 1;
                            CaretInfo midCaretInfo = sortedCarets.get(mid);
                            int midDot = midCaretInfo.getDot();
                            if (midDot < editorRectStartOffset) {
                                low = mid + 1;
                            } else if (midDot > editorRectStartOffset) {
                                high = mid - 1;
                            } else { // midDot == clipStartOffset
                                // There should not be multiple carets at the same offset so use the found one
                                low = mid;
                                break;
                            }
                        }
                    }
                    // Use "low" index (which is higher than "high" at end of binary-search)
                    for (int i = low; i < caretsSize; i++) {
                        CaretInfo caretInfo = sortedCarets.get(i);
                        CaretItem caretItem = caretInfo.getCaretItem();
                        Rectangle caretBounds = null;
                        if (caretItem.getAndClearUpdateCaretBounds()) {
                            caretBounds = lvh.modelToViewBounds(caretItem.getDot(), Position.Bias.Forward);
                            caretItem.setCaretBoundsWithRepaint(caretBounds, c, "EditorCaret.update()", i);                            
                        }
                        if (i > 0) {
                            if (caretBounds == null) {
                                caretBounds = caretItem.getCaretBounds();
                            }
                            if (caretBounds != null && caretBounds.y > editorRectEndY) {
                                break;
                            }
                        }
                    }
                } finally {
                    lvh.unlock();
                }
            }
        }
    }
    
    void invalidateCaretBounds(int startOffset, int endOffset) {
        List<CaretInfo> sortedCarets = getSortedCarets();
        int low = 0;
        int caretsSize = sortedCarets.size();
        if (startOffset > 0 && caretsSize > 1) {
            int high = caretsSize - 1;
            while (low <= high) {
                int mid = (low + high) >>> 1;
                CaretInfo midCaretInfo = sortedCarets.get(mid);
                int midMinOffset = midCaretInfo.getSelectionStart();
                if (midMinOffset < startOffset) {
                    low = mid + 1;
                } else if (midMinOffset > startOffset) {
                    high = mid - 1;
                } else { // midDot == clipStartOffset
                    // There should not be multiple carets at the same offset so use the found one
                    low = mid;
                    break;
                }
            }
        }
        // Use "low" index (which is higher than "high" at end of binary-search)
        for (int i = low; i < caretsSize; i++) {
            CaretInfo caretInfo = sortedCarets.get(i);
            CaretItem caretItem = caretInfo.getCaretItem();
            if (caretInfo.getSelectionStart() > endOffset) {
                break;
            }
            caretItem.markUpdateCaretBounds();
        }
    }

    private void updateSystemSelection() {
        if(component == null) return;
        Clipboard clip = null;
        try {
            clip = component.getToolkit().getSystemSelection();
        } catch (SecurityException ex) {
            // XXX: ignore for now, there is no ExClipboard for SystemSelection Clipboard
        }
        if(clip != null) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            List<CaretInfo> sortedCarets = getSortedCarets();
            for (CaretInfo caret : sortedCarets) {
                CaretItem caretItem = caret.getCaretItem();
                if(caretItem.isSelection()) {
                    if(!first) {
                        builder.append("\n");
                    } else {
                        first = false;
                    }
                    builder.append(getSelectedText(caretItem));
                }
            }
            if(builder.length() > 0) {
                clip.setContents(new java.awt.datatransfer.StringSelection(builder.toString()), null);
            }
        }
    }
    
    /**
     * Returns the selected text contained for this
     * <code>Caret</code>.  If the selection is
     * <code>null</code> or the document empty, returns <code>null</code>.
     *
     * @param caret
     * @return the text
     * @exception IllegalArgumentException if the selection doesn't
     *  have a valid mapping into the document for some reason
     */
    private String getSelectedText(CaretItem caret) {
        String txt = null;
        int p0 = Math.min(caret.getDot(), caret.getMark());
        int p1 = Math.max(caret.getDot(), caret.getMark());
        if (p0 != p1) {
            try {
                Document doc = component.getDocument();
                txt = doc.getText(p0, p1 - p0);
            } catch (BadLocationException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        return txt;
    }

    private void updateRectangularSelectionPositionBlocks() {
        JTextComponent c = component;
        if (rectangularSelection) {
            AbstractDocument doc = activeDoc;
            if (doc != null) {
                doc.readLock();
                try {
                    if (rsRegions == null) {
                        rsRegions = new ArrayList<Position>();
                        component.putClientProperty(RECTANGULAR_SELECTION_REGIONS_PROPERTY, rsRegions);
                    }
                    synchronized (rsRegions) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("EditorCaret.updateRectangularSelectionPositionBlocks: position regions:\n");
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
                                        int tmp = startOffset;
                                        startOffset = endOffset;
                                        endOffset = tmp;
                                    }
                                    Position startPos = activeDoc.createPosition(startOffset);
                                    Position endPos = activeDoc.createPosition(endOffset);
                                    rsRegions.add(startPos);
                                    rsRegions.add(endPos);
                                    if (LOG.isLoggable(Level.FINE)) {
                                        LOG.fine("    <" + startOffset + "," + endOffset + ">\n");
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
                    doc.readUnlock();
                }
            }
        }
    }
    
    private String dumpVisibility() {
        return "visible=" + isVisible() + ", showing=" + showing;
    }

    /*private*/ void resetBlink() {
        boolean visible = isVisible();
        synchronized (listenerList) {
            if (blinkTimer != null) {
                blinkTimer.stop();
                lastBlinkTime = System.currentTimeMillis();
                if (visible) {
                    if (LOG.isLoggable(Level.FINER)){
                        LOG.finer("EditorCaret.resetBlink: Reset blinking (caret already visible)" + // NOI18N
                                " - starting the caret blinking timer: " + dumpVisibility() + '\n'); // NOI18N
                    }
                    setShowing(true);
                    blinkTimer.start();
                } else {
                    if (LOG.isLoggable(Level.FINER)){
                        LOG.finer("EditorCaret.resetBlink: Reset blinking (caret not visible)" + // NOI18N
                                " - caret blinking timer not started: " + dumpVisibility() + '\n'); // NOI18N
                    }
                    setShowing(false);
                }
            }
        }
    }

    /**
     * Return true if the caret is visible and it should currently be painted on the screen.
     * This flag is being toggled by caret blinking timer.
     *
     * @return true if caret is currently painted on the screen.
     */
    boolean isShowing() {
        return showing;
    }

    /**
     * Set whether the carets should physically be showing on screen.
     * <br>
     * It's set to from true to false and vice versa by caret blinking timer.
     *
     * @param showing true if the carets should be showing on screen or false if not.
     */
    /*private*/ void setShowing(boolean showing) {
        synchronized (listenerList) {
            this.showing = showing;
        }
        updateOverwriteModeLayer(false);
    }
    
    private void updateOverwriteModeLayer(boolean forceUpdate) {
        JTextComponent c;
        if ((forceUpdate || overwriteMode) && (c = component) != null) {
            CaretOverwriteModeHighlighting overwriteModeHighlighting = (CaretOverwriteModeHighlighting)
                    c.getClientProperty(CaretOverwriteModeHighlighting.class);
            if (overwriteModeHighlighting != null) {
                overwriteModeHighlighting.setVisible(overwriteMode && visible && showing);
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
            fireStateChanged(null);
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
            LOG.fine("EditorCaret.selectEnsureMinSelection: mark=" + mark + ", dot=" + dot + ", newDot=" + newDot + "\n"); // NOI18N
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
                && (org.openide.util.Utilities.isMac() || (evt.getModifiers() & (InputEvent.META_MASK | InputEvent.ALT_MASK)) == 0));
    }
    
    private boolean isMiddleMouseButtonExt(MouseEvent evt) {
        return (evt.getButton() == MouseEvent.BUTTON2) &&
                (evt.getModifiersEx() & (InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK | /* cannot be tested bcs of bug in JDK InputEvent.ALT_DOWN_MASK | */ InputEvent.ALT_GRAPH_DOWN_MASK)) == 0;
    }

    private int mapDragOperationFromModifiers(MouseEvent e) {
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
    private boolean isDragPossible(MouseEvent e) {
	Object src = e.getSource();
	if (src instanceof JComponent) {
	    JComponent comp = (JComponent) src;
            boolean possible =  (comp == null) ? false : (comp.getTransferHandler() != null);
            if (possible && comp instanceof JTextComponent) {
                JTextComponent c = (JTextComponent) comp;
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
        }
        return false;
    }
    
    private void extendAtomicSectionChangeArea(int changeStartOffset, int changeEndOffset) {
        if (atomicSectionStartChangeOffset == Integer.MAX_VALUE) {
            atomicSectionStartChangeOffset = changeStartOffset;
            atomicSectionEndChangeOffset = changeEndOffset;
        } else {
            atomicSectionStartChangeOffset = Math.min(atomicSectionStartChangeOffset, changeStartOffset);
            atomicSectionEndChangeOffset = Math.max(atomicSectionEndChangeOffset, changeEndOffset);
        }
    }
    
    private static String logMouseEvent(MouseEvent evt) {
        return "x=" + evt.getX() + ", y=" + evt.getY() + ", clicks=" + evt.getClickCount() //NOI18N
            + ", component=" + s2s(evt.getComponent()) //NOI18N
            + ", source=" + s2s(evt.getSource()) + ", button=" + evt.getButton() + ", mods=" + evt.getModifiers() + ", modsEx=" + evt.getModifiersEx(); //NOI18N
    }

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }

    private final class ListenerImpl extends ComponentAdapter
    implements ActionListener, DocumentListener, AtomicLockListener, MouseListener,
            MouseMotionListener, FocusListener, ViewHierarchyListener,
            PropertyChangeListener, PreferenceChangeListener, KeyListener
    {

        ListenerImpl() {
        }

        @Override
        public void actionPerformed(ActionEvent e) { // Blinker timer fired
            JTextComponent c = component;
            if (c != null) {
                setShowing(!showing);
                List<CaretInfo> sortedCarets = getSortedCarets(); // TODO only repaint carets showing on screen
                int sortedCaretsSize = sortedCarets.size();
                for (int i = 0; i < sortedCaretsSize; i++) {
                    CaretInfo caret = sortedCarets.get(i);
                    CaretItem caretItem = caret.getCaretItem();
                    // Repaint either visible or invisible caret to visually toggle its state
                    caretItem.repaint(c, "EditorCaret: repaint-to-" + (isShowing() ? "show" : "hide"), i);
                }
                // Check if the system is responsive enough to blink at the current blink rate
                long tm = System.currentTimeMillis();
                if (tm - (blinkCurrentDelay + (blinkCurrentDelay >> 2)) > 0L) {
                    
                }
            }
        }
        
        public @Override void preferenceChange(PreferenceChangeEvent evt) {
            updateType();
        }

        public @Override void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            JTextComponent c = component;
            if ("document".equals(propName)) { // NOI18N
                if (c != null) {
                    modelChanged(activeDoc, c.getDocument());
                }

            } else if (EditorUtilities.CARET_OVERWRITE_MODE_PROPERTY.equals(propName)) {
                Boolean b = (Boolean) evt.getNewValue();
                overwriteMode = (b != null) ? b : false;
                updateOverwriteModeLayer(true);
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
                final JViewport viewport = getViewport();
                if (viewport != null) {
                    Component parent = viewport.getParent();
                    if (parent instanceof JScrollPane) {
                        JScrollPane scrollPane = (JScrollPane) parent;
                        JScrollBar hScrollBar = scrollPane.getHorizontalScrollBar();
                        if (hScrollBar != null) {
                            // Add weak listener so that editor pane could be removed
                            // from scrollpane without being held by scrollbar
                            hScrollBar.addComponentListener(
                                    (ComponentListener) WeakListeners.create(
                                            ComponentListener.class, listenerImpl, hScrollBar));
                        }
                    }
                }
            } else if ("enabled".equals(propName)) {
                Boolean enabled = (Boolean) evt.getNewValue();
                if (component.isFocusOwner()) {
                    if (enabled == Boolean.TRUE) {
                        if (component.isEditable()) {
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
//                        RectangularSelectionTransferHandler.install(component);

                    } else { // No rectangular selection
//                        RectangularSelectionTransferHandler.uninstall(component);
                    }
                    fireStateChanged(null);
                }
            }
        }

        // DocumentListener methods
        public @Override void insertUpdate(DocumentEvent evt) {
            JTextComponent c = component;
            if (c != null) {
                int offset = evt.getOffset();
                int length = evt.getLength();
                if (offset == 0) {
                    // Manually shift carets at offset zero - do this always even when inside atomic lock
                    runTransaction(CaretTransaction.RemoveType.DOCUMENT_INSERT_ZERO_OFFSET, evt.getLength(), null, null, MoveCaretsOrigin.DISABLE_FILTERS);
                }
                modifiedUpdate(evt, offset, offset + length, offset + length);
                
            }
        }

        public @Override void removeUpdate(DocumentEvent evt) {
            JTextComponent c = component;
            if (c != null) {
                int offset = evt.getOffset();
                runTransaction(CaretTransaction.RemoveType.DOCUMENT_REMOVE, offset, null, null);
                modifiedUpdate(evt, offset, offset, offset);
            }
        }

        public @Override void changedUpdate(DocumentEvent evt) {
        }

        public @Override
        void atomicLock(AtomicLockEvent evt) {
            synchronized (listenerList) {
                inAtomicSection = true;
                atomicSectionStartChangeOffset = Integer.MAX_VALUE;
                atomicSectionImplicitSetDotOffset = Integer.MAX_VALUE;
                atomicSectionAnyCaretChange = false;
            }
        }

        public @Override
        void atomicUnlock(AtomicLockEvent evt) {
            inAtomicUnlock = true;
            synchronized (listenerList) {
                inAtomicSection = false;
            }
            try {
                boolean change = atomicSectionAnyCaretChange;
                if (!change) { // For no explicit caret placement requests during the atomic transaction
                    if (atomicSectionImplicitSetDotOffset != Integer.MAX_VALUE) { // And if there were any modifications
                        implicitSetDot(null, atomicSectionImplicitSetDotOffset); // Request a setDot() on modification's boundary
                        change = true;
                    }
                }
                if (atomicSectionStartChangeOffset != Integer.MAX_VALUE) {
                    invalidateCaretBounds(atomicSectionStartChangeOffset, atomicSectionEndChangeOffset);
                    change = true;
                }
                if (change) {
                    updateAndFireChange();
                }
            } finally {
                inAtomicUnlock = false;
            }
        }

        private void modifiedUpdate(DocumentEvent evt, int offset, int endOffset, int setDotOffset) {
            // For typing modification ensure that the last caret will be visible after the typing modification.
            // Otherwise the caret would go off the screen when typing at the end of a long line.
            // It might make sense to make an implicit dot setting to end of the modification
            // but that would break existing tests like TypingCompletionUnitTest wihch expects
            // that even typing modifications do not influence the caret position (not only the non-typing ones).
            boolean typingModification = DocumentUtilities.isTypingModification(evt.getDocument());
            JTextComponent c = component;
            scrollToLastCaret |= typingModification && (c != null && c.hasFocus());

            if (!implicitSetDot(evt, setDotOffset)) {
                // Ensure that a valid atomicSectionImplicitSetDotOffset value
                // will be updated by the just performed document modification
                if (atomicSectionImplicitSetDotOffset != Integer.MAX_VALUE) {
                    atomicSectionImplicitSetDotOffset = DocumentUtilities.fixOffset(
                            atomicSectionImplicitSetDotOffset, evt);
                }
            }

            if (inAtomicSection) {
                extendAtomicSectionChangeArea(offset, endOffset);
            } else { // Not in atomic section
                invalidateCaretBounds(offset, endOffset);
                updateAndFireChange();
            }
        }
        
        private void updateAndFireChange() {
            dispatchUpdate(false);
            resetBlink();
            fireStateChanged(null);
        }

        /**
         * Either set the dot to the given or remember it later setting (when in atomic section).
         * Impose additional restrictions on whether the the given offset will be used as an implicit
         * dot or not.
         * 
         * @param evt document event or null when called in atomic-unlock.
         * @param offset offset to be used for implicit dot setting.
         * @return true if the given offset was used or false if not.
         */
        private boolean implicitSetDot(DocumentEvent evt, int offset) {
            if (evt == null || UndoRedoDocumentEventResolver.isUndoRedoEvent(evt)) {
                if (getCarets().size() == 1) { // And if there is just a single caret
                    boolean inActiveTransaction;
                    synchronized (listenerList) {
                        inActiveTransaction = (activeTransaction != null);
                    }
                    // Only set the dot implicitly if not already in an active transaction.
                    // Otherwise the caret framework would report illegal nested transaction.
                    // An explicit active transaction uses the new Caret API anyway
                    // so it should also use the proper caret position(s) saving for undo/redo too.
                    if (!inActiveTransaction) {
                        // Ensure no implicit setDot() for post-modification events
                        // (evt == null) for call within atomic unlock
                        if (evt == null || !DocumentPostModificationUtils.isPostModification(evt)) {
                            if (inAtomicSection) {
                                atomicSectionImplicitSetDotOffset = offset;
                            } else {
                                setDot(offset);
                            }
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        // MouseListener methods
        @Override
        public void mousePressed(MouseEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("EditorCaret.mousePressed: " + logMouseEvent(evt) + ", state=" + mouseState + '\n'); // NOI18N
            }

            final JTextComponent c = component;
            final AbstractDocument doc = activeDoc;
            if (c != null && doc != null && isLeftMouseButtonExt(evt)) {
                doc.readLock();
                try {
                    // Expand fold if offset is in collapsed fold
                    final int offset = mouse2Offset(evt);
                    switch (evt.getClickCount()) {
                        case 1: // Single press
                            if (c.isEnabled() && !c.hasFocus()) {
                                c.requestFocus();
                            }
                            c.setDragEnabled(!dndDisabled);
                            // Check Add Caret: Cmd+Shift+Click on Mac or Ctrl+Shift+Click on other platforms
                            if ((org.openide.util.Utilities.isMac() ? evt.isMetaDown() : evt.isControlDown()) &&
                                evt.isShiftDown())
                            {
                                // "Add multicaret" mode
                                mouseState = MouseState.DEFAULT;
                                try {
                                    Position pos = doc.createPosition(offset);
                                    //add/remove caret
                                    runTransaction(CaretTransaction.RemoveType.TOGGLE_CARET, 0,
                                            new CaretItem[]{new CaretItem(EditorCaret.this, pos, Position.Bias.Forward, pos, Position.Bias.Forward)}, null);
                                    evt.consume();
                                } catch (BadLocationException ex) {
                                    // Do nothing
                                }
                            } else if (evt.isShiftDown()) { // Select till offset
                                moveDot(offset);
                                setMagicCaretPosition(null);
                                adjustRectangularSelectionMouseX(evt.getX(), evt.getY()); // also fires state change
                                mouseState = MouseState.CHAR_SELECTION;
                            } else // Regular press
                            // check whether selection drag is possible
                            if (!dndDisabled && isDragPossible(evt) && mapDragOperationFromModifiers(evt) != TransferHandler.NONE) {
                                mouseState = MouseState.DRAG_SELECTION_POSSIBLE;
                            } else { // Drag not possible
                                mouseState = MouseState.CHAR_SELECTION;
                                setDot(offset);
                                setMagicCaretPosition(null);
                            }
                            break;

                        case 2: // double-click => word selection
                            mouseState = MouseState.WORD_SELECTION;
                            // Disable drag which would otherwise occur when mouse would be over text
                            c.setDragEnabled(false);
                            try {
                                boolean foldExpanded = false;
                                CaretFoldExpander caretFoldExpander = CaretFoldExpander.get();
                                if (caretFoldExpander != null) {
                                    foldExpanded = caretFoldExpander.checkExpandFold(c, evt.getPoint());
                                }
                                if (!foldExpanded) {
                                    if ((org.openide.util.Utilities.isMac() ? evt.isMetaDown() : evt.isControlDown()) && evt.isShiftDown())
                                    {
                                        mouseState = MouseState.DEFAULT;
                                        // Check if there's an existing caret at pos (created in single-click handler) and possibly select a word.
                                        // TBD: Possible unselecting of an existing word.
                                        final CaretItem[][] updatedCaretsRef = new CaretItem[1][];
                                        moveCarets(new CaretMoveHandler() {
                                            @Override
                                            public void moveCarets(CaretMoveContext context) {
                                                List<CaretInfo> sortedCarets = context.getOriginalSortedCarets();
                                                int sortedCaretsSize = sortedCarets.size();
                                                LineDocument lineDoc = LineDocumentUtils.asRequired(c.getDocument(), LineDocument.class);
                                                for (int i = 0; i < sortedCaretsSize; i++) {
                                                    CaretInfo caretInfo = sortedCarets.get(i);
                                                    int dot = caretInfo.getDot();
                                                    int mark = caretInfo.getMark();
                                                    if (dot == offset && mark == offset) { // Fresh caret added in single-click handler
                                                        try {
                                                            int wordStartOffset = LineDocumentUtils.getWordStart(lineDoc, offset);
                                                            int wordEndOffset = LineDocumentUtils.getWordEnd(lineDoc, offset);
                                                            Position wordStartPos = doc.createPosition(wordStartOffset);
                                                            Position wordEndPos = doc.createPosition(wordEndOffset);
                                                            context.setDotAndMark(caretInfo, wordEndPos, Position.Bias.Forward, wordStartPos, Position.Bias.Forward);
                                                            minSelectionStartOffset = wordStartOffset;
                                                            minSelectionEndOffset = wordEndOffset;
                                                        } catch (BadLocationException ex) {
                                                            // Do nothing
                                                        }
                                                        break;
                                                    } else if ((dot <= offset && offset <= mark) || (mark <= offset && offset <= dot)) {
                                                        if (sortedCaretsSize > 1) {
                                                            // Remove caret
                                                            CaretItem[] updatedCarets = new CaretItem[sortedCaretsSize - 1];
                                                            for (int j = 0; j < i; j++) {
                                                                updatedCarets[j] = sortedCarets.get(j).getCaretItem();
                                                            }
                                                            for (int j = i + 1, k = i; j < sortedCaretsSize; j++) {
                                                                updatedCarets[k++] = sortedCarets.get(j).getCaretItem();
                                                            }
                                                            updatedCaretsRef[0] = updatedCarets;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                        if (updatedCaretsRef[0] != null) {
                                            runTransaction(CaretTransaction.RemoveType.REMOVE_ALL_CARETS, 0, updatedCaretsRef[0], null);
                                        }

                                    } else {
                                        if (selectWordAction == null) {
                                            selectWordAction = EditorActionUtilities.getAction(
                                                    c.getUI().getEditorKit(c), DefaultEditorKit.selectWordAction);
                                        }
                                        if (selectWordAction != null) {
                                            selectWordAction.actionPerformed(null);
                                        }
                                        // Select word action selects forward i.e. dot > mark
                                        minSelectionStartOffset = getMark();
                                        minSelectionEndOffset = getDot();
                                    }
                                }
                            } catch (Exception ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            break;

                        case 3: // triple-click => line selection
                            mouseState = MouseState.LINE_SELECTION;
                            // Disable drag which would otherwise occur when mouse would be over text
                            c.setDragEnabled(false);
                            if (evt.isControlDown() && evt.isShiftDown()) {
                                // "Add multicaret" mode only with single click
                                mouseState = MouseState.DEFAULT;
                            } else {
                                if (selectLineAction == null) {
                                    selectLineAction = EditorActionUtilities.getAction(
                                                    c.getUI().getEditorKit(c), DefaultEditorKit.selectLineAction);
                                }
                                if (selectLineAction != null) {
                                    selectLineAction.actionPerformed(null);
                                    // Select word action selects forward i.e. dot > mark
                                    minSelectionStartOffset = getMark();
                                    minSelectionEndOffset = getDot();
                                }
                            }
                            break;

                        default: // multi-click
                    }
                } finally {
                    doc.readUnlock();
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("EditorCaret.mouseReleased: " + logMouseEvent(evt) + ", state=" + mouseState + '\n'); // NOI18N
            }

            int offset = mouse2Offset(evt);
            switch (mouseState) {
                case DRAG_SELECTION_POSSIBLE:
                    setDot(offset);
                    adjustRectangularSelectionMouseX(evt.getX(), evt.getY()); // also fires state change
                    break;

                case CHAR_SELECTION:
                    if (evt.isAltDown() && evt.isShiftDown()) {
                        moveDot(offset);
                    } else {
                        moveDot(offset); // Will do setDot() if no selection
                        adjustRectangularSelectionMouseX(evt.getX(), evt.getY()); // also fires state change
                    }
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
                LOG.fine("EditorCaret.mouseClicked: " + logMouseEvent(evt) + ", state=" + mouseState + '\n'); // NOI18N
            }

            JTextComponent c = component;
            if (c != null) {
                if (evt.getClickCount() == 1 && evt.isControlDown() && evt.isShiftDown()) {
                    evt.consume(); // consume event already handled by mousePressed
                }
                if (isMiddleMouseButtonExt(evt)) {
                    if (evt.getClickCount() == 1) {
                        if (c == null) {
                            return;
                        }
                        Clipboard buffer = component.getToolkit().getSystemSelection();

                        if (buffer == null) {
                            return;
                        }

                        Transferable trans = buffer.getContents(null);
                        if (trans == null) {
                            return;
                        }

                        final Document doc = c.getDocument();
                        if (doc == null) {
                            return;
                        }

                        final int offset = c.getUI().viewToModel(c, new Point(evt.getX(), evt.getY()));

                        try {
                            final String pastingString = (String) trans.getTransferData(DataFlavor.stringFlavor);
                            if (pastingString == null) {
                                return;
                            }
                            Runnable pasteRunnable = new Runnable() {
                                public @Override
                                void run() {
                                    try {
                                        doc.insertString(offset, pastingString, null);
                                        setDot(offset + pastingString.length());
                                        setMagicCaretPosition(null);
                                    } catch (BadLocationException exc) {
                                    }
                                }
                            };
                            AtomicLockDocument ald = LineDocumentUtils.as(doc, AtomicLockDocument.class);
                            if (ald != null) {
                                ald.runAtomic(pasteRunnable);
                            } else {
                                pasteRunnable.run();
                            }
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
                } else // stream selection
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
                LOG.fine("EditorCaret.mouseDragged: " + logMouseEvent(evt) + ", state=" + mouseState + '\n'); //NOI18N
            }

            if (isLeftMouseButtonExt(evt)) {
                JTextComponent c = component;
                int offset = mouse2Offset(evt);
                int dot = getDot();
                int mark = getMark();
                LineDocument lineDoc = LineDocumentUtils.asRequired(c.getDocument(), LineDocument.class);
                
                try {
                    switch (mouseState) {
                        case DEFAULT:
                        case DRAG_SELECTION:
                            break;

                        case DRAG_SELECTION_POSSIBLE:
                            mouseState = MouseState.DRAG_SELECTION;
                            break;

                        case CHAR_SELECTION:
                            if (evt.isAltDown() && evt.isShiftDown()) {
                                moveDot(offset);
                            } else {
                                moveDot(offset);
                                adjustRectangularSelectionMouseX(evt.getX(), evt.getY());
                            }
                            break; // Use the offset under mouse pointer

                        case WORD_SELECTION:
                            // Increase selection if at least in the middle of a word.
                            // It depends whether selection direction is from lower offsets upward or back.
                            if (offset >= mark) { // Selection extends forward.
                                offset = LineDocumentUtils.getWordEnd(lineDoc, offset);
                            } else { // Selection extends backward.
                                offset = LineDocumentUtils.getWordStart(lineDoc, offset);
                            }
                            selectEnsureMinSelection(mark, dot, offset);
                            break;

                        case LINE_SELECTION:
                            if (offset >= mark) { // Selection extends forward
                                offset = Math.min(LineDocumentUtils.getLineEnd(lineDoc, offset) + 1, c.getDocument().getLength());
                            } else { // Selection extends backward
                                offset = LineDocumentUtils.getLineStart(lineDoc, offset);
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

        // FocusListener methods
        public @Override void focusGained(FocusEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(
                        "EditorCaret.focusGained: doc=" + // NOI18N
                        component.getDocument().getProperty(Document.TitleProperty) + '\n'
                );
            }
            
            JTextComponent c = component;
            if (c != null) {
                if (component.isEnabled()) {
                    if (component.isEditable()) {
                        setVisible(true);
                }
                    setSelectionVisible(true);
                }
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("    Caret visibility: " + isVisible() + '\n'); // NOI18N
                }
            } else {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("    Text component is null, caret will not be visible" + '\n'); // NOI18N
                }
            }
        }

        public @Override void focusLost(FocusEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("EditorCaret.focusLost: doc=" + // NOI18N
                        component.getDocument().getProperty(Document.TitleProperty) +
                        "\nFOCUS GAINER: " + evt.getOppositeComponent() + '\n' // NOI18N
                );
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("    FOCUS EVENT: " + evt + '\n'); // NOI18N
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
/* The following should already be handled in update()            
            if (hScrollBar != component) { // really called for horizontal scrollbar
                Component scrollPane = hScrollBar.getParent();
                boolean needsScroll = false;
                List<CaretInfo> sortedCarets = getSortedCarets();
                for (CaretInfo caret : sortedCarets) { // TODO This is wrong, but a quick prototype
                    CaretItem caretItem = caret.getCaretItem();
                    if (caretItem.getCaretBounds() != null && scrollPane instanceof JScrollPane) {
                        Rectangle viewRect = ((JScrollPane)scrollPane).getViewport().getViewRect();
                        Rectangle hScrollBarRect = new Rectangle(
                                viewRect.x,
                                viewRect.y + viewRect.height,
                                hScrollBar.getWidth(),
                                hScrollBar.getHeight()
                                );
                        if (hScrollBarRect.intersects(caretItem.getCaretBounds())) {
                            // Update caret's position
                            needsScroll = true;
                        }
                    }
                }
                if (needsScroll) {
                    resetBlink();
                    dispatchUpdate(false);
                }
            }
*/
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
                CaretItem caret = getLastCaretItem();
                if (caret.getCaretBounds() == null) {
                    dispatchUpdate(false);
                    resetBlink();
                    if (caret.getCaretBounds() != null) { // detach the listener - no longer necessary
                        c.removeComponentListener(this);
                    }
                }
            }
        }

        @Override
        public void viewHierarchyChanged(ViewHierarchyEvent evt) {
            int changeStartOffset = evt.changeStartOffset();
            int changeEndOffset = evt.isChangeY() ? Integer.MAX_VALUE : evt.changeEndOffset();
            // Doc should be read/write-locked here (when firing view hierarchy change)
            if (inAtomicSection) {
                extendAtomicSectionChangeArea(changeStartOffset, changeEndOffset);
            } else {
                invalidateCaretBounds(changeStartOffset, changeEndOffset);
            }
            dispatchUpdate(true); // Schedule an update later
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                // Retain just the last caret
                retainLastCaretOnly();
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

    } // End of ListenerImpl class
    
    private enum CaretType {
        
        /**
         * Two-pixel line caret (the default).
         */
        THICK_LINE_CARET,
        
        /**
         * Thin one-pixel line caret.
         */
        THIN_LINE_CARET,

        /**
         * Rectangle corresponding to a single character.
         */
        BLOCK_CARET;
        
        static CaretType decode(String typeStr) {
            switch (typeStr) {
                case EditorPreferencesDefaults.THIN_LINE_CARET:
                    return THIN_LINE_CARET;
                case EditorPreferencesDefaults.BLOCK_CARET:
                    return BLOCK_CARET;
                default:
                    return THICK_LINE_CARET;
            }
        };

    }
    
    private static enum MouseState {

        DEFAULT, // Mouse released; not extending any selection
        CHAR_SELECTION, // Extending character selection after single mouse press 
        WORD_SELECTION, // Extending word selection after double-click when mouse button still pressed
        LINE_SELECTION, // Extending line selection after triple-click when mouse button still pressed
        DRAG_SELECTION_POSSIBLE,  // There was a selected text when mouse press arrived so drag is possible
        DRAG_SELECTION  // Drag is being done (text selection existed at the mouse press)
        
    }
    
}

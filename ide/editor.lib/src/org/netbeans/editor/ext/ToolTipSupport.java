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

import org.netbeans.api.editor.StickyWindowSupport;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLayeredPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.TextAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.GlyphGutter;
import org.netbeans.editor.PopupManager;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.WeakTimerListener;
import org.netbeans.modules.editor.lib.EditorExtPackageAccessor;
import org.openide.modules.PatchedPublic;

/**
 * Support for editor tooltips. Once the user stops moving the mouse
 * for the {@link #INITIAL_DELAY} milliseconds the enterTimer fires
 * and the {@link #updateToolTip()} method is called which searches
 * for the action named {@link ExtKit#buildToolTipAction} and if found
 * it executes it. The tooltips can be displayed by either calling
 * {@link #setToolTipText(java.lang.String)}
 * or {@link #setToolTip(javax.swing.JComponent)}.<BR>
 * However only one of the above ways should be used
 * not a combination of both because in such case
 * the text could be propagated in the previously set
 * custom tooltip component. 
 *
 * @author Miloslav Metelka, Vita Stejskal
 * @since 2.4
 */
public class ToolTipSupport {
    /* From PopupManager.SUPPRESS_POPUP_KEYBOARD_FORWARDING_CLIENT_PROPERTY_KEY. (For private use in
    this module only. */
    private static final String SUPPRESS_POPUP_KEYBOARD_FORWARDING_CLIENT_PROPERTY_KEY =
        "suppress-popup-keyboard-forwarding";

    // -J-Dorg.netbeans.editor.ext.ToolTipSupport.level=FINE
    private static final Logger LOG = Logger.getLogger(ToolTipSupport.class.getName());

    static {
        EditorExtPackageAccessor.register(new Accessor());
    }

    /** Property for the tooltip component change */
    public static final String PROP_TOOL_TIP = "toolTip"; // NOI18N

    /** Property for the tooltip text change */
    public static final String PROP_TOOL_TIP_TEXT = "toolTipText"; // NOI18N

    /** Property for the visibility status change. */
    public static final String PROP_STATUS = "status"; // NOI18N
    
    /** Property for the enabled flag change */
    public static final String PROP_ENABLED = "enabled"; // NOI18N

    /** Property for the initial delay change */
    public static final String PROP_INITIAL_DELAY = "initialDelay"; // NOI18N

    /** Property for the dismiss delay change */
    public static final String PROP_DISMISS_DELAY = "dismissDelay"; // NOI18N

    private static final String UI_PREFIX = "ToolTip"; // NOI18N

    /** Initial delay before the tooltip is shown in milliseconds. */
    public static final int INITIAL_DELAY = 200;

    /** Delay after which the tooltip will be hidden automatically
     * in milliseconds.
     */
    public static final int DISMISS_DELAY = 60000;
    
    /** Status indicating that  the tooltip is not showing on the screen. */
    public static final int STATUS_HIDDEN = 0;
    /** Status indicating that  the tooltip is not showing on the screen
     * but once either the {@link #setToolTipText(java.lang.String)}
     * or {@link #setToolTip(javax.swing.JComponent)} gets called
     * the tooltip will become visible.
     */
    public static final int STATUS_VISIBILITY_ENABLED = 1;
    /** Status indicating that the tooltip is visible
     * because {@link #setToolTipText(java.lang.String)}
     * was called.
     */
    public static final int STATUS_TEXT_VISIBLE = 2;
    /** Status indicating that the tooltip is visible
     * because {@link #setToolTip(javax.swing.JComponent)}
     * was called.
     */
    public static final int STATUS_COMPONENT_VISIBLE = 3;
    
    /** Extra height added to the rectangle of modelToView() for mouse
     * cursor coordinates.
     */
    private static final int MOUSE_EXTRA_HEIGHT = 5;

    private static final String HTML_PREFIX_LOWERCASE = "<html"; //NOI18N
    private static final String HTML_PREFIX_UPPERCASE = "<HTML"; //NOI18N

    private static final String LAST_TOOLTIP_POSITION = "ToolTipSupport.lastToolTipPosition"; //NOI18N
    private static final String MOUSE_MOVE_IGNORED_AREA = "ToolTipSupport.mouseMoveIgnoredArea"; //NOI18N
    private static final String MOUSE_LISTENER = "ToolTipSupport.noOpMouseListener"; //NOI18N

    private static final Action NO_ACTION = new TextAction("tooltip-no-action") { //NOI18N
        public @Override boolean isEnabled() {
            return false;
        }

        public @Override void actionPerformed(ActionEvent e) {
            // no-op
        }
    };

    private final Action HIDE_ACTION = new TextAction("tooltip-hide-action") { //NOI18N
        public @Override void actionPerformed(ActionEvent e) {
            ToolTipSupport.this.setToolTipVisible(false);
            JTextComponent jtc = extEditorUI.getComponent();
            if (jtc != null) {
                Utilities.requestFocus(jtc);
            }
        }
    };

    private static final MouseListener NO_OP_MOUSE_LISTENER = new MouseAdapter() {};

    /** @since 2.10 */
    public static final int FLAG_HIDE_ON_MOUSE_MOVE = 1;
    /** @since 2.10 */
    public static final int FLAG_HIDE_ON_TIMER = 2;
    /** @since 2.10 */
    public static final int FLAG_PERMANENT = 4;

    /** @since 2.10 */
    public static final int FLAGS_LIGHTWEIGHT_TOOLTIP = FLAG_HIDE_ON_MOUSE_MOVE | FLAG_HIDE_ON_TIMER;
    /** @since 2.10 */
    public static final int FLAGS_HEAVYWEIGHT_TOOLTIP = FLAG_PERMANENT;

    private static final String ELIPSIS = "..."; //NOI18N
    
    private final EditorUI extEditorUI;
    private final Timer enterTimer;
    private final Timer exitTimer;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Listener listener = new Listener();

    private boolean enabled;
    private MouseEvent lastMouseEvent;
    private boolean glyphListenerAdded = false;

    // The actual tooltip instance related information
    private int status; // Status of the tooltip visibility
    private JComponent toolTip;
    private String toolTipText;
    private PopupManager.HorizontalBounds horizontalBounds = PopupManager.ViewPortBounds;
    private PopupManager.Placement placement = PopupManager.AbovePreferred;
    private int verticalAdjustment;
    private int horizontalAdjustment;
    private int flags;

    /** Construct new support for tooltips.
     */
//    @SuppressWarnings({"OverridableMethodCallInConstructor", "LeakingThisInConstructor"}) //NOI18N
    @PatchedPublic
    /* package */ ToolTipSupport(EditorUI extEditorUI) {
        this.extEditorUI = extEditorUI;

        enterTimer = new Timer(INITIAL_DELAY, new WeakTimerListener(listener));
        enterTimer.setRepeats(false);
        exitTimer = new Timer(DISMISS_DELAY, new WeakTimerListener(listener));
        exitTimer.setRepeats(false);

        extEditorUI.addPropertyChangeListener(listener);

        setEnabled(true);
    }

    /** @return the component that either contains the tooltip
     * or is responsible for displaying of text tooltips.
     */
    public final JComponent getToolTip() {
        if (toolTip == null) {
            setToolTip(createDefaultToolTip());
        }

        return toolTip;
    }
    
    /** Set the tooltip component.
     * It can be called either to set the custom component
     * that will display the text tooltips or to display
     * the generic component with the tooltip after
     * the tooltip timer has fired.
     * @param toolTip component that either contains the tooltip
     *  or that will display a text tooltip.
     */
    public void setToolTip(JComponent toolTip) {
        setToolTip(toolTip, PopupManager.ViewPortBounds, PopupManager.AbovePreferred);
    }

    public void setToolTip(JComponent toolTip, PopupManager.HorizontalBounds horizontalBounds, PopupManager.Placement placement) {
        setToolTip(toolTip, PopupManager.ViewPortBounds, PopupManager.AbovePreferred, 0, 0);
    }
    
    public void setToolTip(
        JComponent toolTip,
        PopupManager.HorizontalBounds horizontalBounds,
        PopupManager.Placement placement,
        int horizontalAdjustment,
        int verticalAdjustment
    ) {
        setToolTip(toolTip, horizontalBounds, placement, horizontalAdjustment, verticalAdjustment, FLAGS_LIGHTWEIGHT_TOOLTIP);
    }

    /**
     * Sets the tooltip. The tooltip will be positioned at the given coordinates relative to either the scroller 
     * (with {@link PopupManager#ScrollBarBounds} or the viewport {@link PopupManager#ViewPortBounds}. Position
     * of caret in the editor is completely ignored.
     * 
     * @param toolTip the tooltip component
     * @param horizontalBounds positioning relative to the viewport or scrollbar (scroller)
     * @param placeAt x,y coordinates to place the tooltip
     * @param horizontalAdjustment horizontal inset between tooltip component and the tooltip floater
     * @param verticalAdjustment vertical inset between tooltip component and the tooltip floater
     * @param flags various flags, see FLAG_* constants in this class.
     * 
     * @since 3.26
     */
    public void setToolTip(
        JComponent toolTip,
        PopupManager.HorizontalBounds horizontalBounds,
        Point placeAt,
        int horizontalAdjustment,
        int verticalAdjustment,
        int flags
    ) {
        setToolTip(toolTip, horizontalBounds, PopupManager.FixedPoint, 
                placeAt, horizontalAdjustment, verticalAdjustment,
                flags);
    }
    
    /**
     * @since 2.10
     */
    public void setToolTip(
        JComponent toolTip,
        PopupManager.HorizontalBounds horizontalBounds,
        PopupManager.Placement placement,
        int horizontalAdjustment,
        int verticalAdjustment,
        int flags
    ) {
        setToolTip(toolTip, horizontalBounds, placement, 
                null, horizontalAdjustment, verticalAdjustment,
                flags);
    }
    
    private void setToolTip(
        JComponent toolTip,
        PopupManager.HorizontalBounds horizontalBounds,
        PopupManager.Placement placement,
        Point placeAt,
        int horizontalAdjustment,
        int verticalAdjustment,
        int flags
    ) {
        JComponent oldToolTip = this.toolTip;

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "setTooltip: {0}, horizontalBounds={1}, placement={2}, horizontalAdjustment={3}, verticalAdjustment={4}, flags={5}", new Object [] { //NOI18N
                toolTip, horizontalBounds, placement, horizontalAdjustment, verticalAdjustment, flags
            });
        }
        
        this.toolTip = toolTip;
        this.horizontalBounds = horizontalBounds;
        this.placement = placement;
        this.horizontalAdjustment = horizontalAdjustment;
        this.verticalAdjustment = verticalAdjustment;
        this.flags = flags;

        if (this.toolTip.getClientProperty(MOUSE_LISTENER) == null) {
            this.toolTip.putClientProperty(MOUSE_LISTENER, NO_OP_MOUSE_LISTENER);
            this.toolTip.addMouseListener(NO_OP_MOUSE_LISTENER);
        }

        if (status >= STATUS_VISIBILITY_ENABLED) {
            Point pt;
            
            if (placeAt != null) {
                pt = placeAt;
            } else if (oldToolTip == this.toolTip && this.toolTip.getClientProperty(LAST_TOOLTIP_POSITION) != null) {
                pt = (Point)this.toolTip.getClientProperty(LAST_TOOLTIP_POSITION);
            } else {
                pt = getLastMouseEventPoint();
            }
            ensureVisibility(pt);
        }

        firePropertyChange(PROP_TOOL_TIP, oldToolTip, this.toolTip);
    }
    
    /** Create the default tooltip component.
     */
    protected JComponent createDefaultToolTip() {
        return createTextToolTip(false);
    }

    private JEditorPane createHtmlTextToolTip() {
        class HtmlTextToolTip extends JEditorPane {
            public @Override void setSize(int width, int height) {
                Dimension prefSize = getPreferredSize();
                if (width >= prefSize.width) {
                    width = prefSize.width;
                } else { // smaller available width
                    super.setSize(width, 10000); // the height is unimportant
                    prefSize = getPreferredSize(); // re-read new pref width
                }
                if (height >= prefSize.height) { // enough height
                    height = prefSize.height;
                }
                super.setSize(width, height);
            }
            @Override
            public void setKeymap(Keymap map) {
                //#181722: keymaps are shared among components with the same UI
                //a default action will be set to the Keymap of this component below,
                //so it is necessary to use a Keymap that is not shared with other components
                super.setKeymap(addKeymap(null, map));
            }
        }

        JEditorPane tt = new HtmlTextToolTip();
        /* See NETBEANS-403. It still appears possible to use Escape to close the popup when the
        focus is in the editor. */
        tt.putClientProperty(SUPPRESS_POPUP_KEYBOARD_FORWARDING_CLIENT_PROPERTY_KEY, true);

        // setup tooltip keybindings
        filterBindings(tt.getActionMap());
        tt.getActionMap().put(HIDE_ACTION.getValue(Action.NAME), HIDE_ACTION);
        tt.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), HIDE_ACTION.getValue(Action.NAME));
        tt.getKeymap().setDefaultAction(NO_ACTION);

        Font font = UIManager.getFont(UI_PREFIX + ".font"); // NOI18N
        Color backColor = UIManager.getColor(UI_PREFIX + ".background"); // NOI18N
        Color foreColor = UIManager.getColor(UI_PREFIX + ".foreground"); // NOI18N

        if (font != null) {
            tt.setFont(font);
        }
        if (foreColor != null) {
            tt.setForeground(foreColor);
        }
        if (backColor != null) {
            tt.setBackground(backColor);
        }

        tt.setOpaque(true);
        tt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(tt.getForeground()),
            BorderFactory.createEmptyBorder(0, 3, 0, 3)
        ));
        tt.setContentType("text/html"); //NOI18N

        return tt;
    }
    
    private JTextArea createTextToolTip(final boolean wrapLines) {
        class TextToolTip extends JTextArea {
            public @Override void setSize(int width, int height) {
                Dimension prefSize = getPreferredSize();
                if (width >= prefSize.width) {
                    width = prefSize.width;
                } else { // smaller available width
                    // Set line wrapping and do super.setSize() to determine
                    // the real height (it will change due to line wrapping)
                    
                    if (wrapLines) {
                        setLineWrap(true);
                        setWrapStyleWord(true);
                    }
                    
                    super.setSize(width, 10000); // the height is unimportant
                    prefSize = getPreferredSize(); // re-read new pref width
                }
                if (height >= prefSize.height) { // enough height
                    height = prefSize.height;
                } else { // smaller available height
                    // Check how much can be displayed - cannot rely on line count
                    // because line wrapping may display single physical line
                    // into several visual lines
                    // Before using viewToModel() a setSize() must be called
                    // because otherwise the viewToModel() would return -1.
                    super.setSize(width, 10000);
                    int offset = viewToModel(new Point(0, height));
                    Document doc = getDocument();
                    try {
                        if (offset > ELIPSIS.length()) {
                            offset -= ELIPSIS.length();
                            doc.remove(offset, doc.getLength() - offset);
                            doc.insertString(offset, ELIPSIS, null);
                        }
                    } catch (BadLocationException ble) {
                        // "..." will likely not be displayed but otherwise should be ok
                    }
                    // Recalculate the prefSize as it may be smaller
                    // than the present preferred height
                    height = Math.min(height, getPreferredSize().height);
                }
                super.setSize(width, height);
            }
            @Override
            public void setKeymap(Keymap map) {
                //#181722: keymaps are shared among components with the same UI
                //a default action will be set to the Keymap of this component below,
                //so it is necessary to use a Keymap that is not shared with other JTextAreas
                super.setKeymap(addKeymap(null, map));
            }
        }

        JTextArea tt = new TextToolTip();
        /* See NETBEANS-403. It still appears possible to use Escape to close the popup when the
        focus is in the editor. */
        tt.putClientProperty(SUPPRESS_POPUP_KEYBOARD_FORWARDING_CLIENT_PROPERTY_KEY, true);

        // set up tooltip keybindings
        filterBindings(tt.getActionMap());
        tt.getActionMap().put(HIDE_ACTION.getValue(Action.NAME), HIDE_ACTION);
        tt.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), HIDE_ACTION.getValue(Action.NAME));
        tt.getKeymap().setDefaultAction(NO_ACTION);
        
        Font font = UIManager.getFont(UI_PREFIX + ".font"); // NOI18N
        Color backColor = UIManager.getColor(UI_PREFIX + ".background"); // NOI18N
        Color foreColor = UIManager.getColor(UI_PREFIX + ".foreground"); // NOI18N

        if (font != null) {
            tt.setFont(font);
        }
        if (foreColor != null) {
            tt.setForeground(foreColor);
        }
        if (backColor != null) {
            tt.setBackground(backColor);
        }

        tt.setOpaque(true);
        tt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(tt.getForeground()),
            BorderFactory.createEmptyBorder(0, 3, 0, 3)
        ));
        
        return tt;
    }

    private void disableSwingToolTip(final JComponent component) {
        javax.swing.SwingUtilities.invokeLater(
            new Runnable() {
                public @Override void run() {
                    // Prevent default swing tooltip manager
                    javax.swing.ToolTipManager.sharedInstance().unregisterComponent(component);
                    
                    // Also disable the swing tooltip manager on gutter component
                    GlyphGutter gg = extEditorUI.getGlyphGutter();
                    if (gg != null) {
                        javax.swing.ToolTipManager.sharedInstance().unregisterComponent(gg);
                    }
                }
            }
        );
    }
    
    /**
     * True, if the tooltip changed because of mouse event + timer; false, if it has been set
     * externally, e.g. by {@link #setToolTip}.
     */
    private boolean tooltipFromView = false;
    
    /** Update the tooltip by running corresponding action
     * {@link ExtKit#buildToolTipAction}. This method gets
     * called once the enterTimer fires and it can be overriden
     * by children.
     */
    protected void updateToolTip() {
        EditorUI ui = extEditorUI;
        if (ui == null)
            return;
        JTextComponent comp = ui.getComponent();
        if (comp == null)
            return;
        
        JComponent oldTooltip = this.toolTip;
        if (isGlyphGutterMouseEvent(lastMouseEvent)) {
            setToolTipText(extEditorUI.getGlyphGutter().getToolTipText(lastMouseEvent));
        } else { // over the text component
            BaseKit kit = Utilities.getKit(comp);
            if (kit != null) {
                Action a = kit.getActionByName(ExtKit.buildToolTipAction);
                if (a != null) {
                    a.actionPerformed(new ActionEvent(comp, 0, "")); // NOI18N
                }
            }
        }
        // tooltip has changed, mark it as 'automatic'
        if (this.toolTip != oldTooltip) {
            tooltipFromView = true;
        }
    }

    /** 
     * Set the visibility of the tooltip.
     *
     * @param visible whether tooltip should become visible or not.
     *   If true the status is changed to {@link #STATUS_VISIBILITY_ENABLED}
     *   and {@link #updateToolTip()} is called.
     *
     *   <p>It is still possible that the tooltip will not be showing
     *   on the screen in case the tooltip or tooltip text are left unchanged.
     *
     * @since 2.3
     */
    public void setToolTipVisible(boolean visible) {
        setToolTipVisible(visible, true);
    }
    
    /** 
     * Set the visibility of the tooltip.
     * Behaves like {@link #setToolTipVisible(boolean)}, except that it can skip
     * forwarding the tooltip request to the view hierarchy. This is useful when
     * tooltips are actively displayed by the code, not initiated by user's mouse hover
     * or gesture. This call should be then followed by a call to {@code setTooltip}
     * to actually set the tooltip's value and position.
     * <p>
     * Use {@link #setToolTipVisible(boolean)} to display a tooltip relevant to the
     * mouse position.
     * 
     * @param visible whether tooltip should become visible or not.
     * @param updateFromView if true, ask DocumentView to build a tooltip.
     *
     * @since 3.28
     */
    public final void setToolTipVisible(boolean visible, boolean updateFromView) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "setToolTipVisible: visible={0}, status={1}, enabled={2}", new Object [] { //NOI18N
                visible, status, enabled
            });
        }

        if (!visible) { // ensure the timers are stopped
            enterTimer.stop();
            exitTimer.stop();
        }

        if (visible && 
                (status < STATUS_VISIBILITY_ENABLED ||
                    // see defect #219141. The mouse-hoover tooltips should override those set explicitely even if they are visible.
                    (status >= STATUS_VISIBILITY_ENABLED && updateFromView && !tooltipFromView)
                )
            || !visible && status >= STATUS_VISIBILITY_ENABLED 
        ) {
            if (visible) { // try to show the tooltip
                if (enabled) {
                    setStatus(STATUS_VISIBILITY_ENABLED);
                    if (updateFromView) {
                        updateToolTip();
                    }
                }

            } else { // hide tip
                if (toolTip != null) {
                    if (toolTip.isVisible()){
                        toolTip.setVisible(false);
                        toolTip.putClientProperty(LAST_TOOLTIP_POSITION, null);
                        toolTip.putClientProperty(MOUSE_MOVE_IGNORED_AREA, null);
                        PopupManager pm = extEditorUI.getPopupManager();
                        if (pm!=null){
                            pm.uninstall(toolTip);
                        }
                    }
                    toolTip = null;
                }

                setStatus(STATUS_HIDDEN);
                tooltipFromView = false;
            }
        }
    }
    
    /** @return Whether the tooltip is showing on the screen.
     * {@link #getStatus() } gives the exact visibility state.
     */
    public boolean isToolTipVisible() {
        return status >= STATUS_VISIBILITY_ENABLED && toolTip != null;
    }

    private boolean isToolTipShowing() {
        return toolTip != null && toolTip.isShowing();
    }
    
    /** @return status of the tooltip visibility. It can
     * be {@link #STATUS_HIDDEN}
     * or {@link #STATUS_VISIBILITY_ENABLED}
     * or {@link #STATUS_TEXT_VISIBLE}
     * or {@link #STATUS_COMPONENT_VISIBLE}.
     */
    public final int getStatus() {
        return status;
    }
    
    private void setStatus(int status) {
        if (this.status != status) {
            int oldStatus = this.status;
            this.status = status;
            firePropertyChange(PROP_STATUS,
                Integer.valueOf(oldStatus), Integer.valueOf(this.status));
        }
    }

    /** @return the current tooltip text.
     */
    public String getToolTipText() {
        return toolTipText;
    }
    
    
    
    /**
     * Makes the given String displayble. Probably there doesn't exists
     * perfect solution for all situation. (someone prefer display those
     * squares for undisplayable chars, someone unicode placeholders). So lets
     * try do the best compromise.
     */
    private static String makeDisplayable(String str , Font f) {
        if( str == null || f == null){
            return str;
        }
        StringBuilder buf = new StringBuilder(str.length());
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '\t': buf.append(c); break;
                case '\n': buf.append(c); break;
                case '\r': buf.append(c); break;
                case '\b': buf.append("\\b"); break; // NOI18N
                case '\f': buf.append("\\f"); break; // NOI18N
                default:
                    if( f == null || f.canDisplay( c ) ){
                        buf.append(c);
                    } else {
                        buf.append("\\u"); // NOI18N
                        String hex = Integer.toHexString(c);
                        for (int j = 0; j < 4 - hex.length(); j++){
                            buf.append('0'); //NOI18N
                        }
                        buf.append(hex);
                    }
            }
        }
        return buf.toString();
    }
    
    /** Set the tooltip text to make the tooltip
     * to be shown on the screen.
     * @param text tooltip text to be displayed.
     */
    public void setToolTipText(String text) {
        
        final String displayableText = makeDisplayable(text, UIManager.getFont(UI_PREFIX + ".font")); //NOI18N
        
        Utilities.runInEventDispatchThread(new Runnable() {
            public @Override void run() {
                String oldText = toolTipText;
                toolTipText = displayableText;

                firePropertyChange(PROP_TOOL_TIP_TEXT,  oldText, toolTipText);
                
                if (toolTipText != null) {
                    if (toolTipText.startsWith(HTML_PREFIX_LOWERCASE) || toolTipText.startsWith(HTML_PREFIX_UPPERCASE)) {
                        JEditorPane jep = createHtmlTextToolTip();
                        jep.setText(toolTipText);
                        jep.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
                        setToolTip(jep);
                    } else {
// With the improved algorithm for placing popups we can have all text tooltips to wrap lines.
// Should this cause a problem please revert to the previouse state and have only singleline
// tooltips to wrap lines.
//                        boolean multiLineText = toolTipText.contains("\n"); //NOI18N
//                        JTextArea ta = createTextToolTip(!multiLineText);
                        JTextArea ta = createTextToolTip(true);
                        ta.setText(toolTipText);
                        setToolTip(ta);
                    }
                } else { // null text
                    if (status == STATUS_TEXT_VISIBLE) {
                        setToolTipVisible(false);
                    }
                }
            }
        });
    }
    
    private boolean isGlyphGutterMouseEvent(MouseEvent evt) {
        return (evt != null && evt.getSource() == extEditorUI.getGlyphGutter());
    }

    private void ensureVisibility(Point toolTipPosition) {
        LOG.log(Level.FINE, "toolTipPosition={0}", toolTipPosition); //NOI18N
        
        // Find the visual position in the document
        JTextComponent component = extEditorUI.getComponent();
        if (component != null) {
            // Try to display the tooltip above (or below) the line it corresponds to
            int pos = component.viewToModel(toolTipPosition);
            Rectangle cursorBounds = null;
            
            if (placement != PopupManager.FixedPoint && pos >= 0) {
                try {
                    cursorBounds = component.modelToView(pos);
                    extendBounds(cursorBounds);
                } catch (BadLocationException e) {
                    // ignore
                }
            }
            if (cursorBounds == null) { // get mose rect
                cursorBounds = new Rectangle(toolTipPosition, new Dimension(1, 1));
            }

            // updateToolTipBounds();
            PopupManager pm = extEditorUI.getPopupManager();
            
            if (toolTip != null && toolTip.isVisible()) {
                toolTip.setVisible(false);
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "model-pos={0}, cursorBounds={1}", new Object [] { pos, cursorBounds });
            }
            pm.install(toolTip, cursorBounds, placement, horizontalBounds, horizontalAdjustment, verticalAdjustment);
            if (toolTip != null) {
                toolTip.putClientProperty(LAST_TOOLTIP_POSITION, toolTipPosition);
                if (toolTip.getParent() != null) {
                    Rectangle blockBounds = null;
                    try {
                        int[] offsets = Utilities.getSelectionOrIdentifierBlock(component, pos);
                        if (offsets != null) {
                            Rectangle r1 = component.modelToView(offsets[0]);
                            Rectangle r2 = component.modelToView(offsets[1]);
                            blockBounds = new Rectangle(r1.x, r1.y, r2.x - r1.x, r1.height);
                        }
                    } catch (BadLocationException ble) {}
                    toolTip.putClientProperty(MOUSE_MOVE_IGNORED_AREA, computeMouseMoveIgnoredArea(
                            toolTip.getBounds(),
                            blockBounds != null ? SwingUtilities.convertRectangle(component, blockBounds, toolTip.getParent()) : null,
                            SwingUtilities.convertRectangle(component, cursorBounds, toolTip.getParent())));
                }
                toolTip.setVisible(true);
            }
        }
        exitTimer.restart();
    }

    private Rectangle extendBounds(Rectangle r) {
        if (horizontalBounds == PopupManager.ScrollBarBounds) {
            // no extending
        } else {
            if (placement == PopupManager.AbovePreferred || placement == PopupManager.Above) {
                // Enlarge the height slightly to not interfere with mouse cursor
                r.y -= MOUSE_EXTRA_HEIGHT;
                r.height += 2 * MOUSE_EXTRA_HEIGHT; // above and below
            } else if (placement == PopupManager.BelowPreferred || placement == PopupManager.Below) {
                r.y -= MOUSE_EXTRA_HEIGHT;
                r.height += 2 * MOUSE_EXTRA_HEIGHT; // above and below
            }
        }

        return r;
    }

    private Rectangle computeMouseMoveIgnoredArea(Rectangle toolTipBounds, Rectangle blockBounds, Rectangle cursorBounds) {
        Rectangle _toolTipBounds = new Rectangle(toolTipBounds);
        extendBounds(_toolTipBounds);
        
        Rectangle area = new Rectangle();
        Rectangle.union(_toolTipBounds, cursorBounds, area);
        if (blockBounds != null) {
            area.x = blockBounds.x;
            area.width = blockBounds.width;
        }
        area.x -= cursorBounds.width;
        area.width += 2 * cursorBounds.width;
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "toolTip.bounds={0}, blockBounds={1}, cursorBounds={2}, mouseMoveIgnoredArea={3}", new Object [] { _toolTipBounds, blockBounds, cursorBounds, area });
        }
        
        return area;
    }
    
    /** Helper method to get the identifier
     * under the mouse cursor.
     * @return string containing identifier under
     * mouse cursor.
     */
    public String getIdentifierUnderCursor() {
        String word = null;
        if (!isGlyphGutterMouseEvent(lastMouseEvent)) {
            try {
                JTextComponent component = extEditorUI.getComponent();
                BaseTextUI ui = (BaseTextUI)component.getUI();
                Point lmePoint = getLastMouseEventPoint();
                int pos = ui.viewToModel(component, lmePoint);
                if (pos >= 0) {
                    BaseDocument doc = (BaseDocument)component.getDocument();
                    int eolPos = Utilities.getRowEnd(doc, pos);
                    Rectangle eolRect = ui.modelToView(component, eolPos);
                    int lineHeight = extEditorUI.getLineHeight();
                    if (lmePoint.x <= eolRect.x && lmePoint.y <= eolRect.y + lineHeight) {
                        word = Utilities.getIdentifier(doc, pos);
                    }
                }
            } catch (BadLocationException e) {
                // word will be null
            }
        }

        return word;
    }

    /** @return whether the tooltip support is enabled. If it's
     * disabled the tooltip does not become visible.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /** Set whether the tooltip support is enabled. If it's
     * disabled the tooltip does not become visible.
     * @param enabled whether the tooltip will be enabled or not.
     */
    public void setEnabled(boolean enabled) {
        if (enabled != this.enabled) {
            this.enabled = enabled;

            firePropertyChange(PROP_ENABLED,
                enabled ? Boolean.FALSE : Boolean.TRUE,
                enabled ? Boolean.TRUE : Boolean.FALSE
            );

            if (!enabled) {
                setToolTipVisible(false);
            }
        }
    }

    /** @return the delay between stopping
     * mouse movement and displaying
     * of the tooltip in milliseconds.
     */
    public int getInitialDelay() {
        return enterTimer.getDelay();
    }

    /** Set the delay between stopping
     * mouse movement and displaying
     * of the tooltip in milliseconds.
     */
    public void setInitialDelay(int delay) {
        if (enterTimer.getDelay() != delay) {
            int oldDelay = enterTimer.getDelay();
            enterTimer.setDelay(delay);

            firePropertyChange(PROP_INITIAL_DELAY,
                Integer.valueOf(oldDelay), Integer.valueOf(enterTimer.getDelay()));
        }
    }

    /** @return the delay between displaying
     * of the tooltip and its automatic hiding
     * in milliseconds.
     */
    public int getDismissDelay() {
        return exitTimer.getDelay();
    }

    /** Set the delay between displaying
     * of the tooltip and its automatic hiding
     * in milliseconds.
     */
    public void setDismissDelay(int delay) {
        if (exitTimer.getDelay() != delay) {
            int oldDelay = exitTimer.getDelay();
            exitTimer.setDelay(delay);
            
            firePropertyChange(PROP_DISMISS_DELAY,
                Integer.valueOf(oldDelay), Integer.valueOf(exitTimer.getDelay()));
        }
    }

    /** @return last mouse event captured by this support.
     * This method can be used by the action that evaluates
     * the tooltip.
     */
    public final MouseEvent getLastMouseEvent() {
        return lastMouseEvent;
    }
    
    /** Possibly do translation when over the gutter.
     */
    private Point getLastMouseEventPoint() {
        Point p = null;
        MouseEvent lme = lastMouseEvent;
        if (lme != null) {
            p = lme.getPoint();
            if (lme.getSource() == extEditorUI.getGlyphGutter()) {
                // Over glyph gutter - change coords
                JTextComponent c = extEditorUI.getComponent();
                if (c != null) {
                    Container parent = c.getParent();
                    if (parent instanceof JLayeredPane) {
                        parent = parent.getParent();
                    }
                    if (parent instanceof JViewport) {
                        JViewport vp = (JViewport)parent;
                        p = new Point(vp.getViewPosition().x, p.y);
                    }
                }
            }
        }

        return p;
    }
                
                

    /** Called automatically when the
     * {@link javax.swing.JComponent#TOOL_TIP_TEXT_KEY}
     * property of the corresponding editor component
     * gets changed.<BR>
     * By default it calls {@link #setToolTipText(java.lang.String)}
     * with the new tooltip text of the component.
     */
    protected void componentToolTipTextChanged(PropertyChangeEvent evt) {
        JComponent component = (JComponent)evt.getSource();
        setToolTipText(component.getToolTipText());
    }

    /** Add the listener for the property changes. The names
     * of the supported properties are defined
     * as "PROP_" public static string constants.
     * @param listener listener to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    /** Fire the change of the given property.
     * @param propertyName name of the fired property
     * @param oldValue old value of the property
     * @param newValue new value of the property.
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    private static void filterBindings(ActionMap actionMap) {
        for(Object key : actionMap.allKeys()) {
            String actionName = key.toString().toLowerCase(Locale.ENGLISH);

            LOG.log(Level.FINER, "Action-name: {0}", actionName); //NOI18N
            if (actionName.contains("delete") || actionName.contains("insert") || //NOI18N
                actionName.contains("paste") || actionName.contains("default") || //NOI18N
                actionName.contains("cut") //NOI18N
            ) {
                actionMap.put(key, NO_ACTION);
            }
        }
    }

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }

    private final class Listener extends MouseAdapter implements MouseMotionListener, ActionListener, PropertyChangeListener, FocusListener, AncestorListener {

        // -------------------------------------------------------------------
        // PropertyChangeListener implementation
        // -------------------------------------------------------------------

        public @Override void propertyChange(PropertyChangeEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "propertyChange: source={0}, property={1}, old={2}, new={3}", new Object[] { s2s(evt.getSource()), evt.getPropertyName(), s2s(evt.getOldValue()), s2s(evt.getNewValue()) });
            }

            String propName = evt.getPropertyName();

            if (EditorUI.COMPONENT_PROPERTY.equals(propName)) {
                JTextComponent component = (JTextComponent)evt.getNewValue();
                if (component != null) { // just installed
                    component.addPropertyChangeListener(this);

                    disableSwingToolTip(component);

                    component.addAncestorListener(this);
                    component.addFocusListener(this);
                    if (component.hasFocus()) {
                        focusGained(new FocusEvent(component, FocusEvent.FOCUS_GAINED));
                    }
                    component.addMouseListener(this);
                    component.addMouseMotionListener(this);

                    GlyphGutter gg = extEditorUI.getGlyphGutter();
                    if (gg != null && !glyphListenerAdded) {
                        glyphListenerAdded = true;
                        gg.addMouseListener(this);
                        gg.addMouseMotionListener(this);
                    }

                } else if (null != (component = (JTextComponent)evt.getOldValue())) { // just deinstalled
                    component.removeAncestorListener(this);
                    component.removeFocusListener(this);
                    component.removePropertyChangeListener(this);

                    component.removeMouseListener(this);
                    component.removeMouseMotionListener(this);

                    GlyphGutter gg = extEditorUI.getGlyphGutter();
                    if (gg != null) {
                        gg.removeMouseListener(this);
                        gg.removeMouseMotionListener(this);
                    }
                    setToolTipVisible(false);
                }
            }

            if (JComponent.TOOL_TIP_TEXT_KEY.equals(propName)) {
                JComponent component = (JComponent)evt.getSource();
                disableSwingToolTip(component);

                componentToolTipTextChanged(evt);
            }

        }

        // -------------------------------------------------------------------
        // ActionListener implementation
        // -------------------------------------------------------------------

        public @Override void actionPerformed(ActionEvent evt) {
            if (evt.getSource() == enterTimer) {
                if (!isToolTipShowing() || (flags & FLAG_PERMANENT) == 0) {
                    setToolTipVisible(true);
                }

            } else if (evt.getSource() == exitTimer) {
                if (!isToolTipShowing() || (flags & FLAG_HIDE_ON_TIMER) != 0) {
                    setToolTipVisible(false);
                }
            }
        }

        // -------------------------------------------------------------------
        // MouseListener implementation
        // -------------------------------------------------------------------

        public @Override void mouseClicked(MouseEvent evt) {
            lastMouseEvent = evt;
            setToolTipVisible(false);
        }

        public @Override void mousePressed(MouseEvent evt) {
            lastMouseEvent = evt;
            setToolTipVisible(false);
        }

        public @Override void mouseReleased(MouseEvent evt) {
            lastMouseEvent = evt;
            setToolTipVisible(false);

            // Check that if a selection becomes visible by dragging a mouse
            // the tooltip evaluation should be posted.
            EditorUI ui = extEditorUI;
            if (ui != null) {
                JTextComponent component = ui.getComponent();
                if (enabled && component != null && Utilities.isSelectionShowing(component)) {
                    enterTimer.restart();
                }
            }
        }

        public @Override void mouseEntered(MouseEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "mouseEntered: x=" + evt.getX() + "; y=" + evt.getY()); //NOI18N
            }
            lastMouseEvent = evt;
        }

        public @Override void mouseExited(MouseEvent evt) {
            lastMouseEvent = evt;
            if (isToolTipShowing()) {
                Rectangle r = new Rectangle(toolTip.getLocationOnScreen(), toolTip.getSize());

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "mouseExited: screen-x=" + evt.getXOnScreen() + "; screen-y=" + evt.getYOnScreen() //NOI18N
                        + "; tooltip=" + r); //NOI18N
                }
                if (r.contains(evt.getLocationOnScreen())) {
                    // inside the tooltip component -> do not hide
                    return;
                }
            }

            if (!isToolTipShowing() || (flags & FLAG_HIDE_ON_MOUSE_MOVE) != 0) {
                setToolTipVisible(false);
            }
        }

        // -------------------------------------------------------------------
        // MouseMotionListener implementation
        // -------------------------------------------------------------------

        public @Override void mouseDragged(MouseEvent evt) {
            lastMouseEvent = evt;
            setToolTipVisible(false);
        }

        public @Override void mouseMoved(MouseEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "mouseMoved: x=" + evt.getX() + "; y=" + evt.getY() + "enabled=" + enabled + ", status=" + status + ", flags=" + flags); //NOI18N
            }

            if (toolTip != null) {
                Rectangle ignoredArea = (Rectangle) toolTip.getClientProperty(MOUSE_MOVE_IGNORED_AREA);
                Point mousePosition = SwingUtilities.convertPoint(evt.getComponent(), evt.getPoint(), toolTip.getParent());
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Mouse-Move-Ignored-Area=" + ignoredArea + "; mouse=" + mousePosition //NOI18N
                        + "; is-inside=" + (ignoredArea != null ? ignoredArea.contains(mousePosition) : null)); //NOI18N
                }
                if (ignoredArea != null && ignoredArea.contains(mousePosition)) {
                    return;
                }
            }

            if (!isToolTipShowing() || (flags & FLAG_HIDE_ON_MOUSE_MOVE) != 0) {
                setToolTipVisible(false);
            }
            
            if (enabled) {
                enterTimer.restart();
            }
            lastMouseEvent = evt;
        }

        // -------------------------------------------------------------------
        // FocusListener implementation
        // -------------------------------------------------------------------

        public @Override void focusGained(FocusEvent e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "focusGained: {0}", s2s(e.getComponent())); //NOI18N
            }
            GlyphGutter gg = extEditorUI.getGlyphGutter();
            if (gg != null && !glyphListenerAdded) {
                glyphListenerAdded = true;
                gg.addMouseListener(this);
                gg.addMouseMotionListener(this);
            }
        }

        public @Override void focusLost(FocusEvent e) {
            // no-op
        }

        // -------------------------------------------------------------------
        // AncestorListener implementation
        // -------------------------------------------------------------------

        public @Override void ancestorAdded(AncestorEvent event) {
            // no-op
        }

        public @Override void ancestorRemoved(AncestorEvent event) {
            LOG.log(Level.FINE, "ancestorRemoved: source={0}", s2s(event.getSource()));
            setToolTipVisible(false);
        }

        public @Override void ancestorMoved(AncestorEvent event) {
            // no-op
        }
    } // End of Listener class

    private static final class Accessor extends EditorExtPackageAccessor {

        @Override
        public ToolTipSupport createToolTipSupport(EditorUI eui) {
            return new ToolTipSupport(eui);
        }

    } // End of Accessor class

    // -----------------------------------------------------------------------
    // Methods accidentally exposed in public API
    // -----------------------------------------------------------------------

    private @PatchedPublic void mouseDragged(MouseEvent evt) {
        listener.mouseDragged(evt);
    }

    private @PatchedPublic void mouseMoved(MouseEvent evt) {
        listener.mouseMoved(evt);
    }

    private @PatchedPublic void mouseClicked(MouseEvent evt) {
        listener.mouseClicked(evt);
    }

    private @PatchedPublic void mousePressed(MouseEvent evt) {
        listener.mousePressed(evt);
    }

    private @PatchedPublic void mouseReleased(MouseEvent evt) {
        listener.mouseReleased(evt);
    }

    private @PatchedPublic void mouseEntered(MouseEvent evt) {
        listener.mouseEntered(evt);
    }

    private @PatchedPublic void mouseExited(MouseEvent evt) {
        listener.mouseExited(evt);
    }

    private @PatchedPublic void actionPerformed(ActionEvent e) {
        listener.actionPerformed(e);
    }

    private @PatchedPublic void propertyChange(PropertyChangeEvent evt) {
        listener.propertyChange(evt);
    }

    private @PatchedPublic void focusGained(FocusEvent e) {
        listener.focusGained(e);
    }

    private @PatchedPublic void focusLost(FocusEvent e) {
        listener.focusLost(e);
    }

}

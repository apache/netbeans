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
package org.netbeans.lib.terminalemulator;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent.AccessibleJComponent;
import javax.swing.*;
import javax.swing.text.Keymap;

/**
 * Term is a pure Java multi-purpose terminal emulator.
 * <p>
 * It has the following generic features:
 * <ul>
 *      <li>All "dumb" operations. Basically putting characters on a screen and
 *      processing keyboard input.</li>
 *      <li>ANSI mode "smart" operations. Cursor control etc.</li>
 *      <li>Character attributes like color, reverse-video etc.</li>
 *      <li>Selection service in character, word and line modes matching xterm
 *      with configurable word boundary detection.</li>
 *      <li>History buffer.</li>
 *      <li>Facilities to iterate through logical lines, to implement search for
 *      example.</li>
 *      <li>Support for nested pickable regions in order to support hyperlinked
 *      views or more complex active text utilities.</li>
 *      <li>Support for double-width Oriental characters.</li>
 * </ul>
 * <p>
 * <h2>Coordinate systems</h2>
 * The following coordinate systems are used with Term.
 * They are all cartesian and have their origin at the top left.
 * All but the first are 0-origin.
 * But they differ in all other respects:
 * <dl>
 *
 * <dt>ANSI Screen coordinates
 *      <dd>
 *      Address only the visible portion of the screen.
 *      They are 1-origin and extend thru the width and height of the visible
 *      portion of the screen per getColumns() and getRows().
 *      <p>
 *      This is how an application (like 'vi' etc) views the screen.
 *      This coordinate system primarily comes into play in the cursor addressing
 *      directive, op_cm() and otherwise is not really used in the implementation.
 * <p>
 *
 * <dt>Cell coordinates
 *      <dd>
 *      Each character usually takes one cell, and all placement on the screen
 *      is in terms of a grid of cells getColumns() wide This cellular nature
 *      is why fixed font is "required". In some locales some characters may
 *       be double-width.
 *      Japanese characters are like this, so they take up two cells.
 *      There are no double-height characters (that I know of).
 *      <p>
 *      Cursor motion is in cell coordinates, so to move past a Japanese character
 *      you need the cursor to move right twice. A cursor can also be placed on
 *      the second cell of a double-width character.
 *      <p>
 *      Note that this is strictly an internal coordinate system. For example
 *      Term.getCursorCol() and getCursorCoord() return buffer coordinates.
 *      <p>
 *      The main purpose of this coordinate system is to capture logical columns.
 *      In the vertical direction sometimes it extends only the height of the
 *      screen and sometimes the height of the buffer.
 * <p>
 *
 * <dt>Buffer coordinates ...
 *      <dd>
 *      ... address the whole history character buffer.
 *      These are 0-origin and extend thru the width
 *      of the screen per getColumns(), or more if horizontal scrolling is
 *      enabled, and the whole history, that is, getHistorySize()+getRows().
 *      <p>
 *      The BCoord class captures the value of such coordinates.
 *      It is more akin to the 'int offset' used in the Java text package
 *      as opposed to javax.swing.text.Position.
 *      <p>
 *      If there are no double-width characters the buffer coords pretty much
 *      overlap with cell coords. If double-width characters are added then
 *      the buffer column and cell column will have a larger skew the more right
 *      you go.
 * <p>
 * <dt>Absolute coordinates ...
 *      <dd>
 *      ... are like Buffer coordinates in the horizontal direction.
 *      In the vertical direction their origin is the first line that was
 *      sent to the terminal. This line might have scrolled out of history and
 *      might no longer be in the buffer. In effect each line ever printed by
 *      Term gets a unique Absolute row.
 *      <p>
 *      What good is this? The ActiveRegion mechanism maintains coordinates
 *      for its' boundaries. As text scrolls out of history buffer row coordinates
 *      have to shift and all ActiveRegions' coords need to be relocated. This
 *      can get expensive because as soon as the history buffer becomes full
 *      each newline will require a relocation. This is the approach that
 *      javax.swing.text.Position implements and it's justified there because
 *      no Swing component has a "history buffer".
 *      However, if you use absolute coordinates you'll never have to
 *      relocate anything! Simple and effective.
 *      <p>
 *      Well almost. What happens when you reach Integer.MAX_VALUE? You wrap and
 *      that can confuse everything. What are the chances of this happening?
 *      Suppose term can process 4000 lines per second. A runaway process will
 *      produce Integer.MAX_VALUE lines in about 4 days. That's too close
 *      for comfort, so Term does detect the wrap and only then goes and
 *      relocates stuff. This, however, causes a secondary problem with
 *      testability since no-one wants to wait 4 days for a single wrap.
 *      So what I've done is periodically set Term.modulo to something
 *      smaller and tested stuff.
 * <p>
 * I'm indebted to Alan Kostinsky for this bit of lateral thinking.
 * </dl>
 *
 *
 * <p>
 * <h2>Modes of use</h2>
 * There are three ways Term can be used.
 * These modes aren't explicit they are just a convenient way of discussing
 * functionality.
 * <dl>
 * <dt>Screen mode
 *      <dd>
 *      This represents the traditional terminal without a history buffer.
 *      Applications
 *      running under the terminal assume they are dealing with a fixed size
 *      screen and interact with it in the screen coordinate system through
 *      escape sequence (ANSI or otherwise). The most common application which
 *      uses terminals in this ways is the screen editor, like vi or emacs.
 *      <p>
 *      Term will convert keystrokes to an output stream and will process
 *      characters in an input stream and render them unto the screen.
 *      What and how these streams are connected to is up to the client of Term,
 *      since it is usually highly platform dependent. For example on unixes
 *      the streams may be connected to partially-JNI-based "pty" streams.
 *      <p>
 *      This mode works correctly even if there is history and you see a
 *      scrollbar, just as it does under XTerm and it's derivatives.
 *
 * <p>
 * <dt>Buffer/Interactive mode
 *      <dd>
 *      This is the primary facility that XTerm and other derivatives provide. The
 *      screen has a history buffer in the vertical dimension.
 *      <p>
 *      Because of limited history active regions can scroll out of history and
 *      while the coordinate invalidation problem is not addressed by absolute
 *      coordiantes sometimes we don't want stuff to wink out.
 *      <br>
 *      Which is why we have ...
 *
 * <p>
 * <dt>Page mode
 *      <dd>
 *      It is possible to "anchor" a location in the buffer and prevent it
 *      from going out of history. This can be helpful in having the
 *      client of Term make sure that crucial output doesn't get lost due to
 *      short-sighted history settings on the part of the user.
 *      <p>
 *      To use Term
 *      in this mode you can use setText() or appendText() instead of
 *      connecting to Terms streams.
 *      This mode is called page mode because the most common use of it
 *      would be as something akin to a hypertext browser.
 *      To that end
 *      Term supports nestable ActiveRegions and mapping of coordinates
 *      to regions. ActiveTerm puts all of this together in a comprehensive
 *      subclass.
 * </dl>
 *
 * <p>
 * <h2>What Term is not</h2>
 * <ul>
 *      <li>
 *      While there is an internal Buffer class, and while it behaves like a
 *      document in that it can
 *      be implicitly "edited" and character attributes explicitly changed,
 *      Term is not a document editing widget.
 *      <p>
 *      <li>
 *      Term is also not a command line processor in the sense that a MS Windows
 *      console is. Its shuttling of keyboard events to an output stream and
 *      rendering of characters from the input stream unto the screen are completely
 *      independent activities.
 *      <p>
 *      This is due to Terms unix heritage where shells (ksh, bash etc) do their own
 *      cmdline and history editing, but if you're so inclined the LineDiscipline
 *      may be used for experimentation with indigenous cmdline processing.
 * </ul>
 */
public class Term extends JComponent implements Accessible {
    public static class ExternalCommandsConstants {
        public static final String EXECUTION_ENV_PROPERTY_KEY = "ExecutionEnvironment_KEY"; //NOI18N

        public static final String COMMAND_PREFIX = "ext[::] "; //NOI18N
        public static final String IDE_OPEN = "ideopen"; //NOI18N
    }

    private State st = new State();
    private Sel sel = new Sel(this, st);
    private transient Ops ops = new OpsImpl();
    private int top_margin = 0;		// 0 means default (see topMargin())
    private int bot_margin = 0;
    // Stuff to control how often RegionManager.cull() gets called
    private int cull_count = 0;
    private static final int CULL_FREQUENCY = 50;
    // 'firsta' is the absolute line number of the line at 'lines[0]'.
    private int firsta = 0;
    // chars gone by in lines that winked out of history
    // 'firsta' ~= 'linesInPrehistory'
    private int charsInPrehistory = 0;
    private static final int MODULO = Integer.MAX_VALUE / 2;
    private Screen screen;
    private JScrollBar vscroll_bar;
    private ScrollWrapper hscroll_wrapper;
    private JScrollBar hscroll_bar;
    private boolean has_focus;
    // statistics
    private int n_putchar;
    private int n_putchars;
    private int n_linefeeds;
    private int n_repaint;
    private int n_paint;
    private boolean fixedFont = false;
    private MyFontMetrics metrics = null;
    private Map<?, ?> renderingHints;
    private Buffer buf = new Buffer(80);
    private RegionManager region_manager = new RegionManager();
    // 'left_down_point' remembers where the left button came down as a
    // workaround for the flakey mouseDragged event. The flakiness has to with
    // the fact that the mousePressed coord is not delivered in the first drag
    // event, so if you proess and drag very quickly, the first drag coord
    // will be quite far from the initial press location.
    private Point left_down_point;
    // getSystemSelection() wasn't available on Java prior to 1.4
    private Clipboard systemClipboard = getToolkit().getSystemClipboard();
    private Clipboard systemSelection = getToolkit().getSystemSelection();
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    // 
    // The palette maps color indexes into actual Color's.
    // 
    // Attr's FGCOLOR and BGCOLOR store either 0 to imply "default" or
    // the palette index + 1. So use methods Attr.foregroundColor() and
    // backgroudnColor() to convert Attr values to the indexes into the palette.
    //
    // The domain of this map is divided as follows:
    //
    //					"ESC [ ... m" codes
    // index				fg	bg
    //--------------------------------------------------------------------------
    // 0-7	ANSI colors		30-37	40-47		PAL_ANSI
    // 8-15	DtTerm custom colors	50-57	58-65		PAL_BRIGHT
    // 8-15	ANSI "bright" colors	90-97	100-107		PAL_BRIGHT
    // 16-231	RGB cube					PAL_RGB
    // 232-255	Greyscale					PAL_GREY
    //--------------------------------------------------------------------------
    // 256	default foreground				PAL_FG
    // 257	default background				PAL_BG
    // 258	default bold					PAL_BOLD
    // 259	palette size					PAL_SIZE
    //

    private final Color palette[] = new Color[Attr.PAL_SIZE];

    /**
     * ScrollWrapper is a HACK that allows us to make pairs of scrollbars
     * look nice.
     * <p>
     * A JScrollPane, or more specifically ScrollPaneLayout, arranges
     * a pair of vertical and horizontal scrollbars as follows:
     *                   | |
     *                   | |
     *                   |v|
     *      -------------
     *                 >|
     *      -------------
     * ... so that here is a nice square corner.
     *
     * But ScrollPaneLayout insists that it's viewport is a JViewport and that
     * it's container is a JScrollPane. It is probably possible to make the
     * screen be a JViewPort and use a JScrollPane to contain the screen, but
     * it's very tricky. (For that matter it should be possible to avoid doing
     * our own scrolling altogether and use JScrollPane functionality, but
     * it's also tricky).
     *
     * Since we're using a BorderLayout putting the horizontal SB in the SOUTH
     * portion yields something like this:
     *                   | |
     *                   | |
     *                   |v|
     *      ----------------
     *                    >|
     *      ----------------
     * Soooo, to make things look right, we use ScrollWrapper to control the
     * sizing of the horizontal scrollbar. It basically uses a GridBagLayout
     * and GridBagConstraints.insets to create the square corner.
     */
    private class ScrollWrapper extends JComponent implements Accessible {

        public JScrollBar scroll_bar;

        public ScrollWrapper(JScrollBar scroll_bar) {
            GridBagLayout gbl = new GridBagLayout();
            setLayout(gbl);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 1.0f;
            gbc.weighty = 1.0f;
            int slop = vscroll_bar.getMaximumSize().width;
            gbc.insets = new Insets(0, 0, 0, slop);
            add(scroll_bar, gbc);

            this.scroll_bar = scroll_bar;
        }

        @Override
        protected void paintComponent(Graphics g) {
            // If we don't do this, the square corner will end getting filled
            // with random grot.
            Dimension sz = getSize();
            g.clearRect(0, 0, sz.width, sz.height);
        }

        //......................................................................
        // Accessibility stuff is all here
        //......................................................................
        @Override
        public AccessibleContext getAccessibleContext() {
            if (accessible_context == null) {
                accessible_context = new AccessibleScrollWrapper();
            }
            return accessible_context;
        }
        private AccessibleContext accessible_context;

        protected class AccessibleScrollWrapper extends AccessibleJComponent {

            @Override
            public AccessibleRole getAccessibleRole() {
                return AccessibleRole.PANEL;
            }
        }
    }

    private class BaseTermStream extends TermStream {

	@Override
        public void flush() {
            repaint(true);
        }

	@Override
        public void putChar(char c) {
            /*
             * echoes a character unto the screen
             */
            ckEventDispatchThread();
            // OLD NPE-x synchronized(Term.this)
            {
                n_putchar++;
                putc_work(c);
            }
            possibly_repaint(true);

        // pavel.buzek@czech.sun.com put this as a fix to speed up
        // StreamTerm on windows. This will make raw mode not work,
        // Instead now LineDiscipline properly buffers incoming characters.
        // so should be considered temporary.
        // repaint(c == '\n');
        }

	@Override
        public void putChars(char buf[], int offset, int count) {
            ckEventDispatchThread();
            // OLD NPE-x synchronized (Term.this)
            {
                n_putchars++;
                for (int bx = 0; bx < count; bx++) {
                    putc_work(buf[offset + bx]);
                }
            }
            possibly_repaint(true);
        }

	@Override
        public void sendChar(char c) {
            fireChar(c);
        }

	@Override
        public void sendChars(char buf[], int offset, int count) {
            fireChars(buf, offset, count);
        }
    }
    // head is closer to Term
    // pushes extend tail
    private transient TermStream base_stream = new BaseTermStream();
    private transient TermStream dce_end = base_stream;
    private transient TermStream dte_end = base_stream;

    /**
     *
     * @param stream
     */
    public void pushStream(TermStream stream) {
        // Term will send keystrokes by calling dte_end.sendChar
        // Characters sent via Term.putChar will be sent down dce_end.
        //
        // The base stream is strange in that on the first push it will get
        // split into two parts, one sticking at the end of the dce chain,
        // the other at the end of the dte chain. Hence the special case
        // treatment

        if (dce_end == base_stream) {
            // towards dce
            dte_end = stream;
            stream.setToDCE(base_stream);

            // towards dte
            stream.setToDTE(base_stream);
            dce_end = stream;

        } else {
            // towards dce
            dte_end.setToDCE(stream);
            stream.setToDCE(base_stream);

            // towards dte
            stream.setToDTE(dce_end);
            dce_end = stream;
        }

        stream.setTerm(this);
    }
    /*
     * Debugging utilities
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    public static final int DEBUG_OPS = 1 << 0;
    public static final int DEBUG_KEYS = 1 << 1;
    public static final int DEBUG_INPUT = 1 << 2;
    public static final int DEBUG_OUTPUT = 1 << 3;
    public static final int DEBUG_WRAP = 1 << 4;
    public static final int DEBUG_MARGINS = 1 << 5;
    public static final int DEBUG_KEYPASS = 1 << 6;
    private int debug_gutter_width = 0;

    public void setDebugFlags(int flags) {
        debug = flags;
    }
    private int debug = /* DEBUG_OPS|DEBUG_KEYS|DEBUG_INPUT|DEBUG_OUTPUT | */ 0;

    private boolean debugOps() {
        return (debug & DEBUG_OPS) == DEBUG_OPS;
    }

    private boolean debugKeys() {
        return (debug & DEBUG_KEYS) == DEBUG_KEYS;
    }

    private boolean debugWrap() {
        return (debug & DEBUG_WRAP) == DEBUG_WRAP;
    }

    private boolean debugKeypass() {
        return (debug & DEBUG_KEYPASS) == DEBUG_KEYPASS;
    }

    private boolean debugMargins() {
        return true;
    /* TMP
    return (debug & DEBUG_MARGINS) == DEBUG_MARGINS;
     */
    }

    /**
     * Return true if DEBUG_INPUT flag has been set.
     * @return true if DEBUG_INPUT flag has been set.
     */
    protected boolean debugInput() {
        return (debug & DEBUG_INPUT) == DEBUG_INPUT;
    }

    /**
     * Return true if DEBUG_OUTPUT flag has been set.
     * @return true if DEBUG_OUTPUT flag has been set.
     */
    protected boolean debugOutput() {
        return (debug & DEBUG_OUTPUT) == DEBUG_OUTPUT;
    }

    /*
     * Top and bottom margins are stored as 1-origin values, with 0
     * denoting default.
     *
     * topMargin() & botMargin() return 0-origin values which is what we
     * use for screen coordinates.
     *
     * The margin lines are inclusive, that is, lines on the margin lines
     * participate in scrolling.
     */
    private void resetMargins() {
        top_margin = 0;
        bot_margin = 0;
    }

    private int topMargin() {
        return (top_margin == 0) ? 0 : top_margin - 1;
    }

    private int botMargin() {
        return (bot_margin == 0) ? st.rows - 1 : bot_margin - 1;
    }

    /**
     * beginx is row lines above the bottom.
     * It's used for all cursor motion calculations and cursor relative
     * line operations.
     * It's used instead of firstx because firstx changes as we scroll.
     * This allows us to restrict screen editing to the last chunk of
     * the buffer.
     */
    private int beginx() {
        return buf.nlines() - st.rows;
    }

    private Line cursor_line() {
        return buf.lineAt(st.cursor.row);
    }

    /**
     * Set/unset WordDelineator.
     * Passing a null sets it to the default WordDelineator.
     * @param word_delineator Property to set.
     */
    public void setWordDelineator(WordDelineator word_delineator) {
        if (word_delineator == null) {
            this.word_delineator = default_word_delineator;
        } else {
            this.word_delineator = word_delineator;
        }
    }

    /**
     * Get the WordDelineator used by this.
     * @return Property to get.
     */
    public WordDelineator getWordDelineator() {
        return this.word_delineator;
    }
    private WordDelineator default_word_delineator = WordDelineator.createNewlineDelineator();
    private WordDelineator word_delineator = default_word_delineator;

    /**
     * Set/unset input listener.
     * Entered text gets sent via this listener
     *
     * @param l
     * @deprecated Replaced by {@link #addInputListener(TermInputListener)}.
     */
    @Deprecated
    public void setInputListener(TermInputListener l) {
        addInputListener(l);
    }

    /**
     * Add an input listener to this.
     * <p>
     * Text entered via the keyboard gets sent via this listener.
     * @param l
     */
    public void addInputListener(TermInputListener l) {
        input_listeners.add(l);
    }

    public void removeInputListener(TermInputListener l) {
        input_listeners.remove(l);
    }

    private void fireChar(char c) {
        ListIterator<TermInputListener> iter = input_listeners.listIterator();
        while (iter.hasNext()) {
            TermInputListener l = iter.next();
            l.sendChar(c);
        }
    }

    private void fireChars(char buf[], int offset, int count) {
        ListIterator<TermInputListener> iter = input_listeners.listIterator();
        while (iter.hasNext()) {
            TermInputListener l = iter.next();
            l.sendChars(buf, offset, count);
        }
    }
    private LinkedList<TermInputListener> input_listeners = new LinkedList<>();

    /**
     * Set/unset misc listener.
     * The following events gets sent via this listener:
     * window size changes
     *
     * @param l
     * @deprecated Replaced by{@link #addListener(TermListener)}.
     */
    @Deprecated
    public void setListener(TermListener l) {
        addListener(l);
    }

    /**
     * Add a TermListener to this.
     * @param l
     */
    public void addListener(TermListener l) {
        listeners.add(l);
	updateTtySize();
    }

    /**
     * Remove the given TermListener from this.
     * @param l
     */
    public void removeListener(TermListener l) {
        listeners.remove(l);
    }

    private void fireSizeChanged(Dimension cells, Dimension pixels) {
        for (TermListener l : listeners) {
             l.sizeChanged(cells, pixels);
         }
    }
    
    private void fireTitleChanged(String title) {
        for (TermListener l : listeners) {
            l.titleChanged(title);
        }
    }
    
    private void fireCwdChanged(String cwd) {
        for (TermListener l : listeners) {
            l.cwdChanged(cwd);
        }
    }
    
    private void fireExternalCommand(String command) {
        for (TermListener l : listeners) {
            l.externalToolCalled(command);
        }
    }
    
    private final java.util.List<TermListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Set/unset focus policy.
     * <br>
     * When set, the Term screen will grab focus when clicked on, otherwise
     * it will grab focus when the mouse moves into it.
     * @param click_to_type Set the property.
     */
    public void setClickToType(boolean click_to_type) {
        this.click_to_type = click_to_type;
    }

    public boolean isClickToType() {
        return click_to_type;
    }
    private boolean click_to_type = true;

    /**
     * Control whether keystrokes are ignored.
     * @param read_only Set the property.
     */
    public void setReadOnly(boolean read_only) {
        this.read_only = read_only;
    }

    /**
     * Return whether keystrokes are ignored.
     * @return The property value.
     */
    public boolean isReadOnly() {
        return read_only;
    }
    private boolean read_only = false;

    /**
     * Clear the visible portion of screen
     */
    public void clear() {
        for (int row = 0; row < st.rows; row++) {
            Line l = buf.lineAt(beginx() + row);
            l.reset();
        }
        regionManager().reset();
    }

    /**
     * Clear all of the history without repainting the screen.
     * <p>
     * This is useful if you want to avoid flicker.
     */
    public void clearHistoryNoRefresh() {
        sel.cancel(true);

        int old_cols = buf.visibleCols();
        buf = new Buffer(old_cols);

        firsta = 0;
        charsInPrehistory = 0;

        st.firstx = 0;
        st.firsty = 0;
        st.cursor.row = 0;
        st.cursor.col = 0;
        setAttribute(0);
        st.saveCursor();	// This clobbers the saved cursor value
        st.restoreCursor();	// release reference to saved cursor object

        adjust_lines(st.rows);

        st.firstx = 0;
        st.firsty = 0;

        regionManager().reset();

        screen.possiblyUpdateCaretText();
    }

    /**
     * Clear all of the history, including any visible portion of it.
     * <p>
     * Use {@link #clearHistoryNoRefresh()} if you find that clearHistory
     * causes flickering.
     */
    public void clearHistory() {
        clearHistoryNoRefresh();
        repaint(true);
    }

    /**
     * Return the RegionManager associated with this Term
     * @return The RegionManager associated with this Term
     */
    public RegionManager regionManager() {
        return region_manager;
    }

    public String textWithin(Coord begin, Coord end) {
        if (begin == null || end == null) {
            return null;
        }

        final StringBuffer aBuf = new StringBuffer();

        visitLines(begin, end, false, new LineVisitor() {

	    @Override
            public boolean visit(Line l, int row, int bcol, int ecol) {
                // buf.append(l.charArray(), bcol, ecol-bcol+1);
                l.accumulateInto(bcol, ecol, aBuf);
                return true;
            }
        });
        return aBuf.toString();
    }

    public String getRowText(int row) {
        Line line = buf.lineAt(row);
        if (line == null) {
            return null;
        }
        return line.stringBuffer().toString();
    }
    
    private Keymap keymap;
    private Set<String> allowedActions;
            
    /**
     * Set keymap and allowed actions
     * @param keymap - use this to check if a keystroke is used outside the terminal
     * @param allowedActions - if not null we only allow specified actions in the terminal
     */
    public void setKeymap(Keymap keymap, Set<String> allowedActions) {
        this.keymap = keymap;
        this.allowedActions = allowedActions;
    }

    /**
     * Get KeyStroke set.
     * <p>
     * Be default Term consumes all keystrokes.
     * Any KeyStroke added to this set will be passed through and not consumed.
     * <p>
     * Be careful with control characters you need to create the keystroke
     * as follows (note the - 64):
     * <pre>
     * KeyStroke.getKeyStroke(new Character((char)('T'-64)), Event.CTRL_MASK)
     * </pre>
     * @return Property value.
     */
    public HashSet<KeyStroke> getKeyStrokeSet() {
        return keystroke_set;
    }

    /*
     * Set the KeyStroke set.
     * <p>
     * While Term has a KeyStroke set set up by default, often many Terms
     * share the same keystroke. This method allows this sharing.
     */
    public void setKeyStrokeSet(HashSet<KeyStroke> keystroke_set) {
        this.keystroke_set = keystroke_set;

	if (debugKeypass()) {
	    System.out.println("---- setKeyStrokeSet --------------------");//NOI18N
            for (KeyStroke ks : keystroke_set) {
                System.out.println("--- " + ks);//NOI18N
            }
	}
    }

    private HashSet<KeyStroke> keystroke_set = new HashSet<>();
    // attempted partial fix for IZ 17337
    // 'keystroke_set' is a collection of KeyStrokes in the form:
    //	ks3 = getKeyStroke(VK_C, CTRL_MASK)
    // we use maybeConsume() in keyPressed and keyTyped events. During
    // keyTyped the event->KS gives us
    //	ks2 = getKeyStroke((char) ('c'-64), CTRL_MASK)
    // ks2 and ks3 while logically equivalent don't hash to the same so
    // maybeConsume() says yes to ks2 and the Ctrl-C gets passed on.
    //
    // So to detect whether something in 'keystroke_set' needs to be dropped
    // we need to check at keyPress time but take action at keyTyped time.
    // 'passOn' helps us do that.
    private boolean passOn = true;

    /**
     * Return true (and consume it) if 'e' is allowed to be consumed by us.
     *
     * If our owner is interested in some keys they will put something into
     * keystroke_set.
     */
    private boolean maybeConsume(KeyEvent e) {

        if (read_only || e.isConsumed()) {
            return false;
        }

        KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);

	if (debugKeypass()) {
	    System.out.println("Term.maybeConsume(" + e + ")");	// NOI18N
	    System.out.println("\tKS = " + ks);	// NOI18N
	    System.out.println("\tcontained = " + keystroke_set.contains(ks));	// NOI18N
	}
        

        if (keystroke_set == null || !keystroke_set.contains(ks)) {
            e.consume();
            return true;
        }
        return false;
    }

    /**
     * Visit the physical lines from begin, through 'end'.
     * <p>
     * If 'newlines' is set, the passed 'ecol' is set to the actual
     * number of columns in the view to signify that the newline is included.
     * This way of doing it helps with rendering of a whole-line selection.
     * Also Line knows about this and will tack on a "\n" when Line.text()
     * is asked for.
     */
    void visitLines(Coord begin, Coord end, boolean newlines,
            LineVisitor visitor) {
        buf.visitLines(begin.toBCoord(firsta), end.toBCoord(firsta), newlines, visitor);
    }

    /**
     * Visit logical lines from begin through end.
     * <p>
     * If begin is null, then the start of the buffer is assumed.
     * If end is null, then the end of the buffer is assumed.
     * @param begin
     * @param end
     * @param llv visit() is called on 'llv' for each line.
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    public void visitLogicalLines(Coord begin, Coord end,
            final LogicalLineVisitor llv) {

        // Create a trampoline visitor
        LineVisitor tramp = new LineVisitor() {

            private String text = "";	// NOI18N
            private int lineno = 0;
            private Coord begin = null;
            private Coord end = null;

	    @Override
            public boolean visit(Line l, int brow, int bcol, int ecol) {

                if (l.isWrapped()) {
                    if (begin == null) {
                        begin = new Coord(new BCoord(brow, bcol), firsta);
                    }
                    text += l.text(bcol, ecol);

                } else {
                    if (begin == null) {
                        begin = new Coord(new BCoord(brow, bcol), firsta);
                    }
                    end = new Coord(new BCoord(brow, ecol), firsta);
                    text += l.text(bcol, ecol);

                    if (!llv.visit(lineno, begin, end, text)) {
                        return false;
                    }

                    lineno++;
                    text = "";	// NOI18N
                    begin = null;
                    end = null;
                }

                return true;
            }
        };

        if (begin == null) {
            begin = new Coord();
        }

        if (end == null) {
            int lastrow = buf.nlines() - 1;
            Line l = buf.lineAt(lastrow);
            end = new Coord(new BCoord(lastrow, l.length() - 1), firsta);
        }

        if (begin.compareTo(end) > 0) {
            return;
        }

        buf.visitLines(begin.toBCoord(firsta), end.toBCoord(firsta), false, tramp);
    }

    /**
     * Visit logical lines in reverse from end through begin.
     * <p>
     * If begin is null, then the start of the buffer is assumed.
     * If end is null, then the end of the buffer is assumed.
     * @param begin
     * @param end
     * @param llv visit() is called on 'llv' for each line.
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    public void reverseVisitLogicalLines(Coord begin, Coord end,
            final LogicalLineVisitor llv) {

        // Create a trampoline visitor
        LineVisitor tramp = new LineVisitor() {

            private String text = "";	// NOI18N
            private int lineno = 0;
            private Coord begin = null;
            private Coord end = null;

	    @Override
            public boolean visit(Line l, int brow, int bcol, int ecol) {

                boolean line_is_continuation = false;
                if (brow > 0 && buf.lineAt(brow - 1).isWrapped()) {
                    line_is_continuation = true;
                }

                if (line_is_continuation) {
                    if (end == null) {
                        end = new Coord(new BCoord(brow, ecol), firsta);
                    }
                    text = l.text(bcol, ecol) + text;

                } else {
                    if (end == null) {
                        end = new Coord(new BCoord(brow, ecol), firsta);
                    }
                    begin = new Coord(new BCoord(brow, bcol), firsta);
                    text = l.text(bcol, ecol) + text;

                    if (!llv.visit(lineno, begin, end, text)) {
                        return false;
                    }

                    lineno++;
                    text = "";	// NOI18N
                    begin = null;
                    end = null;
                }

                return true;
            }
        };

        if (begin == null) {
            begin = new Coord();
        }

        if (end == null) {
            int lastrow = buf.nlines() - 1;
            Line l = buf.lineAt(lastrow);
            end = new Coord(new BCoord(lastrow, l.length() - 1), firsta);
        }

        if (begin.compareTo(end) > 0) {
            return;
        }

        buf.reverseVisitLines(begin.toBCoord(firsta), end.toBCoord(firsta), false, tramp);
    }

    /**
     * Convert offsets in logical lines into physical coordinates.
     * <p>
     * Usually called from within a LogicalLineVisitor.visit().
     * 'begin' should be the 'begin' Coord passed to visit. 'offset' is an
     * offset into the logical line, the 'text' argument passed to visit().
     * <p>
     * Note that 'offset' is relative to begin.col!
     * <p>
     * Following is an example of a line "aaaaaxxxxxxxxxXXXxxxxxxxx" which
     * got wrapped twice. Suppose you're searching for "XXX" and you
     * began your search from the first 'x' on row 2.
     * <pre>
     *     row |  columns |
     *     ----+----------+
     *     0   |0123456789|
     *     1   |          |
     *     2   |aaaaaxxxxx| wrap
     *     3   |xxxxXXXxxx| wrap
     *     4   |xxxxx     |
     *     5   |          |
     *
     * </pre>
     * The visitor will be called with 'begin' pointing at the first 'x',
     * 'end' pointing at the last 'x' and 'text' containing the above line.
     * Let's say you use String.indexOf("XXX") and get an offset of 9.
     * Passing the 'begin' through and the 9 as an offset and 3 as the
     * length will yield an Extent (3,4) - (3,7) which youcan forward to
     * setSelectionExtent();
     * <p>
     * The implementation of this function is not snappy (it calls
     * Term.advance() in a loop) but the assumption is that his function
     * will not be called in a tight loop.
     * @param begin
     * @param offset
     * @param length
     * @return 
     */
    @SuppressWarnings({"AssignmentToMethodParameter", "ValueOfIncrementOrDecrementUsed"})
    public Extent extentInLogicalLine(Coord begin, int offset, int length) {

        // SHOULD factor the A/B Coord conversion out

        Coord from = (Coord) begin.clone();
        while (offset-- > 0) {
            from = new Coord(buf.advance(from.toBCoord(firsta)), firsta);
        }

        Coord to = (Coord) from.clone();
        while (--length > 0) {
            to = new Coord(buf.advance(to.toBCoord(firsta)), firsta);
        }

        return new Extent(from, to);
    }

    private boolean cursor_was_visible() {
        return st.cursor.row - 1 >= st.firstx &&
                st.cursor.row - 1 < st.firstx + st.rows;
    }

    /**
     * Ensure that the given coordinate is visible.
     * <p>
     * If the given coordinate is visible within the screen nothing happens.
     * Otherwise the screen is scrolled so that the given target ends up
     * approximately mid-screen.
     * @param target Coord to be checked.
     */
    public void possiblyNormalize(Coord target) {
        if (target == null) {
            return;
        }

        BCoord btarget = target.toBCoord(firsta);
        if (btarget.row >= st.firstx && btarget.row < st.firstx + st.rows) {
            return;
        }

        st.firstx = btarget.row - (st.rows / 2);
        if (st.firstx < 0) {
            st.firstx = 0;
        } else if (st.firstx + st.rows > buf.nlines()) {
            st.firstx = buf.nlines() - st.rows;
        }

        repaint(true);
    }

    /**
     * Ensure that maximum of the given region is visible.
     * <p>
     * If the given region is visible within the screen nothing happens.
     * If the region is bigger than the screen height, first line of the region
     * will be displayed in first line of screen and end of region
     * won't be displayed.
     * If the region is bigger than the half of screen height, last line
     * of the region will be displayed in the last line of the screen.
     * Otherwise the region will start approximately in mid-screen
     * @param region Region against which to normalize.
     */
    public void possiblyNormalize(ActiveRegion region) {
        if (region == null) {
            return;
        }

        BCoord bbegin = region.begin.toBCoord(firsta);
        BCoord bend = region.end.toBCoord(firsta);
        if (bbegin.row >= st.firstx && bend.row < st.firstx + st.rows) {
            return;
        }

        if (bend.row - bbegin.row + 1 >= st.rows) {
            // region has more rows then screen
            st.firstx = bbegin.row;
        } else {
            st.firstx = bbegin.row - (st.rows / 2);
            if (st.firstx < 0) {
                st.firstx = 0;
            } else if (st.firstx + st.rows > buf.nlines()) {
                st.firstx = buf.nlines() - st.rows;
            } else if (st.firstx + st.rows <= bend.row) {
                // display also end of region
                st.firstx = bend.row - st.rows + 1;
            }
        }

        repaint(true);
    }

    /**
     * If the given coordinate is visible within the screen returns true,
     * otherwise returns false.
     * @param target Coord to check visibility of.
     * @return true if 'target' is visible within the screen.
     */
    public boolean isCoordVisible(Coord target) {
        BCoord btarget = target.toBCoord(firsta);
        return btarget.row >= st.firstx && btarget.row < st.firstx + st.rows;
    }

    private void possiblyScrollOnInput() {
        if (!scroll_on_input) {
            return;
        }

        // vertical (row) dimension
        if (st.cursor.row >= st.firstx && st.cursor.row < st.firstx + st.rows) {
        } else {
            st.firstx = buf.nlines() - st.rows;
            repaint(true);
        }
    }
    /*
     * Horizontal scrolling to track the cursor.
     *
     * The view/buffer etc calculations, performed by possiblyHScroll(), are not
     * terribly complex, but they do depend on up-to-date cursor position.
     * If we were to do all this work on every character received we would
     * get some slowdown, but worse if say a large file with long lines is
     * being 'cat'ed Term will end up scrolling left and right. It would be
     * rather annoying.
     * But attempting to do the scrolling on typed input isn't going to do
     * because (because of non-local echoing) the cursor isn't yet updated when
     * a key has been pressed.
     * Additionally, a key may result in more than character being echoed.
     * for example, an ENTER might result in a "\n\r", a TAB in 8 ' 's,
     * and a '\b' in a "\b \b" sequence so a single flag won't do.
     * Instead we use a count, 'hscroll_count' which says: attempt
     * possiblyHScroll() on the next <n> received characters. It may happen
     * there there is a lot of keyboard input and very little output. In
     * such cases 'hscroll_count' will start accumulating a defecit. To
     * counter this we reset the count to 8 on "newline" type stuff.
     */
    private int hscroll_count = 0;

    private void hscrollReset(char c) {
        ckEventDispatchThread();
        // OLD NPE-x synchronized(hscroll_lock)
        {
            if (c == '\n' || c == '\r') {
                hscroll_count = 8;
            } else {
                hscroll_count += 8;
            }
        }
    }

    private void possiblyHScroll() {

        // decide if we actually need to do it

        if (!horizontally_scrollable) {
            return;
        }

        ckEventDispatchThread();
        // OLD NPE-x synchronized(hscroll_lock)
        {
            if (hscroll_count > 0) {
                hscroll_count--;
            } else {
                return;
            }
        }

        /* DEBUG
        System.out.println("Checking hscroll cursor.col " + st.cursor.col + 	// NOI18N
        " firsty " + st.firsty 	// NOI18N
        " visibleCols " + buf.visibleCols());	// NOI18N
         */

        // horizontal (col) dimension
        if (st.cursor.col >= st.firsty + buf.visibleCols()) {
            /* DEBUG
            System.out.println("Need to scroll right");	// NOI18N
             */
            st.firsty = st.cursor.col - buf.visibleCols() + 1;

            // Expand our idea of column count so that if the cursor is
            // at the end the scrollbar allows us to scroll to it.
            // This is a bit unusual in that if we type stuff right up to the
            // last visible column and return the hscrollbar will display this
            // one extra column even though nothing was ever typed in it.
            // This is particularly annoying in 'vi' with right-butting lines.
            // In the future SHOULD think up of something "smart".
            buf.noteColumn(st.cursor.col + 1);

            repaint(true);

        } else if (st.cursor.col - buf.visibleCols() < st.firsty) {
            /* DEBUG
            System.out.println("Need to scroll left");	// NOI18N
             */
            st.firsty = st.cursor.col - buf.visibleCols() + 1;
            if (st.firsty < 0) {
                st.firsty = 0;
            } else {
                repaint(true);
            }
        }
    }

    /**
     * Set the display attribute for characters printed from now on.
     * <p>
     * Unrecognized values silently ignored.
     * @param value attribute value
     */
    public void setAttribute(int value) {
        st.attr = Attr.setAttribute(st.attr, value);
    }

    /**
     * Return the complete state of attributes.
     * @return Complete state of attributes.
     */
    int attrSave() {
        return st.attr;
    }

    /**
     * Restore the complete set of attributes.
     * @param attr Attributes to be restored.
     */
    void attrRestore(int attr) {
        st.attr = attr;
    }

    /**
     * Set or unset the display attribute for characters from 'begin' to 'end'
     * inclusive to 'value' depending on the value of 'on'.
     * <p>
     * Will cause a repaint.
     * @param begin
     * @param end
     * @param value
     * @param on
     */
    public void setCharacterAttribute(Coord begin, Coord end,
            final int value, final boolean on) {

        visitLines(begin, end, false, new LineVisitor() {

	    @Override
            public boolean visit(Line l, int brow, int bcol, int ecol) {
                l.setCharacterAttribute(bcol, ecol, value, on);
                return true;
            }
        });

        repaint(false);
    }

    /*
     * Set the glyph and line background colors for the line the cursor is on.
     * <p>
     * Will not repaint.
     */
    public void setGlyph(int glyph_id, int background_id) {
        Line l = cursor_line();
        l.setGlyphId(glyph_id);
        l.setBackgroundColor(background_id);
    }

    /*
     * Set the glyph and line background colors for the give line.
     * <p>
     * Will repaint.
     */
    public void setRowGlyph(int row, int glyph_id, int background_id) {
        Coord c = new Coord();
        c.row = row;
        c.col = 0;
        BCoord b = c.toBCoord(firsta);
        Line l = buf.lineAt(b.row);
        if (l == null) {
            return;
        }
        l.setGlyphId(glyph_id);
        l.setBackgroundColor(background_id);

        possibly_repaint(false);
    }

    /**
     * Adjust lines when the widget is resized (and also at construction time)
     * <p>
     * When growing makes sure that everything in the screen is backed up
     * by a buffer Line. When shrinking removes lines from the top or the
     * bottom as appropriate.
     * <p>
     * In other words implements the xterm resizeGravity=SouthWest semantics.
     * which roughly says: When resizing keep the line that was at the bottom,
     * _at_ the bottom.
     */
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    private void adjust_lines(int delta_row) {

        if (delta_row > 0) {
            // attempt to scroll
            st.firstx -= delta_row;

            // SHOULD eliminate the loop and move the work to Buffer
            while (st.firstx < 0) {
                buf.appendLine();
                st.firstx++;
            }

        } else if (delta_row < 0) {
            // we shrunk
            // int orows = st.rows - delta_row;	// reconstruct orows

            // First attempt to remove lines from the bottom of the buffer.
            // This comes into play mostly when you have just started Term
            // and have but few lines near the top and nothing in history
            // so you really can't scroll.

            // How many lines can we trim at the bottom before we eat
            // into the cursor?
            // Really weird I seem to get the same results regardless of
            // whether I use orows or buf.nlines. SHOULD investigate more.

            int allowed = buf.nlines() - st.cursor.row - 1;

            if (allowed < 0) {
                allowed = 0;
            }

            int delete_from_bottom;
            if (allowed > -delta_row) {
                // no need to scroll, we accomodate the whole resize by
                // removing lines from the bottom
                delete_from_bottom = -delta_row;
            } else {
                // delete as much as we're allowed ...
                delete_from_bottom = allowed;
                // ... and scroll for the rest
                st.firstx += (-delta_row - allowed);
            }

            // SHOULD eliminate the loop and move the work to Buffer
            while (delete_from_bottom-- > 0) {
                buf.removeLineAt(buf.nlines() - 1);
            }


        }
        // printStats("From delta_row of " + delta_row);	// NOI18N

        adjust_scrollbar();
    }

    /**
     * Returns current history size of buffer.
     * @return current history size of buffer.
     */
    public int getHistoryBuffSize() {
        return buf.nlines() - st.rows;
    }

    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    private void limit_lines() {

        /*
         * Make sure we don't exceed the buffer size limit historySize.
         * This implements the vanishing of lines from the beginning of history.
         */

        if (anchored) {
            return;
        }

        int history = buf.nlines() - st.rows;
        if (history < 0) {
            history = 0;
        }

        int toremove = history - history_size;
        if (toremove > 0) {
            int charsRemoved = buf.removeLinesAt(0, toremove);

            charsInPrehistory += charsRemoved;

            // relocate all row indices
            st.adjust(-toremove);

            firsta += toremove;

            // cull any regions that are no longer in history
            if (++cull_count % CULL_FREQUENCY == 0) {
                /* DEBUG
                System.out.println("Culling regions ..."); // NOI18N
                 */
                region_manager.cull(firsta);
            }

            // our absolute coordinates are about to wrap
            if (firsta + buf.nlines() >= MODULO) {
                int old_firsta = firsta;
                firsta = 0;

                sel.relocate(old_firsta, firsta);
                region_manager.relocate(old_firsta, firsta);
            }
        }

        // deal with selection moving out of the buffer
        sel.adjust(firsta, 0, firsta + buf.nlines());

        adjust_scrollbar();
    }

    /**
     * Scroller is used to implement selection auto-scrolling.
     * <p>
     * When a selection drag moves out of the window a scroller object/thread
     * is started to periodically scroll and extend the selection.
     */
    private class Scroller extends Thread {

        public final static int UP = 1 << 1;
        public final static int DOWN = 1 << 2;
        public final static int LEFT = 1 << 3;
        public final static int RIGHT = 1 << 4;
        private int direction;

        public Scroller(int direction) {
            this.direction = direction;
        }

        private boolean extend() {

            ckEventDispatchThread();
            // OLD NPE-x synchronized (Term.this)
            {

                // Selection might wink out while we're auto scrolling.
                // Since we use 'sel.sel_extent' further below we
                // synchronize.
                if (sel.sel_extent == null) {
                    return false;
                }

                BCoord x = sel.sel_extent.toBCoord(firsta);
                BCoord v = toViewCoord(x);
                int r;
                int c;

                if ((direction & DOWN) == DOWN) {
                    lineDown(1);
                    r = st.rows - 1;
                    c = buf.totalCols();
                } else if ((direction & UP) == UP) {
                    lineUp(1);
                    r = 0;
                    c = 0;
                } else {
                    BCoord vc2 = toViewCoord(drag_point);
                    r = vc2.row;
                    c = vc2.col;
                }


                if ((direction & LEFT) == LEFT) {
                    st.firsty--;
                    if (st.firsty < 0) {
                        st.firsty = 0;
                    }
                    c = 0;
                } else if ((direction & RIGHT) == RIGHT) {
                    st.firsty++;
                    int limit = buf.totalCols() - buf.visibleCols();
                    if (limit < 0) {
                        limit = 0;
                    }
                    if (st.firsty > limit) {
                        st.firsty = limit;
                    }
                    c = st.firsty + buf.visibleCols();
                }

                BCoord vc = new BCoord(r, c);
                BCoord bc = toBufCoords(vc);
                sel.track(new Coord(bc, firsta));
                repaint(true);
                return true;
            }
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            while (true) {

                /* DEBUG
                System.out.print("Scrolling ");	// NOI18N
                if ((direction & UP) == UP)
                System.out.print("UP ");	// NOI18N
                if ((direction & DOWN) == DOWN)
                System.out.print("DOWN ");	// NOI18N
                if ((direction & LEFT) == LEFT)
                System.out.print("LEFT ");	// NOI18N
                if ((direction & RIGHT) == RIGHT)
                System.out.print("RIGHT ");	// NOI18N
                System.out.println();
                 */

                extend();

                try {
                    // See issue 36404
                    sleep(10);
                } catch (InterruptedException x) {
                    break;
                }
            }
        /* DEBUG
        System.out.println("Done with Scrolling");	// NOI18N
         */
        }
    }
    private Scroller scroller;
    private Point drag_point;
    private int scrolling_direction = 0;

    private void scroll_to(int direction, MouseEvent e) {
        if (direction == scrolling_direction) {
            if (direction == 0) {
                // We're moving inside the view
                BCoord bc = toBufCoords(toViewCoord(e.getPoint()));
                sel.track(new Coord(bc, firsta));
                repaint(false);
            }
            return;
        }

        // we changed direction

        // get rid of the old scroller
        if (scroller != null) {
            scroller.interrupt();
            scroller = null;
        }

        if (direction == 0) {
            BCoord bc = toBufCoords(toViewCoord(e.getPoint()));
            sel.track(new Coord(bc, firsta));
            repaint(false);
        } else {
            scroller = new Scroller(direction);
            scroller.start();
        }

        scrolling_direction = direction;
    }
    private static Boolean onMac = null;

    private boolean onMac() {
        if (onMac == null) {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.startsWith("mac os x")) { //NOI18N
                onMac = Boolean.TRUE;
            } else {
                onMac = Boolean.FALSE;
            }
        }
        return onMac;
    }

    private void initializePalette() {

        palette[Attr.PAL_ANSI+0] = new Color(0x000000);
        palette[Attr.PAL_ANSI+1] = new Color(0xCD0000);
        palette[Attr.PAL_ANSI+2] = new Color(0x00CD00);
        palette[Attr.PAL_ANSI+3] = new Color(0xCDCD00);
        palette[Attr.PAL_ANSI+4] = new Color(0x1E90FF);
        palette[Attr.PAL_ANSI+5] = new Color(0xCD00CD);
        palette[Attr.PAL_ANSI+6] = new Color(0x00CDCD);
        palette[Attr.PAL_ANSI+7] = new Color(0xE5E5E5);

        palette[Attr.PAL_BRIGHT+0] = new Color(0x0D0D0D);
        palette[Attr.PAL_BRIGHT+1] = new Color(0xFF0000);
        palette[Attr.PAL_BRIGHT+2] = new Color(0x00FF00);
        palette[Attr.PAL_BRIGHT+3] = new Color(0xFFFF00);
        palette[Attr.PAL_BRIGHT+4] = new Color(0x1FF0FF);
        palette[Attr.PAL_BRIGHT+5] = new Color(0xFF00FF);
        palette[Attr.PAL_BRIGHT+6] = new Color(0x00FFFF);
        palette[Attr.PAL_BRIGHT+7] = new Color(0xFFFFFF);

	// Fill RGB cube
	for (int r = 0; r <= 5; r++) {
	    for (int g = 0; g <= 5; g++) {
		for (int b = 0; b <= 5; b++) {
		    int number = 36 * r + 6 * g + b;
		    palette[Attr.PAL_RGB+number] = new Color(r*51, b*51, b*51);
		}
	    }
	}

	// Fill greyscale portion.
	for (int g = 0; g < 24; g++) {
	    int g2 = g+1;
	    palette[Attr.PAL_GREY+g] = new Color(g2*10, g2*10, g2*10);
	}

	palette[Attr.PAL_FG] = UIManager.getColor("TextArea.foreground");	// NOI18N
	palette[Attr.PAL_BG] = UIManager.getColor("TextArea.background");	// NOI18N
	palette[Attr.PAL_BOLD] = palette[Attr.PAL_FG];

	// Ensure nothing is left unfilled.
	for (int px = 0; px < palette.length; px++) {
	    if (palette[px] == null) {
		System.out.printf("palette[%d] is null\n", px); //NOI18N
		palette[px] = Color.GRAY;
	    }
	}
    }

    /**
     * Constructor
     */
    public Term() {
        st.rows = 25;
        st.firstx = 0;
        st.firsty = 0;

	initializePalette();
        Font f = UIManager.getFont("TextArea.font"); //NOI18N
        if (f == null) {
            // on, e.g., GTK L&F
            f = UIManager.getFont("controlFont"); //NOI18N
        }

        if (f != null) {
            setFont(new Font("Monospaced", Font.PLAIN, f.getSize() + 1)); // NOI18N
        } else {
            setFont(new Font("Monospaced", Font.PLAIN, 12)); // NOI18N
        }

        BorderLayout layout = new BorderLayout();
        setLayout(layout);
        screen = new Screen(this,
                (buf.visibleCols() * metrics.width +
                glyph_gutter_width +
                debug_gutter_width),
                st.rows * metrics.height);

        add(BorderLayout.CENTER, screen);
        screen.setBackground(null);	// inherit from this
        screen.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        adjust_lines(st.rows);
        st.cursor.row = 0;

        vscroll_bar = new JScrollBar(JScrollBar.VERTICAL);
        add(BorderLayout.EAST, vscroll_bar);
        vscroll_bar.setValues(st.firstx, st.rows - 1, 0, st.rows);
        vscroll_bar.setUnitIncrement(1);
        vscroll_bar.setEnabled(true);
        vscroll_bar.setFocusable(false);

        vscroll_bar.addAdjustmentListener(new AdjustmentListener() {

	    @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                // JScrollBar sb = (JScrollBar) e.getAdjustable();
                switch (e.getAdjustmentType()) {
                    case AdjustmentEvent.TRACK:

                        int pos = e.getValue();

                        // adjustmentValueChanged() will get called when we
                        // call setValues() on the scrollbar.
                        // This test sort-of filters that out
                        if (pos == st.firstx) {
                            break;
                        }

                        // deal with the user moving the thumb
                        st.firstx = pos;
                        /* DEBUG
                        if (st.firstx + st.rows > buf.nlines) {
                        Thread.dumpStack();
                        printStats("bad scroll value");	// NOI18N
                        }
                         */
                        repaint(false);
                        break;

                    default:
                        /* DEBUG
                        System.out.println("adjustmentValueChanged: " + e); // NOI18N
                         */
                        break;
                }
            }
        });


        hscroll_bar = new JScrollBar(JScrollBar.HORIZONTAL);
        hscroll_bar.setValues(st.firsty, buf.totalCols() - 1, 0, buf.totalCols());
        hscroll_bar.setUnitIncrement(1);
        hscroll_bar.setEnabled(true);
        hscroll_bar.setFocusable(false);

        hscroll_wrapper = new ScrollWrapper(hscroll_bar);
        add(BorderLayout.SOUTH, hscroll_wrapper);

        hscroll_bar.addAdjustmentListener(new AdjustmentListener() {

	    @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                // JScrollBar sb = (JScrollBar) e.getAdjustable();
                switch (e.getAdjustmentType()) {
                    case AdjustmentEvent.TRACK:

                        int pos = e.getValue();

                        // adjustmentValueChanged() will get called when we
                        // call setValues() on the scrollbar.
                        // This test sort-of filters that out
                        if (pos == st.firsty) {
                            break;
                        }

                        // deal with the user moving the thumb
                        st.firsty = pos;
                        repaint(false);
                        break;

                    default:
                        /* DEBUG
                        System.out.println("adjustmentValueChanged: " + e); // NOI18N
                         */
                        break;
                }
            }
        });

        screen.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = screen.getSize();
                sizeChanged(size.width, size.height);

                // workaround for java bug 4711314 where a repaint()
                // appears before componentResized() causing issue 25313
                repaint(true);
            }
        });

        screen.addKeyListener(new KeyListener() {

            // We consume all events so no additional side-effects take place.
            // Examples:
            // - We don't want TAB to move focus away. (see more below)
            // - We don't want ^p to bring up a printer diaolog
            // - etc
            // HACK. Java maps [Return] to 10 as opposed to 13. This is due
            // to it's Windows-chauvinism, a 'reality' pointed out to me by
            // one of the AWT people.
            // The keycode doesn't make it to keyTyped, so for now we detect
            // a Return by capturing press/releases of VK_ENTER and
            // use a flag.
            private boolean saw_return;

	    @SuppressWarnings("AssignmentToMethodParameter")
            private void charTyped(char c, KeyEvent e) {
                if (read_only) {
                    return;
                }

                if (c == 10 && saw_return) {
                    saw_return = false;
                    c = (char) 13;
                }

                // Consume ctrl+tab, ctrl+shift+tab event, see #237990
                if (keymap != null && (c == KeyEvent.VK_TAB)
                        && ((e.getModifiers() == KeyEvent.CTRL_MASK)
                        || (e.getModifiers() == (KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK)))) {
                    e.consume();
                }

                if (passOn && maybeConsume(e)) {
		    if ((e.getModifiers() & KeyEvent.ALT_MASK) == KeyEvent.ALT_MASK) {
			if (getAltSendsEscape()) {
			    on_char(ESC);
			    on_char(c);
			} else {
			    if (c >=0 || c <= 127)
				c += 128;
			    on_char(c);
			}
		    } else {
			on_char(c);
		    }
                    possiblyScrollOnInput();
                }

                hscrollReset(c);
            }

	    @Override
            public void keyTyped(KeyEvent e) {

                char c = e.getKeyChar();

                if (debugKeys()) {
                    System.out.printf("term: keyTyped: %s\n", e); // NOI18N
                    System.out.printf("term: keyTyped: char '%c' %04x\n",// NOI18N
                            c, (int) c);
                }

                charTyped(c, e);
            }

	    @Override
            public void keyPressed(KeyEvent e) {
                if (debugKeys()) {
                    System.out.printf("keyPressed %2d %s\n", e.getKeyCode(), KeyEvent.getKeyText(e.getKeyCode())); // NOI18N
                }
                KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);

                // At first check the keymap (higher priority)
                if (keymap != null) {
                    Action action = keymap.getAction(ks);
                    if (action != null) {
                        if (allowedActions == null || allowedActions.contains(action.getClass().getName())) {
                            return;
                        }
                    }
                }

                // Then let Interp's have go at special keys
                interp.keyPressed(e);
                if (e.isConsumed()) {
                    passOn = false;
                    return;
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        if (onMac()) {
                            // On a Mac AWT doesn't provide us with a keyTyped()
                            // for VK_ESCAPE so we simulate it.
                            charTyped(ESC, e);
                        }
                        break;
                    case KeyEvent.VK_COPY:
                        copyToClipboard();
                        break;
                    case KeyEvent.VK_PASTE:
                        pasteFromClipboard();
                        break;
                    case KeyEvent.VK_ENTER:
                        saw_return = true;
                        break;
                    case KeyEvent.VK_PAGE_UP:
                        if (e.getModifiers() == Event.CTRL_MASK) {
                            pageLeft(1);
                        } else {
                            pageUp(1);
                        }
                        break;
                    case KeyEvent.VK_PAGE_DOWN:
                        if (e.getModifiers() == Event.CTRL_MASK) {
                            pageRight(1);
                        } else {
                            pageDown(1);
                        }
                        break;

                    case KeyEvent.VK_UP:
                        if (e.getModifiers() == Event.CTRL_MASK) {
                            lineUp(1);
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (e.getModifiers() == Event.CTRL_MASK) {
                            lineDown(1);
                        }
                        break;
                    case KeyEvent.VK_LEFT:
                        break;
                    case KeyEvent.VK_RIGHT:
                        break;
                }
                passOn = maybeConsume(e);
            }

	    @Override
            public void keyReleased(KeyEvent e) {
                /* DEBUG
                System.out.println("keyReleased"); // NOI18N
                 */

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    /* DEBUG
                    System.out.println("keyReleased VK_ENTER"); // NOI18N
                     */
                    saw_return = false;
                }
                maybeConsume(e);
            }
        });

        screen.addMouseMotionListener(new MouseMotionListener() {

	    @Override
            public void mouseDragged(MouseEvent e) {
                /* DEBUG
                System.out.println("mouseDragged"); // NOI18N
                 */

                // akemr - fix of #25648
                if (SwingUtilities.isRightMouseButton(e)) {
                    return;
                }

                if (left_down_point != null) {
                    BCoord bc = toBufCoords(toViewCoord(left_down_point));
                    sel.track(new Coord(bc, firsta));
                    left_down_point = null;
                }
                drag_point = e.getPoint();
                /* DEBUG
                System.out.println("mouseDrag: " + drag_point); // NOI18N
                 */

                int scroll_direction = 0;

                if (drag_point.y < 0) {
                    scroll_direction |= Scroller.UP;
                } else if (drag_point.y > screen.getSize().height) {
                    scroll_direction |= Scroller.DOWN;
                }

                if (drag_point.x < 0) {
                    scroll_direction |= Scroller.LEFT;
                } else if (drag_point.x > screen.getSize().width) {
                    scroll_direction |= Scroller.RIGHT;
                }

                scroll_to(scroll_direction, e);
            }

	    @Override
            public void mouseMoved(MouseEvent e) {
                /* DEBUG
                Point p = (Point) e.getPoint().clone();
                BCoord bc = toBufCoords(toViewCoord(p));
                Coord c = new Coord(bc, firsta);
                Extent x = sel.getExtent();
                if (x == null) {
                System.out.println("sel intersect: no extent");	// NOI18N
                } else {
                System.out.println("sel intersect: " +	// NOI18N
                (x.intersects(c.row, c.col)? "intersects"	// NOI18N
                "doesn't intersect"));	// NOI18N
                }
                 */
            }
        });

        addMouseWheelHandler(screen, vscroll_bar);

        screen.addMouseListener(new MouseListener() {

	    @Override
            public void mouseClicked(MouseEvent e) {
                // BCoord bcoord = toBufCoords(toViewCoord(e.getPoint()));

                if (SwingUtilities.isLeftMouseButton(e)) {
                    /* DEBUG
                    System.out.println("LEFT click"); // NOI18N
                     */
                    if (click_to_type) {
                        requestFocus();
                    }

                } else if (SwingUtilities.isMiddleMouseButton(e)) {
                    /* DEBUG
                    System.out.println("MIDDLE click"); // NOI18N
                    System.out.println("Selection: '" + sel.sel_get() + "'"); // NOI18N
                     */
                    pasteFromSelection();
                    
                    // See IZ 193527
                    if (click_to_type) {
                        requestFocus();
                    }
                }
            }

	    @Override
            public void mousePressed(MouseEvent e) {
                /* DEBUG
                System.out.println("mousePressed "+e.getModifiers()); // NOI18N
                 */

                if (SwingUtilities.isLeftMouseButton(e)) {

                    if (e.isShiftDown()) {
                        // JLF/dtterm selection extension
                        // Actually it's _addition_ so SHOULD enhance Sel
                        // to do that instead.
                        BCoord bc = toBufCoords(toViewCoord(e.getPoint()));
                        if (sel.extend(new Coord(bc, firsta))) {
                            fireSelectionExtentChanged();
                            repaint(false);
                        }
                        return;
                    }

                    if (sel.cancel(false)) {
                        repaint(false);
                    }

		    switch (e.getClickCount()) {
		    	case 1:
			    left_down_point = (Point) e.getPoint().clone();
			    break;
		    	case 2:
			    {
				BCoord bcoord = toBufCoords(toViewCoord(e.getPoint()));
				BExtent word = buf.find_word(word_delineator, bcoord);
				sel.select_word(word.toExtent(firsta));
				repaint(false);
				break;
			    }
		    	case 3:
			    {
				BCoord bcoord = toBufCoords(toViewCoord(e.getPoint()));
				BExtent line = buf.find_line(bcoord);
				sel.select_line(line.toExtent(firsta));
				repaint(false);
				break;
			    }
		    	default:
			    break;
		    }

                    fireSelectionExtentChanged();
                }
            }

	    @Override
            public void mouseReleased(MouseEvent e) {
                /* DEBUG
                System.out.println("mouseReleased"); // NOI18N
                 */
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (click_to_type) {
                        requestFocus();
                    }

                    if (e.isShiftDown()) {
                        // we're extending
                        return;
                    }

                    if (scroller != null) {
                        scroller.interrupt();
                        scroller = null;
                    }

                    // Don't put in the selection if we didn't move
                    // When left button goes down 'left_down_point' is set.
                    // As soon as we move the mouse it gets reset.
                    if (left_down_point == null) {
                        sel.done( /* OLD false */);
                    }
                    left_down_point = null;
                }
            }

            /*
             * Implement follow-mouse focus
             */
	    @Override
            public void mouseEntered(MouseEvent e) {
                /* DEBUG
                System.out.println("mouseEntered"); // NOI18N
                 */
                if (!click_to_type) {
                    requestFocus();
                }
            }

	    @Override
            public void mouseExited(MouseEvent e) {
                /* DEBUG
                System.out.println("mouseExited"); // NOI18N
                 */
            }
        });

        screen.addFocusListener(new FocusListener() {

	    @Override
            public void focusGained(FocusEvent e) {
                /* DEBUG
                System.out.println("Focus gained >>>>>>>>>>>>>>>>>>>>>>>"); // NOI18N
                 */
                has_focus = true;
                repaint(false);
            }

	    @Override
            public void focusLost(FocusEvent e) {
                /* DEBUG
                Component o = e.getOppositeComponent();
                System.out.println("Focus lost <<<<<<<<<<<<<<<<<<<<<<<" + o);
                 */

                has_focus = false;
                repaint(false);
            }
        });
    }

    static void indent(boolean indented) {
        if (indented) {
            System.out.print("\t");   // NOI18N
        }
    }
    private MemUse lastMemUse = new MemUse();

    /*
     * Print interesting statistics and facts about this Term
     */
    public void printStats(String message) {
        boolean indented = (message != null);

        if (message != null) {
            System.out.println(message);
        }

        buf.printStats(indented);

        indent(indented);
        System.out.println("  View:" + // NOI18N
                "  rows " + st.rows + // NOI18N
                "  v cols " + buf.visibleCols() + // NOI18N
                "  t cols " + buf.totalCols() + // NOI18N
                "  history " + history_size);       // NOI18N

        indent(indented);
        System.out.println("       " + // NOI18N
                "  firstx " + st.firstx + // NOI18N
                "  firsty " + st.firsty + // NOI18N
                "  firsta " + firsta);              // NOI18N

        indent(indented);
        System.out.println("       " + // NOI18N
                "  gutter " + glyph_gutter_width);  // NOI18N

        indent(indented);
        System.out.println("Cursor:" + // NOI18N
                "  " + st.cursor + // NOI18N
                "  topMargin " + topMargin() + // NOI18N
                "  botMargin " + botMargin());	// NOI18N

        printCounts(indented);

        MemUse memUse = new MemUse();
        MemUse delta = memUse.changeFrom(lastMemUse);

        indent(indented);
        memUse.print("Memory:");                    // NOI18N
        indent(indented);
        delta.print(" Delta:");                    // NOI18N
    }

    public void printCounts(boolean indented) {
        indent(indented);
        System.out.println("Counts:" + // NOI18N
                "  putChar() " + n_putchar + // NOI18N
                "  putChars() " + n_putchars);  // NOI18N

        indent(indented);
        System.out.println("       " + // NOI18N
                "  linefeeds " + n_linefeeds);	// NOI18N

        indent(indented);
        System.out.println("       " + // NOI18N
                "  repaint() " + n_repaint + // NOI18N
                "  paint() " + n_paint);	// NOI18N
    }

    public void resetStats() {
        n_putchar = 0;
        n_putchars = 0;
        n_linefeeds = 0;
        n_repaint = 0;
        n_paint = 0;
        lastMemUse = new MemUse();
    }

    private static class MemUse {

        private final long free;
        private final long max;
        private final long total;

        public MemUse() {
            free = Runtime.getRuntime().freeMemory();
            max = Runtime.getRuntime().maxMemory();
            total = Runtime.getRuntime().totalMemory();
        }

        private MemUse(long free, long max, long total) {
            this.free = free;
            this.max = max;
            this.total = total;
        }

	@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
        public MemUse changeFrom(MemUse old) {
            return new MemUse(this.free - old.free,
                    this.max - old.max,
                    this.total - old.total);
        }

        private long unused() {
            return max - (total + free);
        }

        private void print(String msg) {
            System.out.println(msg +
                    "  max " + max / 1024 + "K" + " = " + // NOI18N
                    "  total " + total / 1024 + "K" + " + " + // NOI18N
                    "  free " + free / 1024 + "K" + " + " + // NOI18N
                    "  unused " + unused() / 1024 + "K" // NOI18N
                    );
        }
    }

    public void paste() {
        pasteFromClipboard();
    }

    private void pasteHelp(Clipboard cb) {
        if (read_only) {
            return;
        }

        Transferable contents = cb.getContents(screen);
        if (contents == null) {
            return;
        }

        if (!contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return;
        }

        try {
            String string;
            string = (String) contents.getTransferData(DataFlavor.stringFlavor);
            
            // bug #237034
            if (string == null) {
                return;
            }
            /* DEBUG
            System.out.println("System selection contains '" + string + "'"); // NOI18N
             */
            char ca[] = string.toCharArray();
            sendChars(ca, 0, ca.length);
        } catch (UnsupportedFlavorException | IOException e) {
            //
        }
    }

    /**
     * Transfer contents of selection to Terms input stream.
     * <p>
     * The pasted content is sent through sendChars.
     * <br>
     * The paste will silently fail if:
     * <ul>
     * <li> The selection has null contents.
     * <li> The selections data flavor is not string.
     * </ul>
     */
    public void pasteFromSelection() {
        /* DEBUG
        System.out.println("Term: pasteFromSelection()"); // NOI18N
         */
        if (systemSelection == null) {
            return;
        }
        pasteHelp(systemSelection);
    }

    /**
     * Transfer contents of clipboard to Terms input stream.
     * <p>
     * The pasted content is sent through sendChars.
     * <br>
     * The paste will silently fail if:
     * <ul>
     * <li> The selection has null contents.
     * <li> The selections data flavor is not string.
     * </ul>
     */
    public void pasteFromClipboard() {
        pasteHelp(systemClipboard);
    }

    /**
     * Transfer selected text into clipboard.
     */
    public void copy() {
        copyToClipboard();
    }

    /**
     * Transfer selected text into clipboard.
     */
    public void copyToClipboard() {
        String text = sel.getSelection();
        if (text != null) {
            StringSelection ss = new StringSelection(text);
            systemClipboard.setContents(ss, sel);
        }
    }

    /**
     * Transfer selected text into selection.
     */
    public void copyToSelection() {
        /* DEBUG
        System.out.println("Term: copyToSelection()");	// NOI18N
         */

        if (systemSelection == null) {
            return;
        }
        String text = sel.getSelection();
        StringSelection ss = new StringSelection(text);	// null 'text' is OK
        systemSelection.setContents(ss, sel);
    }
    private static Extent old_extent = null;

    void fireSelectionExtentChanged() {
        Extent new_extent = getSelectionExtent();
        firePropertyChange("selectionExtent", old_extent, new_extent); // NOI18N
        old_extent = new_extent;
    }

    /**
     * Set the number of character rows in the screen.
     * <p>
     * See setRowsColumns() for some additional important information.
     * @param rows The property value.
     */
    public void setRows(int rows) {
        if (old_rows == -1) {
            old_rows = st.rows;
        }
        st.rows = rows;
        resetMargins();

        updateScreenSize();
    }

    /**
     * Get the number of character rows in the screen
     * @return The property value.
     */
    public int getRows() {
        return st.rows;
    }

    /**
     * Set the number of character columns in the screen.
     * <p>
     * See setRowsColumns() for some additional important information.
     * @param cols The property value.
     */
    public void setColumns(int cols) {
        buf.setVisibleCols(cols);
        updateScreenSize();
    }

    /**
     * Get the number of character columns in the screen
     * @return The property value.
     */
    public int getColumns() {
        return buf.visibleCols();
    }

    /**
     * Trampoline from Line.ensureCapacity() to Buffer.noteColumn()
     */
    void noteColumn(Line l, int capacity) {
        int vcapacity = l.bufToCell(metrics, capacity);
        buf.noteColumn(vcapacity);
    }

    /**
     * Trampoline from Line to MyFontMetrics.checkForMultiCell()
     */
    void checkForMultiCell(char c) {
        metrics.checkForMultiCell(c);
    }

    /**
     * Simultaneously set the number of character rows and columns.
     * <p>
     * A Term is a composite widget made of a contained screen (getScreen())
     * and a scrollbar. It is not a JScrollPane. You're actually setting
     * the screen size here.
     * <p>
     * Setting the column size also sets the width of the buffer. This doesn't
     * alter the length (column at which lines were wrapped) of past lines,
     * but only new additional lines. For example, if you set columns to
     * 20, print a bunch of lines that wrap, then resize to 80 columns, all
     * the lines that were wrapped to 20 will stay wrapped that way. This is
     * consistent with xterm behavior.
     * <p>
     * If this Term is embedded in a component with a layout manager that is
     * set up to accept child resizes gracefully this widget will be resized
     * as expected.
     * <p>
     * Alternatively if this Term is embedded in a Window (like JFrame)
     * the window will need to be re-pack()ed as it does not accommodate it's
     * children's size changes. This has to be done by the application using
     * Term. The best way to do this is to add a TermListener() and call pack()
     * on the appropriate window when a resize notification is fired.
     * @param rows
     * @param columns
     */
    public void setRowsColumns(int rows, int columns) {

        // Combine code in setRows() and setColumns() so we factor
        // the calls to updateScreenSize().

        if (old_rows == -1) {
            old_rows = st.rows;
        }
        st.rows = rows;
        resetMargins();
        buf.setVisibleCols(columns);

        updateScreenSize();
    }

    /**
     * Governs whether Term will round down resize requests to character
     * cell size.
     * <p>
     * Sizes are usually set by containers' layout managers. If rounding is
     * enabled Term will attempt to adjust the size to an even multiple
     * of the character cell size.
     * <p>
     * The layout manager might not necessarily honor the rounded size.
     * The situation can be somewhat improved by making sure that the ultimate
     * container is re-packed as described in {@link #setRowsColumns(int, int)}.
     * @param size_rounded The property value.
     */
    public void setSizeRounded(boolean size_rounded) {
        this.size_rounded = size_rounded;
        updateScreenSize();
    }

    /**
     * Returns true if Term will round down resize requests to character
     * cell size.
     * <p>
     * See {@link #setSizeRounded(boolean)} for more info.
     * @return The property value.
     */
    public boolean isSizeRounded() {
        return size_rounded;
    }
    private boolean size_rounded = true;

    /*
     * Used to extract the dimensions of the terminal.
     * SHOULD replace with getRowsCols and getScreenSize
     */
    public void fillSizeInfo(Dimension cells, Dimension pixels) {
        cells.height = st.rows;
        cells.width = buf.visibleCols();
        Dimension cpixels = screen.getSize();
        pixels.width = cpixels.width - glyph_gutter_width - debug_gutter_width;
        pixels.height = cpixels.height;
    }

    /**
     * Once the terminal is connected to something, use this function to
     * send all Term Listener notifications.
     */
    protected void updateTtySize() {
        if (screen != null) {
            Dimension cells = new Dimension(buf.visibleCols(), st.rows);
            Dimension pixels = screen.getSize();
            fireSizeChanged(cells, pixels);
        }
    }

    /*
     * various coordinate conversion functions
     */
    BCoord toViewCoord(BCoord b) {
        /*
         * Convert from buffer coords to view coords
         */
        Line l = buf.lineAt(b.row);
        if (l != null) { //XXX Hotfix for issue 40189 - Buffer.lineAt() will
            //catch an AIOBEE and return null.  Probably related
            //to Ivan's notes about clipping in Term.paint()
            int vc = buf.lineAt(b.row).bufToCell(metrics, b.col);
            BCoord v = new BCoord(b.row - st.firstx, vc - st.firsty);
            return v;
        } else {
            return null;
        }
    }

    Point toPixel(BCoord v) {
        /*
         * Convert from view coords to pixel coords
         */
        Point p = new Point(v.col * metrics.width +
                glyph_gutter_width +
                debug_gutter_width,
                v.row * metrics.height);
        return p;
    }

    /**
     * Convert row/column coords to pixel coords within the widgets
     * coordinate system.
     * It returns the pixel of the upper left corner of the target cell.
     * @param target
     * @return 
     */
    public Point toPixel(Coord target) {
        BCoord btarget = target.toBCoord(firsta);
        return toPixel(btarget);
    }

    /**
     * Convert pixel coords to view row/column coords (0/0-origin)
     */
    BCoord toViewCoord(Point p) {
        BCoord v = new BCoord(p.y / metrics.height,
                (p.x - glyph_gutter_width - debug_gutter_width) / metrics.width);
        v.clip(st.rows, buf.visibleCols());
        /* DEBUG
        System.out.println("toViewCoord() -> " + v); // NOI18N
         */
        return v;
    }

    BCoord toBufCoords(BCoord v) {
        /*
         * Convert view row/column coords to buffer row/column coords.
         * If the buffer is smaller than the view, map to the last line.
         */
        int brow = st.firstx + v.row;
        if (brow >= buf.nlines()) {
            brow = buf.nlines() - 1;
        }
        int bc = buf.lineAt(brow).cellToBuf(metrics, st.firsty + v.col);
        BCoord b = new BCoord(brow, bc);
        /* DEBUG
        System.out.println("toBufCoords(" + v + ") -> " + b); // NOI18N
         */
        return b;
    }

    /**
     * Convert pixel coords to view (visible area) row/column coords (both
     * 0-origin).
     * <p>
     * In the returned Point, x represents the column, y the row.
     * @param p
     * @return 
     */
    public Point mapToViewRowCol(Point p) {
        BCoord c = toViewCoord(p);
        return new Point(c.col, c.row);
    }

    /**
     * Convert pixel coords to (history) buffer row/column coords (both
     * 0-origin).
     * <p>
     * In the returned Point, x represents the column, y the row.
     * @param p
     * @return 
     */
    public Point mapToBufRowCol(Point p) {
        BCoord c = toBufCoords(toViewCoord(p));
        return new Point(c.col, c.row);
    }

    private Color rendition_to_color(int rendition) {
	final int px = Attr.rendition_to_pindex(rendition);
	if (px == -1)
	    return null;
	else
	    return palette[px];
    }
    private Color actual_foreground;
    private Color actual_background;
    private boolean check_selection;
    private int totcols;

    private void do_run(Graphics g, int yoff, int xoff, int baseline,
            int brow, /* OLD char buf[], */ Line l,
            int attr, int rbegin, int rend) {

        /* DEBUG
        System.out.println("do_run(" + rbegin + ", " + rend + ")");	// NOI18N
         */

        int x;
        int rlength;
        int xlength;

        if (metrics.isMultiCell()) {
            int vbegin = l.bufToCell(metrics, rbegin);
            int vend = l.bufToCell(metrics, rend + 1) - 1;
            x = xoff + (vbegin - st.firsty) * metrics.width;
            int vlength = vend - vbegin + 1;
            if (vlength <= 0) {
                /* DEBUG
                System.out.println("do_run(" + rbegin + ", " + rend + ")");	// NOI18N
                 */
                return;
            }
            rlength = rend - rbegin + 1;
            xlength = vlength * metrics.width;

        } else {
            x = xoff + (rbegin - st.firsty) * metrics.width;
            rlength = rend - rbegin + 1;
            if (rlength <= 0) {
                /* DEBUG
                System.out.println("do_run(" + rbegin + ", " + rend + ")");	// NOI18N
                 */
                return;
            }
            xlength = rlength * metrics.width;
        }

        boolean reverse = Attr.REVERSE.isSet(attr);
        boolean active = Attr.ACTIVE.isSet(attr);

        // choose background color
        Color bg;

        if (active) {
            bg = active_color;
	} else {
	    if (Attr.BGCOLOR.isSet(attr) || Attr.REVERSE.isSet(attr)) {
		bg = backgroundColor(reverse, attr);
	    } else if (l.getBackgroundColor() != 0) {
		bg = rendition_to_color(l.getBackgroundColor());
	    } else {
		// Allow existing BG, i.e. selection, to show through.
		bg = null;
	    }
        }

        if (bg != null) {
            // Draw any background
            g.setColor(bg);
            g.fillRect(x, yoff, xlength, metrics.height - metrics.leading);
        }

        // Set foreground color
        Color fg = foregroundColor(reverse, attr);
        g.setColor(fg);

        // draw any underscores
        if (Attr.UNDERSCORE.isSet(attr)) {
            int h = metrics.height - metrics.leading - 1;
            g.drawLine(x, yoff + h, x + xlength, yoff + h);
        }

        // Draw the foreground character glyphs
        myDrawChars(g, /* OLD buf, */ l, rbegin, rlength, x, baseline);

        // Draw fake bold characters by redrawing one pixel to the right
        if (Attr.BRIGHT.isSet(attr)) {
            myDrawChars(g, /* OLD buf, */ l, rbegin, rlength, x + 1, baseline);
        }
    }
    private final Point newp = new Point();

    /*
     * Tweak glyph X positions so they fall on cell/grid/column boundries.
     */
    private void massage_glyphs(GlyphVector gv, int start, int n, Line l) {
        Point2D pos0 = gv.getGlyphPosition(0);

        // There's one big assumption here that in a monospaced font all the
        // Y placements are identical. So we use the placement for the first
        // glyph only.
        newp.y = (int) pos0.getY();

        int col = (int) pos0.getX();
        for (int gx = 0; gx < n; gx++) {
            newp.x = col;
            gv.setGlyphPosition(gx, newp);
            col += l.width(metrics, start + gx) * metrics.width;
        }
    }
    /**
     * Draw characters in cells.
     *
     * Fixed width or monospaced fonts implies that the glyphs of all characters
     * have the same width. Some non-latin characters (japanese) might have
     * glyph widths that are an _integer multiple_ of the latin glyphs. Thus
     * cellular (grid based) text widget like this termulator can still place
     * all characters nicely. There is a 'C' function wcwidth() which
     * ... determines the number of _column_ positions ... and CDE's DtTrem
     * ultimately depends on it to place things. (See also Tuthill & Smallberg,
     * "Creating worldwide software" PrenticeHall 2nd ed. p98)
     *
     * Unfortunately the fonts used by Java, even the "monospaced" fonts, do
     * not abide by the above convention. I measured a 10pt ja locale latin
     * character at 7 pixels wide and a japanese character at 12 pixels wide,
     * instead of 14. A similar problem existed with respect to the "unprintbale"
     * placeholder square. Until Java 1.4 it used to be 9 or 10 pixels wide!
     * The square is fixed, but I"m not sure the above will be anytime soon.
     *
     * What this means is that Graphics.drawString() when given a mix and match
     * of latin and japanese characters will not place them right. Selection
     * doesn't work etc.
     *
     * Nor does Java provide anything resembling wcwidth() so we're rolling
     * our own here. That's done in Line.width().
     *
     * So one approach would be to place each character individually, but it's
     * rather slow. Fortunately Java provides a GlyphVector class that allows
     * us to tweak the positions of the glyphs. The timing I"ve gotten are
     *		50	for one drawChars() per charactr. (SLOWER below)
     *		15	using the GlyphVector technique
     *		8	using plain drawChars
     * Unfortunately GlyphVector's interface leaves a bit to be desired.
     * - It does not take a (char [], offset, length) triple and depends
     *   on the length of the char array passed in. Since our Line char arrays
     *   have some slop in them we can't pass them directly. Hence the
     *	 "new char[]" and the "System.arraycopy".
     * - The interface for getting and setting positions is also a bit
     *	 awkward as you may notice from massage_glyphs().
     *
     * We SHOULD fall back on plain drawChars() if the host charset is an
     * 8 bit encoding like ASCII or ISO 8859. This encoding is available
     * via System.getProperty("file.encoding") but there are so many aliases
     * for each that I"m wary of hardcoding tests. See
     * http://www.iana.org/assignments/character-sets
     * Java 1.4 has class Charset that helps with the aliases but we can't
     * yet lock into 1.4.
     */
    private char[] xferBuf = new char[80];

    private void myDrawChars(Graphics g, /* OLD char buf[], */ Line l,
            int start, int howmany, int xoff, int baseline) {

        if (xferBuf.length < l.length()) {
            xferBuf = new char[l.length()];
        }
        // OLD final char buf[] = l.XcharArray();
        l.getChars(xferBuf);

        // Use rendering hints (antialiasing etc.)
        Map<?,?> hints = renderingHints;
        if ((hints != null) && (g instanceof Graphics2D)) {
            ((Graphics2D) g).setRenderingHints(hints);
        }
        if (metrics.isMultiCell()) {
            // slow way
            // This looks expensive but it is in fact a whole lot faster
            // than issuing a g.drawChars() _per_ character

            Graphics2D g2 = (Graphics2D) g;
            FontRenderContext frc = g2.getFontRenderContext();
            // Gaaah, why doesn't createGlyphVector() take a (char[],offset,len)
            // triple?
            char[] tmp = new char[howmany];
            System.arraycopy(xferBuf, start, tmp, 0, howmany);
            GlyphVector gv = getFont().createGlyphVector(frc, tmp);
            massage_glyphs(gv, start, howmany, l);
            g2.drawGlyphVector(gv, xoff, baseline);
        } else {
            // fast way
            g.drawChars(xferBuf, start, howmany, xoff, baseline);
        }
    }

    /*
     * Render one line
     * Draw the line on this brow (buffer row 0-origin)
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    private void paint_line_new(Graphics g, Line l, int brow,
            int xoff, int yoff, int baseline,
            Extent selx) {

        int length = l.length();
        if (length == 0) {
            return;
        }

        int lastcol;
        int firstcol;

        if (metrics.isMultiCell()) {

            // Figure what buffer column is the first visible one (moral
            // equivalent of st.firsty)

            // SHOULD replace with something that does cellToBuf/bufToCell
            // all at once. There are a couple of other occurances of this
            // pattern.

            firstcol = l.cellToBuf(metrics, st.firsty);
            int inverse_firstcol = l.bufToCell(metrics, firstcol);
            int delta = st.firsty - inverse_firstcol;
            if (delta > 0) {
                /* This is what to do if we want to draw the right half of the
                 * glyph. However the left half of it will end up in the glyph
                 * gutter and to compensate for thet we'll need to tweak the
                 * clip region. For now taking the easy way out>

                int pdelta = delta * metrics.width;	// pixel delta
                xoff -= pdelta;

                 */

                firstcol++;
                int pdelta = delta * metrics.width;	// pixel delta
                xoff += pdelta;
            }

            lastcol = l.cellToBuf(metrics, st.firsty + buf.visibleCols() - 1);

        /* DEBUG
        System.out.print
        ("firstcol = " + firstcol + " for firsty " + st.firsty); // NOI18N
        System.out.print
        (" delta = " + delta); // NOI18N
        System.out.println
        (" lastcol = " + lastcol +	// NOI18N
        " for visibleCols " + buf.visibleCols()); // NOI18N
         */

        } else {
            lastcol = st.firsty + buf.visibleCols() - 1;
            firstcol = st.firsty;
        }


        lastcol = Math.min(lastcol, length - 1);
        if (firstcol > lastcol) {
            return;
        }
        int howmany = lastcol - firstcol + 1;


        // 'length' is not used from here on down

        // OLD char buf[] = l.charArray();

        if (!l.hasAttributes()) {

            if (debugWrap()) {
                if (l.isWrapped() && l.isAboutToWrap()) {
                    g.setColor(Color.red);	// not a good state to be in
                } else if (l.isAboutToWrap()) {
                    g.setColor(Color.orange);
                } else if (l.isWrapped()) {
                    g.setColor(Color.magenta);
                }
            }

            myDrawChars(g, /* OLD buf, */ l, firstcol, howmany, xoff, baseline);


            return;
        }

        int attrs[] = l.attrArray();

        // find the extent of the selection on this line
        int sbegin = -1;
        int send = -1;
        if (check_selection && selx != null) {
            int arow = firsta + brow;
            Coord b = selx.begin;
            Coord e = selx.end;
            if (b.row <= arow && e.row >= arow) {
                if (b.row == e.row) {
                    sbegin = b.col;
                    send = e.col;
                } else if (arow == b.row) {
                    sbegin = b.col;
                    send = totcols;
                } else if (arow == e.row) {
                    sbegin = 0;
                    send = e.col;
                } else {
                    sbegin = 0;
                    send = totcols;
                }
            }
        }

        // iterate through runs

        int rbegin = firstcol;
        int rend;

        while (true) {

            // find a "run"
            // A run, as in run-length-encoding, is a set of characters with
            // the same attributes.

            int attr = attrs[rbegin];
            rend = rbegin + 1;
            while (rend <= lastcol) {
                if (attrs[rend] != attr) {
                    break;
                }
                rend++;
            }
            rend--;

            // render the run
            // need to do this with awareness of the selection
            // parts that fall under the selection are rendered with an
            // alternative attribute set.

            int alt_attr = attr & ~ Attr.ALT;
            if (sbegin == -1 || send < rbegin || sbegin > rend) {
                // run is not in selection
                do_run(g, yoff, xoff,
                        baseline, brow, /* OLD buf, */ l, attr, rbegin, rend);

            } else if (sbegin <= rbegin && send >= rend) {
                // run entirely in selection
                /* DEBUG
                System.out.println("run entirely in selection");	// NOI18N
                 */
                do_run(g, yoff, xoff,
                        baseline, brow, /* OLD buf, */ l, alt_attr, rbegin, rend);

            } else if (sbegin > rbegin && send < rend) {
                // selection fully within run
                // split into three parts
                /* DEBUG
                System.out.println("run selection fully within run");	// NOI18N
                 */
                do_run(g, yoff, xoff,
                        baseline, brow, /* OLD buf, */ l, attr, rbegin, sbegin - 1);
                do_run(g, yoff, xoff,
                        baseline, brow, /* OLD buf, */ l, alt_attr, sbegin, send);
                do_run(g, yoff, xoff,
                        baseline, brow, /* OLD buf, */ l, attr, send + 1, rend);

            } else if (sbegin <= rbegin) {
                // selection covers left portion of run
                /* DEBUG
                System.out.println("selection covers left portion of run");	// NOI18N
                 */
                // split into two parts
                do_run(g, yoff, xoff,
                        baseline, brow, /* OLD buf, */ l, alt_attr, rbegin, send);
                do_run(g, yoff, xoff,
                        baseline, brow, /* OLD buf, */ l, attr, send + 1, rend);

            } else if (send >= rend) {
                // selection covers right portion of run
                // split into two parts
                /* DEBUG
                System.out.println("selection covers right portion of run");	// NOI18N
                 */
                do_run(g, yoff, xoff,
                        baseline, brow, /* OLD buf, */ l, attr, rbegin, sbegin - 1);
                do_run(g, yoff, xoff,
                        baseline, brow, /* OLD buf, */ l, alt_attr, sbegin, rend);

            } else {
                /* DEBUG
                System.out.println("Odd run/selection overlap");	// NOI18N
                 */
            }

            if (rend >= lastcol) {
                break;
            }

            // shift
            rbegin = rend + 1;
        }
    }

    /* OLD NPE-x synchronized */ void do_paint(Graphics g) {
        ckEventDispatchThread();

        /*
         * Render the buffer unto the screen
         * SHOULD try theses:
         *	- use drawChars?
         *	- make passes first the glyphs and BG, then chars, then cursor
         *	- use drawString(AttributedCharacterIterator iterator, ...)
         *	- precompute any metrics related stuff
         */
        if (st.firstx == -1) {
            /* DEBUG
            System.out.println("Term.paint() no lines"); // NOI18N
             */
            return;
        }

        /* DEBUG
        long paint_start_time = System.currentTimeMillis();
         */

        // If Screen is opaque it seems that there is a bug in Swing where
        // the Graphics that we get here ends up with fonts other than what
        // we assigned to Term. So we make doubly sure here.
        g.setFont(getFont());

        n_paint++;

	actual_foreground = palette[Attr.PAL_FG];
	actual_background = palette[Attr.PAL_BG];

        // clear the screen
        g.setColor(actual_background);
        g.fillRect(0, 0, screen.getSize().width, screen.getSize().height);

        // draw any BG stripes
        // do this before the selection


        int xoff = debug_gutter_width + glyph_gutter_width;

        int lx = st.firstx;

        for (int vrow = 0; vrow < st.rows; vrow++) {
            Line l = buf.lineAt(lx);
            if (l == null) {
                break;	// don't make a big fuss the loop below will
            }
            int yoff = metrics.height * vrow;

            Color background = rendition_to_color(l.getBackgroundColor());
            if (background != null) {
                int rect_height = metrics.height - metrics.leading;
                g.setColor(background);
                g.fillRect(xoff, yoff, screen.getWidth(), rect_height);
            }

            lx++;
        }


        if (!selection_xor) {
            // The screen is clear, draw any selections
            //
            // If most of the lines are w/o attributes then the text just gets
            // draw over this. Lines that are attributed end up doing some
            // redundant work repainting.

            sel.paint(g);
        }


        g.setColor(actual_foreground);

        Extent selx = sel.getExtent();
        check_selection = (selx != null && !selection_xor);
        totcols = buf.totalCols();

        /* DEBUG
        System.out.println("=========================================="); // NOI18N
         */
        lx = st.firstx;

        for (int vrow = 0; vrow < st.rows; vrow++) {
            Line l = buf.lineAt(lx);
            if (l == null) {
                /* DEBUG
                System.out.println("vrow " + vrow + "  lx " + lx); // NOI18N
                 */
                printStats(null);
                break;
            }

            xoff = 0;
            int yoff = metrics.height * vrow;
            int baseline = yoff + metrics.ascent;

            if (debug_gutter_width > 0) {
                String aBuf = "" + (firsta + st.firstx + vrow);	// NOI18N
                g.drawString(aBuf, xoff, baseline);
            }
            xoff += debug_gutter_width;

            // draw any glyphs that we might have
            if (glyph_gutter_width > 0) {
                Image image = glyph_images[l.getGlyphId()];
                if (image != null) {
                    // xy passed to drawImage() is the top-left of the image
                    int gyoff = yoff;
                    g.drawImage(image, xoff, gyoff, TRANSPARENT, null);
                }
            }
            xoff += glyph_gutter_width;

            paint_line_new(g, l, vrow + st.firstx, xoff, yoff, baseline, selx);

            // restore 'g' to something reasonable
            g.setColor(actual_foreground);

            lx++;
        }

        paint_cursor(g);

        if (selection_xor) {
            sel.paint(g);
        }

        if (debugMargins()) {
            paint_margins(g);
        }

    /* DEBUG
    long paint_stop_time = System.currentTimeMillis();
    long paint_time = paint_stop_time - paint_start_time;
    System.out.println("paint_time = " + paint_time);	// NOI18N
     */
    }

    private void paint_margins(Graphics g) {
    }

    private void paint_cursor(Graphics g) {
        if (!cursor_visible) {
            return;
        }

        // figure what row the cursor is on
        if (st.cursor.row == -1) {
            // System.out.println("Term.paint_cursor: " + // NOI18N
            //		       "cursor doesn't point to any line");	// NOI18N
            return;
        }

        int cursor_row = st.cursor.row - st.firstx;
        if (cursor_row >= st.rows) {
            return;		// cursor not visible
        }

        int cursor_col = st.cursor.col - st.firsty;
        if (cursor_col >= buf.visibleCols()) {
            return;		// cursor not visible
        } else if (cursor_col < 0) {
            return;		// cursor not visible
        }

        g.setXORMode(actual_background);
        int rect_x = cursor_col * metrics.width +
                glyph_gutter_width +
                debug_gutter_width;
        int rect_y = cursor_row * metrics.height;
        // we _don't_ make cursor as wide as underlying character
        int rect_width = metrics.width;
        int rect_height = metrics.height - metrics.leading;
        if (has_focus) {
            g.fillRect(rect_x, rect_y, rect_width, rect_height);
        } else {
            g.drawRect(rect_x, rect_y, rect_width, rect_height);
        }
    }
    private static final boolean DO_MARGINS = true;

    private boolean possiblyScrollDown() {
        /*
         * If cursor has moved below the scrollable region scroll down.
         * Buffer manipulation is partly done here or at the callsite if
         * 'true' is returned.
         */

        if (!DO_MARGINS) {
            if (st.cursor.row >= st.firstx + st.rows) {
                // scroll down
                st.firstx++;
                return true;
            }
            return false;
        } else {
            // new margin based scrolling
            if (st.cursor.row >= st.firstx + botMargin() + 1) {
                // scroll down
                if (topMargin() == 0) {
                    if (scroll_on_output || cursor_was_visible() && track_cursor) {
                        st.firstx++;
                    }
                    return true;
                } else {
                    st.cursor.row = st.firstx + botMargin();
                    Line l = buf.moveLineFromTo(st.firstx + topMargin(),
                            st.cursor.row);
                    l.reset();
                    return false;
                }
            }
            return false;
        }
    }

    /**
     * Send a character to the terminal screen.
     * @param c The character to send.
     */
    public void putChar(char c) {
        dce_end.putChar(c);
    }

    /**
     * Send several characters to the terminal screen.
     * <br>
     * While 'buf' will have a size it may not be fully filled, hence
     * the explicit 'nchar'.
     * @param buf
     * @param offset
     * @param count
     */
    public void putChars(char buf[], int offset, int count) {
        dce_end.putChars(buf, offset, count);
    }

    /**
     * Force a repaint.
     * <p>
     * Normally a putChar() or putChars() will call repaint, unless ...
     * setRepaintEnabled() has been called with false. This function
     * allows for some flexibility wrt to buffering and flushing:
     * <pre>
     *		term.setRefreshEnabled(false);
     *		for (cx = 0; cx < buf.length; cx++) {
     *			term.putChar(c);
     *			if (c % 10 == 0)
     *				term.flush();
     *		}
     *		term.setRefreshEnabled(true);
     * </pre>
     */
    public void flush() {
        dce_end.flush();
    }

    /**
     * Send a message back to DCE.
     * <p>
     * Perhaps SHOULD lock out sendChar() so user input doesn't interfere.
     */
    private void reply(String str) {
        /* DEBUG
        System.out.println("replying " + str);	// NOI18N
         */
        for (int sx = 0; sx < str.length(); sx++) {
            sendChar(str.charAt(sx));
        }
    }

    /*
     * Term used to implement Ops but then that forces Ops to be public
     * which we don't want. So we do it this way.
     */
    private class OpsImpl implements Ops {

	@Override
        @SuppressWarnings("CallToThreadYield")
        public void op_pause() {

            // This yields slighlty more reasonable results.
            Thread.yield();

        /*

        DtTerm sends ~240 NUL's between reverse video switching to
        simulate a flash. The resolution of Thread.sleep() isn't
        really one millisecond so the flash ends up being too long.

        try {
        Thread.currentThread().sleep(0, 500);
        } catch (InterruptedException x) {
        ;
        }
         */
        }

	@Override
        public void op_char(char inChar) {
            char c = mapChar(inChar);

            if (debugOps()) {
                // OLD System.out.println("op_char('" + c + "') = " + (int) c); // NOI18N
                System.out.printf("op_char('%c' %#x) maps to '%c' %#x\n",// NOI18N
                                  inChar, (int) inChar, c, (int) c);
            }
            // generic character printing
            Line l = cursor_line();

            int insertion_col = l.cellToBuf(metrics, st.cursor.col);
            if (debugOps()) {
                System.out.println("op_char(): st.cursor.col " + st.cursor.col + // NOI18N
                        " insertion_col " + insertion_col);	// NOI18N
            }
            if (!st.overstrike) {
                // This just shifts stuff the actual character gets put in below.
                l.insertCharAt(Term.this, ' ', insertion_col, st.attr);
            }

            int cwidth = metrics.wcwidth(c);
            if (l.isAboutToWrap() ||
                    (cwidth > 1 &&
                    st.cursor.col + cwidth > buf.visibleCols() &&
                    !horizontally_scrollable)) {

                // 'wrap' the line
                if (debugOps()) {
                    System.out.println("\twrapping it"); // NOI18N
                }
                l.setWrapped(true);
                l.setAboutToWrap(false);
                op_line_feed();
                op_carriage_return();
                l = cursor_line();
                insertion_col = 0;
            // Fall thru
            }

            l.setCharAt(Term.this, c, insertion_col,
                        st.attr, Attr.BGCOLOR.get(st.attr));	// overstrike
            st.cursor.col += cwidth;

            if (st.cursor.col >= buf.visibleCols() && !horizontally_scrollable) {
                if (debugOps()) {
                    System.out.println("\tabout to wrap"); // NOI18N
                }
                l.setAboutToWrap(true);
                st.cursor.col -= cwidth;
            }
        }

        /**
         * map G1 into GL (~ switch to graphic font)
         */
	@Override
        public void op_as() {
            op_selectGL(1);
        }

        /**
         * map G0 into GL (~ switch to normal font)
         */
	@Override
        public void op_ae() {
            op_selectGL(0);
        }

	@Override
        public void op_attr(int attr) {
            if (debugOps()) {
                System.out.println("op_attr(" + attr + ")"); // NOI18N
            }
            setAttribute(attr);
        }

	@Override
        public void op_bel() {
            // ring the bell
            if (debugOps()) {
                System.out.println("op_bel()");     // NOI18N
            }            // SHOULD implement
        }

	@Override
        public void op_back_space() {
            // back-space
            if (debugOps()) {
                System.out.println("op_back_space"); // NOI18N
            }
            if (st.cursor.col > 0) {
                if (!cursor_line().isAboutToWrap()) {
                    st.cursor.col--;
                }
                cursor_line().setAboutToWrap(false);

                // If we' backed up to column 0, maybe we need to consider
                // whether the previous line was wrapped. Older xterms aren't
                // this clever, newer ones (Solaris 8+?) are.

                if (st.cursor.col == 0) {
                    if (st.cursor.row > 0) {
                        // maybe we had wrapped on the previous line?
                        if (debugOps()) {
                            System.out.println("\tchecking if prev is wrapped"); // NOI18N
                        }
                        Line prev = buf.lineAt(st.cursor.row - 1);
                        if (prev.isWrapped()) {
                            if (debugOps()) {
                                System.out.println("\tit is"); // NOI18N
                            }
                            st.cursor.row--;

                            // The below is done in a roundabout way because BS doesn't
                            // really reduce length. So, suppose we went to the end with
                            // latin chars that makes the line 80 long. Then we backspace
                            // to column 78 and enter one 2-cell japanese character. Now
                            // the line is conceptually 79 long, but it still remembers
                            // the 80. So we don't use 'prev.length()' directly.

                            // st.cursor.col = prev.bufToCell(metrics, prev.length()-1);

                            int last_col = prev.cellToBuf(metrics, buf.visibleCols() - 1);
                            st.cursor.col = prev.bufToCell(metrics, last_col);

                            prev.setWrapped(false);

                            // The following isn't entirely correct when we backspaced
                            // over a multi-celled character. SHOULD either note
                            // what we BS'ed over or note the slop at the end of the line.
                            prev.setAboutToWrap(true);
                        }
                    }
                }
            }
        }

	@Override
        public void op_line_feed() {
            // \LF line feed ctrl-J
            // move cursor down one line and if goes past the screen
            // add a new line.
            if (debugOps()) {
                System.out.println("op_line_feed"); // NOI18N
            }
            op_ind(1);

            n_linefeeds++;
        }

	@Override
        public void op_tab() {
            // TAB/HT

            if (debugOps()) {
                System.out.println("op_tab"); // NOI18N
            }
            cht();
        }
        
        private boolean cht() {
            // SHOULD do something better with tabs near the end of the line
            // On the other hand, that's how ANSI terminals are supposed
            // to behave
            if (st.cursor.col == buf.visibleCols() - 1 && !horizontally_scrollable) {
                return false;
            }

            Line l = cursor_line();
            int insert_col = l.cellToBuf(metrics, st.cursor.col);
            st.cursor.col++;
            insert_col++;
            while ((st.cursor.col < buf.visibleCols() - 1 || horizontally_scrollable) &&
                    (st.cursor.col % tab_size) != 0) {
                st.cursor.col++;
                insert_col++;
            }
            return true;
        }

        private boolean cbt() {
            if (st.cursor.col <= 0)
                return false;

            Line l = cursor_line();
            int insert_col = l.cellToBuf(metrics, st.cursor.col);
            st.cursor.col--;
            insert_col--;
            while ((st.cursor.col > 0) &&
                    st.cursor.col % tab_size != 0) {
                st.cursor.col--;
                insert_col--;
            }

            return true;
        }

        @Override
	@SuppressWarnings({"ValueOfIncrementOrDecrementUsed", "AssignmentToMethodParameter"})
        public void op_cbt(int n) {
            if (debugOps()) {
                System.out.printf("op_cbt(%d)\n", n); // NOI18N
            }
            while (n-- > 0) {
                if (!cbt())
                    break;
            }
        }

        @Override
	@SuppressWarnings({"ValueOfIncrementOrDecrementUsed", "ValueOfIncrementOrDecrementUsed"})
        public void op_cht(int n) {
            if (debugOps()) {
                System.out.printf("op_cht(%d)\n", n); // NOI18N
            }
            while (n-- > 0) {
                if (!cht())
                    break;
            }
        }

	@Override
        public void op_carriage_return() {
            if (debugOps()) {
                System.out.println("op_carriage_return"); // NOI18N
            }
            st.cursor.col = 0;
            cursor_line().setAboutToWrap(false);
        }

	@Override
	@SuppressWarnings({"ValueOfIncrementOrDecrementUsed", "AssignmentToMethodParameter"})
        public void op_al(int count) {
            // add new blank line
            if (debugOps()) {
                System.out.println("op_al(" + count + ")"); // NOI18N
            }
            Line l;
            while (count-- > 0) {
                boolean old_atw = cursor_line().setAboutToWrap(false);

                // reverse of op_dl()
                // Rotate a line from bottom to top
                if (!DO_MARGINS) {
                    l = buf.moveLineFromTo(buf.nlines() /*OLD-1*/ , st.cursor.row);
                } else {
                    l = buf.moveLineFromTo(st.firstx + botMargin(), st.cursor.row);
                }
                l.reset();

                cursor_line().setAboutToWrap(old_atw);
            }

            switch (sel.intersection(st.cursor.row - 1)) {
                case Sel.INT_NONE:
                case Sel.INT_ABOVE:
                case Sel.INT_ON:
                    // nothing to do
                    break;
                case Sel.INT_STRADDLES:
                    sel.cancel(true);	// DtTerm behaviour
                    break;
                case Sel.INT_BELOW:
                    sel.adjust(firsta, +1, firsta + buf.nlines());
                    break;
            }
        }

	@Override
	@SuppressWarnings({"ValueOfIncrementOrDecrementUsed", "AssignmentToMethodParameter"})
        public void op_bc(int count) {
            // back cursor/column
            if (debugOps()) {
                System.out.println("op_bc(" + count + ")"); // NOI18N
            }
            while (count-- > 0) {
                if (st.cursor.col <= 0) {
                    return;
                }
                st.cursor.col--;
            }
            cursor_line().setAboutToWrap(false);
        }

	@Override
	@SuppressWarnings("AssignmentToMethodParameter")
        public void op_cm(int row, int col) {
            // cursor motion row and col come in as 1-origin)
            if (debugOps()) {
                System.out.println("op_cm(row " + row + ", col " + col + ")"); // NOI18N
            }
            // "xemacs -nw" seems to overflow and underflow often.

            // 0 is allowed
            if (row == 0) {
                row = 1;
            }
            if (col == 0) {
                col = 1;
            }

            // deal with overflow
            if (row > st.rows) {
                row = st.rows;
            }
            if (col > buf.visibleCols()) {
                col = buf.visibleCols();
            }

            cursor_line().setAboutToWrap(false);
            st.cursor.row = beginx() + row - 1;
            st.cursor.col = col - 1;
        // Maybe SHOULD setAboutToWrap(true) if on last column?
        }

	@Override
        public void op_ce() {
            // clear to end of line
            if (debugOps()) {
                System.out.println("op_ce ="); // NOI18N
            }
            op_el(0);
        }

        @Override
        public void op_el(int code) {
            // Erase in Line
            if (debugOps()) {
                System.out.printf("op_el(%d)\n", code); // NOI18N
            }
            Line l = cursor_line();
            switch (code) {
                case 0:         // from cursor to end
                    l.clearToEndFrom(Term.this,
                                     l.cellToBuf(metrics, st.cursor.col),
                                     buf.visibleCols()-1,
                                     Attr.BGCOLOR.get(st.attr));
                    break;
                case 1:         // from beginning to cursor (inclusive)
                    l.clearTo(Term.this,
                              l.cellToBuf(metrics, st.cursor.col),
                              Attr.BGCOLOR.get(st.attr));
                    break;
                case 2:         // whole line
                    l.clearToEndFrom(Term.this,
                                     0,
                                     buf.visibleCols()-1,
                                     Attr.BGCOLOR.get(st.attr));
                    break;
            }
            switch (sel.intersection(st.cursor.row)) {
                case Sel.INT_NONE:
                case Sel.INT_ABOVE:
                case Sel.INT_BELOW:
                    // nothing to do
                    break;
                case Sel.INT_ON:
                case Sel.INT_STRADDLES:
                    sel.cancel(true);	// DtTerm behaviour
                    break;
            }
        }

	@Override
        public void op_cd() {
            // clear to end of screen
            if (debugOps()) {
                System.out.println("op_cd -- clear to end of screen"); // NOI18N
            }
            for (int lx = st.cursor.row; lx < beginx() + st.rows; lx++) {
                Line l = buf.lineAt(lx);
                l.reset();
            }

            switch (sel.intersection(st.cursor.row)) {
                case Sel.INT_NONE:
                case Sel.INT_ABOVE:
                    // nothing to do
                    break;
                case Sel.INT_BELOW:
                case Sel.INT_ON:
                case Sel.INT_STRADDLES:
                    sel.cancel(true);	// DtTerm behaviour
                    break;
            }
        }

	@Override
        public void op_cl() {
            // clear screen and home cursor
            if (debugOps()) {
                System.out.println("op_cl"); // NOI18N
            }
            cursor_line().setAboutToWrap(false);
            clear();
            st.cursor.row = beginx();
            st.cursor.col = 0;
        }

        @Override
        public void op_ed(int code) {
            // Erase in Line
            if (debugOps()) {
                System.out.printf("op_ed(%d)\n", code); // NOI18N
            }
            Line l;
            switch (code) {
                case 0:         // from cursor to end
                    l = cursor_line();
                    // l.setAboutToWrap(false);
                    l.clearToEndFrom(Term.this,
                                     l.cellToBuf(metrics, st.cursor.col),
                                     buf.visibleCols()-1,
                                     Attr.BGCOLOR.get(st.attr));
                    for (int lx = st.cursor.row+1; lx < beginx() + st.rows; lx++) {
                        l = buf.lineAt(lx);
                        // l.setAboutToWrap(false);
                        l.reset(Term.this, buf.visibleCols()-1, Attr.BGCOLOR.get(st.attr));
                    }
                    break;
                case 1:         // from beginning to cursor (inclusive)
                    for (int lx = beginx(); lx < st.cursor.row; lx++) {
                        l = buf.lineAt(lx);
                        // l.setAboutToWrap(false);
                        l.reset(Term.this, buf.visibleCols()-1, Attr.BGCOLOR.get(st.attr));
                    }
                    l = cursor_line();
                    // l.setAboutToWrap(false);
                    l.clearTo(Term.this,
                              l.cellToBuf(metrics, st.cursor.col),
                              Attr.BGCOLOR.get(st.attr));
                    break;
                case 2:         // whole screen
                    for (int lx = beginx(); lx < beginx() + st.rows; lx++) {
                        l = buf.lineAt(lx);
                        // l.setAboutToWrap(false);
                        l.reset(Term.this, buf.visibleCols()-1, Attr.BGCOLOR.get(st.attr));
                    }
                    break;
            }
            switch (sel.intersection(st.cursor.row)) {
                case Sel.INT_NONE:
                case Sel.INT_ABOVE:
                case Sel.INT_BELOW:
                    // nothing to do
                    break;
                case Sel.INT_ON:
                case Sel.INT_STRADDLES:
                    sel.cancel(true);	// DtTerm behaviour
                    break;
            }
        }

	@Override
	@SuppressWarnings({"AssignmentToMethodParameter", "ValueOfIncrementOrDecrementUsed"})
        public void op_dc(int count) {
            // delete character
            if (debugOps()) {
                System.out.println("op_dc(" + count + ")"); // NOI18N
            }
            if (count == 0) {
                count = 1;
            }
            Line l = cursor_line();
            while (count-- > 0) {
                l.deleteCharAt(l.cellToBuf(metrics, st.cursor.col));
            }
        }

	@Override
	@SuppressWarnings({"ValueOfIncrementOrDecrementUsed", "AssignmentToMethodParameter"})
        public void op_dl(int count) {
            // delete line
            // and scroll everything under it up
            if (debugOps()) {
                System.out.println("op_dl(" + count + ")"); // NOI18N
            }
            Line l;
            while (count-- > 0) {
                boolean old_atw = cursor_line().setAboutToWrap(false);

                // reverse of op_al()
                // Rotate a line from top to bottom
                if (!DO_MARGINS) {
                    l = buf.moveLineFromTo(st.cursor.row,
                            (beginx() + st.rows - 1)/*OLD-1*/);
                } else {
                    l = buf.moveLineFromTo(st.cursor.row,
                            (beginx() + botMargin())/*OLD-1*/);
                }
                l.reset();

                cursor_line().setAboutToWrap(old_atw);
            }

            switch (sel.intersection(st.cursor.row)) {
                case Sel.INT_NONE:
                case Sel.INT_ABOVE:
                    // nothing to do
                    break;
                case Sel.INT_ON:
                case Sel.INT_STRADDLES:
                    sel.cancel(true);	// DtTerm behaviour
                    break;
                case Sel.INT_BELOW:
                    sel.adjust(firsta, -1, firsta + buf.nlines());
                    break;
            }
        }

	@Override
	@SuppressWarnings({"AssignmentToMethodParameter", "ValueOfIncrementOrDecrementUsed"})
        public void op_do(int count) {
            // down count lines
            // SHOULD add a mode: {scroll, warp, stay} for cases where
            // cursor is on the bottom line.

            if (debugOps()) {
                System.out.println("op_do(" + count + ") -- down"); // NOI18N
            }
            boolean old_atw = cursor_line().setAboutToWrap(false);

            while (count-- > 0) {
                st.cursor.row++;
                if (st.cursor.row >= buf.nlines()) {

                    // equivalent of op_newline:
                    if (possiblyScrollDown()) {
                        buf.addLineAt(st.cursor.row);
                        limit_lines();
                        if (debugOps()) {
                            System.out.println("op_do ADJUSTED"); // NOI18N
                        }
                    }
                }
            }
            cursor_line().setAboutToWrap(old_atw);
        }

	@Override
        public void op_ho() {
            // cursor home (upper left of the screen)
            if (debugOps()) {
                System.out.println("op_ho -- home"); // NOI18N
            }
            cursor_line().setAboutToWrap(false);
            st.cursor.row = beginx();
            st.cursor.col = 0;
        }

	@Override
	@SuppressWarnings({"AssignmentToMethodParameter", "ValueOfIncrementOrDecrementUsed"})
        public void op_ic(int count) {
            // insert character
            if (debugOps()) {
                System.out.println("op_ic(" + count + ")"); // NOI18N
            }
            Line l = cursor_line();
            int insertion_col = l.cellToBuf(metrics, st.cursor.col);
            while (count-- > 0) {
                l.insertCharAt(Term.this, ' ', insertion_col, st.attr);
            }
        // SHOULD worry about line wrapping
        }

	@Override
	@SuppressWarnings({"ValueOfIncrementOrDecrementUsed", "AssignmentToMethodParameter"})
        public void op_nd(int count) {
            // cursor right (non-destructive space)
            if (debugOps()) {
                System.out.println("op_nd(" + count + ")"); // NOI18N
            }
            int vc = st.cursor.col;
            while (count-- > 0) {
                vc++;
                if (vc >= buf.visibleCols()) {
                    if (debugOps()) {
                        System.out.println("\tbailing out at count " + count); // NOI18N
                    }
                    vc--;
                    break;
                }
            }
            st.cursor.col = vc;
        }

	@Override
        public void op_up(int count) {
            // cursor up - scroll
            if (debugOps()) {
                System.out.println("op_up(" + count + ")"); // NOI18N
            }
            op_ri(count);
        }

        @Override
	@SuppressWarnings({"ValueOfIncrementOrDecrementUsed", "AssignmentToMethodParameter"})
        public void op_ri(int count) {
            // cursor up - scroll
            // Opposite of op_ind()
            if (debugOps()) {
                System.out.printf("op_ri(%d)\n", count);//NOI18N
            }
            boolean old_atw = cursor_line().setAboutToWrap(false);
            Line l;
            while (count-- > 0) {
                if (st.cursor.row == st.firstx + topMargin()) {
                    // scroll down, Rotate a line from bottom to top
                    l = buf.moveLineFromTo(st.firstx + botMargin(), st.cursor.row);
                    l.reset();
                    // SHOULD note and do something about the selection?
                } else {
                    st.cursor.row--;
                    if (st.cursor.row < st.firstx)
                        st.cursor.row = st.firstx;
                }
            }
            cursor_line().setAboutToWrap(old_atw);
        }

        @Override
        public void op_cuu(int count) {
            // cursor up - no scroll
            // Opposite of op_cud()
            if (debugOps()) {
                System.out.printf("op_cu(%d)\n", count);//NOI18N
            }
            boolean old_atw = cursor_line().setAboutToWrap(false);


            if (top_margin == 0) {
                st.cursor.row -= count;
                if (st.cursor.row < st.firstx)
                    st.cursor.row = st.firstx;
            } else {
                // Only check against margin if we were below it to begin with.
                // This is true xterm behaviour. gnome term for example
                // will always honor the margin
                boolean was_above_margin = (st.cursor.row < st.firstx + topMargin());
                st.cursor.row -= count;
                if (!was_above_margin) {
                    if (st.cursor.row < st.firstx + topMargin())
                        st.cursor.row = st.firstx + topMargin();
                } else {
                    if (st.cursor.row < st.firstx)
                        st.cursor.row = st.firstx;
                }
            }
            cursor_line().setAboutToWrap(old_atw);
        }

        @Override
        public void op_cud(int count) {
            // cursor down - no scroll
            // Opposite of op_cuu()
            if (debugOps()) {
                System.out.printf("op_cud(%d)\n", count); // NOI18N
            }
            boolean old_atw = cursor_line().setAboutToWrap(false);

            if (bot_margin == 0) {
                st.cursor.row += count;
                if (st.cursor.row > st.firstx + st.rows - 1)
                    st.cursor.row = st.firstx + st.rows - 1;
            } else {
                // Only check against margin if we were above it to begin with
                // This is true xterm behaviour. gnome term for example
                // will always honor the margin
                boolean was_below_margin = (st.cursor.row > st.firstx + botMargin());
                st.cursor.row += count;
                if (!was_below_margin) {
                    if (st.cursor.row > st.firstx + botMargin())
                        st.cursor.row = st.firstx + botMargin();
                } else {
                    if (st.cursor.row > st.firstx + st.rows - 1)
                        st.cursor.row = st.firstx + st.rows - 1;
                }
            }

            cursor_line().setAboutToWrap(old_atw);
        }

        @Override
	@SuppressWarnings({"ValueOfIncrementOrDecrementUsed", "AssignmentToMethodParameter"})
        public void op_ind(int count) {
            // cursor down - scroll
            // Opposite of op_ri()
            // \ESCD
            if (debugOps()) {
                System.out.printf("op_ind(%d)\n", count); // NOI18N
            }
            boolean old_atw = cursor_line().setAboutToWrap(false);
            boolean noMargins = topMargin() == 0 && botMargin() == st.rows-1;

            if (noMargins) {
                while (count-- > 0) {
                    st.cursor.row++;
                    if (st.cursor.row >= buf.nlines()) {
                        if (scroll_on_output || cursor_was_visible() && track_cursor) {
                            st.firstx++;
                        }
                        buf.addLineAt(st.cursor.row);
                        limit_lines();
                    }
                }
            } else {
                while (count-- > 0) {
                    if (st.cursor.row == st.firstx + botMargin()) {
                        // scroll up, Rotate a line from top to bottom
                        Line l;
                        l = buf.moveLineFromTo(st.firstx + topMargin(), st.cursor.row);
                        l.reset();
                    } else {
                        st.cursor.row++;
                        if (st.cursor.row > st.firstx + st.rows - 1)
                            st.cursor.row = st.firstx + st.rows - 1;
                    }

                }
            }

            cursor_line().setAboutToWrap(old_atw);
        }

	@Override
        public void op_sc() {
            // save cursor position
            if (debugOps()) {
                System.out.println("op_sc()"); // NOI18N
            }
            st.saveCursor();
        // SHOULD defeat repaint?
        }

	@Override
        public void op_rc() {
            // restore saved cursor position
            if (debugOps()) {
                System.out.println("op_rc()"); // NOI18N
            }
            st.restoreCursor();
        }

	@Override
        public void op_glyph(int glyph, int rendition) {
            if (debugOps()) {
                System.out.println("op_glyph(glyph " + glyph + // NOI18N
                        ", rendition " + rendition + ")");	// NOI18N
            }
            setGlyph(glyph, rendition);
        }

	@Override
        public void op_reverse(boolean reverse_video) {
            setReverseVideo(reverse_video);
        }

	@Override
        public void op_cursor_visible(boolean visible) {
            setCursorVisible(visible);
        }

	@Override
        public void op_icon_name(String iconName) {
            if (debugOps()) {
                System.out.println("op_icon_name(" + iconName + ")"); // NOI18N
            }
        }

	@Override
        public void op_win_title(String winTitle) {
            fireTitleChanged(winTitle);
            if (debugOps()) {
                System.out.println("op_win_title(" + winTitle + ")"); // NOI18N
            }
        }

	@Override
        public void op_cwd(String currentWorkingDirectory) {
            fireCwdChanged(currentWorkingDirectory);
            if (debugOps()) {
                System.out.println("op_cwd(" + currentWorkingDirectory + ")"); // NOI18N
            }
        }
        
        @Override
        public void op_ext(String command) {
            fireExternalCommand(command);
            if (debugOps()) {
                System.out.println("op_ext(" + command + ")"); // NOI18N
            }
        }


        @Override
        public void op_setG(int gx, int fx) {
            if (debugOps()) {
                System.out.printf("op_setG(%d, %d)\n", gx, fx); // NOI18N
            }
            Term.this.st.setG(gx, fx);
        }

        @Override
        public void op_selectGL(int gx) {
            if (debugOps()) {
                System.out.printf("op_selectGL(%d)\n", gx); // NOI18N
            }
            Term.this.st.selectGL(gx);
        }


	@Override
        public void op_margin(int from, int to) {

            if (debugOps()) {
                System.out.println("op_margin(" + from + ", " + // NOI18N

                        to + ")"); // NOI18N
            }

            if (from < 0) {
                top_margin = 0;
            } else if (from > st.rows) {
                top_margin = st.rows;
            } else {
                top_margin = from;
            }

            if (to < 0) {
                bot_margin = 0;
            } else if (to > st.rows) {
                bot_margin = st.rows;
            } else {
                bot_margin = to;
            }

            if (top_margin > bot_margin) {
                int tmp = top_margin;
                top_margin = bot_margin;
                bot_margin = tmp;
            }
        }
        long last_time = System.currentTimeMillis();

	@Override
        public void op_time(boolean repaint) {
            long time = System.currentTimeMillis();
            long elapsed = time - last_time;
            Date d = new Date(time);
            String date_str = d.toString();
            String elapsed_str = "" + elapsed / 1000 + "." + elapsed % 1000;// NOI18N
            String output1 = date_str + " Elapsed (sec): " + elapsed_str;// NOI18N
            String output2 = "putChar " + n_putchar + // NOI18N
                    "  putChars " + n_putchars + // NOI18N
                    "  linefeeds " + n_linefeeds + // NOI18N
                    "  repaint " + n_repaint + // NOI18N
                    "  paint " + n_paint;		// NOI18N

            setAttribute(41);	// Red Bg

            // can't use appendText from within ops.

            for (int sx = 0; sx < output1.length(); sx++) {
                op_char(output1.charAt(sx));
            }
            op_line_feed();
            op_carriage_return();
            for (int sx = 0; sx < output2.length(); sx++) {
                op_char(output2.charAt(sx));
            }

            setAttribute(0);

            last_time = time;
            n_putchar = 0;
            n_putchars = 0;
            n_linefeeds = 0;
            n_paint = 0;
            n_repaint = 0;

            repaint(true);
        // TMP setRefreshEnabled(repaint);
        }

	@Override
        public void op_hyperlink(String url, String text) {
            hyperlink(url, text);
        }

	@Override
        public int op_get_width() {
            return horizontally_scrollable ? buf.totalCols() : buf.visibleCols();
        }

	@Override
        public int op_get_column() {
            return st.cursor.col;
        }

	@Override
        public void op_soft_reset() {
            st.overstrike = true;

            st.selectGL(0);

            st.setG(0, 0);
            st.setG(1, 0);
            st.setG(2, 0);
            st.setG(3, 0);

            resetMargins();
	    setAttribute(0);

            interp.softReset();

            repaint(false);
        }

	@Override
        public void op_full_reset() {
            op_soft_reset();
            op_cl();	// clear screen, home cursor
            clearHistoryNoRefresh();
	    setReverseVideo(false);
            repaint(false);
        }

	@Override
        public void op_set_mode(int mode) {
            switch (mode) {
                case 4:		// insert mode
                    st.overstrike = false;
                    break;
                case 2:		// keyboard lock
                case 12:	// local echo off
                case 20:	// newline
                    // Currently unsupported
                    break;
            }
        }

	@Override
        public void op_reset_mode(int mode) {
            switch (mode) {
                case 4:		// replace mode
                    st.overstrike = true;
                    break;
                case 2:		// keyboard unlock
                case 12:	// local echo on
                case 20:	// newline
                    // Currently unsupported
                    break;
            }
        }

	@Override
        public void op_status_report(int code) {
            switch (code) {
                case 5:
                    reply((char) 27 + "[0n");	// NOI18N
                    break;
                case 6:
                    reply((char) 27 + "[" + // NOI18N
                            (st.cursor.row - st.firstx) + ";" + // NOI18N
                            st.cursor.col + "R");			// NOI18N
                    break;
            }
        }

        @Override
        public void logUnrecognizedSequence(String sequence) {
            if (debugOps()) {
                System.out.printf("Unrecognized sequence '%s'\n", sequence); // NOI18N
            }
            Term.this.logUnrecognizedSequence(sequence);
        }

        @Override
        public void logCompletedSequence(String sequence) {
            Term.this.logCompletedSequence(sequence);
        }

        @Override
        public void op_send_chars(String sequence) {
            Term.this.sendChars(sequence.toCharArray(), 0, sequence.length());
        }

        @Override
	@SuppressWarnings("AssignmentToMethodParameter")
        public void op_cha(int col) {
            // cursor to column
            if (debugOps()) {
                System.out.printf("op_cha(col %d)\n", col); // NOI18N
            }

            // 0 is allowed
            if (col == 0) {
                col = 1;
            }

            // deal with overflow
            if (col > buf.visibleCols()) {
                col = buf.visibleCols();
            }

            cursor_line().setAboutToWrap(false);
            st.cursor.col = col - 1;
            // Maybe SHOULD setAboutToWrap(true) if on last column?
        }

        @Override
	@SuppressWarnings("AssignmentToMethodParameter")
        public void op_vpa(int row) {
            // cursor to row
            if (debugOps()) {
                System.out.printf("op_vpa(row %d)\n", row); // NOI18N
            }

            // 0 is allowed
            if (row == 0) {
                row = 1;
            }

            // deal with overflow
            if (row > st.rows) {
                row = st.rows;
            }

            cursor_line().setAboutToWrap(false);
            st.cursor.row = beginx() + row - 1;
            // Maybe SHOULD setAboutToWrap(true) if on last column?
        }

        @Override
	@SuppressWarnings("AssignmentToMethodParameter")
        public void op_ech(int n) {
            // erase characters
            if (debugOps()) {
                System.out.printf("op_ech(%d)\n", n); // NOI18N
            }

            if (n == 0)
                n = 1;

            Line l = cursor_line();
            int from = l.cellToBuf(metrics, st.cursor.col);
            int to = l.cellToBuf(metrics, st.cursor.col+n-1);
            if (debugOps())
                System.out.printf("op_ech() from %d  to %d\n", from, to); // NOI18N
            l.clearFromTo(Term.this, from, to, Attr.BGCOLOR.get(st.attr));

            switch (sel.intersection(st.cursor.row)) {
                case Sel.INT_NONE:
                case Sel.INT_ABOVE:
                case Sel.INT_BELOW:
                    // nothing to do
                    break;
                case Sel.INT_ON:
                case Sel.INT_STRADDLES:
                    sel.cancel(true);	// DtTerm behaviour
                    break;
            }
        }

    }

    /**
     * Create a hyperlink.
     * @param url
     * @param text
     */
    protected void hyperlink(String url, String text) {
        // default implementation just dumps out the text
        for (char c : text.toCharArray()) {
            ops.op_char(c);
        }
    }

    /**
     * Map a character according to the font attribute
     * @param inChar
     * @return the unicode character that renders the correct glyph.
     */
    private char mapChar(char inChar) {
        switch (st.font()) {
            case 0:
            default:
                return inChar;
            case 1: {
                // Convert the canonical ncurses ACS code to the appropriate unicode character.
                // See:
                // http://vt100.net/docs/vt220-rm/table2-4.html
                // http://en.wikipedia.org/wiki/Box-drawing_character
                // http://en.wikipedia.org/wiki/Arrow_%28symbol%29
                final char outChar = interp.mapACS(inChar);
                if (outChar == '\0')
                    return inChar;
                switch (outChar) {
                    default:
                        return inChar;

                    // xterm and gnome term don't really honor these:
                    case '+':            // ACS_RARROW
                        return '\u2192'; // RIGHTWARDS ARROW
                    case ',':            // ACS_LARROW
                        return '\u2190'; // LEFTWARDS ARROW
                    case '-':            // ACS_UARROW
                        return '\u2191'; // UPWARDS ARROW
                    case '.':            // ACS_DARROW
                        return '\u2193'; // DOWNWARDS ARROW
                    case '0':            // ACS_BLOCK
                        return '\u2588'; // FULL BLOCK


                    case '`':            // ACS_DIAMOND
                        return '\u2666'; // BLACK DIAMOND SUIT
                    case 'a':            // ACS_CKBOARD
                        return '\u2592'; // MEDIUM SHADE
                        // return '\u2593'; // DARK SHADE
                    case 'b':            // ?
                        return '\u240b'; // SYMBOL FOR VERTICAL TABULATION
                    case 'c':            // ?
                        return '\u240c'; // SYMBOL FOR FORM FEED
                    case 'd':            // ?
                        return '\u240d'; // SYMBOL FOR CARRIAGE RETURN
                    case 'e':            // ?
                        return '\u240a'; // SYMBOL FOR LINE FEED
                    case 'f':            // ACS_DEGREE
                        return '\u00b0'; // DEGREE SIGN
                    case 'g':            // ACS_PLMINUS
                        return '\u00b1'; // PLUS-MINUS SIGN
                    case 'h':            // ACS_PLMINUS
                        return '\u2424'; // SYMBOL FOR NEW LINE
                    case 'i':            // ACS_LANTERN
                        return '\u240b'; // SYMBOL FOR VERTICAL TABULATION
                    case 'j':            // ACS_LRCORNER
                        return '\u2518'; // BOX DRAWINGS LIGHT UP AND LEFT
                    case 'k':            // ACS_LRCORNER
                        return '\u2510'; // BOX DRAWINGS LIGHT DOWN AND LEFT
                    case 'l':            // ACS_LRCORNER
                        return '\u250c'; // BOX DRAWINGS LIGHT DOWN AND RIGHT
                    case 'm':            // ACS_LLCORNER
                        return '\u2514'; // BOX DRAWINGS LIGHT UP AND RIGHT
                    case 'n':            // ACS_PLUS
                        return '\u253c'; // BOX DRAWINGS LIGHT VERTICAL AND HORIZONTAL
                    case 'v':            // ACS_RTEE
                        return '\u2534'; // BOX DRAWINGS LIGHT UP AND HORIZONTAL
                    case 'w':            // ACS_TTEE
                        return '\u252c'; // BOX DRAWINGS LIGHT DOWN AND HORIZONTAL
                    case 'o':            // ACS_S1
                    case 'p':            // ACS_S3
                    case 'q':            // ACS_HLINE
                    case 'r':            // ACS_S7
                    case 's':            // ACS_S9
                        return '\u2500'; // BOX DRAWINGS LIGHT HORIZONTAL
                    case 't':            // ACS_LTEE
                        return '\u251c'; // BOX DRAWINGS LIGHT VERTICAL AND RIGHT
                    case 'u':            // ACS_RTEE
                        return '\u2524'; // BOX DRAWINGS LIGHT VERTICAL AND LEFT
                    case 'x':            // ACS_VLINE
                        return '\u2502'; // BOX DRAWINGS LIGHT VERTICAL
                    case 'y':            // ACS_LEQUAL
                        return '\u2264'; // LESS-THAN OR EQUAL TO
                    case 'z':            // ACS_GEQUAL
                        return '\u2265'; // GREATER-THAN OR EQUAL TO
                    case '{':            // ACS_PI
                        return '\u03c0'; // GREEK SMALL LETTER PI
                    case '|':            // ACS_NEQUAL
                        return '\u2260'; // NOT EQUAL TO
                    case '}':            // ACS_STERLING
                        return '\u00a3'; // POUND SIGN
                    case '~':            // ACS_BULLET
                        return '\u00b7'; // MIDDLE DOT
                }
            }
        }
    }

    private void putc_work(char c) {
        interp.processChar(c);
        possiblyHScroll();
        screen.possiblyUpdateCaretText();
    }

    private void on_char(char c) {
        sendChar(c);
    }
    private static final char ESC = (char) 27;

    private void sendChars(char c[], int offset, int count) {
        dte_end.sendChars(c, offset, count);
    }

    private void sendChar(char c) {
        dte_end.sendChar(c);
    }

    /**
     * Adjust vertical scrollbar range
     */
    private void adjust_scrollbar() {

        // JScrollBar is weird.
        // The visible range is 1 (for value) + extent.
        // So extent has to be set to visible-range - 1:

        adjust_scrollbar_impl();

    /* OLD NPE-x
    // It's important that we do this from within the AWT event thread.

    if (SwingUtilities.isEventDispatchThread()) {
    adjust_scrollbar_impl();
    }
    else {
    SwingUtilities.invokeLater(new Runnable() {
    public void run() {
    adjust_scrollbar_impl();
    }
    });
    }
     */
    }

    private void adjust_scrollbar_impl() {
        if (vscroll_bar != null) {
            int value = st.firstx;
            int extent = st.rows - 1;
            int min = 0;
            int max;
            if (buf.nlines() <= st.rows) {
                max = st.rows - 1;
            } else {
                max = buf.nlines() - 1;
            }
            vscroll_bar.setValues(value, extent, min, max);
        }

        if (hscroll_bar != null && horizontally_scrollable) {
            int value = st.firsty;
            int extent = buf.visibleCols() - 1;
            int min = 0;
            int max;
            if (buf.totalCols() <= buf.visibleCols()) {
                max = buf.visibleCols() - 1;
            } else {
                max = buf.totalCols() - 1;
            }

            /* DEBUG
            System.out.println("HSCROLL " + min + " <= " + value  + 	// NOI18N
            "[" + extent + "] " + max);	// NOI18N
             */

            hscroll_bar.setValues(value, extent, min, max);

        /* DEBUG
        System.out.println("HSCROLL " + hscroll_bar.getMinimum() +	// NOI18N
        " <= " + hscroll_bar.getValue()  + 	// NOI18N
        "[" + hscroll_bar.getModel().getExtent() + "] " + hscroll_bar.getMaximum());	// NOI18N
         */
        }
    }

    /**
     * Figure the pixel size of the screen based on various properties.
     */
    private Dimension calculateSize() {
        int dx = buf.visibleCols() * metrics.width +
                glyph_gutter_width +
                debug_gutter_width;
        int dy = st.rows * metrics.height;
        Dimension d = new Dimension(dx, dy);
        return d;
    }

    /**
     * To be called as the result of programmatic changes in properties
     * that affect the size of the screen: font, rows & columns, glyph
     * gutter width etc.
     * Applies the newly calculated size via sizeChanged().
     *
     * We used to call screen.setSize() which would eventually call
     * sizeChanged() through the notification mechanism. That worked well
     * for row column size changes etc, but not so well for font changes.
     * What would happen is that he cause of size changes would be lost
     * by the time we got to sizeChanged() and for example the screen
     * wouldn't resize as a result of font pt-size changes.
     */
    private void updateScreenSize() {
        /* DEBUG
        System.out.println("updateScreenSize("+buf.cols+", "+st.rows+")"); // NOI18N
         */
        if (screen != null) {
            Dimension d = calculateSize();
            sizeChanged(d.width, d.height);
        }
    }
    // HACK:
    // Helper variable to remember the original value of rows across
    // various different control flows.
    private int old_rows = -1;

    /**
     * Called whenver the screens size is to be changed, either by
     * us via updateScreenSize(), or thru user action and the Screen
     * componentResized() notification.
     *
     * Adjust the state and buffer, commit to the size by setting
     * preferredSize and notify any interested parties.
     */
    void sizeChanged(int newWidth, int newHeight) {
        /* DEBUG
        System.out.println("sizeChanged(newheight " + newHeight + // NOI18N
        ", newWidth " + newWidth + ")");
         */

        // Do columns first ... they're easy
        int newcols = (newWidth - glyph_gutter_width - debug_gutter_width) /
                metrics.width;
        buf.setVisibleCols(newcols);


        if (old_rows == -1) {
            // st.rows hasn't changed yet, so remember it before changing it.
            old_rows = st.rows;
        }

        st.rows = newHeight / metrics.height;
        resetMargins();

        // akemr - hack to fix #17807
        if (st.rows < 1) {
            st.rows = 1;
        }

        /* DEBUG
        System.out.println(">>>>>>> rows from "+old_rows+" to "+st.rows); // NOI18N
         */

        int row_delta = st.rows - old_rows;	// negative => we shrunk
        old_rows = -1;

        adjust_lines(row_delta);

        limit_lines();

        // Commit to the size
        //
        // Setting setPreferredSize() is where the commitment is. If we
        // don't do it our layout manager containers won't honor the resizing
        // and snap us back.

        Dimension new_size = isSizeRounded() ? calculateSize() : new Dimension(newWidth, newHeight);

        /* DEBUG
        System.out.println("but I want "+new_size.height+" "+new_size.width); // NOI18N
         */

        // Setting size is a bad idea. the potential for getting into
        // a looping tug-of-war with our containers' layout manager
        // is too high and unpredictable. One nasty example we ran
        // into was JTabbedPane.
        // screen.setSize(new_size);

        screen.setPreferredSize(new_size);

        // Do we really need these?
        invalidate();
        if (getParent() != null) {
            getParent().validate();
        }


        // Notify any interested parties.
        // Normally we'd inline the code in updateTtySize() here but factoring
        // it has it's uses as explained in updateTtySize().
        updateTtySize();
    }

    protected void possibly_repaint(boolean adjust_scrollbar) {
        if (!refresh_enabled) {
            return;
        }
        repaint(adjust_scrollbar);
    }

    /**
     * Model and or view settings have changed, redraw everything.
     * @param adjust_scrollbar
     */
    protected void repaint(boolean adjust_scrollbar) {

        /*
         * A long discussion on performance and smooth vs jump vs jerky
         * scrolling ... (note: a lot of this is based on experiments with
         * Term as a unix terminal emulator application as opposed to
         * within the context of NetBeans).
         *
         * Term spends it's time between collecting and deciphering input
         * and repainting the screen. Input processing always goes on, but
         * screen repainitng can be done more or less often to trade off
         * smoothness of scrolling vs speed.
         *
         * At one end is so-called smooth scrolling. This is where the
         * screen is redrawn on every linefeed. That's a lot of painting.
         * To get into that mode use the paintImmediately() below and
         * uncomment the call to us in op_line_feed(). Also
         * paintImmediately() doesn't really work unless the Screen is
         * opaque. I think that is because the paint request comes
         * to us and we don't forward it to screen; but it could be a
         * Swing bug too. Term is very slow in this. For example I"ve
         * time xterm and DtTerm dealing with "cat /etc/termcap" in 2-3
         * seconds while Term takes 20-25 seconds. Part of this is
         * attributed to the fact that Term doesn't take advantage of
         * bitBlitting when it's adding one line at a time and still
         * redraws everything. However I'll make a case below that this
         * isn't that important.
         *
         * Then there is so-called jump scrolling. In this regime terminal
         * emulators redraw the screen "as time permits". This is in effect
         * what the swing repaint manager helps with. Multiple repaint()
         * requests translate to one actual paint(). With todays computers
         * it's very hard to tell visually that you're jump scrolling
         * things go by so fast (yes, even under Swing), so this is the
         * preferred setup.
         * Here term does a bit better. To deal with a cat'ed 100,000
         * line file DtTerm takes 8 seconds, while Term takes 22 seconds.
         * (That's 3 times slower vs 8 times). From some measurements
         * I've made the number of linefeeds per actual paints has
         * ranged from > 100 to upper 30's. These numbers are sufficiently
         * high that the whole screen has to be repained everytime.
         * I.e. blitting to scroll and drawing only what's new isn't
         * going to help here. To get reasonable jump-scrolling, you need
         * to make sure that the Screen is opaque because if you don't
         * you will get ...
         *
         * Jerky scrolling. If Term is not opaque, the number of actual
         * paints per repaint() requests diminishes drastically. 'cat' of
         * etc/termcap (once the code has been warmed up) sometimes causes
         * a single refresh at the end in contrast to ~100 when Screen
         * is opaque. Naturally Term in this mode can eat up input at
         * a rate comparable to dtterm etc, but the jerkiness is very
         * ugly.
         * Opacity isn't the only criterion. Term, when embeded inside a
         * tabbed pane (like it is in NetBeans) will also act as if it's
         * opaque and you get more frequent refreshes, as in the
         * jump-scrolling regime. But that was way too slow for the
         * taste of NB users which is why OutputTab window calls us on a
         * timer. That brings it's own jerkiness of a different sort.
         *
         * There is a third factor that contributes to slowness. If you
         * just 'cat' a file you get the numbers I presneted above. But
         * if you run an app that actually puts out the 100,000 lines
         * some sort of timing interaction forces Term into near smooth
         * scrolling and as a result things slow down a lot! For example,
         * 	$ generate_100K_lines > /tmp/bag	00:08 sec
         *	$ cat /tmp/bag				00:20 sec
         *	$ generate_100K_lines			03:42 sec (opaque)
         *	$ generate_100K_lines			01:58 sec (!opaque)
         * This happens even if the generating program is a lightweight
         * native application. In fact I believe it is this effect that
         * forced NB's OutputTab to adopt the timer. I believe there are two
         * factors that contrinute to this.
         * a) Running applications are line buffered so putChars(), with
         *    it's attendant repaint(), gets called once per line pushing
         *    us into the smooth scrolling regime. (But why then doesn't
         *    DtTerm suffer from this?)
         * b) timeslicing gives enough time to the repaint manager such
         *    that it converts evey repaint() to a paint.
         * I know (b) is a factor since if I "simulate" (a) by issueing
         * repaints() from op_line_feed() while keeping this function from
         * using paintImmediately() I don't get that many paints.
         * The combined case has 44 paints per repaint as does simulated (a).
         * So ain increased number of paints per repaint doesn't
         * explain this.
         *
         * In the end, currently since jump scrolling is still not very
         * fast and since NB has the timer anyway, Screen is not opaque.
         *
         * A useful quantitative measure is the number of linefeeds vs
         * the number of repaint requests vs the number of actual paints.
         * All these are collected and can be dumped via op_time() or
         * printStats().
         */

        n_repaint++;

        if (adjust_scrollbar) {
            adjust_scrollbar();
        }


        // The following causes Screen.paint() to get called by the Swing
        // repaint manager which in turn calls back to term.paint(Graphics).
        screen.repaint(20);


    // The following should cause an immediate paint. It doesn't
    // always though!
    // I've found that for it to be effective Screen needs to be opaque.

    /*
    NOTE: paintImmediately() is probably not the best thing to use.
    // RepaintManager.currentManager(screen).paintDirtyRegions();
    screen.paintImmediately(0, 0, screen.getWidth(), screen.getHeight());
     */
    }

    /*
     * Term-specific properties
     */
    /**
     * Control whether the default foreground and background colors will
     * be reversed.
     * <p>
     * Note: This is independent of characters' reverse attribute.
     * @param reverse_video The property value.
     */
    public void setReverseVideo(boolean reverse_video) {
	if (this.reverse_video == reverse_video)
	    return;		// nothing to do

        this.reverse_video = reverse_video;

	// Swap PAL_FG and PAL_BG
	Color tmp = palette[Attr.PAL_FG];
	palette[Attr.PAL_FG] = palette[Attr.PAL_BG];
	palette[Attr.PAL_BG] = tmp;

        repaint(false);
    }

    /**
     * Return the value set by setReverseVideo().
     * @return The property value.
     */
    public boolean isReverseVideo() {
        return reverse_video;
    }
    private boolean reverse_video = false;

    /**
     * Set the color of the hilite (selection) - for non XOR mode
     * @param color The property value.
     */
    public void setHighlightColor(Color color) {
        sel.setColor(color);
        repaint(false);
    }

    /**
     * Get the color of the hilite (selection) - for non XOR mode
     * @return The property value.
     */
    public Color getHighlightColor() {
        return sel.getColor();
    }

    /**
     * Set the color of the hilite (selection) - for XOR mode
     * @param color The property value.
     */
    public void setHighlightXORColor(Color color) {
        sel.setXORColor(color);
        repaint(false);
    }

    /**
     * Get the color of the hilite (selection) - for XOR mode
     * @return The property value.
     */
    public Color getHighlightXORColor() {
        return sel.getXORColor();
    }

    /**
     * Set the feedback color of active regions.
     * @param color The property value.
     */
    public void setActiveColor(Color color) {
        // SHOULD check for null color
        active_color = color;
        repaint(false);
    }

    /**
     * Get the feedback color of active regions.
     * @return The property value.
     */
    public Color getActiveColor() {
        // SHOULD clone? but Color is not clonable and has no simple
        // Color(COlor) constructor. What does JComponent do?
        return active_color;
    }
    private Color active_color = Color.lightGray;

    @Override
    public void setBackground(Color c) {
	super.setBackground(c);
	// See setReverseVideo()
        if (reverse_video) {
	    palette[Attr.PAL_FG] = c;
	} else {
	    palette[Attr.PAL_BG] = c;
	}
    }

    @Override
    public void setForeground(Color c) {
	super.setForeground(c);
	// See setReverseVideo()
        if (reverse_video) {
	    palette[Attr.PAL_BG] = c;
	    palette[Attr.PAL_BOLD] = c;
	} else {
	    palette[Attr.PAL_FG] = c;
	    palette[Attr.PAL_BOLD] = c;
	}
    }


    /**
     * Control whether an anchor is set.
     * <p>
     * Setting an anchor will automatically cause the buffer to grow, in
     * excess of what was set by setHistorySize(), to ensure that whatever
     * is displayed and in the current history will still be accessible.
     * <p>
     * Also, if you're working with Extents, Coords and ActiveRegions, or
     * visiting logical lines, you might want to anchor the text so that
     * your coordinates don't get invalidated by lines going out of the buffer.
     * <p>
     * Repeated enabling of the anchor will discard all text that
     * doesn't fit in history and start a new anchor.
     * <p>
     * When anchoring is disabled any text in excess of setHistorySize()
     * is trimmed and the given history size comes into effect again.
     * @param anchored The property value.
     */
    public void setAnchored(boolean anchored) {
        ckEventDispatchThread();
        // OLD NPE-x synchronized(this)
        {
            if (anchored) {
                this.anchored = false;
                limit_lines();
                this.anchored = true;
            } else {
                this.anchored = false;
                limit_lines();
                repaint(false);	// limit_lines() already adjusted scrollbar
            }
        }
    }

    /**
     * Return true if the text is currently anchored.
     * @return The property value.
     */
    public boolean isAnchored() {
        return anchored;
    }
    private boolean anchored = false;

    /**
     * Returns the actual drawing area so events can be interposed upon,
     * like context menus.
     * @return 
     * @deprecated Replaced by{@link #getScreen()}.
     */
    @Deprecated
    public JComponent getCanvas() {
        return screen;
    }

    /**
     * Returns the actual drawing area so events can be interposed upon,
     * like context menus.
     * @return 
     */
    public JComponent getScreen() {
        return screen;
    }

    /**
     * Return the terminal operations implementation.
     * <b>WARNING! This is temporary</b>
     * @return 
     */
    public Ops ops() {
        return ops;
    }

    /**
     * Set the Interpreter type by name.
     * @param emulation The property value.
     * @see Term#setInterp
     */
    public void setEmulation(String emulation) {
        Interp new_interp = InterpKit.forName(emulation, ops);
        if (new_interp == null) {
            return;
        }
        interp = new_interp;
    }

    /**
     * Returns the termcap string that best describes what this Term
     * emulates.
     * @return The property value.
     */
    public String getEmulation() {
        return getInterp().name();
    }

    /**
     * Set the emulation interpreter.
     * <p>
     * It is not advisable to change the emulation after Term has been
     * connected to a process, since it's often impossible to advise
     * the process of the new terminal type.
     * @param interp The property value.
     */
    public void setInterp(Interp interp) {
        this.interp = interp;
    }

    /**
     * Return the Interpreter assigned to this.
     * @return The property value.
     */
    public Interp getInterp() {
        return interp;
    }

    private transient Interp interp = new InterpDumb(ops);	// used to InterpANSI

    /**
     * Set how many lines of history will be available.
     * <p>
     * If an anchor is in effect the history size will only have an
     * effect when the anchor is reset.
     * @param new_size The property value.
     */
    public void setHistorySize(int new_size) {
        history_size = new_size;
        limit_lines();
        repaint(true);
    }

    /**
     * Return the number of lines in history
     * @return The property value.
     */
    public int getHistorySize() {
        return history_size;
    }
    private int history_size = 20;

    /**
     * Set the width of the glyph gutter in pixels
     * @param pixels The property value.
     */
    public void setGlyphGutterWidth(int pixels) {

        glyph_gutter_width = pixels;

        // protect against client mistakes?
        if (glyph_gutter_width > 30) {
            glyph_gutter_width = 30;
        }

        updateScreenSize();
    }
    private int glyph_gutter_width;

    /**
     * Associate an Image with a glyph id, or clear it if image is null.
     *
     * Numbering the glyphs is confusing. They start with 48. That is,
     * if you register glyph #0 using hbvi/vim :K command the escape
     * sequence emitted is 48. 48 is ascii '0'.
     * @param glyph_number
     * @param image
     */
    public void setGlyphImage(int glyph_number, Image image) {
        if (glyph_number > 256) {
            return;		// SHOULD throw an exception?
        }
        glyph_images[glyph_number] = image;
    }
    private Image glyph_images[] = new Image[256];
    ;

    /**
     * Get the usable area for drawing glyphs.
     * <p>
     * This value changes when the gutter width or the font changes
     * @return The usable area for drawing glyphs.
     */
    public Dimension getGlyphCellSize() {
        return new Dimension(glyph_gutter_width, metrics.height);
    }

    /**
     * Register up to 8 new custom colors.
     *
     * Unlike glyph id's you can start the numbers from 0.
     * hbvi/vim's :K command will add a 58 to the number, but that
     * is the code we interpret as custom color #0.
     * @param number
     * @param c
     * @deprecated
     */
    @Deprecated
    public void setCustomColor(int number, Color c) {
	if (c == null)
	    throw new IllegalArgumentException();
	if (number < 0 || number >= 8)
	    throw new IllegalArgumentException();
	palette[Attr.PAL_BRIGHT+number] = c;
    }

    /**
     * Get cursor row in buffer coordinates (0-origin).
     * @return Cursor row in buffer coordinates (0-origin).
     */
    public int getCursorRow() {
        return st.cursor.row;
    }

    /**
     * Get cursor column in buffer coordinates (0-origin).
     * @return Cursor column in buffer coordinates (0-origin).
     */
    public int getCursorCol() {
        return cursor_line().cellToBuf(metrics, st.cursor.col);
    }

    /**
     * Get (absolute) cursor coordinates.
     * <p>
     * The returned Coord is newly allocated and need not be cloned.
     * @return The property value.
     */
    public Coord getCursorCoord() {
        Line l = buf.lineAt(st.cursor.row);
        return new Coord(new BCoord(st.cursor.row,
                l.cellToBuf(metrics, st.cursor.col)),
                firsta);
    }

    /*
     *
     * Move the cursor to the given (absolute) coordinates
     *
     * @deprecated, replaced by{@link #setCursorCoord(Coord)}
     */
    @Deprecated
    public void goTo(Coord coord) {
        setCursorCoord(coord);
    }

    /**
     * Move the cursor to the given (absolute) coordinates
     * SHOULD be setCursorCoord!
     * @param coord The property value.
     */
    public void setCursorCoord(Coord coord) {
        Coord c = (Coord) coord.clone();
        c.clip(st.rows, buf.visibleCols(), firsta);
        st.cursor = c.toBCoord(firsta);
        st.cursor.col = cursor_line().bufToCell(metrics, st.cursor.col);
        repaint(true);
    }

    /**
     * Control whether the cursor is visible or not.
     * <p>
     * We don't want a visible cursor when we're using Term in
     * non-interactive mode.
     * @param cursor_visible The property value.
     */
    public void setCursorVisible(boolean cursor_visible) {
        this.cursor_visible = cursor_visible;
    }

    /**
     * Find out if cursor is visible.
     * @return The property value.
     */
    public boolean isCursorVisible() {
        return cursor_visible;
    }
    private boolean cursor_visible = true;

    /**
     * Back up the coordinate by one character and return new Coord.
     * <p>
     * Travels back over line boundaries
     * <br>
     * Returns null if 'c' is the first character in the buffer.
     * @param c Coord to back up from.
     * @return New Coord derived from 'c'.
     */
    public Coord backup(Coord c) {
        BCoord bRow = buf.backup(c.toBCoord(firsta));
        if (bRow == null) {
            return null;
        }
        return new Coord(bRow, firsta);
    }

    /**
     * Advance the coordinate by one charater and return new coord.
     * <p>
     * Travels forward over line boundaries.
     * <br>
     * Returns null if 'c' is the last character in the buffer.
     * @param c Coord to advance from.
     * @return New Coord derived from 'c'.
     */
    public Coord advance(Coord c) {
        return new Coord(buf.advance(c.toBCoord(firsta)), firsta);
    }

    /**
     * Get contents of current selection.
     * <p>
     * Returns 'null' if there is no current selection.
     * @return Contents of current selection.
     */
    public String getSelectedText() {
        return sel.getSelection();
    }

    /**
     * Get the extent of the current selection.
     * <p>
     * If there is no selection returns 'null'.
     * @return The property value.
     */
    public Extent getSelectionExtent() {
        return sel.getExtent();
    }

    /**
     * Set the extent of the selection.
     * @param extent The property value.
     */
    public void setSelectionExtent(Extent extent) {
        extent.begin.clip(buf.nlines(), buf.totalCols(), firsta);
        extent.end.clip(buf.nlines(), buf.totalCols(), firsta);
        sel.setExtent(extent);
        repaint(false);
    }

    /**
     * Clear the selection.
     */
    public void clearSelection() {
        sel.cancel(true);
        repaint(false);
    }

    /**
     * Set whether slections automatically get copied to the systemSelection
     * when the selection is completed (the button is released).
     * <p>
     * This is how xterm and other X-windows selections work.
     * <p>
     * This property can probably be deprecated. It was neccessary in the
     * pre-1.4.2 days when we didn't have a systemSelection and we wanted
     * to have the option of not cloberring the systemClipboard on text
     * selection.
     *
     * @param auto_copy The property value.
     * @deprecated selections now always get copied to systemSelection if
     * it exists.
     */
    @Deprecated
    public void setAutoCopy(boolean auto_copy) {
        // no-op
    }

    /**
     * Return the value set by setAutoCopy()
     *
     * @return The property value.
     * @deprecated Now always returns 'true'.
     */
    @Deprecated
    public boolean isAutoCopy() {
        return true;
    }

    /*
     * Control whether refreshes are enabled.
     * <p>
     * Turn refresh off if you're about to add a lot of text to the
     * terminal. Another way is to use appendText("stuff", false)
     */
    public void setRefreshEnabled(boolean refresh_enabled) {
        this.refresh_enabled = refresh_enabled;
        if (refresh_enabled) {
            repaint(true);
        }
    }

    public boolean isRefreshEnabled() {
        return refresh_enabled;
    }
    private boolean refresh_enabled = true;

    /**
     * Sets whether the selection highlighting is XOR style or normal
     * Swing style.
     * @param selection_xor The property value.
     */
    public void setSelectionXOR(boolean selection_xor) {
        this.selection_xor = selection_xor;
        repaint(false);
    }
    /*
     * If returns 'true' then selections are drawn using the xor mode,
     * Otherwise they are drawn in regular swing fashion.
     */

    public boolean isSelectionXOR() {
        return selection_xor;
    }

    private boolean selection_xor = false;

    /**
     * Set the TAB size.
     * <p>
     * The cursor is moved to the next column such that
     * <pre>
     * column (0-origin) modulo tab_size == 0
     * </pre>
     * The cursor will not go past the last column.
     * <p>
     * Note that the conventional assumption of what a tab is, is not
     * entirely accurate. ANSI does not define TABs as above but rather
     * as a directive to move to the next "tabstop" which has to have been
     * set previously. In fact on unixes it is the terminal line discipline
     * that expands tabs to spaces  in the conventional way. That in,
     * turn explains why TAB information doesn't make it into selections and
     * why copying and pasting Makefile instructions is liable to lead
     * to hard-to-diagnose make problems, which, in turn drove the ANT people
     * to reinvent the world.
     * @param tab_size The property value.
     */
    public void setTabSize(int tab_size) {
        this.tab_size = tab_size;
    }

    /**
     * Get the TAB size.
     * @return The property value.
     */
    public int getTabSize() {
        return tab_size;
    }
    private int tab_size = 8;
    
    /**
     * Set select-by-word delimiters.
     * <p>
     * When double-clicking on terminal screen selection will expand on left and
     * right until reaches one of a delimiters or the whitespace symbol (which
     * is delimiter by default).
     * @param delimiters The property value.
     */
    public void setSelectByWordDelimiters(String delimiters) {
        this.delimiters = delimiters;
        word_delineator = WordDelineator.createCustomDelineator(delimiters);
    }

    public String getSelectByWordDelimiters() {
        return delimiters;
    }
    private String delimiters;

    /**
     * Control whether Term scrolls to the bottom on keyboard input.
     * <p>
     * This is analogous to the xterm -sk/+sk option.
     * @param scroll_on_input The property value
     */
    public void setScrollOnInput(boolean scroll_on_input) {
        this.scroll_on_input = scroll_on_input;
    }

    /**
     * Return whether Term scrolls to the bottom on keyboard input.
     * @return The property value.
     */
    public boolean isScrollOnInput() {
        return scroll_on_input;
    }
    private boolean scroll_on_input = true;

    /**
     * Control whether Term scrolls on any output.
     * <p>
     * When set to false, if the user moves the scrollbar to see some
     * text higher up in history, the view will not change even if more
     * output is produced. But if the cursor is visible, scrolling will
     * happen. This is so that in an interactive session any prompt will
     * be visible etc.
     * <p>
     * However, the tracking of the cursor is controlled by the 'trackCursor'
     * property which by default is set to 'true'.
     * <p>
     * This property is analogous to the xterm -si/+si option.
     * @param scroll_on_output The property value.
     */
    public void setScrollOnOutput(boolean scroll_on_output) {
        this.scroll_on_output = scroll_on_output;
    }

    /**
     * Return whether Term scrolls on any output.
     * @return The property value.
     */
    public boolean isScrollOnOutput() {
        return scroll_on_output;
    }
    private boolean scroll_on_output = true;

    /**
     * Control whether the Alt key prefixes a key with an ESC or shifts
     * the character.
     * <p>
     * This is based on XTerms altSendsEscape resource. There's a family
     * of interrelated resources as follows. You can read about them in
     * <b>Xterm Control Sequences</b> under <b>Alt and Meta Keys</b> for
     * a summary or the XTerm man page for more details.
     * <table border="1">
     * <tr>
     * 		<th>Resource</th>
     * 		<th>Term assumed value</th>
     * </tr>
     * <tr>
     * 		<td>metaSendsEscape</td>
     * 		<td>N/A</td>
     * </tr>
     * <tr>
     * 		<td>altIsNotMeta</td>
     * 		<td>false (Alt <u>is</u> Meta)</td>
     * </tr>
     * <tr>
     * 		<td>modifyOtherKeys</td>
     * 		<td>0 (disable)</td>
     * </tr>
     * <tr>
     * 		<td>eightBitInput</td>
     * 		<td>true</td>
     * </tr>
     * </table>
     * <p>
     * Default value is true.
     * @param altSendsEscape Sets the property.
     */
    public void setAltSendsEscape(boolean altSendsEscape) {
	this.altSendsEscape = altSendsEscape;
    }

    /**
     * Return whether the Alt key prefixes a key with an ESC or shifts
     * the character.
     * @return The property value.
     */
    public boolean getAltSendsEscape() {
	return altSendsEscape;
    }

    private boolean altSendsEscape = true;

    /**
     * Control whether Term will scroll to track the cursor as text is added.
     * <p>
     * If set to true, as output is being generated Term will try to keep
     * the cursor in view.
     * <p>
     * This property is only relevant when scrollOnOutput is set to false.
     * If scrollOnOutput is true, this property is also implicitly true.
     * @param track_cursor The property value.
     */
    public void setTrackCursor(boolean track_cursor) {
        this.track_cursor = track_cursor;
    }

    /**
     * Return whether Term will scroll to track the cursor as text is added.
     * @return The property value
     */
    public boolean isTrackCursor() {
        return track_cursor;
    }
    private boolean track_cursor = true;

    /**
     * Controls horizontal scrolling and line wrapping.
     * <p>
     * When enabled a horizontal scrollbar becomes visible and line-wrapping
     * is disabled.
     * @param horizontally_scrollable The property value.
     */
    public void setHorizontallyScrollable(boolean horizontally_scrollable) {
        this.horizontally_scrollable = horizontally_scrollable;
        // hscroll_bar.setVisible(horizontally_scrollable);
        hscroll_wrapper.setVisible(horizontally_scrollable);
    }

    /*
     * Returns whether horizontal scrolling is enabled.
     * @see Term.setHorizontallyScrollable
     */
    public boolean isHorizontallyScrollable() {
        return this.horizontally_scrollable;
    }

    private boolean horizontally_scrollable = true;

    public final void setRenderingHints(Map<?, ?> hints) {
        renderingHints = hints;
    }
    
    /**
     * Clear everything and assign new text.
     * <p>
     * If the size of the text exceeds history early parts of it will get
     * lost, unless an anchor was set using setAnchor().
     * @param text The text to put into the terminal buffer.
     */
    public void setText(String text) {
        // SHOULD make a bit more efficient
        clearHistoryNoRefresh();
        appendText(text, true);
    }

    /**
     * Add new text at the current cursor position.
     * <p>
     * Doesn't repaint the view unless 'repaint' is set to 'true'.
     * <br>
     * Doesn't do anything if 'text' is 'null'.
     * @param text
     * @param repaint
     */
    public void appendText(String text, boolean repaint) {

        if (text == null) {
            return;
        }

        ckEventDispatchThread();
        // OLD NPE-x synchronized(this)
        {
            for (int cx = 0; cx < text.length(); cx++) {
                putc_work(text.charAt(cx));
                if (text.charAt(cx) == '\n') {
                    putc_work('\r');
                }
            }
        }
        if (repaint) {
            repaint(true);
        }
    }

    /**
     * Scroll the view 'n' pages up.
     * <p>
     * A page is the height of the view.
     * @param n Number of pages to scroll up.
     */
    public void pageUp(int n) {
        ckEventDispatchThread();
        // OLD NPE-x synchronized(this)
        {
            st.firstx -= n * st.rows;
            if (st.firstx < 0) {
                st.firstx = 0;
            }
        }
        repaint(true);
    }

    /**
     * Scroll the view 'n' pages down.
     * <p>
     * A page is the height of the view.
     * @param n Number of pages to scroll down.
     */
    public void pageDown(int n) {
        ckEventDispatchThread();
        // OLD NPE-x synchronized(this)
        {
            st.firstx += n * st.rows;

            if (st.firstx + st.rows > buf.nlines()) {
                st.firstx = buf.nlines() - st.rows;
            }
        }
        repaint(true);
    }

    /**
     * Scroll the view 'n' lines up.
     * @param n The number of lines to scroll up.
     */
    public void lineUp(int n) {
        ckEventDispatchThread();
        // OLD NPE-x synchronized(this)
        {
            st.firstx -= n;
            if (st.firstx < 0) {
                st.firstx = 0;
            }
        }
        repaint(true);
    }

    /**
     * Scroll the view 'n' lines down.
     * @param n The number of lines to scroll down.
     */
    public void lineDown(int n) {
        ckEventDispatchThread();
        // OLD NPE-x synchronized(this)
        {
            st.firstx += n;
            if (st.firstx + st.rows > buf.nlines()) {
                st.firstx = buf.nlines() - st.rows;
            }
        }
        repaint(true);
    }

    /**
     * Scroll the view 'n' pages to the left.
     * @param n The number of pages to scroll left.
     */
    public void pageLeft(int n) {
        columnLeft(n * buf.visibleCols());
    }

    /**
     * Scroll the view 'n' pages to the right.
     * @param n The number of pages to scroll right.
     */
    public void pageRight(int n) {
        columnRight(n * buf.visibleCols());
    }

    /**
     * Scroll the view 'n' columns to the right.
     * @param n The number of columns to scroll to the right.
     */
    public void columnRight(int n) {
        ckEventDispatchThread();
        // OLD NPE-x synchronized(this)
        {
            st.firsty += n;
            if (st.firsty + buf.visibleCols() > buf.totalCols()) {
                st.firsty = buf.totalCols() - buf.visibleCols();
            }
        }
        repaint(true);
    }

    /**
     * Scroll the view 'n' columns to the left.
     * @param n The number of columns to scroll to the left.
     */
    public void columnLeft(int n) {
        ckEventDispatchThread();
        // OLD NPE-x synchronized(this)
        {
            st.firsty -= n;
            if (st.firsty < 0) {
                st.firsty = 0;
            }
        }
        repaint(true);
    }

    /**
     * Return the cell width of the given character.
     * @param c
     * @return 
     */
    public int charWidth(char c) {
        return metrics.wcwidth(c);
    }

    /**
     * If set to true then it is expected that setFont() will receive a fixed
     * width font.
     * @param fixedFont Controls whether setFont() expects a fixed-width font.
     */
    public void setFixedFont(boolean fixedFont) {
        this.fixedFont = fixedFont;
    }

    /**
     * Returns whether setFont() expects a fixed-width font.
     * @return whether setFont() expects a fixed-width font.
     */
    public boolean isFixedFont() {
        return fixedFont;
    }


    /*
     * The following are overrides of JComponent/Component
     */
    /**
     * Override of JComponent.
     * <p>
     * We absolutely require fixed width fonts, so if the font is changed
     * we create a monospaced version of it with the same style and size.
     * @param new_font
     */
    @Override
    public final void setFont(Font new_font) {
        Font font;
        if (isFixedFont()) {
            font = new_font;
        } else {
            font = new Font("Monospaced",	// NOI18N
                            new_font.getStyle(),
                            new_font.getSize());
        }
        
        super.setFont(font);	// This should invalidate us, which
        // ultimately will cause a repaint

        /* DEBUG
        System.out.println("Font info:"); // NOI18N
        System.out.println("\tlogical name: " + font.getName()); // NOI18N
        System.out.println("\tfamily name: " + font.getFamily()); // NOI18N
        System.out.println("\tface name: " + font.getFontName()); // NOI18N
         */

        // cache the metrics
        metrics = new MyFontMetrics(this, font);
        updateScreenSize();
    }

    /**
     * Override of JComponent.
     * <p>
     * Pass on the request to the screen where all the actual focus
     * management happens.
     */
    @Override
    public void requestFocus() {
        screen.requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        return screen.requestFocusInWindow();
    }

    /**
     * Override of JComponent.
     * <p>
     * Pass on enabledness to sub-components (scrollbars and screen)
     */
    @Override
    public void setEnabled(boolean enabled) {
        // This was done as a result of issue 24824

        super.setEnabled(enabled);

        hscroll_bar.setEnabled(enabled);
        vscroll_bar.setEnabled(enabled);
        screen.setEnabled(enabled);
    }

    //..........................................................................
    // Accessibility stuff is all here
    // Not just the required interfaces but also all the helpers.
    //..........................................................................
    /**
     * Since Term is a composite widget the main accessible JComponent is
     * not Term but an internal JComponent. We'll speak of Term accessibility
     * when we in fact are referring to the that inner component.
     * <p>
     * Accessibility for Term is tricky because it doesn't fit into the
     * roles delineated by Swing. The closest role is that of TEXT and that
     * is too bound to how JTextComponent works. To wit ...
     * <p>
     * <dl>
     * <dt>2D vs 1D coordinates
     * <dd>
     *     Term has a 2D coordinate system while AccessibleText works with 1D
     *     locations. So Term actually has code which translates between the two.
     *     This code is not exactly efficient but only kicks in when assistive
     *     technology latches on.
     *     <br>
     *     Line breaks ('\n's) count as characters! However we only count
     *     logical line breaks ('\n's appearing in the input stream) as opposed to
     *     wrapped lines!
     *     <p>
     *     The current implementation doesn't cache any of the mappings because
     *     that would require a word per line extra storage for the cumulative
     *     char count. The times actually we're pretty fast with a 4000 line
     *     histroy.
     *
     * <dt>WORDs and SENTENCEs
     * <dd>
     *     For AccessibleText.get*Index() functions WORD uses the regular
     *     Term WordDelineator. SENTENCE translates to just a line.
     *
     *     <dt>Character attributes
     *     <dd>
     *     Term uses the ANSI convention of character attributes so when
     *     AccessibleText.getCharacterAttribute() is used a rough translation
     *     is made as follows:
     *     <ul>
     *     <li> ANSI underscore -> StyleConstants.Underline
     *     <li> ANSI bright/bold -> StyleConstants.Bold
     *     <li> Non-black foreground color -> StyleConstants.Foreground
     *     <li> Explicitly set background color -> StyleConstants.Background
     *     </ul>
     *     Font related information is always constant so it is not provided.
     *
     * <dt>History
     * <dd>
     *     Term has history and lines wink out. If buffer coordinates were
     *     used to interact with accessibility, caretPosition and charCount
     *     would be dancing around. Fortunately Term has absolute coordinates.
     *     So positions returned via AccessibleText might eventually refer to
     *     text that has gone by.
     *
     * <dt>Caret and Mark vs Cursor and Selection
     * <dd>
     *     While Term keeps the selection and cursor coordinates independent,
     *     JTextComponent merges them and AccessibleText inherits this view.
     *     With Term caretPosition is the position of the cursor and selection
     *     ends will not neccessarily match with the caret position.
     * </dl>
     * <p>
     * Currently only notifications of ACCESSIBLE_CARET_PROPERTY and
     * ACCESSIBLE_TEXT_PROPERTY are fired and that always in pairs.
     * They are fired on the receipt of any character to be processed.
     * <p>
     * IMPORTANT: It is assumed that under assistive technology Term will be
     * used primarily as a continuous text output device or a readonly document.
     * Therefore ANSI cursor motion and text editing commands or anything that
     * mutates the text will completely invalidate all of AccessibleTexts
     * properties. (Perhaps an exception SHOULD be made for backspace)
     */
    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessible_context == null) {
            accessible_context = new AccessibleTerm();
        }
        return accessible_context;
    }
    private AccessibleContext accessible_context;

    /*
     * Term is really a container. Screen is where things get drawn and
     * where focus is set to, so all real accessibility work is done there.
     * We just declare us to have a generic role.
     */
    protected class AccessibleTerm extends AccessibleJComponent {

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PANEL;
        }

        @Override
        public void setAccessibleName(String name) {
            screen.getAccessibleContext().setAccessibleName(name);
        }
    }

    /**
     * [DO NOT USE] Convert a 2D Coord to a 1D linear position.
     * <p>
     * This function really should be private but I need it to be public for
     * unit-testing purposes.
     * @param c
     * @return 
     */
    public int CoordToPosition(Coord c) {
        BCoord b = c.toBCoord(firsta);
        int nchars = charsInPrehistory;
        for (int r = 0; r < b.row; r++) {
            Line l = buf.lineAt(r);
            nchars += l.length();
            if (!l.isWrapped()) {
                nchars += 1;
            }
        }

        nchars += c.col;

        return nchars;
    }

    /**
     * [DO NOT USE] Convert a 1D linear position to a 2D Coord.
     * <p>
     * This function really should be private but I need it to be public for
     * unit-testing purposes.
     * @param position
     * @return 
     */
    public Coord PositionToCoord(int position) {
        int nchars = charsInPrehistory;
        for (int r = 0; r < buf.nlines(); r++) {
            Line l = buf.lineAt(r);
            nchars += l.length();
            if (!l.isWrapped()) {
                nchars += 1;
            }
            if (nchars > position) {
                BCoord b = new BCoord();
                b.row = r;
                b.col = buf.lineAt(r).length() + 1 - (nchars - position);
                return new Coord(b, firsta);
            }
        }
        return null;
    }

    /**
     * Return the number of characters stored.
     * <p>
     * Include logical newlines for now to match the above conversions.
     * Hmm, do we include chars in prehistory?
     */
    int getCharCount() {
        int nchars = charsInPrehistory;
        for (int r = 0; r < buf.nlines(); r++) {
            Line l = buf.lineAt(r);
            nchars += l.length();
            if (!l.isWrapped()) {
                nchars += 1;
            }
        }
        return nchars;
    }

    /**
     * Return the bounding rectangle of the character at the given coordinate
     */
    Rectangle getCharacterBounds(Coord c) {
        if (c == null) {
            return null;
        }
        BCoord b = c.toBCoord(firsta);

        char ch = '\0';
        try {
            Line l = buf.lineAt(b.row);
            // OLD ch = l.charArray()[b.col];
            ch = l.charAt(b.col);
        } catch (Exception x) {
            //
        }

        Point p1 = toPixel(b);
        Rectangle rect = new Rectangle();
        rect.x = p1.x;
        rect.y = p1.y;
        rect.width = metrics.width * charWidth(ch);
        rect.height = metrics.height;
        return rect;
    }

    private Color csetBG(int attr) {
	final int px = Attr.backgroundColor(attr);
	final Color c = palette[px];
	return c;
    }

    private Color csetFG(int attr) {
	final int px = Attr.foregroundColor(attr);
        final Color c = palette[px];
	return c;
    }

    Color backgroundColor(boolean reverse, int attr) {
        final Color c;
        if (reverse) {
	    c = csetFG(attr);
        } else {
	    c = csetBG(attr);
        }
        return c;
    }

    Color foregroundColor(boolean reverse, int attr) {
        final Color c;
        if (reverse) {
	    c = csetBG(attr);
        } else {
	    c = csetFG(attr);
        }
        return c;
    }

    private static void ckEventDispatchThread() {
        /*
        if (!SwingUtilities.isEventDispatchThread()) {
        System.out.println("term: NOT IN EventDispatchThread");
        Thread.dumpStack();
        }
         */
    }

    /* attaches MouseWheelHandler to scroll the component
     */
    private static void addMouseWheelHandler(JComponent comp, JScrollBar bar) {
        comp.addMouseWheelListener(new MouseWheelHandler(bar)); // XXX who removes this lsnr?
    }

    private static class MouseWheelHandler implements MouseWheelListener {

        private final JScrollBar scrollbar;

        public MouseWheelHandler(JScrollBar scrollbar) {
            this.scrollbar = scrollbar;
        }

	@Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int totalScrollAmount = e.getUnitsToScroll() * scrollbar.getUnitIncrement();
            scrollbar.setValue(scrollbar.getValue() + totalScrollAmount);
        }
    }

    /*
     * Logging.
     */
    private boolean sequenceLogging;
    private Set<String> completedSequences;
    private Set<String> unrecognizedSequences;

    public final void setSequenceLogging(boolean sequenceLogging) {
        this.sequenceLogging = sequenceLogging;
    }

    public final boolean isSequenceLogging() {
        return sequenceLogging;
    }

    public final Set<String> getCompletedSequences() {
        return completedSequences;
    }

    public final Set<String> getUnrecognizedSequences() {
        return unrecognizedSequences;
    }

    private void logCompletedSequence(String sequence) {
        if (!sequenceLogging)
            return;
        if (completedSequences == null)
            completedSequences = new HashSet<>();
        completedSequences.add(sequence);
    }

    private void logUnrecognizedSequence(String sequence) {
        if (!sequenceLogging)
            return;
        if (unrecognizedSequences == null)
            unrecognizedSequences = new HashSet<>();
        unrecognizedSequences.add(sequence);
    }

    /**
     * @return the metrics
     */
    MyFontMetrics metrics() {
	return metrics;
    }

    /**
     * @return the buf
     */
    Buffer buf() {
	return buf;
    }

    /**
     * @return the firsta
     */
    int firsta() {
	return firsta;
    }
}

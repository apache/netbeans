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
package org.netbeans.conffile.ui.comp;

import org.netbeans.conffile.ui.Localization;
import static org.netbeans.conffile.ui.Localization.NO;
import static org.netbeans.conffile.ui.Localization.YES;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.getExtendedKeyCodeForChar;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 *
 * @author Tim Boudreau
 */
public class UIUtils {

    public static final String CLIENT_PROP_NO_MNEMONIC = "nomnemonic"; //NOU18N
    public static final String MNEMONIC_PRIORITY = "mnemonic_priority"; //NOU18N
    private static final String[] DEFAULT_FONTS = new String[]{
        "Arimo", //NOU18N
        "Verdana", //NOU18N
        "SansSerif", //NOU18N
        "Arial", //NOU18N
        "Helvetica Neue", //NOU18N
        "DejaVu Sans", //NOU18N
        "Bitstream Vera Sans", //NOU18N
        "Geneva", //NOU18N
        "Source Sans Pro", //NOU18N
        "Droid Sans", //NOU18N
        "Trebuchet MS", //NOU18N
        "Liberation Sans", //NOU18N
        "Ubuntu", //NOU18N
        "Times New Roman",}; //NOU18N

    /**
     * Set the full-blast text-antialiasing rendering hint on a graphics, then
     * call the callback, restoring its state afterwards.
     *
     * @param g A graphics
     * @param c A callback, typically a method reference to
     * <code>super::paint</code>.
     */
    public static void withTextAntialiasing(Graphics g, Consumer<Graphics> c) {
        Graphics2D gg = (Graphics2D) g;
        Object old = gg.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        c.accept(gg);
        gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, old);
    }

    /**
     * Set the full-blast drawing-antialiasing rendering hint on a graphics,
     * then call the callback, restoring its state afterwards.
     *
     * @param g A graphics
     * @param c A callback, typically a method reference to
     * <code>super::paint</code>.
     */
    public static void withPaintAntialiasing(Graphics g, Consumer<Graphics> c) {
        Graphics2D gg = (Graphics2D) g;
        Object old = gg.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        c.accept(gg);
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, old);
    }

    /**
     * Set the full-blast drawing- <i>and</i> text-antialiasing rendering hint
     * on a graphics, then call the callback, restoring its state afterwards.
     *
     * @param g A graphics
     * @param c A callback, typically a method reference to
     * <code>super::paint</code>.
     */
    public static void withAntialiasing(Graphics g, Consumer<Graphics> c) {
        withPaintAntialiasing(g, g1 -> {
            withTextAntialiasing(g1, c);
        });
    }

    /**
     * Drill through a container's contents, finding any labels, buttons or text
     * boxes which should have a mnemonic set on them, then using the text
     * contents to set appropriate mnemonics on a first-come, first-served
     * basis. Setting the client property MNEMONIC_PRIORITY to Boolean.TRUE will
     * move components to the head of the queue, and is used for components that
     * appear on all pages to avoid inconsistency.
     * <p>
     * If the text contains some text in parentheses, only the parenthesized
     * content will be used for mnemonics.
     * </p><p>
     * This is somewhat brute force, but does the job for accessibility.
     * </p>
     *
     * @param container
     */
    public static void mnemonics(Container container) {
        List<JComponent> comp = new ArrayList<>(20);
        Map<Character, JComponent> used = new HashMap<>();
        findMnemonicComponents(container, comp);
        // Ensure that the Next/Finish and Prev buttons always get handled first
        Collections.sort(comp, (a, b) -> {
            boolean aPrio = Boolean.TRUE.equals(a.getClientProperty(MNEMONIC_PRIORITY));
            boolean bPrio = Boolean.TRUE.equals(b.getClientProperty(MNEMONIC_PRIORITY));
            if (aPrio && bPrio) {
                return 0;
            } else if (aPrio && !bPrio) {
                return -1;
            } else if (!aPrio && bPrio) {
                return 1;
            } else {
                return 0;
            }
        });
        if (!comp.isEmpty()) {
            mnemonics(used, comp);
        }
    }

    @SuppressWarnings({"BoxedValueEquality", "NumberEquality"})
    private static void findMnemonicComponents(Container container, List<? super JComponent> into) {
        for (Component child : container.getComponents()) {
            if (child instanceof JLabel) {
                into.add((JComponent) child);
            } else if (child instanceof JButton) {
                into.add((JComponent) child);
            } else if (child instanceof JCheckBox) {
                into.add((JComponent) child);
            } else if (child instanceof Container) {
                findMnemonicComponents((Container) child, into);
            }
        }
    }

    private static void mnemonics(Map<Character, JComponent> used, Iterable<? extends JComponent> comps) {
        for (JComponent c : comps) {
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                if (l.getLabelFor() != null) {
                    String label = l.getText();
                    char[] chars = label.toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        if (!Character.isAlphabetic(chars[i])) {
                            continue;
                        }
                        char ch = Character.toLowerCase(chars[i]);
                        if (!used.containsKey(ch)) {
                            l.setDisplayedMnemonic(chars[i]);
                            l.setDisplayedMnemonicIndex(i);
                            used.put(ch, l);
                            break;
                        }
                    }
                }
            } else if (c instanceof JCheckBox) {
                JCheckBox l = (JCheckBox) c;
                String label = l.getText();
                char[] chars = label.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if (!Character.isAlphabetic(chars[i])) {
                        continue;
                    }
                    char ch = Character.toLowerCase(chars[i]);
                    if (!used.containsKey(ch)) {
                        l.setMnemonic(chars[i]);
                        l.setDisplayedMnemonicIndex(i);
                        used.put(ch, l);
                        break;
                    }
                }
            } else if (c instanceof JButton) {
                JButton b = (JButton) c;
                String txt = b.getText();
                int offset = 0;
                // Ensure the font buttons use a mnemonic from the font
                // name, not just whatever
                int opIx = txt.indexOf('('); //NOU18N
                int cpIx = txt.indexOf(')'); //NOU18N
                if (cpIx > opIx + 1 && opIx > 0) {
                    txt = txt.substring(opIx + 1, cpIx);
                    offset = opIx + 1;
                }

                char[] chars = txt.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    switch (chars[i]) {
                        case '+': //NOU18N
                        case '-': //NOU18N
                            break;
                        default:
                            if (!Character.isAlphabetic(chars[i]) && !Character.isDigit(chars[i])) {
                                continue;
                            }
                    }
                    char ch = Character.toLowerCase(chars[i]);
                    if (!used.containsKey(ch)) {
                        switch (ch) {
                            case '+':
                                // This is making an assumption about US keyboard layout,
                                // but preferable to no mnemonic
                                ch = '=';
                        }
                        b.setMnemonic(ch);
                        b.setDisplayedMnemonicIndex(i + offset);
                        used.put(ch, b);
                        break;
                    }
                }
            }
        }
    }

    public static void sizeToDisplay(int marginFactor, GraphicsConfiguration config) {
        Window[] ws = Dialog.getWindows();
        for (Window w : ws) {
            if (w instanceof JFrame || w instanceof JDialog) {
                if (w.getGraphicsConfiguration() == null) {
                    continue;
                }
//                Rectangle max = new Rectangle(0, 0, dev.getDisplayMode().getWidth(), dev.getDisplayMode().getHeight());
                if (config == null) {
                    config = w.getGraphicsConfiguration();
                }
                Rectangle max = config.getBounds();
                Dimension dim = max.getSize();

                // Avoid the window jumping, once for the pack and once
                // for the re-centering
                w.setIgnoreRepaint(true);
                try {
                    synchronized (w.getTreeLock()) {
                        Dimension curr = new Dimension(dim.width - (dim.width / (marginFactor * 2)), dim.height - (dim.height / (marginFactor * 2)));
                        Dimension size = w.getPreferredSize();
                        curr.width = Math.min(dim.width, Math.max(curr.width, size.width));
                        curr.height = Math.min(dim.height, Math.max(curr.height, size.height));
                        int left = (max.width - curr.width) / 2;
                        int top = (max.height - curr.height) / 2;
                        Point p = new Point(left + max.x, top + max.y);
                        w.setBounds(new Rectangle(p, curr));
                    }
                } finally {
                    w.setIgnoreRepaint(false);
                    w.repaint();
                }
                break;
            }
        }
    }

    private static Container contentPaneFor(Window w) {
        return w instanceof JFrame ? ((JFrame) w).getContentPane()
                : w instanceof JDialog ? ((JDialog) w).getContentPane()
                        : null;
    }

    /**
     * Repack the top level frame or dialog so its contents fit if it is
     * undersized, leaving it alone if it fits the current content.
     */
    public static void repack(GraphicsConfiguration preferred) {
        Window[] ws = Dialog.getWindows();
        for (Window w : ws) {
            if (w instanceof JFrame || w instanceof JDialog) {
                GraphicsDevice dev = preferred == null ? w.getGraphicsConfiguration().getDevice() : preferred.getDevice();
                Rectangle max = dev.getDefaultConfiguration().getBounds();
                Container contentPane = contentPaneFor(w);
                Dimension dim = contentPane.getPreferredSize();
                Dimension curr = contentPane.getSize();
                if (curr.width < dim.width || curr.height < dim.height) {
                    // Avoid the window jumping, once for the pack and once
                    // for the re-centering
                    w.setIgnoreRepaint(true);
                    try {
                        synchronized (w.getTreeLock()) {
                            w.pack();
                            Dimension size = w.getPreferredSize();
                            int left = max.x + Math.max(0, (max.width / 2) - (size.width / 2));
                            int top = max.y + Math.max(0, (max.height / 2) - (size.height / 2));
                            Point p = new Point(left, top);
                            w.setLocation(p);
                        }
                    } finally {
                        w.setIgnoreRepaint(false);
                        w.repaint();
                    }
                }
                break;
            }
        }
    }

    /**
     * Returns a <i>curated</i> list of reasonable display fonts, some of which
     * should be available on all platforms.
     *
     * @return A sorted set of font names
     */
    public static Set<String> availableUIFonts(String... include) {
        Set<String> fonts = new TreeSet<>(Arrays.asList(DEFAULT_FONTS));
        fonts.addAll(Arrays.asList(include));
        fonts.retainAll(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        return fonts;
    }

    public static Action action(String name, Runnable run) {
        return new RunAction(run, name);
    }

    public static Action action(Runnable run) {
        return action(Long.toString(System.nanoTime()), run);
    }

    public static Action action(Consumer<ActionEvent> c) {
        return action(Long.toString(System.nanoTime()), c);
    }

    public static Action action(String name, Consumer<ActionEvent> c) {
        return new RunAction2(c, name);
    }

    private static final class RunAction extends AbstractAction {

        private final Runnable r;

        public RunAction(Runnable r, String name) {
            super(name);
            this.r = r;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AWTEvent evt = EventQueue.getCurrentEvent();
            if (evt instanceof KeyEvent) {
                ((KeyEvent) evt).consume();
            } else if (evt instanceof MouseEvent) {
                ((MouseEvent) evt).consume();
            }
            r.run();
        }
    }

    private static final class RunAction2 extends AbstractAction {

        private final Consumer<ActionEvent> r;

        public RunAction2(Consumer<ActionEvent> r, String name) {
            super(name);
            this.r = r;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AWTEvent evt = EventQueue.getCurrentEvent();
            if (evt instanceof KeyEvent) {
                ((KeyEvent) evt).consume();
            } else if (evt instanceof MouseEvent) {
                ((MouseEvent) evt).consume();
            }
            r.accept(e);
        }
    }

    private static JDialog createDialog(Class<? extends Component> contentPaneType) {
        JDialog result = null;
        for (Frame f : Frame.getFrames()) {
            if (f instanceof JFrame && contentPaneType.isInstance(((JFrame) f).getContentPane())) {
                result = new JDialog(Frame.getFrames()[0]);
            }
        }
        if (Dialog.getWindows().length > 0) {
            for (Window w : Dialog.getWindows()) {
                if (w instanceof JDialog && contentPaneType.isInstance(((JDialog) w).getContentPane())) {
                    result = new JDialog(w);
                }
            }
        }
        if (result == null) {
            result = new JDialog();
        }
        Component previousFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (previousFocusOwner != null) {
            result.addHierarchyListener(new FocusRestorer(previousFocusOwner));
        } else {
        }
        return result;
    }

    static final class FocusRestorer implements HierarchyListener {

        private final Component previousFocusOwner;

        FocusRestorer(Component previousFocusOwner) {
            this.previousFocusOwner = previousFocusOwner;
        }

        public void windowClosed() {
            if (previousFocusOwner != null) {
                Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class, previousFocusOwner);
                if (w != null) {
                    w.toFront();
                    w.requestFocus();
                }
                previousFocusOwner.requestFocusInWindow();
                previousFocusOwner.requestFocus();
            }
        }

        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (!e.getChanged().isShowing()) {
                    windowClosed();
                }
            }
        }
    }

    static Function<Component, Component> glassPaneSetter(Class<? extends Component> contentPaneType) {
        return glassPaneSetter(container -> {
            return (container instanceof JFrame && contentPaneType.isInstance(((JFrame) container).getContentPane()))
                    || (container instanceof JDialog && contentPaneType.isInstance(((JDialog) container).getContentPane()));

        });
    }

    static Function<Component, Component> glassPaneSetter(Predicate<Container> pred) {
        if (Frame.getFrames().length > 0) {
            for (Frame f : Frame.getFrames()) {
                if (f instanceof JFrame) {
                    JFrame jf = (JFrame) f;
                    if (pred.test(jf)) {
                        return (Component newGlassPane) -> {
                            Component result = jf.getGlassPane();
                            jf.setGlassPane(newGlassPane);
                            jf.invalidate();
                            jf.revalidate();
                            jf.repaint();
                            return result;
                        };
                    }
                }
            }
        } // NOT ELSE! AWT can create invisible frames
        if (Dialog.getWindows().length > 0) {
            for (Window w : Dialog.getWindows()) {
                if (w instanceof JDialog) {
                    JDialog jf = (JDialog) w;
                    if (pred.test(jf)) {
                        return (Component newGlassPane) -> {
                            Component result = jf.getGlassPane();
                            jf.setGlassPane(newGlassPane);
                            jf.invalidate();
                            jf.revalidate();
                            jf.repaint();
                            return result;
                        };
                    }
                }
            }
        }
        return (Component ignored) -> ignored;
    }

    public static void shade(Runnable r, Class<? extends Component> contentPaneType) {
        Function<Component, Component> glassPaneSetter = glassPaneSetter(contentPaneType);
        Shade shade = new Shade();
        Component origGlassPane = glassPaneSetter.apply(shade);
        try {
            shade.setVisible(true);
            r.run();
        } finally {
            glassPaneSetter.apply(origGlassPane);
            origGlassPane.setVisible(false);
        }
    }

    public static boolean showConfirmationDialog(Font textFont, Font buttonFont, Localization message, Localization title, Class<? extends Component> contentPaneTypeForOwner) {
        boolean[] result = new boolean[1];
        JDialog dlg = createDialog(contentPaneTypeForOwner);
        dlg.setUndecorated(true);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.setTitle(title.toString());
        dlg.setAlwaysOnTop(true);
        dlg.setModal(true);
        JPanel contents = new JPanel(new GridBagLayout());
        contents.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        dlg.setContentPane(contents);
        contents.setUI(new GradientBackgroundPanelUI());
        AATextArea textArea = message.set(new AATextArea());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setColumns(textArea.getText().length() / 2);
        textArea.setRows(2);
        textArea.setFont(textFont);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.ipady = 24;
        gbc.fill = GridBagConstraints.BOTH;
        contents.add(textArea, gbc);
        JLabel spacer = new JLabel(""); //NOU18N
        spacer.setFont(textFont);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.ipadx = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(12, 5, 5, 5);
        contents.add(spacer, gbc);

        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.VERTICAL;

        JButton yesButton = YES.button();
        JButton noButton = NO.button();
        yesButton.setFont(buttonFont);
        noButton.setFont(buttonFont);

        contents.add(yesButton, gbc);
        gbc.gridx++;
        contents.add(noButton, gbc);
        ActionListener al = evt -> {
            result[0] = evt.getSource() == yesButton;
            dlg.setVisible(false);
        };
        WindowListener wl = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dlg.setVisible(false);
                dlg.dispose();
            }

            @Override
            public void windowOpened(WindowEvent e) {
                textArea.requestFocus();
            }
        };
        dlg.addWindowListener(wl);
        InputMap inputMap = contents.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = contents.getActionMap();
        Action esc = UIUtils.action(() -> dlg.setVisible(false));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc"); //NOI18N
        actionMap.put("esc", esc); //NOI18N
        yesButton.addActionListener(al);
        noButton.addActionListener(al);

        char y = yesButton.getText().length() == 0 ? 'y' //NOI18N
                : Character.toLowerCase(yesButton.getText().charAt(0));
        char n = noButton.getText().length() == 0 ? 'n' //NOI18N
                : Character.toLowerCase(noButton.getText().charAt(0));
        if (y != n) {
            KeyStroke ks = KeyStroke.getKeyStroke(getExtendedKeyCodeForChar(y), 0, false);
            inputMap.put(ks, "yes"); //NOI18N
            actionMap.put("yes", UIUtils.action(() -> { //NOI18N
                result[0] = true;
                dlg.setVisible(false);
            }));
            KeyStroke noKs = KeyStroke.getKeyStroke(getExtendedKeyCodeForChar(n), 0, false);
            inputMap.put(noKs, "no"); //NOI18N
            actionMap.put("no", esc); //NOI18N
        }

        dlg.setResizable(false);
        dlg.getRootPane().setDefaultButton(noButton);
        dlg.setLocationByPlatform(true);
        dlg.pack();
        Dimension prefSize = dlg.getPreferredSize();
        Rectangle bds = dlg.getGraphicsConfiguration().getBounds();
        int xOffset = (bds.width / 2) - (prefSize.width / 2);
        int yOffset = (bds.height / 2) - (prefSize.height / 2);
        dlg.setLocation(new Point(bds.x + xOffset, bds.y + yOffset));
        mnemonics(contents);
        shade(() -> {
            dlg.setVisible(true);
        }, contentPaneTypeForOwner);
        return result[0];
    }

    private UIUtils() {
        throw new AssertionError();
    }
}

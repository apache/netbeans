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
package org.netbeans.conffile.ui;

import org.netbeans.conffile.ArgsParser;
import org.netbeans.conffile.ConfFile;
import org.netbeans.conffile.DefaultOptionsReplacementChecker;
import org.netbeans.conffile.LineSwitchWriter;
import static org.netbeans.conffile.Main.ARG_PRETEND;
import static org.netbeans.conffile.Main.NETBEANS_DEFAULT_OPTIONS;
import org.netbeans.conffile.OS;
import org.netbeans.conffile.ui.comp.MarkableAAButton;
import org.netbeans.conffile.ui.comp.AATextField;
import org.netbeans.conffile.ui.comp.AATextArea;
import org.netbeans.conffile.ui.comp.GradientBackgroundPanelUI;
import org.netbeans.conffile.ui.ConfFileSettings.DisplayKind;
import static org.netbeans.conffile.ui.Localization.BROWSE;
import static org.netbeans.conffile.ui.Localization.CANCEL_DIALOG_MESSAGE;
import static org.netbeans.conffile.ui.Localization.CANCEL_DIALOG_TITLE;
import static org.netbeans.conffile.ui.Localization.CHOOSE_MONITOR_TEXT;
import static org.netbeans.conffile.ui.Localization.CHOOSE_MONITOR_TITLE;
import static org.netbeans.conffile.ui.Localization.CURRENT_IS_MAX_RESOLUTION_CHECKBOX;
import static org.netbeans.conffile.ui.Localization.DECREASE_FONT_SIZE;
import static org.netbeans.conffile.ui.Localization.DONE;
import static org.netbeans.conffile.ui.Localization.ERR_IS_DIRECTORY;
import static org.netbeans.conffile.ui.Localization.ERR_NO_FILE_SELECTED;
import static org.netbeans.conffile.ui.Localization.ERR_NO_SUCH_FILE;
import static org.netbeans.conffile.ui.Localization.FINISH;
import static org.netbeans.conffile.ui.Localization.FONTS_TITLE;
import static org.netbeans.conffile.ui.Localization.FONT_BUTTON_TEXT;
import static org.netbeans.conffile.ui.Localization.FONT_SIZE_TITLE;
import static org.netbeans.conffile.ui.Localization.INCREASE_FONT_SIZE;
import static org.netbeans.conffile.ui.Localization.LAPTOP_SCREEN;
import static org.netbeans.conffile.ui.Localization.LARGE_DESKTOP;
import static org.netbeans.conffile.ui.Localization.LINE_SWITCHES_INFO;
import static org.netbeans.conffile.ui.Localization.LOCATE_CONFIG_FILE;
import static org.netbeans.conffile.ui.Localization.MAX_MEMORY_SIZE;
import static org.netbeans.conffile.ui.Localization.MEMORY_INSTRUCTIONS;
import static org.netbeans.conffile.ui.Localization.MEMORY_PANEL_TITLE;
import static org.netbeans.conffile.ui.Localization.MIN_MEMORY_SIZE;
import static org.netbeans.conffile.ui.Localization.MONITOR_SIZE_QUESTION;
import static org.netbeans.conffile.ui.Localization.MONITOR_SIZE_TITLE;
import static org.netbeans.conffile.ui.Localization.MONITOR_TYPE_QUESTION;
import static org.netbeans.conffile.ui.Localization.MONITOR_TYPE_TITLE;
import static org.netbeans.conffile.ui.Localization.NEXT;
import static org.netbeans.conffile.ui.Localization.NO;
import static org.netbeans.conffile.ui.Localization.REMOVED_SWITCHES_INFO;
import static org.netbeans.conffile.ui.Localization.SAMPLE_TEXT;
import static org.netbeans.conffile.ui.Localization.SAVE_CONFIGURATION_FILE;
import static org.netbeans.conffile.ui.Localization.SELECT_ANTIALIAS_MODE_TITLE;
import static org.netbeans.conffile.ui.Localization.SMALL_DESKTOP;
import static org.netbeans.conffile.ui.Localization.TOTAL_MEMORY;
import static org.netbeans.conffile.ui.Localization.TWEAKS_TITLE;
import static org.netbeans.conffile.ui.Localization.USE_THESE_SETTINGS;
import static org.netbeans.conffile.ui.Localization.USE_UTF_8;
import static org.netbeans.conffile.ui.Localization.WHERE_NAME_IS_USED;
import static org.netbeans.conffile.ui.Localization.YES;
import static org.netbeans.conffile.ui.Localization.YOUR_NAME;
import static org.netbeans.conffile.ui.TweakEntry.availableTweaks;
import org.netbeans.conffile.ui.comp.AAFileChooser;
import org.netbeans.conffile.ui.comp.AALabel;
import org.netbeans.conffile.ui.comp.AASlider;
import org.netbeans.conffile.ui.comp.AAToolTipUI;
import org.netbeans.conffile.ui.comp.UIUtils;
import static org.netbeans.conffile.ui.comp.UIUtils.CLIENT_PROP_NO_MNEMONIC;
import static org.netbeans.conffile.ui.comp.UIUtils.MNEMONIC_PRIORITY;
import static org.netbeans.conffile.ui.comp.UIUtils.mnemonics;
import static org.netbeans.conffile.ui.comp.UIUtils.repack;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import static java.lang.Boolean.TRUE;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 *
 * @author Tim Boudreau
 */
public final class CalibrationGUI extends JPanel implements IntConsumer {

    private final FontSelectionPanel fonts;
    private final SelectAntialiasModePanel aa;
    private final FontSizePanel fontSizePanel;

    private final List<JPanel> panels = new ArrayList<>();
    private final ConfFileSettings settings;
    private final MarkableAAButton nextButton = Localization.NEXT.button();
    private final MarkableAAButton prevButton = Localization.BACK.button();
    private final MarkableAAButton cancelButton = Localization.CANCEL.button();
    static final String CLIENT_PROPERTY_SIZE = "sz";

    // On JDK 11 and up, casual lambdas get garbage collected if
    // weakly referenced; on JDK 8 they do not.  Yay.
    private final Runnable nextLambda = this::next;

    @SuppressWarnings({"OverridableMethodCallInConstructor", "LeakingThisInConstructor"})
    CalibrationGUI(ConfFileSettings settings) {
        Font f = settings.uiFont();
        UIManager.put("controlFont", f);
        setFont(f);
        if (DisplayAutoConfigurer.isMultipleScreens()) {
            ChooseMonitorPanel choose = new ChooseMonitorPanel(settings);
            choose.listen(nextLambda);
            panels.add(choose);
        }
        MonitorTypePanel typePanel = new MonitorTypePanel(settings);
        typePanel.listen(nextLambda);
        panels.add(typePanel);
        MonitorKindPanel kindPanel = new MonitorKindPanel(settings);
        kindPanel.listen(nextLambda);
        panels.add(kindPanel);
        fonts = new FontSelectionPanel(settings);
        fonts.listen(nextLambda);
        panels.add(fonts);
        aa = new SelectAntialiasModePanel(settings);
        aa.listen(nextLambda);
        panels.add(aa);
        fontSizePanel = new FontSizePanel(settings);
        fontSizePanel.listen(nextLambda);
        panels.add(fontSizePanel);
        MemorySelectionPanel mp = new MemorySelectionPanel(settings);
        panels.add(mp);
        mp.listen(nextLambda);
        panels.add(new TweaksPanel(settings));
        FinishPanel finish = new FinishPanel(settings);
        panels.add(finish);
        nextButton.setFont(f);
        prevButton.setFont(f);
        cancelButton.setFont(f);

        cancelButton.addActionListener(ae -> {
            exit(settings, 10);
        });

        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.ipadx = 12;
        bottom.add(cancelButton, gbc);
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridx++;
        gbc.weightx = 0;
        bottom.add(prevButton, gbc);
        gbc.weightx = 0;
        gbc.gridx++;
        gbc.insets = new Insets(0, 12, 0, 0);
        bottom.add(nextButton, gbc);
        setLayout(new BorderLayout());
        add(panels.get(0), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        nextButton.addActionListener(this::next);
        prevButton.addActionListener(this::prev);
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);
        nextButton.putClientProperty(MNEMONIC_PRIORITY, TRUE);
        prevButton.putClientProperty(MNEMONIC_PRIORITY, TRUE);
        this.settings = settings;
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc"); //NOIO18N
        actionMap.put("esc", UIUtils.action("esc", () -> { //NOIO18N
            exit(settings, 10);
        }));

        mnemonics(this);
        settings.onFontSizeRecomputed(this);
        accept(0);
    }

    public void accept(int newFontSize) {
        Font f = settings.uiFont();
        UIManager.put("controlFont", f); //NOIO18N
        UIManager.put("ComboBox.font", f); //NOIO18N
        UIManager.put("Label.font", f); //NOIO18N
        UIManager.put("CheckBox.font", f); //NOIO18N
        UIManager.put("RadioButton.font", f); //NOIO18N
        UIManager.put("Tree.font", f); //NOIO18N
        UIManager.put("Slider.font", f); //NOIO18N
        UIManager.put("TextArea.font", f); //NOIO18N
        setFont(f);
        nextButton.setFont(f);
        prevButton.setFont(f);
        cancelButton.setFont(f);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        getRootPane().setDefaultButton(nextButton);

    }

    void maybeExit(ActionEvent ae) {
        exit(settings, 10);
    }

    private static final Color SHADOW = new Color(150, 138, 120).brighter();

    static final class TreeIcon implements Icon {

        private static final int SZ = 9;
        private static final int BDS = 16;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            UIUtils.withAntialiasing(g, (Graphics g1) -> {
                g.setColor(UIManager.getColor("Tree.line")); //NOI18N
                int middle = (BDS / 2);
                int start = middle - (SZ / 2);
                g.drawRoundRect(start - 1, SZ - 2, SZ, SZ, 4, 4);
            });
        }

        @Override
        public int getIconWidth() {
            return BDS;
        }

        @Override
        public int getIconHeight() {
            return BDS;
        }
    }

    public static void run(ArgsParser.ArgsResult commandLineArgs) {

        // Try to initially display on the largest monitor available
        // unless --screen was passed on the command line
        ConfFileSettings settings = new ConfFileSettings(commandLineArgs);
        GraphicsConfiguration config = settings.preferredGraphicsConfiguration();
        if (config == null) {
            GraphicsDevice largest = DisplayAutoConfigurer.getLargestGraphicsDevice();
            if (largest != null) {
                config = largest.getDefaultConfiguration();
            }
        }

        // Force the look and feel.  The whole point here is that the
        // JDK defaults are not good, so we must be very presecriptive at
        // getting this UI - which lets the user decide what DOES look good
        // not to be similarly awful
        if (!(UIManager.getLookAndFeel() instanceof MetalLookAndFeel)) {
            try {
                UIManager.setLookAndFeel(new MetalLookAndFeel());
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(CalibrationGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        UIManager.put("TreeUI", BasicTreeUI.class.getName()); //NOI18N
        UIManager.put("PanelUI", GradientBackgroundPanelUI.class.getName()); //NOI18N
        UIManager.put("ToolTipUI", AAToolTipUI.class.getName()); //NOI18N
        UIManager.put("controlShadow", SHADOW); //NOI18N
        UIManager.put("Button.shadow", SHADOW); //NOI18N
        TreeIcon icon = new TreeIcon();
        UIManager.put("Tree.leafIcon", icon); //NOI18N
        UIManager.put("Tree.openIcon", icon); //NOI18N
        UIManager.put("Tree.closedIcon", icon); //NOI18N
        UIManager.put("Tree.line", SHADOW); //NOI18N
        UIManager.put("ToolTip.font", settings.uiFont()); //NOI18N
        UIManager.put("ToolTip.background", Color.WHITE); //NOI18N
        UIManager.put("ToolTip.foreground", Color.BLACK); //NOI18N
        final GraphicsConfiguration targetConfig = config;
        EventQueue.invokeLater(() -> {
            JFrame frm = targetConfig == null ? new JFrame(Localization.DIALOG_TITLE.toString()) :
                    new JFrame(Localization.DIALOG_TITLE.toString(), targetConfig);

            frm.addPropertyChangeListener("graphicsConfiguration", pce -> {
                if (pce.getNewValue() instanceof GraphicsConfiguration) {
                    GraphicsConfiguration newConfig = (GraphicsConfiguration) pce.getNewValue();
                    settings.preferredGraphicsConfiguration(newConfig);
                    UIUtils.sizeToDisplay(6, newConfig);
                }
            });

            frm.setAutoRequestFocus(true);
            frm.setAlwaysOnTop(true);
            frm.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frm.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    exit(settings, 10);
                }
            });
            frm.setContentPane(new CalibrationGUI(settings));
            frm.pack();

            UIUtils.sizeToDisplay(6, frm.getGraphicsConfiguration());
            frm.setVisible(true);
        });
    }

    private int index() {
        for (Component c : getComponents()) {
            int ix = panels.indexOf(c);
            if (ix > 0) {
                return ix;
            }
        }
        return 0;
    }

    private JPanel panel() {
        for (Component c : getComponents()) {
            if (panels.indexOf(c) >= 0) {
                return (JPanel) c;
            }
        }
        return null;
    }

    void done() {
        Path configFile = settings.confFilePath();
        if (configFile == null || !Files.exists(configFile)) {
            LocateConfigFilePanel pnl = new LocateConfigFilePanel(settings, path -> {
                if (path != null) {
                    settings.setConfFilePath(path);
                }
                nextButton.setEnabled(path != null);
                nextButton.setText(Localization.FINISH.toString());
            });
            panels.add(pnl);
            next();
            nextButton.setText(Localization.FINISH.toString());
            prevButton.setVisible(false);
            nextButton.setEnabled(false);
            return;
        }
        try {
            if (settings.args().isSet(ARG_PRETEND)) {
                System.out.println("Pretend mode - not overwriting " + configFile);
            } else {
                rewriteConfigFile(configFile);
            }
        } catch (IOException ex) {
            Logger.getLogger(CalibrationGUI.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(3);
        }
        System.exit(0);
    }

    static boolean showConfirmationDialog(ConfFileSettings settings, Localization message, Localization title) {
        return UIUtils.showConfirmationDialog(settings.uiFont(), settings.uiFont(), message, title, CalibrationGUI.class);
    }

    static void exit(ConfFileSettings settings, int code) {
        if (showConfirmationDialog(settings, CANCEL_DIALOG_MESSAGE, CANCEL_DIALOG_TITLE)) {
            System.exit(code);
        }
    }

    void rewriteConfigFile(Path path) throws IOException {
        // Parse the config file
        ConfFile cf = new ConfFile(path);
        // Get a map of all variables and their values
        Map<String, List<String>> vars = cf.parse();
        // Get the line that interests us, broken out into
        // individual items
        List<String> items = vars.get(NETBEANS_DEFAULT_OPTIONS);
        if (items == null) {
            // uh oh
            items = new ArrayList<>();
        }
        String hwsig = ConfFileSettings.hardwareSignature();
        vars.put("hw_sig", Collections.singletonList(hwsig));
        // Remove any items included in the default netbeans.conf which are of
        // no use on this operating system
        OS.get().removeIrrelevant(items);
        // Create a writer that will rewrite the file (retaining comments!)
        LineSwitchWriter lsw = new LineSwitchWriter(items, new DefaultOptionsReplacementChecker());
        // Apply our settings
        settings.contribute(lsw);
        // Put the result back in the map
        vars.put(NETBEANS_DEFAULT_OPTIONS, lsw.switches());
        // Back up the original file if that hasn't been done
        Path backupFile = path.getParent().resolve(path.getFileName() + ".backup");
        if (!Files.exists(backupFile)) {
            Files.copy(path, backupFile, StandardCopyOption.COPY_ATTRIBUTES);
            System.out.println("Backed up " + path + " to " + backupFile);
        }
        // Rewrite the file
        cf.rewrite(path, vars);
        System.out.println("Wrote " + path);
    }

    private void next() {
        next(null);
    }

    int lastPanelReached;

    private void next(ActionEvent ae) {
        int ix = Math.min(panels.size() - 1, index() + 1);
        if (ix < panels.size()) {
            lastPanelReached = Math.max(lastPanelReached, ix);
            JPanel pnl = panel();
            JPanel next = panels.get(ix);
            nextButton.setEnabled(lastPanelReached > ix || (ix == panels.size() - 2)
                    || (ix == panels.size() - 1));
            boolean hasFile = settings.confFilePath() != null;
            if (hasFile && ix == panels.size() - 1) {
                FINISH.set(nextButton);
            } else {
                NEXT.set(nextButton);
            }
            prevButton.setEnabled(ix > 0);
            if (pnl == next) {
                done();
                return;
            }
            remove(pnl);
            add(next, BorderLayout.CENTER);
            mnemonics(this);
            invalidate();
            revalidate();
            repaint();
            repack(settings.preferredGraphicsConfiguration());
            FocusTraversalPolicy pol = getTopLevelAncestor().getFocusTraversalPolicy();
            if (pol != null) {
                Component c = pol.getFirstComponent(getTopLevelAncestor());
                if (c != null) {
                    c.requestFocus();
                }
            }
        }
    }

    private void prev(ActionEvent ae) {
        int ix = Math.max(0, index() - 1);
        if (ix < panels.size()) {
            JPanel pnl = panel();
            JPanel next = panels.get(ix);
            NEXT.set(nextButton);
            nextButton.setEnabled(true);
            prevButton.setEnabled(ix > 0);
            if (pnl == next) {
                return;
            }
            remove(pnl);
            add(next, BorderLayout.CENTER);
            mnemonics(this);
            invalidate();
            revalidate();
            repaint();
            repack(settings.preferredGraphicsConfiguration());
            // Removing the focused component can result in random things
            // getting focus, usually the cancel button
            FocusTraversalPolicy pol = getTopLevelAncestor().getFocusTraversalPolicy();
            if (pol != null) {
                Component c = pol.getFirstComponent(getTopLevelAncestor());
                if (c != null) {
                    c.requestFocus();
                }
            }
        }
    }

    /**
     * Base class for panels that represent steps, with support for listening
     * for events that suggest going on to the next step; replacing the next
     * button as the default button temporarily, if the next button is disabled;
     * updating fonts when the base font size is changed (happens early in the
     * wizard as the user chooses the monitor to base settings off of). Contains
     * by default a title label and an inner pannel set up with GridBagLayout
     * which is where subclasses can place their content.
     */
    static abstract class Page extends JPanel {

        private final List<WeakReference<Runnable>> listeners = new ArrayList<>();
        protected final ConfFileSettings settings;
        private final Map<WeakReference<Component>, Supplier<Font>> fontSupplierForComponent
                = new IdentityHashMap<>();
        private final JLabel title;
        private JButton prevDefaultButton;
        private JButton defaultButtonReplacement;
        protected final JPanel inner = new JPanel(new GridBagLayout());
        // If this is not a field i will be garbage collected.
        private final IntConsumer onFontChange = this::computedFontChange;

        @SuppressWarnings({"OverridableMethodCallInConstructor"}) //NOI18N
        Page(ConfFileSettings settings, Localization title) {
            this.settings = settings;
            setOpaque(false);
            getAccessibleContext().setAccessibleName(title.toString());
            setLayout(new BorderLayout());
            this.title = title.label();
            this.title.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 2, 0, UIManager.getColor("controlShadow")), //NOI18N
                            BorderFactory.createEmptyBorder(0, 0, 12, 0)));
            add(this.title, BorderLayout.NORTH);
            add(this.inner, BorderLayout.CENTER);
            this.inner.setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            configureFont(this.inner, settings::uiFont);
            configureFont(this.title, settings::largeFont);
            settings.onFontSizeRecomputed(onFontChange);
        }

        /**
         * Allow the passed button to become the window's default button while
         * this page is on screen, if (and only if) the default button (usually
         * Next) is disabled - first pass through the pages without backing up.
         *
         * @param with A button
         * @return this
         */
        protected final Page replaceDefaultButtonIfDisabled(JButton with) {
            if (defaultButtonReplacement != null) {
                throw new IllegalArgumentException("defaultButtonReplacement already set to " + defaultButtonReplacement); //NOI18N
            }
            defaultButtonReplacement = with;
            return this;
        }

        /**
         * Configure components to automatically have its font updated if the
         * graphics configuration derived font size changes (happens on first
         * few pages when the users picks a monitor).
         *
         * @param supplier A supplier of a font, e.g. settings::uiFont
         * @param components Some components
         * @return this
         */
        protected final Page configureFonts(Supplier<Font> supplier, Component... components) {
            for (Component c : components) {
                configureFont(c, supplier);
            }
            return this;
        }

        /**
         * Configure a single component to automatically have its font updated
         * if the graphics configuration derived font size changes (happens on
         * first few pages when the users picks a monitor).
         *
         * @param supplier A supplier of a font, e.g. settings::uiFont
         * @param component A component
         * @return this
         */
        protected final Page configureFont(Component component, Supplier<Font> supplier) {
            component.setFont(supplier.get());
            fontSupplierForComponent.put(new WeakReference<>(component), supplier);
            return this;
        }

        /**
         * Convenience method which can be used as an action listener, ala
         * <code>someComp.addActionListener(this::fireAction)</code> and trigger
         * a settings update and fire.
         *
         * @param ae An action event
         */
        protected final void fireAction(ActionEvent ae) {
            fire();
        }

        @Override
        public void addNotify() {
            super.addNotify();
            if (defaultButtonReplacement != null) {
                JRootPane root = getRootPane();
                if (root != null) {
                    prevDefaultButton = root.getDefaultButton();
                    if (prevDefaultButton == null || !prevDefaultButton.isEnabled()) {
                        root.setDefaultButton(defaultButtonReplacement);
                    }
                }
            }
        }

        @Override
        public void removeNotify() {
            if (defaultButtonReplacement != null) {
                JRootPane root = getRootPane();
                if (root != null && prevDefaultButton != null) {
                    root.setDefaultButton(prevDefaultButton);
                }
            }
            super.removeNotify();
        }

        /**
         * Called when the target user font size changes - this happens when we
         * recompute the display settings based on the monitor the window is on,
         * and need to update previews, and any components with fonts set from
         * the ui fonts available from the config (these can change if they are
         * drastically oversized).
         *
         * @param newSize The new size for --fontsize in the generated config
         */
        protected void onComputedFontChange(int newSize) {

        }

        void computedFontChange(int newSize) {
            boolean changed = false;
            for (Map.Entry<WeakReference<Component>, Supplier<Font>> e : fontSupplierForComponent.entrySet()) {
                Component c = e.getKey().get();
                if (c != null) {
                    c.setFont(e.getValue().get());
                    changed = true;
                }
            }
            onComputedFontChange(newSize);
            if (changed && isDisplayable()) {
                invalidate();
                revalidate();
                repaint();
            }
        }

        /**
         * Write any changed settings represented in ui components to the
         * settings - called at the start of fire().
         */
        protected void updateSettings() {

        }

        /**
         * Update settings and fire to listeners (usually invoking the next
         * action) to indicate this page has been completed or can be navigated
         * away from now.
         *
         */
        protected final void fire() {
            updateSettings();
            for (Iterator<WeakReference<Runnable>> it = listeners.iterator(); it.hasNext();) {
                WeakReference<Runnable> w = it.next();
                Runnable r = w.get();
                if (r == null) {
                    it.remove();
                    if (w instanceof TrackableReference<?>) {
                        ((TrackableReference<?>) ((TrackableReference<?>) w)).creation.printStackTrace();;
                    }
                } else {
                    r.run();
                }
            }
        }

        /**
         * Listen for calls to fire().
         *
         * @param run A runnable-listener
         */
        public final void listen(Runnable run) {
//            listeners.add(new TrackableReference<>(run));
            listeners.add(new WeakReference<>(run));
        }

    }

    /**
     * For debugging disappearing weakly referenced lambdas.
     *
     * @param <T> A type
     */
    private static final class TrackableReference<T> extends WeakReference<T> {

        private final Exception creation;

        public TrackableReference(T referent) {
            super(referent);
            creation = new Exception(referent.toString());
        }

    }

    private static final class ChooseMonitorPanel extends Page {

        private final JButton doneButton = DONE.button();
        private final JLabel text = CHOOSE_MONITOR_TEXT.label();

        @SuppressWarnings("LeakingThisInConstructor") //NOI18N
        ChooseMonitorPanel(ConfFileSettings settings) {
            super(settings, CHOOSE_MONITOR_TITLE);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(12, 0, 12, 0);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            inner.add(text, gbc);
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridy++;
            gbc.insets = new Insets(64, 0, 0, 0);
            inner.add(doneButton, gbc);
            replaceDefaultButtonIfDisabled(doneButton);
            doneButton.addActionListener(this::fireAction);
            configureFont(text, settings::largeFont);
            configureFonts(settings::uiFont, doneButton);
        }

        @Override
        protected void updateSettings() {
            settings.recomputeFontSize(getGraphicsConfiguration().getDevice(), false, true);
            UIUtils.sizeToDisplay(6, settings.preferredGraphicsConfiguration());
        }

    }

    private static final class MonitorTypePanel extends Page implements ActionListener, Runnable {

        private final JLabel text = MONITOR_SIZE_QUESTION.label();
        private final JRadioButton largeDesktop = LARGE_DESKTOP.radioButton();
        private final JRadioButton smallDesktop = SMALL_DESKTOP.radioButton();
        private final JRadioButton laptop = LAPTOP_SCREEN.radioButton();
        private final JCheckBox currentIsMaximum = CURRENT_IS_MAX_RESOLUTION_CHECKBOX.checkbox();
        private final JButton use = USE_THESE_SETTINGS.button();
        private static final String CMD_SMALL = "small"; //NOI18N
        private static final String CMD_LARGE = "large"; //NOI18N
        private static final String CMD_LAPTOP = "laptop"; //NOI18N
        private final ButtonGroup grp = new ButtonGroup();
        private final JPanel innerInner = new JPanel(new GridBagLayout());

        @SuppressWarnings("LeakingThisInConstructor") //NOI18N
        MonitorTypePanel(ConfFileSettings settings) {
            super(settings, MONITOR_SIZE_TITLE);
            settings.listen(this);
            innerInner.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            gbc.weighty = 1;
            // Use an inner panel to have a complex layout, but
            // allow the default behavior of GridBagLayout to float it
            // in the middle of the page
            inner.add(innerInner, gbc);
            gbc.weighty = 0;
            gbc.weightx = 0;
            gbc.gridx = gbc.gridy = 0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            innerInner.add(text, gbc);
            gbc.gridy++;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.VERTICAL;
            gbc.insets = new Insets(64, 64, 0, 0);
            smallDesktop.setActionCommand(CMD_SMALL);
            largeDesktop.setActionCommand(CMD_LARGE);
            laptop.setActionCommand(CMD_LAPTOP);
            for (JRadioButton button : new JRadioButton[]{largeDesktop, smallDesktop, laptop}) {
                innerInner.add(button, gbc);
                gbc.gridy++;
                gbc.insets = new Insets(12, 64, 0, 0);
                button.addActionListener(this);
                grp.add(button);
            }
            gbc.insets = new Insets(64, 64, 0, 0);
            innerInner.add(currentIsMaximum, gbc);
            gbc.insets = new Insets(64, 0, 0, 0);

            gbc.gridy++;
            gbc.anchor = GridBagConstraints.BELOW_BASELINE;
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridx = 0;
            gbc.weighty = 1;
            innerInner.add(use, gbc);
            replaceDefaultButtonIfDisabled(use);
            configureFont(text, settings::largeFont);
            configureFonts(settings::uiFont, largeDesktop, smallDesktop, laptop, use);
            configureFont(currentIsMaximum, settings::smallFont);
            use.setEnabled(settings.displayKind() != null);
            use.addActionListener(super::fireAction);
            run();
        }

        @Override
        public void addNotify() {
            super.addNotify();
            if (DisplayAutoConfigurer.LargerGraphicsModeExists(this)) {
                currentIsMaximum.setVisible(true);
            } else {
                currentIsMaximum.setVisible(false);
            }
        }

        private DisplayKind kind() {
            if (laptop.isSelected()) {
                return DisplayKind.LAPTOP;
            } else if (largeDesktop.isSelected()) {
                return DisplayKind.LARGE_DESKTOP;
            } else if (smallDesktop.isSelected()) {
                return DisplayKind.SMALL_DESKTOP;
            }
            return null;
        }

        @Override
        protected void updateSettings() {
            settings.displayKind(kind());
            use.setEnabled(grp.getSelection() != null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            use.setEnabled(kind() != null);
        }

        @Override
        public void run() {
            DisplayKind k = settings.displayKind();
            if (k != null) {
                switch (k) {
                    case LAPTOP:
                        laptop.setSelected(true);
                        break;
                    case LARGE_DESKTOP:
                        largeDesktop.setSelected(true);
                        break;
                    case SMALL_DESKTOP:
                        smallDesktop.setSelected(true);
                        break;
                    default:
                        grp.clearSelection();
                }
            }
        }
    }

    private static final class MonitorKindPanel extends Page implements ActionListener, Runnable {

        private final MarkableAAButton yes = YES.button();
        private final MarkableAAButton no = NO.button();
        private final JLabel text = MONITOR_TYPE_QUESTION.label();

        @SuppressWarnings("LeakingThisInConstructor") //NOI18N
        MonitorKindPanel(ConfFileSettings settings) {
            super(settings, MONITOR_TYPE_TITLE);
            configureFont(yes, settings::uiFont);
            configureFont(no, settings::uiFont);
            configureFont(text, settings::largeFont);
            replaceDefaultButtonIfDisabled(yes);
            yes.addActionListener(this);
            no.addActionListener(this);
            settings.listen(this);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = gbc.gridy = 0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            inner.add(text, gbc);
            gbc.insets = new Insets(64, 0, 0, 0);
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;

            JPanel innerInner = new JPanel(new GridBagLayout());
            innerInner.setOpaque(false);
            inner.add(innerInner, gbc);
            gbc.insets = new Insets(0, 0, 0, 12);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = 0;
            innerInner.add(yes, gbc);
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.gridx++;
            innerInner.add(no, gbc);
            run();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == yes) {
                settings.displayType(ConfFileSettings.DisplayType.LCD);
            } else {
                settings.displayType(ConfFileSettings.DisplayType.CRT);
            }
            settings.recomputeFontSize(getGraphicsConfiguration().getDevice(), true);
            fire();
        }

        @Override
        public void run() {
            if (null == settings.displayType()) {
                yes.unmark();
                no.unmark();
            } else {
                switch (settings.displayType()) {
                    case CRT:
                        no.mark();
                        yes.unmark();
                        break;
                    case LCD:
                        yes.mark();
                        no.unmark();
                        break;
                    default:
                        yes.unmark();
                        no.unmark();
                        break;
                }
            }
        }
    }

    private static final class SelectAntialiasModePanel extends Page {

        private final Runnable fire = this::fire;

        @SuppressWarnings("OverridableMethodCallInConstructor") //NOI18N
        SelectAntialiasModePanel(ConfFileSettings settings) {
            super(settings, SELECT_ANTIALIAS_MODE_TITLE);
            getAccessibleContext().setAccessibleName(SELECT_ANTIALIAS_MODE_TITLE.toString());
            FontSizeOrAntialiasingDemoPanel[] pnls = createDemoPanels(settings);
            inner.setLayout(new GridLayout(2,
                    pnls.length % 2 == 0 ? pnls.length / 2 : (pnls.length / 2) + 1));
            for (int i = 0; i < pnls.length; i++) {
                inner.add(pnls[i]);
                pnls[i].listen(fire);
                configureFont(pnls[i].button, settings::labelFont);
                configureFont(pnls[i].aaTree, settings::getFont);
            }
            configureFont(inner, settings::uiFont);
        }

        @Override
        public void addNotify() {
            super.addNotify();
            for (Component c : inner.getComponents()) {
                if (c instanceof FontSizeOrAntialiasingDemoPanel) {
                    FontSizeOrAntialiasingDemoPanel p = (FontSizeOrAntialiasingDemoPanel) c;
                    if (!p.button.isEnabled() && p.button.isMarked()) {
                        List<HintEntry> avail = settings.availableHints();
                        if (!avail.isEmpty()) {
                            settings.selectedHint(avail.get(avail.size() - 1));
                        } else {
                            settings.selectedHint(HintEntry.HINT_DEFAULT);
                        }
                        break;
                    }
                }
            }
        }

        @Override
        protected void onComputedFontChange(int newSize) {
            for (Component c : inner.getComponents()) {
                if (c instanceof FontSizeOrAntialiasingDemoPanel) {
                    FontSizeOrAntialiasingDemoPanel p = (FontSizeOrAntialiasingDemoPanel) c;
                    p.baseFontSizeChanged(newSize);
                }
            }
        }
    }

    private static FontSizeOrAntialiasingDemoPanel[] createDemoPanels(ConfFileSettings settings) {
        List<HintEntry> hints = settings.availableHints();
        FontSizeOrAntialiasingDemoPanel[] result = new FontSizeOrAntialiasingDemoPanel[hints.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = new FontSizeOrAntialiasingDemoPanel(settings, hints.get(i), -1);
        }
        return result;
    }

    private static final class FontSizeOrAntialiasingDemoPanel extends JPanel implements ActionListener, Runnable {

        private final HintEntry hints;
        private final ConfFileSettings settings;
        private final AAFontSizeAndHintsTree aaTree;
        private int fontSize;
        private final MarkableAAButton button;
        private Runnable onChange;
        private final IntConsumer changed = this::baseFontSizeChanged;

        @SuppressWarnings("LeakingThisInConstructor") //NOI18N
        FontSizeOrAntialiasingDemoPanel(ConfFileSettings settings, HintEntry hints, int fontSize) {
            this.fontSize = fontSize;
            this.settings = settings;
            aaTree = new AAFontSizeAndHintsTree(settings, hints, fontSize);
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            setName(hints.toString());
            this.hints = hints;
            button = fontSize > 0 ? Localization.POINT_SIZE.button(fontSize)
                    : Localization.SELECT_STYLE.button(hints.displayName());
            if (fontSize <= 0) {
                if (hints.description() == null) {
                    button.setToolTipText(hints.name);
                } else {
                    button.setToolTipText(hints.description());
                }
            }
            if (fontSize > 0) {
                if (fontSize == settings.size()) {
                    button.mark();
                }
            } else {
                button.setEnabled(settings.availableHints().contains(hints));
                if (hints.equals(settings.selectedHint())) {
                    button.mark();
                }
            }
            Font f = settings.getFont();
            if (fontSize > 0) {
                f = f.deriveFont((float) fontSize);
            }
            setFont(f);
            aaTree.setFont(f);
            button.setFont(settings.labelFont());
            add(aaTree, BorderLayout.CENTER);
            button.addActionListener(this);
            aaTree.setToolTipText(button.getToolTipText());
            add(button, BorderLayout.SOUTH);
            settings.listen(this);
            button.putClientProperty("notop", Boolean.TRUE); //NOI18N
            // again, avoid member reference being gc'd
            settings.onFontSizeRecomputed(changed);
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            button.setCursor(cursor);
            aaTree.setCursor(cursor);
            aaTree.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!e.isPopupTrigger() && e.getClickCount() == 1) {
                        button.doClick();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    button.getModel().setRollover(true);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    Point p = SwingUtilities.convertPoint(aaTree, e.getPoint(), button);
                    if (!button.contains(p)) {
                        button.getModel().setRollover(false);
                    }
                }
            });
        }

        void changeExplicitFontSize(int newSize) {
            button.setFont(settings.labelFont());
            if (fontSize <= 0) {
                if (newSize == settings.size()) {
                    button.mark();
                } else {
                    button.unmark();
                }
                Font f = settings.getFont();
                aaTree.setFont(f);
                button.setEnabled(settings.availableHints().contains(hints));
            } else {
                Font f = settings.getFont();
                f = f.deriveFont((float) newSize);
                fontSize = newSize;
                aaTree.setFont(f);
                if (hints.equals(settings.selectedHint())) {
                    button.mark();
                } else {
                    button.unmark();
                }

            }
            invalidate();
            revalidate();
            repaint();
            aaTree.repaint();
        }

        void baseFontSizeChanged(int newSize) {
            Font f = settings.getFont();
            if (fontSize > 0) {
                f = f.deriveFont((float) fontSize);
            }
            setFont(f);
        }

        @Override
        public void requestFocus() {
            EventQueue.invokeLater(button::requestFocus);
        }

        void listen(Runnable r) {
            onChange = r;
        }

        @Override
        public void paint(Graphics g) {
            HintEntry h = fontSize <= 0 ? hints : settings.selectedHint();
            h.apply(g, () -> {
                super.paint(g);
            });
        }

        @Override
        public void run() {
            if (fontSize <= 0) {
                button.setEnabled(settings.availableHints().contains(hints));
            }
            setFont(settings.getFont());
            if (fontSize <= 0) {
                aaTree.setFont(settings.getFont());
            } else {
                aaTree.setFont(settings.getFont().deriveFont((float) fontSize));
            }
            if (fontSize > 0) {
                if (fontSize == settings.size()) {
                    button.mark();
                } else {
                    button.unmark();
                }
            } else {
                if (hints.equals(settings.selectedHint())) {
                    button.mark();
                } else {
                    button.unmark();;
                }
            }
            invalidate();
            revalidate();
            repaint();
            aaTree.repaint();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (fontSize > 0) {
                Integer i = (Integer) getClientProperty(CLIENT_PROPERTY_SIZE);
                settings.size(i);
            } else {
                settings.selectedHint(hints);
            }
            if (onChange != null) {
                onChange.run();
            }
        }
    }

    static final class AAFontSizeAndHintsTree extends JTree implements Runnable, IntConsumer {

        private final ConfFileSettings settings;
        private final HintEntry hints;
        private int fontSize;

        AAFontSizeAndHintsTree(ConfFileSettings settings, int fontSize) {
            this(settings, settings.selectedHint(), fontSize);
        }

        @SuppressWarnings("LeakingThisInConstructor") //NOI18N
        AAFontSizeAndHintsTree(ConfFileSettings settings, HintEntry hints, int fontSize) {
            this.fontSize = fontSize;
            // Keep this out of the focus order
            setFocusable(false);
            setModel(exampleTreeModel());
            setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(2, 2, 0, 2, UIManager.getColor("controlShadow")), //NOI18N
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ));
            this.settings = settings;
            this.hints = hints;
            if (fontSize <= 0) {
                setEnabled(settings.availableHints().contains(hints));
                setFont(settings.getFont());
                settings.onFontSizeRecomputed(this);
            } else {
                setFont(settings.getFont().deriveFont(fontSize));
            }
            setCellRenderer(new AAFontSizeAndHintsCellRenderer(settings, hints, fontSize));
        }

        void baseFontSizeChanged(int newSize) {
            setFont(settings.getFont().deriveFont(newSize));
        }

        @Override
        public void accept(int v) {
            baseFontSizeChanged(v);
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D gg = (Graphics2D) g;
            if (fontSize <= 0) {
                gg.setFont(settings.getFont());
            } else {
                gg.setFont(settings.getFont().deriveFont(fontSize));
            }
            HintEntry h = fontSize > 0 ? settings.selectedHint() : hints;
            h.apply(g, () -> {
                super.paint(g);
            });
        }

        @Override
        public void run() {
            if (fontSize <= 0) {
                setEnabled(settings.availableHints().contains(hints));
            }
            if (isDisplayable()) {
                doLayout();
            }
        }

        static TreeModel exampleTreeModel;

        static final TreeModel exampleTreeModel() {
            if (exampleTreeModel != null) {
                return exampleTreeModel;
            }
            Iterator<String> words = Arrays.asList(SAMPLE_TEXT.toString().split("\\s")).iterator(); //NOI18N
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(words.next());
            while (words.hasNext()) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(words.next());
                root.add(child);
            }
            DefaultTreeModel mdl = new DefaultTreeModel(root);
            return exampleTreeModel = mdl;
        }
    }

    private static final class FontSizePanel extends Page implements ActionListener, Runnable {

        private final Runnable fwd = this::forwardChange;

        @SuppressWarnings("LeakingThisInConstructor") //NOI18N
        FontSizePanel(ConfFileSettings settings) {
            super(settings, FONT_SIZE_TITLE);
            int sz = settings.size();
            int total = updatePanels(sz);
            inner.setLayout(new GridLayout(2, total % 2 == 0 ? total / 2 : (total / 2) + 1));
            inner.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            settings.listen(this);
            add(inner);
        }

        @Override
        protected void onComputedFontChange(int newSize) {
            updatePanels(newSize);
            invalidate();
            revalidate();
            repaint();
        }

        int updatePanels(int sz) {
            int total = 0;
            if (inner.getComponentCount() > 0) {
                inner.removeAll();
            }
            for (int i = 4; i >= 1; i--) {
                int targetSize = sz - i;
                if (targetSize > 4) {
                    FontSizeOrAntialiasingDemoPanel p = new FontSizeOrAntialiasingDemoPanel(settings, settings.selectedHint(), targetSize);
                    p.putClientProperty(CLIENT_PROPERTY_SIZE, targetSize);
                    p.listen(fwd);
                    inner.add(p);
                    total++;
                }
            }
            for (int i = 0; i < 4; i++) {
                int targetSize = sz + i;
                if (targetSize > 4 && targetSize < 48) {
                    FontSizeOrAntialiasingDemoPanel p = new FontSizeOrAntialiasingDemoPanel(settings, settings.selectedHint(), targetSize);
                    p.putClientProperty(CLIENT_PROPERTY_SIZE, targetSize);
                    p.listen(fwd);
                    inner.add(p);
                    total++;
                }
            }
            run();
            return total;
        }

        void forwardChange() {
            fire();
        }

        @Override
        protected void paintComponent(Graphics g) {
            settings.selectedHint().apply(g, () -> super.paintComponent(g));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Integer i = (Integer) ((JComponent) e.getSource()).getClientProperty(CLIENT_PROPERTY_SIZE);
            settings.size(i);
        }

        @Override
        public void run() {
            Font f = settings.getFont();
            for (Component c : inner.getComponents()) {
                if (c instanceof FontSizeOrAntialiasingDemoPanel) {
                    Integer i = (Integer) ((FontSizeOrAntialiasingDemoPanel) c).getClientProperty(CLIENT_PROPERTY_SIZE);
                    c.setFont(f.deriveFont((float) i));
                    if (c.isDisplayable()) {
                        c.doLayout();
                    }
                }
            }
            if (isDisplayable()) {
                doLayout();
            }
            invalidate();
            revalidate();
            repaint();
        }
    }

    private static final class FontSelectionPanel extends Page implements ActionListener {

        private String preferred = "Sans Serif"; //NOI18N
        private final List<MarkableAAButton> buttons = new ArrayList<>();
        private final MarkableAAButton decreaseFontButton = DECREASE_FONT_SIZE.button();
        private final MarkableAAButton increaseFontButton = INCREASE_FONT_SIZE.button();

        @SuppressWarnings("LeakingThisInConstructor") //NOI18N
        public FontSelectionPanel(ConfFileSettings settings) {
            super(settings, FONTS_TITLE);
            Set<String> toUse = settings.availableUIFonts();

            increaseFontButton.setFont(settings.labelFont());
            decreaseFontButton.setFont(settings.labelFont());
            JPanel innerInner = new JPanel();
            innerInner.setOpaque(false);
            innerInner.setLayout(new VerticallyJustifiedLayout(7, 6));
            innerInner.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            JPanel bottom = new JPanel();
            bottom.setOpaque(false);
            bottom.setLayout(new GridBagLayout());
            bottom.setBorder(BorderFactory.createEmptyBorder(5, 5, 3, 5));
            increaseFontButton.putClientProperty(CLIENT_PROP_NO_MNEMONIC, TRUE);
            decreaseFontButton.putClientProperty(CLIENT_PROP_NO_MNEMONIC, TRUE);

            Action left = UIUtils.action("inc", (Runnable) increaseFontButton::doClick); //NOI18N
            Action right = UIUtils.action("dec", (Runnable) decreaseFontButton::doClick); //NOI18N

            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "inc"); //NOI18N
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "dec"); //NOI18N
            getActionMap().put("inc", right);
            getActionMap().put("dec", left);

            bottom.add(decreaseFontButton);
            bottom.add(increaseFontButton);

            // Allow arrow keys to rotate between font buttons
            Action down = UIUtils.action("down", e -> { //NOI18N
                int index = buttons.indexOf(e.getSource());
                if (index == buttons.size() - 1) {
                    index = 0;
                } else {
                    index++;
                }
                buttons.get(index).requestFocus();
            });

            Action up = UIUtils.action("up", e -> { //NOI18N
                int index = buttons.indexOf(e.getSource());
                if (index > 0) {
                    index--;
                } else {
                    index = buttons.size() - 1;
                }
                buttons.get(index).requestFocus();
            });
            for (String s : toUse) {
                if (preferred == null) {
                    preferred = s;
                }
                MarkableAAButton btn = FONT_BUTTON_TEXT.button(s);
                btn.setHorizontalAlignment(SwingConstants.LEADING);
                innerInner.add(btn);
                btn.setName(s);
                btn.addActionListener(this);
                Font f = new Font(s, Font.PLAIN, settings.size());
                if (settings.font().equals(s)) {
                    btn.mark();
                } else {
                    btn.unmark();
                }
                btn.setFont(f);
                buttons.add(btn);
                btn.getActionMap().put("up", up); //NOI18N
                btn.getActionMap().put("down", down); //NOI18N
                btn.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down"); //NOI18N
                btn.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up"); //NOI18N
            }
            inner.add(innerInner);
            add(bottom, BorderLayout.SOUTH);
            decreaseFontButton.addActionListener(UIUtils.action(this::decrease));
            increaseFontButton.addActionListener(UIUtils.action(this::increase));
            configureFonts(settings::labelFont, increaseFontButton, decreaseFontButton);
        }

        @Override
        protected void onComputedFontChange(int size) {
            String name = settings.font();
            for (MarkableAAButton b : buttons) {
                Font f = b.getFont();
                if (f.getName().equals(name)) {
                    b.mark();
                } else {
                    b.unmark();
                }
                f = f.deriveFont((float) size);
                b.setFont(f);
            }
            evenOutButtonSizes();
            invalidate();
            revalidate();
            repaint();
        }

        private void evenOutButtonSizes() {
            if (isDisplayable()) {
                Dimension a = decreaseFontButton.getPreferredSize();
                Dimension b = increaseFontButton.getPreferredSize();
                a.width = Math.max(a.width, b.width);
                a.height = Math.max(a.height, b.height);
                decreaseFontButton.setPreferredSize(a);
                increaseFontButton.setPreferredSize(a);
            }
        }

        @Override
        public void addNotify() {
            super.addNotify();
            // Cheaper than a horizontal layout
            evenOutButtonSizes();
        }

        public void increase() {
            float size = settings.size();
            size = Math.min(48, size + 1);
            settings.size((int) size);
            for (JButton btn : buttons) {
                Font f = btn.getFont();
                f = f.deriveFont(size);
                btn.setFont(f);
                btn.invalidate();
                btn.revalidate();
                btn.repaint();
            }
            repack(settings.preferredGraphicsConfiguration());
        }

        public void decrease() {
            float size = settings.size();
            size = Math.max(4, size - 1);
            settings.size((int) size);
            for (JButton btn : buttons) {
                Font f = btn.getFont();
                f = f.deriveFont(size);
                btn.setFont(f);
                btn.invalidate();
                btn.revalidate();
                btn.repaint();
            }
            repack(settings.preferredGraphicsConfiguration());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton btn = (JButton) e.getSource();
            preferred = btn.getName();
            settings.font(preferred);
            for (MarkableAAButton b : buttons) {
                if (b.getFont().getName().equals(preferred)) {
                    b.mark();
                } else {
                    b.unmark();
                }
            }
            fire();
        }
    }

    private static final class AAFontSizeAndHintsCellRenderer extends DefaultTreeCellRenderer {

        private final ConfFileSettings settings;
        private final HintEntry hints;
        private final int fontSize;

        public AAFontSizeAndHintsCellRenderer(ConfFileSettings settings, HintEntry hints, int fontSize) {
            this.settings = settings;
            this.hints = hints;
            this.fontSize = fontSize;
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component result = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (fontSize <= 0) {
                result.setFont(settings.getFont());
            } else {
                result.setFont(settings.getFont().deriveFont((float) fontSize));
            }
            return result;
        }

        @Override
        public void paint(Graphics g) {
            if (fontSize < 0) {
                g.setFont(settings.getFont());
            } else {
                g.setFont(settings.getFont().deriveFont((float) fontSize));
            }
            HintEntry h = fontSize > 0 ? settings.selectedHint() : hints;
            h.apply(g, () -> {
                super.paint(g);
            });
        }
    }

    private static final class MemorySelectionPanel extends Page {

        private final JSlider xmxSlider = new AASlider();
        private final JSlider xmsSlider = new AASlider();
        private final MarkableAAButton accept = USE_THESE_SETTINGS.button();
        private final JTextArea area = MEMORY_INSTRUCTIONS.textArea();
        private final JPanel innerInner = new JPanel();
        private final JLabel xmxLabel = MAX_MEMORY_SIZE.label();
        private final JLabel xmxValue = new AALabel("0000"); // NOI18N
        private final JLabel xmsValue = new AALabel("0000"); // NOI18N
        private final JLabel xmsLabel = MIN_MEMORY_SIZE.label();
        private final JLabel memLabel = TOTAL_MEMORY.label();
        private final JLabel memAmount = new AALabel();

        @SuppressWarnings("LeakingThisInConstructor") //NOI18N
        MemorySelectionPanel(ConfFileSettings settings) {
            super(settings, MEMORY_PANEL_TITLE);
            xmxSlider.setPaintTicks(true);
            xmsSlider.setPaintTicks(true);
            xmxSlider.setToolTipText(xmxLabel.getToolTipText());
            xmsSlider.setToolTipText(xmsLabel.getToolTipText());

            MemorySettings ms = settings.memory();
            area.setEditable(false);
            area.setBorder(BorderFactory.createEmptyBorder(12, 0, 24, 0));
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            configureFont(area, settings::smallFont);
            inner.setLayout(new BorderLayout());
            inner.add(area, BorderLayout.NORTH);
            innerInner.setOpaque(false);
            add(innerInner, BorderLayout.CENTER);

            xmxSlider.setOpaque(false);
            xmxSlider.setPaintTicks(true);
            xmxSlider.setPaintLabels(true);
            xmxSlider.setLabelTable(ms.dictionary(settings));

            xmxLabel.setLabelFor(xmxSlider);

            xmsSlider.setOpaque(false);
            xmsSlider.setPaintTicks(true);
            xmsSlider.setPaintLabels(true);
            xmsSlider.setLabelTable(ms.dictionary(settings));

            ms.models((BoundedRangeModel maxModel, Supplier<String> maxVal, BoundedRangeModel minModel, Supplier<String> minVal) -> {
                xmxSlider.setModel(maxModel);
                xmsSlider.setModel(minModel);
                maxModel.addChangeListener(ce -> {
                    xmxValue.setText(maxVal.get());
                });
                minModel.addChangeListener(ce -> {
                    xmsValue.setText(minVal.get());
                });
                xmxValue.setText(maxVal.get());
                xmsValue.setText(minVal.get());
            });

            xmsLabel.setLabelFor(xmsSlider);
            xmsLabel.setLabelFor(xmsSlider);

            xmsLabel.setHorizontalAlignment(SwingConstants.LEADING);
            xmxLabel.setHorizontalAlignment(SwingConstants.LEADING);

            xmsValue.setVerticalAlignment(SwingConstants.TOP);
            xmxValue.setVerticalAlignment(SwingConstants.TOP);
            xmsLabel.setVerticalAlignment(SwingConstants.TOP);
            xmxLabel.setVerticalAlignment(SwingConstants.TOP);

            xmsValue.setHorizontalAlignment(SwingConstants.TRAILING);
            xmxValue.setHorizontalAlignment(SwingConstants.TRAILING);

            innerInner.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            int baseIpad = 32;

            gbc.insets = new Insets(12, 12, 12, 12);
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.weighty = 0;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.ipadx = baseIpad;
            gbc.ipady = baseIpad;

            gbc.anchor = GridBagConstraints.LINE_START;

            memLabel.setHorizontalAlignment(SwingConstants.LEADING);
            innerInner.add(memLabel, gbc);
            gbc.gridwidth = 2;
            memAmount.setText(ms.memorySize());
            gbc.fill = GridBagConstraints.HORIZONTAL;
            memLabel.setLabelFor(memAmount);
            memAmount.setHorizontalAlignment(SwingConstants.LEADING);
            gbc.gridx++;
            innerInner.add(memAmount, gbc);

            gbc.gridwidth = 1;
            gbc.gridy++;
            gbc.gridx = 0;

            innerInner.add(xmxLabel, gbc);
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.ipadx = 512;
            gbc.gridx++;
            gbc.weightx = 0.66;
            innerInner.add(xmxSlider, gbc);
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.weightx = 0;
            gbc.ipadx = 0;
            gbc.gridx++;
            innerInner.add(xmxValue, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.anchor = GridBagConstraints.LINE_START;

            innerInner.add(xmsLabel, gbc);
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.ipadx = 300;
            gbc.gridx++;
            gbc.weightx = 0.66;
            innerInner.add(xmsSlider, gbc);
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.ipadx = 0;
            gbc.gridx++;
            innerInner.add(xmsValue, gbc);

            gbc.gridy++;
            JPanel bp = new JPanel(new GridBagLayout());
            bp.setOpaque(false);

            bp.add(accept, new GridBagConstraints());
            gbc.gridx = 0;
            gbc.gridwidth = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0;
            innerInner.add(bp, gbc);
            accept.addActionListener(this::fireAction);
            configureFonts(settings::labelFont, xmsLabel, xmxLabel, memLabel);
            configureFonts(settings::uiFont, xmsValue, xmxValue, accept, memAmount);
            replaceDefaultButtonIfDisabled(accept);
        }
    }

    private static final class TweaksPanel extends Page implements ActionListener, DocumentListener {

        private final JPanel namePanel = new JPanel(new FlowLayout(SwingConstants.HORIZONTAL, 5, 5));
        private final AATextField nameField = new AATextField();
        private final JPanel innerInner = new JPanel(new VerticallyJustifiedLayout(12, 6));
        private final JLabel nameLabel = YOUR_NAME.label();
        private final JLabel nameDescription = WHERE_NAME_IS_USED.label();
        private final JCheckBox useUtf8;

        @SuppressWarnings({"LeakingThisInConstructor", "OverridableMethodCallInConstructor"}) //NOI18N
        TweaksPanel(ConfFileSettings settings) {
            super(settings, TWEAKS_TITLE);
            useUtf8 = USE_UTF_8.checkbox(settings.charset().name());

            configureFonts(settings::uiFont, nameField, nameLabel, nameDescription, useUtf8);
            configureFont(nameLabel, () -> settings.uiFont().deriveFont(Font.BOLD));
            configureFont(nameDescription, () -> settings.uiFont().deriveFont(settings.uiFont().getSize() - 4));

            namePanel.setOpaque(false);

            GridBagConstraints gbc = new GridBagConstraints();
            inner.add(innerInner, gbc);
            inner.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

            nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
            nameField.setColumns(60);
            nameField.setText(settings.userName());
            nameField.getDocument().addDocumentListener(this);
            nameLabel.setLabelFor(nameField);
            namePanel.add(nameLabel);
            namePanel.add(nameField);
            namePanel.add(nameDescription);
            innerInner.add(namePanel);

            for (TweakEntry entry : availableTweaks()) {
                JCheckBox box = entry.info().checkbox();
                box.setFont(settings.uiFont());
                box.setSelected(settings.hasTweak(entry));
                box.putClientProperty("tweak", entry); //NOI18N
                box.addActionListener(this);
                innerInner.add(box);
                configureFonts(settings::uiFont, box);
            }

            nameField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    nameField.selectAll();
                }
            });
            innerInner.setOpaque(false);

            Charset charset = settings.charset();
            if (!UTF_8.equals(charset)) {
                innerInner.add(useUtf8);
                useUtf8.addActionListener(ae -> {
                    settings.charset(UTF_8);
                });
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBox jc = (JCheckBox) e.getSource();
            TweakEntry tweak = (TweakEntry) jc.getClientProperty("tweak"); //NOI18N
            if (tweak != null) {
                if (jc.isSelected()) {
                    settings.addTweak(tweak);
                } else {
                    settings.removeTweak(tweak);
                }
            }
        }

        void change() {
            settings.setUserName(nameField.getText());
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            change();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            change();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            change();
        }
    }

    private static final class FinishPanel extends Page {

        private final AATextArea computedSwitches = new AATextArea();
        private final AATextArea changedSwitches = new AATextArea();
        private final JLabel computedSwitchesLabel = LINE_SWITCHES_INFO.label();
        private final JLabel changedSwitchesLabel = REMOVED_SWITCHES_INFO.label();

        FinishPanel(ConfFileSettings settings) {
            super(settings, SAVE_CONFIGURATION_FILE);
            configureFont(computedSwitchesLabel, settings::labelFont);
            configureFont(changedSwitchesLabel, settings::labelFont);
            configureFonts(() -> {
                return new Font("Courier New", Font.PLAIN, 12) // NOI18N
                        .deriveFont(settings.uiFont().getSize2D());
            }, computedSwitches, changedSwitches);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.insets = new Insets(12, 12, 12, 12);

            computedSwitchesLabel.setLabelFor(computedSwitches);
            changedSwitchesLabel.setLabelFor(changedSwitches);

            inner.add(computedSwitchesLabel, gbc);
            gbc.gridy++;

            computedSwitches.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
            computedSwitches.setEditable(false);
            computedSwitches.setBackground(UIManager.getColor("control")); // NOI18N
            computedSwitches.setLineWrap(true);
            computedSwitches.setWrapStyleWord(true);
            computedSwitches.setToolTipText(computedSwitchesLabel.getToolTipText());

            inner.add(computedSwitches, gbc);

            changedSwitches.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
            changedSwitches.setEditable(false);
            changedSwitches.setBackground(UIManager.getColor("control")); // NOI18N
            changedSwitches.setLineWrap(true);
            changedSwitches.setWrapStyleWord(true);
            changedSwitches.setToolTipText(changedSwitchesLabel.getToolTipText());

            gbc.gridy++;
            gbc.insets = new Insets(48, 12, 12, 12);
            inner.add(changedSwitchesLabel, gbc);
            gbc.insets = new Insets(12, 12, 12, 12);
            gbc.gridy++;
            inner.add(changedSwitches, gbc);

            onComputedFontChange(0);
        }

        @Override
        public void addNotify() {
            super.addNotify(); //To change body of generated methods, choose Tools | Templates.

            LineSwitchWriter writer = new LineSwitchWriter(new ArrayList<>(), new DefaultOptionsReplacementChecker());
            settings.contribute(writer);
            StringBuilder sb = new StringBuilder();
            for (String sw : writer.switches()) {
                sb.append(sw).append(' ');
            }
            computedSwitches.setText(sb.toString());

            if (settings.confFilePath() != null) {
                ConfFile cf = new ConfFile(settings.confFilePath());
                try {
                    Map<String, List<String>> data = cf.parse();
                    List<String> items = data.get(NETBEANS_DEFAULT_OPTIONS);
                    List<String> origItems = new ArrayList<>(items);

                    LineSwitchWriter w = new LineSwitchWriter(items);
                    settings.contribute(w);

                    List<String> revisedItems = new ArrayList<>(w.switches());
                    origItems.removeAll(revisedItems);

                    if (origItems.size() > 0) {
                        sb = new StringBuilder();
                        for (String s : origItems) {
                            sb.append(s).append(' '); // NOI18N
                        }
                        changedSwitches.setText(sb.toString());
                        changedSwitches.setVisible(true);
                        changedSwitchesLabel.setVisible(true);
                    } else {
                        changedSwitches.setVisible(false);
                        changedSwitchesLabel.setVisible(false);
                    }

                } catch (IOException ex) {
                    Logger.getLogger(CalibrationGUI.class.getName()).log(Level.SEVERE,
                            "Could not read " + settings.confFilePath(), ex); // NOI18N
                }
            } else {
                changedSwitches.setVisible(false);
                changedSwitchesLabel.setVisible(false);
            }
        }
    }

    /**
     * Panel which is only added if no configuration file was passed on the
     * command-line.
     */
    private static final class LocateConfigFilePanel extends Page implements ActionListener, DocumentListener {

        private final AATextField field = new AATextField();
        JFileChooser chooser;
        // Dirty trick to force the default preferred size not to be 0x0
        // which would cause the layout to shift every time an error apppears or
        // disappears
        private final JLabel errors = new AALabel("<html>&nbsp;"); // NOI18N
        private final Consumer<Path> consumer;
        private final JButton button = BROWSE.button();
        private final JLabel fileLabel = Localization.CONFIGURATION_FILE.label();
        private final JPanel innerOuter = new JPanel(new GridBagLayout());

        @Override
        protected void onComputedFontChange(int newSize) {
            File f = null;
            File dir = null;
            if (chooser != null) {
                f = chooser.getSelectedFile();
                dir = chooser.getCurrentDirectory();
            }
            chooser = new AAFileChooser(settings.uiFont());
            if (dir != null) {
                chooser.setCurrentDirectory(dir);
            }
            if (f != null) {
                chooser.setSelectedFile(f);
            }
            chooser.setBackground(Color.WHITE);
            chooser.addActionListener(this);
            field.setFont(settings.uiFont().deriveFont(settings.uiFont().getSize() - 2));
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory()
                            || (f.isFile() && f.canRead() && f.canWrite() && f.getName().endsWith(".conf")); //NOI18N
                }

                @Override
                public String getDescription() {
                    return Localization.CONFIGURATION_FILES.toString();
                }
            });
        }

        @SuppressWarnings("LeakingThisInConstructor") //NOI18N
        LocateConfigFilePanel(ConfFileSettings settings, Consumer<Path> consumer) {
            super(settings, LOCATE_CONFIG_FILE);

            configureFonts(settings::uiFont, errors, field, button, fileLabel);

            errors.setFont(settings.uiFont());
            field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0), field.getBorder()));

            button.addActionListener(this);
            fileLabel.setLabelFor(field);
            field.setColumns(72);
            Insets bi = button.getInsets();
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(bi.top + 12, 0, bi.bottom, 12),
                    field.getBorder()));

            innerOuter.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.weightx = 0.75F;
            gbc.weighty = 1;
            gbc.ipady = 0;
            innerOuter.add(fileLabel, gbc);
            gbc.gridx++;
            gbc.insets = new Insets(0, 0, 0, 12);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            innerOuter.add(field, gbc);
            gbc.ipady = 0;
            gbc.gridx++;
            gbc.weightx = 0.25F;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            innerOuter.add(button, gbc);
            field.getDocument().addDocumentListener(this);
            field.setBackground(UIManager.getColor("control")); //NOI18N
            field.setOpaque(false);

            errors.setForeground(Color.RED);
            // avoid the layout jumping when text appears
            errors.setMinimumSize(new Dimension(30, 30));
            add(innerOuter, BorderLayout.CENTER);
            add(errors, BorderLayout.SOUTH);

            field.getDocument().addDocumentListener(this);

            this.consumer = consumer;
            onComputedFontChange(0);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JButton) {
                chooser.showDialog(field, Localization.SELECT.toString());
            } else {
                File file = chooser.getSelectedFile();
                if (file != null) {
                    field.setText(file.getAbsolutePath());
                } else {
                    field.setText(""); //NOI18N
                }
            }
        }

        void change() {
            String pth = field.getText().trim();
            if (pth.isEmpty()) {
                errors.setText(ERR_NO_FILE_SELECTED.toString());
            } else {
                Path path = Paths.get(pth);
                if (Files.exists(path)) {
                    if (Files.isDirectory(path)) {
                        errors.setText(ERR_IS_DIRECTORY.toString());
                        path = null;
                    } else {
                        errors.setText("<html>&nbsp;"); //NOI18N
                    }
                    consumer.accept(path);
                } else {
                    consumer.accept(null);
                    errors.setText(ERR_NO_SUCH_FILE.toString());
                }
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            change();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            change();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            change();
        }
    }
}

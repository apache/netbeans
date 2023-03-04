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
package org.netbeans.modules.print.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExClipboard;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.14
 */
public final class UI {

    private UI() {}

    public static boolean isAlt(int modifiers) {
        return isModifier(modifiers, KeyEvent.ALT_MASK);
    }

    public static boolean isShift(int modifiers) {
        return isModifier(modifiers, KeyEvent.SHIFT_MASK);
    }

    public static boolean isCtrl(int modifiers) {
        return isModifier(modifiers, KeyEvent.CTRL_MASK) || isModifier(modifiers, KeyEvent.META_MASK);
    }

    private static boolean isModifier(int modifiers, int mask) {
        return (modifiers & mask) != 0;
    }

    public static MyComboBox createComboBox(Object[] items) {
        return new MyComboBox(items);
    }

    public static JLabel createLabel(String message) {
        JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label, message);
        return label;
    }

    public static JRadioButton createRadioButton(String text, String toolTip) {
        JRadioButton button = new JRadioButton();
        Mnemonics.setLocalizedText(button, text);
        button.setText(cutMnemonicAndAmpersand(text));
        button.setToolTipText(toolTip);
        return button;
    }

    public static JButton createButton(Action action) {
        return (JButton) createAbstractButton(new JButton(), action);
    }

    public static JCheckBox createCheckBox(Action action) {
        return (JCheckBox) createAbstractButton(new JCheckBox(), action);
    }

    public static JToggleButton createToggleButton(Action action) {
        return (JToggleButton) createAbstractButton(new JToggleButton(), action);
    }

    public static void setItems(JComboBox comboBox, Object[] items) {
        Object selected = comboBox.getSelectedItem();
        comboBox.removeAllItems();

        for (int i = 0; i < items.length; i++) {
            comboBox.insertItemAt(items[i], i);
        }
        if (items.length > 0) {
            comboBox.setSelectedIndex(0);
        }
        if (selected != null) {
            comboBox.setSelectedItem(selected);
        }
    }

    public static JPanel createSeparator(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;

        c.insets = new Insets(LARGE_SIZE, 0, LARGE_SIZE, 0);
        panel.add(createLabel(message), c);

        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(LARGE_SIZE, LARGE_SIZE, LARGE_SIZE, 0);
        panel.add(new JSeparator(), c);

        return panel;
    }

    private static AbstractButton createAbstractButton(AbstractButton button, Action action) {
        button.setAction(action);
        mnemonicAndToolTip(button, (String) action.getValue(Action.SHORT_DESCRIPTION));
        return button;
    }

    private static void mnemonicAndToolTip(AbstractButton button, String toolTip) {
        String text = button.getText();

        if (text == null) {
            Mnemonics.setLocalizedText(button, toolTip);
            button.setText(null);
        }
        else {
            Mnemonics.setLocalizedText(button, text);
            button.setText(cutMnemonicAndAmpersand(text));
        }
        button.setToolTipText(cutMnemonicAndAmpersand(toolTip));
    }

    private static String cutMnemonicAndAmpersand(String value) {
        if (value == null) {
            return null;
        }
        int k = value.lastIndexOf(" // "); // NOI18N

        if (k != -1) {
            value = value.substring(0, k);
        }
        k = value.indexOf("&"); // NOI18N

        if (k == -1) {
            return value;
        }
        return value.substring(0, k) + value.substring(k + 1);
    }

    public static JTextArea createTextArea(int columns, String message) {
        JTextArea text = new JTextArea(message);
        text.setBackground(null);
        text.setEditable(false);
        text.setColumns(columns);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        return text;
    }

    public static void a11y(Component component, String a11y) {
        a11y(component, a11y, a11y);
    }

    public static void a11y(Component component, String name, String description) {
        if (name != null) {
            component.getAccessibleContext().setAccessibleName(name);
        }
        if (description != null) {
            component.getAccessibleContext().setAccessibleDescription(description);
        }
    }

    public static String i18n(Class clazz, String key) {
        if (key == null) {
            return null;
        }
        return NbBundle.getMessage(clazz, key);
    }

    public static String i18n(Class clazz, String key, String param) {
        if (key == null) {
            return null;
        }
        return NbBundle.getMessage(clazz, key, param);
    }

    public static String i18n(Class clazz, String key, String param1, String param2) {
        if (key == null) {
            return null;
        }
        return NbBundle.getMessage(clazz, key, param1, param2);
    }

    public static String i18n(Class clazz, String key, String param1, String param2, String param3) {
        if (key == null) {
            return null;
        }
        return NbBundle.getMessage(clazz, key, param1, param2, param3);
    }

    public static boolean printWarning(String message) {
        NotifyDescriptor confirm = new NotifyDescriptor.Confirmation(message, NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(confirm);
        return confirm.getValue() == NotifyDescriptor.YES_OPTION;
    }

    public static boolean printConfirmation(String message) {
        return NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(message, NotifyDescriptor.YES_NO_OPTION)));
    }

    public static void printInformation(String message) {
        print(message, NotifyDescriptor.INFORMATION_MESSAGE);
    }

    public static void printError(String message) {
        print(message, NotifyDescriptor.ERROR_MESSAGE);
    }

    public static void printError(Exception exception) {
        print(exception.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
    }

    private static void print(String message, int type) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, type));
    }

    public static ImageIcon icon(Class clazz, String name) {
        if (name == null) {
            return null;
        }
        return new ImageIcon(clazz.getResource("image/" + name + ".gif")); // NOI18N
    }

    public static Node getSelectedNode() {
        Node[] nodes = getSelectedNodes();

        if (nodes == null) {
            return null;
        }
        return nodes[0];
    }

    public static Node[] getSelectedNodes() {
//out();
        TopComponent top = getActiveTopComponent();
//out("top: " + top);
        if (top == null) {
            return null;
        }
        Node[] nodes = top.getActivatedNodes();
//out("nodes: " + nodes);

        if (nodes == null || nodes.length == 0) {
            return null;
        }
        return nodes;
    }

    public static TopComponent getActiveTopComponent() {
        return TopComponent.getRegistry().getActivated();
    }

    public static void setWidth(JComponent component, int width) {
        setSize(component, new Dimension(width, component.getPreferredSize().height));
    }

    public static void setHeight(JComponent component, int height) {
        setSize(component, new Dimension(component.getPreferredSize().width, height));
    }

    public static void setSize(JComponent component, Dimension dimension) {
        component.setMinimumSize(dimension);
        component.setPreferredSize(dimension);
    }

    public static void setSize(JComponent component, int width, int height) {
        setSize(component, new Dimension(width, height));
    }

    public static int getInt(Object value) {
        try {
            return Integer.parseInt(value.toString());
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }

    public static double getDouble(Object value) {
        try {
            return Double.parseDouble(value.toString());
        }
        catch (NumberFormatException e) {
            return -1.0;
        }
    }

    public static int round(double value) {
        return (int) Math.ceil(value);
    }

    public static String replace(String source, String searchFor, String replaceWith) {
        if (source == null) {
            return null;
        }
        if (searchFor == null || searchFor.length() == 0) {
            return null;
        }
        int k = 0;
        int found = source.indexOf(searchFor, k);
        StringBuilder builder = new StringBuilder();

        while (true) {
            if (found == -1) {
                break;
            }
            builder.append(source.substring(k, found));
            builder.append(replaceWith);

            k = found + searchFor.length();
            found = source.indexOf(searchFor, k);
        }
        if (k > 0) {
            builder.append(source.substring(k));
            return builder.toString();
        }
        else {
            return source;
        }
    }

    public static DataObject getDataObject(Object object) {
        if ( !(object instanceof Node)) {
            return null;
        }
        return (DataObject) ((Node) object).getLookup().lookup(DataObject.class);
    }

    public static JComponent getResizableX(JPanel panel) {
        return getResizable(panel, GridBagConstraints.HORIZONTAL);
    }

    public static JComponent getResizableXY(JPanel panel) {
        return getResizable(panel, GridBagConstraints.BOTH);
    }

    private static JComponent getResizable(JPanel panel, int fill) {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill =  fill;
        c.insets = new Insets(0, LARGE_SIZE, 0, LARGE_SIZE);
        c.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(panel, c);
//      panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.red));
//      mainPanel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.blue));

        return mainPanel;
    }

    public static String removeHtml(String value) {
        if (value == null) {
            return null;
        }
        value = replace(value, "<b>", "'"); // NOI18N
        value = replace(value, "</b>", "'"); // NOI18N
        value = replace(value, "&nbsp;", " "); // NOI18N

        return value;
    }

    public static String getHtml(String value) {
        return "<html>" + value + "</html>"; // NOI18N
    }

    public static <T> List<T> getInstances(Class<T> clazz) {
        Collection<? extends T> collection = Lookup.getDefault().lookupAll(clazz);
        List<T> list = new ArrayList<T>();

        for (Object object : collection) {
            list.add(clazz.cast(object));
        }
        return list;
    }

    public static void copyToClipboard(String value) {
        getClipboard().setContents(new StringSelection(value), null);
    }

    private static Clipboard getClipboard() {
        Clipboard clipboard = Lookup.getDefault().lookup(ExClipboard.class);

        if (clipboard == null) {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return clipboard;
    }

    public static boolean isDigit(char c) {
        return "0123456789".indexOf(c) != -1; // NOI18N
    }

    public static void show(Container container, String indent) {
        out((container.isShowing() ? "[[ " : "") + indent + container.getClass().getName()); // NOI18N
        Component[] children = container.getComponents();

        for (Component child : children) {
            if (child instanceof Container) {
                show((Container) child, "    " + indent); // NOI18N
            }
        }
    }

    public static void startTimeln() {
        tim();
        startTime();
    }

    public static void startTime() {
        ourTimes.push(System.currentTimeMillis());
    }

    public static void endTime(Object object) {
        long currentTime = System.currentTimeMillis();
        tim(object + ": " + ((currentTime - ourTimes.pop()) / MILLIS) + " sec."); // NOI18N
    }

    public static void tim() {
        if (ENABLE_TIM) {
            System.out.println();
        }
    }

    public static void tim(Object object) {
        if (ENABLE_TIM) {
            System.out.println("*** " + object); // NOI18N
        }
    }

    public static void log() {
        if (ENABLE_LOG) {
            System.out.println();
        }
    }

    public static void log(Object object) {
        if (ENABLE_LOG) {
            System.out.println("*** " + object); // NOI18N
        }
    }

    public static void dump() {
        dump(null);
    }

    public static void dump(Object object) {
        out();
        out();

        if (object != null) {
            out(object);
        }
        new Exception("!!!").printStackTrace(); // NOI18N
    }

    public static void out() {
        if (ENABLE_OUT) {
            System.out.println();
        }
    }

    public static void out(Object object) {
        if (ENABLE_OUT) {
            System.out.println("*** " + object); // NOI18N
        }
    }

    // ------------------------------------------------
    public static class MyComboBox extends JComboBox {

        public MyComboBox(Object[] items) {
            super(items);
            init();
        }

        @Override
        public boolean selectWithKeyChar(char key) {
            processKey(key);
            setSelectedIndex(myIndex);
            return true;
        }

        public void init() {
//out();
//out("init");
            myIndex = 0;
            myPrefix = ""; // NOI18N
        }

        private void processKey(char key) {
//out("select: '" + key);
            if (((int) key) == Event.BACK_SPACE) {
                init();
                return;
            }
            myPrefix += key;
            myPrefix = myPrefix.toLowerCase();

//out("prefix: " + myPrefix);
            for (int i = myIndex; i < getItemCount(); i++) {
                String item = getItemAt(i).toString().toLowerCase();
//out("  see: " + item);

                if (item.startsWith(myPrefix)) {
                    myIndex = i;
                    return;
                }
            }
        }
        private int myIndex;
        private String myPrefix;
    }

    // -------------------------------------------------------------
    public abstract static class IconAction extends AbstractAction {

        protected IconAction(String name, String toolTip, Icon icon) {
            super(name, icon);
            putValue(SHORT_DESCRIPTION, toolTip);
        }

        protected final Node getLastNode() {
            Node node = getSelectedNode();

            if (node == null) {
                node = myLastNode;
            }
            else {
                myLastNode = node;
            }
            return node;
        }
        private Node myLastNode;
    }

    // ---------------------------------------------------------------
    public abstract static class ButtonAction extends AbstractAction {

        public ButtonAction(String text, String toolTip) {
            this(text, null, toolTip);
        }

        public ButtonAction(Icon icon, String toolTip) {
            this(null, icon, toolTip);
        }

        public ButtonAction(String text) {
            this(text, null, text);
        }

        private ButtonAction(String text, Icon icon, String toolTip) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, toolTip);
        }
    }

    // --------------------------------------------------------
    public abstract static class Dialog extends WindowAdapter {

        protected void opened() {}

        protected void closed() {}

        protected void resized() {}

        protected void updated() {}

        protected abstract DialogDescriptor createDescriptor();

        public void show() {
            show(true, true);
        }

        public void show(boolean isResizable) {
            show(true, isResizable);
        }

        public void showAndWait() {
            show(false, true);
        }

        protected final void setModal(boolean isModal) {
            myIsModal = isModal;
        }

        private void show(boolean inSwingThread, boolean isResizable) {
            if (myDialog == null) {
                myDialog = DialogDisplayer.getDefault().createDialog(createDescriptor());
                myDialog.addWindowListener(this);
                myDialog.setResizable(isResizable);
                myDialog.setModal(myIsModal);

                if (isResizable) {
                    setCorner();
                }
                myDialog.addComponentListener(
                    new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent event) {
                            resized();
                        }
                    }
                );
            }
            else {
                opened();
            }
            updated();

            if (inSwingThread) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        myDialog.setVisible(true);
                    }
                });
            }
            else {
                myDialog.setVisible(true);
            }
        }

        public Component getUIComponent() {
            return myDialog;
        }

        @Override
        public void windowOpened(WindowEvent event) {
            opened();
        }

        @Override
        public void windowClosed(WindowEvent event) {
            closed();
        }

        protected final String i18n(String key) {
            return UI.i18n(getClass(), key);
        }

        protected final String i18n(String key, String param) {
            return UI.i18n(getClass(), key, param);
        }

        private void setCorner() {
            if (myDialog instanceof JDialog) {
                ((JDialog) myDialog).getRootPane().setBorder(CORNER_BORDER);
            }
        }

        private boolean myIsModal = true;
        private java.awt.Dialog myDialog;
    }

    // ----------------------------------------------------------
    private static final class CornerBorder extends EmptyBorder {

        public CornerBorder() {
            super(0, SMALL_SIZE, SMALL_SIZE, SMALL_SIZE);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            CORNER.paintIcon(c, g, w - CORNER.getIconWidth(), h - CORNER.getIconHeight());
        }

        private static final Icon CORNER = new ImageIcon(new byte[] {
            (byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x39, (byte) 0x61, (byte) 0x0c,
            (byte) 0x00, (byte) 0x0c, (byte) 0x00, (byte) 0xf7, (byte) 0x00, (byte) 0x00, (byte) 0x83,
            (byte) 0x83, (byte) 0x83, (byte) 0xd3, (byte) 0xd3, (byte) 0xc8, (byte) 0xfd, (byte) 0xfd,
            (byte) 0xfd, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x21, (byte) 0xf9, (byte) 0x04,
            (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0x2c, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0c, (byte) 0x00, (byte) 0x0c, (byte) 0x00,
            (byte) 0x40, (byte) 0x08, (byte) 0x34, (byte) 0x00, (byte) 0xff, (byte) 0x09, (byte) 0x1c,
            (byte) 0x48, (byte) 0xf0, (byte) 0x9f, (byte) 0x80, (byte) 0x81, (byte) 0x02, (byte) 0x00,
            (byte) 0x00, (byte) 0x30, (byte) 0xa8, (byte) 0xd0, (byte) 0x60, (byte) 0x41, (byte) 0x81,
            (byte) 0x09, (byte) 0x17, (byte) 0x1e, (byte) 0x7c, (byte) 0x08, (byte) 0xb1, (byte) 0x21,
            (byte) 0xc1, (byte) 0x88, (byte) 0x0c, (byte) 0x25, (byte) 0x36, (byte) 0xc4, (byte) 0x88,
            (byte) 0x91, (byte) 0x62, (byte) 0x45, (byte) 0x8f, (byte) 0x1d, (byte) 0x0b, (byte) 0x72,
            (byte) 0x5c, (byte) 0x88, (byte) 0x70, (byte) 0xa3, (byte) 0xc5, (byte) 0x8c, (byte) 0x28,
            (byte) 0x13, (byte) 0x8e, (byte) 0xd4, (byte) 0xb8, (byte) 0x30, (byte) 0x20, (byte) 0x00,
            (byte) 0x3b
        });
    }
    private static Stack<Long> ourTimes = new Stack<Long>();
    public static final int TINY_SIZE = 2; // the 3-th Fibonacci number
    public static final int SMALL_SIZE = 3; // the 4-th Fibonacci number
    public static final int MEDIUM_SIZE = 5; // the 5-th Fibonacci number
    public static final int LARGE_SIZE = 8; // the 6-th Fibonacci number
    public static final int HUGE_SIZE = 13; // the 7-th Fibonacci number
    private static final double MILLIS = 1000.0;
    public static final String UH = System.getProperty("user.home"); // NOI18N
    public static final String LS = System.getProperty("line.separator"); // NOI18N
    public static final String FS = System.getProperty("file.separator"); // NOI18N
    private static final Border CORNER_BORDER = new CornerBorder();
    private static final boolean ENABLE_OUT = System.getProperty("org.netbeans.modules.out") != null; // NOI18N
    private static final boolean ENABLE_LOG = System.getProperty("org.netbeans.modules.log") != null; // NOI18N
    private static final boolean ENABLE_TIM = System.getProperty("org.netbeans.modules.tim") != null; // NOI18N
}

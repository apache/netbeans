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
package org.netbeans.modules.editor.search;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.MultiKeymap;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.util.Exceptions;

public class SearchComboBoxEditor implements ComboBoxEditor {
    private final JScrollPane scrollPane;
    private final JEditorPane editorPane;
    private Object oldValue;
    private static final JTextField referenceTextField = (JTextField) new JComboBox<String>().getEditor().getEditorComponent();
    private static final Logger LOG = Logger.getLogger(SearchComboBoxEditor.class.getName());

    public SearchComboBoxEditor() {
        editorPane = new JEditorPane();
        changeToOneLineEditorPane(editorPane);

        Set<AWTKeyStroke> tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, tfkeys);
        tfkeys = referenceTextField.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        editorPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, tfkeys);
        LOG.log(Level.FINE, "Constructor - Reference Font: Name: {0}, Size: {1}\n", new Object[]{referenceTextField.getFont().getFontName(), referenceTextField.getFont().getSize()}); //NOI18N
        editorPane.setFont(referenceTextField.getFont());
        LOG.log(Level.FINE, "Constructor - Set Font: Name: {0}, Size: {1}\n", new Object[]{editorPane.getFont().getFontName(), editorPane.getFont().getSize()}); //NOI18N
        final Insets margin = referenceTextField.getMargin();

        scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                               JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {

            @Override
            public void setViewportView(Component view) {
                if (view instanceof JComponent) {
                    ((JComponent) view).setBorder(new EmptyBorder(margin)); // borderInsets
                }
                if (view instanceof JEditorPane) {
                    adjustScrollPaneSize(this, (JEditorPane) view);
                }
                super.setViewportView(view);
            }
        };
        editorPane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("editorKit".equals(evt.getPropertyName())) { // NOI18N
                    adjustScrollPaneSize(scrollPane, editorPane);
                }
            }
        });

        final Border border = referenceTextField.getBorder();
        if (border != null) {
            final Insets borderInsets = border.getBorderInsets(referenceTextField);
            if (isCurrentLF("Aqua")) {  //NOI18N
                scrollPane.setBorder(new EmptyBorder (0, 0, 0, 0));
            } else {
                scrollPane.setBorder(new EmptyBorder(borderInsets));
            }
        } else {
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
        }
        scrollPane.setFont(referenceTextField.getFont());
        scrollPane.setBackground(referenceTextField.getBackground());
        int preferredHeight = referenceTextField.getPreferredSize().height;
        Dimension spDim = scrollPane.getPreferredSize();
        spDim.height = preferredHeight + getLFHeightAdjustment();
        if (!isCurrentLF("Aqua")) {  //NOI18N
            spDim.height += margin.bottom + margin.top; //borderInsets.top + borderInsets.bottom;
        }
        scrollPane.setPreferredSize(spDim);
        scrollPane.setMinimumSize(spDim);
        scrollPane.setMaximumSize(spDim);
        scrollPane.setViewportView(editorPane);

        final DocumentListener manageViewListener = new ManageViewPositionListener(editorPane, scrollPane);
        DocumentUtilities.addDocumentListener(editorPane.getDocument(), manageViewListener, DocumentListenerPriority.AFTER_CARET_UPDATE);
        editorPane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("document".equals(evt.getPropertyName())) { // NOI18N
                    Document oldDoc = (Document) evt.getOldValue();
                    if (oldDoc != null) {
                        DocumentUtilities.removeDocumentListener(oldDoc, manageViewListener, DocumentListenerPriority.AFTER_CARET_UPDATE);
                    }
                    Document newDoc = (Document) evt.getNewValue();
                    if (newDoc != null) {
                        DocumentUtilities.addDocumentListener(newDoc, manageViewListener, DocumentListenerPriority.AFTER_CARET_UPDATE);
                    }
                }
                // NETBEANS-4444
                // selection is not removed when text is input via IME
                // so, just remove it when the caret is changed
                if ("caret".equals(evt.getPropertyName())) { // NOI18N
                    if (evt.getOldValue() instanceof Caret
                            && evt.getNewValue() instanceof Caret) { // NETBEANS-4620 check new value is not null but Caret
                        Caret oldCaret = (Caret) evt.getOldValue();
                        int dotPosition = oldCaret.getDot();
                        int markPosition = oldCaret.getMark();
                        if (dotPosition != markPosition) {
                            try {
                                editorPane.getDocument().remove(Math.min(markPosition, dotPosition), Math.abs(markPosition - dotPosition));
                            } catch (BadLocationException ex) {
                                LOG.log(Level.WARNING, "Invalid removal offset: {0}", ex.offsetRequested()); // NOI18N
                            }
                        }
                    }
                }
            }
        });
    }

    public static void changeToOneLineEditorPane(JEditorPane editorPane) {
        editorPane.putClientProperty("AsTextField", Boolean.TRUE); //NOI18N
        editorPane.putClientProperty(
            "HighlightsLayerExcludes", //NOI18N
            ".*(?<!TextSelectionHighlighting)$" //NOI18N
        );

        EditorKit kit = MimeLookup.getLookup(SearchNbEditorKit.SEARCHBAR_MIMETYPE).lookup(EditorKit.class);
        if (kit == null) {
            throw new IllegalArgumentException("No EditorKit for '" + SearchNbEditorKit.SEARCHBAR_MIMETYPE + "' mimetype."); //NOI18N
        }

        editorPane.setEditorKit(kit);

        ActionInvoker.putActionToComponent(new ActionInvoker(SearchNbEditorKit.SEARCH_ACTION, editorPane), editorPane);
        ActionInvoker.putActionToComponent(new ActionInvoker(SearchNbEditorKit.REPLACE_ACTION, editorPane), editorPane);
        ActionInvoker.putActionToComponent(new ActionInvoker(ExtKit.gotoAction, editorPane), editorPane);

        InputMap im = editorPane.getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), NO_ACTION);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), NO_ACTION);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), NO_ACTION);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), NO_ACTION);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), NO_ACTION);


        ((AbstractDocument) editorPane.getDocument()).setDocumentFilter(new DocumentFilter() {

                    @Override
                    public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                        if (string != null) {
                            fb.insertString(offset, string.replace("\t", "").replace("\n", ""), attr); //NOI18N
                        }
                    }

                    @Override
                    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String string, AttributeSet attr) throws BadLocationException {
                        if (string != null) {
                            fb.replace(offset, length, string.replace("\t", "").replace("\n", ""), attr); //NOI18N
                        }
                    }
                });
        editorPane.setBorder(new EmptyBorder (0, 0, 0, 0));
        if (isCurrentLF("GTK")) {
            editorPane.setBackground((Color) UIManager.get("text"));
        } else {
            editorPane.setBackground(referenceTextField.getBackground());
        }
        LOG.log(Level.FINE, "Changed editorkit - Set Font: Name: {0}, Size: {1}\n", new Object[]{editorPane.getFont().getFontName(), editorPane.getFont().getSize()}); //NOI18N
        editorPane.setFont(referenceTextField.getFont());
        if (!isCurrentLF("Nimbus")) {
            editorPane.setCaretColor(referenceTextField.getCaretColor());
        }
        LOG.log(Level.FINE, "Changed editorkit - Set Font: Name: {0}, Size: {1}\n", new Object[]{editorPane.getFont().getFontName(), editorPane.getFont().getSize()}); //NOI18N
    }

    private static void adjustScrollPaneSize(JScrollPane sp, JEditorPane editorPane) {
        int height;
        Dimension prefSize = sp.getPreferredSize();
        Insets borderInsets = sp.getBorder() != null ? sp.getBorder().getBorderInsets(sp) : sp.getInsets();
        int vBorder = borderInsets.bottom + borderInsets.top;
        EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(editorPane);
        if (eui != null) {
            height = eui.getLineHeight();
            if (height < eui.getLineAscent()) {
                height = (eui.getLineAscent() * 4) / 3; // Hack for the case when line height = 1
            }
        } else {
            java.awt.Font font = editorPane.getFont();
            java.awt.FontMetrics fontMetrics = editorPane.getFontMetrics(font);
            height = fontMetrics.getHeight();
        }
        height += vBorder + getLFHeightAdjustment();
        //height += 2; // 2 for border
        if (prefSize.height < height) {
            prefSize.height = height;
            sp.setPreferredSize(prefSize);
            sp.setMinimumSize(prefSize);
            sp.setMaximumSize(prefSize);
            java.awt.Container c = sp.getParent();
            if (c instanceof JComponent) {
                ((JComponent) c).revalidate();
            }
        }
    }

    private static boolean isCurrentLF(String lf) {
        LookAndFeel laf = UIManager.getLookAndFeel();
        String lfID = laf.getID();
        return lf.equals(lfID);
    }

    private static int getLFHeightAdjustment() {
        if (isCurrentLF("Metal")) { //NOI18N
            return -7;
        }
        if (isCurrentLF("GTK")) { //NOI18N
            return 2;
        }
        if (isCurrentLF("Motif")) { //NOI18N
            return 3;
        }
        if (isCurrentLF("Nimbus")) { //NOI18N
            return 0;
        }
        if (isCurrentLF("Aqua")) { //NOI18N
            return -10;
        }
        return 0;
    }

    private static final class ManageViewPositionListener implements DocumentListener {

        private final JEditorPane editorPane;
        private final JScrollPane sp;

        public ManageViewPositionListener(JEditorPane editorPane, JScrollPane sp) {
            this.editorPane = editorPane;
            this.sp = sp;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            changed();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changed();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            changed();
        }

        private void changed() {
            JViewport viewport = sp.getViewport();
            Point viewPosition = viewport.getViewPosition();
            if (viewPosition.x > 0) {
                try {
                    Rectangle textRect = editorPane.getUI().modelToView(editorPane, editorPane.getDocument().getLength());
                    int textLength = textRect.x + textRect.width;
                    int viewLength = viewport.getExtentSize().width;
                    if (textLength < (viewPosition.x + viewLength)) {
                        viewPosition.x = Math.max(textLength - viewLength, 0);
                        viewport.setViewPosition(viewPosition);
                    }
                } catch (BadLocationException blex) {
                    Exceptions.printStackTrace(blex);
                }
            }
        }
    }

    private static final String NO_ACTION = "no-action"; //NOI18N


    @Override
    public Component getEditorComponent() {
        return scrollPane;
    }

    @Override
    public void setItem(Object anObject) {
        String text;

        if (anObject != null) {
            text = anObject.toString();
            oldValue = anObject;
        } else {
            text = "";
        }
        // workaround for 4530952
        if (!text.equals(editorPane.getText())) {
            editorPane.setText(text);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object getItem() {
        Object newValue = editorPane.getText();

        if (oldValue != null && !(oldValue instanceof String)) {
            // The original value is not a string. Should return the value in it's
            // original type.
            if (newValue.equals(oldValue.toString())) {
                return oldValue;
            } else {
                // Must take the value from the editor and get the value and cast it to the new type.
                Class cls = oldValue.getClass();
                try {
                    Method method = cls.getMethod("valueOf", new Class[]{String.class}); //NOI18N
                    newValue = method.invoke(oldValue, new Object[]{editorPane.getText()});
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    // Fail silently and return the newValue (a String object)
                }
            }
        }
        return newValue;
    }

    @Override
    public void selectAll() {
        editorPane.selectAll();
        editorPane.requestFocus();
    }

    @Override
    public void addActionListener(ActionListener l) {
    }

    @Override
    public void removeActionListener(ActionListener l) {
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JEditorPane getEditorPane() {
        return editorPane;
    }




    private static final class ActionInvoker extends AbstractAction {
        private static final String PREFIX = "search-invoke-";  //NOI18N
        private final String originalActionName;
        private final Action delegateAction;
        public ActionInvoker(String name, JTextComponent component) {
            super(PREFIX + name);
            originalActionName = name;
            delegateAction = component.getActionMap().get(originalActionName);
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            if (SearchBar.getInstance().getActualTextComponent() != null) {
                ActionEvent newE = new ActionEvent(SearchBar.getInstance().getActualTextComponent(), e.getID(), e.getActionCommand());
                delegateAction.actionPerformed(newE);
            }
        }

        private static void putActionToComponent(ActionInvoker action, JTextComponent component) {
            Keymap keymap = component.getKeymap();
            if (keymap instanceof MultiKeymap) {
                MultiKeymap multiKeymap = (MultiKeymap) keymap;
                KeyStroke[] keyStrokesForAction = multiKeymap.getKeyStrokesForAction(component.getActionMap().get(action.getOriginalActionName()));
                if (keyStrokesForAction == null) {
                    return;
                }
                for (KeyStroke ks : keyStrokesForAction) {
                    component.getInputMap().put(KeyStroke.getKeyStroke(ks.getKeyCode(), ks.getModifiers()), PREFIX + action.getOriginalActionName()); // NOI18N
                }
                component.getActionMap().put(PREFIX + action.getOriginalActionName(), action);
            }
        }

        public String getOriginalActionName() {
            return originalActionName;
        }
    };
}

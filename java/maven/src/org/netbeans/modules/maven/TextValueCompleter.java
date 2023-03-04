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

package org.netbeans.modules.maven;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;

/**
 * inner class does the matching of the JTextField's
 * document to completion strings kept in an ArrayList
 * @author mkleint
 */

public class TextValueCompleter implements DocumentListener {
    private static final String ACTION_FILLIN = "fill-in"; //NOI18N
    private static final String ACTION_HIDEPOPUP = "hidepopup"; //NOI18N
    private static final String ACTION_LISTDOWN = "listdown"; //NOI18N
    private static final String ACTION_LISTPAGEDOWN = "listpagedown"; //NOI18N
    private static final String ACTION_LISTUP = "listup"; //NOI18N
    private static final String ACTION_LISTPAGEUP = "listpageup"; //NOI18N
    private static final String ACTION_SHOWPOPUP = "showpopup"; //NOI18N
    private Pattern pattern;
    private Collection<String> completions;
    private JList completionList;
    private DefaultListModel<String> completionListModel;
    private JScrollPane listScroller;
    private Popup popup;
    private JTextField field;
    private String separators;
    private CaretListener caretListener;
    
    private boolean loading;
    private static final String LOADING = Bundle.LBL_Loading();
    private boolean partial;
    
    public TextValueCompleter(Collection<String> completions, JTextField fld) {
        this.completions = completions;
        this.field = fld;
        field.getDocument().addDocumentListener(this);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                hidePopup();
            }
        });
        caretListener = new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent arg0) {
                // only consider caret updates if the popup window is visible
                if (completionList.isDisplayable() && completionList.isVisible()) {
                    buildAndShowPopup();
                }
            }
        };
        field.addCaretListener(caretListener);
        completionListModel = new DefaultListModel<>();
        completionList = new JList(completionListModel);
        completionList.setFocusable(false);
        completionList.setPrototypeCellValue("lets have it at least this wide and add some more just in case"); //NOI18N
        completionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    if (LOADING.endsWith(completionList.getSelectedValue().toString())) {
                        return;
                    }
                    field.getDocument().removeDocumentListener(TextValueCompleter.this);
                    applyCompletion(completionList.getSelectedValue().toString());
                    hidePopup();
                    field.getDocument().addDocumentListener(TextValueCompleter.this);
                }
            }
        });
        completionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listScroller =new JScrollPane(completionList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listScroller.setFocusable(false);
        field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),ACTION_LISTDOWN);
        field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),ACTION_LISTUP); 
        field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),ACTION_LISTPAGEUP); 
        field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),ACTION_LISTPAGEDOWN);
        field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK),ACTION_SHOWPOPUP);
        field.getActionMap().put(ACTION_LISTDOWN, new AbstractAction() { //NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                if (popup == null) {
                    buildAndShowPopup(0);
                }
                completionList.setSelectedIndex(Math.min(completionList.getSelectedIndex() + 1, completionList.getModel().getSize()));
                completionList.ensureIndexIsVisible(completionList.getSelectedIndex());                            
            }
        });
        field.getActionMap().put(ACTION_LISTUP,  new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (popup == null) {
                    buildAndShowPopup(0);
                }
                completionList.setSelectedIndex(Math.max(completionList.getSelectedIndex() - 1, 0));
                completionList.ensureIndexIsVisible(completionList.getSelectedIndex());
            }
        });
        field.getActionMap().put(ACTION_LISTPAGEDOWN, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                completionList.setSelectedIndex(Math.min(completionList.getSelectedIndex() + completionList.getVisibleRowCount(), completionList.getModel().getSize()));
                completionList.ensureIndexIsVisible(completionList.getSelectedIndex());
            }
        });
        field.getActionMap().put(ACTION_LISTPAGEUP, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                completionList.setSelectedIndex(Math.max(completionList.getSelectedIndex() - completionList.getVisibleRowCount(), 0));
                completionList.ensureIndexIsVisible(completionList.getSelectedIndex());
            }
        });
        field.getActionMap().put(ACTION_FILLIN, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selVal = completionList.getSelectedValue();
                if (selVal != null && LOADING.endsWith(selVal.toString())) {
                    return;
                }
                field.getDocument().removeDocumentListener(TextValueCompleter.this);
                if (selVal != null) {
                    applyCompletion(selVal.toString());
                }
                hidePopup();
                field.getDocument().addDocumentListener(TextValueCompleter.this);
            }
        });
        field.getActionMap().put(ACTION_HIDEPOPUP, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hidePopup();
            }
        });
        field.getActionMap().put(ACTION_SHOWPOPUP, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buildAndShowPopup();
            }
        });
    }
    
    public TextValueCompleter(Collection<String> completions, JTextField fld, String separators) {
        this(completions, fld);
        this.separators = separators;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
          completionListModel.removeAllElements();
          completionListModel.addElement(LOADING);
        } else {
          completionListModel.removeElement(LOADING);
        }
    }

    
    @NbBundle.Messages("PARTIAL_RESULT=<Result is incomplete, some indices are processed>")
    private void buildPopup() {
        pattern = Pattern.compile(getCompletionPrefix() + ".+"); //NOI18N
        int entryindex = 0;
        for (String completion : completions) {
            // check if match
            Matcher matcher = pattern.matcher(completion);
            if (matcher.matches()) {
                if (!completionListModel.contains(completion)) {
                    completionListModel.add(entryindex,
                            completion);
                }
                entryindex++;
            } else {
                completionListModel.removeElement(completion);
            }
        }
        completionListModel.removeElement(Bundle.PARTIAL_RESULT());
        if (partial) {
            completionListModel.addElement(Bundle.PARTIAL_RESULT());
        }
    }
    
    private void applyCompletion(String completed) {
        field.removeCaretListener(caretListener);
        if (separators != null) {
            int pos = field.getCaretPosition();
            String currentText = field.getText();
             int caretPosition=0;
            StringTokenizer tok = new StringTokenizer(currentText, separators, true);
           int tokens =tok.countTokens();
            int count = 0;
            String newValue = ""; //NOI18N
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                if (count + token.length() >= pos) {
                    if (separators.indexOf(token.charAt(0)) != -1) {
                        newValue = newValue + token;
                    }
                    newValue = newValue + completed+separators;
                     caretPosition=newValue.length();
                    while (tok.hasMoreTokens()) {
                        newValue = newValue + tok.nextToken();
                    }
                    field.setText(newValue);
                  
                    field.setCaretPosition(caretPosition);
                    field.addCaretListener(caretListener);
                    return;
                } else {
                    count = count + token.length();
                    newValue = newValue + token;
                }
            }
            newValue = newValue + completed+separators;
            field.setText(newValue);
            field.setCaretPosition(newValue.length());
        } else {
            field.setText(completed);
        }
        field.addCaretListener(caretListener);
    }
    
    private String getCompletionPrefix() {
        if (separators != null) {
            int pos = field.getCaretPosition();
            String currentText = field.getText();
            StringTokenizer tok = new StringTokenizer(currentText, separators, true);
            int count = 0;
            String lastToken = ""; //NOI18N
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                if (count + token.length() >= pos) {
                    if (separators.indexOf(token.charAt(0)) != -1) {
                        return ""; //NOI18N
                    }
                    return Pattern.quote(token.substring(0, pos - count));
                } else {
                    count = count + token.length();
                    lastToken = token;
                }
            }
            if (lastToken.length() > 0 && separators.indexOf(lastToken.charAt(0)) == -1) {
                return Pattern.quote(lastToken);
            }
            return ""; //NOI18N
        } else {
            return Pattern.quote(field.getText().trim());
        }
    }
    
    private void showPopup() {
        hidePopup();
        if (completionListModel.getSize() == 0) {
            return;
        }
        // figure out where the text field is,
        // and where its bottom left is
        java.awt.Point los = field.getLocationOnScreen();
        int popX = los.x;
        int popY = los.y + field.getHeight();
        popup = PopupFactory.getSharedInstance().getPopup(field, listScroller, popX, popY);
        field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),ACTION_HIDEPOPUP);
        field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),ACTION_FILLIN);
        popup.show();
        if (completionList.getSelectedIndex() != -1) {
            completionList.ensureIndexIsVisible(completionList.getSelectedIndex());
        }
    }
    
    private void hidePopup() {
        field.getInputMap().remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        field.getInputMap().remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        if (popup != null) {
            popup.hide();
            popup = null;
        }
    }
    
    private class BuildTimer extends Timer {
        private static final int DEFAULT_DELAY = 400;
        public BuildTimer() {
            super(DEFAULT_DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!field.isShowing()) {
                        // became closed in the meantime
                        return;
                    }
                    buildPopup();
                    showPopup();
                }
            });
            setRepeats(false);
        }
    };
    
    private final BuildTimer buildTimer = new BuildTimer();
    
    private void buildAndShowPopup() {
        buildAndShowPopup(BuildTimer.DEFAULT_DELAY);
    }
    
    private void buildAndShowPopup(int delay) {
        buildTimer.setInitialDelay(delay);
        buildTimer.restart();
    }
    
    // DocumentListener implementation
    @Override
    public void insertUpdate(DocumentEvent e) { 
        if (field.isFocusOwner()) {
            buildAndShowPopup(); 
        }
    }
    @Override
    public void removeUpdate(DocumentEvent e) { 
        if (field.isFocusOwner() && completionList.isDisplayable() && completionList.isVisible()) {
            buildAndShowPopup(); 
        }
    }
    @Override
    public void changedUpdate(DocumentEvent e) { 
        if (field.isFocusOwner()) {
            buildAndShowPopup(); 
        }
    }
    
    public void setValueList(Collection<String> values, boolean partial) {
        assert SwingUtilities.isEventDispatchThread();
        completionListModel.removeAllElements();
        completions = values;
        this.partial = partial;
        if (field.isFocusOwner() && completionList.isDisplayable() && completionList.isVisible()) {
            buildAndShowPopup(); 
        }
    }
}


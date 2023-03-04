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
package org.netbeans.lib.terminalemulator.support;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A panel to facilitate text searches with the following elements:
 * <ul
 * <li>A search pattern text entry field.
 * <li>A Prev search button.
 * <li>A Next search button.
 * <li>An error area.
 * <li>A close button.
 * </ul>
 * <p>
 * A FindBar doesn't do any searching by itself but acts as a controller of a
 * {@link FindState} which it can multiplex via {@link FindBar#setState}.
 * @author ivan
 */
public final class FindBar extends JPanel {

    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);

    private final Owner owner;
    private FindState state;

    private boolean updating = false;   // true while view is being updated

    private Action closeAction;
    private Action nextAction;
    private Action prevAction;

    private JTextField findText;
    private JLabel errorLabel;
    private Color originalColor;

    /**
     * Callback interface used to communicate to the owner of a {@link FindBar}
     * that it's close button was pressed.
     */
    public interface Owner {
        public void close(FindBar who);
    }

    private final class CloseAction extends AbstractAction {

        public CloseAction() {
            super(Catalog.get("CTL_Close"), new ImageIcon(FindBar.class.getResource("find_close.png")));
        }

	@Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            close();
        }
    }

    private final class NextAction extends AbstractAction {

        public NextAction() {
            super(Catalog.get("CTL_Next"), new ImageIcon(FindBar.class.getResource("find_next.png")));	// NOI18N
        }

	@Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            next();
        }
    }

    private final class PrevAction extends AbstractAction {

        public PrevAction() {
            super(Catalog.get("CTL_Previous"), new ImageIcon(FindBar.class.getResource("find_previous.png"))); // NOI18N
        }

	@Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            prev();
        }
    }

    /**
     * Construct a FindBar.
     * @param owner Is used to call {@link Owner#close()} when the close
     *              button is pressed.
     */
    public FindBar(Owner owner) {
        super();
        this.owner = owner;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JLabel findLabel = new JLabel();
        findLabel.setText(Catalog.get("LBL_Find") + ":");// NOI18N
        findText = new JTextField() {

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, super.getPreferredSize().height);
            }
        };
        originalColor = findText.getForeground();

        findText.getDocument().addDocumentListener(new DocumentListener() {

	    @Override
            public void insertUpdate(DocumentEvent e) {
                if (!updating) {
                    state.setPattern(findText.getText());
                    error(state.getStatus(), false);
                }
            }

	    @Override
            public void removeUpdate(DocumentEvent e) {
                insertUpdate(e);
            }

	    @Override
            public void changedUpdate(DocumentEvent e) {
                insertUpdate(e);
            }
        });

        findLabel.setLabelFor(findText);
        prevAction = new PrevAction();
        JButton prevButton = new JButton(prevAction);
        adjustButton(prevButton);
        nextAction = new NextAction();
        JButton nextButton = new JButton(nextAction);
        adjustButton(nextButton);
        closeAction = new CloseAction();
        JButton closeButton = new JButton(closeAction);
        adjustButton(closeButton);

        InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_MASK), getName(prevAction));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), getName(nextAction));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), getName(closeAction));


        ActionMap actionMap = getActionMap();
        actionMap.put(prevAction.getValue(Action.NAME), prevAction);
        actionMap.put(nextAction.getValue(Action.NAME), nextAction);
        actionMap.put(closeAction.getValue(Action.NAME), closeAction);

        findText.getActionMap().put(nextAction.getValue(Action.NAME), nextAction);
        findText.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), getName(nextAction));

        errorLabel = new JLabel();

        add(Box.createRigidArea(new Dimension(5, 0)));
        add(findLabel);
        add(findText);
        add(prevButton);
        add(nextButton);
        add(Box.createRigidArea(new Dimension(5, 0)));
        add(errorLabel);
        add(Box.createHorizontalGlue());
        add(closeButton);
    }

    private static String getName(Action a) {
        Object name = a.getValue(Action.NAME);
        if (name instanceof String) {
            return (String) name;
        } else {
            return "null"; // NOI18N
        }
    }

    /**
     * Set the FindState for this panel.
     * @param state the FindState.
     */
    public void setState(FindState state) {
        this.state = state;

        // Adjust the view to reflect the model
        updating = true;
        try {
            if (state != null) {
                findText.setText(state.getPattern());
                error(state.getStatus(), false);
            } else {
                findText.setText("");
                error(FindState.Status.OK, false);
            }
        } finally {
            updating = false;
        }
    }

    public void requestTextFocus() {
        findText.requestFocus();
    }

    /**
     * Get the FindState for this panel.
     * @return the FindState.
     */
    public FindState getState() {
        return state;
    }

    private void error(FindState.Status status, boolean prevNext) {
        switch (status) {
            case OK:
                errorLabel.setText("");
                findText.setForeground(originalColor);
                break;
            case NOTFOUND:
                errorLabel.setText(Catalog.get("MSG_NotFound"));// NOI18N
                findText.setForeground(FontPanel.ERROR_COLOR);
                break;
            case WILLWRAP:
                errorLabel.setText(Catalog.get("MSG_OneMore"));	// NOI18N
                findText.setForeground(originalColor);
                break;
            case EMPTYPATTERN:
                if (prevNext)
                    errorLabel.setText(Catalog.get("MSG_Empty"));// NOI18N
                else
                    errorLabel.setText("");
                findText.setForeground(originalColor);
                break;
        }
    }

    private void close() {
        owner.close(this);
    }

    private void next() {
        if (state != null) {
            state.next();
            error(state.getStatus(), true);
        }
    }

    private void prev() {
        if (state != null) {
            state.prev();
            error(state.getStatus(), true);
        }
    }

    /*
     * We're a panel so do our own toolbar-style fly-over hiliting of buttons.
     * Why not be a toolbar?
     * Because of it's graded background which we don't want.
     */
    private void adjustButton(final JButton button) {
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);

        button.setMargin(BUTTON_INSETS);
        button.setFocusable(false);
        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setContentAreaFilled(true);
                    button.setBorderPainted(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
            }
        });
    }
}

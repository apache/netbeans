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


package org.netbeans.modules.properties;


import javax.swing.GroupLayout;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.accessibility.AccessibleContext;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.PREFERRED_SIZE;


/**
 * Panel for customizing <code>Element.ItemElem</code> element.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 * @see Element.ItemElem
 */
final class PropertyPanel extends JPanel {

    /** Element to customize. */
    private final Element.ItemElem element;

    private JTextField keyText;
    private JTextField valueText;
    private JTextField commentText;

    /**
     * Creates a new {@code PropertyPanel}.
     */
    PropertyPanel() {
        this(null);
    }

    /**
     * Creates a new {@code PropertyPanel}.
     * 
     * @param  element  element to customize, or {@code null}
     */
    PropertyPanel(Element.ItemElem element) {
        this.element = element;
        
        initComponents();
        initInteraction();
        initAccessibility();             
                
        if (element != null) {
            keyText.setText(element.getKey());
            valueText.setText(element.getValue());
            commentText.setText(element.getComment());
        }

        // Unregister Enter on text fields so default button could work.
        final KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        keyText.getKeymap().removeKeyStrokeBinding(enterKeyStroke);
        valueText.getKeymap().removeKeyStrokeBinding(enterKeyStroke);
        commentText.getKeymap().removeKeyStrokeBinding(enterKeyStroke);
        
        HelpCtx.setHelpIDString(this, Util.HELP_ID_ADDING);
    }

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(getClass(), "ACS_PropertyPanel"));                
        AccessibleContext context;
        context = keyText.getAccessibleContext();
        context.setAccessibleName(NbBundle.getMessage(getClass(), "ACSN_CTL_KeyText"));
        context.setAccessibleDescription(NbBundle.getMessage(getClass(), "ACSD_CTL_KeyText"));
        context = valueText.getAccessibleContext();
        context.setAccessibleName(NbBundle.getMessage(getClass(), "ACSN_CTL_ValueText"));
        context.setAccessibleDescription(NbBundle.getMessage(getClass(), "ACSD_CTL_ValueText"));
        context = commentText.getAccessibleContext();
        context.setAccessibleName(NbBundle.getMessage(getClass(), "ACSN_CTL_CommentText"));
        context.setAccessibleDescription(NbBundle.getMessage(getClass(), "ACSD_CTL_CommentText"));
    }
    
    // <editor-fold defaultstate="collapsed" desc="UI initialization code">
    private void initComponents() {

        JLabel keyLabel = new JLabel();
        JLabel valueLabel = new JLabel();
        JLabel commentLabel = new JLabel();

        keyText = new JTextField(25);
        valueText = new JTextField(25);
        commentText = new JTextField(25);

        keyLabel.setLabelFor(keyText);
        valueLabel.setLabelFor(valueText);
        commentLabel.setLabelFor(commentText);
        
        Mnemonics.setLocalizedText(keyLabel, NbBundle.getMessage(getClass(), "LBL_KeyLabel")); // NOI18N
        Mnemonics.setLocalizedText(valueLabel, NbBundle.getMessage(getClass(), "LBL_ValueLabel")); // NOI18N
        Mnemonics.setLocalizedText(commentLabel, NbBundle.getMessage(getClass(), "LBL_CommentLabel")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(LEADING)
                    .addComponent(keyLabel)
                    .addComponent(valueLabel)
                    .addComponent(commentLabel))
                .addPreferredGap(RELATED)
                .addGroup(layout.createParallelGroup(LEADING)
                    .addComponent(keyText, DEFAULT_SIZE, PREFERRED_SIZE, Short.MAX_VALUE)
                    .addComponent(valueText, DEFAULT_SIZE, PREFERRED_SIZE, Short.MAX_VALUE)
                    .addComponent(commentText, DEFAULT_SIZE, PREFERRED_SIZE, Short.MAX_VALUE))
                .addContainerGap()
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(BASELINE)
                    .addComponent(keyLabel)
                    .addComponent(keyText, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE))
                .addPreferredGap(RELATED)
                .addGroup(layout.createParallelGroup(BASELINE)
                    .addComponent(valueText, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
                    .addComponent(valueLabel))
                .addPreferredGap(RELATED)
                .addGroup(layout.createParallelGroup(BASELINE)
                    .addComponent(commentText, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
                    .addComponent(commentLabel))
                .addContainerGap()
        );
    }// </editor-fold>

    private void initInteraction() {
        final Listener listener = new Listener();

        keyText.addActionListener(listener);
        valueText.addActionListener(listener);
        commentText.addActionListener(listener);

        if (element != null) {
            keyText.addFocusListener(listener);
            valueText.addFocusListener(listener);
            commentText.addFocusListener(listener);
        }
    }

    private final class Listener extends FocusAdapter implements ActionListener {

        @Override
        public void focusLost(FocusEvent e) {
            storeText(e.getSource());
        }

        public void actionPerformed(ActionEvent e) {
            storeText(e.getSource());
            workaround11364();      //press the dialogue's default button
        }

        private void storeText(Object source) {
            if (element != null) {
                if (source == keyText) {
                    element.getKeyElem().setValue(keyText.getText());
                } else if (source == valueText) {
                    element.getValueElem().setValue(valueText.getText());
                } else if (source == commentText) {
                    element.getCommentElem().setValue(commentText.getText());
                } else {
                    assert false;
                }
            }
        }

    }

    String getKey() {
        return keyText.getText();
    }

    String getValue() {
        return valueText.getText();
    }

    String getComment() {
        return commentText.getText();
    }

    private void workaround11364() {
        JRootPane root = getRootPane();
        if (root != null) {
            JButton defaultButton = root.getDefaultButton();
            if (defaultButton != null) {
                defaultButton.doClick();
            }
        }
    }

}

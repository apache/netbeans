/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

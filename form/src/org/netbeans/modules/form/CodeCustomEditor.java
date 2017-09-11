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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.form;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.SimpleSection;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Custom editor for RADConnectionPropertyEditor. Allows editing the custom code
 * of a property.
 *
 * @author Tomas Pavek
 */
class CodeCustomEditor extends javax.swing.JPanel implements DocumentListener, Runnable {

    private RADConnectionPropertyEditor propertyEditor;

    private JEditorPane codePane;
    private boolean ignoreUpdate;

    private JLabel headerLabel = new JLabel();
    private JLabel footerLabel = new JLabel();
    private JTextField typeField = new JTextField();
    private JScrollPane jScrollPane = new JScrollPane() {
        // We want the editor pane's height to accommodate to the actual number
        // of lines. For that we also need to include the horizontal scrollbar
        // height into the preferred height. See also invokeUpdate method.
        @Override
        public Dimension getPreferredSize() {
            Dimension prefSize = super.getPreferredSize();
            Component hBar = getHorizontalScrollBar();
            if (hBar != null && hBar.isVisible()) {
                prefSize = new Dimension(prefSize.width, prefSize.height + hBar.getPreferredSize().height);
            }
            return prefSize;
        }
    };

    public CodeCustomEditor(RADConnectionPropertyEditor propertyEditor,
                            FormModel formModel, FormProperty property)
    {
        this.propertyEditor = propertyEditor;
        
        codePane = new JEditorPane() { // editor pane does not count the pref. height correctly
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(super.getPreferredScrollableViewportSize().width, getPrefHeight(this));
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, getPrefHeight(this));
            }
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(super.getMinimumSize().width, getPrefHeight(this));
            }
        };
        codePane.getAccessibleContext().setAccessibleName(
            NbBundle.getBundle(CodeCustomEditor.class).getString("ACSN_CustomCode_EditorPane")); //NOI18N
        headerLabel.setLabelFor(codePane);
        jScrollPane.setViewportView(codePane);
        jScrollPane.getViewport().setBackground(new Color(codePane.getBackground().getRGB()));

        int codePos = -1;
        FormDataObject dobj = FormEditor.getFormDataObject(formModel);
        EditorSupport es = dobj.getFormEditorSupport();
        GuardedSectionManager gsm = es.getGuardedSectionManager();
        SimpleSection sec = gsm.findSimpleSection(EditorSupport.SECTION_INIT_COMPONENTS);
        if ((property instanceof RADProperty) && (property.getWriteMethod() != null)) {
            RADComponent metacomp = ((RADProperty)property).getRADComponent();
            headerLabel.setText("<html>" + metacomp.getName() + ".<b>" // NOI18N
                    + property.getWriteMethod().getName() + "</b>("); // NOI18N
            footerLabel.setText(");"); // NOI18N

            String codeSnippet;
            if (metacomp != formModel.getTopRADComponent()) {
                codeSnippet = " " + metacomp.getName() + "." + property.getWriteMethod().getName() + "("; // NOI18N
            } else {
                codeSnippet = " " + property.getWriteMethod().getName() + "("; // NOI18N
            }
            codePos = sec.getText().indexOf(codeSnippet);
            if (codePos >= 0) {
                codePos += codeSnippet.length();
            }
        }
        else {
            headerLabel.setText(FormUtils.getBundleString("CodeCustomEditor.codeLabel")); // NOI18N
        }
        if (codePos < 0) {
            codePos = sec.getText().indexOf('{') + 2;
        }
        FormUtils.setupEditorPane(codePane, dobj.getPrimaryFile(), codePos + sec.getStartPosition().getOffset());

        headerLabel.setFont(codePane.getFont());
        footerLabel.setFont(codePane.getFont());
        typeField.setBorder(BorderFactory.createEmptyBorder());
        typeField.setEditable(false);
        typeField.setFont(codePane.getFont());
        typeField.setText(Utilities.getClassName(property.getValueType()));
        if (UIManager.getLookAndFeel().getID().equals("Nimbus")) { // NOI18N
            // We have to "clone" the Color because Nimbus ignores ColorUIResources
            typeField.setBackground(new Color(getBackground().getRGB()));
        }

        resetLayout();

        codePane.getDocument().addDocumentListener(this);

        // issue 103809
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ev) {
                revalidate();
                repaint();
            }
        });
    }

    private void resetLayout() {
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addComponent(headerLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addGap(3)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane, GroupLayout.PREFERRED_SIZE, 320, Short.MAX_VALUE)
                .addComponent(typeField))
            .addGap(3)
            .addComponent(footerLabel));        
        
               
        int prefHeight;
        int maxHeight;
        if(jScrollPane.getPreferredSize().getHeight() > 300) {
            prefHeight = 300;
            maxHeight = GroupLayout.DEFAULT_SIZE;
        } else {
            prefHeight = GroupLayout.DEFAULT_SIZE;
            maxHeight = GroupLayout.PREFERRED_SIZE;
        }
        
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(jScrollPane.getInsets().top)
                    .addGroup(layout.createParallelGroup().addComponent(headerLabel).addComponent(footerLabel)))
                .addComponent(jScrollPane, GroupLayout.DEFAULT_SIZE, prefHeight, maxHeight))
            .addComponent(typeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
    }
    
    void setValue(RADConnectionPropertyEditor.RADConnectionDesignValue value) {
        if (value != null && value.getType() == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_CODE) {
            ignoreUpdate = true;
            codePane.setText(value.getCode());
            ignoreUpdate = false;
            
            resetLayout();
        }
    }

    // DocumentListener
    @Override
    public void insertUpdate(DocumentEvent e) {
        invokeUpdate();
    }

    // DocumentListener
    @Override
    public void removeUpdate(DocumentEvent e) {
        invokeUpdate();
    }

    // DocumentListener
    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    private void invokeUpdate() {
        if (!ignoreUpdate) {
            ignoreUpdate = true;
            EventQueue.invokeLater(this); // set the value
            
            // also update the editor pane size according to the number of lines
            // (can't just track line count changes because the preferred height
            // also changes when the horizontal scrollbar appears/hides)
            revalidate();
            repaint();
        }
    }

    private int lineHeight = -1;
    private int getPrefHeight(JEditorPane pane) {
        if (lineHeight < 0) {
            lineHeight = pane.getFontMetrics(pane.getFont()).getHeight();
        }
        int lineCount = pane.getDocument().getDefaultRootElement().getElementCount();
        Insets ins = pane.getInsets();
        return lineHeight * lineCount + ins.top + ins.bottom;
    }

    // updates the value in the property editor
    @Override
    public void run() {
        propertyEditor.setValue(new RADConnectionPropertyEditor.RADConnectionDesignValue(codePane.getText()));
        ignoreUpdate = false;
    }
}

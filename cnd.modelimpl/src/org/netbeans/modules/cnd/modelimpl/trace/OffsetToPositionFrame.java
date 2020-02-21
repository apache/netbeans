/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.trace;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;

/**
 *
 */
public class OffsetToPositionFrame extends JFrame {
    
    private final FileImpl file;
    
    private final JTextField inputField = new JTextField(10);
    
    private final JLabel outputField = new JLabel();

    public OffsetToPositionFrame(FileImpl file) {
        this.file = file;
        Container content = getContentPane();
        content.setLayout(new BorderLayout());
        
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitter.setDividerSize(2);
        splitter.setResizeWeight(.4d);        
        splitter.setTopComponent(inputField);
        splitter.setBottomComponent(outputField);
        content.add(splitter, BorderLayout.CENTER);
        
        inputField.setMaximumSize(inputField.getPreferredSize());
        inputField.setHorizontalAlignment(SwingConstants.CENTER);
        outputField.setHorizontalAlignment(SwingConstants.CENTER);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Frame f = (Frame)e.getSource();
                f.setVisible(false);
                f.dispose();
            }
        });
        
        setListeners();
        
        setSize(200, 100);
    }
    
    private void setListeners() {
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
              convertOffset();
            }
            public void removeUpdate(DocumentEvent e) {
              convertOffset();
            }
            public void insertUpdate(DocumentEvent e) {
              convertOffset();
            }
        });
    }
    
    private void convertOffset() {
        String text = inputField.getText();
        if (text != null && !text.isEmpty()) {
            try {
                int offset = Integer.parseInt(text);
                int lineColumn[] = file.getLineColumn(offset);
                outputField.setText("" + lineColumn[0] + ":" + lineColumn[1]); // NOI18N
            } catch (NumberFormatException ex) {
                outputField.setText("Not a number!"); // NOI18N
            }
        } else {
            outputField.setText(""); // NOI18N
        }
    }
}

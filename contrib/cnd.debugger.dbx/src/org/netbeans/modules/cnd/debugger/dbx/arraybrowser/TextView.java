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

package org.netbeans.modules.cnd.debugger.dbx.arraybrowser;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

class TextView extends JPanel implements ActionListener {

    private final JLabel tvLabel;
    private final JTextField tvTextField;

    public TextView() {
        super(new FlowLayout());
        setToolTipText(Catalog.get("ToolTip_Array_Expression")); 
        tvLabel = new JLabel(Catalog.get("LBL_Array Expression"));
        tvLabel.setToolTipText(Catalog.get("ToolTip_Array_Expression")); 

        tvTextField = new JTextField(20);
        tvTextField.addActionListener(this);

        add(tvLabel);
        add(tvTextField);
    }

    public void actionPerformed(ActionEvent actionEvent) {

	String text = tvTextField.getText();

	 // Need to parse the 'text' string??

        if (text != null && text.compareTo("") != 0)
            ArrayBrowserWindow.getDefault().getArrayBrowserController().displayArray(text, "");
    }
}

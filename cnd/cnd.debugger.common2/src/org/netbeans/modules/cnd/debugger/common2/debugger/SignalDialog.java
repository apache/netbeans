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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class SignalDialog {
    private Dialog dialog;
    private DialogDescriptor dlg;
    private JPanel panel;
    private JTextArea textArea;
    private JCheckBox jc;
    private JButton BDiscardAndPause;
    private JButton BDiscardAndContinue;
    private JButton BForwardAndContinue;

    private String explanation = null;
    private String senderpid = null;
    private String session = null;
    private long receiverPid = -1;
    
    public SignalDialog() {
	panel = new JPanel();
	panel.setLayout(new GridBagLayout());
	GridBagConstraints ct = new GridBagConstraints();

	ct.gridwidth = java.awt.GridBagConstraints.REMAINDER;
	ct.anchor = java.awt.GridBagConstraints.WEST;
	ct.fill = java.awt.GridBagConstraints.BOTH;
	ct.weightx = 1.0;
	ct.weighty = 1.0;
	ct.insets = new java.awt.Insets(12, 0, 0, 12);
	
	textArea = new JTextArea();
	textArea.setWrapStyleWord(true);
	textArea.setLineWrap(true);
	textArea.setEditable(false);
	textArea.setBackground((java.awt.Color) javax.swing.UIManager.getDefaults().get("Label.background")); // NOI18N
	textArea.setRows(9);
	textArea.setColumns(40);
	textArea.setBorder(BorderFactory.createEmptyBorder());
	Catalog.setAccessibleName(textArea, "ACSN_SignalMsgArea");// NOI18N
	Catalog.setAccessibleDescription(textArea,
					 "ACSD_SignalMsgArea");	// NOI18N

	JScrollPane scrollPane = new JScrollPane();
	// scrollPane.setPreferredSize(new Dimension(300, 150));
	scrollPane.setViewportView(textArea);
	scrollPane.setBorder(BorderFactory.createEmptyBorder());
	// scrollPane.setBorder(BorderFactory.createEtchedBorder());
	panel.add(scrollPane, ct);



	ct = new GridBagConstraints();
	ct.gridwidth = java.awt.GridBagConstraints.REMAINDER;
	ct.anchor = java.awt.GridBagConstraints.WEST;
	ct.fill = java.awt.GridBagConstraints.BOTH;
	ct.weightx = 1.0;
	ct.weighty = 0.0;
	ct.insets = new java.awt.Insets(12, 0, 0, 0);

	jc = new JCheckBox();
	jc.setText(Catalog.get("SignalAddToIgnore")); // NOI18N
	Catalog.setAccessibleDescription(jc,
					 "ACSD_Signal_AddToIgnore");// NOI18N
	jc.setMnemonic(Catalog.getMnemonic("MNEM_Signal_AddToIgnore"));// NOI18N

	panel.add(jc, ct);
	jc.setSelected(false);


	BDiscardAndPause = new JButton();
	BDiscardAndPause.setText(Catalog.get("SignalDiscardAndPause")); // NOI18N
	Catalog.setAccessibleDescription(BDiscardAndPause,
					 "ACSD_Signal_DiscardAndPause");// NOI18N
	BDiscardAndPause.setMnemonic(Catalog.
	    getMnemonic("MNEM_Signal_DiscardAndPause"));	// NOI18N


	BDiscardAndContinue = new JButton();
	BDiscardAndContinue.setText(Catalog.get("SignalDiscardAndContinue")); //NOI18N
	Catalog.setAccessibleDescription(BDiscardAndContinue,
					 "ACSD_Signal_DiscardAndContinue");// NOI18N
	BDiscardAndContinue.setMnemonic(Catalog.
	    getMnemonic("MNEM_Signal_DiscardAndContinue"));	// NOI18N


	BForwardAndContinue = new JButton();
	BForwardAndContinue.setText(Catalog.get("SignalForwardAndContinue")); // NOI18N
	Catalog.setAccessibleDescription(BForwardAndContinue,
					 "ACSD_Signal_ForwardAndContinue");// NOI18N
	BForwardAndContinue.setMnemonic(Catalog.
	    getMnemonic("MNEM_Signal_ForwardAndContinue"));	// NOI18N
    }
    
    public void setSignalInfo(String explanation) {
	this.explanation = explanation;
    }

    public void setSenderInfo(String senderpid) {
	this.senderpid = senderpid;
    }

    public void setReceiverInfo(String session, long receiverPid) {
	this.session = session;
	this.receiverPid = receiverPid;
    }

    public void setIgnore(boolean signalKnow, boolean ignore) {
	jc.setEnabled(signalKnow);
	jc.setSelected(ignore);
    }
    
    public void hideIgnore() {
        jc.setVisible(false);
    }

    private void fillText() {
        StringBuilder sb = new StringBuilder();
        sb.append(explanation).append('\n');

        if (senderpid != null) {
            sb.append(Catalog.format("SignalSrc", senderpid)).append('\n'); // NOI18N
        }

        sb.append(Catalog.format("SignalDst", session, receiverPid)).append("\n\n"); // NOI18N
        sb.append(Catalog.get("SignalHelp")).append('\n'); // NOI18N
        
        // currently we do not support signals handling setup fof gdb
        //sb.append(Catalog.get("SignalConfigHint")); // NOI18N

	textArea.setText(sb.toString());
    }

    public void show() {
	fillText();
	String title = Catalog.format("SignalTitle", session); // NOI18N
	
	dlg = new DialogDescriptor(
		panel,
		title,
		true,
		new JButton [] {
		    BDiscardAndPause,
		    BDiscardAndContinue,
		    BForwardAndContinue,
		},
		BDiscardAndPause,
		DialogDescriptor.DEFAULT_ALIGN,
		null, // help context?
		null);
	dlg.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
	dialog = DialogDisplayer.getDefault().createDialog(dlg);
	Catalog.setAccessibleDescription(dialog, "ACSD_SignalCaught");	// NOI18N
	dialog.setVisible(true);
    } 

    public boolean isIgnore() {
	return jc.isSelected();
    } 

    public boolean discardSignal() {
	Object pressedButton = dlg.getValue();
	return pressedButton == BDiscardAndPause ||
	       pressedButton == BDiscardAndContinue;
    } 

    public boolean shouldContinue() {
	Object pressedButton = dlg.getValue();
	return pressedButton == BDiscardAndContinue ||
	       pressedButton == BForwardAndContinue;
    } 

}


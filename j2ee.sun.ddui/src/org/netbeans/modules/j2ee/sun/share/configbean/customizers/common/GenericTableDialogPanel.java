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
/*
 * GenericTableDialogPanel.java
 *
 * Created on October 8, 2003, 1:47 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;

import org.netbeans.modules.j2ee.sun.share.Constants;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;

/**
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class GenericTableDialogPanel extends JPanel implements GenericTableDialogPanelAccessor {

	/** resource bundle */
	private final ResourceBundle bundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N		

	private List fieldEntries;
	private String [] values;
	private JTextField [] textFields;
	private int preferredWidth;

	/** Creates generic field entry panel using unvalidated JTextFields for all 
	 *  inputs.
	 */
	public GenericTableDialogPanel() {
	}
	
	public void init(ASDDVersion asVersion, int width, List entries, Object data) {
		// data field not used in generic dialog.	
		fieldEntries = entries;
		preferredWidth = width;
		values = new String [entries.size()];
		textFields = new JTextField [entries.size()];

		initUserComponents();
	}
	
	public void setValues(Object[] v) {
		if(v != null && v.length == values.length) {
			for(int i = 0; i < values.length && i < v.length; i++) {
				values[i] = (v[i] != null) ? v[i].toString() : "";	// NOI18N
			}
		} else {
			if(v != null) {
				assert (v.length == values.length);	// Should fail
			}
			
			// default values
			for(int i = 0; i < values.length; i++) {
				values[i] = "";	// NOI18N
			}
		}

		setComponentValues();
	}
	
	public Object [] getValues() {
		return values;
	}
	
	private void setComponentValues() {
		for(int i = 0; i < values.length; i++) {
			textFields[i].setText(values[i]);
		}
	}

	private void initUserComponents() {
        GridBagConstraints gridBagConstraints;
		
		// panel parameters
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(new Insets(5, 5, 0, 5)));
        setPreferredSize(new Dimension(preferredWidth, 22*fieldEntries.size()+8));
		
		for(int i = 0; i < fieldEntries.size(); i++) {
			GenericTableModel.TableEntry entry = (GenericTableModel.TableEntry) fieldEntries.get(i);
			JLabel requiredMark = new JLabel();
			JLabel label = new JLabel();
			textFields[i] = new JTextField();

			// First control is either empty label or '*' label to mark required
			// field.
			if(entry.isRequiredField()) {
				requiredMark.setText(bundle.getString("LBL_RequiredMark"));	// NOI18N
				requiredMark.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_RequiredMark"));	// NOI18N
				requiredMark.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_RequiredMark"));	// NOI18N
			}
			requiredMark.setLabelFor(textFields[i]);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.insets = new Insets(0, 0, 4, 4);
			add(requiredMark, gridBagConstraints);
			
			// Initialize and add label
			label.setLabelFor(textFields[i]);
			label.setText(entry.getLabelName());	// NOI18N
			label.setDisplayedMnemonic(entry.getLabelMnemonic());
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.insets = new Insets(0, 0, 4, 4);
			add(label, gridBagConstraints);

			// Initialize and add text field
			textFields[i].addKeyListener(new TextFieldHandler(textFields[i], i));
			textFields[i].getAccessibleContext().setAccessibleName(entry.getAccessibleName());
			textFields[i].getAccessibleContext().setAccessibleDescription(entry.getAccessibleDescription());

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.insets = new Insets(0, 0, 4, 0);
			add(textFields[i], gridBagConstraints);
		}	
	}
	
	public Collection getErrors(ValidationSupport validationSupport) {
		ArrayList errorList = new ArrayList();
		
		// Only record one error per field.
		for(int i = 0; i < fieldEntries.size(); i++) {
			GenericTableModel.TableEntry entry = (GenericTableModel.TableEntry) fieldEntries.get(i);
			if(entry.isRequiredField()) {
				if(!Utils.notEmpty(values[i])) {
					Object [] args = new Object [1];
					args[0] = entry.getColumnName();
					errorList.add(MessageFormat.format(bundle.getString("ERR_SpecifiedFieldIsEmpty"), args));
					continue;
				}
			}
			
			if(entry.isNameField()) {
				if(Utils.containsWhitespace(values[i])) {
					Object [] args = new Object [1];
					args[0] = entry.getColumnName();
					errorList.add(MessageFormat.format(bundle.getString("ERR_NameFieldContainsWhitespace"), args));
					continue;
				}
			}

                        // Validate that this field is an integer value.  Empty is acceptable
                        // as well (use required field flag to force non-empty fields.)
//			if(entry.isIntegerField()) {
//				if(Utils.notInteger(values[i])) {
//					Object [] args = new Object [1];
//					args[0] = entry.getColumnName();
//					errorList.add(MessageFormat.format(bundle.getString("ERR_FieldMustBeInteger"), args));
//					continue;
//				}
//			}
		}
		
		return errorList;
	}

	public boolean requiredFieldsFilled() {
		boolean result = true;
		
		for(int i = 0; i < fieldEntries.size(); i++) {
			GenericTableModel.TableEntry entry = (GenericTableModel.TableEntry) fieldEntries.get(i);
			if(entry.isRequiredField()) {
				if(!Utils.notEmpty(values[i])) {
					result = false;
					break;
				}
			}
		}
		
		return result;
	}
	
	/** private class to allow easy binding between each text field in the array
	 *  of JTextFields and the corresponding String in the array of values entered
	 *  by the user.
	 */
	private class TextFieldHandler extends KeyAdapter {
		private JTextField textField;
		private int controlIndex;
		
		public TextFieldHandler(JTextField tf, int index) {
			textField = tf;
			controlIndex = index;
		}
		
		public void keyReleased(KeyEvent evt) {
			values[controlIndex] = textField.getText();
			firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
		}		
	}
}

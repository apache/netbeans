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

/**
 * NameValueCellEditor.java
 *
 *
 * Created: Thursday Feb  15
 *
 * @author Simran Gleason
 * @author Ana von Klopp
 * @version
 */


package org.netbeans.modules.web.monitor.client;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.*;
import java.util.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;
import org.openide.util.HelpCtx;

import org.openide.util.NbBundle;

/* Should this one get the events from the Param editor? MAO, far
 * Param editorn vara non-modal? */
public class NameValueCellEditor extends DefaultCellEditor  {

    private final static boolean debug = false;
    private static String editNameAndValueTitle;
    private static String editValueOnlyTitle;

    private JTable table;
    private Object[][] data;
    private boolean nameEditable;
    private int row;
    private int type;

    public static NameValueCellEditor createCellEditor(JTable table,
						       Object data [][],
						       boolean nameEditable,
						       int row, final int type)  {

	JButton b = new JButton(NbBundle.getMessage(NameValueCellEditor.class, "MON_Edit_dots")); // NOI18N
	if(type == DisplayTable.UNEDITABLE) 
	    b.setToolTipText(NbBundle.getMessage(NameValueCellEditor.class, "MON_DisplayValue")) ;
	else 
	    b.setToolTipText(NbBundle.getMessage(NameValueCellEditor.class, "MON_EditAttribute")) ;
	final NameValueCellEditor ed = new NameValueCellEditor(b,
							       table,
							       data,
							       nameEditable,
							       row, type);

	b.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		ed.showParamEditor();
	    }
	});

	return ed;
    }

					
    public NameValueCellEditor(JButton b,
			       JTable table,
			       Object data [][],
			       boolean nameEditable,
			       int row, 
			       int type)  {
	super(new JCheckBox());
	editorComponent = b;
	setClickCountToStart(1); 

	this.table = table;
	this.data = data;
	this.nameEditable = nameEditable;
	this.row = row;    
	this.type = type;
    }

    
    protected void fireEditingStopped() {
	super.fireEditingStopped();
    }
    
    public Object getCellEditorValue() {
	return NbBundle.getMessage(NameValueCellEditor.class, "MON_Edit_dots");
    }
 
    public Component getTableCellEditorComponent(JTable table, 
						 Object value,
						 boolean isSelected,
						 int row,
						 int column) {
	((JButton)editorComponent).setText(value.toString());
	return editorComponent;
    }


    public void showParamEditor() {

	int currentRow = table.getSelectedRow();
	TableModel model = table.getModel();
	String name =  (String)model.getValueAt(currentRow, 0);
	String value = (String)model.getValueAt(currentRow, 1);

	ParamEditor.Condition condition = ParamEditor.Condition.NONE; 
	ParamEditor.Editable editable = ParamEditor.Editable.BOTH; 
	String title = null; 
	
	if(debug) 
	    System.out.println("type = " + String.valueOf(type)); //NOI18N

	if(type == DisplayTable.UNEDITABLE) {
	    editable = ParamEditor.Editable.NEITHER;
	    title = NbBundle.getMessage(NameValueCellEditor.class, 
					"MON_ParamValue"); 
	}
	else if(type == DisplayTable.HEADERS) {
	    title = NbBundle.getMessage(NameValueCellEditor.class, 
					"MON_Edit_header"); 
	    condition = ParamEditor.Condition.HEADER; 
	}
	else if(type == DisplayTable.PARAMS) 
	    title = NbBundle.getMessage(NameValueCellEditor.class, 
					"MON_Edit_param");  
	else if(type == DisplayTable.REQUEST) {
	    editable = ParamEditor.Editable.VALUE;
	    title = NbBundle.getMessage(NameValueCellEditor.class, 
					"MON_Edit_request"); 
	    condition = ParamEditor.Condition.VALUE;
	}
	else if(type == DisplayTable.COOKIES) {
	    title = NbBundle.getMessage(NameValueCellEditor.class, 
					"MON_Edit_cookie"); 
	    condition = ParamEditor.Condition.COOKIE; 
	}
	else if(type == DisplayTable.SERVER) {
	    title = NbBundle.getMessage(NameValueCellEditor.class, 
					"MON_Edit_server"); 
	    condition = ParamEditor.Condition.VALUE; 
	    editable = ParamEditor.Editable.VALUE;
	}
	// This should not happen
	else 
	    title = NbBundle.getMessage(NameValueCellEditor.class, "MON_Edit_value"); 
	


	ParamEditor pe = new ParamEditor(name, value, editable, condition,
					 title); 

	pe.showDialog(); 

	if(debug) 
	    System.out.println("NameValueCellEditor::has " + //NOI18N
			       pe.getName() + " " + pe.getValue());//NOI18N

	if ((type > DisplayTable.UNEDITABLE) && pe.getDialogOK()) {
	    if(debug) System.out.println("Updating the model");//NOI18N
	    
	    if (nameEditable) {
		model.setValueAt(pe.getName(), currentRow, 0);
		if(debug) System.out.println("Updated the name");//NOI18N
	    }
	    model.setValueAt(pe.getValue(), currentRow, 1);
	    if(debug) System.out.println("Updated the value");//NOI18N
	}
    }
} // NameValueCellEditor

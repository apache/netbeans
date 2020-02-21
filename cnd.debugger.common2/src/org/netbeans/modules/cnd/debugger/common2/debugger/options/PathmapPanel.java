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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import java.util.ArrayList;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.util.HelpCtx;

import org.openide.explorer.propertysheet.PropertyEnv;

import org.netbeans.modules.cnd.debugger.common2.utils.ListUI;

class PathmapPanel extends JPanel implements PropertyChangeListener, HelpCtx.Provider {
    private Pathmap pathmap;
    private ListUI pathmapList;
    private PropertyEditorSupport propertyEditor;

    /** Creates new form CustomizerCompile */
    public PathmapPanel(PropertyEditorSupport propertyEditor,
			PropertyEnv env,
			Pathmap pathmap) {
        this.propertyEditor = propertyEditor;
        initComponents();                

	adjustButtons();

	// Arrange for propertyChange to get called when OK is pressed.
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);

	this.pathmap = pathmap;

	initValues(pathmap);
    }
    
    /**
     * was: part of RunConfigDialog.configToGui()
     */

    private void initValues(Pathmap pathmap) {
	this.pathmap = pathmap;

	if (pathmap.getPathmap() != null) {
	    int n = pathmap.getPathmap().length;
	    ArrayList<String> col0 = new ArrayList<String>(n+3); // Leave slop for inserts
	    ArrayList<String> col1 = new ArrayList<String>(n+3);
	    for (int i = 0; i < n; i++) {
		col0.add(pathmap.getPathmap()[i].from());
		if (pathmap.getPathmap()[i].to() == null) {
		    col1.add(""); // NOI18N
		} else {
		    col1.add(pathmap.getPathmap()[i].to());
		}
	    }
	    pathmapList.model.setData(n, col0, col1);
	}
    } 


    /**
     * Transfer information from GUI to profile object.
     */
    private Object getPropertyValue() throws IllegalStateException {
	pathmapList.model.finishEditing();

	int numRows = pathmapList.model.getRowCount();
	if (numRows > 0) {
	    // Do real count (ignore empty rows)
	    int num = 0;
	    for (int j = 0; j < numRows; j++) {
		String from = (String) pathmapList.model.getValueAt(j, 0);
		if (from.length() == 0)
		    continue;
		String to = (String) pathmapList.model.getValueAt(j, 1);
		// Empty right hand side is legal
		num++;
	    }
	    
	    Pathmap.Item [] map = new Pathmap.Item[num];
	    num = 0;
	    for (int j = 0; j < numRows; j++) {
		String from = (String) pathmapList.model.getValueAt(j, 0);
		if (from.length() == 0)
		    continue;
		String to = (String) pathmapList.model.getValueAt(j, 1);
		if (to.length() == 0) {
		    to = null;
		}
		Pathmap.Item p = new Pathmap.Item(from, to, false);
		map[num++] = p;
	    }
	    pathmap.setPathmap(map);
	} else {
	    pathmap.setPathmap(null);
	}
	return pathmap;
    }
        
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) &&
	    evt.getNewValue() == PropertyEnv.STATE_VALID) {

	    // OK was pressed
            propertyEditor.setValue(getPropertyValue());
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
	return new HelpCtx("DebuggerOptions" );
    }
    

    private void initComponents() {
        GridBagConstraints gbc;
        setLayout(new GridBagLayout());

	Insets insets = new Insets(12, 12, 12, 12);
	Border margin = new EmptyBorder(insets);
	Border border = new CompoundBorder(new EtchedBorder(), margin);
	setBorder(border);

	pathmapList = new ListUI();
	pathmapList.labelText = Catalog.get("Pathmap");		// NOI18N
	pathmapList.labelMnemonic =
	    Catalog.getMnemonic("MNEM_Pathmap");		// NOI18N
	pathmapList.column0Text = Catalog.get("ExistingPath");	// NOI18N
	pathmapList.column1Text = Catalog.get("ReplacementPath");// NOI18N
	pathmapList.accessibleDescription =
	    Catalog.get("ACSD_PathmapTable");			// NOI18N
	pathmapList.addText = Catalog.get("AddMap");		// NOI18N
	pathmapList.addActionListener = new ActionListener() {
            @Override
	    public void actionPerformed(ActionEvent evt) {
		addPathmapRow(evt);
	    }
	};
	pathmapList.remText = Catalog.get("RemoveMap");		// NOI18N
	pathmapList.remActionListener = new ActionListener() {
            @Override
	    public void actionPerformed(ActionEvent evt) {
		deletePathmapRow(evt);
	    }
	};

	JPanel pathmapPanel = pathmapList.make(true);

	gbc = new GridBagConstraints();
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.insets = new Insets(11, 0, 0, 0);
	this.add(pathmapPanel, gbc);
    }

    private void adjustButtons() {
	pathmapList.adjustButtons(false);
    }

    private void deletePathmapRow(ActionEvent evt) {
	int[] selRows = pathmapList.table.getSelectedRows();
	if ((selRows != null) && (selRows.length > 0)) {
	    pathmapList.model.removeRows(selRows);
	    adjustButtons();
	}
    }

    private void addPathmapRow(ActionEvent evt) {
	pathmapList.model.addRow();
	adjustButtons();
    }
}

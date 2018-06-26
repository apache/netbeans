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

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;


import org.netbeans.modules.j2ee.sun.share.Constants;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public abstract class BeanInputDialog extends InputDialog {

	/** This is the property that child dialog panels should indicate has changed
	 *  when they want the parent dialog (this object) to update the error status.
	 */
	public static final String USER_DATA_CHANGED = Constants.USER_DATA_CHANGED;	// NOI18N
	
	private final ResourceBundle bundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle"); //NOI18N

	// Owner & child panels
	private JPanel parentPanel;
	private JPanel dialogPanel;
	private boolean editmode;
	
	// Validation support
	protected ValidationSupport validationSupport;;

	/** Creates a new instance of BeanInputDialog */
	public BeanInputDialog(JPanel parent, String title, Object[] values) {
		this(parent, title, false, values);
	}

	public BeanInputDialog(JPanel parent, String title, boolean showRequiredNote, Object[] values) {
		super(parent, title, showRequiredNote);

		editmode = true;
		parentPanel = parent;
		dialogPanel = getDialogPanel(values);
		validationSupport = new ValidationSupport();
		
		initComponents();
	}

	/** Creates a new instance of BeanInputDialog */
	public BeanInputDialog(JPanel parent, String title) {
		this(parent, title, false);
	}

	public BeanInputDialog(JPanel parent, String title, boolean showRequiredNote) {
		super(parent, title, showRequiredNote);
		editmode = false;
		parentPanel = parent;
		dialogPanel = getDialogPanel();
		validationSupport = new ValidationSupport();
		
		initComponents();
	}

	private void initComponents() {
            
		// Add the content panel
		getContentPane().add(dialogPanel, BorderLayout.CENTER);
		
		// add user data listener
		addListeners();

		//adjust size of dialog based on parent panel
		adjustSize();

		// reorder controls
		pack();
		
		// set proper location
		setLocationInside(parentPanel);
		
		// Display any initial errors (mostly likely related to empty fields with
		// no defaults or possible duplicate entry based on defaults that conflict
		// with an existing item.
		handleErrorDisplay(); 
	}

	//this method adjust the size of the size of this dialog based on parent
	//panel width  and no of elements in this dialog
	private void adjustSize(){
		int preferredWidth = parentPanel.getWidth()*3/4;
		int noOfFields = getNOofFields();
		if(-1 != noOfFields){
            ///dialogPanel.setBorder(new EmptyBorder(new Insets(5, 5, 0, 5)));
            //dialogPanel.setPreferredSize(new Dimension(preferredWidth,
                //(int)dialogPanel.getPreferredSize().getHeight()));
            Dimension dm =  getContentPane().getPreferredSize();
            int preferredHeight = (int)dialogPanel.getPreferredSize().getHeight();

            if(preferredWidth < dm.getWidth()) {
                preferredWidth = (int)dm.getWidth();
            }
            if(preferredHeight < dm.getHeight()) {
                preferredHeight = (int)dm.getHeight();
            }
		}
	};

	// method to get the number of elements in this dialog
	protected int getNOofFields() {
		return -1;
	};

	protected abstract Object[] getValues();

	protected abstract Collection getErrors();

	protected abstract JPanel getDialogPanel();

	protected abstract JPanel getDialogPanel(Object[] values);
	
    @Override
	abstract protected String getHelpId();

	// Handle change events from child panel
	private void addListeners() {
		dialogPanel.addPropertyChangeListener(USER_DATA_CHANGED, new PropertyChangeListener() {
            @Override
			public void propertyChange(PropertyChangeEvent evt) {
				handleErrorDisplay();
			}
		});
	}

	private void handleErrorDisplay() {
		// Check for duplicate entry, then check for other errors
		ArrayList errors = new ArrayList();
		
		if(hasDuplicateEntry()) {
			errors.add(bundle.getString("ERR_ObjectIsDuplicate"));
		} else {
			errors.addAll(getErrors());
		}
		
		setErrors(errors);
	}
	
	protected boolean hasDuplicateEntry() {
		boolean result = false;
		
		if(parentPanel instanceof BeanTablePanel) {
			BeanTablePanel beanPanel = (BeanTablePanel) parentPanel;
			BeanTableModel model = beanPanel.getModel();
			Object [] newValues = getValues();
			
			if(!editmode) {
				// New item mode is easy
				if(model.alreadyExists(newValues)) {
					result = true;
				}
			} else {
				// Edit mode is more difficult.  The item is allowed to match
				// the original item.  But it is not allowed to match any other
				// item.  What is a clean way to do this?  Only the model knows
				// which are the key fields.
//				if(model.alreadyExists(newValues) && !original) {
//					result = true;
//				}
			}
		}
	
		return result;
	}

    protected BeanTableModel getModel(){
        BeanTableModel model = null;
        if(parentPanel instanceof BeanTablePanel) {
            BeanTablePanel beanPanel = (BeanTablePanel) parentPanel;
            model = beanPanel.getModel();
        }
        return model;
    }
}

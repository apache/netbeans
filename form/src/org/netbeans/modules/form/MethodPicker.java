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

import java.beans.*;
import java.util.*;

import org.openide.awt.Mnemonics;

/** The MethodPicker is a form which allows user to pick one of methods
 * with specified required return type.
 *
 * @author  Ian Formanek
 */
public class MethodPicker extends javax.swing.JPanel {

    static final long serialVersionUID =7355140527892160804L;
    /**
     * Initializes the Form.
     * 
     * @param formModel form model.
     * @param componentToSelect component whose methods should be displayed.
     * @param requiredType required return type of the method.
     */
    public MethodPicker(FormModel formModel, RADComponent componentToSelect, Class requiredType) {
        this.requiredType = requiredType;
        initComponents();

        java.util.List<RADComponent> componentsList = formModel.getComponentList();
        Collections.sort(componentsList, new ParametersPicker.ComponentComparator());
        components = new RADComponent[componentsList.size()];
        componentsList.toArray(components);

        int selIndex = -1;
        for (Iterator it = componentsList.iterator(); it.hasNext(); ) {
            RADComponent radComp = (RADComponent) it.next();
            if (componentToSelect != null && componentToSelect == radComp)
                selIndex = componentsCombo.getItemCount();
            if (radComp == formModel.getTopRADComponent())
                componentsCombo.addItem(
                    FormUtils.getBundleString("CTL_FormTopContainerName")); // NOI18N
            else
                componentsCombo.addItem(radComp.getName());
        }
        if (selIndex >= 0)
            componentsCombo.setSelectedIndex(selIndex);

        updateMethodList();

        Mnemonics.setLocalizedText(componentLabel, FormUtils.getBundleString("CTL_CW_Component")); // NOI18N
        Mnemonics.setLocalizedText(listLabel, FormUtils.getBundleString("CTL_CW_MethodList")); // NOI18N

        componentsCombo.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_Component")); // NOI18N
        methodList.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_MethodList")); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_MethodPicker")); // NOI18N
    }

    public boolean isPickerValid() {
        return pickerValid;
    }
    
    private void setPickerValid(boolean v) {
        boolean old = pickerValid;
        pickerValid = v;
        firePropertyChange("pickerValid", old, pickerValid); // NOI18N
    }

    RADComponent getSelectedComponent() {
        return selectedComponent;
    }

    void setSelectedComponent(RADComponent selectedComponent) {
        if (selectedComponent != null)
            componentsCombo.setSelectedItem(selectedComponent.getName());
    }

    MethodPickerItem getSelectedMethod() {
        if ((selectedComponent == null) ||(methodList.getSelectedIndex() == -1))
            return null;
        return items [methodList.getSelectedIndex()];
    }

    void setSelectedMethod(MethodDescriptor selectedMethod) {
        if (selectedMethod == null) {
            methodList.setSelectedIndex(-1);
        } else {
            methodList.setSelectedValue(FormUtils.getMethodName(selectedMethod), true);
        }
    }

    // ----------------------------------------------------------------------------
    // private methods

    private void updateMethodList() {
        RADComponent sel = getSelectedComponent();
        if (sel == null) {
            methodList.setListData(new Object [0]);
            methodList.revalidate();
            methodList.repaint();
        } else {
	    MethodDescriptor[] descs;	    
	
	    descs = sel.getBeanInfo().getMethodDescriptors();	

	    Map<String,MethodPickerItem> filtered = new HashMap<String,MethodPickerItem>();
	    for (int i = 0; i < descs.length; i ++) {
		if (requiredType.isAssignableFrom(descs[i].getMethod().getReturnType()) &&
		    (descs[i].getMethod().getParameterTypes().length == 0)) // [FUTURE: - currently we allow only methods without params]
		{
		    MethodPickerItem item = createItem(descs[i]);
		    filtered.put(item.getMethodName(), item);
		}
	    }
	    
	    if(sel == sel.getFormModel().getTopRADComponent() ) {
		String[] names = FormEditor.getFormJavaSource(sel.getFormModel()).getMethodNames(requiredType);
		for (int i = 0; i < names.length; i++) {		    
		    MethodPickerItem item = createItem(names[i]);
		    if(!filtered.containsKey(item.getMethodName())){
			filtered.put(item.getMethodName(), item);		    
		    }		    
		}		
	    } 
	    	    
	    items = new MethodPickerItem[filtered.size()];
	    filtered.values().toArray(items);
	    
            // sort the methods by name
            Arrays.sort(items, new Comparator<MethodPickerItem>() {
                @Override
                public int compare(MethodPickerItem o1, MethodPickerItem o2) {
                    return o1.getMethodName().compareTo(o2.getMethodName());
                }
            });

	    String[] listItems = new String [items.length];
	    for (int i = 0; i < listItems.length; i++)
		listItems[i] = items[i].getMethodName();
	    
            methodList.setListData(listItems);
            methodList.revalidate();
            methodList.repaint();
        }
    }

    private MethodPickerItem createItem(final MethodDescriptor desc) {
	return new MethodPickerItem() {		
	    private String name = FormUtils.getMethodName(desc);
            @Override
	    public String getMethodName() {
		return name;
	    }
            @Override
	    public Class[] getParameterTypes() {
		return desc.getMethod().getParameterTypes();
	    }
            @Override
	    public MethodDescriptor getMethodDescriptor() {
		return desc;
	    }
	};
    }

    private MethodPickerItem createItem(final String methodName) {
	return new MethodPickerItem() {			
	    private String name = FormUtils.getMethodName(methodName, NO_PARAMETERS);
            @Override
	    public String getMethodName() {
		return name;
	    }
            @Override
	    public Class[] getParameterTypes() {
		return NO_PARAMETERS;
	    }
            @Override
	    public MethodDescriptor getMethodDescriptor() {
		return null;
	    }	
	};
    }

    private void updateState() {
        if ((getSelectedComponent() == null) || (getSelectedMethod() == null)) {
            setPickerValid(false);
        } else {
            setPickerValid(getSelectedMethod().getParameterTypes().length == 0);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        componentLabel = new javax.swing.JLabel();
        componentsCombo = new javax.swing.JComboBox();
        listLabel = new javax.swing.JLabel();
        propertiesScrollPane = new javax.swing.JScrollPane();
        methodList = new javax.swing.JList();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 0, 11));
        setLayout(new java.awt.GridBagLayout());

        componentLabel.setLabelFor(componentsCombo);
        componentLabel.setText("Component:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 6);
        add(componentLabel, gridBagConstraints);

        componentsCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                componentsComboItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 128;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(componentsCombo, gridBagConstraints);

        listLabel.setLabelFor(methodList);
        listLabel.setText("Methods");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(listLabel, gridBagConstraints);

        methodList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        methodList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                methodListValueChanged(evt);
            }
        });
        propertiesScrollPane.setViewportView(methodList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(propertiesScrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void methodListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_methodListValueChanged
        updateState();
    }//GEN-LAST:event_methodListValueChanged

    private void componentsComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_componentsComboItemStateChanged
        if (componentsCombo.getSelectedIndex() == -1)
            selectedComponent = null;
        else
            selectedComponent = components[componentsCombo.getSelectedIndex()];
        updateMethodList();
    }//GEN-LAST:event_componentsComboItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel componentLabel;
    private javax.swing.JComboBox componentsCombo;
    private javax.swing.JLabel listLabel;
    private javax.swing.JList methodList;
    private javax.swing.JScrollPane propertiesScrollPane;
    // End of variables declaration//GEN-END:variables

    private boolean pickerValid = false;

    interface MethodPickerItem {
	public String getMethodName();
	public Class[] getParameterTypes();		
	public MethodDescriptor getMethodDescriptor();
    }
    
    private RADComponent[] components;
    private Class<?> requiredType;
    private MethodPickerItem[] items;
    private RADComponent selectedComponent;
    private static Class[] NO_PARAMETERS = new Class[0];	

}

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

/** The PropertyPicker is a form which allows user to choose from property set
 * of specified required class.
 *
 * @author  Ian Formanek
 */
public class PropertyPicker extends javax.swing.JPanel {

    public static final int CANCEL = 0;
    public static final int OK = 1;

    static final long serialVersionUID =5689122601606238081L;
    
    /**
     * Initializes the Form.
     * 
     * @param formModel form model.
     * @param componentToSelect component whose property should be selected.
     * @param requiredType required type of the property.
     */
    public PropertyPicker(FormModel formModel, RADComponent componentToSelect, Class requiredType) {
        this.requiredType = requiredType;
        initComponents();

        java.util.List<RADComponent> componentsList = formModel.getComponentList();
        Collections.sort(componentsList, new ParametersPicker.ComponentComparator());
        components = new RADComponent[componentsList.size()];
        componentsList.toArray(components);

        int selIndex = -1;
        for (Iterator<RADComponent> it = componentsList.iterator(); it.hasNext(); ) {
            RADComponent radComp = it.next();
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

        propertyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        updatePropertyList();

        // localize components
        Mnemonics.setLocalizedText(componentLabel, FormUtils.getBundleString("CTL_CW_Component")); // NOI18N
        Mnemonics.setLocalizedText(listLabel, FormUtils.getBundleString("CTL_CW_PropertyList")); // NOI18N

        componentsCombo.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_Component")); // NOI18N
        propertyList.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_PropertyList")); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_PropertyPicker")); // NOI18N

//        HelpCtx.setHelpIDString(this, "gui.connecting.code"); // NOI18N
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

    PropertyPickerItem getSelectedProperty() {
        if ((selectedComponent == null) ||(propertyList.getSelectedIndex() == -1))
            return null;
        return items [propertyList.getSelectedIndex()];
    }

    void setSelectedProperty(PropertyDescriptor selectedProperty) {
        if (selectedProperty == null) {
            propertyList.setSelectedIndex(-1);
        } else {
            propertyList.setSelectedValue(selectedProperty.getName(), true);
        }
    }
    // ----------------------------------------------------------------------------
    // private methods

    private void updatePropertyList() {	
        RADComponent sel = getSelectedComponent();
        if (sel == null) {
            propertyList.setListData(new Object [0]);
            propertyList.revalidate();
            propertyList.repaint();
        } else {
            PropertyDescriptor[] descs = sel.getBeanInfo().getPropertyDescriptors();
            Map<String,PropertyPickerItem> filtered = new HashMap<String,PropertyPickerItem>();
            for (int i = 0; i < descs.length; i ++) {
                if ((descs[i].getReadMethod() != null) &&       // filter out non-readable properties
                    (descs[i].getPropertyType() != null) &&  // indexed properties return null from getPropertyType
                    requiredType.isAssignableFrom(descs[i].getPropertyType())) {
		    PropertyPickerItem item = createItem(descs[i]);
                    filtered.put(item.getPropertyName(), item);
                }
            }
	    
	    if(sel == sel.getFormModel().getTopRADComponent() ) {
		String[] names = FormEditor.getFormJavaSource(sel.getFormModel()).getPropertyReadMethodNames(requiredType);
		for (int i = 0; i < names.length; i++) {
		    PropertyPickerItem item = createItem(names[i]);
		    if(!filtered.keySet().contains(item.getPropertyName())){
			filtered.put(item.getPropertyName(), item);	
		    }                    
		}		
	    } 
	    
	    items = new PropertyPickerItem[filtered.size()];
            filtered.values().toArray(items);	    

            // sort the properties by name
            Arrays.sort(items, new Comparator<PropertyPickerItem>() {
                @Override
                public int compare(PropertyPickerItem o1, PropertyPickerItem o2) {
                    return o1.getPropertyName().compareTo(o2.getPropertyName());
                }
            });
            
	    String[] listItems = new String [items.length];
            for (int i = 0; i < listItems.length; i++)
                listItems[i] = items[i].getPropertyName();
	    
            propertyList.setListData(listItems);
            propertyList.revalidate();
            propertyList.repaint();
        }
    }

    private PropertyPickerItem createItem(final PropertyDescriptor desc) {
	return new PropertyPickerItem() {
            @Override
	    public String getPropertyName() {
		return desc.getName();
	    }
            @Override
	    public String getReadMethodName() {
		return desc.getReadMethod().getName();
	    }
            @Override
	    public PropertyDescriptor getPropertyDescriptor() {
		return desc;
	    }
	};		
    }
	    
    private PropertyPickerItem createItem(final String name) {
	return new PropertyPickerItem() {
            @Override
	    public String getPropertyName() {
		return FormJavaSource.extractPropertyName(name);
	    }
            @Override
	    public String getReadMethodName() {
		return FormUtils.getMethodName(name, NO_PARAMETERS);
	    }
            @Override
	    public PropertyDescriptor getPropertyDescriptor() {
		return null;
	    }
	};		
    }

    private void updateState() {
        setPickerValid((getSelectedComponent() != null) &&(getSelectedProperty() != null));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        componentsCombo = new javax.swing.JComboBox();
        propertiesScrollPane = new javax.swing.JScrollPane();
        propertyList = new javax.swing.JList();
        componentLabel = new javax.swing.JLabel();
        listLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 0, 11));
        setLayout(new java.awt.GridBagLayout());

        componentsCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                componentsComboItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 128;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(componentsCombo, gridBagConstraints);

        propertyList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                propertyListValueChanged(evt);
            }
        });
        propertiesScrollPane.setViewportView(propertyList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(propertiesScrollPane, gridBagConstraints);

        componentLabel.setLabelFor(componentsCombo);
        componentLabel.setText("Component:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 6);
        add(componentLabel, gridBagConstraints);

        listLabel.setLabelFor(propertyList);
        listLabel.setText("Properties");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(listLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    private void propertyListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_propertyListValueChanged
        updateState();
    }//GEN-LAST:event_propertyListValueChanged

    private void componentsComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_componentsComboItemStateChanged
        if (componentsCombo.getSelectedIndex() == -1)
            selectedComponent = null;
        else
            selectedComponent = components[componentsCombo.getSelectedIndex()];
        updatePropertyList();
    }//GEN-LAST:event_componentsComboItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel componentLabel;
    private javax.swing.JComboBox componentsCombo;
    private javax.swing.JLabel listLabel;
    private javax.swing.JScrollPane propertiesScrollPane;
    private javax.swing.JList propertyList;
    // End of variables declaration//GEN-END:variables

    private boolean pickerValid = false;

    private RADComponent[] components;
    private Class<?> requiredType;
    private PropertyPickerItem[] items;
    private RADComponent selectedComponent;    
    private static Class[] NO_PARAMETERS = new Class[0];	

    interface PropertyPickerItem {
	public String getPropertyName();
	public String getReadMethodName();
	public PropertyDescriptor getPropertyDescriptor();
    }

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.installer.wizard.components.panels;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiDirectoryChooser;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiList;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.NbiTextField;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class ApplicationLocationPanel extends ErrorMessagePanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ApplicationLocationPanel() {
        setProperty(LOCATION_LABEL_TEXT_PROPERTY, 
                DEFAULT_LOCATION_LABEL_TEXT);
        setProperty(LOCATION_BUTTON_TEXT_PROPERTY, 
                DEFAULT_LOCATION_BUTTON_TEXT);
        setProperty(LIST_LABEL_TEXT_PROPERTY, 
                DEFAULT_LIST_LABEL_TEXT);        
        
        setProperty(ERROR_NOTHING_FOUND_PROPERTY, 
                DEFAULT_ERROR_NOTHING_FOUND);
    }
    
    public abstract List<File> getLocations();
    
    public abstract List<String> getLabels();
    
    public abstract File getSelectedLocation();
    
    public abstract String validateLocation(String value);
    
    public abstract void setLocation(File location);
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new ApplicationLocationPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class ApplicationLocationPanelUi extends ErrorMessagePanelUi {
        protected ApplicationLocationPanel component;
        
        public ApplicationLocationPanelUi(final ApplicationLocationPanel component) {
            super(component);
            
            this.component = component;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new ApplicationLocationPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class ApplicationLocationPanelSwingUi extends ErrorMessagePanelSwingUi {
        private ApplicationLocationPanel component;
        
        private NbiLabel locationLabel;
        private NbiTextField locationField;
        private NbiButton locationButton;
        
        private NbiLabel locationsLabel;
        private NbiList locationsList;
        private NbiScrollPane locationsScrollPane;
        
        private NbiPanel locationsListReplacement;
        
        private NbiDirectoryChooser fileChooser;
        
        public ApplicationLocationPanelSwingUi(
                final ApplicationLocationPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        @Override
        public JComponent getDefaultFocusOwner() {
            return locationField;
        }

        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initialize() {
            super.initialize();
            
            locationLabel.setText(component.getProperty(LOCATION_LABEL_TEXT_PROPERTY));
            locationButton.setText(component.getProperty(LOCATION_BUTTON_TEXT_PROPERTY));
            locationsLabel.setText(component.getProperty(LIST_LABEL_TEXT_PROPERTY));
            
            //TODO
            //? This should be set somewhere at the early stage otherwise Issue 144955 occurs.
            //Now it is workarounded my checking model == null in LocationsComboBoxEditor.getItem()
            LocationsListModel model = new LocationsListModel(
                    component.getLocations(),
                    component.getLabels());
            locationsList.setModel(model);
            
            File selectedLocation = component.getSelectedLocation();
            if (model.getSize() > 0) {
                if (selectedLocation != null) {
                    locationField.setText(selectedLocation.getAbsolutePath());
                } else {
                    locationField.setText(model.getLocationAt(0).getAbsolutePath());
                }
                
                locationsLabel.setVisible(true);
                locationsScrollPane.setVisible(true);
                locationsListReplacement.setVisible(false);
            } else {
                if (selectedLocation != null) {
                    locationField.setText(selectedLocation.getAbsolutePath());
                } else {
                    locationField.setText(SystemUtils.resolvePath(DEFAULT_LOCATION).getAbsolutePath());
                }
                
                locationsLabel.setVisible(false);
                locationsScrollPane.setVisible(false);
                locationsListReplacement.setVisible(true);
            }
            
            updateErrorMessage();
        }
        
        @Override
        protected void saveInput() {
            component.setLocation(new File(locationField.getText().trim()));
        }
        
        @Override
        protected String validateInput() {
            return component.validateLocation(locationField.getText().trim());
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // locationField ////////////////////////////////////////////////////////
            locationField = new NbiTextField();
            locationField.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(final DocumentEvent event) {
                    locationChanged();
                }
                
                public void insertUpdate(final DocumentEvent event) {
                    locationChanged();
                }
                
                public void removeUpdate(final DocumentEvent event) {
                    locationChanged();
                }
            });
            
            // locationLabel ////////////////////////////////////////////////////////
            locationLabel = new NbiLabel();
            locationLabel.setLabelFor(locationField);
            
            // locationButton ///////////////////////////////////////////////////////
            locationButton = new NbiButton();
            locationButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    browseButtonPressed();
                }
            });
            
            // locationsList ////////////////////////////////////////////////////////
            locationsList = new NbiList();
            locationsList.setBorder(new EmptyBorder(0, 0, 0, 0));
            locationsList.setCellRenderer(new LocationsListCellRenderer());
            locationsList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent event) {
                    if (!event.getValueIsAdjusting()) {
                        listSelectionChanged();
                    }
                }
            });
            
            // locationsScrollPane //////////////////////////////////////////////////
            locationsScrollPane = new NbiScrollPane(locationsList);
            
            // locationsLabel ///////////////////////////////////////////////////////
            locationsLabel = new NbiLabel();
            locationsLabel.setLabelFor(locationsList);
            
            // locationsListReplacement /////////////////////////////////////////////
            locationsListReplacement = new NbiPanel();
            
            // fileChooser //////////////////////////////////////////////////////////
            fileChooser = new NbiDirectoryChooser();            
            // this /////////////////////////////////////////////////////////////////
            add(locationLabel, new GridBagConstraints(
                    0, 1,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(7, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(locationField, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 4),          // padding
                    0, 0));                           // padx, pady - ???
            add(locationButton, new GridBagConstraints(
                    1, 2,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(4, 0, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
            add(locationsLabel, new GridBagConstraints(
                    0, 3,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(locationsScrollPane, new GridBagConstraints(
                    0, 4,                             // x, y
                    2, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(4, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(locationsListReplacement, new GridBagConstraints(
                    0, 5,                             // x, y
                    2, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(4, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
        }
        
        private void browseButtonPressed() {
            fileChooser.setSelectedFile(new File(locationField.getText()));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                locationField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
        
        private void listSelectionChanged() {
            final LocationsListModel model = (LocationsListModel) locationsList.getModel();
            
            final int index = locationsList.getSelectedIndex();
            if (index != -1) {
                String location = model.getLocationAt(index).getAbsolutePath();
                if (!location.equals(locationField.getText())) {
                    locationField.setText(location);
                }
            }
        }
        
        private void locationChanged() {
            updateErrorMessage();
            
            final LocationsListModel model = (LocationsListModel) locationsList.getModel();
            final String value = locationField.getText().trim();
            
            for (int i = 0; i < model.getSize(); i++) {
                final String element = (String) model.getLocationAt(i).getAbsolutePath();
                
                if (value.equals(element)) {
                    locationsList.setSelectedIndex(i);
                    return;
                }
            }
            
            locationsList.clearSelection();
        }
    }
    
    public static class LocationsListCellRenderer extends JLabel implements ListCellRenderer {
        public LocationsListCellRenderer() {
            setBorder(new EmptyBorder(3, 3, 3, 3));
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.toString());
            setToolTipText(value.toString());
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                setOpaque(true);
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                setOpaque(false);
            }
            
            return this;
        }
    }
    
    public static class LocationsListModel implements ListModel {
        private List<File> locations;
        private List<String> labels;
        
        public LocationsListModel(final List<File> locations, final List<String> labels) {
            this.locations = locations;
            this.labels = labels;
        }
        
        public int getSize() {
            return locations.size();
        }
        
        public Object getElementAt(int index) {
            return getLabelAt(index);
        }
        
        public String getLabelAt(int index) {
            return labels.get(index);
        }
        
        public File getLocationAt(int index) {
            return locations.get(index);
        }
        
        public void addListDataListener(ListDataListener listener) {
            // does nothing
        }
        
        public void removeListDataListener(ListDataListener listener) {
            // does nothing
        }
    }
    
    public static class LocationsComboBoxEditor implements ComboBoxEditor {
        private NbiTextField textField;
        
        private LocationValidator validator;
        private LocationsComboBoxModel model;
        
        public LocationsComboBoxEditor(LocationValidator validator) {
            this.validator = validator;
            
            textField = new NbiTextField();
            textField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    LocationsComboBoxEditor.this.validator.validate(
                            textField.getText());
                }
                
                public void removeUpdate(DocumentEvent e) {
                    LocationsComboBoxEditor.this.validator.validate(
                            textField.getText());
                }
                
                public void changedUpdate(DocumentEvent e) {
                    LocationsComboBoxEditor.this.validator.validate(
                            textField.getText());
                }
            });
            
            // l&f tweaks
            if (!SystemUtils.isMacOS()) {
                textField.setBorder(new EmptyBorder(1, 1, 1, 1));
            }
        }
        
        public void setModel(LocationsComboBoxModel model) {
            this.model = model;
        }
        
        // comboboxeditor ///////////////////////////////////////////////////////////
        public Component getEditorComponent() {
            return textField;
        }
        
        public void setItem(Object object) {
            if (object != null) {
                final int index = model.getLabels().indexOf(object);
                
                if (index != -1) {
                    textField.setText(model.getLocations().get(index));
                } else {
                    textField.setText(object.toString());
                }
            } else {
                textField.setText("");
            }
        }
        
        public Object getItem() {
            final String text = textField.getText();
            if(model == null) {
                return text;
            }
            final int index = model.getLocations().indexOf(text);
            
            if (index != -1) {
                return model.getLabels().get(index);
            } else {
                return text;
            }
        }
        
        public void selectAll() {
            textField.selectAll();
        }
        
        public void addActionListener(ActionListener listener) {
            textField.addActionListener(listener);
        }
        
        public void removeActionListener(ActionListener listener) {
            textField.removeActionListener(listener);
        }
    }
    
    public static class LocationsComboBoxModel implements ComboBoxModel {
        private List<ListDataListener> listeners;
        
        private List<String> locations;
        private List<String> labels;
        
        private String selectedItem;
        private boolean selectedItemFromList;
        
        public LocationsComboBoxModel(List<File> locations, List<String> labels) {
            this.locations = new LinkedList<String>();
            for (File file: locations) {
                this.locations.add(file.toString());
            }
            
            this.labels = new LinkedList<String>();
            this.labels.addAll(labels);
            
            this.listeners = new LinkedList<ListDataListener>();
            
            if (labels.size() > 0) {
                this.selectedItem = labels.get(0);
                this.selectedItemFromList = true;
            } else {
                this.selectedItem = StringUtils.EMPTY_STRING;
                this.selectedItemFromList = false;
            }
        }
        
        public List<String> getLabels() {
            return labels;
        }
        
        public List<String> getLocations() {
            return locations;
        }
        
        public String getLocation() {
            if (selectedItemFromList) {
                return locations.get(labels.indexOf(selectedItem));
            } else {
                return selectedItem;
            }
        }
        
        // comboboxmodel ////////////////////////////////////////////////////////////
        public void setSelectedItem(Object item) {
            selectedItem = (String) item;
            
            if (labels.indexOf(item) != -1) {
                selectedItemFromList = true;
            } else {
                selectedItemFromList = false;
            }
            
            fireContentsChanged(-1);
        }
        
        public Object getSelectedItem() {
            return selectedItem;
        }
        
        public int getSize() {
            return labels.size();
        }
        
        public Object getElementAt(int index) {
            return labels.get(index);
        }
        
        public void addListDataListener(ListDataListener listener) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
        
        public void removeListDataListener(ListDataListener listener) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void fireContentsChanged(int index) {
            final ListDataListener[] clone;
            synchronized (listeners) {
                clone = listeners.toArray(new ListDataListener[listeners.size()]);
            }
            
            final ListDataEvent event = new ListDataEvent(
                    this,
                    ListDataEvent.CONTENTS_CHANGED,
                    index,
                    index);
            
            for (ListDataListener listener: clone) {
                listener.contentsChanged(event);
            }
        }
    }
    
    public static interface LocationValidator {
        void validate(String location);
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String LOCATION_LABEL_TEXT_PROPERTY = 
            "location.label.text"; // NOI18N
    public static final String LOCATION_BUTTON_TEXT_PROPERTY = 
            "location.button.text"; // NOI18N
    public static final String LIST_LABEL_TEXT_PROPERTY = 
            "list.label.text"; // NOI18N
    
    public static final String DEFAULT_LOCATION_LABEL_TEXT = 
            ResourceUtils.getString(ApplicationLocationPanel.class, 
            "ALP.location.label.text"); // NOI18N
    public static final String DEFAULT_LOCATION_BUTTON_TEXT = 
            ResourceUtils.getString(ApplicationLocationPanel.class, 
            "ALP.location.button.text"); // NOI18N
    public static final String DEFAULT_LIST_LABEL_TEXT = 
            ResourceUtils.getString(ApplicationLocationPanel.class, 
            "ALP.list.label.text"); // NOI18N
           
    
    public static final String ERROR_NOTHING_FOUND_PROPERTY = 
            "error.nothing.found"; // NOI18N
    
    public static final String DEFAULT_ERROR_NOTHING_FOUND = 
            ResourceUtils.getString(ApplicationLocationPanel.class, 
            "ALP.error.nothing.found"); // NOI18N
    
    public static final String DEFAULT_LOCATION = 
            ResourceUtils.getString(ApplicationLocationPanel.class, 
            "ALP.default.location"); // NOI18N
}

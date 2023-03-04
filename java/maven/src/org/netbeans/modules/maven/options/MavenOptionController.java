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

package org.netbeans.modules.maven.options;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * controller for maven2 settings in the options dialog.
 * @author Milos Kleint
 */
@OptionsPanelController.SubRegistration(
    location=JavaOptions.JAVA,
    id=MavenOptionController.OPTIONS_SUBPATH,
    displayName="#TIT_Maven_Category",
    keywords="#KW_MavenOptions",
    keywordsCategory=JavaOptions.JAVA + "/Maven"
//    toolTip="#TIP_Maven_Category"
)
public class MavenOptionController extends OptionsPanelController {
    public static final String OPTIONS_SUBPATH = "Maven"; // NOI18N
    public static final String TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + //NOI18N
            "<settings xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +//NOI18N
            "  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd\">" +//NOI18N
            "</settings>";//NOI18N
    private SettingsPanel panel;
    private SettingsModel setts;
    private final List<PropertyChangeListener> listeners;
    /**
     * Creates a new instance of MavenOptionController
     */
    public MavenOptionController() {
        listeners = new ArrayList<PropertyChangeListener>();
    }

    @Override
    public void update() {
        getPanel().setValues();
    }
    
    @Override
    public void applyChanges() {
        getPanel().applyValues();
    }
    
    @Override
    public void cancel() {
        setts = null;
    }
    
    @Override
    public boolean isValid() {
        return getPanel().hasValidValues();
    }
    
    @Override
    public boolean isChanged() {
        return getPanel().hasChangedValues();
    }
    
    @Override
    public JComponent getComponent(Lookup lookup) {
        return getPanel();
    }

    void firePropChange(String property, Object oldVal, Object newVal) {
        ArrayList<PropertyChangeListener> lst;
        synchronized (listeners) {
            lst = new ArrayList<PropertyChangeListener>(listeners);
        }
        PropertyChangeEvent evnt = new PropertyChangeEvent(this, property, oldVal, newVal);
        for (PropertyChangeListener prop : lst) {
            if (prop == null) {
                //#180218
                continue;
            }
            prop.propertyChange(evnt);
        }
    }
    
    private SettingsPanel getPanel() {
        if (panel == null) {
            panel = new SettingsPanel(this);
        }
        return panel;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.maven.options.MavenAdvancedOption"); //NOI18N
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        synchronized (listeners) {
            listeners.add(propertyChangeListener);
        }
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        synchronized (listeners) {
            listeners.remove(propertyChangeListener);
        }
    }
    
}

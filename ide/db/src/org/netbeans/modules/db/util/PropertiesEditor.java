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
package org.netbeans.modules.db.util;

import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Custom editor for properties - mainly exists to call custom editor
 *
 * @author Matthias BlÃ¤sing
 */
public class PropertiesEditor extends PropertyEditorSupport implements ExPropertyEditor {

    private static final Logger LOG = Logger.getLogger(PropertiesEditor.class.getName());
    
    private boolean canWrite = true;

    @Override
    public String getAsText() {
        Properties value = (Properties) getValue();
        if (value == null || value.isEmpty()) {
            return NbBundle.getMessage(PropertiesEditor.class,
                    "NoPropertiesSet");                                 //NOI18N
        } else {
            return value.toString();
        }
    }

    @Override
    public void setValue(Object value) {
        if(value == null || value instanceof Properties) {
            super.setValue(value);
        } else {
            super.setValue(null);
            LOG.log(Level.INFO, "Illegal value supplied to PropertiesEditor#setValue: {0}", value.getClass().getName());
        }
    }

    /**
     * Can't be called and throws IllegalArgumentException
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException("Can't be set by setAsText");//NOI18N
    }

    @Override
    public String getJavaInitializationString() {
        return null; // does not generate any code
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public java.awt.Component getCustomEditor() {
        PropertyEditorPanel pep = new PropertyEditorPanel(
                (Properties) this.getValue(), canWrite);
        pep.addPropertyChangeListener(PropertyEditorPanel.PROP_VALUE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                setValue(((PropertyEditorPanel) pce.getSource()).getValue());
            }
        });
        return pep;
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        FeatureDescriptor d = env.getFeatureDescriptor();
        if (d instanceof Node.Property) {
            canWrite = ((Node.Property) d).canWrite();
        }
    }

    public boolean isEditable() {
        return canWrite;
    }
}
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
package org.netbeans.modules.cnd.makeproject.ui.runprofiles;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.IntConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.IntNodeProp;

public class ConsoleIntNodeProp extends IntNodeProp {

    private final IntConfiguration intConfiguration;
    IntEditor intEditor = null;
    private final PropertyChangeSupport pcs;

    @SuppressWarnings("unchecked")
    public ConsoleIntNodeProp(IntConfiguration intConfiguration, boolean canWrite, String name, String displayName, String description) {
        super( intConfiguration,  canWrite,  name,  displayName,  description);
        this.intConfiguration = intConfiguration;
        pcs = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void remotePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void setValue(Object v) {
        String s = (String)v;
        if (s != null) {
            for (int i = 0; i < intConfiguration.getNames().length; i++) {
                if (s.equals(intConfiguration.getNames()[i])) {
                    intConfiguration.setValue(i+1);
                    pcs.firePropertyChange(getName(), null, s);
                    break;
                }
            }
        }
        //intConfiguration.setValue((String) v);
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        if (intEditor == null) {
            intEditor = new IntEditor();
        }
        return intEditor;
    }

    private class IntEditor extends PropertyEditorSupport {

        @Override
        public String getJavaInitializationString() {
            return getAsText();
        }

        @Override
        public String getAsText() {
            int v = intConfiguration.getValue();
            if (v == 0) {
                v = RunProfile.getDefaultConsoleType();
            }
             return intConfiguration.getNames()[v-1];
        }

        @Override
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            super.setValue(text);
        }

        @Override
        public String[] getTags() {
            return intConfiguration.getNames();
        }
    }
}

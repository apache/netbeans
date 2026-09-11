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
package org.netbeans.modules.jcef.browser;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.SimpleBeanInfo;
import javax.swing.JPanel;

/**
 *
 * @author Laszlo Kishalmi
 */
public class BrowserFactoryBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(BrowserFactory.class);
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties;
        try {
            properties = new PropertyDescriptor[]{
                new PropertyDescriptor("id", BrowserFactory.class, "getId", null)
            };

            properties[0].setPreferred(true);
            properties[0].setPropertyEditorClass(EBPropertyEditor.class);

        } catch (IntrospectionException ie) {
            return null;
        }
        return properties;
    }

    public static class EBPropertyEditor implements PropertyEditor {

        public EBPropertyEditor() {

        }

        @Override
        public void setValue(Object value) {
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public boolean isPaintable() {
            return false;
        }

        @Override
        public void paintValue(Graphics gfx, Rectangle box) {
        }

        @Override
        public String getJavaInitializationString() {
            return null;
        }

        @Override
        public String getAsText() {
            return "";
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
        }

        @Override
        public String[] getTags() {
            return null;
        }

        @Override
        public Component getCustomEditor() {
            return new JPanel();
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }

}

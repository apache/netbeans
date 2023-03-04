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
package org.netbeans.modules.maven.model.settings.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.settings.SettingsComponent;
import org.netbeans.modules.maven.model.settings.SettingsComponentVisitor;
import org.netbeans.modules.maven.model.settings.SettingsExtensibilityElement;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.netbeans.modules.maven.model.settings.impl.SettingsComponentImpl;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class SettingsExtensibilityElementBase extends SettingsComponentImpl
        implements SettingsExtensibilityElement {
    
    public SettingsExtensibilityElementBase(SettingsModel model, Element e) {
        super(model, e);
    }

    public SettingsExtensibilityElementBase(SettingsModel model, QName name) {
        this(model, createElementNS(model, name));
    }
     
    @Override
    public void accept(SettingsComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getElementText() {
        return getText();
    }

    @Override
    public void setElementText(String text) {
        setText(getQName().getLocalPart(), text);
    }

    public static class StringAttribute implements Attribute {
        private String name;
        public StringAttribute(String name) { this.name = name; }
        @Override
        public Class getType() { return String.class; }
        @Override
        public String getName() { return name; }
        @Override
        public Class getMemberType() { return null; }
    }
    
    @Override
    public String getAttribute(String attribute) {
        return getAttribute(new StringAttribute(attribute));
    }
    
    @Override
    public void setAttribute(String attribute, String value) {
        setAttribute(attribute, new StringAttribute(attribute), value);
    }
    
    @Override
    public String getContentFragment() {
        return super.getXmlFragment();
    }
    
    @Override
    public void setContentFragment(String text) throws IOException {
        super.setXmlFragment(CONTENT_FRAGMENT_PROPERTY, text);
    }

    @Override
    public void addAnyElement(SettingsExtensibilityElement anyElement, int index) {
        List<SettingsComponent> all = getChildren();
        if (index > all.size() || index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        insertAtIndex(EXTENSIBILITY_ELEMENT_PROPERTY, anyElement, index);
    }

    @Override
    public void removeAnyElement(SettingsExtensibilityElement any) {
        super.removeExtensibilityElement(any);
    }

    @Override
    public List<SettingsExtensibilityElement> getAnyElements() {
        List<SettingsExtensibilityElement> result = new ArrayList<SettingsExtensibilityElement>();
        List<SettingsExtensibilityElement> allEEs = super.getExtensibilityElements();
        for (SettingsExtensibilityElement ee : allEEs) {
            if (! ee.getModel().getQNames().contains(ee.getQName())) {
                result.add(ee);
            }
        }
        return Collections.unmodifiableList(result);
    }   
}

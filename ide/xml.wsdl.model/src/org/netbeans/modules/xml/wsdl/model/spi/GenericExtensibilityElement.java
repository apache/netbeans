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
package org.netbeans.modules.xml.wsdl.model.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class GenericExtensibilityElement extends WSDLComponentBase implements ExtensibilityElement.ParentSelector {
    
    /** Creates a new instance of GenericExtensibilityElement */
    public GenericExtensibilityElement(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public GenericExtensibilityElement(WSDLModel model, QName qname){
        this(model, createNewElement(qname, model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public static class StringAttribute implements Attribute {
        private String name;
        public StringAttribute(String name) { this.name = name; }
        public Class getType() { return String.class; }
        public String getName() { return name; }
        public Class getMemberType() { return null; }
    }
    
    public String getAttribute(String attribute) {
        return getAttribute(new StringAttribute(attribute));
    }
    public void setAttribute(String attribute, String value) {
        setAttribute(attribute, new StringAttribute(attribute), value);
    }
    
    public String getContentFragment() {
        return super.getXmlFragment();
    }
    
    public void setContentFragment(String text) throws IOException {
        super.setXmlFragment(CONTENT_FRAGMENT_PROPERTY, text);
    }

    public void addAnyElement(ExtensibilityElement anyElement, int index) {
        List<WSDLComponent> all = getChildren();
        if (index > all.size() || index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        insertAtIndex(EXTENSIBILITY_ELEMENT_PROPERTY, anyElement, index);
    }

    public void removeAnyElement(ExtensibilityElement any) {
        super.removeExtensibilityElement(any);
    }

    public List<ExtensibilityElement> getAnyElements() {
        List<ExtensibilityElement> result = new ArrayList<ExtensibilityElement>();
        List<ExtensibilityElement> allEEs = super.getExtensibilityElements();
        for (ExtensibilityElement ee : allEEs) {
            if (! ee.getModel().getQNames().contains(ee.getQName())) {
                result.add(ee);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Generic extensibility by default can be added to any WSDL component.
     */
    public boolean canBeAddedTo(Component target) {
        if (target instanceof WSDLComponent) {
            return true;
        }
        return false;
    }
}

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
package org.netbeans.modules.maven.model.pom.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.pom.ModelList;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMComponentFactory;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author mkleint
 */
public abstract class POMComponentImpl extends AbstractDocumentComponent<POMComponent>
        implements POMComponent {

   public POMComponentImpl(POMModel model, Element e) {
        super(model, e);
    }
    
    @Override
    protected String getNamespaceURI() {
        return POMQName.NS_URI;
    }
        
    @Override
    public POMModel getModel() {
        return (POMModel) super.getModel();
    }
    
    @Override
    protected void populateChildren(List<POMComponent> children) {
        //System.out.println("populateChildren: " + getPeer().getNodeName());
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            POMModel model = getModel();
            POMComponentFactory componentFactory = model.getFactory();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    POMComponent comp = componentFactory.create((Element)n, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }

    @Override
    protected Object getAttributeValueOf(Attribute attribute, String stringValue) {
        return stringValue;
    }  
    
    public static Element createElementNS(POMModel model, POMQName rq) {
        return createElementNS(model, rq.getQName());
    }

    public static Element createElementNS(POMModel model, QName rq) {
        String qualified = rq.getPrefix() + ":" + rq.getLocalPart();
        return model.getDocument().createElementNS(
                rq.getNamespaceURI(), qualified);
    }
        
    @Override
    public void removeExtensibilityElement(POMExtensibilityElement ee) {
        removeChild(EXTENSIBILITY_ELEMENT_PROPERTY, ee);
    }
    
    @Override
    public void addExtensibilityElement(POMExtensibilityElement ee) {
        appendChild(EXTENSIBILITY_ELEMENT_PROPERTY, ee);
    }
    
    @Override
    public List<POMExtensibilityElement> getExtensibilityElements() {
        return getChildren(POMExtensibilityElement.class);
    }
    
    @Override
    public <T extends POMExtensibilityElement> List<T> getExtensibilityElements(Class<T> type) {
        return getChildren(type);
    }

    /**
     * Utility method to get the QName of a QName-type attribute value.
     * @param qNameTypeAttributeValue
     * @return
     */
    protected QName getQName(String qNameTypeAttributeValue) {
        QName ret = null;
        if (qNameTypeAttributeValue != null) {
            int colonIndex = qNameTypeAttributeValue.indexOf(":");  // NOI18N
            if (colonIndex != -1) {
                String prefix = qNameTypeAttributeValue.substring(0, colonIndex);
                String localPart = qNameTypeAttributeValue.substring(colonIndex + 1);
                String namespaceURI = lookupNamespaceURI(prefix);
                ret = new QName(namespaceURI, localPart, prefix);
            } else {
                String localPart = qNameTypeAttributeValue;
                String namespaceURI = lookupNamespaceURI(null);
                ret = new QName(namespaceURI, localPart);
            }
        }
        return ret;
    }

    /**
     * overrides the parent method as that one cannot be rollbacked.
     * see testModelWrite() test.
     * @param qname
     * @return
     */
    @Override
    public String getChildElementText(QName qname) {
        List<POMExtensibilityElement> els = getChildren(POMExtensibilityElement.class);
        for (POMExtensibilityElement el : els) {
            if (el.getQName().equals(qname)) {
                return el.getElementText();
            }
        }
        return null;
    }


    /**
     * overrides the parent method as that one cannot be rollbacked.
     * see testModelWrite() test.
     * @param qname
     * @return
     */
    @Override
    public void setChildElementText(String propertyName, String text, QName qname) {
        List<POMExtensibilityElement> els = getChildren(POMExtensibilityElement.class);
        for (POMExtensibilityElement el : els) {
            if (el.getQName().equals(qname)) {
                if (text != null) {
                    el.setElementText(text);
                } else {
                    removeChild(qname.getLocalPart(), el);
                }
                return;
            }
        }
        if (text != null) {
            POMExtensibilityElement el = getModel().getFactory().createPOMExtensibilityElement(qname);
            addBefore(qname.getLocalPart(), el, Collections.<Class<? extends POMComponent>>emptySet());
            if (!text.isEmpty()) {
                el.setElementText(text);
            }
        }
    }

    @Override
    public int findChildElementPosition(QName qname) {
        List<POMExtensibilityElement> els = getChildren(POMExtensibilityElement.class);
        for (POMExtensibilityElement el : els) {
            if (el.getQName().equals(qname)) {
                return el.findPosition();
            }
        }
        return -1;
    }

    protected final Collection<Class<? extends POMComponent>> getClassesBefore(List<Class<? extends POMComponent>> ordering, Class<? extends POMComponent> current) {
        ArrayList<Class<? extends POMComponent>> toRet = new ArrayList<Class<? extends POMComponent>>();
        for (Class<? extends POMComponent> ord : ordering) {
            if (ord.equals(current)) {
                break;
            }
            toRet.add(ord);
        }
        return toRet;
    }
    
    protected final <T extends POMComponent> void remove(T valueToRemove, String listname, Class<? extends ModelList<T>> listClazz) {
        ModelList<T> childs = getChild(listClazz);
        if (childs != null) {
            childs.removeListChild(valueToRemove);
            if (childs.getListChildren().isEmpty()) {
                removeChild(listname, childs);
            }
        }
    }
}


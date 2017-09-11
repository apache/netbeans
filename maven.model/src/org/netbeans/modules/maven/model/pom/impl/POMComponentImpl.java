/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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


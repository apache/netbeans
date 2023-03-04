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

package org.netbeans.modules.xml.xam;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.ComponentFactory;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.dom.ElementIdentity;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class TestModel3 extends AbstractDocumentModel<TestComponent3> implements DocumentModel<TestComponent3> {
    TestComponent3 testRoot;
    
    /** Creates a new instance of TestModel */
    public TestModel3(Document doc) {
        super(createModelSource(doc));
        try {
            super.sync();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        this.addPropertyChangeListener(new FaultGenerator());
    }

    @Override
    protected void setIdentifyingAttributes() {
        ElementIdentity eid = getAccess().getElementIdentity();
        eid.addIdentifier("id");
        eid.addIdentifier("index");
        eid.addIdentifier("name");
        eid.addIdentifier("ref");
    }

    @Override
    public TestComponent3 createRootComponent(org.w3c.dom.Element root) {
        if (TestComponent3.NS_URI.equals(root.getNamespaceURI()) &&
            "test".equals(root.getLocalName())) {
                testRoot = new TestComponent3(this, root);
        } else {
            testRoot = null;
        }
        return testRoot;
    }

    @Override
    public TestComponent3 createComponent(TestComponent3 parent, org.w3c.dom.Element element) {
        return TestComponent3.createComponent(this, parent, element);
    }

    @Override
    public TestComponent3 getRootComponent() {
        return testRoot;
    }
    
    private boolean faultInSyncUpdater = false;
    public void injectFaultInSyncUpdater() {
        faultInSyncUpdater = true;
    }

    @Override
    protected ComponentUpdater<TestComponent3> getComponentUpdater() {
        if (faultInSyncUpdater) {
            faultInSyncUpdater = false;
            Object npe = null; npe.getClass();
        }
        return new TestComponentUpdater3();
    }
    
    private boolean faultInFindComponent = false;
    public void injectFaultInFindComponent() {
        faultInFindComponent = true;
    }

    @Override
    public DocumentComponent findComponent(List<Element> pathFromRoot) {
        if (faultInFindComponent) {
            faultInFindComponent = false;
            Object npe = null; 
            npe.getClass();
        }
        return super.findComponent(pathFromRoot);
    }
    
    private boolean faultInEventFiring = false;
    public void injectFaultInEventFiring() {
        faultInEventFiring = true;
    }
    private class FaultGenerator implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (faultInEventFiring) {
                faultInEventFiring = false;
                Object foo = null;  foo.getClass();
            }
        }
    }
    
    public static ModelSource createModelSource(Document doc) {
        Lookup lookup = Lookups.fixed(new Object[] { doc } ); //maybe later a simple catalog
        return new ModelSource(lookup, true);
    }
    
    private static Set<QName> qnames = null;

    @Override
    public Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            qnames.add(TestComponent3.A.QNAME);
            qnames.add(TestComponent3.Aa.QNAME);
            qnames.add(TestComponent3.B.QNAME);
            qnames.add(TestComponent3.C.QNAME);
            qnames.add(TestComponent3.D.QNAME);
            qnames.add(TestComponent3.E.QNAME);
            qnames.add(new QName(TestComponent3.NS_URI, "test"));
            qnames.add(TestComponent3.Err.QNAME);
        }
        return qnames;
    }
    
    public ComponentFactory<TestComponent3> getFactory() {
        return new ComponentFactory<TestComponent3>() {
            @Override
            public TestComponent3 create(Element child, TestComponent3 parent) {
                return TestModel3.this.createComponent(parent, child);
            }
        };
    }
    
    public TestComponent3.A createA(TestComponent3 parent) {
        QName q = TestComponent3.A.QNAME;
        Element e = this.getDocument().createElementNS(q.getNamespaceURI(), q.getLocalPart());
        return (TestComponent3.A) TestComponent3.createComponent(this, parent, e);
    }
    public TestComponent3.Aa createAa(TestComponent3 parent) {
        QName q = TestComponent3.Aa.QNAME;
        Element e = this.getDocument().createElementNS(q.getNamespaceURI(), q.getLocalPart());
        return (TestComponent3.Aa) TestComponent3.createComponent(this, parent, e);
    }
    public TestComponent3.B createB(TestComponent3 parent) {
        QName q = TestComponent3.B.QNAME;
        Element e = this.getDocument().createElementNS(q.getNamespaceURI(), q.getLocalPart());
        return (TestComponent3.B) TestComponent3.createComponent(this, parent, e);
    }
    public TestComponent3.C createC(TestComponent3 parent) {
        QName q = TestComponent3.C.QNAME;
        Element e = this.getDocument().createElementNS(q.getNamespaceURI(), q.getLocalPart());
        return (TestComponent3.C) TestComponent3.createComponent(this, parent, e);
    }
    public TestComponent3.D createD(TestComponent3 parent) {
        QName q = TestComponent3.D.QNAME;
        Element e = this.getDocument().createElementNS(q.getNamespaceURI(), q.getLocalPart());
        return (TestComponent3.D) TestComponent3.createComponent(this, parent, e);
    }

    public TestComponent3.Err createErr(TestComponent3 parent) {
        QName q = TestComponent3.Err.QNAME;
        Element e = this.getDocument().createElementNS(q.getNamespaceURI(), q.getLocalPart());
        return (TestComponent3.Err) TestComponent3.createComponent(this, parent, e);
    }
}

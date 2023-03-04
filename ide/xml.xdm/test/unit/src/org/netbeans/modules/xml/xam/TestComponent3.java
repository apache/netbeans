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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Nam Nguyen
 */
public class TestComponent3 extends AbstractDocumentComponent<TestComponent3> implements NamedReferenceable<TestComponent3> {
    public static String NS_URI = "http://www.test.com/TestModel";
    public static String NS2_URI = "http://www.test2.com/TestModel";
    // public static String NS_ERR_URI = "http://www.test2.com/TestModel/Err";
    
    public TestComponent3(TestModel3 model, org.w3c.dom.Element e) {
        super(model, e);
    }
    public TestComponent3(TestModel3 model, String name, String ns) {
        this(model, model.getDocument().createElementNS(ns, name));
    }
    public TestComponent3(TestModel3 model, String name, String ns, int index) {
        this(model, name, ns);
        setIndex(index);
    }
    public TestComponent3(TestModel3 model, String name, String ns, int index, String value) {
        this(model, name, ns, index);
        setValue(value);
    }

    @Override
    public String toString() { return getName(); }

    @Override
    public String getName() { return getPeer().getLocalName()+getIndex(); }

    @Override
    public String getNamespaceURI() {
        return super.getNamespaceURI();
    }

    @Override
    protected void populateChildren(List<TestComponent3> children) {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            for (int i = 0; i < nl.getLength(); i++) {
                org.w3c.dom.Node n = nl.item(i);
                if (n instanceof Element) {
                    Element e = (Element) n;
                    TestComponent3 comp = createComponent(getModel(), this, e);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }
    
    static TestComponent3 createComponent(TestModel3 model, TestComponent3 parent, Element e)  {
        String namespace = e.getNamespaceURI();
        if (namespace == null && parent != null) {
            namespace = parent.lookupNamespaceURI(e.getPrefix());
        }
        if (e.getLocalName().equals("a") && NS_URI.equals(namespace)) {
            return new TestComponent3.A(model, e);
        } else if (e.getLocalName().equals("a") && NS2_URI.equals(namespace)) {
            return new TestComponent3.Aa(model, e);
        } else if (e.getLocalName().equals("b") && NS_URI.equals(namespace)) {
            return new TestComponent3.B(model, e);
        } else if (e.getLocalName().equals("c") && NS_URI.equals(namespace)) {
            return new TestComponent3.C(model, e);
        } else if (e.getLocalName().equals("d") && NS_URI.equals(namespace)) {
            return new TestComponent3.D(model, e);
        } else if (e.getLocalName().equals("e") && NS_URI.equals(namespace)) {
            return new TestComponent3.E(model, e);
        } else if (e.getLocalName().equals(Err.LNAME) && NS_URI.equals(namespace)) {
            return new TestComponent3.Err(model, e);
        } else {
            return null;
            //throw new RuntimeException("unsupported element type "+ e.getNodeName());
        }
    }
    
    public void setValue(String v) { 
        setAttribute(TestAttribute3.VALUE.getName(), TestAttribute3.VALUE, v);
    }
    public String getValue() { 
        return getAttribute(TestAttribute3.VALUE);
    }

    public void setIndex(int index) {
        setAttribute(TestAttribute3.INDEX.getName(), TestAttribute3.INDEX, Integer.valueOf(index));
    }
    public int getIndex() {
        String s = getAttribute(TestAttribute3.INDEX);
        return s == null ? -1 : Integer.parseInt(s); 
    }

    @Override
    public void updateReference(Element n) {
        assert (n != null);
        assert n.getLocalName().equals(getQName().getLocalPart());
        super.updateReference(n);
    }
    
//    public QName getQName() { return ROOT_QNAME; }

    @Override
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        if (stringValue == null) return null;
        if (String.class.isAssignableFrom(attr.getType())) {
            return stringValue;
        } else if (Integer.class.isAssignableFrom(attr.getType())) {
            return Integer.valueOf(stringValue);
        }
        assert false : "unsupported type"+attr.getType();
        return stringValue;
    }
    
    public static class A extends TestComponent3 {
        public static final QName QNAME = new QName(NS_URI, "a");
        public A(TestModel3 model, int i) {
            super(model, "a", NS_URI, i);
        }

        public A(TestModel3 model, Element e) {
            super(model, e);
        }

        @Override
        public QName getQName() { return QNAME; }
    }
    public static class Aa extends TestComponent3 {
        public static final QName QNAME = new QName(NS2_URI, "a");
        public Aa(TestModel3 model, int i) {
            super(model, "a", NS2_URI, i);
        }
        public Aa(TestModel3 model, Element e) {
            super(model, e);
        }

        @Override
        public QName getQName() { return QNAME; }
    }
    public static class B extends TestComponent3 {
        public static final QName QNAME = new QName(NS_URI, "b");
        public B(TestModel3 model, int i) {
            super(model, "b", NS_URI, i);
        }
        public B(TestModel3 model, Element e) {
            super(model, e);
        }

        @Override
        public QName getQName() { return QNAME; }
    }
    public static class C extends TestComponent3 {
        public static final QName QNAME = new QName(NS_URI, "c");
        public C(TestModel3 model, int i) {
            super(model, "c", NS_URI, i);
        }
        public C(TestModel3 model, Element e) {
            super(model, e);
        }

        @Override
        public QName getQName() { return QNAME; }
    }
    public static class D extends TestComponent3 {
        public static final QName QNAME = new QName(NS_URI, "d");
        public D(TestModel3 model, int i) {
            super(model, "d", NS_URI, i);
        }
        public D(TestModel3 model, Element e) {
            super(model, e);
        }

        @Override
        public QName getQName() { return QNAME; }
    }
    public static class E extends TestComponent3 {
        public static final QName QNAME = new QName(NS_URI, "e");
        public E(TestModel3 model, int i) {
            super(model, "e", NS_URI, i);
        }
        public E(TestModel3 model, Element e) {
            super(model, e);
        }

        @Override
        public QName getQName() { return QNAME; }

        @Override
        public String getValue() {
            String retValue;
            
            retValue = super.getValue();
            return retValue;
        }

        @Override
        public String getName() {
            return super.getAttribute(TestAttribute3.NAME);
        }

        public void setName(String v) {
            setAttribute(TestAttribute3.NAME.getName(), TestAttribute3.NAME, v);
        }
    }

    /**
     * Special component for testing error processing in ComponentUpdater. 
     */
    public static class Err extends TestComponent3 {
        public static final String LNAME = "err";
        public static final QName QNAME = new QName(NS_URI, LNAME);
        public Err(TestModel3 model, int i) {
            super(model, LNAME, NS_URI, i);
        }
        public Err(TestModel3 model, Element e) {
            super(model, e);
        }

        @Override
        public QName getQName() { return QNAME; }

        @Override
        public String getValue() {
            String retValue;

            retValue = super.getValue();
            return retValue;
        }

        @Override
        public String getName() {
            return super.getAttribute(TestAttribute3.NAME);
        }

        public void setName(String v) {
            setAttribute(TestAttribute3.NAME.getName(), TestAttribute3.NAME, v);
        }
    }
    
    public static class TestComponentReference<T extends TestComponent3>
            extends AbstractNamedComponentReference<T> {
        public TestComponentReference(Class<T> type, TestComponent3 parent, String ref) {
            super(type, parent, ref);
        }
        public TestComponentReference(T ref, Class<T> type, TestComponent3 parent) {
            super(ref, type, parent);
        }

        @Override
        public TestComponent3 getParent() {
            return (TestComponent3) super.getParent();
        }

        @Override
        public String getEffectiveNamespace() {
            if (getReferenced() != null) {
                return getReferenced().getModel().getRootComponent().getTargetNamespace();
            }
            return getParent().getModel().getRootComponent().getTargetNamespace();
        }

        @Override
        public T get() {
            if (getReferenced() == null) {
                String tns = getQName().getNamespaceURI();
                TestComponent3 root = getParent().getModel().getRootComponent();
                if (tns != null && tns.equals(root.getTargetNamespace())) {
                    setReferenced(getType().cast(new ReferencedFinder().
                            findReferenced(root, getQName().getLocalPart())));
                }
            }
            return getReferenced();
        }
    }

    @Override
    public String getLeadingText(TestComponent3 child) {
        return super.getLeadingText(child);
    }

    @Override
    public String getTrailingText(TestComponent3 child) {
        return super.getTrailingText(child);
    }
    
    public void setText(String propName, String value, TestComponent3 child, final boolean leading) {
        if (leading) {
            setLeadingText(propName, value, child);
        } else {
            setTrailingText(propName, value, child);
        }
    }

    @Override
    public TestModel3 getModel() {
        return (TestModel3) super.getModel();
    }
    
    public void accept(TestVisitor3 visitor) {
        visitor.visit(this);
    }
    
    public String getTargetNamespace() {
        return getAttribute(TestAttribute3.TNS);
    }
    public void setTargetNamespace(String v) {
        setAttribute(TestAttribute3.TNS.getName(), TestAttribute3.NAME, v);
    }
    
    public <T extends TestComponent3> TestComponentReference<T> getRef(Class<T> type) {
        String v = getAttribute(TestAttribute3.REF);
        return v == null ? null : new TestComponentReference<T>(type, this, v);
    }
    
    public <T extends TestComponent3> void setRef(T referenced, Class<T> type) {
        TestComponentReference<T> ref = new TestComponentReference<T>(referenced, type, this);
        super.setAttribute(TestAttribute3.REF.getName(), TestAttribute3.REF, ref);
    }
    
    
    public static class ReferencedFinder extends TestVisitor3 {
        String name;
        TestComponent3 found;
        
        public ReferencedFinder() {
        }
        public TestComponent3 findReferenced(TestComponent3 root, String name) {
            this.name = name;
            root.accept(this);
            return found;
        }

        @Override
        public void visit(TestComponent3 component) {
            if (name.equals(component.getName())) {
                found = component;
            } else {
                visitChildren(component);
            }
        }

        @Override
        public void visitChildren(TestComponent3 component) {
            for (TestComponent3 child : component.getChildren()) {
                child.accept(this);
                if (found != null) {
                    return;
                }
            }
        }
    }
    
    static Collection<Class<? extends TestComponent3>> EMPTY = new ArrayList<Class<? extends TestComponent3>>();
    public static Collection <Class<? extends TestComponent3>> _ANY = new ArrayList<Class<? extends TestComponent3>>();
    static { _ANY.add(TestComponent3.class); }
    public static Collection <Class<? extends TestComponent3>> _A = new ArrayList<Class<? extends TestComponent3>>();
    static {  _A.add(A.class); }
    public static Collection <Class<? extends TestComponent3>> _B = new ArrayList<Class<? extends TestComponent3>>();
    static {  _B.add(B.class); }
    public static Collection <Class<? extends TestComponent3>> _C = new ArrayList<Class<? extends TestComponent3>>();
    static {  _C.add(C.class); }
    public static Collection <Class<? extends TestComponent3>> _D = new ArrayList<Class<? extends TestComponent3>>();
    static {  _D.add(D.class); }
    public static Collection <Class<? extends TestComponent3>> _AB = new ArrayList<Class<? extends TestComponent3>>();
    static {  _AB.add(A.class); _AB.add(B.class); }
    public static Collection <Class<? extends TestComponent3>> _BC = new ArrayList<Class<? extends TestComponent3>>();
    static {  _BC.add(B.class); _BC.add(C.class); }
    public static Collection <Class<? extends TestComponent3>> _AC = new ArrayList<Class<? extends TestComponent3>>();
    static {  _AC.add(A.class); _AC.add(C.class); }
    public static Collection <Class<? extends TestComponent3>> _ABC = new ArrayList<Class<? extends TestComponent3>>();
    static {  _ABC.add(A.class); _ABC.add(B.class); _ABC.add(C.class); }
    public static Collection <Class<? extends TestComponent3>> _BAC = new ArrayList<Class<? extends TestComponent3>>();
    static {  _BAC.add(B.class); _BAC.add(A.class); _BAC.add(C.class); }
}

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

import java.util.List;

/**
 *
 * @author Nam Nguyen
 */
public class TestComponent extends AbstractComponent<TestComponent> implements NamedReferenceable<TestComponent>, Cloneable {
    int index;
    String value;
    
    public TestComponent(TestModel model, int index) {
        super(model);
        this.index = index;
    }
    
    public String toString() { return getName(); }
    public String getName() { return "test"; }
    
    protected void populateChildren(List<TestComponent> children) {
        children.add(new A(getModel(), 1));
        children.add(new A(getModel(), 2));
        children.add(new A(getModel(), 3));
    }
    
    public void setValue(String v) { 
        String old = value;
        this.value = v;
        super.firePropertyChange("value", old, value);
        super.fireValueChanged();
    }
    public String getValue() { 
        return value;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    public int getIndex() {
        return index;
    }

    public static class A extends TestComponent {
        public A(TestModel model, int i) {
            super(model, i);
            this.index = i;
        }
        public String getName() { return "a"+index; }
    }
    
    public static class B extends TestComponent {
        public B(TestModel model, int i) {
            super(model, i);
            this.index = i;
        }
        public String getName() { return "b"+index; }
    }

    public static class C extends TestComponent {
        public C(TestModel model, int i) {
            super(model, i);
            this.index = i;
        }
        public String getName() { return "c"+index; }
    }

    public TestModel getModel() {
        return (TestModel) super.getModel();
    }

    protected void insertAtIndexQuietly(TestComponent newComponent, List<TestComponent> children, int index) {
        children.add(index, newComponent);
    }

    public Component copy(TestComponent parent) {
        try {
            return (Component) this.clone();
        } catch(CloneNotSupportedException ex) {
            return null;
        }
    }

    protected void removeChildQuietly(TestComponent component, List<TestComponent> children) {
        children.remove(component);
    }

    protected void appendChildQuietly(TestComponent component, List<TestComponent> children) {
        children.add(component);
    }
    
}

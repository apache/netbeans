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

package org.openide.explorer.propertysheet;

import java.beans.PropertyEditorSupport;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Node;

public class IndexedPropertyEditorTest extends NbTestCase {

    public IndexedPropertyEditorTest(String name) {
        super(name);
    }

    public void testGetJavaInitializationString() throws Exception {
        ExPropertyEditor editor = new IndexedPropertyEditor();
        PropertyEnv env = new PropertyEnv();
        Node.Property<?> prop = null;
        Bean bean = new Bean();
        bean.setStuff(new Stuff[] {new Stuff(1), new Stuff(2)});
        for (Node.PropertySet s : new BeanNode<Bean>(bean).getPropertySets()) {
            for (Node.Property<?> p : s.getProperties()) {
                if (p.getName().equals("stuff")) {
                    prop = p;
                    break;
                }
            }
        }
        assertNotNull(prop);
        assertTrue(prop instanceof Node.IndexedProperty<?,?>);
        editor.setValue(bean.getStuff());
        env.setFeatureDescriptor(prop);
        editor.attachEnv(env);
        String n = Stuff.class.getCanonicalName();
        assertEquals("new" + n + "[]{new" + n + "(1),new" + n + "(2)}", editor.getJavaInitializationString().replaceAll("\\s+", ""));
    }

    public static class Stuff {
        final int v;
        Stuff(int v) {
            this.v = v;
        }
    }

    public static class StuffEditor extends PropertyEditorSupport {
        @Override public String getJavaInitializationString() {
            return "new " + Stuff.class.getCanonicalName() + "(" + ((Stuff) getValue()).v + ")";
        }
    }

    public static class Bean {
        private Stuff[] stuff;
        public Stuff[] getStuff() {
            return stuff.clone();
        }
        public void setStuff(Stuff[] stuff) {
            this.stuff = stuff.clone();
        }
        public Stuff getStuff(int index) {
            return stuff[index];
        }
        public void setStuff(int index, Stuff stuff) {
            this.stuff[index] = stuff;
        }
    }

}

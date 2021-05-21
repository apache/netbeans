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

package org.openide.explorer.propertysheet;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.PropertySupport;

/**
 * Check that enumeration types have some kind of minimal proped.
 * @author Jesse Glick
 */
public class EnumPropertyEditorTest extends NbTestCase {

    public EnumPropertyEditorTest(String name) {
        super(name);
    }

    public void testEnumPropEd() throws Exception {
        EProp prop = new EProp();
        PropertyEditor ed = PropUtils.getPropertyEditor(prop);
        assertEquals( EnumPropertyEditor.class, ed.getClass());
        assertFalse(ed.supportsCustomEditor());
        assertFalse(ed.isPaintable());
        String[] tags = ed.getTags();
        assertNotNull(tags);
        assertEquals("[CHOCOLATE, VANILLA, STRAWBERRY]", Arrays.toString(tags));
        assertEquals(E.VANILLA, ed.getValue());
        assertEquals("VANILLA", ed.getAsText());
        ed.setAsText("STRAWBERRY");
        assertEquals(E.STRAWBERRY, ed.getValue());
        assertEquals(E.class.getName().replace('$', '.') + ".STRAWBERRY", ed.getJavaInitializationString());
    }

    public void testNulls() throws Exception {
        EProp prop = new EProp();
        PropertyEditor ed = PropUtils.getPropertyEditor(prop);
        assertEquals( EnumPropertyEditor.class, ed.getClass());
        ed.setAsText("");
        assertEquals(null, ed.getValue());
        assertEquals("", ed.getAsText());
        assertEquals("null", ed.getJavaInitializationString());
    }

    public void testLocalizedNames() throws Exception {
        ABProp prop = new ABProp();
        PropertyEditor ed = PropUtils.getPropertyEditor(prop);
        assertEquals( EnumPropertyEditor.class, ed.getClass());
        ed.setAsText("");
        assertEquals("myA", ed.getTags()[0]);
        assertEquals("myB", ed.getTags()[1]);
        assertEquals(null, ed.getValue());
        ed.setAsText("myB");
        assertEquals("myB", ed.getAsText());
        assertEquals(BetterToString.class.getName().replace('$', '.') + ".B", ed.getJavaInitializationString());
    }

    public enum E {
        CHOCOLATE, VANILLA, STRAWBERRY
    }

    private static class EProp extends PropertySupport.ReadWrite<E> {

        private E e = E.VANILLA;

        public EProp() {
            super("eval", E.class, "E Val", "E value");
        }

        public E getValue() throws IllegalAccessException, InvocationTargetException {
            return e;
        }

        public void setValue(E val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            e = val;
        }

    }

    public enum BetterToString {
        A, B;

        @Override
        public String toString() {
            return "my" + name();
        }
    }

    private static class ABProp extends PropertySupport.ReadWrite<BetterToString> {

        private BetterToString e = BetterToString.A;

        public ABProp() {
            super("eval", BetterToString.class, "E Val", "E value");
        }

        @Override
        public BetterToString getValue() throws IllegalAccessException, InvocationTargetException {
            return e;
        }

        @Override
        public void setValue(BetterToString val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            e = (BetterToString) val;
        }
    }
}

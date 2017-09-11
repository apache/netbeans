/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

    private static class EProp extends PropertySupport.ReadWrite {

        private E e = E.VANILLA;

        public EProp() {
            super("eval", E.class, "E Val", "E value");
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return e;
        }

        public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            e = (E) val;
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

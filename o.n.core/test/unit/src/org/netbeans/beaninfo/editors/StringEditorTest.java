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

package org.netbeans.beaninfo.editors;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import org.openide.nodes.Node;

/**
 *
 * @author rkubacki
 */
public class StringEditorTest extends TestCase {
    static {
        PropertyEditorManager.registerEditor (String.class, StringEditor.class);
    }

    public void testNullValueSupport() throws Exception {
        NP np = new NP();
        String defaultValue = "<null value>";
        String customValue = "Hello world!";
        np.setValue(ObjectEditor.PROP_NULL, defaultValue);
        
        PropertyEditor p = np.getPropertyEditor();
        assertNotNull("There is some editor", p);
        assertEquals("It is StringEditor", StringEditor.class, p.getClass());
        ((StringEditor) p).readEnv(np);
        
        p.setValue(null);
        String value = (String)p.getValue ();
        assertNull(value);
        assertEquals(defaultValue, p.getAsText());

        p.setValue(customValue);
        value = (String)p.getValue ();
        assertEquals(customValue, value);
        assertEquals(customValue, p.getAsText());

        np.setValue(ObjectEditor.PROP_NULL, Boolean.TRUE);
        ((StringEditor) p).readEnv(np);
        p.setValue(null);
        value = (String)p.getValue ();
        assertNull(value);
        assertFalse("we've better than default 'null' string", "null".equals(defaultValue));
    }

    public void testNoCustomEditorWithoutPropertyEnv() {
        NP np = new NP();
        PropertyEditor p = np.getPropertyEditor();
        assertNotNull("There is some editor", p);
        assertEquals("It is StringEditor", StringEditor.class, p.getClass());

        assertFalse("Custom property editor not working without PropertyEnv, so should not be offered.",
                    p.supportsCustomEditor());
    }

    class NP extends Node.Property<String> {
        public String value;
        
        public NP () {
            super (String.class);
        }

        public @Override void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            value = val;
        }

        public @Override String getValue() throws IllegalAccessException, InvocationTargetException {
            return value;
        }

        public @Override boolean canWrite() {
            return true;
        }

        public @Override boolean canRead() {
            return true;
        }
    }
}

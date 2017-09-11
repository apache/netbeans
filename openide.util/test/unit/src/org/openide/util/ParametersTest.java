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

package org.openide.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import junit.framework.TestCase;
import org.openide.util.Parameters;

/**
 *
 * @author Andrei Badea
 */
public class ParametersTest extends TestCase {

    public ParametersTest(String testName) {
        super(testName);
    }

    public void testNotNull() throws Exception {
        assertNPEOnNull(Parameters.class.getMethod("notNull", CharSequence.class, Object.class));
        Parameters.notNull("param", "");
    }

    public void testNotEmpty() throws Exception {
        assertNPEOnNull(Parameters.class.getMethod("notEmpty", CharSequence.class, CharSequence.class));
        try {
            Parameters.notEmpty("param", "");
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException e) {}
        Parameters.notEmpty("param", "foo");
    }

    public void testNotWhitespace() throws Exception {
        assertNPEOnNull(Parameters.class.getMethod("notWhitespace", CharSequence.class, CharSequence.class));
        try {
            Parameters.notWhitespace("param", "");
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException e) {}
        try {
            Parameters.notWhitespace("param", " ");
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException e) {}
        Parameters.notWhitespace("param", " foo ");
    }

    public void testJavaIdentifier() throws Exception {
        assertNPEOnNull(Parameters.class.getMethod("javaIdentifier", CharSequence.class, CharSequence.class));
        try {
            Parameters.javaIdentifier("param", "");
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException e) {}
        try {
            Parameters.javaIdentifier("param", "foo#Method");
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException e) {}
        Parameters.javaIdentifier("param", "fooMethod");
    }

    public void testJavaIdentifierOrNull() throws Exception {
        try {
            Parameters.javaIdentifierOrNull("param", "");
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException e) {}
        try {
            Parameters.javaIdentifierOrNull("param", "foo#Method");
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException e) {}
        Parameters.javaIdentifierOrNull("param", null);
        Parameters.javaIdentifierOrNull("param", "fooMethod");
    }

    private void assertNPEOnNull(Method method) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException  {
        try {
            method.invoke(null, "param", null);
            fail("Should have thrown NPE");
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            assertEquals(NullPointerException.class, target.getClass());
            // ensure the NPE was thrown by us, not by the VM
            assertEquals("The param parameter cannot be null", target.getMessage());
        }
    }
}

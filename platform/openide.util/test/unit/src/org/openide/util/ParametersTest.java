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

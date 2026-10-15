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
package org.netbeans.modules.classfile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Converted from org.netbeans.jmi.javamodel.getters.ParamNameTest to
 * directly use classfile API instead of javacore.
 *
 * @author  tball
 */
public class ParamNameTest extends TestCase {
    ClassFile classFile;
    List<String> results;
    
    String[] result = {
	"void assertTrue(String message, boolean condition)",
	"void assertTrue(boolean condition)",
	"void assertFalse(String message, boolean condition)",
	"void assertFalse(boolean condition)",
	"void fail(String message)",
	"void fail()",
	"void assertEquals(String message, Object expected, Object actual)",
	"void assertEquals(Object expected, Object actual)",
	"void assertEquals(String message, String expected, String actual)",
	"void assertEquals(String expected, String actual)",
	"void assertEquals(String message, double expected, double actual, double delta)",
	"void assertEquals(double expected, double actual, double delta)",
	"void assertEquals(String message, float expected, float actual, float delta)",
	"void assertEquals(float expected, float actual, float delta)",
	"void assertEquals(String message, long expected, long actual)",
	"void assertEquals(long expected, long actual)",
	"void assertEquals(String message, boolean expected, boolean actual)",
	"void assertEquals(boolean expected, boolean actual)",
	"void assertEquals(String message, byte expected, byte actual)",
	"void assertEquals(byte expected, byte actual)",
	"void assertEquals(String message, char expected, char actual)",
	"void assertEquals(char expected, char actual)",
	"void assertEquals(String message, short expected, short actual)",
	"void assertEquals(short expected, short actual)",
	"void assertEquals(String message, int expected, int actual)",
	"void assertEquals(int expected, int actual)",
	"void assertNotNull(Object object)",
	"void assertNotNull(String message, Object object)",
	"void assertNull(Object object)",
	"void assertNull(String message, Object object)",
	"void assertSame(String message, Object expected, Object actual)",
	"void assertSame(Object expected, Object actual)",
	"void assertNotSame(String message, Object expected, Object actual)",
	"void assertNotSame(Object expected, Object actual)",
	"void failSame(String message)",
	"void failNotSame(String message, Object expected, Object actual)",
	"void failNotEquals(String message, Object expected, Object actual)",
	"java.lang.String format(String message, Object expected, Object actual)"
    };

    /** Creates a new instance of ParamNameTest */
    public ParamNameTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws IOException {
        ClassLoader loader = ParamNameTest.class.getClassLoader();
        InputStream is = loader.getResourceAsStream("junit/framework/Assert.class");
	classFile = new ClassFile(is);
        is.close();
        results = Arrays.asList(result);
    }

    public void test() {
        for (Method m : classFile.getMethods()) {
            if (m.getName().equals("<init>")) {
                continue;
            }
            String s = m.getReturnSignature() + ' ' + m.getName() + '(';
            for (Iterator<Parameter> itPar = m.getParameters().iterator(); itPar.hasNext(); ) {
                Parameter p = itPar.next();
                s += p.getDeclaration();
                if (itPar.hasNext()) {
                    s += ", ";
                }
            }
            s += ')';
            
            assertTrue("has \"" + s + "\"", results.contains(s));
        }
    }

    public static void main(String[] args) {
        TestRunner.run(ParamNameTest.class);
    }
}

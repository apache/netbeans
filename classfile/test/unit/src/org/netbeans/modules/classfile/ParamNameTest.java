/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
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
    List results;
    
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
        int counter = 0;
        for (Iterator it = classFile.getMethods().iterator(); it.hasNext(); ) {
            Method m = (Method) it.next();
            if (m.getName().equals("<init>"))
                continue;
            String s = m.getReturnSignature() + ' ' + m.getName() + '(';
            for (Iterator itPar = m.getParameters().iterator(); itPar.hasNext(); ) {
                Parameter p = (Parameter) itPar.next();
                s += p.getDeclaration();
                if (itPar.hasNext()) {
                    s += ", ";
                }
            }
            s += ')';
            
            assertTrue("has \"" + s + "\"", results.contains(s));
            counter++;
        }
    }

    public static void main(String[] args) {
        TestRunner.run(ParamNameTest.class);
    }
}

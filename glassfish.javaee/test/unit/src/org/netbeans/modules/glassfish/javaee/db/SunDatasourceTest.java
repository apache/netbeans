/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee.db;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vkraemer
 */
public class SunDatasourceTest {

    public SunDatasourceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of equals method, of class SunDatasource.
     */
    @Test
    public void testEquals() {
        Object obj = null;
        String a="A";
        String b="B";
        String c="C";
        String d="D";
        String e="E";
        SunDatasource instance = new SunDatasource(a,b,c,d,e);
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
        obj = "String";
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = new SunDatasource("a","b","c","d","e");
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = new SunDatasource("A","B","C","D","E");
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = new SunDatasource(a,b,c,d,e);
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class SunDatasource, when at least one field
     * is set to null (resulting from a missing property for example).
     */
    @Test
    public void testEqualsNull() {
        Object obj = null;
        String a="A";
        String b="B";
        String c="C";
        String d="D";
        String e=null;
        SunDatasource instance = new SunDatasource(a,b,c,d,e);
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
        obj = "String";
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = new SunDatasource("a","b","c","d","e");
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = new SunDatasource("A","B","C","D",null);
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = new SunDatasource(a,b,c,d,e);
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
    }
}

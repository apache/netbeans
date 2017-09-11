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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.dataview.util;

import java.io.UnsupportedEncodingException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.meta.DBException;

/**
 *
 * @author jawed
 */
public class BinaryToStringConverterTest extends NbTestCase {
    
    public BinaryToStringConverterTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(BinaryToStringConverterTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of convertToString method, of class BinaryToStringConverter.
     */
    public void testConvertToString() {
        byte[] data = new byte[1];
        int base = 10;
        boolean showAscii = false;
        String expResult = "022";
        data[0] = new Byte(expResult);
        String result = BinaryToStringConverter.convertToString(data, base, showAscii);
        assertEquals(expResult, result);
    }

    /**
     * Test SQL hexadecimal encoding - adapted from
     * http://dev.mysql.com/doc/refman/5.0/en/hexadecimal-literals.html
     */
    public void testConvertToString2() throws UnsupportedEncodingException,
            DBException {
        byte[] data = "\u0000MySQL".getBytes("ASCII");
        int base = 16;
        boolean showAscii = false;
        String expResult = "004d7953514c";
        String result = BinaryToStringConverter.convertToString(
                data, base, showAscii);
        assertEquals(expResult, result);

        data = "Paul".getBytes("ASCII");
        expResult = "5061756c";
        result = BinaryToStringConverter.convertToString(data, base, showAscii);
        assertEquals(expResult, result);
    }
}

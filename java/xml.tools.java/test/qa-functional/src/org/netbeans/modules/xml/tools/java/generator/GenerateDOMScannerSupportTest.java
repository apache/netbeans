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
package org.netbeans.modules.xml.tools.java.generator;

import java.lang.reflect.Method;
import junit.textui.TestRunner;
import org.netbeans.modules.xml.core.DTDDataObject;
import org.netbeans.tests.xml.XTest;

//import org.openide.*;
import org.openide.filesystems.FileObject;

/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module API Test: XMLGenerator2Test
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 * XMLGenerator2Test checks 'Generate DOM Tree Scanner' action on DTD document. The action is
 * accesible from popup menu an all DTD document nodes.<BR>
 *
 * <BR><B>How it works:</B><BR>
 * Test opens DTD document, generates Java source text for the document and writes it into output.<BR>
 *
 * <BR><BR><B>Settings:</B><BR>
 * None
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * Scanner of DOM tree as Java source text.<BR>
 *
 * <BR><B>Possible reasons of failure:</B>
 * <UL>
 * <LI type="circle">
 * <I>None<BR></I>
 * </LI>
 * </UL>
 * <P>
 */

public class GenerateDOMScannerSupportTest extends XTest {
    
    /** Creates new CoreSettingsTest */
    public GenerateDOMScannerSupportTest(String testName) {
        super(testName);
    }
    
    public void test() throws Exception {
        DTDDataObject dao = (DTDDataObject) TestUtil.THIS.findData("books.dtd");
        if (dao == null) {
            fail("\"data/books.dtd\" data object is not found!");
        }
        FileObject primFile = dao.getPrimaryFile();
        String rawName = primFile.getName();
        String name = rawName.substring(0,1).toUpperCase() + rawName.substring(1) + "Scanner";
        FileObject folder = primFile.getParent();
        String packageName = folder.getPackageName('.');
        GenerateDOMScannerSupport gen = new GenerateDOMScannerSupport(dao);
        // prepareDOMScanner() is private at GenerateDOMScannerSupport.class
        Method m = gen.getClass().getDeclaredMethod("prepareDOMScanner", new Class[] {String.class, String.class, FileObject.class});
        m.setAccessible(true);
        String result = (String) m.invoke(gen, new Object[] {name, packageName, primFile});
        // first comment contains variable informations - remove it
        result = TestUtil.replaceString(result, "/*", "*/", "/* REMOVED */");
        ref(result);
        compareReferenceFiles();
    }
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        TestRunner.run(GenerateDOMScannerSupportTest.class);
    }
}

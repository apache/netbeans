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

package org.netbeans.modules.j2ee.dd.api.webservices;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.InputSource;

/*
 * DDProviderTest.java
 * JUnit based test
 *
 * Created on 16 December 2005, 14:15
 */

/**
 *
 * @author jungi
 */
public class DDProviderTest extends NbTestCase {

    private DDProvider dd;
    
    public DDProviderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dd = DDProvider.getDefault();
        assertNotNull("DDProvider cannot be null.", dd);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        dd = null;
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DDProviderTest.class);
        return suite;
    }

    /**
     * Test of getDDRoot method, of class org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.
     */
    public void testGetDDRootFromFOWHandler() throws Exception {
        File f = new File(getDataDir(), "wHandler/webservices.xml").getAbsoluteFile();
        readWriteDD(f, true);
    }

    /**
     * Test of getDDRoot method, of class org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.
     */
    public void testGetDDRootFromInputSourceWHandler() throws Exception {
        File f = new File(getDataDir(), "wHandler/webservices.xml").getAbsoluteFile();
        readWriteDD(f, false);
    }
    
    /**
     * Test of getDDRoot method, of class org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.
     */
    public void testGetDDRootFromFOWoHandler() throws Exception {
        File f = new File(getDataDir(), "woHandler/webservices.xml").getAbsoluteFile();
        readWriteDD(f, true);
    }

    /**
     * Test of getDDRoot method, of class org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.
     */
    public void testGetDDRootFromInputSourceWoHandler() throws Exception {
        File f = new File(getDataDir(), "woHandler/webservices.xml").getAbsoluteFile();
        readWriteDD(f, false);
    }
    
    private void readWriteDD(File ddFile, boolean useFO) throws Exception {
        Webservices result = null;
        if (!useFO) {
            InputSource is = new InputSource(new BufferedInputStream(new FileInputStream(ddFile)));
            result = dd.getDDRoot(is);
        } else {
            FileObject fo = FileUtil.toFileObject(ddFile);
            result = dd.getDDRoot(fo);
        }
        assertNotNull("Result cannot be null.", result);
        assertEquals(Webservices.STATE_VALID, result.getStatus());
        File dest = new File(getWorkDir(), "webservices.xml");
        File diff = new File(getWorkDir(), "webservices.xml.diff");
        if (dest.exists()) dest.delete();
        if (diff.exists()) diff.delete();
        assertTrue(dest.createNewFile());
        assertTrue(diff.createNewFile());
        result.write(new BufferedOutputStream(new FileOutputStream(dest)));
        assertFile(dest, ddFile, diff);
    }
}

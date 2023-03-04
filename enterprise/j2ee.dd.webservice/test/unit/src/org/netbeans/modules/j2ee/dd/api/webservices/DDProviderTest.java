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

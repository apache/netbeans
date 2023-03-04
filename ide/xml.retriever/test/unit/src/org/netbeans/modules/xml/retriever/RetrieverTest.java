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
/*
 * RetrieverTest.java
 * JUnit based test
 *
 * Created on August 16, 2006, 5:02 PM
 */

package org.netbeans.modules.xml.retriever;

import junit.framework.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author girix
 */
public class RetrieverTest extends TestCase {
    
    public RetrieverTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(RetrieverTest.class);
        
        return suite;
    }
    
    /**
     * Test of getDefault method, of class org.netbeans.modules.xml.retriever.Retriever.
     */
    public void testGetDefault() throws URISyntaxException, UnknownHostException, IOException {
        System.out.println("getDefault");
        
        Retriever expResult = null;
        Retriever result = Retriever.getDefault();
        //uncomment the following to test the retriever method
        /*File destFolder = new File(System.getProperty("java.io.tmpdir")+File.separator+"RetrieverTest");
        if(destFolder.isDirectory())
            destFolder.renameTo(new File(destFolder.toString()+System.currentTimeMillis()));
        destFolder.mkdirs();
        URI catFileURI = null;
        catFileURI = new URI(destFolder.toURI().toString() + "/catalogfile.xml");
        FileObject dstFO = FileUtil.toFileObject(FileUtil.normalizeFile(destFolder));
        
        result.retrieveResource(dstFO, catFileURI, new URI("http://localhost:8084/grt/maindoc/UBL-Order-1.0"));
         **/
    }
    
    /**
     * Test of getDefault method, of class org.netbeans.modules.xml.retriever.Retriever.
     */
    public void testRelativize() throws Exception {
        System.out.println("getDefault");
        URI masterURI = new URI("A/B/C");
        URI slaveURI = new URI("A/B/C/D/E");
        String result = Utilities.relativize(masterURI, slaveURI);
        assert(result.equals("D/E"));        
	
        masterURI = new URI("file:/A/B/C/");
	slaveURI = new URI("file:/A/F/G");
        result = Utilities.relativize(masterURI, slaveURI);
        assert( result.equals("../../F/G"));
    }
    
    
}

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

package org.netbeans.modules.websvc.api;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

/**
 *
 * @author Lukas Jungmann
 */
public class CustomWebServicesClientSupportProviderTest extends NbTestCase {
    
    private FileObject datadir;
    private FileObject nows;
    private FileObject ws;
    private FileObject jaxws;
    private FileObject both;
    
    static {
        CustomWebServicesClientSupportProviderTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    /** Creates a new instance of CustomWebServicesSupportProviderTest */
    public CustomWebServicesClientSupportProviderTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        File f = getWorkDir();
        assertTrue("work dir exists", f.exists());
        LocalFileSystem lfs = new LocalFileSystem ();
        lfs.setRootDirectory (f);
        Repository.getDefault ().addFileSystem (lfs);
        datadir = FileUtil.toFileObject(f);
        assertNotNull("no FileObject", datadir);
        nows = datadir.createData("custom", "nows");
        assertNotNull("no ws FileObject", nows);
        ws = datadir.createData("custom", "ws");
        assertNotNull("no ws FileObject", ws);
        jaxws = datadir.createData("custom", "jaxws");
        assertNotNull("no ws FileObject", jaxws);
        both = datadir.createData("custom", "both");
        assertNotNull("no ws FileObject", both);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        ws.delete();
        jaxws.delete();
        nows.delete();
        both.delete();
    }
    
    public void testProviders() throws Exception {
        Lookup.Result res = Lookup.getDefault().lookup(new Lookup.Template(WebServicesClientSupportProvider.class));
        assertEquals("there should be 2 instances - one from websvc/clientapi and one from tests", 2, res.allInstances ().size ());
    }
    
    public void testGetWebServicesClientSupport() throws Exception {
        WebServicesClientSupport ws1 = WebServicesClientSupport.getWebServicesClientSupport(nows);
        assertNull("not found ws support", ws1);
        WebServicesClientSupport ws2 = WebServicesClientSupport.getWebServicesClientSupport(ws);
        assertNotNull("found ws support", ws2);
        WebServicesClientSupport ws3 = WebServicesClientSupport.getWebServicesClientSupport(jaxws);
        assertNull("not found ws support", ws3);
        WebServicesClientSupport ws4= WebServicesClientSupport.getWebServicesClientSupport(both);
        assertNotNull("found ws support", ws4);
        
        JAXWSClientSupport jaxws1 = JAXWSClientSupport.getJaxWsClientSupport(nows);
        assertNull("not found jaxws support", jaxws1);
        JAXWSClientSupport jaxws2 = JAXWSClientSupport.getJaxWsClientSupport(ws);
        assertNull("not found jaxws support", jaxws2);
        JAXWSClientSupport jaxws3 = JAXWSClientSupport.getJaxWsClientSupport(jaxws);
        assertNotNull("found jaxws support", jaxws3);
        JAXWSClientSupport jaxws4 = JAXWSClientSupport.getJaxWsClientSupport(both);
        assertNotNull("found jaxws support", jaxws4);
    }
}

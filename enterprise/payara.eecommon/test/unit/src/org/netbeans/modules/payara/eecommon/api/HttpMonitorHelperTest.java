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

package org.netbeans.modules.payara.eecommon.api;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.Test;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import static org.junit.Assert.*;
import org.openide.util.Utilities;
import org.xml.sax.SAXException;

/**
 *
 * @author vkraemer
 */
public class HttpMonitorHelperTest {

    public HttpMonitorHelperTest() {
    }

    @Test
    public void testGetDefaultWebXML() throws SAXException, IOException, ClassNotFoundException  {
        System.out.println("getDefaultWebXML");
        URL codebase = getClass().getProtectionDomain().getCodeSource().getLocation();
        if (!codebase.getProtocol().equals("file")) {  // NOI18N
            throw new Error("Cannot find data directory from " + codebase); // NOI18N
        }
        File dataDir;
        try {
            dataDir = new File(Utilities.toFile(codebase.toURI()).getParentFile(), "data");  // NOI18N
        } catch (URISyntaxException x) {
            throw new Error(x);
        }
        String domainLoc = dataDir.getAbsolutePath();
        String domainName = "domain1"; // NOI18N
        File f = new File(domainLoc + "/" + domainName +"/config/default-web.xml.orig");
        f.delete();
        f = new File(domainLoc + "/" + domainName +"/config/default-web.xml");
         WebApp webApp;
        try {
            webApp = DDProvider.getDefault().getDDRoot(f);
        } catch (IOException ioe) {
            fail("Could not parse default-web.xml file");
        }
         // first pass
        f =  HttpMonitorHelper.getDefaultWebXML(domainLoc, domainName);
        webApp = DDProvider.getDefault().getDDRoot(f);
        f = new File(domainLoc + "/" + domainName +"/config/default-web.xml");
        webApp = DDProvider.getDefault().getDDRoot(f);
        // second pass
        f =  HttpMonitorHelper.getDefaultWebXML(domainLoc, domainName);
        webApp = DDProvider.getDefault().getDDRoot(f);
        f = new File(domainLoc + "/" + domainName +"/config/default-web.xml");
        webApp = DDProvider.getDefault().getDDRoot(f);
        HttpMonitorHelper.changeFilterMonitor(webApp, true);
        HttpMonitorHelper.specifyFilterPortParameter(webApp);
        HttpMonitorHelper.specifyFilterPortParameter(webApp);
        HttpMonitorHelper.changeFilterMonitor(webApp, false);
        f = new File(domainLoc + "/" + domainName +"/config/default-web.xml.orig");
        try {
            webApp = DDProvider.getDefault().getDDRoot(f);
        } catch (IOException ioe) {
            fail("Could not parse default-web.xml file");
        }

    }

    /**
     * Test of createCopyAndUpgrade method, of class HttpMonitorHelper.
     */
    @Test
    public void testCreateCopyAndUpgrade() throws IOException, SAXException {
        System.out.println("createCopyAndUpgrade");

        // empty source and dest
        File webXML = File.createTempFile("foo", "bar"); // NOI18N
        File newWebXML = File.createTempFile("bar", "foo");  // NOI18N
        newWebXML.delete();
        HttpMonitorHelper.createCopyAndUpgrade(webXML, newWebXML);
        assert newWebXML.exists();

        // source doesn't exist
        webXML.delete();
        newWebXML.delete();
        HttpMonitorHelper.createCopyAndUpgrade(webXML, newWebXML);
        assert !newWebXML.exists();

        // expected data in source
        URL codebase = getClass().getProtectionDomain().getCodeSource().getLocation();
        if (!codebase.getProtocol().equals("file")) {  // NOI18N
            throw new Error("Cannot find data directory from " + codebase); // NOI18N
        }
        File dataDir;
        try {
            dataDir = new File(Utilities.toFile(
                    codebase.toURI()).getParentFile(), "data");  // NOI18N
        } catch (URISyntaxException x) {
            throw new Error(x);
        }
        webXML = new File(dataDir,"default-web.xml");   // NOI18N
        HttpMonitorHelper.createCopyAndUpgrade(webXML, newWebXML);
        assert newWebXML.exists();
        try {
            WebApp webApp = DDProvider.getDefault().getDDRoot(webXML);
        } catch (IOException ioe) {
            fail("Could not parse default-web.xml file");
        }
//        WebApp webApp = DDProvider.getDefault().getDDRoot(newWebXML);
        newWebXML.delete();
    }


}

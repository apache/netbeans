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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.glassfish.eecommon.api;

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
        WebApp webApp = DDProvider.getDefault().getDDRoot(newWebXML);
        newWebXML.delete();
    }


}

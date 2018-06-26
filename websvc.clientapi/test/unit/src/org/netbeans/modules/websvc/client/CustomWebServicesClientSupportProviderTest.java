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

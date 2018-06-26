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
import org.netbeans.modules.websvc.api.client.WebServicesClientView;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

/**
 *
 * @author Lukas Jungmann
 */
public class CustomWebServicesClientViewProviderTest extends NbTestCase {
    
    private FileObject datadir;
    private FileObject ws;
    private FileObject nows;
    
    static {
        CustomWebServicesClientViewProviderTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    /** Creates a new instance of CustomJAXWSViewProviderTest */
    public CustomWebServicesClientViewProviderTest(String name) {
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
        ws = datadir.createData("custom", "ws");
        assertNotNull("no ws FileObject", ws);
        nows = datadir.createData("custom", "nows");
        assertNotNull("no ws FileObject", nows);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        ws.delete();
        nows.delete();
    }
    
    public void testProviders() throws Exception {
        Lookup.Result res = Lookup.getDefault().lookup(new Lookup.Template(WebServicesClientViewProvider.class));
        assertEquals("there should be 1 instance - from websvc/clientapi", 1, res.allInstances().size());
    }
    
    public void testGetWebServicesClientView() {
        WebServicesClientView view = WebServicesClientView.getWebServicesClientView(ws);
        assertNotNull("found view support", view);
        WebServicesClientView view2 = WebServicesClientView.getWebServicesClientView(nows);
        assertNull("no found view support", view2);
    }
    
}

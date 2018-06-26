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

package org.netbeans.modules.j2ee.ejbjar;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.spi.ejbjar.CarProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

/**
 * Base class for web module project tests.
 * @author Pavel Buzek
 * @author Lukas Jungmann
 */
public class CustomProviderCarTest extends NbTestCase {
    
    static {
        CustomProviderCarTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    public CustomProviderCarTest (String name) {
        super(name);
    }
    
    private FileObject datadir;
    
    protected void setUp() throws Exception {
        super.setUp();
        File f = getDataDir();
        assertTrue("example dir exists", f.exists());
        LocalFileSystem lfs = new LocalFileSystem ();
        lfs.setRootDirectory (f);
        Repository.getDefault ().addFileSystem (lfs);
        datadir = FileUtil.toFileObject (f);
        assertNotNull ("no FileObject", datadir);
    }

    public void testProviders () throws Exception {
        Lookup.Result res = Lookup.getDefault ().lookup (new Lookup.Template (CarProvider.class));
        assertEquals ("there should be 2 instances - one from j2ee/ejbapi and one from tests", 2, res.allInstances ().size ());
    }
    
    public void testGetCar () throws Exception {
        FileObject foo = datadir.getFileObject ("a.foo");
        FileObject bar = datadir.getFileObject ("b.bar");
        Car wm1 = Car.getCar (bar);
        assertNotNull ("found car module", wm1);
        Car wm2 = Car.getCar (foo);
        assertNull ("no car module", wm2);
    }
}

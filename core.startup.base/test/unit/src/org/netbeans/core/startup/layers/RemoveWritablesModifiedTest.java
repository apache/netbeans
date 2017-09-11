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

package org.netbeans.core.startup.layers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Stanislav Aubrecht
 */
public class RemoveWritablesModifiedTest extends NbTestCase {
    Module myModule;
    File configDir;
    
    private static final String manifest = "Manifest-Version: 1.0\n"
                + "OpenIDE-Module: org.netbeans.modules.foo\n"
                + "OpenIDE-Module-Specification-Version: 1.0\n"
                + "OpenIDE-Module-Implementation-Version: today\n"
                + "OpenIDE-Module-Layer: foo/mf-layer.xml\n";
    private ModuleManager mgr;

    public RemoveWritablesModifiedTest(String testName) {
        super(testName);
    }
    
    @Override
    protected int timeOut() {
        return 15000;
    }
    

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        
        File u = new File(getWorkDir(), "userdir");
        File uc = new File(u, "config");
        uc.mkdirs();
        System.setProperty("netbeans.user", u.toString());
        
        File h = new File(getWorkDir(), "nb/installdir");
        new File(h, "config").mkdirs();
        System.setProperty("netbeans.home", h.toString());

        File moduleJar = createModuleJar( manifest );
        mgr = Main.getModuleSystem().getManager();
        try {
            mgr.mutexPrivileged().enterWriteAccess();
            myModule = mgr.create( moduleJar, null, true, false, false );
            mgr.enable( myModule );
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        
        assertNotNull("Module layer is installed", FileUtil.getConfigFile( "foo" ) );
        
        configDir = FileUtil.toFile( FileUtil.getConfigRoot() );//new File( getWorkDir(), "userdir/config" );
        
    }

    protected @Override void tearDown() throws Exception {
        if( null != myModule ) {
            mgr = Main.getModuleSystem().getManager();
            try {
                mgr.mutexPrivileged().enterWriteAccess();
                mgr.disable( myModule );
                mgr.delete( myModule );
            } finally {
                mgr.mutexPrivileged().exitWriteAccess();
            }
        }
    }
    

    public void testModifiedAttributesFile() throws Exception {
        FileObject existingFile = FileUtil.getConfigFile( "foo/test1" );
        
        assertNotNull( existingFile );
        
        existingFile.setAttribute( "myAttribute", "myAttributeValue" );
        
        assertFalse("removeWritables does not work for file attributes", FileUtil.getConfigFile("foo").canRevert());
    }


    private File createModuleJar(String manifest) throws IOException {
        // XXX use TestFileUtils.writeZipFile
        File jarFile = new File( getWorkDir(), "mymodule.jar" );
        
        JarOutputStream os = new JarOutputStream(new FileOutputStream(jarFile), new Manifest(
            new ByteArrayInputStream(manifest.getBytes())
        ));
        JarEntry entry = new JarEntry("foo/mf-layer.xml");
        os.putNextEntry( entry );
        
        File l3 = new File(new File(new File(getDataDir(), "layers"), "data"), "layer3.xml");
        InputStream is = new FileInputStream(l3);
        FileUtil.copy( is, os );
        is.close();
        os.close();
        
        return jarFile;
    }
}

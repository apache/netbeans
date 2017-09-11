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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.ui.customizer.ClusterInfo;
import org.openide.util.test.TestFileUtils;

/**
 * Test functionality of ModuleList needed by Netigso.
 * @author Jesse Glick
 */
public class NetigsoModuleListTest extends TestBase {
    
    public NetigsoModuleListTest(String name) {
        super(name);
    }

    public void testParseConfigXML() throws Exception {
        File c = new File(new File(getWorkDir(), "config"), "Modules");
        c.mkdirs();
        File j = new File(new File(getWorkDir(), "subdir"), "x-y-z.jar");
        j.getParentFile().mkdirs();
        Manifest mf = new Manifest();
        mf.getMainAttributes().putValue("Manifest-Version", "1.0");
        mf.getMainAttributes().putValue("Bundle-SymbolicName", "x.y.z;singleton:=true");
        JarOutputStream os = new JarOutputStream(new FileOutputStream(j), mf);
        os.close();

        String cnt = "<?xml version='1.0' encoding='UTF-8'?>" +
                "<!DOCTYPE module PUBLIC '-//NetBeans//DTD Module Status 1.0//EN'" +
                "   'http://www.netbeans.org/dtds/module-status-1_0.dtd'>" +
                "<module name='x.y.z'>" +
                "  <param name='autoload'>true</param>" +
                "  <param name='eager'>false</param>" +
                "  <param name='jar'>subdir/x-y-z.jar</param>" +
                "</module>";
        TestFileUtils.writeFile(new File(c, "x-y-z.xml"), cnt);

        ClusterInfo ci = ClusterInfo.createExternal(getWorkDir(), new URL[0], new URL[0], true);
        ModuleList ml = ModuleList.scanCluster(getWorkDir(), getWorkDir(), true, ci);

        assertEquals("One found", 1, ml.getAllEntries().size());
        ModuleEntry me = ml.getEntry("x.y.z");
        assertNotNull("Correct entry found", me);
        assertEquals("Cnb is OK", "x.y.z", me.getCodeNameBase());
        assertEquals("Jar file OK", j, me.getJarLocation());
    }


}

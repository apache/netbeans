/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.netbeans.junit.NbTestCase;

public class BrandingTest extends NbTestCase {

    public BrandingTest(String n) {
        super(n);
    }

    public void testDefaultExclusions() throws Exception { // #161724
        File cluster = new File(getWorkDir(), "cluster");
        cluster.mkdir();
        File overrides = new File(getWorkDir(), "overrides");
        File overriding = new File(overrides, "core/core.jar/org/netbeans/core/startup/Bundle.properties");
        overriding.getParentFile().mkdirs();
        new FileOutputStream(overriding).close();
        File junk = new File(overrides, "core/core.jar/org/netbeans/core/startup/.svn/props");
        junk.getParentFile().mkdirs();
        new FileOutputStream(junk).close();
        junk = new File(overrides, "core/.svn/props");
        junk.getParentFile().mkdirs();
        new FileOutputStream(junk).close();
        Project p = new Project();
        p.init();
        p.addBuildListener(new BuildListener() {
            public void buildStarted(BuildEvent event) {}
            public void buildFinished(BuildEvent event) {}
            public void targetStarted(BuildEvent event) {}
            public void targetFinished(BuildEvent event) {}
            public void taskStarted(BuildEvent event) {}
            public void taskFinished(BuildEvent event) {}
            public void messageLogged(BuildEvent event) {
                if (event.getPriority() < Project.MSG_INFO && event.getMessage().contains("stray")) {
                    fail(event.getMessage());
                }
            }
        });
        Branding b = new Branding();
        b.setProject(p);
        b.setCluster(cluster);
        b.setOverrides(overrides);
        b.setToken("myapp");
        b.setLocales("en");
        b.execute();
        File jar = new File(cluster, "core/locale/core_myapp.jar");
        assertTrue(jar.isFile());
        JarFile jf = new JarFile(jar);
        try {
            Enumeration<JarEntry> entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                assertEquals("META-INF/MANIFEST.MF", entry.getName());
                while (entries.hasMoreElements()) {
                    entry = entries.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }
                    assertEquals("org/netbeans/core/startup/Bundle_myapp.properties", entry.getName());
                }
            }
        } finally {
            jf.close();
        }
    }

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.PatternSet;
import org.netbeans.junit.NbTestCase;

public class LocFilesTest extends NbTestCase {
    private File src;
    private File dist;
    private LocFiles task;
    
    public LocFilesTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        dist = new File(getWorkDir(), "dist");
        dist.mkdirs();
        src = new File(getWorkDir(), "src");
        src.mkdirs();
        
        Project p = new Project();
        task = new LocFiles();
        task.setProject(p);
        task.setCluster("platform");
        task.setLocales("cs");
        task.setPatternSet("pattern.set");
        task.setSrc(src);
        task.setDestDir(dist);
    }
    
    public void testOpenideUtilLookup() throws Exception {
        createSource("cs/platform/org-openide-util-lookup/netbeans/lib/org-openide-util-lookup/org/openide/util/lookup/Bundle_cs.properties");
        task.setCodeNameBase("org.openide.util.lookup");
        task.execute();
        assertDist("platform/lib/locale/org-openide-util-lookup_cs.jar", "org/openide/util/lookup/Bundle_cs.properties");
        assertPattern("platform", "lib/locale/org-openide-util-lookup_cs.jar");
    }

    public void testSettings() throws Exception {
        createSource("cs/platform/settings/settings/org/netbeans/modules/settings/resources/Bundle_cs.properties");
        task.setCodeNameBase("org.netbeans.modules.settings");
        task.execute();
        assertDist("platform/modules/locale/org-netbeans-modules-settings_cs.jar", "org/netbeans/modules/settings/resources/Bundle_cs.properties");
        assertPattern("platform", "modules/locale/org-netbeans-modules-settings_cs.jar");
    }

    public void testSettingsPatternsOnly() throws Exception {
        createSource("cs/platform/settings/settings/org/netbeans/modules/settings/resources/Bundle_cs.properties");
        task.setCodeNameBase("org.netbeans.modules.settings");
        task.setDestDir(null);
        task.execute();
        assertPattern("platform", "modules/locale/org-netbeans-modules-settings_cs.jar");
    }
    
    public void testCoreWindows() throws Exception {
        final String pref = "cs/platform/org-netbeans-core-windows/org-netbeans-core-windows/org/netbeans/core/windows/";
        createSource(
            pref + "resources/Bundle_cs.properties",
            pref + "actions/Bundle_cs.properties",
            pref + "services/Bundle_cs.properties",
            pref + "options/Bundle_cs.properties",
            pref + "view/ui/Bundle_cs.properties",
            pref + "persistence/Bundle_cs.properties"
        );
        task.setCodeNameBase("org.netbeans.core.windows");
        task.execute();
        assertDist("platform/modules/locale/org-netbeans-core-windows_cs.jar", 
            "org/netbeans/core/windows/resources/Bundle_cs.properties",
            "org/netbeans/core/windows/actions/Bundle_cs.properties",
            "org/netbeans/core/windows/services/Bundle_cs.properties",
            "org/netbeans/core/windows/options/Bundle_cs.properties",
            "org/netbeans/core/windows/view/ui/Bundle_cs.properties",
            "org/netbeans/core/windows/persistence/Bundle_cs.properties"
        );
        assertPattern("platform", "modules/locale/org-netbeans-core-windows_cs.jar");
    }
    
    public void testAutoupdateServices() throws Exception {
        final String pref = "cs/platform/autoupdate-services/autoupdate-services/org/netbeans/modules/autoupdate/";
        createSource(
            "cs/platform/autoupdate-services/ext/updater/org/netbeans/updater/Bundle_cs.properties",
            pref + "services/resources/Bundle_cs.properties",
            pref + "services/Bundle_cs.properties",
            pref + "updateprovider/Bundle_cs.properties"
        );
        task.setCodeNameBase("org.netbeans.modules.autoupdate.services");
        task.execute();
        assertDist("platform/modules/locale/org-netbeans-modules-autoupdate-services_cs.jar", 
            "org/netbeans/modules/autoupdate/services/resources/Bundle_cs.properties",
            "org/netbeans/modules/autoupdate/services/Bundle_cs.properties",
            "org/netbeans/modules/autoupdate/updateprovider/Bundle_cs.properties"
        );
        assertDist("platform/modules/ext/locale/updater_cs.jar", 
            "org/netbeans/updater/Bundle_cs.properties"
        );
        assertPattern("platform", "modules/locale/org-netbeans-modules-autoupdate-services_cs.jar");
        assertPattern("platform", "modules/ext/locale/updater_cs.jar");
    }
    
    public void testAnt() throws Exception {
        task.setLocales("ja");
        task.setCluster("java");
        task.setCodeNameBase("org.apache.tools.ant.module");
        createSource("ja/java/org-apache-tools-ant-module/netbeans/ant/nblib/bridge/org/apache/tools/ant/module/bridge/impl/Bundle_ja.properties");
        task.execute();

        assertDist("java/ant/nblib/locale/bridge_ja.jar", "org/apache/tools/ant/module/bridge/impl/Bundle_ja.properties");
        assertPattern("java", "ant/nblib/locale/bridge_ja.jar");
    }
    public void testIdeBranding() throws Exception {
        task.setLocales("ja");
        task.setCluster("nb");
        task.setCodeNameBase("org.netbeans.modules.ide.branding");
        createSource("ja/nb/ide-branding/locale/options-api_nb/org/netbeans/modules/options/export/Bundle_nb_ja.properties");
        task.execute();

        assertDist("nb/modules/locale/org-netbeans-modules-options-api_nb_ja.jar", "org/netbeans/modules/options/export/Bundle_nb_ja.properties");
        assertPattern("nb", "modules/locale/org-netbeans-modules-options-api_nb_ja.jar");
    }

    private void createSource(String... files) throws IOException {
        for (String f : files) {
            File c = new File(src, f.replace('/', File.separatorChar));
            c.getParentFile().mkdirs();
            c.createNewFile();
        }
    }

    private void assertDist(String jar, String... files) throws IOException {
        File f = new File(dist, jar.replace('/', File.separatorChar));
        assertTrue("File " + f + " is created", f.exists());
        JarFile jf = new JarFile(f);
        Set<String> expected = new HashSet<String>(Arrays.asList(files));
        Enumeration<JarEntry> en = jf.entries();
        while (en.hasMoreElements()) {
            JarEntry e = en.nextElement();
            expected.remove(e.getName());
        }
        assertTrue("All files are present: " + expected, expected.isEmpty());
    }

    private void assertPattern(String cluster, String file) {
        Object ref = task.getProject().getReference("pattern.set");
        assertNotNull("Reference is found", ref);
        assertTrue("Right instance: " + ref, ref instanceof PatternSet);
        PatternSet ps = (PatternSet)ref;
        
        List<String> arr = Arrays.asList(ps.getIncludePatterns(task.getProject()));
        assertTrue(file + " is there: " + arr, arr.contains(file));
    }
}

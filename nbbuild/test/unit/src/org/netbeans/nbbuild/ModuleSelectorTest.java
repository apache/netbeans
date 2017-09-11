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

package org.netbeans.nbbuild;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.types.Parameter;


/** Check behaviour of ModuleSelector.
 *
 * @author Jaroslav Tulach
 */
public class ModuleSelectorTest extends TestBase {
    private ModuleSelector selector;
    
    public ModuleSelectorTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        selector = new ModuleSelector();
    }

    public void testIsSelectedForNotAModule() throws IOException {
        File noModule = generateJar(new String[0], createManifest ());
        assertFalse("Not acceptable", selector.isSelected(getWorkDir(), noModule.toString(), noModule));
    }

    public void testIncludesAllModulesByDefault() throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);
        assertTrue("Accepted", selector.isSelected(getWorkDir(), aModule.toString(), aModule));
    }
    
    public void testCanExcludeAModule() throws Exception {
        Parameter p = new Parameter();
        p.setName("excludeModules");
        p.setValue("org.my.module");
        selector.setParameters(new Parameter[] { p });
        
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);
        assertFalse("Refused", selector.isSelected(getWorkDir(), aModule.toString(), aModule));
    }

    public void testCanExcludeOSGiWithAttributes() throws Exception {
        Parameter p = new Parameter();
        p.setName("excludeModules");
        p.setValue("org.eclipse.core.jobs");
        selector.setParameters(new Parameter[] { p });
        
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("Bundle-SymbolicName", "org.eclipse.core.jobs; singleton:=true");
        File aModule = generateJar(new String[0], m);
        assertFalse("Refused", selector.isSelected(getWorkDir(), aModule.toString(), aModule));
    }
    
    public void testCanShowOnlyExcludedModules() throws Exception {
        Parameter p = new Parameter();
        p.setName("excluded");
        p.setValue("true");
        Parameter p2 = new Parameter();
        p2.setName("excludeModules");
        p2.setValue("org.my.module");
        selector.setParameters(new Parameter[] { p, p2 });
        
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);
        assertTrue("Now we are accepting only excluded modules", selector.isSelected(getWorkDir(), aModule.toString(), aModule));
    }
    
    public void testIsSelectedForNotAModuleIsStillFalseEvenWeAcceptOnlyExcludedModules() throws IOException {
        Parameter p = new Parameter();
        p.setName("excluded");
        p.setValue("true");
        Parameter p2 = new Parameter();
        p2.setName("excludeModules");
        p2.setValue("org.my.module");
        selector.setParameters(new Parameter[] { p, p2 });
        
        
        File noModule = generateJar(new String[0], createManifest ());
        assertFalse("Not acceptable", selector.isSelected(getWorkDir(), noModule.toString(), noModule));
    }
    
    public void testCanExcludeACluster() throws Exception {
        Parameter p = new Parameter();
        p.setName("includeClusters");
        p.setValue("nonexistent");
        selector.setParameters(new Parameter[] { p });
        
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);
        new File(getWorkDir(), "update_tracking").mkdir();
        assertFalse("Refused", selector.isSelected(getWorkDir().getParentFile(), aModule.toString(), aModule));
    }
    
    public void testWhatItDoesOnADirectory() throws Exception {
        assertFalse("Refused", selector.isSelected(getWorkDir().getParentFile(), getWorkDir().getName(), getWorkDir()));
    }
    
    public void testNoManifest() throws Exception {
        File aModule = generateJar(new String[] { "some/fake/entry.txt" }, null);
        assertFalse("Refused", selector.isSelected(getWorkDir().getParentFile(), aModule.toString(), aModule));
    }

    public void testParsingOfUpdateTrackingFiles() throws Exception {
        doParsingOfUpdateTrackingFiles(1);
    }
    
    public void testParsingOfUpdateTrackingFilesOnMoreDirs() throws Exception {
        doParsingOfUpdateTrackingFiles(2);
    }
    
    
    private void doParsingOfUpdateTrackingFiles(int parents) throws Exception {
        File updateTracking = new File(getWorkDir(), "update-tracking");
        updateTracking.mkdirs();
        assertTrue("Created", updateTracking.isDirectory());
        
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);
        
        File trackingFile = new File(updateTracking, "org-my-module.xml");
        FileWriter w = new FileWriter(trackingFile);
        w.write(
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<module codename='org.apache.tools.ant.module/3'>\n" +
    "<module_version specification_version='3.22' origin='installer' last='true' install_time='1124194231878'>\n" +
        "<file name='ant/bin/ant' crc='1536373800'/>\n" +
        "<file name='ant/bin/ant.bat' crc='3245456472'/>\n" +
        "<file name='ant/bin/ant.cmd' crc='3819623376'/>\n" +
        "<file name='ant/bin/antRun' crc='2103827286'/>\n" +
        "<file name='ant/bin/antRun.bat' crc='2739687679'/>\n" +
        "<file name='ant/bin/antRun.pl' crc='3955456526'/>\n" +
"    </module_version>\n" +
"</module>\n"
        );
        w.close();

        StringBuilder sb = new StringBuilder();
        sb.append(trackingFile.getPath());
        
        while (--parents > 0) {
            File x = new File(getWorkDir(), parents + ".xml");
            FileWriter xw = new FileWriter(x);
            xw.write(
    "<?xml version='1.0' encoding='UTF-8'?>\n" +
    "<module codename='" + x + "/3'>\n" +
        "<module_version specification_version='3.22' origin='installer' last='true' install_time='1124194231878'>\n" +
    "    </module_version>\n" +
    "</module>\n"
            );
            xw.close();
            
            sb.insert(0, File.pathSeparator);
            sb.insert(0, x.getPath());
        }
        
        Parameter p = new Parameter();
        p.setName("updateTrackingFiles");
        p.setValue(sb.toString());
        selector.setParameters(new Parameter[] { p });
        
        assertTrue("module accepted", selector.isSelected(getWorkDir(), aModule.toString(), aModule));
        assertTrue("its file as well", selector.isSelected(getWorkDir(), "ant/bin/ant.bat", new File(aModule.getParent(), "ant/bin/ant.bat")));
        assertTrue("also the tracking file is accepted", selector.isSelected(getWorkDir(), "update-tracking/" + trackingFile.getName(), trackingFile));
    }
    
    private File createNewJarFile() throws IOException {
        int i = 0;
        for (;;) {
            File f = new File (this.getWorkDir(), i++ + ".jar");
            if (!f.exists ()) {
                return f;
            }
        }
    }

    protected final File generateJar (String[] content, Manifest manifest) throws IOException {
        File f = createNewJarFile ();
        
        JarOutputStream os;
        if (manifest != null) {
            os = new JarOutputStream (new FileOutputStream (f), manifest);
        } else {
            os = new JarOutputStream (new FileOutputStream (f));
        }
        
        for (int i = 0; i < content.length; i++) {
            os.putNextEntry(new JarEntry (content[i]));
            os.closeEntry();
        }
        os.closeEntry ();
        os.close();
        
        return f;
    }
    
}

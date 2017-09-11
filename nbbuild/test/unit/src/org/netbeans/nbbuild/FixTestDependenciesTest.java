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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 *
 * @author pzajac
 */
public class FixTestDependenciesTest extends TestBase {

    public FixTestDependenciesTest(String testName) {
        super(testName);
    }

    public void testOpenideUtilTestDepNeedsToBeRecursive() throws IOException, Exception {
        File prjFile = copyFile("FixTestDependencies-openide.filesystems.xml");
        File propertiesFile = new File(getWorkDir(), "empty.properties");
        propertiesFile.createNewFile();
        FixTestDependencies ftd = newFixTestDependencies();
        ftd.setPropertiesFile(propertiesFile);
        ftd.setProjectXml(prjFile);
        ftd.cachedEntries = getEntries();
        ftd.execute();

        String result = readFile(prjFile);
        int first = result.indexOf("test-dependencies");
        if (first == -1) {
            fail("No test deps found in " + result);
        }
        result = result.substring(first);

        if (result.indexOf("org.openide.util") == -1) {
            fail("org.openide.util should be there: " + result);
        }
        if (result.indexOf("org.openide.util.lookup") == -1) {
            fail("org.openide.util.lookup should be there: " + result);
        }
    }
    public void testNoChangeForProjectsWithoutTests() throws IOException, Exception {
        File prjFile = copyFile("FixTestDependencies-o.apache.xml.resolver.xml");
        String before = readFile(prjFile);
        File propertiesFile = new File(getWorkDir(), "some.properties");
        Properties np = new Properties();
        np.put("is.autoload", "true");
        np.store(new FileOutputStream(propertiesFile), "");
        FixTestDependencies ftd = newFixTestDependencies();
        ftd.setPropertiesFile(propertiesFile);
        ftd.setProjectXml(prjFile);
        ftd.cachedEntries = getEntries();
        ftd.execute();

        String result = readFile(prjFile);
        assertEquals("No change expected", before, result);
    }

    public void testSimple() throws IOException, Exception {
          File prjFile = copyFile("FixTestDependenciesProject.xml");
          File propertiesFile = copyFile("FixTestDependencies.properties");
          doFixProjectXml(propertiesFile, prjFile);
          doFixProjectXml(propertiesFile, copyFile("FixTestDependenciesProject2.xml"));
    }
    
    public void testStandalone() throws IOException, Exception {
        File prjFile = copyFile("FixTestDependenciesProjectStandalone.xml");
        File propertiesFile = copyFile("FixTestDependencies.properties");
        FixTestDependencies ftd = newFixTestDependencies();
        ftd.setPropertiesFile(propertiesFile);
        ftd.setProjectXml(prjFile);
        ftd.cachedEntries = getEntries();
        ftd.execute();
        assertFile(copyFile("FixTestDependenciesProjectStandalonePass.xml"),prjFile);
        assertFile(copyFile("FixTestDependenciesPass.properties"),propertiesFile);
    }

    public void testWrongBuilClassDep() throws IOException {
        FixTestDependencies ftd = newFixTestDependencies();
        Set<String> cnb = new HashSet<String>();
        Set<String> testCnb = new HashSet<String>();
 
        Properties props = new Properties();
        String PNAME = "cp.extra";
        String PVALUE = "../build/test/unit/classes";
        props.setProperty(PNAME,PVALUE);
        ftd.readCodeNameBases(cnb, testCnb, props, "cp.extra", Collections.<String>emptySet(), Collections.<ModuleListParser.Entry>emptySet());
        assertEquals("No dependency on module.",0,cnb.size());        
        assertEquals("No test dependency on module.",0,testCnb.size()); 
        assertEquals("property value",PVALUE,props.getProperty(PNAME));
    }

    private FixTestDependencies newFixTestDependencies() throws IOException, BuildException {
        Project project = new Project();
        project.setBaseDir(getWorkDir());
        FixTestDependencies ftd = new FixTestDependencies();
        ftd.setProject(project);
        return ftd;
    }
    private void doFixProjectXml(final File propertiesFile, final File prjFile) throws Exception, IOException {
        FixTestDependencies ftd = newFixTestDependencies();
        ftd.setPropertiesFile(propertiesFile);
        ftd.setProjectXml(prjFile);
        ftd.cachedEntries = getEntries();
        ftd.execute();
        assertFile(copyFile("FixTestDependenciesProjectPass.xml"),prjFile);
        assertFile(copyFile("FixTestDependenciesPass.properties"),propertiesFile);
    }

    private File copyFile(String resourceName) throws IOException {
       InputStream is = getClass().getResourceAsStream(resourceName);
       byte buf[] = new byte[10000];
       File retFile = new File(getWorkDir(),resourceName);
       FileOutputStream fos = new FileOutputStream(retFile);
       int size;
       while ((size = is.read(buf)) > 0 ) {
           fos.write(buf,0,size);
       }
       is.close();
       fos.close();
       return retFile;
    }

    private Set<ModuleListParser.Entry> getEntries() {
        Set<ModuleListParser.Entry> entries = new HashSet<ModuleListParser.Entry>();
        File nonexistent = new File("nonexistent");
        entries.add(new ModuleListParser.Entry("org.openide.io",new File("extra/modules/org-openide-io.jar"),
            new File[0],    null,"openide/io",
            new String[]{"org.openide.util"},
            "extra",
            new String[]{"org.openide.util"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.openide.compat",new File("extra/modules/org-openide-compat.jar"),
            new File[0],    null,"openide/compat",
            new String[]{"org.netbeans.core","org.openide.actions","org.openide.awt","org.openide.dialogs","org.openide.explorer","org.openide.filesystems","org.openide.nodes","org.openide.options","org.openide.text","org.openide.util","org.openide.windows"},
            "extra",
            new String[]{"org.netbeans.core","org.openide.actions","org.openide.awt","org.openide.dialogs","org.openide.explorer","org.openide.filesystems","org.openide.nodes","org.openide.options","org.openide.text","org.openide.util","org.openide.windows"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.netbeans.modules.projectapi",new File("extra/modules/org-netbeans-modules-projectapi.jar"),
            new File[0],    null,"projects/projectapi",
            new String[]{"org.netbeans.modules.queries","org.openide.filesystems","org.openide.util"},
            "extra",
            new String[]{"org.netbeans.modules.queries","org.openide.filesystems","org.openide.util"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.openide.loaders",new File("extra/modules/org-openide-loaders.jar"),
            new File[0],    null,"openide/loaders",
            new String[]{"org.netbeans.api.progress","org.openide.actions","org.openide.awt","org.openide.dialogs","org.openide.explorer","org.openide.filesystems","org.openide.modules","org.openide.nodes","org.openide.text","org.openide.util","org.openide.windows"},
            "extra",
            new String[]{"org.netbeans.api.progress","org.openide.actions","org.openide.awt","org.openide.dialogs","org.openide.explorer","org.openide.filesystems","org.openide.modules","org.openide.nodes","org.openide.text","org.openide.util","org.openide.windows"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.netbeans.core",new File("extra/modules/org-netbeans-core.jar"),
            new File[0],    null,"core",
            new String[]{"org.netbeans.bootstrap","org.netbeans.core.startup","org.netbeans.swing.plaf","org.openide.actions","org.openide.awt","org.openide.dialogs","org.openide.explorer","org.openide.filesystems","org.openide.loaders","org.openide.modules","org.openide.nodes","org.openide.options","org.openide.text","org.openide.util","org.openide.windows"},
            "extra",
            new String[]{"org.netbeans.bootstrap","org.netbeans.core.startup","org.netbeans.swing.plaf","org.openide.actions","org.openide.awt","org.openide.dialogs","org.openide.explorer","org.openide.filesystems","org.openide.loaders","org.openide.modules","org.openide.nodes","org.openide.options","org.openide.text","org.openide.util","org.openide.windows"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.netbeans.modules.masterfs",new File("extra/modules/org-netbeans-modules-masterfs.jar"),
            new File[0],    null,"openide/masterfs",
            new String[]{"org.openide.filesystems","org.openide.util","org.openide.options","org.netbeans.modules.queries"},
            "extra",
            new String[]{"org.openide.filesystems","org.openide.util","org.openide.options","org.netbeans.modules.queries"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.netbeans.bootstrap",new File("extra/lib/boot.jar"),
            new File[0],    null,"core/bootstrap",
            new String[]{"org.openide.modules","org.openide.util"},
            "extra",
            new String[]{"org.openide.modules","org.openide.util"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.netbeans.libs.xerces",new File("extra/modules/org-netbeans-libs-xerces.jar"),
            new File[]{new File("nonsence:/home/pzajac/cvss/freshtrunk/libs/external/xerces-2.8.0.jar"),new File("nonsence:/home/pzajac/cvss/freshtrunk/libs/external/xml-commons-dom-ranges-1.0.b2.jar")},
            null,"libs/xerces",
           new String[0],
            "extra",
           new String[0],
           Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.netbeans.api.progress",new File("extra/modules/org-netbeans-api-progress.jar"),
            new File[0],    null,"core/progress",
            new String[]{"org.openide.util","org.openide.awt"},
            "extra",
            new String[]{"org.openide.util","org.openide.awt"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.openide.options",new File("extra/modules/org-openide-options.jar"),
            new File[0],    null,"openide/options",
            new String[]{"org.openide.util"},
            "extra",
            new String[]{"org.openide.util"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.openide.explorer",new File("extra/modules/org-openide-explorer.jar"),
            new File[0],    null,"openide/explorer",
            new String[]{"org.openide.util","org.openide.nodes","org.openide.awt","org.openide.dialogs","org.openide.options"},
            "extra",
            new String[]{"org.openide.util","org.openide.nodes","org.openide.awt","org.openide.dialogs","org.openide.options"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.openide.dialogs",new File("extra/modules/org-openide-dialogs.jar"),
            new File[0],    null,"openide/dialogs",
            new String[]{"org.netbeans.api.progress","org.openide.awt","org.openide.util"},
            "extra",
            new String[]{"org.netbeans.api.progress","org.openide.awt","org.openide.util"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.openide.nodes",new File("extra/modules/org-openide-nodes.jar"),
            new File[0],    null,"openide/nodes",
            new String[]{"org.openide.util","org.openide.awt","org.openide.dialogs"},
            "extra",
            new String[]{"org.openide.util","org.openide.awt","org.openide.dialogs"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.openide.awt",new File("extra/modules/org-openide-awt.jar"),
            new File[0],    null,"openide/awt",
            new String[]{"org.openide.util"},
            "extra",
            new String[]{"org.openide.util"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.openide.text",new File("extra/modules/org-openide-text.jar"),
            new File[0],    null,"openide/text",
            new String[]{"org.netbeans.modules.editor.mimelookup","org.openide.awt","org.openide.dialogs","org.openide.nodes","org.openide.options","org.openide.util","org.openide.windows"},
            "extra",
            new String[]{"org.netbeans.modules.editor.mimelookup","org.openide.awt","org.openide.dialogs","org.openide.nodes","org.openide.options","org.openide.util","org.openide.windows"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.openide.actions",new File("extra/modules/org-openide-actions.jar"),
            new File[0],    null,"openide/actions",
            new String[]{"org.openide.util","org.openide.nodes","org.openide.awt","org.openide.options","org.openide.text","org.openide.explorer","org.openide.dialogs","org.openide.windows"},
            "extra",
            new String[]{"org.openide.util","org.openide.nodes","org.openide.awt","org.openide.options","org.openide.text","org.openide.explorer","org.openide.dialogs","org.openide.windows"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.openide.util",new File("extra/lib/org-openide-util.jar"),
            new File[0],    null,"openide/util",
           new String[0],
            "extra",
           new String[0],
           Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.netbeans.core.startup",new File("extra/core/core.jar"),
            new File[0],    null,"core/startup",
            new String[]{"org.netbeans.bootstrap","org.openide.filesystems","org.openide.modules","org.openide.util"},
            "extra",
            new String[]{"org.netbeans.bootstrap","org.openide.filesystems","org.openide.modules","org.openide.util"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.openide.modules",new File("extra/lib/org-openide-modules.jar"),
            new File[0],    null,"openide/modules",
            new String[]{"org.openide.util"},
            "extra",
            new String[]{"org.openide.util"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        entries.add(new ModuleListParser.Entry("org.openide.filesystems",new File("extra/core/org-openide-filesystems.jar"),
            new File[0],    null,"openide/fs",
            new String[]{"org.openide.util"},
            "extra",
            new String[]{"org.openide.util"},
            Collections.<String,String[]>emptyMap(), nonexistent));
        return entries;
    } 
}
    

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


/** Check behaviour of ModuleStateSelector.
 *
 * @author Jaroslav Tulach
 */
public class ModuleStateSelectorTest extends TestBase {
    private ModuleStateSelector selector;
    
    public ModuleStateSelectorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        selector = new ModuleStateSelector();
    }

    public void testModuleRejectedIfNotEager() throws Exception {
        File cfg = new File(new File(getWorkDir(), "config"), "Modules");
        cfg.mkdirs();
        assertTrue("Created", cfg.isDirectory());
        
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar("org.my.module", new String[0], m);
        
        File trackingFile = new File(cfg, "org-my-module.xml");
        FileWriter w = new FileWriter(trackingFile);
        w.write(
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"\n" +
"                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">\n" +
"<module name=\"org.my.module\">\n" +
"    <param name=\"autoload\">true</param>\n" +
"    <param name=\"eager\">false</param>\n" +
"    <param name=\"jar\">modules/org-openide-awt.jar</param>\n" +
"    <param name=\"reloadable\">false</param>\n" +
"    <param name=\"specversion\">7.4.0.1</param>\n" +
"</module>\n"
        );
        w.close();

        Parameter p = new Parameter();
        p.setName("acceptEager");
        p.setValue("true");
        Parameter p2 = new Parameter();
        p2.setName("acceptAutoload");
        p2.setValue("false");
        selector.setParameters(new Parameter[] { p, p2 });
        
        assertFalse("module not accepted", selector.isSelected(getWorkDir(), aModule.toString(), aModule));
    }
    
    public void testModuleAcceptedIfEager() throws Exception {
        File cfg = new File(new File(getWorkDir(), "config"), "Modules");
        cfg.mkdirs();
        assertTrue("Created", cfg.isDirectory());

        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module/1");
        File aModule = generateJar("org.my.module", new String[0], m);

        File trackingFile = new File(cfg, "org-my-module.xml");
        FileWriter w = new FileWriter(trackingFile);
        w.write(
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"\n" +
"                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">\n" +
"<module name=\"org.my.module\">\n" +
"    <param name=\"autoload\">false</param>\n" +
"    <param name=\"eager\">true</param>\n" +
"    <param name=\"jar\">modules/org-openide-awt.jar</param>\n" +
"    <param name=\"reloadable\">false</param>\n" +
"    <param name=\"specversion\">7.4.0.1</param>\n" +
"</module>\n"
        );
        w.close();

        Parameter p = new Parameter();
        p.setName("acceptEager");
        p.setValue("true");
        Parameter p2 = new Parameter();
        p2.setName("acceptAutoload");
        p2.setValue("false");
        selector.setParameters(new Parameter[] { p, p2 });

        assertTrue("module accepted", selector.isSelected(getWorkDir(), aModule.toString(), aModule));
    }
    
    private File createNewJarFile (String cnb) throws IOException {
        File f = new File (new File(this.getWorkDir(), "modules"), cnb.replace('.', '-') + ".jar");
        f.delete();
        f.deleteOnExit();
        return f;
    }

    protected final File generateJar (String cnb, String[] content, Manifest manifest) throws IOException {
        File f = createNewJarFile (cnb);
        f.getParentFile().mkdirs();
        
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

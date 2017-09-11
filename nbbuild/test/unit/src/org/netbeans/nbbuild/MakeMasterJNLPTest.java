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
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/** Is generation of Jnlp files correct?
 *
 * @author Jaroslav Tulach
 */
public class MakeMasterJNLPTest extends TestBase {
    public MakeMasterJNLPTest (String name) {
        super (name);
    }
    
    public void testOSGiModule() throws Exception {
        int cnt = 3;
        Manifest m;

        m = createManifest ();
        m.getMainAttributes ().putValue ("Bundle-SymbolicName", "org.my.module");
        File simpleJar = generateJar (new String[0], m);

        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.second.module/3");
        File secondJar = generateJar (new String[0], m);

        File parent = simpleJar.getParentFile ();
        assertEquals("They are in the same folder", parent, secondJar.getParentFile());

        File output = new File(parent, "output");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeMasterJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" +
            "  <jnlp dir='" + output + "'  >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "      <include name='" + secondJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        while (cnt-- > 0) {
            execute (f, new String[] { "-verbose" });
        }

        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());

        String[] files = output.list();
        assertEquals("It has two files", 2, files.length);

        java.util.Arrays.sort(files);

        assertEquals("The res1 file: " + files[0], "org-my-module.ref", files[0]);
        assertEquals("The res2 file: "+ files[1], "org-second-module.ref", files[1]);

        File r1 = new File(output, "org-my-module.ref");
        String res1 = readFile (r1);

        File r2 = new File(output, "org-second-module.ref");
        String res2 = readFile (r2);

        assertExt(res1, "org.my.module");
        assertExt(res2, "org.second.module");
    }
    
    public void testGenerateReferenceFilesOnce() throws Exception {
        doGenerateReferenceFiles(1);
    }
    public void testGenerateReferenceFilesThrice() throws Exception {
        doGenerateReferenceFiles(3);
    }
    
    private void doGenerateReferenceFiles(int cnt) throws Exception {
        Manifest m;
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        File simpleJar = generateJar (new String[0], m);

        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.second.module/3");
        File secondJar = generateJar (new String[0], m);
        
        File parent = simpleJar.getParentFile ();
        assertEquals("They are in the same folder", parent, secondJar.getParentFile());
        
        File output = new File(parent, "output");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeMasterJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "'  >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "      <include name='" + secondJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        while (cnt-- > 0) {
            execute (f, new String[] { "-verbose" });
        }
        
        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());
        
        String[] files = output.list();
        assertEquals("It has two files", 2, files.length);

        java.util.Arrays.sort(files);
        
        assertEquals("The res1 file: " + files[0], "org-my-module.ref", files[0]);
        assertEquals("The res2 file: "+ files[1], "org-second-module.ref", files[1]);
        
        File r1 = new File(output, "org-my-module.ref");
        String res1 = readFile (r1);

        File r2 = new File(output, "org-second-module.ref");
        String res2 = readFile (r2);
        
        assertExt(res1, "org.my.module");
        assertExt(res2, "org.second.module");
    }
    
    private static void assertExt(String res, String module) {
        int ext = res.indexOf("<extension");
        if (ext == -1) {
            fail ("<extension tag shall start there: " + res);
        }
        
        assertEquals("Just one extension tag", -1, res.indexOf("<extension", ext + 1));

        int cnb = res.indexOf(module);
        if (cnb == -1) {
            fail("Cnb has to be there: " + module + " but is " + res);
        }
        assertEquals("Just one cnb", -1, res.indexOf(module, cnb + 1));
        
        String dashcnb = module.replace('.', '-');
        
        int dcnb = res.indexOf(dashcnb);
        if (dcnb == -1) {
            fail("Dash Cnb has to be there: " + dashcnb + " but is " + res);
        }
        assertEquals("Just one dash cnb", -1, res.indexOf(dashcnb, dcnb + 1));
    }

    private File createNewJarFile() throws IOException {
        int i = 0;
        for (;;) {
            File f = new File (this.getWorkDir(), i++ + ".jar");
            if (!f.exists()) {
                return f;
            }
        }
    }
    
    protected final File generateJar (String[] content, Manifest manifest) throws IOException {
        File f = createNewJarFile ();
        
        JarOutputStream os = new JarOutputStream (new FileOutputStream (f), manifest);
        
        for (int i = 0; i < content.length; i++) {
            os.putNextEntry(new JarEntry (content[i]));
            os.closeEntry();
        }
        os.closeEntry ();
        os.close();
        
        return f;
    }

}

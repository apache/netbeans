//This class is automatically generated - DO NOT MODIFY (ever)
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
package org.netbeans.test.java.editor.completion;
import java.io.BufferedReader;
import java.io.PrintWriter;
import org.netbeans.junit.NbTestCase;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.test.java.editor.lib.LineDiff;

/**This class is automatically generated from <I>config.txt</I> using bash
 * script <I>create</I>. For any changes, change the code generating script
 * and re-generate.
 *
 * Althought this class is runned as a test, there is no real code. This class
 * is only wrapper between xtest and harness independet test code. Main information
 * source is <B>CompletionTest</B> class ({@link CompletionTest}).
 *
 * @see CompletionTest
 */
public class CompletionTestPerformer extends JellyTestCase {
    
    
    // automatic generation of golden files
    protected boolean generateGoledFiles = false;
    
    protected PrintWriter outputWriter  = null;
    
    protected PrintWriter logWriter = null;
    
    
    private static CompletionTestPerformer instance;
    
    public static void openProject(String name) {
        try {
            instance.openDataProjects(name);
        } catch (IOException ex) {
            fail("Project cannot be opened");
        }
    }
    /** Need to be defined because of JUnit */
    public CompletionTestPerformer(String name) {
        super(name);
        instance = this;
    }
    
    protected void setUp() {
        log("CompletionTestPerformer.setUp started.");
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        log("CompletionTestPerformer.setUp finished.");
        log("Test "+getName()+  "started");
    }
    
    
    protected void tearDown() throws Exception{
        log("Test "+getName()+" finished");
        log("CompletionTestPerformer.tearDown");
        outputWriter.flush();        
        String goldenName = getJDKVersionCode() + "-" + getName() + ".pass";        
        File ref = new File(getWorkDir(), this.getName() + ".ref");
        if(generateGoledFiles) {
            BufferedReader br = null;
            FileWriter fw = null;
            try {
                String newGoldenName = "data/goldenfiles/"+this.getClass().getName().replace('.', '/')+ "/" + goldenName;
                File newGolden = new File(getDataDir().getParentFile().getParentFile().getParentFile(),newGoldenName);
                newGolden.getParentFile().mkdirs();
                br = new BufferedReader(new FileReader(ref));
                fw = new FileWriter(newGolden);
                getLog().println("Creating golden file "+newGolden.getName()+" in "+newGolden.getAbsolutePath());
                String s;
                while((s=br.readLine())!=null) fw.write(s+"\n");
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            } finally {
                try {
                    if(fw!=null) fw.close();
                    if(br!=null) br.close();
                } catch (IOException ioe) {
                    fail(ioe.getMessage());
                }
            }
            fail("Generating golden files");            
        }
        File golden =  getGoldenFile(goldenName);
        File diff = new File(getWorkDir(), this.getName() + ".diff");
        logWriter.flush();
        assertFile("Output does not match golden file.", golden, ref, diff, new LineDiff(false));
        
    }
    private String getJDKVersionCode() {
        String specVersion = System.getProperty("java.version");
        
        if (specVersion.startsWith("1.7"))
            return "jdk17";
        
        if (specVersion.startsWith("1.8"))
            return "jdk18";
        
        throw new IllegalStateException("Specification version: " + specVersion + " not recognized.");
    }
       
}

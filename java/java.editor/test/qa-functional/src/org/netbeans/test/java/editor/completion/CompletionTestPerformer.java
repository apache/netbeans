//This class is automatically generated - DO NOT MODIFY (ever)
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

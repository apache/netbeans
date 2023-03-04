/*
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

package org.netbeans.test.java;

import java.io.*;
import java.util.ArrayList;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.diff.LineDiff;


/** LogTestCase
 * @author Jan Becicka
 */
public class LogTestCase extends JavaTestCase {

    /**
     * state - true - testing
     *       - false - generating goldenfiles
     */
    public static boolean CREATE_GOLDENFILES=false;

    static {
        if (System.getProperty("create.goldenfiles") != null && System.getProperty("create.goldenfiles").equals("true")) {
            CREATE_GOLDENFILES=true;
        }
    }
    
    /** directory, where the golden and .diff files resides
     */
    protected File classPathWorkDir;
    /** test will generate this file
     */
    protected File refFile;
    
    protected PrintWriter log = null;
    protected PrintWriter ref = null;
    protected PrintWriter golden = null;
    
    public LogTestCase(java.lang.String testName) {
        super(testName);
    }
    
    /** sets the PrintWriters
     */
    protected void setUp() {
        prepareProject();
        try {
            //logs and refs
            refFile = new File(getWorkDir(), getName() + ".ref");
            File logFile = new File(getWorkDir(), getName() + ".log");
            ref = new PrintWriter(new BufferedWriter(new FileWriter(refFile)));
            log = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
            openDefaultProject();
            if (CREATE_GOLDENFILES) { //generates golden files
                File f;
                //generate goldenfile name
                f=getDataDir();
                ArrayList names=new ArrayList();
                names.add("goldenfiles");
                while (!f.getName().equals("test")) {
                    if (!f.getName().equals("sys") && !f.getName().equals("work") &&!f.getName().equals("tests")) {
                        names.add(f.getName());
                    }
                    f=f.getParentFile();
                }
                for (int i=names.size()-1;i > -1;i--) {
                    f=new File(f,(String)(names.get(i)));
                }
                f=new File(f, getClass().getName().replace('.', File.separatorChar));
                f=new File(f, getName()+".pass");
                if (!f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
                golden=new PrintWriter(new BufferedWriter(new FileWriter(f)));
                log("Passive mode: generate golden file into "+f.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.toString(), false);
        }
    }
    
    public void prepareProject() {//default - override for another projects
        classPathWorkDir=new File(getDataDir(), "projects.default.src".replace('.', File.separatorChar));
    }
    
    public void log(String s) {
        log.println(s);
    }
    
    public void log(Object o) {
        log.println(o);
    }
    
    public void ref(String s) {
        ref.println(s);
        if (CREATE_GOLDENFILES) {
            golden.println(s);
        }
    }
    
    public void ref(Object o) {
        ref.println(o.toString());
        if (CREATE_GOLDENFILES) {
            golden.println(o.toString());
        }
    }
    
    public void ref(File file) {
        try {
            BufferedReader br=new BufferedReader(new FileReader(file));
            String line;
            while ((line=br.readLine()) != null) {
                ref(line);
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    /** sets the PrintWriters
     */
    protected void tearDown() {
        ref.close();
        log.close();
        if (CREATE_GOLDENFILES && golden != null) {
            golden.close();
            assertTrue("Passive mode", false);
        } else {
            try {
                assertFile("Golden file differs ", refFile, getGoldenFile(), getWorkDir(), new LineDiff());
            } catch (Exception ex) {
                ex.printStackTrace();
                assertTrue(ex.toString(), false);
            }
        }
    }
}

